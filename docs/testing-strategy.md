# Testing strategy

## Current automated coverage

JVM tests use coroutine virtual time to cover connect/disconnect, synthetic success, timeout and reconnect, detach/reconnect while capture is running, disconnect during capture, cancellation, disconnected capture rejection, and overlapping capture rejection.

Run `./gradlew :test-support:fake-device:test :app:lintDebug :app:assembleDebug`. GitHub Actions runs the same tasks and uploads unit test reports and a debug APK. It does not run emulator or physical hardware tests.

## Manual checks

Install on API 26+; exercise success, timeout, detach, reconnect, and disconnect. Rotate the device during capture and confirm state survives activity recreation. Check large fonts and screen reader labels.

## Planned coverage

Add Compose UI tests, process recreation scenarios, real adapter contract tests, and Android instrumentation tests for Keystore signing/verification once those components exist. Passing JVM tests cannot establish hardware compatibility or security assurance.
