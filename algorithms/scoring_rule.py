# -*- coding: utf-8 -*-
#Aykut Uzunoglu/Sören Merting
#python imports
import random, itertools, math
from collections import OrderedDict
from sets import Set
import logging
import datetime

class ScoringRule(object):

	def __init__(self, bundle_list,max_courses,lunch_time_dict,day_priority_dict, courses = list()):
		self.__bundle_list = bundle_list
		self.days = ["Monday","Tuesday","Wednesday","Thursday","Friday"]
		self.__max_courses = max_courses
		self.__day_to_points = self.__init_day_to_point()
		self.__bundle_days_slots = dict()
		self.__bundle_score = dict()
		self.__bundle_days_span = dict()
		self.__bundle_days_num_course = dict()
		self.__bundle_days_working_time = dict()
		self.__bundle_lunch_times = lunch_time_dict
		self.__bundle_days_lunch = dict()
		self.__day_priority_dict = day_priority_dict
		self.__lectures = courses
		self.__init_slots_of_day()
		for bundle in self.__bundle_list:
			self.__bundle_score[bundle] = 0
		self.__compute_day_span()
		self.__compute_working_time()
		self.__compute_lunch_time()
		self.__compute_score()

	def __init_day_to_point(self):
		mc = self.__max_courses
		mcOrdered = None
		if mc:
			mcOrdered = OrderedDict(sorted(mc.items(), key = lambda x:x[1], reverse=True))
		else:
			mcOrdered = dict((day,4) for day in self.days)
		points = [40,30,20,10,0]
		counter = 0
		day_to_point = dict()
		for day in mcOrdered:
			day_to_point[day] = points[counter]
		return day_to_point

	def get_score(self):
		return self.__bundle_score

	def get_score_with_random_mix(self,random_mix = 0.2,seed_random = random.random()):
		random.seed(seed_random)
		random_mix_score = {}
		for bundle,score in self.__bundle_score.iteritems():
			x = random.random()
			multiplicator = x*random_mix
			multiplicator -= (random_mix/2)
			#logging.info("score:{}".format(score))
			#logging.info("rand_score:{}".format(score + multiplicator*score))
			random_mix_score[bundle] = score + multiplicator*score
		return random_mix_score

	#maps bundle to [Monday,Tuesday,Wednesday] and bundle+day to the corresponding timeslots of the tutorials
	def __init_slots_of_day(self):
		bundle_days_slots = dict()
		bundle_days_num_course = dict()		
		for bundle in self.__bundle_list:
			bundle_days_slots[bundle] = dict()
			bundle_days_num_course[bundle] = dict()
			#auch die lectures müssen für Punktevergabe berücksichtigt werden
			for course_ts in self.__lectures:
				if course_ts.day in bundle_days_slots[bundle]:
					bundle_days_slots[bundle][course_ts.day].append(course_ts)
					bundle_days_num_course[bundle][course_ts.day] += 1
				else:
					bundle_days_slots[bundle][course_ts.day] = list()
					bundle_days_slots[bundle][course_ts.day].append(course_ts)
					bundle_days_num_course[bundle][course_ts.day] = 1
			for ts in bundle:
				if ts.day in bundle_days_slots[bundle]:
					bundle_days_slots[bundle][ts.day].append(ts)
					bundle_days_num_course[bundle][ts.day] += 1
				else:
					bundle_days_slots[bundle][ts.day] = list()
					bundle_days_slots[bundle][ts.day].append(ts)
					bundle_days_num_course[bundle][ts.day] = 1

		for bundle in bundle_days_slots:
			for day in bundle_days_slots[bundle]:
				bundle_days_slots[bundle][day].sort()
		self.__bundle_days_slots = bundle_days_slots
		self.__bundle_days_num_course = bundle_days_num_course

	def __compute_score(self):		
		for bundle in self.__bundle_list:
			day_score = 0
			daycount = 0
			for day in self.__bundle_days_span[bundle]:				
				span = self.__bundle_days_span[bundle][day][2]
				spanmin = span.seconds/60
				if(spanmin != 0):
					#no Free Day: dayscore = (workingtime/dayspan)*DaySpanPoints*DayPriority
					points_span = 0
					daycount += 1
					if span < datetime.timedelta(hours = 2):
						points_span += 1
					elif span < datetime.timedelta(hours = 4):
						points_span += 2
					elif span < datetime.timedelta(hours = 6):
						points_span += 3
					elif span < datetime.timedelta(hours = 8):
						points_span += 4
					elif span < datetime.timedelta(hours = 10):
						points_span += 2.5
					elif span >= datetime.timedelta(hours = 10):
						points_span += 1
					working = self.__bundle_days_working_time[bundle][day]
					priority = self.__day_priority_dict[day]
					#day_score_old = day_score
					day_score += (float(working)/spanmin)*points_span*priority
					#logging.info("{}".format(day) + " score:{}".format(day_score - day_score_old))
			points_lunch = 0
			for day in self.__bundle_lunch_times[bundle]:
				lunch = self.__bundle_lunch_times[bundle][day]
				priority = self.__day_priority_dict[day]
				#points_lunch_old = points_lunch
				if lunch >= 90:
					points_lunch += 1* priority
				elif lunch >= 75:
					points_lunch += 1.5* priority
				elif lunch >= 60:
					points_lunch += 2* priority
				elif lunch >= 45:
					points_lunch += 1.5* priority
				elif lunch >= 30:
					points_lunch += 1* priority
				#logging.info("lunchtime:{}".format(lunch) + " -> {}".format(points_lunch - points_lunch_old))
			score = (day_score + points_lunch + (5-daycount)*30)
			#logging.info("add freedays:{}".format(5-daycount))
			#logging.info("score:{}".format(score))
			self.__bundle_score[bundle] = score

	def __compute_lunch_time(self):
		bundle_day_lunch_time = dict()
		for bundle in self.__bundle_list:
			bundle_day_lunch_time[bundle] = dict()
			for day,timeslots in self.__bundle_days_slots[bundle].iteritems():
				start_lunch_time = datetime.timedelta(hours=11, minutes =00)
				end_lunch_time = datetime.timedelta(hours=14, minutes = 00)
				in_block_start = None
				in_block_end = None
				for ts in timeslots:
					ts_end = datetime.timedelta(hours = ts.end.hour, minutes = ts.end.minute)
					ts_begin = datetime.timedelta(hours = ts.begin.hour, minutes = ts.begin.minute)
					if not (ts_end <= start_lunch_time or ts_begin >= end_lunch_time):
						if start_lunch_time < ts_end and start_lunch_time <= ts_begin:
							start_lunch_time = ts_end
						if end_lunch_time > ts_begin and end_lunch_time >= ts_end:
							end_lunch_time = ts_begin
					if (ts_begin > start_lunch_time and ts_end < end_lunch_time):
						if not in_block_start:
							in_block_start = ts_begin
							in_block_end = ts_end
						else:
							in_block_end = ts_end
				lunch_time = end_lunch_time - start_lunch_time
				if in_block_start:
					first_delta = start_lunch_time - in_block_start
					second_delta = in_block_end - end_lunch_time
					lunch_time = max(first_delta,second_delta)
				bundle_day_lunch_time[bundle][day] = lunch_time
		self.__bundle_days_lunch = bundle_day_lunch_time

		#computes the timespan of every day in every bundle
	def __compute_day_span(self):
		start_to_end = dict()
		for bundle_unsorted in self.__bundle_list:
			start_to_end[bundle_unsorted] = dict()
			for day,timeslots in self.__bundle_days_slots[bundle_unsorted].iteritems():
				min_time = timeslots[0].begin
				max_time = timeslots[0].end
				for ts in timeslots:
					if min_time > ts.begin:
						min_time = ts.begin
					if max_time < ts.end:
						max_time = ts.end
				min_date = datetime.timedelta(hours=min_time.hour, minutes = min_time.minute)
				max_date = datetime.timedelta(hours=max_time.hour, minutes = max_time.minute)
				start_to_end[bundle_unsorted][day] = (min_time , max_time, (max_date-min_date))
		self.__bundle_days_span = start_to_end

	# computes the working time of every day in every bundle
	# bundle or bundle_unsorted????
	def __compute_working_time(self):
		bundle_day_working_time = dict()
		for bundle in self.__bundle_list:
			bundle_day_working_time[bundle] = dict()
			for day,timeslots in self.__bundle_days_slots[bundle].iteritems():
				working_time = 0
				for ts in timeslots:# etwas abenteuerlich, aber hoffentlich richtig ^^
					ts_end = datetime.timedelta(hours = ts.end.hour, minutes = ts.end.minute)
					ts_begin = datetime.timedelta(hours = ts.begin.hour, minutes = ts.begin.minute)
					working_time += ((ts_end-ts_begin).seconds/60)
				bundle_day_working_time[bundle][day] = working_time
		self.__bundle_days_working_time = bundle_day_working_time
