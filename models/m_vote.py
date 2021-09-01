# Franz Diebold
#python lib imports
from collections import OrderedDict
#django imports
from django.db import models
from django.core.validators import MinValueValidator, MaxValueValidator
#bundled_matching imports
from bundled_matching.util import keys
from bundled_matching.datastructures.preference_list import ACCEPTABLE, INVISIBLE
import logging
ID_LENGTH = 10


def _validate_id(value):
    return BMVote.is_valid_id(value)


class BMVote(models.Model):
    id = models.CharField(max_length=(ID_LENGTH), blank=False, primary_key=True, db_index=True, editable=False ,validators=[_validate_id])

    created = models.DateTimeField(blank=True,null=True, auto_now_add=True, verbose_name='Creation date', editable=False)
    modified = models.DateTimeField(blank=True,null=True, auto_now=True, verbose_name='Modification date', editable=False)

    class Meta:
        abstract = True
        app_label = 'bundled_matching'


    def _get_value_label_tags_dict(self, votes):
        values = map(lambda vote: vote.id, votes)
        labels = map(lambda vote: vote.label_name, votes)
        tags = map(lambda vote: vote.tags, votes)
        return OrderedDict(zip(values, zip(labels, tags)))


    def save(self, force_insert=False, force_update=False, using=None, update_fields=None):
        if not BMVote.is_valid_id(self.id):
            self.id = BMVote.generate_id()
        super(BMVote, self).save(force_insert=force_insert, force_update=force_update, using=using, update_fields=update_fields)


    @classmethod
    def get_object_or_none(cls, model, **kwargs):
        try:
            return model.objects.get(**kwargs)
        except model.DoesNotExist:
            return None

    @property
    def label_name(self):
        raise NotImplementedError("Please implement this method.")

    @property
    def tags(self):
        raise NotImplementedError("Please implement this method.")

    @classmethod
    def get_filters(cls):
        raise NotImplementedError("Please implement this method.")


    @classmethod
    def is_valid_id(cls, id_string):
        if id_string:
            if len(id_string) == ID_LENGTH:
                return True
        return False


    @classmethod
    def generate_id(cls):
        return keys.get_random_string(ID_LENGTH)

    @classmethod
    def get_choices_list(cls, number_of_options):
        choices = []
        for i in range(1, number_of_options + 1):
            choices.append((i, str(i)))
        choices.append((ACCEPTABLE, '-'))
        choices.append((INVISIBLE, 'invisible'))

        return choices


    def check_read_permission(self, user):
        raise NotImplementedError("Please implement this method.")

    def check_admin_read_permission(self, user):
        raise NotImplementedError("Please implement this method.")

    def check_write_permission(self, user):
        raise NotImplementedError("Please implement this method.")


    def get_full_preference_list(self):
        raise NotImplementedError("Please implement this method.")
