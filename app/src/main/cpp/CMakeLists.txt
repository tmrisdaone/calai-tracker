cmake_minimum_required(VERSION 3.22.1)

project(CalAI_Native)

# CalAI uses a PRE-COMPILED libcactus.so shipped under src/main/jniLibs/<abi>/.
# The full Cactus source tree lives under cpp/cactus/ for reference and for
# regenerating libcactus.so on a host machine (run cpp/cactus/build.sh, which
# drives cpp/cactus/CMakeLists.txt). The Android build below does NOT compile
# that source tree — it just links our thin JNI shim against the prebuilt .so.
#
# See cpp/cactus/CMakeLists.txt and cpp/cactus/build.sh for the host recipe.

# Point to the pre-compiled libcactus.so. The prebuilt binary lives under
# app/src/main/jniLibs/<abi>/, NOT under the cmake source dir. This
# CMakeLists.txt lives at app/src/main/cpp/CMakeLists.txt, so the path
# from here to the prebuilt is "../jniLibs/<abi>/libcactus.so" — just one
# level up, not two (the previous ../../ version resolved to app/jniLibs/
# which doesn't exist).
#
# Use ANDROID_ABI rather than CMAKE_ANDROID_SMI_ARCH: ANDROID_ABI is the
# stable, AGP-set value (e.g. "arm64-v8a"); CMAKE_ANDROID_SMI_ARCH is the
# raw NDK toolchain value and is not always populated, which caused the
# ninja build to look for "app/src/jniLibs/libcactus.so" with an empty
# arch segment.
set(CACTUS_LIB_PATH ${CMAKE_CURRENT_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libcactus.so)

# Defensive: if the prebuilt isn't at the expected location, print the
# computed path so the build log shows the exact problem instead of just
# "missing and no known rule to make it".
if(NOT EXISTS "${CACTUS_LIB_PATH}")
    message(FATAL_ERROR
        "calai_jni: prebuilt libcactus.so not found at ${CACTUS_LIB_PATH}. "
        "Expected layout: app/src/main/jniLibs/<abi>/libcactus.so. "
        "ANDROID_ABI='${ANDROID_ABI}', CMAKE_CURRENT_SOURCE_DIR='${CMAKE_CURRENT_SOURCE_DIR}'.")
endif()

# If the above path is not ideal for CMake's specific structure, we explicitly define a pre-built library
add_library(cactus SHARED IMPORTED)
set_target_properties(cactus PROPERTIES IMPORTED_LOCATION ${CACTUS_LIB_PATH})

# Create a thin wrapper for CalAI to interact with Cactus
add_library(calai_jni SHARED
    native-lib.cpp
)

target_link_libraries(calai_jni cactus log)
