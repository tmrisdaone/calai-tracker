cmake_minimum_required(VERSION 3.22.1)

project(CalAI_Native)

# Point to the pre-compiled libcactus.so
set(CACTUS_LIB_PATH ${CMAKE_SOURCE_DIR}/jniLibs/${CMAKE_ANDROID_SMI_ARCH}/libcactus.so)

# If the above path is not ideal for CMake's specific structure, we explicitly define a pre-built library
add_library(cactus SHARED IMPORTED)
set_target_properties(cactus PROPERTIES IMPORTED_LOCATION ${CMAKE_SOURCE_DIR}/jniLibs/${CMAKE_ANDROID_SMI_ARCH}/libcactus.so)

# Create a thin wrapper for CalAI to interact with Cactus
add_library(calai_jni SHARED
    native-lib.cpp
)

target_link_libraries(calai_jni cactus log)
