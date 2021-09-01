#Aykut Uzunoglu
from datetime import datetime

from django.db import models
from django.core.urlresolvers import reverse
from django.utils.text import slugify
from django.core.validators import MinValueValidator, MaxValueValidator

from bundled_matching.models.m_stud_vote import M_StudVote
from bundled_matching.models.timeslot import TimeSlot
from bundled_matching.datastructures.preference_list import ACCEPTABLE

class PreferenceList(models.Model):

    stud_vote = models.ForeignKey(M_StudVote, related_name='b_m_bundle', on_delete=models.CASCADE, db_index =True, editable=False)
    bundle = models.ManyToManyField(TimeSlot, related_name='bundle_timeslots',  db_index =True)
    rank = models.PositiveIntegerField(blank=False, verbose_name='Rank', default=ACCEPTABLE, validators=[MinValueValidator(1)])
    second_round = models.PositiveSmallIntegerField(blank=False, default=1, verbose_name='Preference of Second Round',validators=[MinValueValidator(1)])

    class Meta:
        app_label = 'bundled_matching'
        db_table = 'preference_list'
        verbose_name = 'PreferenceList'

    def __str__(self):
        all_items = list(self.bundles_list)
        if not all_items:
            return ""
        string = ""
        for item in all_items[:-1]:
            string+=str(item)+" | "
        string+=" "+str(all_items[-1])
        return string

    @property
    def bundles_list(self):
        return self.bundle.all()

    def as_tuple(self):
        return frozenset(list(self.bundle.all()))

    def add_items(self, bundle_items):
        bundle = list(bundle_items)
        self.bundle.add(*bundle)
        self.save()
