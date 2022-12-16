package com.braintreepayments.api

import com.braintreepayments.api.AnalyticsConfiguration.Companion.fromJson
import org.json.JSONException
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AnalyticsConfigurationUnitTest {

    @Test
    @Throws(JSONException::class)
    fun fromJson_parsesFullInput() {
        val input = JSONObject()
            .put("url", "https://example.com/analytics")
        val sut = fromJson(input)
        assertTrue(sut.isEnabled)
        assertEquals("https://example.com/analytics", sut.url)
    }

    @Test
    fun fromJson_whenInputNull_returnsConfigWithDefaultValues() {
        val sut = fromJson(null)
        assertFalse(sut.isEnabled)
        assertNull(sut.url)
    }

    @Test
    fun fromJson_whenInputEmpty_returnsConfigWithDefaultValues() {
        val sut = fromJson(JSONObject())
        assertFalse(sut.isEnabled)
        assertNull(sut.url)
    }
}