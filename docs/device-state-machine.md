# Device state machine

| Starting state | Event | Result |
| --- | --- | --- |
| Disconnected / Failed | Connect | Ready |
| Ready / Captured | Capture | Capturing |
| Capturing | Synthetic success | Captured |
| Capturing | Simulated timeout | Failed(TIMEOUT) |
| Connected state | Detach | Failed(DETACHED) |
| Any state | Disconnect | Disconnected |
| Capturing | Coroutine cancellation | Ready, if session is still current |

Capture in Disconnected, Failed, or Capturing throws IllegalStateException. Connect is ignored during capture. A session generation invalidates outstanding completions after disconnect, detach, or reconnect. A new capture may start from Captured. Failures require reconnect before another capture.

The 500 ms delay and timeout outcome are simulations, not a physical device deadline or biometric quality evaluation.
