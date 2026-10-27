package io.github.mani360.securedevicebridge

import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.test.platform.app.InstrumentationRegistry
import android.graphics.Bitmap
import java.io.File
import org.junit.Rule
import org.junit.Test

class DashboardSmokeTest {
    @get:Rule val compose = createAndroidComposeRule<MainActivity>()

    private fun awaitText(text: String) {
        compose.waitUntil(timeoutMillis = 15_000) {
            compose.onAllNodesWithText(text, substring = true).fetchSemanticsNodes().isNotEmpty()
        }
    }
    private fun screenshot(name: String) {
        compose.waitForIdle()
        val directory = File(InstrumentationRegistry.getInstrumentation().targetContext.getExternalFilesDir(null), "screenshots").apply { mkdirs() }
        File(directory, name).outputStream().use {
            check(compose.onRoot().captureToImage().asAndroidBitmap().compress(Bitmap.CompressFormat.PNG, 100, it))
        }
    }

    @Test fun deviceCaptureAndTimeoutRecover() {
        compose.onNodeWithText("Connect device").performClick()
        compose.onNodeWithText("Capture sample").performClick()
        awaitText("Sample captured")
        screenshot("device-capture.png")
        compose.onNodeWithText("Timeout", substring = false).performClick()
        awaitText("Capture timed out")
        compose.onNodeWithText("Reconnect device").performClick()
        compose.onNodeWithText("Ready to capture").assertExists()
        compose.onNodeWithText("Detach", substring = false).performClick()
        compose.onNodeWithText("Device detached").assertExists()
        compose.onNodeWithText("Disconnect", substring = false).performClick()
        compose.onNodeWithText("Disconnected", substring = false).assertExists()
    }

    @Test fun signingVerifiesTamperAndDeletesKey() {
        compose.onNodeWithText("Create key & sign demo").performScrollTo().performClick()
        awaitText("Original verified: true; changed sample rejected: true.")
        compose.onNodeWithText("Original verified: true; changed sample rejected: true.", substring = true).performScrollTo()
        screenshot("keystore-signing.png")
        compose.onNodeWithText("Delete demo key").performScrollTo().performClick()
        awaitText("Demo key deleted.")
    }
}
