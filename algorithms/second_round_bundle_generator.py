import random, itertools, math
from collections import OrderedDict
from sets import Set

from bundled_matching.datastructures.preference_list import PreferenceList
import logging
import datetime

class SecondRoundBundleGenerator(object):
    """ Creates Bundles for Students who did not get allocated in the first matching round. """
    def __init__(self,student,course_valid_ts,ts_assigned,ts_capacity,matching_round):
        self.student = student
        self.ts_assigned = ts_assigned
        self.ts_capacity = ts_capacity
        self.matching_round = matching_round
        self.course_valid_ts = course_valid_ts
        self.course_constraint_violations = list()
        self.MAX = 200
        time_tables = student.b_m_timetable_student.all()
        if time_tables:
            time_table = list(time_tables)
            self.courses_constraints = time_table[0].get_courses_list()
            self.interested_tutorials = time_table[0].get_tutorials_list()
            self.time_table = time_table[0]
            #bug falsche tutorials list
        else:
            self.courses_constraints = course_valid_ts.keys()
            self.interested_tutorials = self.student.preferences.get_tutorials_list()

    def create_second_preflist(self):
        """ Creates a preference list for unmatched students """
        if self.matching_round == 2:
            return self.filter_invalid_bundles()
        if self.matching_round == 3:
            return self.__first_round_in_second_round()
        elif self.matching_round == 4:
            return self.__second_round_in_second_round()
        elif self.matching_round == 5 :
            return self.__additional_rounds_in_second_round(with_course_constraint=True)
        elif self.matching_round > 5 :
            return self.__additional_rounds_in_second_round(with_course_constraint=False)
        return new_pref_list

    def filter_invalid_bundles(self):
        pref = self.student.preferences
        ex_dict = pref.export_dict()
        valid_dict = {}
        for rank,bundles in ex_dict.iteritems():
            for bundle in bundles:
                valid = True
                for item in bundle:
                    if not item in self.ts_assigned:
                        valid = False
                if valid:
                    if rank in valid_dict:
                        valid_dict[rank].append(bundle)
                    else:
                        valid_dict[rank] = [bundle]
        return PreferenceList(valid_dict)


    def __first_round_in_second_round(self):
        from bundled_matching.algorithms.bundle_generator_dfs import BundleGeneratorDFS
        new_pref_list = PreferenceList()
        courses_list = self.courses_constraints
        tuts = self.interested_tutorials
        time_schedule = self.student.time_table.get_time_table()
        bg = BundleGeneratorDFS(self.time_table.get_day_priority(),self.course_valid_ts, tuts, time_schedule,courses_list,self.time_table.get_max_courses(),0,0,300,False,True)
        bundles = bg.get_bundles_as_tuples()
        rank = 1
        for bundle in bundles:
            new_pref_list.add_item(rank,frozenset(bundle))
        return new_pref_list

    def __second_round_in_second_round(self):
        new_pref_list = PreferenceList()
        bundle_length = 0
        if self.student.b_m_timetable_student.exists():
            bundle_length = len(list(self.student.b_m_timetable_student.get().get_tutorials_list()))
        else:
            bundle_length = self.student.preferences.get_bundle_length()
        violating_dict = self.__get_violating_timeslot_dict(bundle_length)
        rank = 1
        for num_violations in violating_dict:
            all_bundles_num_violation = self.create_valid_bundles(num_violations,violating_dict)
            for bundle in all_bundles_num_violation:
                new_pref_list.add_item(rank,frozenset(bundle))
            rank += 1
        return new_pref_list

    def __additional_rounds_in_second_round(self,with_course_constraint):
        new_pref_list = PreferenceList()
        bundles = self.create_no_constraint_prefs(self.interested_tutorials,with_course_constraint)
        rank = 1
        for bundle in bundles:
            new_pref_list.add_item(rank, frozenset(bundle))
        return new_pref_list

    def create_no_constraint_prefs(self,tuts,with_course_constraint):
        """ Create randomized bundles without any constraints in time schedule  """
        from bundled_matching.algorithms.bundle_generator_dfs import BundleGeneratorDFS
        time_schedule = {"Monday":[["08:00","21:00"]],"Tuesday":[["08:00","21:00"]],"Wednesday":[["08:00","21:00"]],"Thursday":[["08:00","21:00"]],"Friday":[["08:00","21:00"]]}
        courses_list = []
        if with_course_constraint:
            courses_list = self.courses_constraints
        bg = BundleGeneratorDFS(days_priority_dict = self.time_table.get_day_priority(),courses_list = courses_list, ts_dict = self.course_valid_ts,tutorials_list = tuts,relevant_timeslots = time_schedule,MAX=200,randomized=True, sorted_dfs=False)
        bundles = bg.get_bundles_as_tuples()
        return bundles

    def create_valid_bundles(self,num_violations, violating_dict ):
        """Creates all valid bundles of num_violations"""
        solution_list = list()
        last_rank = list()
        for bundle in violating_dict[num_violations]:
            valid_ts_list = bundle[0]
            invalid_ts_list = bundle[1]
            invalid_courses_list = [ts.course for ts in invalid_ts_list]
            solutions = list()
            self.dfs(valid_ts_list,invalid_courses_list,solutions)
            solution_list.extend(solutions)
        return solution_list

    def dfs(self,current_bundle,invalid_courses_list,solution_list):
        """ DFS for valid bundles """
        if len(solution_list)>self.MAX:
            return
        if len(invalid_courses_list)==0:
            return
        if len(invalid_courses_list)==1:
            for ts in self.course_valid_ts[invalid_courses_list[0]]:
                if self.is_valid(current_bundle,ts):
                    solution_list.append(current_bundle+[ts])
        for ts in self.course_valid_ts[invalid_courses_list[0]]:
            if self.is_valid(current_bundle,ts):
                new_bundle = current_bundle + [ts]
                self.dfs(new_bundle,invalid_courses_list[1:],solution_list)

    def is_valid(self, current_bundle, timeslot):
        if not current_bundle:
            return True
        for ts in current_bundle:
            if ts.is_colliding(ts = timeslot,free_time = 0):
                return False
        return True

    def collides_with_course_constraint(self):
        return []

    def __get_violating_timeslot_dict(self,bundle_length):
        values = self.student.preferences.values()
        num_violation_to_bundle = OrderedDict()
        for bundle in values:
            counter = 0
            violating_list = list()
            non_violating_list = list()
            for ts in bundle:
                if not ts in self.ts_assigned: #not self.ts_assigned[ts] < self.ts_capacity[ts]:
                    counter += 1
                    violating_list.append(ts)
                else:
                    non_violating_list.append(ts)
            if counter >= bundle_length:
                continue
            if counter in  num_violation_to_bundle:
                num_violation_to_bundle[counter].append((non_violating_list,violating_list))
            else:
                num_violation_to_bundle[counter] = [(non_violating_list,violating_list)]
        return num_violation_to_bundle
