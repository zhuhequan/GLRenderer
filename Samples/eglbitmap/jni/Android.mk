LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_CFLAGS    := -DEGL_EGLEXT_PROTOTYPES -DGL_GLEXT_PROTOTYPES
LOCAL_CFLAGS    += -Wno-unused-parameter

LOCAL_MODULE := libeglbitmap
LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES :=  EGLBitmap.cpp \
                    com_android_common_renderer_EGLBitmap.cpp

LOCAL_C_INCLUDES += \
    frameworks/base/native/include \
    frameworks/base/core/jni/android/graphics \
    frameworks/base/libs/hwui

LOCAL_SHARED_LIBRARIES := libGLESv2 \
                          libEGL \
                          libgui \
                          libcutils \
                          libutils \
                          libandroid \
                          libui \
                          libjnigraphics\
                          libandroid_runtime \
                          libskia
include $(BUILD_SHARED_LIBRARY)
