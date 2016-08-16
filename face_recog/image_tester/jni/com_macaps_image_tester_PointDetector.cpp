
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/contrib/contrib.hpp>

#include <iostream>
#include <string>
#include <cstring>
#include <vector>
#include <android/log.h>

#include <com_macaps_image_tester_PointDetector.h>
#include <flandmark_detector.h>


#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
#define nullptr 0

using namespace std;
using namespace cv;


JNIEXPORT jfloatArray JNICALL Java_com_macaps_image_1tester_PointDetector_detectFLAND
  (JNIEnv *env, jclass, jlong input_addr, jintArray bounds, jstring model_path){

	jint *bbox = env->GetIntArrayElements(bounds, NULL);
	jsize length = env->GetArrayLength(bounds);

	const char * path = env->GetStringUTFChars(model_path, nullptr);
	FLANDMARK_Model * model = flandmark_init(path);

	if (model == 0){
		return nullptr;
	}

	Mat& input_mat = *(Mat*)input_addr;
	IplImage* img = new IplImage(input_mat);
	float *landmarks = (float*)malloc(2*model->data.options.M*sizeof(float));
	flandmark_detect(img, bbox, model, landmarks);

	jfloatArray fpoints = env->NewFloatArray(2*model->data.options.M);
	env->SetFloatArrayRegion(fpoints, 0, 2*model->data.options.M, landmarks);

	delete img;
	delete landmarks;
	return fpoints;

}



