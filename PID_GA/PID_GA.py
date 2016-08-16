import random 
import csv
from copy import copy, deepcopy
from matplotlib.pyplot import * # Grab MATLAB plotting functions
from control.matlab import *    # Load the controls systems library
from scipy import arange        # function to create range of numbers



def step_info(yout, t):

	#integral squared error
	ISE = 0;
	for i in range (0, len(yout)):
		ISE = ISE + (yout[i] - 1) ** 2

	#rising time is calculated from the 10% to 90% range
	rising_index1 = -1;
	rising_index2 = -1;
	start = True;
	for i in range (0,len(yout)):
		if yout[i]>yout[-1]*.10 and start:
			rising_index1 = i
			start = False			
		if yout[i]>yout[-1]*.90:	
			rising_index2 = i;
			break
	tr = t[rising_index2] - t[rising_index1]

	#settling time is calculated based on a 2% boundary of the final value
	ss_index = -1;
	for i,e in reversed(list(enumerate(yout))):
		if (yout[i] > yout[-1]*1.02) or (yout[i] < yout[-1]*0.98):
			ss_index = i
			break
	ts = t[ss_index] - t[0]

	#overshoot
	mp = (yout.max()/yout[-1]-1)*100

	return (ISE, tr, ts, mp)


def evaluate(Kp, Ti, Td):
	G = Kp*tf([Ti*Td,Ti,1],[Ti,0])
	F = tf(1,[1,6,11,6,0])

	sys = feedback(series(G,F),1)
	yout, T = step(sys, T = arange(0, 100, 0.01));

	(ISE, tr, ts, mp) = step_info(yout, T)

	#edge cases where performance params fail
	#so we skew the values to make it evaluate as a terrible solution
	if tr <= 0.0:
		tr = 9999
	if ts >= 99.99:
		ts = 9999	
	score = 1/(tr+ts+ISE+mp)	# we want to maximize this
	score = score*100

	#print "ISE: {}, t_r: {}, t_s: {}, M_p: {}".format(ISE, tr, ts, mp)
	#print "Score: {}".format(score)

	return score

def generate_population(size):

	population = []
	for i in range(0, size):
		kp = random.uniform(2, 18)
		ti = random.uniform(1.05, 9.42)
		td = random.uniform(0.26, 2.37)
		score = evaluate(kp, ti, td)
		population.append([i,kp,ti,td,score])
		#print "Solution: #{}, kp: {}, ti: {}, td: {}, score: {}".format(i, kp, ti, td, score)

	return population

def calculate_first_second(population):
	largest = max(population, key=lambda x:x[4])
	largest_value = largest[4]
	second_largest_value = 0
	second_largest_index = 0
	for i in range(0, len(population)):
		if population[i][4] > second_largest_value and population[i][4] != largest[4]:
			second_largest_value = population[i][4]
			second_largest_index = population[i][0]
	second_largest = population[second_largest_index]

	return largest[0], second_largest[0]


# a 50% chance that each param will be crossed
def crossover(p1, p2):
	#print "crossover operator"
	parent1 = deepcopy(p1)
	parent2 = deepcopy(p2)

	for i in range(0, 3):
		coin_flip = random.uniform(0,1)
		if coin_flip < 0.5:	
			temp = parent1[i+1]
			parent1[i+1] = parent2[i+1]
			parent2[i+1] = temp

	#update scores
	parent1[4] = evaluate(parent1[1],parent1[2],parent1[3])
	parent2[4] = evaluate(parent2[1],parent2[2],parent2[3])	

	#print "Parent 1: {}\nParent 2: {}".format(parent1, parent2)
	return (parent1, parent2)

#a 25% chance that each param will mutate
def mutate(p, prob):
	#print "mutation operator"
	parent = deepcopy(p)

	for i in range(0, 3):
		coin_flip = random.uniform(0,1)
		if coin_flip < prob:
			if i+1 == 1:	#kp
				roll = random.uniform(2, 18)				
			elif i+1 == 2:	#ti
				roll = random.uniform(1.05, 9.42)
			elif i+1 == 3:	#td
				roll = random.uniform(0.26, 2.37)
			parent[i+1] = (parent[i+1] + roll)/2.00

	parent[4] = evaluate(parent[1],parent[2],parent[3])
	#print "Parent : {}".format(parent)
	return parent

def generate_fitness_scale(pop):
	#Fitness proportionate selection
	#the higher your score, the more likely you'll be chosen
	total_score = 0.00;
	for i in range(0, len(pop)):
		total_score = total_score + pop[i][4]

	#normalize all the scores
	normalized = []
	for i in range(0, len(pop)):
		normalized_score = (pop[i][4]/total_score)	
		entry = (pop[i][0], normalized_score)
		normalized.append(entry)
	normalized = sorted(normalized, key=lambda x: x[1])	#sorts the solutions by their normalized scores
	
	#generate the scale from 0 to 1
	scale = []
	sum_scale = 0
	for i in range(0, len(normalized)):
		sum_scale = sum_scale + normalized[i][1]
		scale.append(sum_scale)

	return (scale, normalized)



