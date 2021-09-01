# -*- coding: utf-8 -*-
# Aykut Uzunoglu
#python imports
import hashlib
import logging
#django imports
from django.db import models
from django.shortcuts import get_object_or_404
from django.http import Http404
from django.core.urlresolvers import reverse
from django.utils.functional import cached_property
#bundled_matching imports
from bundled_matching.models.m_vote import BMVote
from bundled_matching.models.instance_m import Instance
from bundled_matching.datastructures.preference_list import PreferenceList
from bundled_matching.util.im_edu_features_of_study import IM_EDU_FeaturesOfStudy
#app imports
from app.models.tum_user import TUMUser
from bundled_matching.algorithms.second_round_bundle_generator import SecondRoundBundleGenerator
from bundled_matching.datastructures.preference_list import ACCEPTABLE, INVISIBLE
from bundled_matching.util import keys
import json

class M_StudVote(BMVote):
    DEGREE_BSC = 'bsc'
    DEGREE_MSC = 'msc'
    DEGREE_NONE = ''


    TYPE_OF_STUDY_CHOICES = (
                        ('BSc Informatik', 'BSc Informatik'),
                        ('BSc Games', 'BSc Informatik: Games Engineering'),
                        ('BSc WirtInform', 'BSc Wirtschaftsinformatik'),
                        ('BSc BioInform', 'BSc Bioinformatik'),
                        ('BSc TUM BWL', 'BSc TUM BWL'),
                        ('BSc MSE', 'BSc MSE'),
                        ('BSc ElekTech', 'BSc Elektrotechnik'),
                        ('BEd Lehramt', 'Bachelor Naturw. Bildung'),
                        ('BSc Sonst', 'Bachelor Sonstiges'),
                        ('MSc Informatik', 'MSc Informatik'),
                        ('MSc Games', 'MSc Informatik: Games Engineering'),
                        ('MSc WirtInform', 'MSc Wirtschaftsinformatik'),
                        ('MSc BioInform', 'MSc Bioinformatik'),
                        ('MSc TUM BWL', 'MSc TUM BWL'),
                        ('MSc Robotiks', 'MSc Robotiks'),
                        ('MSc AutomSE', 'MSc Automotive Softw. Eng.'),
                        ('MSc BiomComp', 'MSc Biomedical Computing'),
                        ('MSc ElekTech', 'MSc Elektrotechnik'),
                        ('MEd Lehramt', 'Master Naturw. Bildung'),
                        ('MSc CSE', 'MSc CSE'),
                        ('MSc Sonst', 'Master Sonstiges'),
                        ('Other Info', 'Aufbaustudium Informatik'),
    )

    matching = models.ForeignKey(Instance, related_name='b_m_stud_votes', on_delete=models.CASCADE, db_index=True, editable=False)
    user = models.ForeignKey(TUMUser, blank=False, related_name='b_m_stud_user', on_delete=models.CASCADE, db_index=True, editable=False)
    preferences_backup = models.TextField(default='', null=True,blank=True)
    student_number = models.CharField(max_length=16, null=True, blank=True, editable=False, verbose_name='Student Number')
    type_of_study = models.CharField(max_length=32, blank=True, editable=False, verbose_name='Type of study')
    type_of_study_id = models.CharField(max_length=6, blank=True, editable=False, verbose_name='Type of study TUM ID')
    semester = models.SmallIntegerField(blank=True, null=True, editable=False, verbose_name='Semester')
    type_of_study_manual = models.CharField(max_length=128, blank=True, editable=True, choices=TYPE_OF_STUDY_CHOICES, verbose_name='Type of study next term')

    class Meta:
        app_label = 'bundled_matching'
        db_table = 'm_stud_vote'
        unique_together = (('matching', 'user'), ('matching', 'student_number'), )
        verbose_name = 'student'

    @property
    def capacity(self):
        return 1
    @property
    def type_of_study_id_tum(self):
        return "1630 {}".format(self.type_of_study_id)
    @property
    def preferences(self):
        from bundled_matching.models.preference_list import PreferenceList as PL
        try:
            bundles  = list(PL.objects.filter(stud_vote=self,second_round = 1).prefetch_related('bundle'))
        except PL.DoesNotExist:
            return PreferenceList()
        pref_dict = {}
        for bundle in bundles:
            if bundle.rank in pref_dict:
                pref_dict[bundle.rank].append(frozenset(bundle.bundles_list))
            else:
                pref_dict[bundle.rank] = []
                pref_dict[bundle.rank].append(frozenset(bundle.bundles_list))
        pref_list = PreferenceList(pref_dict)
        return pref_list

    @preferences.setter
    def preferences(self,value):
        from bundled_matching.models.preference_list import PreferenceList as PL
        if isinstance(value,PreferenceList):
            ex_dict = value.export_dict()
        elif isinstance(value,dict):
            ex_dict = value
        else:
            raise TypeError('Wrong input type')
        self.set_backup_preflist(ex_dict,0)
        try:
            self.b_m_bundle.all().delete()
        except PL.DoesNotExist:
            bundles = []
        for rank in ex_dict:
            for bundle in ex_dict[rank]:
                pl = PL(rank = rank ,stud_vote = self)
                pl.save()
                pl.add_items(bundle)

    def get_preferences_output(self):
        from bundled_matching.models.preference_list import PreferenceList as PL
        bundles  = list(PL.objects.filter(stud_vote=self, second_round = 1).prefetch_related('bundle'))
        pref_dict = dict()
        for bundle in bundles:
            if bundle.rank == INVISIBLE:
                continue
            if bundle.rank in pref_dict:
                pref_dict[bundle.rank].append(str(bundle))
            else:
                pref_dict[bundle.rank] = list()
                pref_dict[bundle.rank].append(str(bundle))
        return pref_dict

    @cached_property
    def time_table(self):
        list_of_tt = self.b_m_timetable_student.all()
        if list_of_tt:
            return list_of_tt[0]
        else:
            return None

    @property
    def label_name(self):
        return self.user.first_name + ' ' + self.user.last_name + ' (' + (self.student_number if self.student_number else '-') + ')'

    @property
    def type_of_study_with_id(self):
        if self.type_of_study or self.type_of_study_id:
            return self.type_of_study + ' (' + self.type_of_study_id + ')'
        return None

    def get_tutorials_list(self):
        from bundled_matching.models.preference_list import PreferenceList as PL
        if self.b_m_timetable_student.exists():
            return list(self.b_m_timetable_student.all()[0].get_tutorials_list())
        else:
            pl = PL.objects.filter(stud_vote=self).prefetch_related('bundle')
            if pl.exists():
                pl = pl[0]
                cl = [ elem.course for elem in pl.bundles_list]
                return cl
        return []
    @property
    def tags(self):
        return self.get_degree()

    @classmethod
    def get_filters(cls):
        filters = [
            ('bsc', 'B.Sc.'),
            ('msc', 'M.Sc.'),
            ('exchange', 'Exchange students')
        ]
        return filters

    def __unicode__(self):
        return self.user.first_name + ' ' + self.user.last_name + ' (' + (self.student_number if self.student_number else '-') + ')' + ' - ' + unicode(self.matching)

    def get_degree(self):
        type_of_study = self.type_of_study_manual if self.type_of_study_manual else self.type_of_study
        if type_of_study[:1] == 'B':
            return M_StudVote.DEGREE_BSC
        elif type_of_study[:1] == 'M':
            return M_StudVote.DEGREE_MSC
        else:
            return M_StudVote.DEGREE_NONE


    def get_absolute_bundle_url(self):
        return reverse('instance.stud_vote_bundle.edit', kwargs={'pk': str(self.matching.id), 'v_pk': str(self.id)})

    def get_absolute_bundle_edit_url(self):
        if self.matching.tutor_matching:
            return reverse('instance.stud_vote_bundle.edit', kwargs={'pk': str(self.matching.id), 'v_pk': str(self.id)})
        else:
            return reverse('instance.stud_vote_bundle.edit', kwargs={'pk': str(self.matching.id), 'v_pk': str(self.id)})

    def get_absolute_url(self):
        return reverse('instance.stud_vote.view', kwargs={'pk': str(self.matching.id), 'v_pk': str(self.id)})

    def get_absolute_edit_url(self):
        if self.matching.tutor_matching:
            return reverse('instance.tut_vote.edit', kwargs={'pk': str(self.matching.id), 'v_pk': str(self.id)})
        else:
            return reverse('instance.stud_vote.edit', kwargs={'pk': str(self.matching.id), 'v_pk': str(self.id)})

    def get_absolute_delete_url(self):
        return reverse('instance.stud_vote.delete', kwargs={'pk': str(self.matching.id), 'v_pk': str(self.id)})


    def total_number_of_items(self):
        return len(self.matching.course_votes)

    def get_value_label_tags_dict(self):
        return self._get_value_label_tags_dict(self.matching.course_votes)


    def check_read_permission(self, user):
        return (user == self.user) or (user in self.matching.admins) or user.is_superuser

    def check_admin_read_permission(self, user):
        return (user == self.user) or (user in self.matching.admins) or user.is_superuser

    def check_write_permission(self, user):
        return user == self.user


    @property
    def assigned_items(self):
        return self.matching.get_assignment_for_stud(self)


    @classmethod
    def get_stud_vote_or_abort(cls, id, matching_id):
        stud_vote = get_object_or_404(M_StudVote, pk=id)
        if stud_vote.matching.id != matching_id:
            raise Http404
        return stud_vote


    @classmethod
    def __get_saml_dict(cls, request):
        saml2_identities = request.session.get(u'_saml2_identities')
        if saml2_identities:
            saml2_list = request.session.get(u'_saml2_identities').itervalues().next().itervalues().next()

            for saml2_item in saml2_list:
                if isinstance(saml2_item, dict):
                    ava_values = saml2_item.get(u'ava')
                    return ava_values
        return None

    @classmethod
    def get_student_number(cls, request):
        saml_dict = M_StudVote.__get_saml_dict(request)
        if saml_dict:
            mwnMatrikelnummer = saml_dict.get(u'mwnMatrikelnummer')
            if mwnMatrikelnummer:
                return mwnMatrikelnummer[0]
            else:
                return '-'
        else:
            seed = 'g802h49j'
            user_string = request.user.username + '_' + request.user.first_name + '_' + request.user.last_name + '_' + str(request.user.email)
            user_hash = hashlib.sha256(user_string.encode('utf-8') + '#' + seed).hexdigest()[:10]
            return 'ExSt_' + user_hash


    @classmethod
    def __get_features_of_study(cls, request):
        saml_dict = M_StudVote.__get_saml_dict(request)
        if saml_dict:
            featuresOfStudies = saml_dict.get(u'imEduPersonFeaturesOfStudy')
            return featuresOfStudies
        return None

    @classmethod
    def get_degree_and_type_of_study(cls, request):
        degree_and_type_of_study = M_StudVote.__get_features_of_study(request)
        if degree_and_type_of_study:
            return IM_EDU_FeaturesOfStudy.get_degree_and_type_of_study(degree_and_type_of_study)
        else:
            return 'Exchange student (w/o TUM acc.)'

    @classmethod
    def get_type_of_study_id(cls, request):
        degree_and_type_of_study = M_StudVote.__get_features_of_study(request)
        if degree_and_type_of_study:
            return IM_EDU_FeaturesOfStudy.get_type_of_study_id(degree_and_type_of_study)
        else:
            return '-'

    @classmethod
    def get_semester(cls, request):
        return IM_EDU_FeaturesOfStudy.get_semester(M_StudVote.__get_features_of_study(request))

    def get_full_preference_list(self):
        return self.preferences.get_full_preferences(self.get_value_label_tags_dict())

    def get_ordered_pref_dict(self):
        return self.preferences.get_ordered_pref_dict()

    @classmethod
    def get_stud_votes_from_student_number(cls, student_number, matching):
        try:
            stud_votes = M_StudVote.objects.filter(student_number=student_number, matching=matching)
            return stud_votes
        except M_StudVote.DoesNotExist:
            return []

    def set_backup_preflist(self, export_dict, matching_round):
        id_dict = dict()
        for rank, list_of_bundles in export_dict.iteritems():
            id_dict[rank] = list()
            for bundle in list_of_bundles:
                id_dict[rank].append([ts.id for ts in bundle])
        id_dict['round'] = matching_round
        json_id_dict = json.dumps(id_dict)
        self.preferences_backup = json_id_dict
        self.save()

    def get_backup_preflist(self):
        if not self.preferences_backup:
            return dict()
        else:
            return json.loads(self.preferences_backup)

    def create_second_preflist(self,course_valid_ts,ts_assigned,ts_capacity,matching_round):
        from bundled_matching.models.preference_list import PreferenceList as PL
        srbg = SecondRoundBundleGenerator(self,course_valid_ts,ts_assigned,ts_capacity,matching_round)
        pref_list = srbg.create_second_preflist()
        ex_dict = pref_list.export_dict()
        self.set_backup_preflist(ex_dict,matching_round)
        return pref_list
