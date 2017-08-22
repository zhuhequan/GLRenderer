LOCAL_PATH := $(call my-dir)
include $(CLEAR_VARS)
LOCAL_CFLAGS    := -DEGL_EGLEXT_PROTOTYPES -DGL_GLEXT_PROTOTYPES
LOCAL_CFLAGS    += -Wno-unused-parameter
LOCAL_MODULE := glrenderer
LOCAL_MODULE_TAGS := optional

LOCAL_SRC_FILES :=  DrawGLFunctor.cpp \
                    com_android_common_renderer_DrawGLFunctor.cpp \
                    com_android_common_renderer_Utils.cpp \
                    com_android_common_renderer_BitmapBlur \
                    Register.cpp

LOCAL_LDLIBS+= -llog -lGLESv2 -lEGL -landroid -ljnigraphics
include $(BUILD_SHARED_LIBRARY)
