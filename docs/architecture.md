# Architecture

## Current design

`app -> domain:device <- test-support:fake-device`, with app wiring the fake implementation into a ViewModel. The domain exposes StateFlow; the dashboard collects state with lifecycle awareness. A ViewModel owns the device across activity recreation and launches capture in viewModelScope.

The fake adapter serializes state mutations and uses a session generation to discard completions after detach, disconnect, or reconnect. It deliberately has no Android dependency.

## Planned boundaries

The reserved core, data, biometric, security, and feature folders document intended responsibilities. They are not Gradle modules yet. Extract them when a concrete second consumer or adapter warrants the boundary. The fake adapter is intentionally included in this demonstration app.

## Open decisions

- Real adapter threading and permission ownership.
- Capture result types and cancellation contract for vendor SDKs.
- Signing authorization and key lifecycle policy.
