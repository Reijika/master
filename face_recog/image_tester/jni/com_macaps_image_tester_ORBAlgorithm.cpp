#include <com_macaps_image_tester_ORBAlgorithm.h>
#include <opencv2/core/core.hpp>
#include <opencv2/highgui/highgui.hpp>
#include <opencv2/imgproc/imgproc.hpp>
#include <opencv2/contrib/contrib.hpp>


#include <iostream>
#include <string>
#include <cstring>
#include <vector>
#include <stdlib.h>
#include <android/log.h>


#define LOGD(...) ((void)__android_log_print(ANDROID_LOG_DEBUG, LOG_TAG, __VA_ARGS__))
#define nullptr 0

using namespace std;
using namespace cv;

/*

		//FASTX( input_mat , keypoints_input , 60 , false , FastFeatureDetector::TYPE_9_16 );
		//cv::BriefDescriptorExtractor *extractor = new cv::BriefDescriptorExtractor();
		//drawKeypoints(output_mat, keypoints_input, output_mat);
		//cv::BriefDescriptorExtractor *extractor = new cv::BriefDescriptorExtractor();


		//drawKeypoints(input_mat,keypoints_input,output_mat, Scalar::all(-1), DrawMatchesFlags::NOT_DRAW_SINGLE_POINTS);
		//std::vector<std::vector<cv::DMatch> > matches;
		//std::vector<std::vector<cv::DMatch> > good_matches;
		//matcher.knnMatch(descriptor_input, descriptor_train, matches, 2, Mat(), false);

*/

JNIEXPORT jstring JNICALL Java_com_macaps_image_1tester_ORBAlgorithm_predict
  (JNIEnv * env, jclass, jlong input_addr, jlong training_addr){

		Mat& train_mat = *(Mat*)training_addr;
		cvtColor(train_mat, train_mat, CV_BGR2GRAY);

		Mat& input_mat = *(Mat*)input_addr;
		cvtColor(input_mat, input_mat, CV_BGR2GRAY);

		std::vector<KeyPoint> keypoints_input, keypoints_train;
		//OrbFeatureDetector detector (500, 1.2, 8, 31, 0, 2, ORB::HARRIS_SCORE, 31);
		OrbDescriptorExtractor extractor (500, 1.2, 8, 31, 0, 2, ORB::HARRIS_SCORE, 31);;
		//detector.detect(input_mat, keypoints_input);
		//detector.detect(train_mat, keypoints_train);

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

			if (xdifference <= 30 && ydifference <= 30){
				good_matches.push_back( cross_valid_match[i] );
			}
		}

		//1 nail in the coffin, 2 nails in the coffin, 3 nails in the coffin, 4 nails in the coffin...
		//Default Match Output
		//drawMatches( input_mat, keypoints_input, train_mat, keypoints_train,
     	//             good_matches, output_mat, Scalar::all(-1), Scalar::all(-1),
	    //             vector<char>(), DrawMatchesFlags::NOT_DRAW_SINGLE_POINTS );

		char str_a[20];
		sprintf(str_a, "match12: %d, ", match12.size());
		char str_b[20];
		sprintf(str_b, "match21: %d, ", match21.size());
		char str_c[30];
		sprintf(str_c, "cross_valid_match: %d, ", cross_valid_match.size());
		char str_d[20];
		sprintf(str_d, "good_matches: %d", good_matches.size());

		char outstr[90];
		strcpy (outstr, str_a);
		strcat (outstr, str_b);
		strcat (outstr, str_c);
		strcat (outstr, str_d);

		if (good_matches.size() >= 130){
			strcat (outstr, "_0");
		}
		else{
			strcat (outstr, "_1");
		}


		return env->NewStringUTF(outstr);
}
