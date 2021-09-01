#Aykut Uzunoglu
import collections
import sys
from collections import OrderedDict
import logging

class RankError(KeyError):
    pass


# ACCEPTABLE = 900000
# INVISIBLE = 990000
ACCEPTABLE = 999999
INVISIBLE = 1000000

# Only acceptable (rank 1-89999) and unacceptable values are stored, no ACCEPTABLE values
class PreferenceList(object):
    # Everything is stored in a Dictionary and a reverse Dictionary
    # __pref_dict:    1 -> [a, b], 2 -> [c]
    # __reverse_dict: a -> 1,      b -> 1,    c -> 2
    def __init__(self, pref_dict=dict()):
        self.__pref_dict = {}
        self.__reverse_dict = {}

        if pref_dict:
            self.__import_dict(pref_dict)

    # count the number of values stored in the PreferenceList
    def __len__(self):
        return self.__reverse_dict.__len__()

    # return whole list for rank
    def __getitem__(self, rank):
        if not isinstance(rank, int):
            raise TypeError('Rank has to be an integer!')

        if rank not in self.__pref_dict:
            raise RankError('Rank does not exist!')

        return self.__pref_dict.__getitem__(rank)

    # set whole list of rank
    def __setitem__(self, rank, items):
        if not isinstance(items, list):
            raise TypeError('Items has to be a list!')

        for item in items:
            self.add_item(rank, item)

    def __delitem__(self, rank):
        if not isinstance(rank, int):
            raise TypeError('Rank has to be an integer!')

        if rank not in self.__pref_dict:
            raise RankError('Rank does not exist!')

        items = self.__pref_dict[rank]
        for item in items:
            self.remove_item(item)

    def __iter__(self):
        return self.__reverse_dict.__iter__()

    def __contains__(self, item):
        return item in self.__reverse_dict

    def __eq__(self, other):
        if isinstance(other, PreferenceList):
            return self.__reverse_dict == other.__reverse_dict
        return NotImplemented

    def __ne__(self, other):
        result = self.__eq__(other)
        if result is NotImplemented:
            return result
        return not result

    def clear(self):
        self.__pref_dict.clear()
        self.__reverse_dict.clear()

    # return a list of the ranks
    def ranks(self):
        return self.__pref_dict.keys()

    def actived_ranked(self):
        active_ranked_ranks = filter(lambda x:x < ACCEPTABLE, self.__pref_dict.keys())
        active_ranked = dict()
        for rank in active_ranked_ranks:
            active_ranked[rank] = self.__pref_dict[rank]
        return active_ranked

    def active_ranked_ranks(self):
        ranks = self.ranks()
        #remove acc and invacc
        if ACCEPTABLE in ranks:
            ranks.remove(ACCEPTABLE)
        if INVISIBLE in ranks:
            ranks.remove(INVISIBLE)
        return ranks

    def max_active_rank(self):
        a_r_r = self.active_ranked_ranks()
        if a_r_r:
            return max(self.active_ranked_ranks())
        else:
            return 0

    def acceptable_ranked(self):
        if ACCEPTABLE in self.__pref_dict:
            return self.__pref_dict[ACCEPTABLE]
        else:
            return []

    def invisible_ranked(self):
        return self.__pref_dict.get(INVISIBLE,list())

    def acceptable_ranks(self):
        # return filter(lambda x: x < INVISIBLE, self.__pref_dict.keys())
        # return self.__pref_dict.get(ACCEPTABLE,list())
        return filter(lambda x : x < INVISIBLE , self.__pref_dict.keys())

    def max_acceptable_rank(self):
        acceptable_ranks = self.acceptable_ranks()
        if acceptable_ranks:
            return max(acceptable_ranks)
        else:
            return 1

    # return a list of the values stored in the PreferenceList
    def values(self):
        return self.__reverse_dict.keys()

    def get_bundle_length(self):
        if self.__reverse_dict.keys():
            return len(self.__reverse_dict.keys()[0])
        else:
            return 0

    def get_tutorials_list(self):
        if not self.__reverse_dict.keys():
            return []
        tuts = list(self.__reverse_dict.keys()[0])
        tut_courses = [tut.course for tut in tuts]
        return tut_courses

    def get_pref_dict(self):
        return self.__pref_dict

    def get_prefered_pref_dict(self):
        prefered_pref_dict = dict()
        for rank, preflist in self.__pref_dict.iteritems():
            if rank < ACCEPTABLE:
                prefered_pref_dict[rank] = preflist
        return prefered_pref_dict

    def get_acceptable_pref_list(self):
        return self.__pref_dict.get(ACCEPTABLE)

    def acceptable_values(self):
        return dict((option, rank) for option, rank in self.__reverse_dict.iteritems() if rank < ACCEPTABLE).keys()

    def is_acceptable(self, item):
        return self.get_rank_of_item(item) < INVISIBLE

    def __str__(self):
        return str(self.__pref_dict)

    def add_item(self, rank, item):
        if not isinstance(rank, int):
            raise TypeError('Rank has to be an integer!')
        elif rank < 1:
            raise TypeError('Rank has to be true positive!')
        if self.__contains__(item):
            self.remove_item(item)
        if rank not in self.__pref_dict:
                # add new list
            self.__pref_dict[rank] = [item]
        else:
                # append to list
            self.__pref_dict[rank].append(item)
        self.__reverse_dict[item] = rank

    def remove_item(self, item):
        if self.__contains__(item):
            rank = self.__reverse_dict[item]
            del self.__reverse_dict[item]
            self.__pref_dict[rank] = filter(lambda a: a != item, self.__pref_dict[rank])
            if not self.__pref_dict[rank]:
                # remove empty list from dict
                del self.__pref_dict[rank]
            return rank
        else:
            return None

    def __import_dict(self, pref_dict):
        if not isinstance(pref_dict, dict):
            raise TypeError('Dictionary expected!')
        self.unnormalize(pref_dict)

    def export_dict(self):
        return self.__pref_dict

    def get_rank_of_item(self, item):
        if self.__contains__(item):
            return self.__reverse_dict[item]
        else:
            return ACCEPTABLE

    def get_reverse_dict(self):
        return self.__reverse_dict

    def compare_items(self, i1, i2):
        r1 = self.get_rank_of_item(i1)
        r2 = self.get_rank_of_item(i2)
        return PreferenceList.rank_compare(r1, r2)

    def compare_items_with_ties(self, i1, i2, seed):
        comp = self.compare_items(i1, i2)
        if comp == 0:
            h1 = hash((i1, seed))
            h2 = hash((i2, seed))
            return PreferenceList.rank_compare(h1, h2)
        else:
            return comp

    @classmethod
    def rank_compare(cls, rank1, rank2):
        return cmp(rank2, rank1)

    def responsive_set_compare(self, set1, set2):
        return PreferenceList.responsive_ranks_compare(self.get_ranks_from_set(set1), self.get_ranks_from_set(set2))

    @classmethod
    def responsive_ranks_compare(cls, ranks1, ranks2):
        len_dif = len(ranks1) - len(ranks2)
        if len_dif > 0:
            ranks2.extend([sys.maxint] * abs(len_dif))
        elif len_dif < 0:
            ranks1.extend([sys.maxint] * abs(len_dif))

        rank_differences = [r1 - r2 for r1, r2 in zip(sorted(ranks1), sorted(ranks2))]
        s1_p_s2 = sum(rd < 0 for rd in rank_differences)
        s2_p_s1 = sum(rd > 0 for rd in rank_differences)

        if s1_p_s2 > 0 and s2_p_s1 == 0:
            return 1
        elif s1_p_s2 == 0 and s2_p_s1 > 0:
            return -1
        elif s1_p_s2 == 0 and s2_p_s1 == 0:
            return 0
        else:
            return None

    def get_ranks_from_set(self, items):
        ranks = []
        for item in items:
            ranks.append(self.get_rank_of_item(item))
        return ranks

    def to_flat_list(self):
        flat_list = []
        for rank in sorted(self.acceptable_ranks()):
            flat_list.extend(self.__getitem__(rank))
        return flat_list

    def as_list(self):
        flat_list = list()
        for rank, pref_list in self.__pref_dict.iteritems():
            flat_list.extend(pref_list)
        return flat_list

    def get_sorted_reverse_dict(self):
        return OrderedDict(sorted(self.__reverse_dict.items(), key=lambda t: t[1]))

    def fill(self, items, fill_rank=None):
        if not isinstance(items, collections.Iterable):
            raise TypeError('items must be iterable!')
        if fill_rank:
            if not isinstance(fill_rank, int):
                raise TypeError('fill_rank must be integer!')
            if fill_rank < 1:
                raise ValueError('fill_rank must be positive!')

        if not fill_rank:
            fill_rank = 1
            if self.ranks():
                max_acceptable_rank = self.max_acceptable_rank()
                fill_rank = max_acceptable_rank + len(self.__getitem__(max_acceptable_rank))

        for item in items:
            if not self.__contains__(item):
                self.add_item(fill_rank, item)

    def normalize(self, pref_dict=None):
        if not pref_dict:
            pref_dict = self.__pref_dict.copy()
        self.clear()

        prefs = OrderedDict(sorted(pref_dict.items(), key=lambda t: t[0]))
        cur_rank = 1
        for rank, items in prefs.items():
            for item in items:
                self.add_item(cur_rank, item)
            cur_rank += len(items)

    def unnormalize(self,pref_dict=None):
        if not pref_dict:
            pref_dict = self.__pref_dict.copy()
        self.clear()

        prefs = OrderedDict(sorted(pref_dict.items(), key=lambda t: t[0]))
        for rank, items in prefs.items():
            for item in items:
                self.add_item(rank, item)

    def get_transformation(self, transformation_dict):
        transformed_pref_list = PreferenceList()
        for rank, items in self.__pref_dict.iteritems():
            for item in items:
                if item in transformation_dict:
                    transformed_pref_list.add_item(rank, transformation_dict[item])
        return transformed_pref_list


    def get_full_preferences(self, value_label_tags_dict):
        full_preferences = {}
        for rank, bundles in self.__pref_dict.iteritems():
            full_preferences[rank] = OrderedDict()
            if rank!=INVISIBLE:
                for bundle in bundles:
                    full_preferences[rank][bundle] = value_label_tags_dict[bundle]
        return OrderedDict(sorted(full_preferences.items(), key=lambda t: t[0]))

    def get_ordered_pref_dict(self):
        return OrderedDict(sorted(self.__pref_dict.items(), key = lambda t: t[0]))
