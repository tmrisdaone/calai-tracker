#include <jni.h>
#include <string>
#include <android/log.h>
#include "cactus/cactus.h"

#define LOG_TAG "CalAI_Native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C"
JNIEXPORT jstring JNICALL
Java_com_calai_tracker_CactusBridge_nativeInit(J boulder, jobject thiz, jstring model_path) {
    const char *path = env->GetStringUTFChars(model_path, 0);
    LOGI("Initializing Cactus with model path: %s", path);
    
    // This calls the actual Cactus FFI init
    int result = cactus_init(path); 
    
    env->ReleaseStringUTFChars(model_path, path);
    return env->NewStringUTF(result == 0 ? "Success" : "Error initializing Cactus");
}

JNIEXPORT jstring JNICALL
Java_com_calai_tracker_CactusBridge_nativeGenerate(JNIEnv *env, jobject thiz, jstring prompt) {
    const char *text = env->GetStringUTFChars(prompt, 0);
    
    // Call Cactus FFI to generate text
    const char* response = cactus_complete(text);
    
    env->ReleaseStringUTFChars(prompt, text);
    return env->NewStringUTF(response ? response : "No response from model");
}
