LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := gfortranbegin

LOCAL_SRC_FILES := libgfortranbegin.a

include $(PREBUILT_STATIC_LIBRARY)