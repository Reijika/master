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

#include <com_macaps_nativecv_NativeFaceRecognizer.h>

#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
#define nullptr 0

using namespace std;
using namespace cv;


JNIEXPORT jint JNICALL Java_com_macaps_nativecv_NativeFaceRecognizer_predictLBPH
  (JNIEnv * env, jclass, jlong input_addr, jlongArray training_addr, jstring filepath, jboolean save){

	/*  NOTE: A clean LBPHFaceRecognizer with no threshold argument always returns the image with the closest match to the input.
		Furthermore, the threshold value of 100.0 was selected using a limited image set. Further refinement may be required.
		Online sources do not give any recommendations. Tutorials use an arbitrary value of 123.0.

		A cross validation test using images from 33 people gives a rough optimal threshold of 45 for most cases.
		IRL, this result is most likely too strict. A slightly higher one may be recommended.
	*/

	vector<Mat>images;
	vector<int>labels;

	const char * pathstring = env->GetStringUTFChars(filepath, nullptr);

	jlong *training_set = env->GetLongArrayElements(training_addr, NULL);
	jsize length = env->GetArrayLength(training_addr);

	for (jint i = 0; i < length;i++){
		Mat& temp_mat = *(Mat*)training_set [i];
		cvtColor(temp_mat, temp_mat, CV_BGR2GRAY);
		images.push_back(temp_mat);
		labels.push_back(i);
	}

	Ptr<FaceRecognizer> model;
	if (save){
		model = createLBPHFaceRecognizer(1, 8, 8, 8, 150.0);
		model->train(images, labels);
		model->save(pathstring);
	}
	else{
		model = createLBPHFaceRecognizer(1, 8, 8, 8, 150.0);
		model->load(pathstring);
	}

	//Convert input image to grayscale
	Mat& input_mat = *(Mat*)input_addr;
	cvtColor(input_mat, input_mat, CV_BGR2GRAY);

	//Execute prediction
	jint predictedLabel = -1;
	jdouble confidence = 0.0; //the lower the confidence, the closer the match
	model->predict(input_mat, predictedLabel, confidence);

	env->ReleaseStringUTFChars(filepath, pathstring);

	return predictedLabel;
}

JNIEXPORT jint JNICALL Java_com_macaps_nativecv_NativeFaceRecognizer_predictORB
  (JNIEnv *env, jclass, jlong input_addr, jlongArray training_addr, jlong output_addr){

	jlong *training_set = env->GetLongArrayElements(training_addr, NULL);
	jsize length = env->GetArrayLength(training_addr);

	Mat& train_mat = *(Mat*)training_set [0];
	cvtColor(train_mat, train_mat, CV_BGR2GRAY);

	Mat& input_mat = *(Mat*)input_addr;
	cvtColor(input_mat, input_mat, CV_BGR2GRAY);

	Mat& output_mat = *(Mat*)output_addr;
	cvtColor(output_mat, output_mat, CV_BGR2GRAY);

	std::vector<KeyPoint> keypoints_input, keypoints_train;
	OrbDescriptorExtractor extractor (500, 1.2, 8, 5, 0, 2, ORB::HARRIS_SCORE, 5);;

	FAST(input_mat,keypoints_input,5,true);
	FAST(train_mat,keypoints_train,5,true);

	Mat descriptor_input, descriptor_train, mask;

	extractor.compute(input_mat, keypoints_input, descriptor_input);
	extractor.compute(train_mat, keypoints_train, descriptor_train);

	BFMatcher matcher(NORM_HAMMING, false);
	std::vector< DMatch > match12;
	std::vector< DMatch > match21;
	std::vector< DMatch > cross_valid_match;
	std::vector< DMatch > good_matches;

	matcher.match(descriptor_input, descriptor_train, match12);
	matcher.match(descriptor_train, descriptor_input, match21);

	//Cross Validation
	for( size_t i = 0; i < match12.size(); i++ ){
	    DMatch input_match = match12[i];
	    DMatch train_match = match21[input_match.trainIdx];
	    if( train_match.trainIdx == input_match.queryIdx )
	    	cross_valid_match.push_back( input_match );
	}

	//Euclidean distance
	for ( size_t i = 0; i < cross_valid_match.size(); i++){
		Point input_coord = keypoints_input[cross_valid_match[i].queryIdx].pt;
		Point train_coord = keypoints_train[cross_valid_match[i].trainIdx].pt;

		double xdifference = abs(input_coord.x - input_coord.x);
		double ydifference = abs(input_coord.y - train_coord.y);

		if (xdifference <= 8 && ydifference <= 8){ //euclidean distance of 30 by 30 for 300x300 resolution
			good_matches.push_back( cross_valid_match[i] );
		}
	}

	//Default Match Output
	drawMatches( input_mat, keypoints_input, train_mat, keypoints_train,
	              good_matches, output_mat, Scalar::all(-1), Scalar::all(-1),
	              vector<char>(), DrawMatchesFlags::NOT_DRAW_SINGLE_POINTS );

	if (good_matches.size() >= 50){ //If we have at least 50 'good' matches, it passes
		return 0;
	}
	else{
		return -1;
	}
}

