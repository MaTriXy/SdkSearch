package com.jakewharton.sdksearch.reference

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNull

class ExtensionsTest {
  private val reference = AndroidReference("https://git.example.com/", "https://dac.example.com/")

  @Test fun sourceUrl() {
    val url = reference.sourceUrl("https://dac.example.com/reference/android/widget/Toolbar")
    assertEquals("https://git.example.com/platform/frameworks/base/+/refs/heads/master/core/java/android/widget/Toolbar.java", url)
  }

  @Test fun sourceUrlNested() {
    val url = reference.sourceUrl("https://dac.example.com/reference/android/widget/Toolbar.Nested")
    assertEquals("https://git.example.com/platform/frameworks/base/+/refs/heads/master/core/java/android/widget/Toolbar.java", url)
  }

  @Test fun sourceUrlWithQueryAndFragment() {
    val url = reference.sourceUrl("https://dac.example.com/reference/android/widget/Toolbar?ping=pong#whatup")
    assertEquals("https://git.example.com/platform/frameworks/base/+/refs/heads/master/core/java/android/widget/Toolbar.java", url)
  }

  @Test fun sourceUrlWrongDomain() {
    val url = reference.sourceUrl("https://dac.example2.com/reference/android/view/View")
    assertNull(url)
  }

  @Test fun sourceUrlWrongPath() {
    val url = reference.sourceUrl("https://dac.example.com/reference2/android/view/View")
    assertNull(url)
  }

  @Test fun sourceUrlUnknownType() {
    val url = reference.sourceUrl("https://dac.example.com/reference/missing/entry/NotThere")
    assertNull(url)
  }
}
