import csv
import matplotlib.pyplot as plt
import numpy as np
from keras.models import Sequential
from keras.layers import Dense

def f1(x):
    return x * np.sin(6*np.pi*x) * np.exp(-x**2)

def f2(x):
    return np.exp(-x**2) * np.arctan(x) * np.sin(4*np.pi*x)

def generate_data(func, size, start, end, split):
    func = np.vectorize(func)
    f_x_train = np.random.uniform(start, end, size=int(round(size*split)))
    f_x_train = np.sort(f_x_train)
    f_y_train = func(f_x_train)
    f_x_test = np.random.uniform(start, end, size=int(round(size*(1.0-split))))
    f_x_test = np.sort(f_x_test)
    f_y_test = func(f_x_test)
    return f_x_train.T, f_x_test.T, f_y_train.T, f_y_test.T

def create_single_layer_model(neurons):
    weight_init = [100*np.random.randn(1, neurons), 100*np.random.randn(neurons)]
    model = Sequential()
    model.add(Dense(neurons, activation='sigmoid', input_dim=1, weights=weight_init))
    model.add(Dense(1, activation='linear'))
    model.compile(loss='mean_squared_error', optimizer='sgd')
    return model

def train_model(x, y, model, epoch_size, batch_size):              
    x = (x - x.min(0)) / x.ptp(0)    
    model.fit(x, y, epochs=epoch_size, batch_size=batch_size)
    return model

def predict_model(x, model):    
    x = (x - x.min(0)) / x.ptp(0)    
    predictions = model.predict(x)
    return predictions

error_list = []
func = f2
sample = 1000
neurons = 100
#sample_range = [10,30,50,90,130,170]
#neurons_range = [3,5,10,20,50,100]
epochs = 400

# rfile = open('Q2_f2_neuron.txt', 'w')
# out = csv.writer(rfile, delimiter=',', quoting=csv.QUOTE_ALL)
# for i in range(3):    
#     for n in neurons_range:
#         xtrain, xtest, ytrain, ytest = generate_data(func, sample, -2, 2, 0.7)
#         one_layer_model = create_single_layer_model(n)
#         one_layer_model = train_model(xtrain, ytrain, one_layer_model, epochs, 1)
#         predictions = predict_model(xtest, one_layer_model)
#         error = np.mean(np.absolute((predictions-ytest)))        
#         error_list.append(error)        
#     out.writerow(error_list)
#     error_list = []
# rfile.close()
    
def plot(x,y_predict,y_actual):
    plt.plot(x, y_predict, marker='.')
    plt.plot(x, y_actual, marker='o')
    plt.show()

xtrain, xtest, ytrain, ytest = generate_data(f1, sample, -2, 2, 0.7)
one_layer_model = create_single_layer_model(neurons)
one_layer_model = train_model(xtrain, ytrain, one_layer_model, epochs, 1)
predictions = predict_model(xtest, one_layer_model)
plot(xtest, predictions, ytest)