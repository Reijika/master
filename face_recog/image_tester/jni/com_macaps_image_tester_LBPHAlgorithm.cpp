#include <com_macaps_image_tester_LBPHAlgorithm.h>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/contrib/contrib.hpp>


#include <iostream>
#include <string>
#include <cstring>
#include <vector>

#include <android/log.h>


#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
#define nullptr 0

using namespace std;
using namespace cv;


JNIEXPORT jint JNICALL Java_com_macaps_image_1tester_LBPHAlgorithm_predict
  (JNIEnv * env, jclass, jlong training_addr, jlong input_addr){

	vector<Mat>images;
	vector<int>labels;

	Mat& training_mat = *(Mat*)training_addr;
	Mat& input_mat = *(Mat*)input_addr;

	cvtColor(training_mat, training_mat, CV_BGR2GRAY);
	cvtColor(input_mat, input_mat, CV_BGR2GRAY);

	images.push_back(training_mat);
	labels.push_back(0);

	Ptr<FaceRecognizer> model = createLBPHFaceRecognizer(1,8,8,8,45);
	model->train(images, labels);

	jint predictedLabel = -1;
	jdouble confidence = 0.0;
	model->predict(input_mat, predictedLabel, confidence);

	return predictedLabel;
}




