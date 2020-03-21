LOCAL_PATH:= $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := libyuvjni
LOCAL_SRC_FILES := lib_yuv_jni.c
LOCAL_SHARED_LIBRARIES := libyuv

include $(BUILD_SHARED_LIBRARY)