LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := octave

LOCAL_SRC_FILES := liboctave.a

include $(PREBUILT_STATIC_LIBRARY)