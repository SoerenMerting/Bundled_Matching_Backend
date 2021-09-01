# Franz Diebold

from bundled_matching.datastructures.preference_list import PreferenceList, INVISIBLE
import random
import copy


class TieBreaker(object):
    def __init__(self, seed=random.random()):
        self.__seed = seed

    def break_ties(self, preference_list_with_ties):
        preference_list_tie_broken = PreferenceList()

        random.seed(self.__seed)
        rank = 1
        for p_rank in sorted(preference_list_with_ties.ranks()):
            shuffled_items = copy.copy(preference_list_with_ties[p_rank])
            random.shuffle(shuffled_items)
            if p_rank < INVISIBLE:
                for item in shuffled_items:
                    preference_list_tie_broken.add_item(rank, item)
                    rank += 1
            else:
                for item in shuffled_items:
                    preference_list_tie_broken.add_item(INVISIBLE, item)

        return preference_list_tie_broken
