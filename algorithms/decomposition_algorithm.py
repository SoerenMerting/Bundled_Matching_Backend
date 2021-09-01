#Aykut Uzunoglu
from bundled_matching.models.instance_m import Instance
from collections import OrderedDict
from sets import Set
import random, itertools, math
import logging


from bundled_matching.datastructures.assignment import Assignment
from bundled_matching.datastructures.matching_instance import MatchingInstance
from bundled_matching.datastructures.preference_list import PreferenceList
from bundled_matching.datastructures.tie_breaker import TieBreaker
from bundled_matching.algorithms.bundled_probabilistic_serial import BundledProbabilisticSerial
from bundled_matching.maxcu_interface import MaxcuInterface
from django.db.models import Max


class DecompositionAlgorithm(object):
    def __init__(self, matching_algorithm):
        if not isinstance(matching_algorithm, BundledProbabilisticSerial):
            raise TypeError('BPS expected!')
        self.__matching_algorithm = matching_algorithm
        self.__bundles = matching_algorithm.get_bundles()
        #matching_instance has the required information for the matching
        self.__matching_instance = matching_algorithm.get_matching_instance()
        self.__students = self.__matching_algorithm.get_students()
        self.__timeslots = self.__matching_algorithm.get_timeslots()
        self.__bundles = self.__matching_algorithm.get_bundles()
        self.__preferences = self.__matching_algorithm.get_preferences()
        self.__frac_matching_as_list = list()
        self.__fractional_matching = dict()
        self.__lambdas = []
        self.__vectors =[]
        self.__convex_combination = list()


    def decompose(self):
        frac_matching = self.__matching_algorithm.get_cleaned_result()
        self.__frac_matching_as_list = self.parse_matching_to_list(frac_matching)
        self.__fractional_matching = frac_matching
        maxcu = MaxcuInterface(self.__students,self.__timeslots,self.__bundles,self.__preferences,self.__frac_matching_as_list,frac_matching)
        maxcu.match()
        self.__lambdas = maxcu.getLambdas()
        self.__vectors = maxcu.getVectors()
        # self.__init_convex_combination()

    def parse_matching_to_list(self,matching):
        result = list()
        for stud in matching:
            result.extend(matching[stud].values())
        return result

    def get_lambdas(self):
        return self.__lambdas

    def get_vectors(self):
        return self.__vectors

    def randomVector(self, seed_random = random.random()):
        random.seed(seed_random)
        x = random.random()
        for i in range(len(self.__vectors)):
            if(x<=self.__lambdas[i]):
                return self.__vectors[i]
            else:
                x -= self.__lambdas[i]
        return []

    def __init_convex_combination(self):
        multiplied = [map(lambda x: x*self.__lambdas[i],self.__vectors) for i in range(len(self.__lambdas))]
        sum_of_list = reduce(lambda x,y:map(lambda x,y:x+y,x,y),multiplied)
        self.__convex_combination = sum_of_list

    def get_convex_combination(self):
        return self.__convex_combination

    def get_error(self):
        if len(self.__convex_combination) == len(self.__frac_matching_as_list):
            return map(lambda x,y:x-y,self.__convex_combination,self.__frac_matching_as_list)
        return [-100,-100]

    def get_assignment(self):
    	self.decompose()
    	vector = self.randomVector()
    	counter = 0
    	length_bundle_list = len(self.__bundles)
    	assignment = []
    	for stud in self.__fractional_matching:
    		for bundle in self.__fractional_matching[stud]:
    			if vector[counter] == 1:
    				assignment.append((stud,bundle))
    			counter += 1
    	return assignment


	def get_matching_as_dict(self,result):
		all_bundles = self.__bundles
		students = self.__matching_instance.get_students()
		rs_vec = OrderedDict()
		for stud in students:
			rs_vec[stud] = OrderedDict()
			for bundle in all_bundles:
					rs_vec[stud][bundle] = result[stud].get(bundle,0.0)
		return rs_vec

	#return matching as a one-dimensional vector
    def matching_as_list(self,result):
        all_bundles = self.__bundles
        students = self.__matching_instance.get_students()
        rs_vec = []
        for stud in students:
            stud_vec = []
            for bundle in all_bundles:
                if stud in result and bundle in result[stud]:
                    stud_vec.append(result[stud][bundle])
            rs_vec.extend(stud_vec)
        return rs_vec
