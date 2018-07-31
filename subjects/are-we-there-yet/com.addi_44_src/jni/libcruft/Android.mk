LOCAL_PATH := $(call my-dir)

include $(CLEAR_VARS)

LOCAL_MODULE := cruft

LOCAL_SRC_FILES := libcruft.a

include $(PREBUILT_STATIC_LIBRARY)