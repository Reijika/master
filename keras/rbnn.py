import matplotlib.pyplot as plt
import numpy as np
from keras.models import Sequential
from keras.layers import Dense, Activation
from keras.layers.custom_activations import Radial

def f(x):
    return np.sqrt(np.absolute(x))*np.sin(x*np.pi/2)

def generate_data(func, size, start, end, split):
    func = np.vectorize(func)
    f_x_train = np.random.uniform(start, end, size=int(round(size*split)))
    f_x_train = np.sort(f_x_train)
    f_y_train = func(f_x_train)
    f_x_test = np.random.uniform(start, end, size=int(round(size*(1.0-split))))
    f_x_test = np.sort(f_x_test)
    f_y_test = func(f_x_test)
    return f_x_train.T, f_x_test.T, f_y_train.T, f_y_test.T

def plot(x,y_predict,y_actual):
    plt.plot(x, y_predict, marker='None')
    plt.plot(x, y_actual, marker='None')
    plt.show()

def create_single_layer_model(sigma):
    model = Sequential()    
    custom_act = Radial(sigma)    
    model.add(Dense(7, input_dim=1)) #, weights=weight_init
    model.add(Activation(custom_act))
    model.add(Dense(1))
    model.add(Activation('linear'))
    model.compile(loss='mean_squared_error', optimizer='sgd')
    return model

def train_model(x, y, model, epoch_size, batch_size):                  
    model.fit(x, y, epochs=epoch_size, batch_size=batch_size)
    return model

def predict_model(x, model):        
    predictions = model.predict(x)
    return predictions

func = f
sample = 1000
sigma = 6.0
epochs = 100

# xtrain = np.array([-6,-5,-2,0,1,3,5])
# ytrain = np.array([0, -2.23607, 0, 0, 1, -1.73205,-2.23607])
# xtest = np.array([3.6])
# ytest = np.array([-1.11524])

x_point = np.array([3.6])
xtrain, xtest, ytrain, ytest = generate_data(func, sample, -6, 6, 0.7)
one_layer_model = create_single_layer_model(sigma)
one_layer_model = train_model(xtrain, ytrain, one_layer_model, epochs, 1)
predictions = predict_model(xtest, one_layer_model)
y_point = predict_model(x_point, one_layer_model)
print "x: {}, y: {}".format(x_point[0],y_point[0])
plot(xtest, predictions, ytest)


#/home/scmchan/.virtualenvs/keras/lib/python2.7/site-packages/keras/layers