LOCAL_PATH := $(call my-dir)

#Face Recognition Configuration
include $(CLEAR_VARS)
OPENCV_CAMERA_MODULES:=off
OPENCV_INSTALL_MODULES:=on

include C:/Android/OpenCV-2.4.9-android-sdk/sdk/native/jni/OpenCV.mk

LOCAL_SRC_FILES  := com_macaps_nativecv_NativeFaceRecognizer.cpp
LOCAL_C_INCLUDES += $(LOCAL_PATH)
LOCAL_LDLIBS     += -llog -ldl
LOCAL_MODULE     := face_recognizer
include $(BUILD_SHARED_LIBRARY)




#Image Processing Configuration
include $(CLEAR_VARS)
OPENCV_CAMERA_MODULES:=off
OPENCV_INSTALL_MODULES:=on

include C:/Android/OpenCV-2.4.9-android-sdk/sdk/native/jni/OpenCV.mk
 
LOCAL_SRC_FILES  := com_macaps_nativecv_NativeFilter.cpp
LOCAL_C_INCLUDES +=$(LOCAL_PATH)
LOCAL_LDLIBS     += -llog -ldl
LOCAL_MODULE	 := native_filter
include $(BUILD_SHARED_LIBRARY)




#Facial Feature Detection Configuration
include $(CLEAR_VARS)
OPENCV_CAMERA_MODULES:=off
OPENCV_INSTALL_MODULES:=on

include C:/Android/OpenCV-2.4.9-android-sdk/sdk/native/jni/OpenCV.mk
 
LOCAL_SRC_FILES  := \
					com_macaps_nativecv_FacialPointDetector.cpp \
					flandmark_detector.cpp \
					liblbp.cpp

LOCAL_C_INCLUDES +=$(LOCAL_PATH)
LOCAL_LDLIBS     += -llog -ldl
LOCAL_MODULE	 := fp_detector
include $(BUILD_SHARED_LIBRARY)
