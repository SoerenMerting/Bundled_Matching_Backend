from collections import OrderedDict
from py4j.java_gateway import JavaGateway, GatewayParameters
from py4j.java_collections import SetConverter, MapConverter, ListConverter
import logging
import time
import random, itertools, math
from thread import start_new_thread

def start_java(value):
    """
    Starts the Java Programme FacadeDMatrix
    """
    import os
    os.chdir('/var/www/matchingsystem/bundled_matching/maxcu_bps/src/')
    # os.system('bash -c \'echo "export GUROBI_HOME=/opt/gurobi652/linux64" >> ~/.bashrc\'')
    # os.system('bash -c \'echo "export GRB_LICENSE_FILE=/opt/gurobi652/gurobi.lic" >> ~/.bashrc\'')
    # os.system('bash -c \'echo "export PATH=$PATH:$GUROBI_HOME/bin" >> ~/.bashrc\'')
    # os.system('bash -c \'echo "export LD_LIBRARY_PATH=/opt/gurobi652/linux64/lib/" >> ~/.bashrc\'')
    # os.system('bash -c \'echo "export JAVA_HOME=/usr/lib/jvm/java-8-oracle" >> ~/.bashrc\'')
    # os.system('bash -c \'echo "export PATH=$JAVA_HOME/bin:$PATH" >> ~/.bashrc\'')
    # os.system('bash -c \'source ~/.bashrc\'')

    os.system('java -Xms3200m -classpath maxcu/py4j0.10.2.1.jar:maxcu/gurobi.jar: maxcu/FacadeDMatrix > output.txt')

