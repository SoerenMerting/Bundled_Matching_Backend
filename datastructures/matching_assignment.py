# Aykut Uzunoglu
import logging
class MatchingAssignment(object):

    def __init__(self, matching_instance,pairs=list(), instance = None):
        self.__stud_to_bundle = dict()
        self.__stud_to_group = dict()
        self.__timeslot_to_stud = dict()
        self.__course_to_stud = dict()
        self.__matching_instance = matching_instance
        self.__stud_bundle_list = list()

        self.__g_assigned_capacity = dict()
        self.__ts_assigned_capacity = dict()
        self.__g_capacity = dict()
        self.__instance = instance
        self.__ts_capacity = dict()
        self.__init_capacities()
        self.__ts_to_group = matching_instance.get_ts_to_group()
        self.__pairs = list(pairs)
        if pairs:
            self.__init_dicts(pairs = pairs)

    def __init_dicts(self,pairs):
        for (student,bundle) in pairs:
            self.add_pair(stud = student, bundle = bundle)

    def __init_ts_to_group(self,ts_to_group):
        ts_group_dict_without_zeros = {key:filter(lambda x:x>0, value_list) for key,value_list in ts_to_group.iteritems()}
        return ts_group_dict_without_zeros

    def __init_capacities(self):
        groups = self.__instance.get_groups()
        for group in groups:
            self.__g_capacity[group] = group.capacity
            self.__g_assigned_capacity[group] = group.assigned_capacity
        timeslots = self.__matching_instance.get_timeslots()
        for ts in timeslots:
            self.__ts_capacity[ts] = ts.capacity
            self.__ts_assigned_capacity[ts] = ts.assigned_capacity

    def __str__(self):
        return str(self.__stud_bundle_list)

    def pair_students(self):
        from bundled_matching.models.pair import Pair
        for (stud,bundle) in self.__pairs:
            pair_db_obj = Pair(student = stud,matching = self.__instance)
            pair_db_obj.save()
            pair_db_obj.add_timeslots(bundle)
            for timeslot in bundle:
                group = self.get_group(timeslot)
                pair_db_obj.groups.add(group)
                self.assign_group(group)
            pair_db_obj.save()
        self.update_assigned_capacities()

    def get_group(self,timeslot):
        if self.__ts_assigned_capacity[timeslot] >= self.__ts_capacity[timeslot]:
            pointer = (self.__ts_assigned_capacity[timeslot] % len(self.__ts_to_group[timeslot]))
        else:
            pointer = (self.__ts_assigned_capacity[timeslot] % len(self.__ts_to_group[timeslot]))
            first_group = self.__ts_to_group[timeslot][pointer]
            group = self.__ts_to_group[timeslot][pointer]
            while not (self.__g_assigned_capacity[group] < self.__g_capacity[group]):
                pointer +=1
                pointer = pointer % len(self.__ts_to_group[timeslot])
                group = self.__ts_to_group[timeslot][pointer]
            return group
        return self.__ts_to_group[timeslot][pointer]

    def assign_group(self,group):
        self.__ts_assigned_capacity[group.timeslot] += 1
        self.__g_assigned_capacity[group] += 1

    def get_studs(self):
        return self.__stud_to_bundle.keys()

    def add_pair(self,stud, bundle):
        self.__stud_bundle_list.append((stud,bundle))
        self.__stud_to_bundle[stud] = bundle
        for timeslot in bundle:
            if timeslot in self.__timeslot_to_stud:
                self.__timeslot_to_stud[timeslot].append(stud)
            else:
                self.__timeslot_to_stud[timeslot] = list()
                self.__timeslot_to_stud[timeslot].append(stud)
            course = timeslot.course
            if course in self.__course_to_stud:
                if not stud in self.__course_to_stud[course]:
                    self.__course_to_stud[course].append(stud)
            else:
                self.__course_to_stud[course] = list()
                self.__course_to_stud[course].append(stud)

    def update_assigned_capacities(self):
        for timeslot in self.__ts_assigned_capacity:
            timeslot.assigned_capacity = self.__ts_assigned_capacity[timeslot]
            timeslot.save()
        for group in self.__g_assigned_capacity:
            group.assigned_capacity = self.__g_assigned_capacity[group]
            group.save()
    def get_bundle_of_stud(self, stud):
        return self.__stud_to_bundle.get(stud)

    def get_matched_rank_stud(self,stud):
        bundle = self.get_bundle_of_stud(stud)
        rank = stud.preferences.get_rank_of_item(bundle)
        return rank

    def get_courses(self):
        return self.__course_to_stud.keys()

    # def get_studs_of_timeslot(self, timeslot):
        # return self.__timeslot_to_stud.get(timeslot)
    #
    # def get_studs_of_course(self,course):
    #     return self.__course_to_stud.get(course)
    #
    # def get_studs_of_group(self,group):
    #     return self.__group_to_stud.get(group)
    #
    def export_list(self):
        return self.__pairs
