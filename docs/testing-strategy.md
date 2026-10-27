# Testing strategy

## Current automated coverage

JVM tests use coroutine virtual time to cover connect/disconnect, synthetic success, timeout and reconnect, detach/reconnect while capture is running, disconnect during capture, cancellation, disconnected capture rejection, and overlapping capture rejection.

Run `./gradlew :test-support:fake-device:test :app:lintDebug :data:security:lintDebug :app:assembleDebug`. GitHub Actions runs the same tasks and uploads unit test reports and a debug APK. A separate job runs `:data:security:connectedDebugAndroidTest` on API 29 and API 35 emulators. It uploads instrumentation reports. Physical hardware tests are not included.

## Keystore integration coverage

Eight Android instrumentation tests use unique test-only aliases and delete them after each test. Coverage includes round-trip verification, changed payload rejection, malformed signature rejection, key reuse across adapter instances, deletion and missing-key errors, replacement identity rejection, non-exportable private key encoding, and protection reporting. Run `./gradlew :data:security:connectedDebugAndroidTest` with an emulator or device attached.

API 29 exercises legacy protection reporting; API 35 exercises the API 31+ security-level branch. Authentication-bound key invalidation and hardware-backed protection are not asserted by these tests.

## Manual checks

Install on API 26+; exercise success, timeout, detach, reconnect, and disconnect. Rotate the device during capture and confirm state survives activity recreation. Check large fonts and screen reader labels.

## Planned coverage

Add Compose UI tests, process recreation scenarios, real adapter contract tests, and authentication-bound key invalidation. Passing JVM tests cannot establish hardware compatibility or security assurance.
