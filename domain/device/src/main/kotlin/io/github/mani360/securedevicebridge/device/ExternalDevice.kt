package io.github.mani360.securedevicebridge.device

import kotlinx.coroutines.flow.StateFlow

sealed interface DeviceState {
    data object Disconnected : DeviceState
    data object Ready : DeviceState
    data object Capturing : DeviceState
    data class Captured(val sample: String) : DeviceState
    data class Failed(val reason: FailureReason) : DeviceState
}
enum class FailureReason { TIMEOUT, DETACHED }
enum class CaptureScenario { SUCCESS, TIMEOUT }

interface ExternalDevice {
    val state: StateFlow<DeviceState>
    fun connect()
    fun disconnect()
    fun detach()
    suspend fun capture(scenario: CaptureScenario = CaptureScenario.SUCCESS)
}