def genetic_simulation(pop_size, generations, cross_prob, mutation_prob, filename):
	print "Simulation Start"
	print "Population Size : {}".format(pop_size)
	print "Generations     : {}".format(generations)
	print "Crossover Prob  : {}".format(cross_prob)
	print "Mutation  Prob  : {}".format(mutation_prob)
	print ""

	pop = generate_population(pop_size)
	best_per_generation = []	#records the best parent per generation

	# print "Initial Population: "
	# for entry in pop:
	# 	print entry
	# print ""

	#iterate the specified amount of generations
	for i in range(0, generations):
		print "Generation #{}".format(i)
		first, second = calculate_first_second(pop)

		#in accordance elitism survival strategy, the best two solution are alway kept
		new_pop = []
		new_pop.append( pop[first] )
		new_pop.append( pop[second] )
		best_per_generation.append( pop[first] )

		#generate the normalized scores and the scale for FPS
		scale, normalized = generate_fitness_scale(pop)
		
		while (len(new_pop) != pop_size):
			
			#generate 2 random numbers (for crossover) and figure out where on scale is falls on
			cross_parents = []
			while(len(cross_parents) != 2):	
				select = random.uniform(0,1)
				index = 0;
				for i in range(0, len(scale)):
					if select < scale[i]:
						index = i
						break			
				selected = normalized[index][0]

				#makes sure the parents can't be the same solution
				if selected not in cross_parents:
					cross_parents.append(selected)

			#60% chance of cross
			cross_or_not = random.uniform(0,1)
			if cross_or_not < cross_prob:
				parent1, parent2 = crossover(pop[cross_parents[0]],pop[cross_parents[1]])		
			else:
				parent1 = deepcopy(pop[cross_parents[0]])
				parent2 = deepcopy(pop[cross_parents[1]])

			#attempt to mutate each parent individually
			parent1 = mutate(parent1, mutation_prob)
			parent2 = mutate(parent2, mutation_prob)

			if (pop_size - len(new_pop)) == 1:
				new_pop.append(parent1)
			else:
				new_pop.append(parent1)
				new_pop.append(parent2)

		#reset the solution labels
		for i in range(0, len(new_pop)):
			new_pop[i][0] = i

		pop = deepcopy(new_pop)

	# print ""
	# print "Final Population: "
	# for entry in pop:
	# 	print entry

	#writing best per generation into file
	f = open(filename, 'w')
	for i in range(0, len(best_per_generation)):
		record = "{}, {}\n".format(i, best_per_generation[i][4])
		f.write(record)
	
	best_solution = max(best_per_generation, key=lambda x:x[4])
	print "Best Solution: {}".format(best_solution)

	f.write("Best Solution: {}".format(best_solution))
	f.close()


	return 0

#sample run of the GA Simulation
genetic_simulation(5, 10, 0.60, 0.25, "test.csv")

#Uncomment this block to run all the simulations described in the report
# #messing with generation length
# genetic_simulation(50, 150, 0.60, 0.25, "data/pop_50_gen_150.csv")
# print "Simulation 0 complete"

# genetic_simulation(50, 100, 0.60, 0.25, "data/pop_50_gen_100.csv")
# print "Simulation 1 complete"

# genetic_simulation(50, 50, 0.60, 0.25, "data/pop_50_gen_50.csv")
# print "Simulation 2 complete"

# #messing with population size
# genetic_simulation(30, 150, 0.60, 0.25, "data/pop_30_gen_150.csv")
# print "Simulation 3 complete"

# genetic_simulation(10, 150, 0.60, 0.25, "data/pop_10_gen_150.csv")
# print "Simulation 4 complete"

# #messing with crossover prob
# genetic_simulation(25, 75, 0.30, 0.25, "data/cross_30_mut_25.csv")
# print "Simulation 5 complete"

# genetic_simulation(25, 75, 0.60, 0.25, "data/cross_60_mut_25.csv")
# print "Simulation 6 complete"

# genetic_simulation(25, 75, 0.90, 0.25, "data/cross_90_mut_25.csv")
# print "Simulation 7 complete"

# #messing with mutation prob
# genetic_simulation(25, 75, 0.60, 0.05, "data/cross_60_mut_05.csv")
# print "Simulation 8 complete"

# genetic_simulation(25, 75, 0.60, 0.15, "data/cross_60_mut_15.csv")
# print "Simulation 9 complete"

