# -*- coding: utf-8 -*-
#python lib imports
from datetime import datetime
import logging
import hashlib
#django imports
from django.utils.text import slugify
from django.utils.functional import cached_property
from django.db import models
from django.core.validators import MinValueValidator, MaxValueValidator, MinLengthValidator
from django.shortcuts import get_object_or_404
from django.core.exceptions import ValidationError
from django.core.urlresolvers import reverse

#bundled_matching imports
from bundled_matching.util import keys
from bundled_matching.datastructures.matching_assignment import MatchingAssignment
from bundled_matching.datastructures.matching_instance import MatchingInstance
from bundled_matching.datastructures.preference_list import PreferenceList

#app imports
from app.models.tum_user import TUMUser
import sys
import json

MAX_LENGTH = 256
ID_LENGTH = 7

MAX_SELECTED_PARTICIPANTS_Q = 99
def _validate_id(value):
    if not Instance.is_valid_id(value):
        raise ValidationError(u'%s is not a valid ID' % value)


def _generate_id():
    return Instance.generate_id()


class Instance(models.Model):
    BRSD = 'BRSD'
    BPS = 'BPS'
    ALGORITHM_TYPES = (
                (BRSD,BRSD),
                (BPS,BPS),
    )
    id = models.CharField(max_length=ID_LENGTH, blank=False, primary_key=True, db_index=True, editable=False, default=_generate_id, validators=[_validate_id])
    m_admins = models.ManyToManyField(TUMUser, db_table='b_matching_admins')
    title = models.CharField(max_length=MAX_LENGTH, blank=False, verbose_name='Title') #Minlength validator?
    description = models.TextField(blank=True, verbose_name='Description')
    begin = models.DateTimeField(blank=False, auto_now_add=False, auto_now=False, verbose_name='Begin of voting')
    end_stud = models.DateTimeField(blank=False, auto_now_add=False, auto_now=False, verbose_name='End of voting for students')
    end_instruc = models.DateTimeField(blank=False, auto_now_add=False, auto_now=False, verbose_name='End of voting for instructors')
    assignment_published = models.BooleanField(blank=False, default=False, verbose_name='Assignment published')
    is_open = models.BooleanField(blank=False, default=True, verbose_name='Open', help_text='Is it open?')
    visible = models.BooleanField(blank=False, default=False, verbose_name='Visible', help_text='Visible on front page?')
    gap_time = models.BooleanField(blank=False, default=True, verbose_name='Gap Time', help_text='Gap time between lectures allowed?')
    generated_bundles = models.PositiveIntegerField(blank=False, verbose_name='Number of generated bundles', default=100, validators=[MinValueValidator(1)])
    visible_bundles = models.PositiveIntegerField(blank=False, verbose_name='Number of visible bundles', default=30, validators=[MinValueValidator(1)])
    epsilon = models.FloatField(blank=False, verbose_name='Epsilon', default=2.0, validators=[MinValueValidator (1)])
    distribution = models.PositiveIntegerField(blank=False, verbose_name='Distribution of epsilon(a)', default=2, validators=[MinValueValidator(0.0)])
    tutor_matching = models.BooleanField(blank=False, default=False, verbose_name='Matching for Tutors', help_text='Matching for Tutors?')
    algorithm_type = models.CharField(max_length=128, blank=True, editable=True, choices=ALGORITHM_TYPES, verbose_name='Which algorithm should be used?',default=BPS)

    def __str__(self):
        return self.title

    @property
    def slugified_title(self):
        return slugify(self.title)

    class Meta:
        app_label = 'bundled_matching'
        verbose_name = 'matching instance'
        db_table = 'instance'

    @cached_property
    def stud_votes(self):
        from bundled_matching.models.m_stud_vote import M_StudVote
        return list(M_StudVote.objects.filter(matching=self).select_related('user','matching'))
        # return list(self.b_m_stud_votes.select_related('user').all())

    # @property
    # def empty_students(self):
        # from bundled_matching.models.preference_list import PreferenceList as PL
        # empty_students = [student for student in self.stud_votes if not PL.objects.filter(stud_vote=student).exists()]
        # return empty_students

    @cached_property
    def course_votes(self):
        from bundled_matching.models.m_course_vote import M_CourseVote
        return list(M_CourseVote.objects.filter(matching=self))

    @property
    def matched_pairs(self):
        return self.pairs.all()

    def clean_matching(self):
        from bundled_matching.models.preference_list import PreferenceList as PL
        self.pairs.all().delete()
        for course, groups in self.groups_as_dict.iteritems():
            for group in groups:
                group.assigned_capacity = 0
                group.save()
        for timeslot in self.all_timeslots:
            timeslot.assigned_capacity = 0
            timeslot.save()
        studs = self.stud_votes
        #TODO nicht pl sondern preference backup löschen bei jedem studenten
        for student in self.stud_votes:
            student.preferences_backup = ""

    @property
    def number_students(self):
        from bundled_matching.models.m_stud_vote import M_StudVote
        number = M_StudVote.objects.filter(matching=self).count()
        return number

    @property
    def light_students(self):
        from bundled_matching.models.m_stud_vote import M_StudVote
        students = M_StudVote.objects.raw("select id, user_id, student_number from m_stud_vote where matching_id = %s ",  [self.id])
        studs = list(students)
        return studs

    def has_filter(self,course_list):
        for course in course_list:
            for gr in self.groups_as_dict[course]:
                if gr.is_english:
                    return True
        return False

    @cached_property
    def all_timeslots(self):
        timeslot_list = list()
        for course in self.course_votes:
            timeslot_list.extend(course.timeslots)
        return timeslot_list

    @cached_property
    def groups_as_dict(self):
        group_course = dict()
        for course in self.course_votes:
            group_course[course] = course.groups
        return group_course

    @cached_property
    def timeslots_as_dict(self):
        ts_course = dict()
        for course in self.course_votes:
            ts_course[course] = course.timeslots
        return ts_course

    @cached_property
    def course_timeslots_as_dict(self):
        ts_course = dict()
        for course in self.course_votes:
            ts_course[course] = course.constraint_timeslots
        return ts_course

    @property
    def matching_instance(self):
        from bundled_matching.models.m_stud_vote import M_StudVote
        stud_dict = {}
        course_dict = {}
        # studs = M_StudVote.objects.filter(matching=self).select_related('user','matching')
        for x in self.stud_votes:
            stud_dict[x] = x.preferences
        for y in self.course_votes:
            course_dict[y] = y.timeslots
        matching_instance = MatchingInstance(students_to_prefs = stud_dict, courses_to_timeslots = course_dict)
        return matching_instance

    @property
    def assignment_metric(self):
        from bundled_matching.metrics.assignment_metric import AssignmentMetric
        from bundled_matching.models.pair import Pair
        return AssignmentMetric(self.matching_instance,list(Pair.objects.filter(matching=self).select_related('student').prefetch_related('groups','timeslot')))

    @cached_property
    def matching_assignment(self):
        matching_assignment = MatchingAssignment(list(self.pairs.all()),self.matching_instance)
        return matching_assignment

    def relevant_stud_votes(self, q_vote):
        relevant_stud_votes = []
        for stud_vote in self.stud_votes:
            if stud_vote.preferences.is_acceptable(q_vote.id):
                relevant_stud_votes.append(stud_vote)
        return relevant_stud_votes

    def get_groups(self):
        from bundled_matching.models.m_course_vote import M_CourseVote
        from bundled_matching.models.group import Group
        courses = M_CourseVote.objects.filter(matching=self)
        groups = Group.objects.filter(course__in=courses)
        return groups

    def get_total_capacity_course(self):
        from django.db.models import Sum
        groups = self.get_groups()
        if not groups.exists():
            return 0
        sum_tot_capa = groups.aggregate(Sum('capacity')).values()[0]
        return sum_tot_capa

    @cached_property
    def admins(self):
        return self.m_admins.all()

    @property
    def assignment(self):
        """ Assignment as dict """
        from bundled_matching.models.pair import Pair
        matched_pairs = list(Pair.objects.filter(matching=self).select_related('student','student__matching').prefetch_related('groups'))
        assignment_dict = dict()
        for pair in matched_pairs:
             assignment_dict[pair.student] = list(pair.groups.all())
        return assignment_dict

    @property
    def is_assigned(self):
        from bundled_matching.models.pair import Pair
        return Pair.objects.filter(matching=self).exists()

    @cached_property
    def course_assignment(self):
        from bundled_matching.models.pair import Pair
        from bundled_matching.models.m_course_vote import M_CourseVote
        course_assignment = dict()
        for course in self.course_votes:
            course_assignment[course] = dict()
            for ts in course.timeslots:
                for group in ts.groups:
                    course_assignment[course][group] = list()
        matched_pairs = list(Pair.objects.filter(matching=self).select_related('student','student__matching','student__user').prefetch_related('groups','groups__course'))
        for pair in matched_pairs:
            for group in pair.groups.all():
                course_assignment[group.course][group].append(pair.student)
        return course_assignment

    def get_assignment_for_stud(self, stud):
        return self.assignment.get(stud)
