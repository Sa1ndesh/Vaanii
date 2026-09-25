# Vani Kanoon - Android Mobile Setup Guide

This guide covers setting up and building the Vani Kanoon Android application using Capacitor.

## Prerequisites

### Required Software

1. **Node.js** (v18 or higher)
   - Download from: https://nodejs.org/
   - Verify installation: `node --version`

2. **Android Studio** (Latest stable version)
   - Download from: https://developer.android.com/studio
   - During installation, ensure these components are selected:
     - Android SDK
     - Android SDK Platform-Tools
     - Android Virtual Device (AVD)

3. **Java Development Kit (JDK 17)**
   - Android Studio bundles JDK, but you may need to configure it
   - Alternatively, install OpenJDK 17:
     - Windows: Download from https://adoptium.net/
     - Verify: `java -version`

### Android SDK Requirements

Open Android Studio and go to **Settings > Languages & Frameworks > Android SDK**:

- **SDK Platforms**: Install Android 14 (API 36) or the latest available
- **SDK Tools**: Ensure these are installed:
  - Android SDK Build-Tools
  - Android SDK Command-line Tools
  - Android Emulator
  - Android SDK Platform-Tools

### Environment Variables (Windows)

Add these to your system environment variables:

```
ANDROID_HOME = C:\Users\<YourUsername>\AppData\Local\Android\Sdk
JAVA_HOME = C:\Program Files\Android\Android Studio\jbr
```

Add to PATH:
```
%ANDROID_HOME%\platform-tools
%ANDROID_HOME%\tools
%ANDROID_HOME%\tools\bin
```

## Step-by-Step Setup

### 1. Install Dependencies

```bash
cd frontend
npm install
```

### 2. Build the Web Application

```bash
npm run build
```

This creates the production build in the `dist/` folder.

### 3. Sync with Android Project

```bash
npx cap sync android
```

This copies the web assets to the Android project and updates native dependencies.

### 4. Open in Android Studio

```bash
npx cap open android
```

Or open the `frontend/android` folder directly in Android Studio.

### 5. Wait for Gradle Sync

When Android Studio opens, it will automatically sync the Gradle project. This may take several minutes on first run as it downloads dependencies.

## Adding the Offline LLM Model

The app supports offline AI capabilities through a local LLM model. Here's how to add it:

### Model Location

Place your GGUF model file in one of these locations:

**Option A: App Assets (bundled with APK)**
```
frontend/android/app/src/main/assets/models/
```

**Option B: Device Storage (downloaded after install)**
```
/sdcard/Android/data/com.vanikanoon.app/files/models/
```

### Recommended Models

For mobile devices, use quantized models optimized for limited resources:

| Model | Size | RAM Required | Quality |
|-------|------|--------------|---------|
| Phi-3-mini-4k-Q4_K_M.gguf | ~2.4 GB | 4 GB | Good |
| Mistral-7B-Q4_K_M.gguf | ~4.3 GB | 6 GB | Better |
| Llama-3.2-3B-Q4_K_M.gguf | ~2 GB | 3 GB | Good |

### Adding Model to Assets

1. Create the models directory:
   ```bash
   mkdir -p frontend/android/app/src/main/assets/models
   ```

2. Copy your model file:
   ```bash
   cp /path/to/your-model.gguf frontend/android/app/src/main/assets/models/
   ```

3. Update `capacitor.config.ts` if needed to reference the model path

**Note**: Large models significantly increase APK size. For production, consider downloading models on first launch instead.

## Building and Running

### Development Build (Debug APK)

**Using npm scripts:**
```bash
npm run mobile:build    # Builds web + syncs to Android
npm run mobile:open     # Opens Android Studio
```

**From Android Studio:**
1. Select your device/emulator from the toolbar
2. Click the green "Run" button (or press Shift+F10)

**Using command line:**
```bash
cd frontend/android
./gradlew assembleDebug
```

The debug APK will be at:
```
frontend/android/app/build/outputs/apk/debug/app-debug.apk
```

### Production Build (Release APK)

1. **Generate a signing key** (one-time):
   ```bash
   keytool -genkey -v -keystore vani-kanoon-release.keystore -alias vanikanoon -keyalg RSA -keysize 2048 -validity 10000
   ```

2. **Configure signing in** `android/app/build.gradle`:
   ```gradle
   android {
       signingConfigs {
           release {
               storeFile file('vani-kanoon-release.keystore')
               storePassword 'your-store-password'
               keyAlias 'vanikanoon'
               keyPassword 'your-key-password'
           }
       }
       buildTypes {
           release {
               signingConfig signingConfigs.release
               minifyEnabled true
               proguardFiles getDefaultProguardFile('proguard-android-optimize.txt'), 'proguard-rules.pro'
           }
       }
   }
   ```

3. **Build the release APK:**
   ```bash
   cd frontend/android
   ./gradlew assembleRelease
   ```

The release APK will be at:
```
frontend/android/app/build/outputs/apk/release/app-release.apk
```

### Running on Physical Device

1. **Enable Developer Options** on your Android device:
   - Go to Settings > About Phone
   - Tap "Build Number" 7 times

