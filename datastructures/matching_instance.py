# Aykut Uzunoglu
from bundled_matching.datastructures.preference_list import PreferenceList

# Preferences and capacities
class MatchingInstance(object):
    def __init__(self, students_to_prefs=dict(), courses_to_timeslots = dict(), groups = list()):
        self.__stud_preferences = dict()
        self.__courses_to_timeslots = dict()
        self.__timeslots = list()
        self.__groups = list(groups)
        self.__ts_to_group = dict()
        self.__init_students(students_to_prefs)
        self.__init_courses(courses_to_timeslots)

    def __init_students(self, students):
        if not isinstance(students, dict):
            raise TypeError('Dict expected!')
        for stud, preference in students.items():
            self.insert_student(stud, preference)

    def __init_courses(self, courses):
        if not isinstance(courses, dict):
            raise TypeError('Dict expected!')
        for course,timeslots in courses.items():
            self.insert_course(course,timeslots)

    def insert_student(self, student, p_stud):
        self.__stud_preferences[student] = p_stud


    def insert_course(self, course,timeslots):
        timeslots = list(timeslots)
        self.__courses_to_timeslots[course] = timeslots
        self.__timeslots.extend(timeslots)

    #all students, even with empty preferences
    def get_students(self):
        return self.__stud_preferences.keys()

    def get_ts_to_group(self):
        ts_gr = dict()
        for c, ts_list in self.__courses_to_timeslots.iteritems():
            for ts in ts_list:
                ts_gr[ts] = ts.groups
        return ts_gr

    def get_groups(self):
        return self.__groups

    def get_courses(self):
        return self.__courses_to_timeslots.keys()

    def get_course_to_timeslots(self):
        return self.__courses_to_timeslots

    def get_timeslots(self):
        return self.__timeslots

    def get_capacity_p(self, p):
        return 1

    def get_capacity_ts(self, ts):
        return ts.capacity


    def get_preferences_stud(self, stud):
        if stud not in self.__stud_preferences:
            raise IndexError(str(stud) + ' not in P!')
        return self.__stud_preferences[stud]

    def get_prefered_preferences_stud(self,stud):
        if stud not in self.__stud_preferences:
            raise IndexError(str(stud) + ' not in P!')
        return self.__stud_preferences[stud].get_prefered_pref_dict()

    def get_acceptable_preferences_stud(self,stud):
        if stud not in self.__stud_preferences:
            raise IndexError(str(stud) + ' not in P!')
        return self.__stud_preferences[stud].get_acceptable_pref_list()
