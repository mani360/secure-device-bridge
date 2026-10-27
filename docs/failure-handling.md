# Failure handling

## Implemented

- Timeout: fake capture emits Failed(TIMEOUT); reconnect restores Ready.
- Detach: emits Failed(DETACHED), invalidating any outstanding operation.
- Disconnect: explicitly returns to Disconnected and rejects stale completion.
- Cancellation: restores Ready only for the current session, then rethrows cancellation.
- Invalid or overlapping capture: rejected before state mutation.

No automatic retries are implemented. Recovery is explicit in the dashboard.

## Future adapter requirements

Document permission denial, SDK exceptions, native errors, transport timeouts, cancellation acknowledgement, and bounded retry policies before adding hardware. Keep device identifiers and biometric payloads out of logs.