2. **Enable USB Debugging**:
   - Go to Settings > Developer Options
   - Enable "USB Debugging"

3. **Connect device** via USB cable

4. **Accept the debugging prompt** on your device

5. **Select your device** in Android Studio and click Run

### Running on Emulator

1. Open **AVD Manager** in Android Studio (Tools > Device Manager)
2. Create a new virtual device:
   - Select a device definition (e.g., Pixel 7)
   - Choose system image: API 34 or higher
   - Finish configuration
3. Start the emulator
4. Run the app

## Development Mode (Live Reload)

For faster development with live reload:

1. **Start the Vite dev server:**
   ```bash
   cd frontend
   npm run dev
   ```

2. **Ensure `capacitor.config.ts` has the server URL:**
   ```typescript
   server: {
     url: 'http://10.0.2.2:5173',  // 10.0.2.2 maps to localhost from emulator
     cleartext: true
   }
   ```

3. **For physical device**, use your computer's local IP:
   ```typescript
   server: {
     url: 'http://192.168.x.x:5173',  // Your computer's IP
     cleartext: true
   }
   ```

4. **Sync and run:**
   ```bash
   npx cap sync android
   npx cap run android
   ```

**Important**: Remove or comment out the `server` block for production builds.

## Troubleshooting

### Gradle Sync Failed

**Problem**: Android Studio shows "Gradle sync failed"

**Solutions**:
1. File > Invalidate Caches and Restart
2. Delete `frontend/android/.gradle` folder and re-sync
3. Ensure JAVA_HOME points to JDK 17
4. Check internet connection for dependency downloads

### SDK Location Not Found

**Problem**: `SDK location not found`

**Solution**: Create `frontend/android/local.properties`:
```
sdk.dir=C\:\\Users\\<YourUsername>\\AppData\\Local\\Android\\Sdk
```

### App Crashes on Launch

**Problem**: App immediately closes after splash screen

**Solutions**:
1. Check `adb logcat` for error messages:
   ```bash
   adb logcat | grep -i "vanikanoon\|capacitor\|fatal"
   ```
2. Ensure web build completed successfully (`dist/` folder exists)
3. Run `npx cap sync android` to ensure assets are copied

### White Screen

**Problem**: App shows white/blank screen

**Solutions**:
1. Check browser console in Chrome DevTools:
   - Open `chrome://inspect` in Chrome
   - Select your device/app
2. Verify `dist/index.html` exists
3. Check for JavaScript errors in build

### Network Requests Failing

**Problem**: API calls not working

**Solutions**:
1. For emulator: Backend must be accessible at `10.0.2.2:<port>`
2. For device: Use your computer's actual IP address
3. Add network security config for cleartext traffic (already configured)

### Build Takes Too Long

**Problem**: Gradle build is extremely slow

**Solutions**:
1. Increase Gradle memory in `gradle.properties`:
   ```
   org.gradle.jvmargs=-Xmx4096m -XX:+HeapDumpOnOutOfMemoryError
   org.gradle.parallel=true
   org.gradle.caching=true
   ```
2. Enable offline mode for repeated builds (Android Studio settings)

### Model Loading Fails

**Problem**: Offline LLM model not loading

**Solutions**:
1. Verify model file is in correct location
2. Check file permissions on device
3. Ensure sufficient device storage and RAM
4. Try a smaller quantized model (Q4 instead of Q8)

### APK Installation Failed

**Problem**: Cannot install APK on device

**Solutions**:
1. Enable "Install from unknown sources" in device settings
2. Uninstall existing app version first
3. Check if device has sufficient storage
4. Ensure APK is signed (for release builds)

## Project Structure

```
frontend/
├── android/                    # Native Android project
│   ├── app/
│   │   ├── src/main/
│   │   │   ├── assets/        # Web assets (synced from dist/)
│   │   │   ├── java/          # Native Java code
│   │   │   └── res/           # Android resources
│   │   └── build.gradle       # App-level build config
│   ├── build.gradle           # Project-level build config
│   └── variables.gradle       # SDK versions and dependencies
├── capacitor.config.ts        # Capacitor configuration
├── dist/                      # Built web app (generated)
└── package.json               # npm scripts and dependencies
```

## Useful Commands

| Command | Description |
|---------|-------------|
| `npm run build` | Build web app |
| `npm run mobile:build` | Build web + sync Android |
| `npm run mobile:open` | Open Android Studio |
| `npx cap sync android` | Sync web assets to Android |
| `npx cap run android` | Build and run on connected device |
| `npx cap copy android` | Copy web assets only (no native update) |
| `./gradlew assembleDebug` | Build debug APK |
| `./gradlew assembleRelease` | Build release APK |
| `adb devices` | List connected devices |
| `adb install app.apk` | Install APK on device |
| `adb logcat` | View device logs |

## Additional Resources

- [Capacitor Documentation](https://capacitorjs.com/docs)
- [Android Studio User Guide](https://developer.android.com/studio/intro)
- [Android Developer Guides](https://developer.android.com/guide)
