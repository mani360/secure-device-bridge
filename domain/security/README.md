# domain/security

Active Kotlin signing contract. The adapter owns a single alias. Operations are blocking and must run on a worker thread. Signing requires an existing key; creation is explicit. Verification uses the current alias's public key, so deleting/replacing that key also replaces its verification identity. Certificate trust and remote identity are outside this contract.
