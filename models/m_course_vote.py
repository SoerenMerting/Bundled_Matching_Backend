#Aykut Uzunoglu
#django imports
from django.db import models
from django.shortcuts import get_object_or_404
from django.http import Http404
from django.core.urlresolvers import reverse
from django.core.validators import MinLengthValidator
from django.utils.functional import cached_property
#bundled_matching imports
from bundled_matching.models.m_vote import BMVote
from bundled_matching.models.instance_m import Instance, MAX_LENGTH
from bundled_matching.util.tum_webservice_reader import get_course_view_url
#app imports
from app.models.tum_user import TUMUser

import logging
import sys  # import sys package, if not already imported

reload(sys)
sys.setdefaultencoding('utf-8')

class M_CourseVote(BMVote):
    name = models.CharField(max_length=MAX_LENGTH, blank=False, verbose_name='Name', validators=[MinLengthValidator(2)])
    description = models.TextField(blank=True, verbose_name='Description')
    matching = models.ForeignKey(Instance, related_name='b_m_course_votes', on_delete=models.CASCADE, db_index=True, editable=False)
    course_admins = models.ManyToManyField(TUMUser, db_table='b_m_course_course_admins')
    short_name = models.TextField(blank=True, verbose_name = 'Short Name')
    course_id = models.CharField(max_length=16, blank=True, editable=False, verbose_name='TUMonline courseID')
    chair = models.CharField(max_length=256, blank=True, verbose_name='Chair')
    tos_bsc = models.BooleanField(default=True, verbose_name='B.Sc.')
    tos_msc = models.BooleanField(default=True, verbose_name='M.Sc.')


    class Meta:
        app_label = 'bundled_matching'
        db_table = 'm_course_vote'
        ordering = ['matching']
        verbose_name = 'course'

    def __str__(self):
        output = ""
        if self.short_name:
            output = self.short_name
        else:
            output = self.name
        return str(output)

    @cached_property
    def admins(self):
        return self.course_admins.all()

    @cached_property
    def timeslots(self):
        return list(self.b_m_course_timeslot.filter(course_type = 'Tutorial'))

    @cached_property
    def constraint_timeslots(self):
        return list(self.b_m_course_timeslot.filter(course_type = 'Course'))

    @cached_property
    def groups(self):
        return self.b_m_course_group.all()

    @property
    def label_name(self):
        output = ""
        if self.short_name:
            output = self.short_name
        else:
            output = self.name
        return str(output)

    @property
    def capacity(self):
        from django.db.models import Sum
        from bundled_matching.models.group import Group
        sum_capacity = Group.objects.filter(course = self).aggregate(Sum('capacity')).values()[0]
        return sum_capacity

    @property
    def tags(self):
        bsc = 'bsc' if self.tos_bsc else ''
        msc = 'msc' if self.tos_msc else ''

        return bsc + ',' + msc

    @classmethod
    def get_filters(cls):
        filters = [
            ('bsc', 'B.Sc.'),
            ('msc', 'M.Sc.')
        ]
        return filters

    def __unicode__(self):
        return self.name + ' - ' + unicode(self.matching)

    def get_amount_voting_students(self):
        from bundled_matching.models.preference_list import PreferenceList as PL
        count = PL.objects.distinct('stud_vote').filter(bundle__course=self).count()
        return count

    def get_absolute_url(self):
        return reverse('instance.course_vote.view', kwargs={'pk': str(self.matching.id), 'v_pk': str(self.id)})

    def get_absolute_edit_url(self):
        return reverse('instance.course_vote.edit', kwargs={'pk': str(self.matching.id), 'v_pk': str(self.id)})

    def get_absolute_matching_export_url(self):
        return reverse('instance.course_vote.export_matching', kwargs={'pk': str(self.matching.id), 'v_pk': str(self.id)})


    def get_tumonline_course_view_url(self):
        if self.course_id:
            return get_course_view_url(self.course_id)
        else:
            return None


    def total_number_of_items(self):
        return len(self.matching.course_votes)

    def get_value_label_tags_dict(self):
        return self._get_value_label_tags_dict(self.matching.relevant_stud_votes(self))


    def check_read_permission(self, user):
        return True

    def check_admin_read_permission(self, user):
        return (user in self.admins) or (user in self.matching.admins) or user.is_superuser

    def check_write_permission(self, user):
        return (user in self.admins) or (user in self.matching.admins) or user.is_superuser

    def check_superuser_permission(self, user):
        return (user in self.matching.admins) or user.is_superuser


    @property
    def assigned_items(self):
        return self.matching.get_assignment_for_course(self)


    @classmethod
    def get_course_vote_or_abort(cls, id, matching_id):
        course_vote = get_object_or_404(M_CourseVote, pk=id)
        if course_vote.matching.id != matching_id:
            raise Http404
        return course_vote


    @classmethod
    def get_or_create_course_admin_user(cls, username, first_name=None, last_name=None, email=None):
        course_admin_user, created = TUMUser.objects.get_or_create(username=username)
        if created:
            course_admin_user.first_name = first_name
            course_admin_user.last_name = last_name
            course_admin_user.email = email
            course_admin_user.save()
        return course_admin_user

    def get_full_preference_list(self):
        return self.preferences.get_full_preferences(self._get_value_label_tags_dict(self.matching.p_votes))
