# This file will contain your train_and_test.py script.

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
import data_preprocessing as dp
from sklearn.tree import DecisionTreeClassifier
from sklearn.naive_bayes import GaussianNB
from sklearn.svm import SVC
from sklearn.neighbors import KNeighborsClassifier
from sklearn.metrics import accuracy_score
from sklearn.pipeline import Pipeline
from sklearn.decomposition import PCA
from sklearn.model_selection import cross_val_score

continuous = ['age', 'fnlwgt', 'education-num', 'capital-gain', 'capital-loss', 'hours-per-week']
categories = ['workclass', 'education', 'marital-status', 'occupation', 'relationship', 'race', 'sex', 'native-country']
classification = ['income-class']

def preprocess_data(data):	
	#data = dp.scale_continuous(data, continuous)
	data = dp.standardize_continuous(data, continuous)

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
		call_model("nbayes", x_train, y_train, x_test, y_test)
		print "using naive bayes model"
	elif algorithm == "decision_tree":
		call_model("dtree", x_train, y_train, x_test, y_test)
		print "using decision tree model"
	elif algorithm == "knn":
		call_model("knn", x_train, y_train, x_test, y_test)
		print "using k nearest neighbors"
	elif algorithm == "svm":
		call_model("svm", x_train, y_train, x_test, y_test)
		print "using support vector machines"
	else:
		print "Algorithm not supported"

#this excuse of a function is so that I can make my calls while passing in my data and labels
def call_model(type, x_train, y_train, x_test, y_test):
	x_train, x_test = amend_test_data(x_train, x_test)

	if type == "nbayes":
		accuracy = use_naive_bayes(x_train, y_train, x_test, y_test)
		print ("Naive bayes model accuracy: %f ", accuracy)
	elif type == "dtree":
		accuracy = use_decision_tree(x_train, y_train, x_test, y_test)
		print ("Decision tree model accuracy: %f ", accuracy)
	elif type == "knn":
		accuracy = use_k_nearest_neighbors(x_train, y_train, x_test, y_test)
		print ("K nearest neighbors accuracy: %f ", accuracy)
	elif type == "svm":
		accuracy = use_support_vector_machine(x_train, y_train, x_test, y_test)
		print ("Support vector machine accuracy: %f ", accuracy)		


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
	pipe = Pipeline([('tree', DecisionTreeClassifier())])
	pipe.fit(x_train.values, y_train.values)
	y_predict = pipe.predict(x_test.values)	
	acc = accuracy_score(y_test.values, y_predict)
	return acc

def use_naive_bayes(x_train, y_train, x_test, y_test):
	pipe = Pipeline([('bayes', GaussianNB())])
	pipe.fit(x_train.values, y_train.values)
	y_predict = pipe.predict(x_test.values)	
	acc = accuracy_score(y_test.values, y_predict)
	return acc

def use_k_nearest_neighbors(x_train, y_train, x_test, y_test):	
	pipe = Pipeline([('neighbors', KNeighborsClassifier(n_neighbors=50))])
	pipe.fit(x_train.values, y_train.values)
	y_predict = pipe.predict(x_test.values)	
	acc = accuracy_score(y_test.values, y_predict)
	return acc

def use_support_vector_machine(x_train, y_train, x_test, y_test):
	pipe = Pipeline([('support vectors', SVC())])
	pipe.fit(x_train.values, y_train.values)
	y_predict = pipe.predict(x_test.values)	
	acc = accuracy_score(y_test.values, y_predict)
	return acc

def search_knn_param_space():
	#https://kevinzakka.github.io/2016/07/13/k-nearest-neighbor/
	myList = list(range(1,50))
	neighbors = filter(lambda x: x % 2 != 0, myList)	
	cv_scores = []

	for k in neighbors:
		pipe = Pipeline([('neighbors', KNeighborsClassifier(n_neighbors=k))])
		pipe.fit(x_train.values, y_train.values)
		y_predict = pipe.predict(x_test.values)	

		acc = accuracy_score(y_test.values, y_predict)
		cv_scores.append(acc)
		print acc

	MSE = [1 - x for x in cv_scores]

	# determining best k
	optimal_k = neighbors[MSE.index(min(MSE))]
	print "The optimal number of neighbors is %d" % optimal_k

	# plot misclassification error vs k
	plt.plot(neighbors, MSE)
	plt.xlabel('Number of Neighbors K')
	plt.ylabel('Misclassification Error')
	plt.show()

# 1 - 0.795402637589
# 3 - 0.819789287556
# 5 - 0.826493774405
# 7 - 0.828630369115
# 9 - 0.831356369263
# 11 - 0.832977234215
# 13 - 0.834303396449
# 15 - 0.834524423488
# 17 - 0.834524423488
# 19 - 0.835334855964
# 21 - 0.8362926398
# 23 - 0.835408531644
# 25 - 0.835776910042
# 27 - 0.83614528844
# 29 - 0.835776910042
# 31 - 0.837692477713
# 33 - 0.837471450674
# 35 - 0.837029396596
# 37 - 0.837766153393
# 39 - 0.836587342518
# 41 - 0.83614528844
# 43 - 0.836513666839
# 45 - 0.837397774994
# 47 - 0.837103072276
# 50 - 0.83842923451

training_data = dp.load_data('train_data.txt')
testing_data = dp.load_data('test_data.txt')
x_train, y_train = preprocess_data(training_data)
x_test, y_test = preprocess_data(testing_data)

train_and_validate("decision_tree")
train_and_validate("naive_bayes")
train_and_validate("svm")
train_and_validate("knn")


#use scikit's pipeline to streamline things
#https://stats.stackexchange.com/questions/144439/applying-pca-to-test-data-for-classification-purposes