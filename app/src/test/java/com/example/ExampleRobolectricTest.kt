package com.example

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("FF India Pro", appName)
  }

  @Test
  fun `single html asset contains all required gaming panel modules`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val html = context.assets.open("ff_india_pro_panel.html").bufferedReader().use { it.readText() }
    val requiredMarkers =
      listOf(
        "Sensitivity Sliders",
        "Headshot Tracker",
        "Aim Practice Trainer",
        "HUD Layout Planner",
        "DPI Guide",
        "Weapon Arsenal",
        "Live Crosshair Studio",
        "Drag Technique Visualizer",
        "India &amp; Esports Reference Presets",
        "localStorage",
        "confirmResetAll",
      )
    for (marker in requiredMarkers) {
      assertTrue("Missing marker: $marker", html.contains(marker))
    }
  }
}