#
    def get_assignment_for_course(self, course):
        return self.course_assignment.get(course)

    def get_unassigned_groups(self,courses=list()):
        courses_list = courses
        if not courses:
            courses_list = self.course_votes
        courses_groups = dict()
        for course in courses_list:
            courses_groups[course] = course.groups
            #muss nach unassigned suchen
        return courses_groups

    def get_unassigned_students(self):
        studs = self.assignment.keys()
        unassigned = [x for x in self.stud_votes if x not in studs]
        return unassigned

    @property
    def free_ts_dict(self):
        ts_dict = self.timeslots_as_dict
        free_ts_dict = dict()
        for course in ts_dict:
            free_ts_dict[course] = list()
            for ts in ts_dict[course]:
                if ts.has_free_cap():
                    free_ts_dict[course].append(ts)
        return free_ts_dict

    def match_unmatched(self,matching_round = 2):
        from bundled_matching.models.pair import Pair
        unmatched_studs = self.get_unassigned_students()
        studs_to_prefs = dict()
        free_ts_dict = self.free_ts_dict
        timeslots = [ts for course in free_ts_dict for ts in free_ts_dict[course]]
        ts_assigned = {ts:ts.assigned_capacity for ts in timeslots}
        ts_capacity = {ts:ts.capacity for ts in timeslots}
        for stud in unmatched_studs:
            studs_to_prefs[stud] = stud.create_second_preflist(free_ts_dict,ts_assigned,ts_capacity, matching_round)
        matching_instance = MatchingInstance(students_to_prefs = studs_to_prefs, courses_to_timeslots = free_ts_dict)
        self.match_brsd(matching_instance)
        return studs_to_prefs

    def match_brsd(self,matching_instance):
        from bundled_matching.algorithms.bundled_random_serial_dictatorship import BundledRandomSerialDictatorship
        brsd = BundledRandomSerialDictatorship(matching_instance)
        brsd_assignment = brsd.get_assignment()
        assignment = MatchingAssignment(pairs = brsd_assignment, matching_instance = self.matching_instance, instance = self)
        assignment.pair_students()
        self.save()

    def __get_total_capacity(self, votes):
        total_capacity = 0
        for vote in votes:
            total_capacity += vote.capacity
        return total_capacity

    @classmethod
    def get_stud_name(cls):
        return 'Students'

    @classmethod
    def get_course_name(cls):
        return 'Courses'

    def __eq_ts_group(self,ts,group):
        #auslagern in ts oder group
        if not ts.course is group.course:
            return False
        if not ts.day == group.day:
            return False
        if not ts.begin == group.begin:
            return False
        if not ts.end == group.end:
            return False
        if not ts.is_english == group.is_english:
            return False
        return True

    def create_timeslots(self):
        from bundled_matching.models.timeslot import TimeSlot
        ts_list = self.all_timeslots
        for course, group_list in self.groups_as_dict.items():
            for group in group_list:
                if ts_list:
                    timeslot_found = False
                    for ts in ts_list:
                        if self.__eq_ts_group(ts=ts, group = group):
                            group.timeslot = ts
                            timeslot_found = True
                    if not timeslot_found:
                        new_timeslot = TimeSlot(day = group.day, begin = group.begin, end = group.end, course = group.course, is_english = group.is_english ,course_type = 'Tutorial')
                        new_timeslot.save()
                        ts_list.append(new_timeslot)
                        group.timeslot = new_timeslot
                else:
                    new_timeslot = TimeSlot(day = group.day, begin = group.begin, end = group.end, course = group.course, course_type = 'Tutorial')
                    new_timeslot.save()
                    ts_list.append(new_timeslot)
                    group.timeslot = new_timeslot
                group.save()

    def get_timeslots(self):
        timeslots = []
        for c in self.course_votes:
            timeslots.append(c.timeslots)
        return timeslots

    def check_read_permission(self, user):
        return True

    def check_admin_read_permission(self, user):
        return (user in self.admins) or user.is_superuser

    def check_write_permission(self, user):
        return (user in self.admins) or user.is_superuser

    @classmethod
    def check_create_permission(cls, user):
        return user.is_superuser

    def get_unique_stud_vote_or_none(self, user):
        from bundled_matching.models.m_stud_vote import M_StudVote
        try:
            return self.b_m_stud_votes.get(user=user)
        except M_StudVote.DoesNotExist:
            return None

    def get_course_votes_or_empty_list(self, user):
        from bundled_matching.models.m_course_vote import M_CourseVote
        return list(M_CourseVote.objects.filter(course_admins=user,matching=self))
        # if user in self.admins:
            # return self.course_votes
        #return self.course_votes.filter(course_admins=user)


    #TimeCheckStudStart
    def is_before_voting_period_stud(self):
        return self.begin and (self.begin > datetime.now())

    def is_after_voting_period_stud(self):
        return self.end_stud and (self.end_stud < datetime.now())

    def is_in_voting_period_stud(self):
        return not (self.is_before_voting_period_stud() or self.is_after_voting_period_stud())

    def is_open_now_stud(self):
        return self.is_open and self.is_in_voting_period_stud()

    def has_ended_stud(self):
        return self.is_after_voting_period_stud() or (not self.is_open)
    #TimeCheckStudEnd

    #TimeCheckInstrucStart
    def is_before_voting_period_instuc(self):
        return self.is_before_voting_period_stud

    def is_after_voting_period_instruc(self):
        return self.end_instruc and (self.end_instruc < datetime.now())

    def is_in_voting_period_instruc(self):
        return not (self.is_before_voting_period() or self.is_after_voting_period_instruc())

    def is_open_now_instruc(self):
        return self.is_open and self.is_in_voting_period_instruc()

    def has_ended_instruc(self):
        return self.is_after_voting_period_instruc() or (not self.is_open)
    #TimeCheckInstrucEnd

    def is_before_voting_period(self):
        before_begin = False
        if self.begin:
            before_begin = (datetime.now() < self.begin)
        return before_begin

    def is_after_voting_period(self):
        after_end = False
        if self.end_stud and self.end_instruc:
            after_end = (datetime.now() > max(self.end_stud, self.end_instruc))
        elif self.end_stud:
            after_end = (datetime.now() > self.end_stud)
        elif self.end_instruc:
            after_end = (datetime.now() > self.end_instruc)
        return after_end

    def is_after_voting_period_instruc(self):
        if self.end_instruc:
            return (datetime.now() > self.end_instruc)
        return False

    def is_in_voting_period(self):
        return not (self.is_before_voting_period() or self.is_after_voting_period())

    def is_open_now(self):
        return self.is_open and self.is_in_voting_period()

    def has_ended(self):
        return self.is_after_voting_period() or (not self.is_open)

    def get_absolute_url(self):
        return reverse('instance.view.with_slug', kwargs={'pk': str(self.id), 'slug': self.slugified_title})

    def get_absolute_admin_url(self):
        return reverse('instance.view.admin', kwargs={'pk': str(self.id)})

    def get_absolute_edit_url(self):
        return reverse('instance.edit', kwargs={'pk': str(self.id)})

    def get_absolute_open_url(self):
        return reverse('instance.open', kwargs={'pk': str(self.id)})

    def get_absolute_close_url(self):
        return reverse('instance.close', kwargs={'pk': str(self.id)})

    def get_absolute_delete_url(self):
        return reverse('instance.delete', kwargs={'pk': str(self.id)})

    def get_absolute_new_stud_vote_url(self):
        if self.tutor_matching:
            return reverse('instance.tut_vote.new', kwargs={'pk': str(self.id)})
        else:
            return reverse('instance.stud_vote.new', kwargs={'pk': str(self.id)})

    def get_absolute_match_url(self):
        return reverse('instance.match', kwargs={'pk': str(self.id)})

    def get_absolute_match_unmatched_url(self):
        return reverse('instance.match_unmatched', kwargs={'pk': str(self.id)})

    def get_absolute_view_matching_url(self):
        return reverse('instance.view_matching', kwargs={'pk': str(self.id)})

    def get_absolute_publish_matching_url(self):
        return reverse('instance.publish_matching', kwargs={'pk': str(self.id)})

    def get_absolute_export_matching_url(self):
        return reverse('instance.export_matching', kwargs={'pk': str(self.id)})

    def get_absolute_export_assignment_statistics_url(self):
        return reverse('instance.export_matching_statistics', kwargs={'pk': str(self.id)})

    def get_absolute_export_preferences_url(self):
        return reverse('instance.export_preferences', kwargs={'pk': str(self.id)})


    @classmethod
    def get_matching_or_abort(cls, id):
        return get_object_or_404(Instance, pk=id)


    def match(self):
        if self.tutor_matching:
            self.match_brsd(self.matching_instance)
        else:
            if self.algorithm_type == self.BPS:
                self.match_bps(self.matching_instance)
            else:
                self.match_brsd(self.matching_instance)

    def match_bps(self,matching_instance):
        from bundled_matching.algorithms.bundled_probabilistic_serial import BundledProbabilisticSerial
        from bundled_matching.algorithms.dmatrix_generator import DMatrixGenerator
        from bundled_matching.models.pair import Pair
        matching_algo = BundledProbabilisticSerial(matching_instance)
        bps = matching_algo.get_cleaned_result()
        dm = DMatrixGenerator(bps,self)
        dm.create_index_matrix()
        dm.init_java_gateway()
        pairs = dm.get_assignment()
        assignment = MatchingAssignment(pairs = pairs, matching_instance = matching_instance, instance = self)
        assignment.pair_students()
        self.save()
        dm.close()

    def __unicode__(self):
        return self.title

    @classmethod
    def is_valid_id(cls, id_string):
        if id_string:
            if len(id_string) == ID_LENGTH:
                return True
        return False

    @classmethod
    def generate_id(cls):
        return keys.get_random_string(ID_LENGTH)

    @classmethod
    def get_visible_matchings(cls):
        return Instance.objects.filter(visible=True)

    @classmethod
    def get_all_matchings(cls):
        return Instance.objects.all()
