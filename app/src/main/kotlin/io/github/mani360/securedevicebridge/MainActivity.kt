package io.github.mani360.securedevicebridge

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import io.github.mani360.securedevicebridge.device.*
import io.github.mani360.securedevicebridge.fake.FakeExternalDevice
import kotlinx.coroutines.launch
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import io.github.mani360.securedevicebridge.security.DeviceSigner
import io.github.mani360.securedevicebridge.security.android.AndroidKeystoreSigner

data class SigningState(val busy: Boolean = false, val message: String = "No signing operation yet")

class DashboardViewModel : ViewModel() {
    private val signer: DeviceSigner = AndroidKeystoreSigner()
    private val mutableSigning = MutableStateFlow(SigningState())
    val signing = mutableSigning.asStateFlow()
    fun signDemo() = signingOperation {
        val protection = signer.createKey()
        val payload = "SecureDeviceBridge demo v1: SYNTHETIC-SAMPLE".toByteArray(Charsets.UTF_8)
        val signature = signer.sign(payload)
        val valid = signer.verify(payload, signature)
        val changedRejected = !signer.verify(payload + 1.toByte(), signature)
        "Original verified: $valid; changed sample rejected: $changedRejected. Key protection: $protection"
    }
    fun deleteSigningKey() = signingOperation {
        signer.deleteKey()
        "Demo key deleted. A new key will be created on the next signing demo."
    }
    private fun signingOperation(operation: () -> String) {
        if (mutableSigning.value.busy) return
        mutableSigning.value = SigningState(busy = true, message = "Working…")
        viewModelScope.launch {
            try {
                mutableSigning.value = SigningState(message = withContext(Dispatchers.IO) { operation() })
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                mutableSigning.value = SigningState(message = "Keystore operation failed. Retry or delete the demo key to reset it.")
            }
        }
    }
    private val device: ExternalDevice = FakeExternalDevice()
    val state = device.state
    fun connect() = device.connect()
    fun disconnect() = device.disconnect()
    fun detach() = device.detach()
    fun capture(scenario: CaptureScenario) {
        viewModelScope.launch {
            if (state.value == DeviceState.Ready || state.value is DeviceState.Captured) device.capture(scenario)
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val model: DashboardViewModel = viewModel()
                val state by model.state.collectAsStateWithLifecycle()
                val signing by model.signing.collectAsStateWithLifecycle()
                val canCapture = state == DeviceState.Ready || state is DeviceState.Captured
                Surface(modifier = Modifier.fillMaxSize()) {
                    Column(
                        Modifier.safeDrawingPadding().padding(24.dp).verticalScroll(rememberScrollState()),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Text("SecureDeviceBridge", style = MaterialTheme.typography.headlineMedium)
                        Text("Synthetic device demo · No real biometric data")
                        Text("Device state: $state")
                        Button(onClick = model::connect, enabled = state == DeviceState.Disconnected || state is DeviceState.Failed) { Text("Connect / reconnect") }
                        Button(onClick = { model.capture(CaptureScenario.SUCCESS) }, enabled = canCapture) { Text("Capture synthetic sample") }
                        Button(onClick = { model.capture(CaptureScenario.TIMEOUT) }, enabled = canCapture) { Text("Simulate capture timeout") }
                        Button(onClick = model::detach, enabled = state != DeviceState.Disconnected) { Text("Simulate detach") }
                        OutlinedButton(onClick = model::disconnect, enabled = state != DeviceState.Disconnected) { Text("Disconnect") }
                        HorizontalDivider()
                        Text("Signing demo", style = MaterialTheme.typography.titleLarge)
                        Text("Signs a separate fixed synthetic message using a local P-256 key. No biometric authentication is required.")
                        Button(onClick = model::signDemo, enabled = !signing.busy) { Text("Create key & sign demo") }
                        OutlinedButton(onClick = model::deleteSigningKey, enabled = !signing.busy) { Text("Delete demo key") }
                        Text(signing.message)
                        Text("Certificate trust and physical device adapters are planned extensions.")
                    }
                }
            }
        }
    }
}
