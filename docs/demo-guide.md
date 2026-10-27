# Try SecureDeviceBridge in five minutes

## Install

Build from this repository using JDK 17 or 21 and Android SDK platform 35. An Android 8.0 / API 26 or newer emulator or test device is required. No scanner, account, or server is needed. No downloadable release has been published.

Set `ANDROID_HOME` or configure `sdk.dir` in an untracked `local.properties`, then run from the project root:

```sh
./gradlew :app:assembleDebug
./gradlew :app:installDebug
adb shell am start -n io.github.mani360.securedevicebridge/.MainActivity
```

This creates a debug-signed portfolio demo using synthetic strings. It requests no network, camera, USB, or biometric permissions. The debug APK is generated at `app/build/outputs/apk/debug/app-debug.apk`.

If an installed copy has a different debug signing certificate, Android may reject the update. Uninstall the old demo first only if you are happy to delete its data and demo keys.

## Device sandbox

1. Tap **Connect device**, then **Capture sample**. Expect **Sample captured**.
2. Tap **Timeout**. Expect **Capture timed out** and a reconnect action.
3. Tap **Reconnect device**, then **Detach**. Expect **Device detached**.
4. Reconnect and capture again. **Disconnect** returns to the initial state.

The simulation completes captures after 500 ms; it is not a physical-device latency measurement.

## Local signing

Scroll to **Local signing** and tap **Create key & sign demo**. The status reports that the original message verifies and a changed sample is rejected, along with the platform's key protection metadata. Tap **Delete demo key** to remove the local alias. Signing again creates a new identity.

The signed message is independent of the device sandbox. No biometric authentication, certificate trust validation, or hardware attestation is performed. See [security scope](security.md).

## Explore the implementation

- [Module architecture](architecture.md)
- [Device lifecycle and recovery](device-state-machine.md)
- [Automated checks](testing-strategy.md)

## Screenshot capture

The emulator workflow is configured to upload screenshots from the UI tests as artifacts named `demo-screenshots-api-29` and `demo-screenshots-api-35`. They show the actual running app using synthetic data. They are not bundled in this source guide.
