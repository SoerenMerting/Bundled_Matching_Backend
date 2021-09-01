# Aykut Uzunoglu

from bundled_matching.datastructures.matching_instance import MatchingInstance


NOT_MATCHED = 9990000


# many-to-many assignment: P and Q
# no overassignments
class Assignment(object):
    # Everything is stored in two Dictionaries
    def __init__(self, matching_instance, matched_pairs_list=list()):
        if not isinstance(matching_instance, MatchingInstance):
            raise TypeError('MatchingInstance expected!')

        self.__matching_instance = matching_instance

        self.__p_assignments = {}
        self.__q_assignments = {}

        for p in matching_instance.get_p():
            self.__p_assignments[p] = []
        for q in matching_instance.get_q():
            self.__q_assignments[q] = []

        if matched_pairs_list is not None:
            if not isinstance(matched_pairs_list, list):
                raise TypeError('List expected!')

            for (p, q) in matched_pairs_list:
                self.match(p, q)

    def get_p_assignments(self):
        return self.__p_assignments

    def get_underassigned_p_assignments(self):
        underassigned_p_assignments = {}
        for p, q_s in self.get_p_assignments().items():
            if not self.is_full_p(p):
                underassigned_p_assignments[p] = q_s
        return underassigned_p_assignments

    def get_assignment_for_p(self, p):
        if not p in self.__p_assignments:
            raise ValueError(str(p) + ' not in P!')
        return self.__p_assignments[p]

    def get_q_assignments(self):
        return self.__q_assignments

    def get_underassigned_q_assignments(self):
        underassigned_q_assignments = {}
        for q, p_s in self.get_q_assignments().items():
            if not self.is_full_q(q):
                underassigned_q_assignments[q] = p_s
        return underassigned_q_assignments

    def get_assignment_for_q(self, q):
        if not q in self.__q_assignments:
            raise ValueError(str(q) + ' not in Q!')
        return self.__q_assignments[q]

    def export_Q_assignments(self):
        Q_assignments = []
        for (q, q_assignment) in self.__q_assignments.items():
            Q_assignments.append((q, ';'.join(q_assignment)))

        return Q_assignments

    def get_assignment_pairs(self):
        assignment_pairs = []

        for (p, p_assignment) in self.__p_assignments.items():
            for q in p_assignment:
                assignment_pairs.append((p, q))

        return assignment_pairs

    def get_matching_instance(self):
        return self.__matching_instance

    def get_ranks_p(self, p):
        if p not in self.__p_assignments:
            raise ValueError(str(p) + ' not in P!')
        ranks_p = self.__matching_instance.get_preferences_p(p).get_ranks_from_set(self.__p_assignments[p])
        number_not_matched = self.__matching_instance.get_capacity_p(p) - len(ranks_p)
        ranks_p.extend(number_not_matched * [NOT_MATCHED])
        return ranks_p

    def get_ranks_q(self, q):
        if q not in self.__q_assignments:
            raise ValueError(str(q) + ' not in Q!')
        ranks_q = self.__matching_instance.get_preferences_q(q).get_ranks_from_set(self.__q_assignments[q])
        number_not_matched = self.__matching_instance.get_capacity_q(q) - len(ranks_q)
        ranks_q.extend(number_not_matched * [NOT_MATCHED])
        return ranks_q

    def get_one_of_worst_assigned_p(self, p):
        return self.__matching_instance.get_preferences_p(p).get_one_of_worst_items_from_set(self.__p_assignments[p])

    def get_one_of_worst_assigned_q(self, q):
        return self.__matching_instance.get_preferences_q(q).get_one_of_worst_items_from_set(self.__q_assignments[q])

    # count the number of matched pairs in the Matching
    def __len__(self):
        return sum(len(p_assignment) for p_assignment in self.__p_assignments.values())

    def __nonzero__(self):
        return self.__len__() > 0

    def __iter__(self):
        return self.get_assignment_pairs().__iter__()

    def __eq__(self, other):
        if isinstance(other, Assignment):
            return set(other.get_assignment_pairs()) == set(self.get_assignment_pairs())

        return NotImplemented

    def __ne__(self, other):
        result = self.__eq__(other)
        if result is NotImplemented:
            return result
        return not result

    def clear(self):
        for p in self.__p_assignments:
            self.__p_assignments[p] = []

        for q in self.__q_assignments:
            self.__q_assignments[q] = []

    def __str__(self):
        output = ''
        for (q, q_assignment) in self.__q_assignments.items():
            output += str(q) + ': ' + str(q_assignment) + '\n'
        return output

    def get_number_of_matches_p(self, p):
        if p not in self.__p_assignments:
            raise ValueError(str(p) + ' not in P!')
        return len(self.__p_assignments[p])

    def get_number_of_matches_q(self, q):
        if q not in self.__q_assignments:
            raise ValueError(str(q) + ' not in Q!')
        return len(self.__q_assignments[q])

    def is_full_p(self, p):
        return self.get_number_of_matches_p(p) >= self.__matching_instance.get_capacity_p(p)

    def is_full_q(self, q):
        return self.get_number_of_matches_q(q) >= self.__matching_instance.get_capacity_q(q)

    def is_matched(self, p, q):
        return (q in self.__p_assignments.get(p, [])) and (p in self.__q_assignments.get(q, []))

    def match(self, p, q):
        if self.is_matched(p, q):
            return

        if p not in self.__p_assignments:
            raise ValueError(str(p) + ' not in P!')

        self.__p_assignments[p].append(q)
        self.__q_assignments[q].append(p)

    def un_match(self, p, q):
        if not self.is_matched(p, q):
            raise ValueError(str(p) + ' and ' + str(q) + ' are not matched!')

        self.__p_assignments[p].remove(q)
        self.__q_assignments[q].remove(p)
