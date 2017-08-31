LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := gfortran

LOCAL_SRC_FILES := libgfortran.a

include $(PREBUILT_STATIC_LIBRARY)