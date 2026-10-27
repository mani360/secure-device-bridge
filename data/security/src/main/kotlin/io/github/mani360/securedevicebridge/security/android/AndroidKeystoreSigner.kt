package io.github.mani360.securedevicebridge.security.android

import android.os.Build
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyInfo
import android.security.keystore.KeyProperties
import io.github.mani360.securedevicebridge.security.DeviceSigner
import io.github.mani360.securedevicebridge.security.KeyProtection
import java.security.KeyFactory
import java.security.KeyPairGenerator
import java.security.KeyStore
import java.security.Signature
import java.security.SignatureException
import java.security.spec.ECGenParameterSpec

class AndroidKeystoreSigner(
    private val alias: String = "secure-device-bridge.demo.signing.v1"
) : DeviceSigner {
    init { require(alias.startsWith("secure-device-bridge.")) { "Use a dedicated demo alias" } }

    private fun store() = KeyStore.getInstance(PROVIDER).apply { load(null) }

    override fun createKey(): KeyProtection = synchronized(lock) {
        if (!store().containsAlias(alias)) {
            KeyPairGenerator.getInstance(KeyProperties.KEY_ALGORITHM_EC, PROVIDER).apply {
                initialize(
                    KeyGenParameterSpec.Builder(alias, KeyProperties.PURPOSE_SIGN or KeyProperties.PURPOSE_VERIFY)
                        .setAlgorithmParameterSpec(ECGenParameterSpec("secp256r1"))
                        .setDigests(KeyProperties.DIGEST_SHA256)
                        .setUserAuthenticationRequired(false)
                        .build()
                )
            }.generateKeyPair()
        }
        keyProtection()
    }

    @Suppress("DEPRECATION")
    override fun keyProtection(): KeyProtection = synchronized(lock) {
        val entry = store().getEntry(alias, null) as? KeyStore.PrivateKeyEntry
            ?: return@synchronized KeyProtection.MISSING
        val info = KeyFactory.getInstance(entry.privateKey.algorithm, PROVIDER)
            .getKeySpec(entry.privateKey, KeyInfo::class.java)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            when (info.securityLevel) {
                KeyProperties.SECURITY_LEVEL_SOFTWARE -> KeyProtection.SOFTWARE
                KeyProperties.SECURITY_LEVEL_TRUSTED_ENVIRONMENT -> KeyProtection.TRUSTED_ENVIRONMENT
                KeyProperties.SECURITY_LEVEL_STRONGBOX -> KeyProtection.STRONGBOX
                else -> KeyProtection.UNKNOWN
            }
        } else {
            if (info.isInsideSecureHardware) KeyProtection.HARDWARE_UNSPECIFIED else KeyProtection.SOFTWARE
        }
    }

    override fun sign(payload: ByteArray): ByteArray = synchronized(lock) {
        val entry = store().getEntry(alias, null) as? KeyStore.PrivateKeyEntry
        checkNotNull(entry) { "Create the demo key before signing" }
        Signature.getInstance(ALGORITHM).run {
            initSign(entry.privateKey)
            update(payload)
            sign()
        }
    }

    override fun verify(payload: ByteArray, signature: ByteArray): Boolean = synchronized(lock) {
        val certificate = store().getCertificate(alias)
        checkNotNull(certificate) { "No demo key available for verification" }
        val verifier = Signature.getInstance(ALGORITHM).apply {
            initVerify(certificate.publicKey)
            update(payload)
        }
        try { verifier.verify(signature) } catch (_: SignatureException) { false }
    }

    override fun deleteKey() = synchronized(lock) { store().deleteEntry(alias) }

    private companion object {
        const val PROVIDER = "AndroidKeyStore"
        const val ALGORITHM = "SHA256withECDSA"
        // Serialize lifecycle changes even across adapter instances in this process.
        val lock = Any()
    }
}
