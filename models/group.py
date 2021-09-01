#Aykut Uzunoglu
from datetime import datetime

from django.db import models
from django.shortcuts import get_object_or_404
from django.core.urlresolvers import reverse
from django.utils.text import slugify
from django.core.validators import MinValueValidator, MaxValueValidator, MinLengthValidator
from bundled_matching.models.instance_m import MAX_LENGTH

from bundled_matching.models.m_course_vote import M_CourseVote
from bundled_matching.models.timeslot import TimeSlot

class Group(models.Model):

    days_choices = (
        ('Monday', 'Monday'),
        ('Tuesday', 'Tuesday'),
        ('Wednesday', 'Wednesday'),
        ('Thursday', 'Thursday'),
        ('Friday', 'Friday'),)

    day = models.CharField(max_length=15, choices = days_choices)
    begin = models.TimeField(blank=False, null=True )
    end = models.TimeField(blank=False, null=True )
    lv_id = models.TextField(default="",verbose_name="'Lehrveranstaltungs-ID' in TUMOnline")
    name = models.CharField(max_length=MAX_LENGTH, blank=True, verbose_name='Name', validators=[MinLengthValidator(2)])
    capacity = models.PositiveSmallIntegerField(blank=False, verbose_name='Capacity', default=1, validators=[MinValueValidator(0), MaxValueValidator(9999)] )
    course = models.ForeignKey(M_CourseVote, related_name='b_m_course_group', on_delete=models.CASCADE , editable=False )
    timeslot = models.ForeignKey(TimeSlot, related_name='b_m_timeslot_group', on_delete = models.CASCADE, null=True, blank = True)
    assigned_capacity  = models.PositiveSmallIntegerField(blank=False, verbose_name='Capacity', default=0, validators=[MinValueValidator(0), MaxValueValidator(9999)] )
    location = models.CharField(blank=True, max_length=100)
    is_english = models.BooleanField(blank=False, default=False, verbose_name='English', help_text='Is it in English')

    def __str__(self):
        add_info = ""
        if self.is_english:
            add_info = "(ENG)"
        return "Group"+add_info+":" + str(self.day)[:3]+'. ' + str(self.begin)[:-3] + ' - ' + str(self.end)[:-3]

    def is_free(self):
    	return self.assigned_capacity<self.capacity

    def assign_student(self):
    	self.assigned_capacity = self.assigned_capacity + 1
        self.save()

    def get_assigned_students(self):
        from bundled_matching.models.pair import Pair
        pairs = Pair.objects.filter(groups__id = self.id)
        students = [pair.student for pair in pairs]
        return students


    @classmethod
    def get_group_or_abort(cls, id, course_id):
        group = get_object_or_404(Group, pk=id)
        if group.course.id != course_id:
            raise Http404
        return group

    def get_absolute_matching_export_url(self):
        return reverse('instance.group.export_matching', kwargs={'pk': str(self.course.matching.id), 'v_pk': str(self.course.id), 'g_pk':str(self.id)})

    class Meta:
        app_label = 'bundled_matching'
        db_table = 'Group'
        verbose_name = 'Group'
