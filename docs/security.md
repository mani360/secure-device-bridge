# Security

## Current scope

Only synthetic strings are produced. The app requests no network, USB, camera, or biometric permissions, stores no captures, and implements no cryptographic operations. No employer implementation, vendor SDK, certificate, key, or customer information is included.

## Planned Keystore demonstration

Define a signing interface and implement it with Android Keystore. Cover signing and verification on an emulator or device, key deletion, invalidated keys, and user authentication policy. Hardware backing must be detected and reported; it must never be assumed. StrongBox availability is device dependent.

Signature verification alone does not establish certificate trust. Define trust anchors, validity checks, algorithm policy, chain validation, and revocation behavior before claiming PKI support. Never put private keys or biometric templates in diagnostics or source control.

## Open questions

- Which operations need user authentication?
- How are aliases, rotation, deletion, and failures handled?
- What data is signed, and how is its meaning bound to the signature?
