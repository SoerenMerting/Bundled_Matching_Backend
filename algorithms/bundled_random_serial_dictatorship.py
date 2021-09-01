#Aykut Uzunoglu
#python imports
import random, itertools, math
from collections import OrderedDict
from sets import Set
import logging


from bundled_matching.datastructures.assignment import Assignment
from bundled_matching.datastructures.matching_instance import MatchingInstance
from bundled_matching.datastructures.preference_list import PreferenceList
from bundled_matching.datastructures.tie_breaker import TieBreaker

from django.db.models import Max

class BundledRandomSerialDictatorship(object):
    """ Extension of the Random Serial Dictatorship for Bundled Matching Problems """
    def __init__(self, matching_instance, bundles=list(), order = None):
        if not isinstance(matching_instance, MatchingInstance):
            raise TypeError('MatchingInstance expected!')
		#matching_instance has the required information for the matching
        self._matching_instance = matching_instance
		#bundles is passed by the Instance data model
        self._all_bundles = bundles
        self._all_timeslots = self._matching_instance.get_timeslots()
        self.__timeslot_allocation = dict()
        self._all_students = self._matching_instance.get_students()#self._delete_empty_students(self._matching_instance.get_students())
        if not order:
            order = self._all_students
            random.shuffle(order)
        self.__order = order
        self._tb = TieBreaker()
        self._preferences_of_studs = self.get_bundled_preferences_without_ties_as_dict()
		#if a student placed several bundles on the same rank -> break it

    def _delete_empty_students(self, stud_list):
        studentlist = list()
        for stud in stud_list:
            if len(stud.preferences) > 0:
                studentlist.append(stud)
        return studentlist


    def get_matching_instance(self):
        return self._matching_instance

    def get_students(self):
        return self._all_students

    def get_timeslots(self):
        return self._all_timeslots

    def get_bundles(self):
        return self._all_bundles

    def get_assignment(self):
        result = self.get_result()
        assignment = list()
        for stud, bundle in result.iteritems():
            assignment.append((stud,bundle))
        return assignment

    def get_preferences(self):
        return self._preferences_of_studs

    def get_bundled_preferences_without_ties_as_dict(self):
        result_dict = OrderedDict()
        studs = self.get_students()
        for stud in studs:
			# result_dict[stud] = self._tb.break_ties(self._matching_instance.get_preferences_stud(stud)).to_flat_list()
            result_dict[stud] = self._matching_instance.get_preferences_stud(stud).to_flat_list()
        return result_dict

    def get_result(self):
        students = self.get_students()
        timeslots = self.get_timeslots()
        ts_assigned_cap = OrderedDict()
        matching = OrderedDict()
        ts_cap = OrderedDict()
        preferences = self._preferences_of_studs
        for ts in timeslots:
            ts_assigned_cap[ts] = ts.assigned_capacity
            ts_cap[ts] = ts.capacity
        for stud in self.__order:
            for bundle in preferences[stud]:
                bundle_valid = True
                for ts in bundle:
                    if not ts_assigned_cap[ts] < ts_cap[ts]:
                        bundle_valid = False
                        break
                if bundle_valid:
                    matching[stud] = bundle
                    for ts in bundle:
                        ts_assigned_cap[ts] += 1
                    break
        self.__timeslot_allocation = ts_assigned_cap
        return matching

    def get_timeslot_allocation(self):
        return self.__timeslot_allocation
