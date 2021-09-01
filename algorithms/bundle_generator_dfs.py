# -*- coding: utf-8 -*-
#Aykut Uzunoglu/Sören Merting
#python imports
import random, itertools, math
from collections import OrderedDict
from sets import Set
import logging
import datetime
#
from bundled_matching.algorithms.scoring_rule import ScoringRule


class BundleGeneratorDFS(object):
    """This class generates valid bundles via depth-first-search algorithm"""
    def __init__(self, days_priority_dict, ts_dict, tutorials_list, relevant_timeslots, courses_list = list(), max_courses_dict=list(), gap_time=0, lunch_time=0, MAX=1000000, sorted_dfs=True, randomized = True):
        self.__gap_time = gap_time
        self.__lunch_time = lunch_time
        self.__days_priority_dict = days_priority_dict
        #student is attending to tutorials of which course?
        self.__sorted = sorted_dfs
        self.__tutorials = list(tutorials_list)
        #student is attending to lecture of which course?
        self.__solution_list = list()
        self.__randomized = randomized
        self.__next_course = dict()
        next_element = None
        self.__MAX = MAX
        self.__scores = None

        self.__course_constraints = list(courses_list)
        self.__course_constraints_compl = list()
        self.__max_courses_dict = max_courses_dict
        if self.__randomized:
            random.shuffle(self.__tutorials)
            random.shuffle(self.__course_constraints)
        for course in self.__course_constraints:
            cct = list(course.constraint_timeslots)
            if self.__randomized:
                random.shuffle(cct)
            self.__course_constraints_compl.extend(cct)
        for tut in self.__tutorials:
            if next_element:
                self.__next_course[next_element] = tut
            next_element = tut
        self.__ts_dict = self.__clean_ts_dict(ts_dict)

        self.__course_to_timeslots = self.__create_course_to_timeslots(relevant_timeslots = relevant_timeslots)

    #wenn cap true: Beschränkung der Anzahl der in dem Teilbaum generierten Bündel
	# -> Maximal 3 Blätter pro Knoten
	# -> Maximal 40/depth Bündel in einem Teilbaum mit Elternknoten in Suchtiefe depth
    def dfs(self,course, current_bundle, counter = 0, cap = False, depth = 1):
        """
        Searches for valid bundles in depth-first-search manner
        """
        next_element = self.__next_course.get(course)
        length = len(self.__solution_list)
        random.shuffle(self.__course_to_timeslots[course])
        dfscounter = 0
        for ts in self.__course_to_timeslots[course]:
            valid = self.is_valid(current_bundle,ts)
            new_bundle = current_bundle + [ts]
            if cap and counter > 40/depth and depth > 2:
                return counter
            if cap and counter > 20 and depth == 2:
                #genug Bündel für Elternknoten generiert
                #-> setze counter zurück und gehe zum nächsten Elternknoten
                return 0
            if cap and dfscounter > 2 and valid and length<self.__MAX:
                return counter
            elif (not next_element) and valid and length<self.__MAX:
                self.__solution_list.append(new_bundle)
                counter += 1
                dfscounter += 1
            elif valid and length<self.__MAX:
                counter = self.dfs(next_element,new_bundle, counter, cap, depth+1)
        return counter

    def get_bundles(self):
        if not self.__tutorials:
            return []
        self.dfs(self.__tutorials[0],[])
        if len(self.__solution_list) < 50:
            self.__solution_list = []
            self.dfs(self.__tutorials[0],[],0,False)
        return self.__solution_list

    def get_score(self):
        return self.__scores

    def get_bundles_as_tuples(self):
        """ Returns valid timeslots bundles as list of tuples """
        bundles = self.get_bundles()
        tuples = list()
        for bundle in bundles:
            b_tup = frozenset(bundle)
            tuples.append(b_tup)
        b_l_t = self.__compute_lunch_time(tuples)
        solution = list()
        for bundle in tuples:
            valid = True
            for day in b_l_t[bundle]:
                if b_l_t[bundle][day] < self.__lunch_time:
                    valid = False
                    break
            if valid:
                solution.append(bundle)
        if self.__sorted:
            sr = ScoringRule(solution,self.__max_courses_dict,b_l_t,self.__days_priority_dict, self.__course_constraints_compl)
            #self.__scores = sr.get_score_with_random_mix()
            self.__scores = sr.get_score()
            solution.sort(key=lambda b:self.__scores[b], reverse=True)
        return solution

    def __clean_ts_dict(self,ts_dict):
        """ Returns dictionary of timeslots with courses as keys that do not
        overlap with course timeslots"""
        clean_ts_dict = dict()
        for course in ts_dict:
            clean_ts_dict[course] = list()
            for timeslot in ts_dict[course]:
                valid = True
                for constraint in self.__course_constraints_compl:
                    if timeslot.is_colliding(constraint,self.__gap_time):
                        valid = False
                        break
                if valid:
                    clean_ts_dict[course].append(timeslot)
        return clean_ts_dict


    def __create_course_to_timeslots(self, relevant_timeslots):
        """ Returns dictionary with courses as key and list of valid timeslots according
        to the timetable """
        feasible_timeslots = dict()
        for course in self.__tutorials:
            feasible_timeslots[course] = list()
            for ts in self.__ts_dict[course]:
                if ts.is_feasible(times = relevant_timeslots):
                    feasible_timeslots[course].append(ts)
        return feasible_timeslots


    def is_valid(self, current_bundle, timeslot):
        if not current_bundle:
            return True
        if self.__is_max_course_violated(current_bundle = current_bundle, timeslot=timeslot):
            return False
        for ts in current_bundle:
            if ts.is_colliding(ts = timeslot,free_time = self.__gap_time ):
                return False
        return True

    def __is_max_course_violated(self,current_bundle,timeslot):
        if not self.__max_courses_dict:
            return False
        day = timeslot.day
        timeslots_on_same_day = list()
        for ts in current_bundle:
            if ts.day == day:
                timeslots_on_same_day.append(ts)
        if len(timeslots_on_same_day) < self.__max_courses_dict[day]:
            return False
        return True
