package io.github.mani360.securedevicebridge.fake

import io.github.mani360.securedevicebridge.device.*
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/** Synthetic device only; no hardware, biometric templates, or network access. */
class FakeExternalDevice(private val latencyMillis: Long = 500) : ExternalDevice {
    init { require(latencyMillis >= 0) }
    private val lock = Any()
    private var generation = 0L
    private val mutableState = MutableStateFlow<DeviceState>(DeviceState.Disconnected)
    override val state = mutableState.asStateFlow()

    override fun connect() = synchronized(lock) {
        if (mutableState.value != DeviceState.Capturing) {
            generation++
            mutableState.value = DeviceState.Ready
        }
    }
    override fun disconnect() = synchronized(lock) {
        generation++
        mutableState.value = DeviceState.Disconnected
    }
    override fun detach() = synchronized(lock) {
        if (mutableState.value != DeviceState.Disconnected) {
            generation++
            mutableState.value = DeviceState.Failed(FailureReason.DETACHED)
        }
    }
    override suspend fun capture(scenario: CaptureScenario) {
        val session = synchronized(lock) {
            check(mutableState.value == DeviceState.Ready || mutableState.value is DeviceState.Captured) {
                "Connect or reconnect before capture"
            }
            mutableState.value = DeviceState.Capturing
            ++generation
        }
        try {
            delay(latencyMillis)
            synchronized(lock) {
                if (session == generation) {
                    mutableState.value = when (scenario) {
                        CaptureScenario.SUCCESS -> DeviceState.Captured("SYNTHETIC-SAMPLE")
                        CaptureScenario.TIMEOUT -> DeviceState.Failed(FailureReason.TIMEOUT)
                    }
                }
            }
        } catch (cancelled: CancellationException) {
            synchronized(lock) {
                if (session == generation) mutableState.value = DeviceState.Ready
            }
            throw cancelled
        }
    }
}
