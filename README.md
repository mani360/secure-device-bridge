# SecureDeviceBridge

Android/Kotlin portfolio reference project for device integration, explicit state management, and synthetic biometric capture. Built independently by Manitosh Bepari.

**Status:** initial implementation. This is an educational portfolio project; physical hardware, real biometrics, PKI, and Keystore signing are not implemented.

## Implemented

- Android app: `io.github.mani360.securedevicebridge`, Compose and Material 3.
- Device contract with observable states and a fake adapter.
- Dashboard with connect, disconnect, synthetic capture, timeout, and detach simulation.
- Reconnect and stale callback protection; coroutine cancellation handling.
- JVM tests for state transitions, failure recovery, cancellation, and overlapping operations.
- GitHub Actions definition for unit tests, Android lint, and a debug build. Run status is available after publication and execution.

No employer code, commercial device SDKs, credentials, or real biometric data are included. The sample value is a fixed synthetic string, not a biometric template.

## Run locally

Use JDK 17 or 21 and an Android SDK with platform 35 and Build Tools 34.0.0. Set `ANDROID_HOME` or configure `sdk.dir` in an untracked `local.properties`. Open this directory in Android Studio or run:

```sh
./gradlew :test-support:fake-device:test :app:lintDebug :app:assembleDebug
./gradlew :app:installDebug
```

Installation requires an emulator or device running Android 8.0 / API 26 or later. The debug APK is produced at `app/build/outputs/apk/debug/app-debug.apk`.

The pinned baseline is AGP 8.6.1, Gradle 8.7, Kotlin and Compose compiler 2.0.21, and compile/target SDK 35. This is a reproducible starter baseline, not a claim to use the latest SDK or meet future store submission requirements.

## Try the demo

1. Connect, then capture a synthetic sample.
2. Simulate capture timeout; reconnect to recover.
3. Start a capture and simulate detach or disconnect before it completes.
4. Reconnect and capture again. An old operation must not overwrite the new session.

## Structure

```text
app/                    Compose dashboard and app wiring
core/{common,logging}/  Reserved shared boundaries
domain/device/          Active Kotlin device contract
domain/{biometric,security}/  Reserved contracts
data/{device,biometric,security}/  Reserved adapters
feature/{dashboard,device,capture,security,diagnostics}/  Reserved UI boundaries
test-support/fake-device/  Active fake adapter and JVM tests
docs/                   Architecture, lifecycle, failures, security, testing
.github/workflows/      Build and test definition
```

Only `app`, `domain:device`, and `test-support:fake-device` are active Gradle modules. Reserved folders contain scope notes; they are not implemented features.

## Design notes

- [Architecture](docs/architecture.md)
- [Device state machine](docs/device-state-machine.md)
- [Failure handling](docs/failure-handling.md)
- [Security and planned PKI work](docs/security.md)
- [Testing strategy](docs/testing-strategy.md)
- [Contributing](CONTRIBUTING.md) · [Security reporting](SECURITY.md)

## Next milestones

- Add Android Keystore signing and verification with instrumentation tests.
- Define certificate validation policy before adding PKI workflows.
- Extract feature modules when boundaries justify separate builds.
- Introduce a public, redistributable physical device adapter only when licensing permits.

## License

[Apache License 2.0](LICENSE). Third-party dependencies and the upstream Gradle wrapper retain their own licenses. See [NOTICE](NOTICE).
