from datetime import datetime
#django imports
from django.db import models
from django.utils.functional import cached_property
from django.core.urlresolvers import reverse
from django.utils.text import slugify
from django.core.validators import MinValueValidator, MaxValueValidator
#bundled_matching imports
from app.models.tum_user import TUMUser
from bundled_matching.models.instance_m import Instance
from bundled_matching.models.m_stud_vote import M_StudVote
from bundled_matching.models.m_course_vote import M_CourseVote
import json


class TestCase(models.Model):
    instance = models.ForeignKey(Instance, blank=False, db_index=True, editable=False)
    test_user = models.ManyToManyField(TUMUser)
    vectors = models.CharField(default='', max_length=10000000, null=True)
    lambdas = models.CharField(default='', max_length=100000, null=True)



    @cached_property
    def test_courses(self):
        return self.instance.course_votes

    @cached_property
    def test_votes(self):
        return self.instance.stud_votes

    @cached_property
    def all_students(self):
        return self.test_user.all()

    def delete_users(self):
        for user in self.test_user.all():
            user.delete()

    def add_all_users(self, users):
        for user in users:
            self.test_user.add()

    def set_lambdas(self, lambdas):
        self.lambdas = json.dumps(list(lambdas))

    def get_lambdas(self):
        if not self.lambdas:
            return list()
        return json.loads(self.lambdas)

    def set_vectors(self, vectors):
        self.vectors = json.dumps(list(vectors))

    def get_vectors(self):
        if not self.vectors:
            return list()
        return json.loads(self.vectors)

    class Meta:
        app_label = 'bundled_matching'
        db_table = 'test_case'
        verbose_name = 'test_case'
