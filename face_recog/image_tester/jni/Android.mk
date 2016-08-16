LOCAL_PATH := $(call my-dir)


#Gamma Correction + CLAHE Filter JNI Component
include $(CLEAR_VARS)
OPENCV_CAMERA_MODULES:=off
OPENCV_INSTALL_MODULES:=on

include C:/Android/OpenCV-2.4.9-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_SRC_FILES  := com_macaps_image_tester_ContrastFilter.cpp
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_LDLIBS     += -llog -ldl
LOCAL_MODULE     := contrast_filter
include $(BUILD_SHARED_LIBRARY)


#OpenCV LBPH JNI Component
include $(CLEAR_VARS)
OPENCV_CAMERA_MODULES:=off
OPENCV_INSTALL_MODULES:=on

include C:/Android/OpenCV-2.4.9-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_SRC_FILES  := com_macaps_image_tester_LBPHAlgorithm.cpp
LOCAL_C_INCLUDES +=$(LOCAL_PATH)
LOCAL_LDLIBS     += -llog -ldl
LOCAL_MODULE     := lbph_algorithm
include $(BUILD_SHARED_LIBRARY)


#ORB Features JNI Component
include $(CLEAR_VARS)
OPENCV_CAMERA_MODULES:=off
OPENCV_INSTALL_MODULES:=on

include C:/Android/OpenCV-2.4.9-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_SRC_FILES  := com_macaps_image_tester_ORBAlgorithm.cpp
LOCAL_C_INCLUDES +=$(LOCAL_PATH)
LOCAL_LDLIBS     += -llog -ldl
LOCAL_MODULE     := orb_algorithm
include $(BUILD_SHARED_LIBRARY)


#Feature Detection Configuration
include $(CLEAR_VARS)
OPENCV_CAMERA_MODULES:=off
OPENCV_INSTALL_MODULES:=on

include C:/Android/OpenCV-2.4.9-android-sdk/sdk/native/jni/OpenCV.mk
 
LOCAL_SRC_FILES  := \
					com_macaps_image_tester_PointDetector.cpp \
					flandmark_detector.cpp \
					liblbp.cpp

LOCAL_C_INCLUDES +=$(LOCAL_PATH)
LOCAL_LDLIBS     += -llog -ldl
LOCAL_MODULE	 := fp_detector
include $(BUILD_SHARED_LIBRARY)



#CNN Template Matching Configuration
include $(CLEAR_VARS)
OPENCV_CAMERA_MODULES:=off
OPENCV_INSTALL_MODULES:=on

include C:/Android/OpenCV-2.4.9-android-sdk/sdk/native/jni/OpenCV.mk
 
LOCAL_SRC_FILES  := com_macaps_image_tester_CNNAlgorithm.cpp
LOCAL_C_INCLUDES +=$(LOCAL_PATH)
LOCAL_LDLIBS     += -llog -ldl
LOCAL_MODULE	 := cnn_algorithm
include $(BUILD_SHARED_LIBRARY)



