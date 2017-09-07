# This file will contain your train_and_test.py script.

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import data_preprocessing as dp

continuous = ['age', 'fnlwgt', 'education-num', 'capital-gain', 'capital-loss', 'hours-per-week']
categories = ['workclass', 'education', 'marital-status', 'occupation', 'relationship', 'race', 'sex', 'native-country']
classification = ['income-class']

def preprocess_data(fileName):
	data = dp.load_data(fileName)
	data = dp.normalize_continuous(data, continuous)
	data = dp.ordinal_encode(data, classification)

	#extract the classification column temporarily
	labels = data['income-class']
	data = data.drop('income-class', 1)

	data = dp.onehot_encode(data, categories)
	data = data.join(labels)	
	return data

def train_and_validate(algorithm):
	if algorithm == "naive_bayes":
		print "amai amai"
	elif algorithm == "decision_tree":
		use_decision_tree(data)
		print "trees"
	elif algorithm == "knn":
		print "neighbors"
	elif algorithm == "svm":
		print "vectors"
	else:
		print "Algorithm not supported"

def use_decision_tree(data):
	return 0

#numpy_data = apply_PCA(data, 2)
#display_graph(numpy_data)
data = preprocess_data('train_data.txt')
print data.head(5)