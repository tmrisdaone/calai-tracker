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
Due to the complexity of the native ARM64 build, it is recommended to build the APK using Android Studio on a PC:
1. Clone the repo.
2. Open in Android Studio (Ladybug or newer).
3. Build $\rightarrow$ Build Bundle(s) / APK(s) $\rightarrow$ Build APK(s).
