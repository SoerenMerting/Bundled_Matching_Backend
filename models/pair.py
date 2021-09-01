from django.db import models
from django.utils.functional import cached_property


from bundled_matching.models.instance_m import Instance
from bundled_matching.models.m_stud_vote import M_StudVote
from bundled_matching.models.group import Group
from bundled_matching.models.timeslot import TimeSlot

class Pair(models.Model):
    matching = models.ForeignKey(Instance, related_name='pairs', on_delete=models.CASCADE, db_index=True, editable=False)
    student = models.ForeignKey(M_StudVote)
    groups = models.ManyToManyField(Group, db_table='student_groups')
    timeslot = models.ManyToManyField(TimeSlot, db_table='pair_timeslot')


    def __str__(self):
        return str(self.groups)

    @cached_property
    def timeslots(self):
    	return self.timeslot.all()

    def add_timeslots(self,timeslot_list):
        for timeslot in timeslot_list:
            self.timeslot.add(timeslot)
        self.save()

    @property
    def all_groups(self):
        return self.groups.all()

    class Meta:
        app_label = 'bundled_matching'
        db_table = 'pair'
        verbose_name = 'Pair'

    def initGroup(self):
        #probleme wegen datenbank, verschiebe init group auf assignment objekt
        for ts in self.timeslots:
            group = ts.assign_student()
            self.groups.add(group)
        self.save()
