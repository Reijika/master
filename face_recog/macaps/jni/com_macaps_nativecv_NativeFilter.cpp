#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/contrib/contrib.hpp>
#include <opencv2/features2d/features2d.hpp>

#include <iostream>
#include <string>
#include <cstring>
#include <vector>
#include <android/log.h>

#include <com_macaps_nativecv_NativeFilter.h>

#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
#define nullptr 0

using namespace std;
using namespace cv;


JNIEXPORT jboolean JNICALL Java_com_macaps_nativecv_NativeFilter_applyGamma
  (JNIEnv * env , jclass, jlong inaddr, jlong outaddr){

	if (inaddr == nullptr){
			return false;
		}

	Mat& input_mat = *(Mat*)inaddr;
	Mat& output_mat = *(Mat*)outaddr;

	double gamma = 1.70;
	double inverse_gamma = 1.0 / gamma;
	Mat lut_matrix(1, 256, CV_8UC1);
	uchar * ptr = lut_matrix.ptr();

	for( int i = 0; i < 256; i++ ){
		ptr[i] = (int)( pow( (double) i / 255.0, inverse_gamma ) * 255.0 );
	}

	LUT( input_mat, lut_matrix, output_mat );

	return true;
}

JNIEXPORT jboolean JNICALL Java_com_macaps_nativecv_NativeFilter_applyCLAHE
  (JNIEnv * env , jclass, jlong inaddr, jlong outaddr){

	if (inaddr == nullptr){
		return false;
	}

	Mat& input_mat = *(Mat*)inaddr;
	Mat& output_mat = *(Mat*)outaddr;
	cvtColor(input_mat, input_mat, CV_BGR2GRAY);
	cvtColor(output_mat, output_mat, CV_BGR2GRAY);

	Ptr<CLAHE> clahe = createCLAHE(2, Size(8,8));
	clahe->apply(input_mat, output_mat);

	//output_mat = ExtractBackground(output_mat);

	return true;

}
