import csv
import numpy as np
from keras.utils import np_utils
from keras.models import Sequential
from keras.layers import Dense

def load_csv(name):
    csv = np.genfromtxt(name, delimiter=",")    
    np.random.shuffle(csv)          #quick shuffle of the data along the first axis before splitting it into x and y arrays
    y = csv[:,0]                    #extracts the first column
    y = y - 1                       #unique values are 1,2,3, remaps to 0,1,2
    x = np.delete(csv, [0],axis=1)  #deletes the first column from the original, leaving the rest
    return x,y

def split_data(x,y, split):    
    x_split_index = int(round(x.shape[0]*split)) #x.shape[0] = number of rows
    x = np.vsplit(x, np.array([x_split_index]))
    xtrain = x[0]
    xtest = x[1]
    y_split_index = int(round(y.shape[0]*split)) #y.shape[0] = number of rows    
    y = np.split(y, [y_split_index])
    ytrain = y[0]
    ytest = y[1]    
    ytrain = np.reshape(ytrain, (ytrain.shape[0], 1)).astype(int)
    ytest = np.reshape(ytest, (ytest.shape[0], 1)).astype(int)
    return xtrain, xtest, ytrain, ytest

def construct_data(split):
    x1, y1 = load_csv('Q4_training_C1')
    xtrain1, xtest1, ytrain1, ytest1 = split_data(x1,y1,split)
    x2, y2 = load_csv('Q4_training_C2')
    xtrain2, xtest2, ytrain2, ytest2 = split_data(x2,y2,split)
    x3, y3 = load_csv('Q4_training_C3')
    xtrain3, xtest3, ytrain3, ytest3 = split_data(x3,y3,split)
    xtrain = np.concatenate( (xtrain1, xtrain2, xtrain3), axis=0)
    xtest  = np.concatenate( (xtest1 , xtest2 , xtest3 ), axis=0)
    ytrain = np.concatenate( (ytrain1, ytrain2, ytrain3), axis=0)
    ytest  = np.concatenate( (ytest1 , ytest2 , ytest3 ), axis=0)
    return xtrain, xtest, ytrain, ytest

def create_multi_layer_model(neurons):
    model = Sequential()
    model.add(Dense(13, activation='sigmoid', init="normal", input_dim=13))    
    model.add(Dense(3, init='normal', activation='softmax'))
    model.compile(loss='categorical_crossentropy', optimizer='rmsprop')
    return model

def train_model(x, y, model, epoch_size, batch_size):    
    x = (x - x.min(0)) / x.ptp(0) 
    y_labels = np_utils.to_categorical(y, num_classes=3)
    model.fit(x, y_labels, epochs=epoch_size, batch_size=batch_size)
    return model

def predict_model(x,model):
    x = (x - x.min(0)) / x.ptp(0)
    predictions = model.predict(x)
    return predictions

def convert_to_class(y):
    output = []
    y = y.tolist()
    for i in y:
        highest = max(i)        
        for j in i:            
            if j == highest:
                index = i.index(j)
                output.append(index)                
                break;    
    return output

def calculate_accuracy(y_predict, y):    
    y_actual = []
    for i in y.tolist():
        for j in i:
            y_actual.append(j)
    correct = 0
    length = len(y_predict)
    for i in range(length):
        if y_predict[i] == y_actual[i]:
            correct += 1    
    accuracy = float(correct)/float(length)
    print 'Correct: {}, Total: {}, Accuracy: {}'.format(correct, length, accuracy)
    return accuracy

# neurons_range = [13,10,7,4]
# accuracy_list = []
# rfile = open('Q4_neurons.csv', 'w')
# out = csv.writer(rfile, delimiter=',', quoting=csv.QUOTE_ALL)

# for i in range(3):
#     for n in neurons_range:
#         xtrain, xtest, ytrain, ytest = construct_data(0.75)
#         mlp_model = create_multi_layer_model(n)
#         mlp_model = train_model(xtrain,ytrain,mlp_model, 500,1)
#         predictions = predict_model(xtest, mlp_model)
#         classified = convert_to_class(predictions)
#         test_accuracy = calculate_accuracy(classified, ytest)
#         print 'Test accuracy: {}'.format(test_accuracy)
#         accuracy_list.append(test_accuracy)
#     out.writerow(accuracy_list)
#     accuracy_list = []
# rfile.close()

# predictions = predict_model(xtrain, mlp_model)
# classified = convert_to_class(predictions)
# training_accuracy = calculate_accuracy(classified, ytrain)
#print 'Training accuracy: {}, Test accuracy: {}'.format(training_accuracy,test_accuracy)

xtrain, xtest, ytrain, ytest = construct_data(0.75)
mlp_model = create_multi_layer_model(13)
mlp_model = train_model(xtrain,ytrain,mlp_model, 500,1)
predictions = predict_model(xtest, mlp_model)
classified = convert_to_class(predictions)
test_accuracy = calculate_accuracy(classified, ytest)
print 'Test accuracy: {}'.format(test_accuracy)

sample_test = np.array([[13.72, 1.43, 2.5, 16.7, 108, 3.4, 3.67, 0.19, 2.04, 6.8, 0.89, 2.87, 1285],
    [12.04, 4.3, 2.38, 22, 80, 2.1, 1.75, 0.42, 1.35, 2.6, 0.79, 2.57, 580],
    [14.13, 4.1, 2.74, 24.5, 96, 2.05, 0.76, 0.56, 1.35, 9.2, 0.61, 1.6, 560]])

predictions = predict_model(sample_test, mlp_model)
classified = convert_to_class(predictions)
print classified
