package io.github.mani360.securedevicebridge.security

/** Local demo key protection reported by the platform; not remote attestation. */
enum class KeyProtection { MISSING, SOFTWARE, TRUSTED_ENVIRONMENT, STRONGBOX, HARDWARE_UNSPECIFIED, UNKNOWN }

/** Blocking operations: call from a worker thread. Keys remain in the adapter. */
interface DeviceSigner {
    fun createKey(): KeyProtection
    fun keyProtection(): KeyProtection
    fun sign(payload: ByteArray): ByteArray
    fun verify(payload: ByteArray, signature: ByteArray): Boolean
    fun deleteKey()
}