JNIEXPORT jint JNICALL Java_com_macaps_nativecv_NativeFaceRecognizer_predictCNN
  (JNIEnv *env, jclass, jlong input_addr, jlongArray training_addr, jlong output_addr, jfloatArray opencv_poi, jfloatArray flandmark_poi){

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
	jlong *training_set = env->GetLongArrayElements(training_addr, NULL);
	jsize length = env->GetArrayLength(training_addr);
	Mat& train_mat = *(Mat*)training_set [0];
	cvtColor(train_mat, train_mat, CV_BGR2GRAY);

	//Prep the photo capture image
	Mat& input_mat = *(Mat*)input_addr;
	cvtColor(input_mat, input_mat, CV_BGR2GRAY);

	//Prep an output image for debug
	Mat& output_mat = *(Mat*)output_addr;
	cvtColor(output_mat, output_mat, CV_BGR2GRAY);


	//Offset Constants for ROIs
	const int a = abs(opencv[2].x - opencv[0].x)*0.1;
	const int b = abs(opencv[2].x - opencv[0].x)*0.2;
	const int c = abs(opencv[2].x - opencv[0].x)*0.3;
	const int d = abs(opencv[2].x - opencv[0].x)*0.6;

	//Define the regions of interest for template matching
	Rect regionofinterest [7];
	regionofinterest[0] = Rect (Point(flandmark[5].x, flandmark[5].y - c), Point(flandmark[6].x, flandmark[6].y + c)); // eye line
	regionofinterest[0] &= Rect(Point(0, 0), input_mat.size());
	regionofinterest[1] = Rect (Point(flandmark[5].x, opencv[0].y - d), Point(flandmark[1].x, flandmark[1].y+b)); //left eyebrow
	regionofinterest[1] &= Rect(Point(0, 0), input_mat.size());
	regionofinterest[2] = Rect (Point(flandmark[6].x, opencv[1].y - d), Point(flandmark[2].x, flandmark[2].y+b)); //right eyebrow
	regionofinterest[2] &= Rect(Point(0, 0), input_mat.size());
	regionofinterest[3] = Rect (Point(flandmark[5].x, flandmark[5].y - a), Point(flandmark[1].x, flandmark[3].y)); //left cheek
	regionofinterest[3] &= Rect(Point(0, 0), input_mat.size());
	regionofinterest[4] = Rect (Point(flandmark[6].x, flandmark[6].y - a), Point(flandmark[2].x, flandmark[4].y)); //right cheek
	regionofinterest[4] &= Rect(Point(0, 0), input_mat.size());
	regionofinterest[5] = Rect (Point(flandmark[3].x - c, flandmark[3].y - c), Point(flandmark[4].x + c, flandmark[4].y+c)); //mouth
	regionofinterest[5] &= Rect(Point(0, 0), input_mat.size());
	regionofinterest[6] = Rect (Point(flandmark[3].x, opencv[2].y), Point(flandmark[4].x, flandmark[4].y)); //centerpiece
	regionofinterest[6] &= Rect(Point(0, 0), input_mat.size());


    Rect match_roi [7]; //Store the match locations on the comparison image
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
    	match_roi[i] = Rect(matchLoc, Point( matchLoc.x + tmp.cols , matchLoc.y + tmp.rows ));
    }

    for (int i = 0; i < 7; i++){
       	rectangle(train_mat, match_roi[i], Scalar::all(0), 2, 8, 0 );
        rectangle(input_mat, regionofinterest[i], Scalar(255,255,255), 2, -1, 0);
    }

    hconcat(input_mat, train_mat, output_mat);

    double score = match_probability[0]*0.13 //eyeline
    	    		     + match_probability[1]*0.08 //left eyebrow
    	    		     + match_probability[2]*0.08 //right eyebrow
    	    		     + match_probability[3]*0.08 //left cheek
    	    		     + match_probability[4]*0.08 //right cheek
    	    		     + match_probability[5]*0.15 //mouth
    	    		     + match_probability[6]*0.40; //centerpiece

    env->ReleaseFloatArrayElements(opencv_poi, open_set, 0);
    env->ReleaseFloatArrayElements(flandmark_poi, fland_set, 0);
    delete [] regionofinterest;
    delete [] match_probability;
    train_mat.release();
    input_mat.release();

    if (score >= 9.0){
    	return 0;
    }
    else{
    	return -1;
    }

}
