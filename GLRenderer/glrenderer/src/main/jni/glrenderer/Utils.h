/**
 * @version  1.0
 * @author   zhuhequan
 * @date     2015-06-25
 * */
#pragma once
#include <jni.h>
#include "helper.h"

namespace android {
struct field {
    const char *class_name;
    const char *field_name;
    const char *field_type;
    jfieldID   *jfield;
};

struct fields_t {
    jboolean    init;
    jfieldID    info_clip_left;
    jfieldID    info_clip_top;
    jfieldID    info_clip_right;
    jfieldID    info_clip_bottom;
    jfieldID    info_viewport_width;
    jfieldID    info_viewport_height;
    jfieldID    info_transform;
    jfieldID    info_is_layer;
    jmethodID   info_ctor;
    jmethodID   post_event;
    fields_t() {
        init = JNI_FALSE;
    };
};

static int find_fields(JNIEnv *env, field *fields, int count)
{
    for (int i = 0; i < count; i++) {
        field *f = &fields[i];
        jclass clazz = env->FindClass(f->class_name);
        if (clazz == NULL) {
            LOG_E("Can't find %s", f->class_name);
            return -1;
        }

        jfieldID field = env->GetFieldID(clazz, f->field_name, f->field_type);
        if (field == NULL) {
            LOG_E("Can't find %s.%s", f->class_name, f->field_name);
            return -1;
        }

        *(f->jfield) = field;
    }

    return 0;
}

}

