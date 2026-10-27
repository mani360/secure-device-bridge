package io.github.mani360.securedevicebridge.fake

import io.github.mani360.securedevicebridge.device.*
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.*
import org.junit.Assert.*
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class FakeExternalDeviceTest {
    @Test fun connectsAndDisconnects() {
        val device = FakeExternalDevice()
        assertEquals(DeviceState.Disconnected, device.state.value)
        device.connect()
        assertEquals(DeviceState.Ready, device.state.value)
        device.disconnect()
        assertEquals(DeviceState.Disconnected, device.state.value)
    }
    @Test fun capturesSyntheticSample() = runTest {
        val device = FakeExternalDevice()
        device.connect()
        device.capture()
        assertEquals(DeviceState.Captured("SYNTHETIC-SAMPLE"), device.state.value)
    }
    @Test fun timeoutRequiresReconnect() = runTest {
        val device = FakeExternalDevice()
        device.connect()
        device.capture(CaptureScenario.TIMEOUT)
        assertEquals(DeviceState.Failed(FailureReason.TIMEOUT), device.state.value)
        device.connect()
        assertEquals(DeviceState.Ready, device.state.value)
    }
    @Test fun detachAndReconnectRejectsStaleCompletion() = runTest {
        val device = FakeExternalDevice()
        device.connect()
        val operation = launch { device.capture() }
        runCurrent()
        assertEquals(DeviceState.Capturing, device.state.value)
        device.detach()
        assertEquals(DeviceState.Failed(FailureReason.DETACHED), device.state.value)
        device.connect()
        operation.join()
        assertEquals(DeviceState.Ready, device.state.value)
    }
    @Test fun disconnectDuringCaptureRemainsDisconnected() = runTest {
        val device = FakeExternalDevice()
        device.connect()
        val operation = launch { device.capture() }
        runCurrent()
        device.disconnect()
        operation.join()
        assertEquals(DeviceState.Disconnected, device.state.value)
    }
    @Test fun cancellationRestoresReady() = runTest {
        val device = FakeExternalDevice()
        device.connect()
        val operation = launch { device.capture() }
        runCurrent()
        operation.cancel()
        operation.join()
        assertEquals(DeviceState.Ready, device.state.value)
    }
    @Test fun rejectsCaptureWhenDisconnected() = runTest {
        val device = FakeExternalDevice()
        try { device.capture(); fail("Disconnected capture must fail") }
        catch (expected: IllegalStateException) { assertEquals(DeviceState.Disconnected, device.state.value) }
    }
    @Test fun rejectsOverlappingCapture() = runTest {
        val device = FakeExternalDevice()
        device.connect()
        val operation = launch { device.capture() }
        runCurrent()
        try { device.capture(); fail("Overlapping capture must fail") }
        catch (expected: IllegalStateException) { assertEquals(DeviceState.Capturing, device.state.value) }
        operation.join()
        assertTrue(device.state.value is DeviceState.Captured)
    }
}
