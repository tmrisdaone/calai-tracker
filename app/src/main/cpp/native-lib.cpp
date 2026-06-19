// JNI shim for the CalAI Cactus bridge.
//
// This file is intentionally minimal. It exposes the two native entry points
// the Kotlin side (CactusBridge.kt / CactusEngine.kt) calls and delegates to
// the Cactus FFI exported by the prebuilt libcactus.so under jniLibs/<abi>/.
//
// The Cactus FFI surface (see app/src/main/cpp/cactus/ffi/cactus_ffi.h) is
// richer than the old sketch this file used to assume:
//
//   cactus_model_t cactus_init(const char* model_path,
//                              const char* corpus_dir,    // nullable
//                              bool        cache_index);
//
//   int cactus_complete(cactus_model_t model,
//                       const char*    messages_json,   // chat history
//                       char*          response_buffer,
//                       size_t         buffer_size,
//                       const char*    options_json,    // nullable
//                       const char*    tools_json,      // nullable
//                       cactus_token_callback callback, // nullable
//                       void*          user_data,       // nullable
//                       const uint8_t* pcm_buffer,      // nullable
//                       size_t         pcm_buffer_size);
//
// Wiring that up properly — model lifetime, JSON message construction, token
// streaming callback, output buffer management — belongs in the Kotlin layer
// (see CactusEngine.kt) once the Cactus integration is real. For now this
// shim just compiles and links against libcactus.so so the assembleDebug
// pipeline can produce an APK.
//
// TODO: Replace the stub bodies below with real Cactus FFI calls when the
// Kotlin-side engine is ready to drive the native API.

#include <jni.h>
#include <string>
#include <android/log.h>
#include "cactus/cactus.h"

#define LOG_TAG "CalAI_Native"
#define LOGI(...) __android_log_print(ANDROID_LOG_INFO, LOG_TAG, __VA_ARGS__)

extern "C"
JNIEXPORT jstring JNICALL
Java_com_calai_tracker_CactusBridge_nativeInit(JNIEnv *env, jobject /*thiz*/, jstring model_path) {
    const char *path = env->GetStringUTFChars(model_path, nullptr);
    LOGI("nativeInit called with model path: %s", path);
    env->ReleaseStringUTFChars(model_path, path);

    // TODO: real Cactus integration. The current cactus_init signature is
    // (model_path, corpus_dir /*nullable*/, cache_index). Wiring a real model
    // load belongs in CactusEngine.kt.
    return env->NewStringUTF("Cactus integration pending");
}

extern "C"
JNIEXPORT jstring JNICALL
Java_com_calai_tracker_CactusBridge_nativeGenerate(JNIEnv *env, jobject /*thiz*/, jstring prompt) {
    const char *text = env->GetStringUTFChars(prompt, nullptr);
    LOGI("nativeGenerate called with prompt: %s", text);
    env->ReleaseStringUTFChars(prompt, text);

    // TODO: real Cactus integration. The current cactus_complete signature is
    // (model, messages_json, response_buffer, buffer_size, options_json,
    //  tools_json, callback, user_data, pcm_buffer, pcm_buffer_size). The
    // real call (with a loaded model handle and a properly sized output
    // buffer) belongs in CactusEngine.kt.
    return env->NewStringUTF("Cactus integration pending");
}
