LOCAL_PATH := $(call my-dir)
ROOT_PATH := $(LOCAL_PATH)

include $(call all-subdir-makefiles)
include $(CLEAR_VARS)

LOCAL_PATH = $(ROOT_PATH)
LOCAL_CFLAGS := -Wall -Wextra

LOCAL_MODULE    := addiLib

LOCAL_LDLIBS := -L$(SYSROOT)/usr/lib -llog
LOCAL_STATIC_LIBRARIES := fftw3
LOCAL_SRC_FILES := addiLib.c

include $(BUILD_SHARED_LIBRARY)

include $(CLEAR_VARS)

LOCAL_PATH = $(ROOT_PATH)

LOCAL_MODULE    := octaveLib
LOCAL_LDLIBS := -llog -lz 

LOCAL_STATIC_LIBRARIES := octinterp octave cruft pcre lapack blas gfortranbegin gfortran

LOCAL_SRC_FILES := octaveLib.cpp

include $(BUILD_SHARED_LIBRARY)