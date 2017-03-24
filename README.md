# Projects

CVRP_SA - An attempt to solve the Capacitated Vehicle Routing Problem using simulated annealing mixed with crossover/mutation operators

PID_GA - Optimizing the parameters for a hypothetical PID controller using a genetic algorithm

face_recog - face recognition software for Android using OpenCV (requires JNI, accuracy sitting at approximately 86%)

flexo - posture detection software for Flexo (UW FYDP)
	  -> main.ino - the main sketch file
	  -> data_io.ino - experiments with basic file IO using a SD card for the Teensy
	  -> libraries/PostureDetector - incorrect posture indicator checker code
	  -> libraries/Weight - weight estimation code

keras - sample experiments at building neural networks using the Python neural network library, Keras
	  -> mlp_2D_regression.py - single hidden layer MLP that can predict 2D functions (regression)
	  -> mlp_3D_regression.py - single hidden layer MLP that can predict 3D functions (regression)
	  -> mlp_classification.py - single hidden layer MLP that takes in a 13-element input vector and outputs 1 of 3 possible classifications (classification)
	  -> rbnn.py - an attempt to reproduce a radial basis neural network to solve a regression problem
	  -> custom_activations.py - custom gaussian basis activation function for Keras (required for rbnn)
	  

