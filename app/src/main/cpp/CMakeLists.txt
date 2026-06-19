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
# app/src/main/jniLibs/<abi>/, NOT under the cmake source dir. Compute the path
# from the location of this CMakeLists.txt (CMAKE_CURRENT_SOURCE_DIR = app/src/main/cpp/)
# so the link step finds the right file on every build host.
set(CACTUS_LIB_PATH ${CMAKE_CURRENT_SOURCE_DIR}/../../jniLibs/${CMAKE_ANDROID_SMI_ARCH}/libcactus.so)

# If the above path is not ideal for CMake's specific structure, we explicitly define a pre-built library
add_library(cactus SHARED IMPORTED)
set_target_properties(cactus PROPERTIES IMPORTED_LOCATION ${CACTUS_LIB_PATH})

# Create a thin wrapper for CalAI to interact with Cactus
add_library(calai_jni SHARED
    native-lib.cpp
)

target_link_libraries(calai_jni cactus log)
