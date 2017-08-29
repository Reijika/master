# This file will contain your data_preprocessing.py script.

import pandas as pd
import numpy as np
from sklearn.preprocessing import MinMaxScaler, OneHotEncoder, LabelEncoder

pd.options.mode.chained_assignment = None

def load_data(fileName):
	columns = ['age', 'workclass', 'fnlwgt', 'education', 'education-num', 'marital-status', 'occupation', 'relationship', 'race', 'sex', 'capital-gain', 'capital-loss', 'hours-per-week', 'native-country', 'income-class']
	data = pd.read_csv('data/'+fileName, sep=',')
	data.columns = columns
	data = normalize(data)
	return data

def normalize(dataframe):
	continuous = ['age', 'fnlwgt', 'education-num', 'capital-gain', 'capital-loss', 'hours-per-week']
	categories = ['workclass', 'education', 'marital-status', 'occupation', 'relationship', 'race', 'sex', 'native-country']
	classification = ['income-class']

	#normalize the continuous columns
	scaler = MinMaxScaler()	
	dataframe[continuous] = scaler.fit_transform(dataframe[continuous])

	#remove rows with missing data
	for category in categories:
		dataframe = dataframe[dataframe[category] != ' ?'] #drop any rows with missing data

	#label encode the classification column
	dataframe = ordinal_encode(dataframe, classification)	

	#extract the classification column temporarily
	labels = dataframe['income-class']
	dataframe = dataframe.drop('income-class', 1)

	#one hot encode the categorical columns
	dataframe = onehot_encode(dataframe, categories)	

	#rejoin the labels
	dataframe = dataframe.join(labels)	

	return dataframe

def ordinal_encode(dataframe, categories):	
	label_encoder = LabelEncoder()		
	for category in categories:
		dataframe[category] = label_encoder.fit_transform(dataframe[category])	#label encode
	return dataframe

def onehot_encode(dataframe, categories):	
	return pd.get_dummies(dataframe, columns=categories)
	# dataframe = pd.get_dummies(dataframe, columns=['workclass'])



def show_max(data):
	#checking number of categories after a label encoding
	print str(data['workclass'].max()) 
	print str(data['education'].max()) 
	print str(data['marital-status'].max())
	print str(data['occupation'].max()) 
	print str(data['relationship'].max())
	print str(data['race'].max()) 
	print str(data['sex'].max()) 
	print str(data['native-country'].max()) 
	print str(data['income-class'].max()) 

#label encoder starts at 0
data = load_data('train_data.txt')
print data.head(5)
# show_max(data)


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