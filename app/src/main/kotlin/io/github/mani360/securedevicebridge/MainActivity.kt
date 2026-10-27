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

class DashboardViewModel : ViewModel() {
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
                        Text("Keystore signing and physical device adapters are planned extensions.")
                    }
                }
            }
        }
    }
}
