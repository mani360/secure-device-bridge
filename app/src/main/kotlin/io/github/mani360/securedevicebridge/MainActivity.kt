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

private fun DeviceState.label(): String = when (this) {
    DeviceState.Disconnected -> "Disconnected"
    DeviceState.Ready -> "Ready to capture"
    DeviceState.Capturing -> "Capturing…"
    is DeviceState.Captured -> "Sample captured"
    is DeviceState.Failed -> when (reason) {
        FailureReason.TIMEOUT -> "Capture timed out"
        FailureReason.DETACHED -> "Device detached"
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme(colorScheme = lightColorScheme(
                primary = androidx.compose.ui.graphics.Color(0xFF006A68),
                secondary = androidx.compose.ui.graphics.Color(0xFF435F75),
                background = androidx.compose.ui.graphics.Color(0xFFF4F7F8),
                surface = androidx.compose.ui.graphics.Color.White
            )) {
                val model: DashboardViewModel = viewModel()
                val state by model.state.collectAsStateWithLifecycle()
                val signing by model.signing.collectAsStateWithLifecycle()
                val canCapture = state == DeviceState.Ready || state is DeviceState.Captured
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    Column(
                        Modifier.safeDrawingPadding().verticalScroll(rememberScrollState()).padding(20.dp),
                        verticalArrangement = Arrangement.spacedBy(18.dp)
                    ) {
                        Text("SecureDeviceBridge", style = MaterialTheme.typography.headlineSmall)
                        Text("Android engineering · Device integration", style = MaterialTheme.typography.bodyMedium)
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer)) {
                            Column(Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                                Text("SYNTHETIC DATA ONLY", style = MaterialTheme.typography.labelLarge)
                                Text("Explore device recovery and local signing without biometric hardware.", style = MaterialTheme.typography.bodyMedium)
                            }
                        }
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("01 / Device sandbox", style = MaterialTheme.typography.titleLarge)
                                Text(state.label(), style = MaterialTheme.typography.titleMedium, color = if (state is DeviceState.Failed) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.primary)
                                Text(when (state) {
                                    DeviceState.Disconnected -> "Connect the simulated device to begin."
                                    DeviceState.Ready -> "Ready for a synthetic capture or failure scenario."
                                    DeviceState.Capturing -> "A simulated operation is in progress."
                                    is DeviceState.Captured -> "Synthetic sample received. No biometric data was collected."
                                    is DeviceState.Failed -> "Reconnect to start a new device session."
                                }, style = MaterialTheme.typography.bodyMedium)
                                Button(onClick = model::connect, enabled = state == DeviceState.Disconnected || state is DeviceState.Failed, modifier = Modifier.fillMaxWidth()) {
                                    Text(if (state is DeviceState.Failed) "Reconnect device" else "Connect device")
                                }
                                Button(onClick = { model.capture(CaptureScenario.SUCCESS) }, enabled = canCapture, modifier = Modifier.fillMaxWidth()) { Text("Capture sample") }
                                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                    OutlinedButton(onClick = { model.capture(CaptureScenario.TIMEOUT) }, enabled = canCapture, modifier = Modifier.weight(1f)) { Text("Timeout") }
                                    OutlinedButton(onClick = model::detach, enabled = state != DeviceState.Disconnected, modifier = Modifier.weight(1f)) { Text("Detach") }
                                }
                                TextButton(onClick = model::disconnect, enabled = state != DeviceState.Disconnected) { Text("Disconnect") }
                            }
                        }
                        Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)) {
                            Column(Modifier.padding(18.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                                Text("02 / Local signing", style = MaterialTheme.typography.titleLarge)
                                Text("Sign a separate fixed demo message with Android Keystore, then verify it and reject a changed message.", style = MaterialTheme.typography.bodyMedium)
                                Button(onClick = model::signDemo, enabled = !signing.busy, modifier = Modifier.fillMaxWidth()) { Text("Create key & sign demo") }
                                OutlinedButton(onClick = model::deleteSigningKey, enabled = !signing.busy, modifier = Modifier.fillMaxWidth()) { Text("Delete demo key") }
                                Text(signing.message, style = MaterialTheme.typography.bodyMedium)
                                Text("Demo keys do not require user authentication. Certificate trust and hardware attestation are outside this demo.", style = MaterialTheme.typography.bodySmall)
                            }
                        }
                        Text("Built by Manitosh Bepari · Independent portfolio project", style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
        }
    }
}
