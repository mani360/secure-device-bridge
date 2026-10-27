# SecureDeviceBridge

Android/Kotlin portfolio reference project for device integration, explicit state management, and synthetic biometric capture. Built independently by Manitosh Bepari.

**Status:** initial implementation. This is an educational portfolio project; physical device integration, real biometrics, and certificate trust workflows are not implemented.

## Implemented

- Android app: `io.github.mani360.securedevicebridge`, Compose and Material 3.
- Device contract with observable states and a fake adapter.
- Dashboard with connect, disconnect, synthetic capture, timeout, and detach simulation.
- Reconnect and stale callback protection; coroutine cancellation handling.
- Android Keystore P-256 / SHA256withECDSA signing, verification, tamper rejection, protection reporting, and demo key deletion.
- JVM device lifecycle tests and Android Keystore instrumentation tests.
- GitHub Actions for unit tests, Android lint, a debug build, and Keystore tests on API 29 and API 35 emulators.

No employer code, commercial device SDKs, credentials, or real biometric data are included. The sample value is a fixed synthetic string, not a biometric template.

## Run locally

Use JDK 17 or 21 and an Android SDK with platform 35 and Build Tools 34.0.0. Set `ANDROID_HOME` or configure `sdk.dir` in an untracked `local.properties`. Open this directory in Android Studio or run:

```sh
./gradlew :test-support:fake-device:test :app:lintDebug :app:assembleDebug
./gradlew :app:installDebug
# Requires a connected emulator or device:
./gradlew :data:security:connectedDebugAndroidTest
```

Installation requires an emulator or device running Android 8.0 / API 26 or later. The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk`.

The pinned baseline is AGP 8.6.1, Gradle 8.7, Kotlin and Compose compiler 2.0.21, and compile/target SDK 35. This is a reproducible starter baseline, not a claim to use the latest SDK or meet future store submission requirements.

## Try the demo

1. Connect, then capture a synthetic sample.
2. Simulate capture timeout; reconnect to recover.
3. Start a capture and simulate detach or disconnect before it completes.
4. Reconnect and capture again. An old operation must not overwrite the new session.
5. Select **Create key & sign demo**. Check original verification, rejection of a changed sample, and the reported key protection.
6. Select **Delete demo key**, then run signing again to create a new identity.

The signing demo uses a separate fixed message; it does not certify a device capture. Keys do not require biometric/user authentication. Verification establishes consistency with the current local key, not a trusted person, hardware attestation, or a certificate chain.

## Structure

```text
app/                    Compose dashboard and app wiring
core/{common,logging}/  Reserved shared boundaries
domain/device/          Active Kotlin device contract
domain/security/        Active signing contract
domain/biometric/       Reserved biometric contracts
data/security/          Active Android Keystore adapter
data/{device,biometric}/ Reserved hardware/capture adapters
feature/{dashboard,device,capture,security,diagnostics}/  Reserved UI boundaries
test-support/fake-device/  Active fake adapter and JVM tests
docs/                   Architecture, lifecycle, failures, security, testing
.github/workflows/      Build and test definition
```

Active Gradle modules: `app`, `domain:device`, `domain:security`, `data:security`, and `test-support:fake-device`. Reserved folders contain scope notes; they are not implemented features.

## Design notes

- [Architecture](docs/architecture.md)
- [Device state machine](docs/device-state-machine.md)
- [Failure handling](docs/failure-handling.md)
- [Security and planned PKI work](docs/security.md)
- [Testing strategy](docs/testing-strategy.md)
- [Contributing](CONTRIBUTING.md) · [Security reporting](SECURITY.md)

## Next milestones

- Add authentication-gated signing and explicit handling for invalidated keys.
- Define certificate validation policy before adding PKI workflows.
- Extract feature modules when boundaries justify separate builds.
- Introduce a public, redistributable physical device adapter only when licensing permits.

## License

[Apache License 2.0](LICENSE). Third-party dependencies and the upstream Gradle wrapper retain their own licenses. See [NOTICE](NOTICE).
