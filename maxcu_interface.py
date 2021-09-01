#Aykut Uzunoglu

#python imports
import random, itertools, math
from sets import Set
from collections import OrderedDict
import logging
#django imports
from django.db.models import Max
#py4j imports
from py4j.java_gateway import JavaGateway
from py4j.java_collections import SetConverter, MapConverter, ListConverter
#bundled_matching imports
from bundled_matching.datastructures.matching_instance import MatchingInstance
from bundled_matching.algorithms.bundled_probabilistic_serial import BundledProbabilisticSerial
from bundled_matching.models.instance_m import Instance

#This class is needed for the communication with the Java implementation of
#the MAXCU algorithm
#The data of the matching instance has to be passed to the MAXCU algorithm
#before the calculation can be triggered
class MaxcuInterface(object):


	def __init__(self, students, timeslots, all_bundles ,preferences, solution,solution_dict):
		#Set needed information
		self._students = solution_dict.keys()
		self._stud_to_id = OrderedDict(sorted((map(lambda a,b:(a,b),self._students,range(len(self._students)))), key = lambda t: t[1]))
		self._timeslots = timeslots
		self._ts_to_id = OrderedDict(sorted((map(lambda a,b:(a,b),self._timeslots,range(len(self._timeslots)))), key = lambda t: t[1]))

		self._solution = solution
		self._all_bundles = all_bundles
		self._bundle_to_id = OrderedDict(sorted((map(lambda a,b:(a,b),self._all_bundles,range(len(self._all_bundles)))), key = lambda t: t[1]))
		self._preferences = self.create_preferences(bps_matching=solution_dict)

		#Setting up the connection to java
		self._gw = JavaGateway()
		self._solver = self._gw.entry_point


		self._lambdas = []
		self._vectors = []
		self._max_bundle_size = self.get_maxBundleSize()
		self._max_pref_list_size = self.get_maxPrefListSize()

	def create_preferences(self,bps_matching):
		preferences = OrderedDict()
		for stud in bps_matching:
			preferences[stud] = list()
			for pref_pos in bps_matching[stud]:
				preferences[stud].append(pref_pos)
		return preferences

	def create_solution_vector(self, solution):
		return []

	def addTimeslots(self):
		ts_ids = []
		ts_capacities = []
		for ts in self._timeslots:
			ts_ids.append(self._ts_to_id[ts])
			ts_capacities.append(ts.capacity)
		ts_ids_java = ListConverter().convert(ts_ids,self._gw._gateway_client)
		ts_capacities_java = ListConverter().convert(ts_capacities, self._gw._gateway_client)
		self._solver.addTimeslots(ts_ids_java,ts_capacities_java)

	def addClients(self):
		client_id_list =[]
		for stud in self._students:
			client_id_list.append(str(self._stud_to_id[stud]))
		client_id_list_java = ListConverter().convert(client_id_list,self._gw._gateway_client)
		self._solver.addClientList(client_id_list_java)

	def addBundles(self):
		self._solver.initBundlearray(len(self._all_bundles))
		for bundle in self._all_bundles:
			ts_of_bundle = list(bundle)
			ts_list = map(lambda x: self._ts_to_id[x], ts_of_bundle)
			java_ts_list = ListConverter().convert(ts_list,self._gw._gateway_client)
			self._solver.addBundle(self._bundle_to_id[bundle],java_ts_list)


	def match(self):
		solution_vector = self._solution
		#Create objects on java side
		self._solver.init(self._max_bundle_size)
		#Create slots on java side
		self.addTimeslots()
		#Create Clients on java side
		self.addClients()
		#Create Bundles on java side
		self.addBundles()
		#Create Prefrence Table on java side
		self._solver.initPrefTable()
		self.addAllPrefs()
		#Convert the vector into a list in java
		java_vector = ListConverter().convert(solution_vector, self._gw._gateway_client)
		self._solver.initMM()
		self._solver.writeDMatrix()
		#Create MAXCU object on java side and start computation
		self._solver.createMaxcu(0.5, 0.5, java_vector, 0.0)
		#Get the lambdas of the decomposition lottery
		self._lambdas = self._solver.getLambdas()
		#Get the vectors of the lottery
		self._vectors = self._solver.getVectors()

	def getLambdas(self):
		return list(self._lambdas)

	def getVectors(self):
		return list(self._vectors)

	#MAXCU needs the size of the largest Bundle
	def get_maxPrefListSize(self):
		all_studs = self._students
		max_prefListSize = 0
		for student in all_studs:
			preferencelist_of_stud = self._preferences[student]
			temp = len(list(preferencelist_of_stud))
			if (max_prefListSize < temp):
				max_prefListSize = temp
		return max_prefListSize

	def get_maxBundleSize(self):
		max_bundle_size = 0
		for bundle in self._all_bundles:
			temp = len(bundle)
			if(temp>max_bundle_size):
				max_bundle_size = temp
		return max_bundle_size


	#send all preference lists of the students to the maxcu algorithm
	def addAllPrefs(self):
		#pref weights start at maxOrder and go on with maxOrder - 1, maxOrder - 2
		maxOrder = self._max_pref_list_size
		for stud in self._students:
			order = maxOrder
			for bundle in self._preferences[stud]:
				self._solver.addPreferences(self._stud_to_id[stud],self._bundle_to_id[bundle],order)
				order = order - 1
