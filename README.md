# CalAI Tracker 🥗

High-performance AI-powered calorie and nutrition tracker with local LLM support.

## 🚀 Key Features
- **AI Food Analysis:** Instant nutritional estimation from food images.
- **Local Inference (Cactus Engine):** Run Gemma 4 models locally on-device for privacy and offline capability.
- **Hybrid Architecture:** Seamlessly switch between Cloud APIs and Native Local Inference.
- **Modern UI:** Built with Jetpack Compose using a luxury glassmorphism aesthetic.

## 🛠 Technical Stack
- **Frontend:** Kotlin, Jetpack Compose
- **Native Core:** C++ (Cactus Engine)
- **AI Models:** Gemma 4 (Local) / OpenAI-Compatible APIs (Cloud)
- **Infrastructure:** Android NDK, JNI, ARM64 Optimization

## 📦 Native Integration
This project utilizes the **Cactus Engine** for high-performance local inference.
- **Native Library:** `libcactus.so` (ARM64)
- **JNI Bridge:** `libcalai_jni.so` provides a thin layer for the JVM to communicate with the C++ core.

## ⚙️ Local Setup
To use the local AI engine:
1. Open **Settings**.
2. Select a local GGUF/Cactus compatible model path on your device.
3. The app will automatically route analysis requests to the local engine.

## 🛠 Build Instructions

The native build links against the pre-compiled `libcactus.so` shipped under
`app/src/main/jniLibs/arm64-v8a/`, so the Android build no longer compiles the
Cactus source tree itself. That means a headless Gradle build works as long as
you have the Android NDK installed (r25c or newer) — no need to run
`cpp/cactus/build.sh`.

### Option A — Android Studio (easiest)
1. Clone the repo.
2. Open in Android Studio (Ladybug or newer).
3. Build → Build Bundle(s) / APK(s) → Build APK(s).

### Option B — Headless Gradle (Linux / macOS / WSL2 with full NDK)
```bash
# One-time: install the Android command-line tools and NDK r25c+, then
# export ANDROID_HOME (or ANDROID_SDK_ROOT) and ANDROID_NDK_HOME.
git clone https://github.com/tmrisdaone/calai-tracker.git
cd calai-tracker
./gradlew assembleDebug
# APK at: app/build/outputs/apk/debug/app-debug.apk
```

### Regenerating `libcactus.so` from source (advanced)
Only needed if you change the Cactus engine itself. Requires a non-Android
host with cmake + clang/g++.
```bash
cd app/src/main/cpp/cactus
./build.sh   # produces libcactus.a; rebuild the Android JNI shim and re-stage
             # the .so into src/main/jniLibs/arm64-v8a/libcactus.so
```

> Note: A full native build does **not** work in Termux. The NDK toolchain it
> ships is incomplete (missing `lld`, sysroot pieces, etc.), so use a Linux
> machine, WSL2, macOS, or Android Studio.
