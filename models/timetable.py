#Aykut Uzunoglu
import datetime
import logging
from collections import Counter
from django.db import models
from django.core.urlresolvers import reverse
from django.utils.text import slugify
from django.utils.functional import cached_property
from django.core.validators import MinValueValidator, MaxValueValidator

import json

from bundled_matching.models.m_stud_vote import M_StudVote
from bundled_matching.models.m_course_vote import M_CourseVote

class TimeTable(models.Model):
    stud = models.ForeignKey(M_StudVote, blank = False ,related_name='b_m_timetable_student', on_delete=models.CASCADE , editable=False )
    time_table_json = models.CharField(default='', max_length=10000, null=True)
    tutorials_list_json = models.TextField(default='', null=True)
    tutorials_list = models.ManyToManyField(M_CourseVote, related_name='b_m_tutorials_list',  blank = True)
    courses_list = models.ManyToManyField(M_CourseVote, related_name='b_m_courses_list',blank = True)
    max_courses = models.CharField(default='', max_length = 10000, null=True)
    day_priority = models.CharField(default='', max_length = 10000, null=True)
    lunch_time = models.IntegerField(blank=True, null=True)
    gap_time = models.IntegerField(blank=True, null=True)

    class Meta:
        app_label = 'bundled_matching'
        db_table = 'timetable'
        verbose_name = 'Timetable'

    def get_courses_list(self):
        return self.courses_list.all()

    def course_exists(self,id):
        return self.courses_list.filter(id=id).exists()

    def tutorial_exists(self,id):
        return self.tutorials_list.filter(id=id).exists()

    def set_courses_list(self,courses_list):
        for course in courses_list:
            self.courses_list.add(course)

    def set_tutorials_list(self,tutorials_list):
        #c = Counter(tutorials_list)
        save_json = []
        for ts in tutorials_list:
            save_json.append(ts.id)

        self.tutorials_list_json = json.dumps(save_json)
        self.save()

    def get_tutorials_list(self):
        id_list = json.loads(self.tutorials_list_json)
        output = []
        for i in id_list:
            output.append(M_CourseVote.objects.get(id=i))
        return output

    def set_max_courses(self, max_courses):
        self.max_courses = json.dumps(max_courses)

    def get_max_courses(self):
        if not self.max_courses:
            return list()
        return json.loads(self.max_courses)

    def set_day_priority(self, day_priority):
        self.day_priority = json.dumps(day_priority)

    def get_day_priority(self):
        if not self.day_priority:
            return list()
        return json.loads(self.day_priority)

    def set_time_table(self, time_table_as_json):
        self.time_table_json = json.dumps(time_table_as_json)

    def get_time_table(self):
        if not self.time_table_json:
            return dict()
        return json.loads(self.time_table_json)
