# Security

## Current scope

Capture produces synthetic strings only. No network, USB, camera, or biometric permissions are requested. No captures are persisted. The signing demonstration creates a local Android Keystore key; private key material is never exported by application code. No employer implementation, vendor SDK, production credentials, or customer information is included.

## Implemented signing demonstration

- P-256 (`secp256r1`) with `SHA256withECDSA`.
- Explicit create-or-reuse operation with alias `secure-device-bridge.demo.signing.v1`.
- Signing requires an existing key; deletion does not silently recreate it.
- Verify with the current alias's public key. A different message or replacement key rejects the old signature. Malformed signature encodings return false; a missing verification key fails explicitly.
- Report KeyInfo security level on API 31+ and software/hardware status on older APIs. Hardware protection is never assumed. No StrongBox requirement or attestation is requested.
- Blocking cryptographic work runs on Dispatchers.IO from the dashboard. The adapter serializes operations within this app process.
- Delete removes only the adapter's demo alias. No key rotation/migration scheme is implemented.

The signed bytes are the UTF-8 encoding of `SecureDeviceBridge demo v1: SYNTHETIC-SAMPLE`. This separate fixed demo message is not a live capture record, freshness proof, transaction authorization, or protocol with replay protection. The dashboard displays verification outcomes and protection metadata, not private material or signatures.

## Authentication policy and limitations

Demo keys deliberately set user authentication to false so the flow can run unattended on an emulator. This is not a biometric authentication workflow. Device lock changes and biometric enrollment invalidation for authentication-bound keys are not implemented or tested. Platform errors surface as a generic failure in the dashboard; no automatic key replacement occurs after a signing failure.

Key protection is local platform metadata, not independently verified hardware attestation. Tests check that the provider does not expose the private key encoding; they do not establish resistance to device compromise. Deleting a key changes the verification identity: old signatures cannot be checked through this alias after deletion without separately retaining the old public key.

Signature verification alone does not establish certificate trust. The provider's local certificate supplies a public key; no trust chain, identity certification, revocation, certificate validity policy, or remote trust is claimed.

## Next security work

- Define user authorization and authentication-bound key policy.
- Define typed failures for invalidation, unavailable providers, and user cancellation.
- Specify payload structure, domain separation, freshness, and key identity before signing capture records.
- Define trust anchors, chain validation, algorithm policy, validity, and revocation before PKI workflows.

Reference: [Android Keystore documentation](https://developer.android.com/privacy-and-security/keystore).
