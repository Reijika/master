# This file will contain your data_preprocessing.py script.

import pandas as pd
import numpy as np
import matplotlib.pyplot as plt
from sklearn.preprocessing import MinMaxScaler, LabelEncoder
from sklearn.decomposition import PCA

def load_data(fileName):
	columns = ['age', 'workclass', 'fnlwgt', 'education', 'education-num', 'marital-status', 'occupation', 'relationship', 'race', 'sex', 'capital-gain', 'capital-loss', 'hours-per-week', 'native-country', 'income-class']
	data = pd.read_csv('data/'+fileName, sep=',')
	data.columns = columns	
	return data

def normalize_continuous(dataframe, continuous):	
	#normalize the continuous columns
	scaler = MinMaxScaler()	
	dataframe[continuous] = scaler.fit_transform(dataframe[continuous])
	return dataframe

def ordinal_encode(dataframe, categories):
	#remove rows with missing data
	for category in categories:
		dataframe = dataframe[dataframe[category] != ' ?'] #drop any rows with missing data

	label_encoder = LabelEncoder()		
	for category in categories:
		dataframe[category] = label_encoder.fit_transform(dataframe[category])	#label encode
	return dataframe

def onehot_encode(dataframe, categories):	
	#remove rows with missing data
	for category in categories:
		dataframe = dataframe[dataframe[category] != ' ?'] #drop any rows with missing data

	return pd.get_dummies(dataframe, columns=categories)

def apply_PCA(dataframe, dimensions):
	pca = PCA(n_components=dimensions)

	y_df = dataframe['income-class'].values
	y_df = np.reshape(y_df, (y_df.size,1))
	x_df = dataframe.drop('income-class', 1)
	x_df = pca.fit_transform(x_df)
	
	numpy_data = np.column_stack((x_df,y_df))

	return numpy_data

def display_graph(nd):
	c_zero = nd[nd[:,2] == 0]
	c_one = nd[nd[:,2] == 1]
	print c_zero
	print c_one

	with plt.style.context('seaborn-whitegrid'):
		fig = plt.figure(figsize=(6,4))
		ax1 = fig.add_subplot(111)
		ax1.scatter(c_zero[:,0], c_zero[:,1], c='b', marker='.', label='<=50K')
		ax1.scatter(c_one[:,0], c_one[:,1], c='r', marker='.', label='>50K')
		plt.xlabel('Principal Component 1')
    	plt.ylabel('Principal Component 2')
    	plt.legend(loc='lower left')
    	plt.tight_layout()
    	plt.show()

# https://stackoverflow.com/questions/21057621/sklearn-labelencoder-with-never-seen-before-values
# https://machinelearningmastery.com/how-to-one-hot-encode-sequence-data-in-python/
# dataset explanation: https://archive.ics.uci.edu/ml/machine-learning-databases/adult/adult.names

#data ranges
#continuous
# age: continuous.
#fnlwgt: continuous.
# education-num: continuous.
# capital-gain: continuous.
# capital-loss: continuous.
# hours-per-week: continuous.

#discrete categories
# 8  - workclass: Private, Self-emp-not-inc, Self-emp-inc, Federal-gov, Local-gov, State-gov, Without-pay, Never-worked.
# 16 - education: Bachelors, Some-college, 11th, HS-grad, Prof-school, Assoc-acdm, Assoc-voc, 9th, 7th-8th, 12th, Masters, 1st-4th, 10th, Doctorate, 5th-6th, Preschool.
# 7  - marital-status: Married-civ-spouse, Divorced, Never-married, Separated, Widowed, Married-spouse-absent, Married-AF-spouse.
# 14 - occupation: Tech-support, Craft-repair, Other-service, Sales, Exec-managerial, Prof-specialty, Handlers-cleaners, Machine-op-inspct, Adm-clerical, Farming-fishing, Transport-moving, Priv-house-serv, Protective-serv, Armed-Forces.
# 6  - relationship: Wife, Own-child, Husband, Not-in-family, Other-relative, Unmarried.
# 5  - race: White, Asian-Pac-Islander, Amer-Indian-Eskimo, Other, Black.
# 2  - sex: Female, Male.
# 41 - native-country: United-States, Cambodia, England, Puerto-Rico, Canada, Germany, Outlying-US(Guam-USVI-etc), India, Japan, Greece, 
					 # South, China, Cuba, Iran, Honduras, Philippines, Italy, Poland, Jamaica, Vietnam, 
					 # Mexico, Portugal, Ireland, France, Dominican-Republic, Laos, Ecuador, Taiwan, Haiti, Columbia,
					 # Hungary, Guatemala, Nicaragua, Scotland, Thailand, Yugoslavia, El-Salvador, Trinadad&Tobago, Peru, Hong,
					 # Holand-Netherlands.					 