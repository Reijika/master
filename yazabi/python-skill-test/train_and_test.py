# This file will contain your train_and_test.py script.

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import data_preprocessing as dp
from sklearn import tree
from sklearn.metrics import accuracy_score
from sklearn.pipeline import Pipeline
from sklearn.decomposition import PCA

continuous = ['age', 'fnlwgt', 'education-num', 'capital-gain', 'capital-loss', 'hours-per-week']
categories = ['workclass', 'education', 'marital-status', 'occupation', 'relationship', 'race', 'sex', 'native-country']
classification = ['income-class']

def preprocess_data(data):	
	data = dp.normalize_continuous(data, continuous)

	#remove rows with missing data
	for category in categories:
		data = data[data[category] != ' ?'] #drop any rows with missing data

	data = dp.ordinal_encode(data, classification)

	#extract the classification column temporarily
	labels = data['income-class']
	data = data.drop('income-class', 1)

	data = dp.onehot_encode(data, categories)	
	return data, labels

def train_and_validate(algorithm):
	if algorithm == "naive_bayes":
		print "amai amai"
	elif algorithm == "decision_tree":
		call_model("dtree", x_train, y_train, x_test, y_test)
		print "using decision tree model"
	elif algorithm == "knn":
		print "neighbors"
	elif algorithm == "svm":
		print "vectors"
	else:
		print "Algorithm not supported"

#this excuse of a function is so that I can make my calls while passing in my data and labels
def call_model(type, x_train, y_train, x_test, y_test):
	x_train, x_test = amend_test_data(x_train, x_test)

	if type == "dtree":		
		accuracy = use_decision_tree(x_train, y_train, x_test, y_test)
		print ("Decision tree model accuracy: %f ", accuracy)


# the test data does not have any vectors for native-country with the value Holand-Betherlands
# hot encoding the test data will result a missing column, this function will manually readd it
def amend_test_data(x_train, x_test):
	training_columns = list(x_train.columns)
	testing_columns = list(x_test.columns)

	#get the diff between the 2 lists of columns
	missing_columns = filter(lambda x: x not in testing_columns,training_columns)

	#add the missing column back into x_test
	for column in missing_columns:
		x_test[column] = 0
		x_test[column] = x_test[column].astype(np.uint8)		

	#sort by column the axis so that both dataframe line up (prior to conversion into a numpy array, order matters)
	#we are not sorting by row value, so we don't have to worry about the labels
	x_test = x_test.reindex_axis(sorted(x_test.columns), axis=1)
	x_train = x_train.reindex_axis(sorted(x_train.columns), axis=1)

	return x_train, x_test

def use_decision_tree(x_train, y_train, x_test, y_test):	
	#using pipeline makes it easy to try different transformations
	#pipe = Pipeline([('pca', PCA()),('tree', tree.DecisionTreeClassifier())])
	pipe = Pipeline([('tree', tree.DecisionTreeClassifier())])
	pipe.fit(x_train.values, y_train.values)
	y_predict = pipe.predict(x_test.values)	

	acc = accuracy_score(y_test.values, y_predict)
	return acc

training_data = dp.load_data('train_data.txt')
testing_data = dp.load_data('test_data.txt')
x_train, y_train = preprocess_data(training_data)
x_test, y_test = preprocess_data(testing_data)

train_and_validate("decision_tree")

#use scikit's pipeline to streamline things
#https://stats.stackexchange.com/questions/144439/applying-pca-to-test-data-for-classification-purposes







#numpy_data = apply_PCA(data, 2)
#display_graph(numpy_data)

