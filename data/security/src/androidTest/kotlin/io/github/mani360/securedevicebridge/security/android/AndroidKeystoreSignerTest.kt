package io.github.mani360.securedevicebridge.security.android

import androidx.test.ext.junit.runners.AndroidJUnit4
import io.github.mani360.securedevicebridge.security.KeyProtection
import java.security.KeyStore
import java.util.UUID
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AndroidKeystoreSignerTest {
    private lateinit var alias: String
    private lateinit var signer: AndroidKeystoreSigner
    private val payload = "SecureDeviceBridge demo v1: SYNTHETIC-SAMPLE".toByteArray(Charsets.UTF_8)

    @Before fun setUp() {
        alias = "secure-device-bridge.test.${UUID.randomUUID()}"
        signer = AndroidKeystoreSigner(alias)
    }
    @After fun tearDown() { signer.deleteKey() }

    @Test fun signsAndVerifiesUnchangedPayload() {
        signer.createKey()
        assertTrue(signer.verify(payload, signer.sign(payload)))
    }
    @Test fun rejectsChangedPayload() {
        signer.createKey()
        val signature = signer.sign(payload)
        assertFalse(signer.verify(payload + 1.toByte(), signature))
    }
    @Test fun rejectsMalformedSignature() {
        signer.createKey()
        assertFalse(signer.verify(payload, byteArrayOf(0, 1, 2)))
    }
    @Test fun keyCreationReusesExistingIdentity() {
        signer.createKey()
        val signature = signer.sign(payload)
        signer.createKey()
        assertTrue(AndroidKeystoreSigner(alias).verify(payload, signature))
    }
    @Test fun deletionRemovesKeyAndSigningFails() {
        signer.createKey()
        signer.deleteKey()
        assertEquals(KeyProtection.MISSING, signer.keyProtection())
        try { signer.sign(payload); fail("Missing key must not be silently recreated") }
        catch (expected: IllegalStateException) { /* expected */ }
    }
    @Test fun replacementRejectsOldSignature() {
        signer.createKey()
        val signature = signer.sign(payload)
        signer.deleteKey()
        signer.createKey()
        assertFalse(signer.verify(payload, signature))
    }
    @Test fun privateKeyIsNotExportableAndProtectionIsReported() {
        val protection = signer.createKey()
        assertNotEquals(KeyProtection.MISSING, protection)
        val store = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        assertNull(store.getKey(alias, null).encoded)
    }
    @Test fun missingVerificationKeyFailsExplicitly() {
        try { signer.verify(payload, byteArrayOf()); fail("Missing verification key must be reported") }
        catch (expected: IllegalStateException) { /* expected */ }
    }
}
