#include <com_macaps_image_tester_CNNAlgorithm.h>
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

JNIEXPORT jint JNICALL Java_com_macaps_image_1tester_CNNAlgorithm_predictCNN
  (JNIEnv *env, jclass, jlong input_addr, jlong training_addr, jfloatArray opencv_poi, jfloatArray flandmark_poi){


		//Convert the jfloatArray object into a opencv fp Point array
		jfloat *open_set = env->GetFloatArrayElements(opencv_poi, NULL);
		jsize open_length = env->GetArrayLength(opencv_poi);
		Point opencv [open_length/2];
		jint count1 = 0;
		for (int i = 0; i < open_length; i=i+2){
			opencv[count1].x = open_set[i];
			opencv[count1].y = open_set[i+1];
			count1++;
		}
		//Convert the jfloatArray object into a flandmark fp Point array
		jfloat *fland_set = env->GetFloatArrayElements(flandmark_poi, NULL);
		jsize fland_length = env->GetArrayLength(flandmark_poi);
		Point flandmark [fland_length/2];
		jint count2 = 0;
		for (int i = 0; i < fland_length; i=i+2){
			flandmark[count2].x = fland_set[i];
			flandmark[count2].y = fland_set[i + 1];
			count2++;
		}

		//Prep the comparison image
		Mat& train_mat = *(Mat*)training_addr;
		cvtColor(train_mat, train_mat, CV_BGR2GRAY);

		//Prep the photo capture image
		Mat& input_mat = *(Mat*)input_addr;
		cvtColor(input_mat, input_mat, CV_BGR2GRAY);


		//ISSUE: CHECK THE BOUNDS - the ROI's are falling outside the Mat boundaries

		//Define the regions of interest for template matching
		Rect regionofinterest [7];
	    regionofinterest[0] = Rect (Point(flandmark[5].x,flandmark[5].y - 30), Point(flandmark[6].x,flandmark[6].y + 30)); // eye line
	    regionofinterest[0] &= Rect(Point(0, 0), input_mat.size());
	    regionofinterest[1] = Rect (Point(flandmark[5].x,opencv[0].y - 50), Point(flandmark[1].x,flandmark[1].y+20)); //left eyebrow
	    regionofinterest[1] &= Rect(Point(0, 0), input_mat.size());
	    regionofinterest[2] = Rect (Point(flandmark[6].x,opencv[1].y - 50), Point(flandmark[2].x,flandmark[2].y+20)); //right eyebrow
	    regionofinterest[2] &= Rect(Point(0, 0), input_mat.size());
	    regionofinterest[3] = Rect (Point(flandmark[5].x,flandmark[5].y - 10), Point(flandmark[1].x,flandmark[3].y)); //left cheek
	    regionofinterest[3] &= Rect(Point(0, 0), input_mat.size());
	    regionofinterest[4] = Rect (Point(flandmark[6].x,flandmark[6].y - 10), Point(flandmark[2].x,flandmark[4].y)); //right cheek
	    regionofinterest[4] &= Rect(Point(0, 0), input_mat.size());
	    regionofinterest[5] = Rect (Point(flandmark[3].x-30,flandmark[3].y - 30), Point(flandmark[4].x+30,flandmark[4].y+30)); //mouth
	    regionofinterest[5] &= Rect(Point(0, 0), input_mat.size());
	    regionofinterest[6] = Rect (Point(flandmark[3].x,opencv[2].y), Point(flandmark[4].x,flandmark[4].y)); //centerpiece
	    regionofinterest[6] &= Rect(Point(0, 0), input_mat.size());

	    //http://stackoverflow.com/questions/23599169/check-if-cvrect-is-within-cvmat


	    double match_probability [7];

	    for (int i = 0; i < 7; i++){
	    	double minVal; double maxVal;
	    	Point minLoc; Point maxLoc; Point matchLoc;
	    	Mat tmp = input_mat(regionofinterest[i]);
	    	int result_cols =  train_mat.cols - tmp.cols + 1;
	    	int result_rows = train_mat.rows - tmp.rows + 1;
	    	Mat result = Mat( result_cols, result_rows, CV_32FC1 );


	    	matchTemplate( train_mat, tmp, result, CV_TM_CCORR_NORMED );
	    	minMaxLoc( result, &minVal, &maxVal, &minLoc, &maxLoc, Mat() );
	    	matchLoc = maxLoc;
	    	match_probability[i] = (maxVal * 100) - 90; //change from decimal to percentage, then chop off %90
	    	if (match_probability[i] < 0){
	    		match_probability[i] = 0;
	    	}

	    	tmp.release();
	    	result.release();
	    }
	    double score = match_probability[0]*0.13 //eyeline
	    		     + match_probability[1]*0.08 //left eyebrow
	    		     + match_probability[2]*0.08 //right eyebrow
	    		     + match_probability[3]*0.08 //left cheek
	    		     + match_probability[4]*0.08 //right cheek
	    		     + match_probability[5]*0.15 //mouth
	    		     + match_probability[6]*0.40; //centerpiece


	    //////////////////////////////////////////////////////////////////////
	    //NECESSARY, otherwise it runs out of memory and crashes around comparison #510 mohit -> simon
	    env->ReleaseFloatArrayElements(opencv_poi, open_set, 0);
	    env->ReleaseFloatArrayElements(flandmark_poi, fland_set, 0);
	    delete [] regionofinterest;
	    delete [] match_probability;
	    train_mat.release();
	    input_mat.release();
	    ///////////////////////////////////////////////////////////////////////

	    if (score >= 9.0){
	    	return 0;
	    }
	    else{
	    	return -1;
	    }
}


