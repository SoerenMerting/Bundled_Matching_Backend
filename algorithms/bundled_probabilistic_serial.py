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

class BundledProbabilisticSerial(object):

	def __init__(self, matching_instance,bundles=list()):
		if not isinstance(matching_instance, MatchingInstance):
			raise TypeError('MatchingInstance expected!')
		#matching_instance has the required information for the matching
		self.__matching_instance = matching_instance
		#bundles is passed by the Instance data model
		self.__all_bundles = bundles
		self.__all_timeslots = self.__matching_instance.get_timeslots()
		self.__all_students = self.__delete_empty_students(self.__matching_instance.get_students())

		self.__tb = TieBreaker()
		self.__preferences_of_studs = self.get_bundled_preferences_without_ties_as_dict()
		#if a student placed several bundles on the same rank -> break it

	def __delete_empty_students(self, stud_list):
		studentlist = list()
		for stud in stud_list:
			if len(stud.preferences) > 0:
				studentlist.append(stud)
		return studentlist


	def get_matching_instance(self):
		return self.__matching_instance

	def get_students(self):
		return self.__all_students

	def get_timeslots(self):
		return self.__all_timeslots

	def get_bundles(self):
		return self.__all_bundles

	def get_assignment(self):
		return self.__assignment

	def get_preferences(self):
		return self.__preferences_of_studs

	def get_bundled_preferences_without_ties_as_dict(self):
		result_dict = OrderedDict()
		studs = self.get_students()
		for stud in studs:
			# result_dict[stud] = self._tb.break_ties(self._matching_instance.get_preferences_stud(stud)).to_flat_list()
			result_dict[stud] = self.get_matching_instance().get_preferences_stud(stud).as_list()
		return result_dict

	def get_cleaned_result(self):
		result_dict_zeros = self.get_result()
		result_dict = OrderedDict()
		for stud in result_dict_zeros:
			result_dict[stud] = OrderedDict()
			for pref_pos in result_dict_zeros[stud]:
				if result_dict_zeros[stud][pref_pos] != 0.0:
					result_dict[stud][pref_pos] = result_dict_zeros[stud][pref_pos]
		for stud in result_dict:
			demand = 0.0
			for pref in result_dict[stud]:
				demand += result_dict[stud][pref]

		return result_dict

	def get_result(self):
		pointers = OrderedDict()
		all_bundles = self.get_bundles()
		students = self.get_students()
		timeslots = self.get_timeslots()
		ts_assigned_cap = OrderedDict()
		matching = OrderedDict()
		ts_freeCap = OrderedDict()
		ts_overdemanded = OrderedDict()
		bundle_overdemanded = OrderedDict()
		to_eat = OrderedDict()
		preferences = self.get_preferences()

		#initialize
		for stud in students:
			pointers[stud] = 0
			matching[stud] = OrderedDict()
			bundle_overdemanded[stud] = OrderedDict()
			counter = 0
			for item in preferences[stud]:
				preferences[stud][counter] = item
				matching[stud][item] = 0.0
				bundle_overdemanded[stud][item] = False
				counter += 1
		for ts in timeslots:
			ts_assigned_cap[ts] = 0.0
			to_eat[ts] = 0.0
			ts_freeCap[ts] = ts.capacity*(1.0)
			ts_overdemanded[ts] = False

		#allocation loop from t = 0.0 to 1.0
		time = 0.0
		index = 0
		while (abs(time-1.0) > 0.007) & (len(students)>0):
			index += 1
			curr_intervall = 0.0
			#reset the demand in every step to 0
			for key, value in ts_assigned_cap.iteritems():
				ts_assigned_cap[key] = 0.0
			for key, value in to_eat.iteritems():
				to_eat[key] = 0.0
			#calculate the demand of every block_course
			rest_time = float(1.0 - float(time))
			for stud in students:
				currentPref = preferences[stud][pointers[stud]]
				#ich brauche get_timeslots
				for ts in currentPref:
					ts_assigned_cap[ts] += rest_time
					to_eat[ts] += 1
			ratio = {}
			#calculate ratios for every block
			for ts in timeslots:
				if not ts_assigned_cap[ts] is 0.0:
					ratio[ts] = ts_freeCap[ts]/ts_assigned_cap[ts]
				else:
					ratio[ts] = 1.0
				if not ratio[ts] > 0.0:
					ratio[ts] = 1.0

			bundles_minRatio_int = {}
			overall_minRatio = 100.0

			#calculate for every bundle the minimum amount of possible allocation
			#calculate the overall minimum bundle for the current step
			#this overall minimum is the time a step can endure
			for stud in students:
				for ts in preferences[stud][pointers[stud]]:
					overall_minRatio = min(ratio[ts],overall_minRatio)
			#correct errors
			if overall_minRatio>1.0:
				overall_minRatio = 1.0
			# length of current interval is restTime x minRatio
			curr_intervall = rest_time*overall_minRatio

			time +=  (curr_intervall)

			#set the freeCapacity of every timeslot
			for block in timeslots:
				if(not ts_overdemanded[block]):
					ts_freeCap[block] = float(ts_freeCap[block]) - (curr_intervall*to_eat[block])
					if abs(ts_freeCap[block] ) < 0.01:
						ts_overdemanded[block] = True
						ts_freeCap[block] = 0.0
			#allocate the current demanded bundle to the student by the overall min ratio
			for stud in students:
				if not bundle_overdemanded[stud][preferences[stud][pointers[stud]]]:
					matching[stud][preferences[stud][pointers[stud]]] += (curr_intervall)

			#pick for every student the next free bundle or kick him out
			studs_to_delete = []
			for stud in students:
				bundle_valid = False
				empty_set = False
				while (not bundle_valid) & (not empty_set):
					pref_row = preferences[stud][pointers[stud]]
					bundle_valid = True
					for ts in pref_row:
						if bundle_valid:
							if  ts_overdemanded[ts]:
								bundle_valid = False
								bundle_overdemanded[stud][pointers[stud]] = True
								pointers[stud] += 1
								if not pointers[stud]<(len(preferences[stud])):
									empty_set = True
									studs_to_delete.append(stud)
			students = [x for  x in students if x not in studs_to_delete]
		return matching