#----------------------
#LUNCH TIME COMPUTATION
#----------------------
    def __compute_lunch_time(self, bundles_list):
        days = ["Monday","Tuesday","Wednesday","Thursday","Friday"]
        days_timeslots_dict = {day: list() for day in days}
        for course_ts in self.__course_constraints_compl:
            days_timeslots_dict[course_ts.day].append(course_ts)
        bundle_lunch_times_dict = dict()
        for bundle in bundles_list:
            copy_dtd = dict()
            for day in days_timeslots_dict:
                copy_dtd[day]  = list(days_timeslots_dict[day])
            for ts in bundle:
                copy_dtd[ts.day].append(ts)
            for day,ts_list in copy_dtd.iteritems():
                ts_list.sort(key = lambda item:item.begin)
            lunch_time_by_day = dict()
            for day in copy_dtd:
                #abklären ob ohne tag ohne zeitslots punkte bringen soll
                if copy_dtd[day]:
                    lunch_time_by_day[day] = self.__compute_lunch_time_day(copy_dtd[day])
            bundle_lunch_times_dict[bundle] = lunch_time_by_day
        return bundle_lunch_times_dict

    def __compute_lunch_time_day(self,ts_list):
        lunch_time = 60*3
        start = datetime.timedelta(hours = 11, minutes = 00)
        end = datetime.timedelta(hours = 14, minutes = 00)
        if len(ts_list) < 2 or datetime.timedelta(hours=ts_list[0].begin.hour,minutes=ts_list[0].begin.minute) > start:
            return lunch_time
        max_time = 0
        full_lunch_time = True
        for i in range(len(ts_list[:-1])):
            current_ts = ts_list[i]
            next_ts = ts_list[i+1]
            current_ts_end = datetime.timedelta(hours = current_ts.end.hour, minutes = current_ts.end.minute)
            current_ts_begin = datetime.timedelta(hours = current_ts.begin.hour, minutes = current_ts.begin.minute)
            next_ts_end = datetime.timedelta(hours = next_ts.end.hour, minutes = next_ts.end.minute)
            next_ts_begin = datetime.timedelta(hours = next_ts.begin.hour, minutes = next_ts.begin.minute)
            if full_lunch_time and ((current_ts_begin >start or current_ts_begin < end) or (current_ts_end > start or current_ts_end < end)):
                full_lunch_time = False
            left = max(current_ts_end,start)
            right = min(next_ts_begin,end)
            max_time = max((right-left).seconds//60,max_time)
        if full_lunch_time:
            max_time = lunch_time
        return max_time
