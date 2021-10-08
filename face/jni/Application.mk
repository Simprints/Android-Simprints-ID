ROOT_PATH:= $(call my-dir)

LIBYUV_DISABLE_JPEG := "yes"

include $(ROOT_PATH)/libyuv/Android.mk

include $(ROOT_PATH)/libyuvjni/Android.mk

APP_MODULES = libyuv libyuvjni
