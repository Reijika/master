import csv
import matplotlib.pyplot as plt
from mpl_toolkits.mplot3d import Axes3D
import numpy as np
from keras.models import Sequential
from keras.layers import Dense

def f(x1,x2):
    return np.sin(2*np.pi*x1) * np.cos(0.5*np.pi*x2) * np.exp(-x1**2)

def generate_data(func, size, split):
    func = np.vectorize(func)
    f_x1_train = np.random.uniform(-1, 1, size=int(round(size*split)))
    f_x1_train = np.sort(f_x1_train)
    f_x2_train = np.random.uniform(-4, 4, size=int(round(size*split)))
    f_x2_train = np.sort(f_x2_train)
    f_y_train = func(f_x1_train, f_x2_train)

    f_x1_test = np.random.uniform(-1, 1, size=int(round(size*(1.0-split))))
    f_x1_test = np.sort(f_x1_test)
    f_x2_test = np.random.uniform(-4, 4, size=int(round(size*(1.0-split))))
    f_x2_test = np.sort(f_x2_test)
    f_y_test = func(f_x1_test, f_x2_test)
    return f_x1_train.T, f_x2_train.T, f_x1_test.T, f_x2_test.T, f_y_train.T, f_y_test.T

def create_mlp_model(neurons):
    weight_init = [100*np.random.randn(2, neurons), 100*np.random.randn(neurons)]
    model = Sequential()
    model.add(Dense(neurons, activation='sigmoid', input_dim=2, weights=weight_init))
    model.add(Dense(1, activation='linear'))
    model.compile(loss='mean_squared_error', optimizer='sgd')
    return model

def train_model(x1, x2, y, model, epoch_size, batch_size):              
    x1 = (x1 - x1.min(0)) / x1.ptp(0)
    x2 = (x2 - x2.min(0)) / x2.ptp(0)    
    x = np.column_stack((x1,x2))
    model.fit(x, y, epochs=epoch_size, batch_size=batch_size)
    return model

def predict_model(x1,x2, model):    
    x1 = (x1 - x1.min(0)) / x1.ptp(0)
    x2 = (x2 - x2.min(0)) / x2.ptp(0)
    x = np.column_stack((x1,x2))
    predictions = model.predict(x)
    return predictions

error_list = []
func = f
sample = 1000
neurons = 50
#neurons_range = [2,4,6,8,12,20]
epochs = 400

# rfile = open('Q3_f_neurons.csv', 'w')
# out = csv.writer(rfile, delimiter=',', quoting=csv.QUOTE_ALL)
# for i in range(3):
#     for n in neurons_range:
#         x1train, x2train, x1test, x2test, ytrain, ytest = generate_data(func, sample, 0.7)
#         mlp_model = create_mlp_model(n)
#         mlp_model = train_model(x1train, x2train, ytrain, mlp_model, epochs, 1)
#         predictions = predict_model(x1test, x2test, mlp_model)
#         error = np.mean(np.absolute((predictions-ytest)))            
#         error_list.append(error)    
#     out.writerow(error_list)
#     error_list = []
# rfile.close()

def plot(x1,x2,y_predict,y_actual):
    fig = plt.figure()
    ax = fig.add_subplot(111, projection='3d')
    ax.scatter(x1,x2,y_predict)    
    ax.scatter(x1,x2,y_actual)    
    ax.set_xlabel('X1')
    ax.set_ylabel('X2')
    ax.set_zlabel('Y')    
    plt.show()

x1train, x2train, x1test, x2test, ytrain, ytest = generate_data(func, sample, 0.7)
mlp_model = create_mlp_model(neurons)
mlp_model = train_model(x1train, x2train, ytrain, mlp_model, epochs, 1)
predictions = predict_model(x1test, x2test, mlp_model)
plot(x1test, x2test, predictions, ytest)