class DMatrixGenerator(object):
    """ Converts the matching problem into matrix form for
    the computation in Java, establishes connection to Java-Side and
    interprets the result to the initial matching problem """

    def __init__(self,bps_dict,instance,epsilon = 2.0, delta = 3.0):
        import time
        start_new_thread(start_java,(1,))
        time.sleep(5)
        self.__bps_dict = bps_dict
        self.epsilon = epsilon
        self.delta = delta
        self.__bps_list = list()
        for stud in bps_dict:
            self.__bps_list.extend(bps_dict[stud].values())
        self.__student_to_bundles_list = OrderedDict()
        self.__stud_ranges_dict = OrderedDict()
        self.__cols = len(self.__bps_list)
        range_counter = 0
        self.__gw = JavaGateway(gateway_parameters=GatewayParameters(port=25335))
        self.__solver = self.__gw.entry_point
        self.__timeslots = instance.all_timeslots
        self.__timeslot_index = dict()
        self.__vectors = list()
        self.__lambdas = list()
        self.__dmatrix = list()
        self.__dconstraint = list()
        self.__dconstraint_type = list()
        self.__cost_coeff = list()
        self.__max_bundle_size = 0
        self.__studbundles_list = list()
        counter = 0
        self.__max_length = 0
        self.__timeslot_index_reverse = dict()
        for ts in self.__timeslots:
            self.__timeslot_index[ts] = counter
            self.__timeslot_index_reverse[counter] = ts
            counter +=1
        for stud in bps_dict:
            self.__max_length = max(self.__max_length,len(bps_dict[stud].keys()))
            if bps_dict[stud]:
                self.__max_bundle_size = max(self.__max_bundle_size, len(next(iter(bps_dict[stud]))))
            self.__student_to_bundles_list[stud] = bps_dict[stud].keys()
            self.__studbundles_list.extend(bps_dict[stud].keys())
            self.__stud_ranges_dict[stud] = [range_counter]
            range_counter += len(bps_dict[stud].keys())
            self.__stud_ranges_dict[stud].append(range_counter-1)


    def create_index_matrix(self):
        dmatrix_index = list()
        dconstraint = list()
        dconstraint_type = list()
        for stud in self.__student_to_bundles_list:
            ranges = self.__stud_ranges_dict[stud]
            row_index = list()
            for col in range(ranges[0],ranges[1]+1):
                row_index.append(col)
            dmatrix_index.append(row_index)
            dconstraint.append(1)
            dconstraint_type.append("demand")
        supply_matrix = list()
        for ts in self.__timeslots:
            supply_matrix.append([])
            dconstraint.append(ts.capacity)
            dconstraint_type.append("supply")
        counter = 0
        for bundle in self.__studbundles_list:
            for ts in bundle:
                supply_matrix[self.__timeslot_index[ts]].append(counter)
            counter+=1
        supply_counter = 0
        for supply_row in supply_matrix:
            supply = 0.0
            for pos in supply_row:
                supply += self.__bps_list[pos]
            supply_counter += 1
        for row in supply_matrix:
            dmatrix_index.append(row)
        self.__dmatrix = dmatrix_index
        self.__dconstraint = dconstraint
        self.__dconstraint_type = dconstraint_type
        for stud in self.__student_to_bundles_list:
            coeff = self.__max_length
            for item in self.__student_to_bundles_list[stud]:
                self.__cost_coeff.append(coeff)
                coeff -=1

    def create_matrix(self):
        dmatrix = list()
        dmatrix_index = list()
        dconstraint = list()
        dconstraint_type = list()
        for stud in self.__student_to_bundles_list:
            row = [0] * self.__cols
            ranges = self.__stud_ranges_dict[stud]
            row_index = list()
            for col in range(ranges[0],ranges[1]+1):
                row[col] = 1
                row_index.append(col)
            dmatrix.append(row)
            dconstraint.append(1)
            dconstraint_type.append("demand")
        supply_matrix = list()
        for ts in self.__timeslots:
            row = [0] * self.__cols
            supply_matrix.append(row)
            dconstraint.append(ts.capacity)
            dconstraint_type.append("supply")
        counter = 0
        for bundle in self.__studbundles_list:
            for ts in bundle:
                supply_matrix[self.__timeslot_index[ts]][counter] = 1
            counter+=1
        for row in supply_matrix:
            dmatrix.append(row)
        self.__dmatrix = dmatrix
        self.__dconstraint = dconstraint
        self.__dconstraint_type = dconstraint_type
        for stud in self.__student_to_bundles_list:
            coeff = self.__max_length
            for item in self.__student_to_bundles_list[stud]:
                self.__cost_coeff.append(coeff)
                coeff -=1

    def get_dmatrix(self):
        return self.__dmatrix

    def get_dconstraint(self):
        return self.__dconstraint

    def get_dconstraint_type(self):
        return self.__dconstraint_type

    def get_cost_coeff(self):
        return self.__cost_coeff

    def random_vector(self):
        seed = random.random()
        random.seed(seed)
        x = random.random()
        for i in range(len(self.__vectors)):
            if(x<=self.__lambdas[i]):
                return self.__vectors[i]
            else:
                x -= self.__lambdas[i]
        return []

    def get_assignment(self):
        vector = self.random_vector()
        counter = 0
        assignment = []
        for stud in self.__bps_dict:
            for bundle in self.__bps_dict[stud]:
                if vector[counter] == 1:
                    assignment.append((stud,bundle))
                counter += 1
        return assignment

    def init_java_gateway(self):
        start = time.time()
        # import os
        # os.chdir('/var/www/matchingsystem/bundled_matching/maxcu_bps/src/')
        # os.system('java -Xms3200m -classpath maxcu/py4j0.10.2.1.jar:maxcu/gurobi.jar: maxcu/FacadeDMatrix')
        # subprocess.Popen('java -Xms3200m -classpath maxcu/py4j0.10.2.1.jar:maxcu/gurobi.jar: maxcu/FacadeDMatrix',cwd='/var/www/matchingsystem/bundled_matching/maxcu_bps/src/')
        self.__solver.initFacade(len(self.__dconstraint),len(self.__cost_coeff))
        end = time.time()
        counter = 0
        start = time.time()
        for row in self.__dmatrix:
            row_java = ListConverter().convert(row,self.__gw._gateway_client)
            self.__solver.fillDMatrix(counter,row_java)
            counter += 1
        end = time.time()
        dc_java = ListConverter().convert(self.__dconstraint,self.__gw._gateway_client)
        self.__solver.filldConstraint(dc_java)
        dcT_java = ListConverter().convert(self.__dconstraint_type,self.__gw._gateway_client)
        self.__solver.filldConstraintType(dcT_java)
        cost_coeff_java = ListConverter().convert(self.__cost_coeff,self.__gw._gateway_client)
        self.__solver.fillCostCoeff(cost_coeff_java)
        self.__solver.createModelMatrix(self.__max_bundle_size)
        bps_java = ListConverter().convert(self.__bps_list, self.__gw._gateway_client)
        self.__solver.createMaxcu(bps_java,self.epsilon,self.delta)
        self.__solver.calculate()
        self.__lambdas = self.__solver.getLambdas()
        self.__vectors = self.__solver.getVectors()

    def get_lambdas(self):
        return list(self.__lambdas)

    def close(self):
        self.__gw.shutdown()

    def get_vectors(self):
        return [list(elem) for elem in list(self.__vectors)]

    def scalar_multi(self,scalar,vector):
        return [scalar*element for element in vector]

    def vector_addition(self,vector1,vector2):
        solution_vector = list()
        for i in range(len(vector1)):
            solution_vector.append(vector1[i] + vector2[i])
        return solution_vector

    def convex_combination(self):
        sum_vector = self.scalar_multi(self.get_lambdas()[0],self.get_vectors()[0])
        for i in range(1,len(self.get_lambdas())):
            multi_vector = self.scalar_multi(self.get_lambdas()[i],self.get_vectors()[i])
            sum_vector = self.vector_addition(sum_vector,multi_vector)
        return sum_vector

    def vector_difference(self,vector1,vector2):
        solution_vector = list()
        for i in range(len(vector1)):
            solution_vector.append(vector1[i] - vector2[i])
        return solution_vector

    def get_difference(self):
        convex_combi = self.convex_combination()
        solution = self.vector_difference(convex_combi,self.__bps_list)
        return solution

    def get_bps_list(self):
        return self.__bps_list
