package com.calai.tracker

import android.util.Log
import android.content.Context

/**
 * Kotlin wrapper around the Cactus FFI. The JNI symbols
 * `Java_com_calai_tracker_CactusBridge_nativeInit` and `_nativeGenerate`
 * exist in native-lib.cpp as stubs that return "Cactus integration
 * pending" without actually calling into libcactus.so.
 *
 * The `external` declarations are kept for ABI compatibility with the
 * prebuilt app (so existing `CactusInferenceEngine` call sites compile),
 * but the system loadLibrary calls were removed from the `init` block
 * because the 45 MB prebuilt `libcactus.so` has runtime NDK dependencies
 * that are not satisfied on all devices — loading it on app start was
 * crashing the launcher before any UI rendered.
 *
 * If you need the real Cactus inference path, restore the loadLibrary
 * calls AND verify the device has the matching NDK toolchain symbols
 * (libc++_shared, libdl, liblog, etc.) that libcactus.so depends on.
 */
object CactusBridge {
    private const val TAG = "CactusBridge"

    external fun nativeInit(modelPath: String): String
    external fun nativeGenerate(prompt: String): String

    fun initialize(context: Context, modelPath: String): String {
        return try {
            nativeInit(modelPath)
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, "nativeInit not linked; returning stub", e)
            "Cactus integration pending"
        } catch (e: Throwable) {
            Log.e(TAG, "Init failed", e)
            "Failure: ${e.message}"
        }
    }

    fun generate(prompt: String): String {
        return try {
            nativeGenerate(prompt)
        } catch (e: UnsatisfiedLinkError) {
            Log.w(TAG, "nativeGenerate not linked; returning stub", e)
            "Cactus integration pending"
        } catch (e: Throwable) {
            Log.e(TAG, "Generation failed", e)
            "Error: ${e.message}"
        }
    }
}
