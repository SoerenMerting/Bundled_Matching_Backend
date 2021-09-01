#Aykut Uzunoglu
import datetime
import logging
from django.db import models
from django.core.urlresolvers import reverse
from django.shortcuts import get_object_or_404
from django.http import Http404
from django.utils.text import slugify
from django.utils.functional import cached_property
from django.core.validators import MinValueValidator, MaxValueValidator
from django.db.models import F

from bundled_matching.models.m_course_vote import M_CourseVote
from bundled_matching.util import keys

ID_LENGTH = 10

def _validate_id(value):
    return BMVote.is_valid_id(value)

class TimeSlot(models.Model):
    days_choices = (
        ('Monday', 'Monday'),
        ('Tuesday', 'Tuesday'),
        ('Wednesday', 'Wednesday'),
        ('Thursday', 'Thursday'),
        ('Friday', 'Friday'),)
    course_type_choices = (('Course','Course'),('Tutorial','Tutorial'))

    id = models.CharField(max_length=ID_LENGTH, blank=False, primary_key=True, db_index=True, editable=False, validators=[_validate_id])
    description = models.CharField(max_length=155, blank=False)
    day = models.CharField(max_length=15, choices = days_choices )
    course_type = models.CharField(max_length = 20 , choices = course_type_choices )
    begin = models.TimeField(blank=False, null=True)
    end = models.TimeField(blank=False, null=True)
    course = models.ForeignKey(M_CourseVote, related_name='b_m_course_timeslot', on_delete=models.CASCADE , editable=False )
    assigned_capacity  = models.PositiveSmallIntegerField(blank=False, verbose_name='Capacity', default=0, validators=[MinValueValidator(0), MaxValueValidator(9999)] )
    acceptable_popularity = models.PositiveSmallIntegerField(blank=True, verbose_name='Acceptable Popularity', default=0, validators=[MinValueValidator(0)] )
    popularity = models.PositiveSmallIntegerField(blank=True, verbose_name='Popularity', default=0, validators=[MinValueValidator(0)] )
    is_english = models.BooleanField(blank=False, default=False, verbose_name='English', help_text='Is it in English')

    def __str__(self):
        if not self.description:
            english = ""
            if self.is_english:
                english="(ENG)"
                self.description = str(self.course)+english+": "+str(self.day)[:3]+'. ' + str(self.begin)[:5] + ' - ' + str(self.end)[:5]
            else:
                self.description = str(self.course)+": "+str(self.day)[:3]+'. ' + str(self.begin)[:5] + ' - ' + str(self.end)[:5]
        return str(self.description)

    def save(self, force_insert=False, force_update=False, using=None, update_fields=None):
        if not TimeSlot.is_valid_id(self.id):
            self.id = TimeSlot.generate_id()
        self.description = str(self)
        super(TimeSlot, self).save(force_insert=force_insert, force_update=force_update, using=using, update_fields=update_fields)

    @cached_property
    def groups(self):
        from bundled_matching.models.group import Group
        return list(Group.objects.filter(timeslot = self))

    @property
    def current_capacity(self):
        from django.db.models import Sum
        from bundled_matching.models.group import Group
        return Group.objects.filter(timeslot = self).aggregate(Sum('assigned_capacity')).get('assigned_capacity__sum',0)

    def assign_student(self):
        from bundled_matching.models.group import Group
        from bundled_matching.models.pair import Pair
        groups_list = Group.objects.filter(timeslot=self)
        assigned_capacity = Pair.objects.filter(timeslot__id = self.id).count()
        counter = (assigned_capacity % (groups_list.count()))
        group = groups_list[counter]
        group.assign_student()
        self.assigned_capacity += 1
        self.save()
        return group

    def has_free_cap(self):
        return self.current_capacity<self.capacity

    def is_colliding(self,ts,free_time=0):
        if self.day != ts.day:
            return False
        my_begin = self.convert_time_beg
        my_end = self.convert_time_end
        ts_begin = ts.convert_time_beg
        ts_end = ts.convert_time_end
        delta = datetime.timedelta(minutes=free_time)
        if (my_end+delta)<=ts_begin:
            return False
        if my_begin>=(ts_end+delta):
            return False
        return True

    @cached_property
    def convert_time_beg(self):
        obj = datetime.datetime(2000, 1, 1, int(str(self.begin)[:-6]), int(str(self.begin)[3:-3]), 0, 0)
        return obj

    @cached_property
    def convert_time_end(self):
        obj = datetime.datetime(2000, 1, 1, int(str(self.end)[:-6]), int(str(self.end)[3:-3]), 0, 0)
        return obj

    def __create_time(self,time_string):
        obj = datetime.datetime(2000, 1, 1, int(time_string[:2]), int(time_string[3:]), 0, 0)
        return obj

    def clean_capacity(self):
        for gr in self.groups:
            gr.capacity = 0
            gr.save()
        self.assigned_capacity = 0
        self.save()

    #checks if timeslot is feasible with timetable of student
    #enchancement possible?
    def is_feasible(self, times):
        for day, slots in times.iteritems():
            for slot in slots:
                solution = self.__is_feasible(day=day, begin=slot[0], end = slot[1])
                if solution:
                    return True
        return False

    def between(self,time,day):
        if day == self.day:
            obj_time = self.__create_time(time)
            if self.convert_time_end==obj_time:
                return False
            if self.convert_time_beg<= obj_time and self.convert_time_end> obj_time:
                return True
            else:
                obj_time_plus_30 = obj_time + datetime.timedelta(minutes=30)
                if self.convert_time_beg> obj_time and self.convert_time_beg < obj_time_plus_30:
                    return True
                else:
                    return False
        else:
            return False


    def __is_feasible(self, day, begin , end):
        if self.day != day:
            return False
        dt_begin_self = self.convert_time_beg
        dt_end_self = self.convert_time_end
        dt_begin = self.__create_time(begin)
        dt_end = self.__create_time(end)
        if dt_begin <= dt_begin_self and dt_end >= dt_end_self:
            return True
        return False

    @property
    def capacity(self):
        from django.db.models import Sum
        from bundled_matching.models.group import Group
        return Group.objects.filter(timeslot = self).aggregate(Sum('capacity')).get('capacity__sum',0)


    def get_absolute_url(self):
        return reverse('instance.timeslot.view', kwargs={'pk': str(self.course.matching.id), 'v_pk': str(self.course.id),'t_pk':str(self.id)})

    def get_absolute_edit_url(self):
        return reverse('instance.timeslot.edit', kwargs={'pk': str(self.course.matching.id), 'v_pk': str(self.course.id),'t_pk':str(self.id)})

    @classmethod
    def get_timeslot_or_abort(cls, id, matching_id,course_id):
        timeslot = get_object_or_404(TimeSlot, pk=id)
        cv = timeslot.course
        if cv.id != course_id or cv.matching.id != matching_id:
            raise Http404
        return timeslot

    @classmethod
    def is_valid_id(cls, id_string):
        if id_string:
            if len(id_string) == ID_LENGTH:
                return True
        return False


    @classmethod
    def generate_id(cls):
        return keys.get_random_string(ID_LENGTH)

    class Meta:
        app_label = 'bundled_matching'
        db_table = 'timeslot'
        verbose_name = 'Timeslot'
