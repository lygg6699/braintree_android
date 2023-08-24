package com.braintreepayments.api

import android.os.Parcel
import com.braintreepayments.api.AuthenticationInsight.Companion.fromJson
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import org.json.JSONException
import org.json.JSONObject
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class AuthenticationInsightUnitTest {
    @Test
    @Throws(JSONException::class)
    fun fromJson_successfullyParsesPSDTWO() {
        val response = JSONObject()
            .put("customerAuthenticationRegulationEnvironment", "psdtwo")
        val authenticationInsight = fromJson(response)
        assertEquals("psd2", authenticationInsight!!.regulationEnvironment)
    }

    @Test
    @Throws(JSONException::class)
    fun fromJson_onUnknownRegulationEnvironment_returnsUnknown() {
        val response = JSONObject()
            .put("customerAuthenticationRegulationEnvironment", "FaKeVaLuE")
        val authenticationInsight = fromJson(response)
        assertEquals("fakevalue", authenticationInsight!!.regulationEnvironment)
    }

    @Test
    @Throws(JSONException::class)
    fun fromJson_withRegulationEnvironmentKey_returnsValue() {
        val response = JSONObject()
            .put("regulationEnvironment", "UNREGULATED")
        val authenticationInsight = fromJson(response)
        assertEquals("unregulated", authenticationInsight!!.regulationEnvironment)
    }

    @Test
    fun fromJson_onNullJsonObject_returnsNull() {
        assertNull(fromJson(null))
    }

    @Test
    @Throws(JSONException::class)
    fun parcelsCorrectly() {
        val authInsight = fromJson(
            JSONObject()
                .put("regulationEnvironment", "psdtwo")
        )
        val parcel = Parcel.obtain()
        authInsight!!.writeToParcel(parcel, 0)
        parcel.setDataPosition(0)
        val parceledInsight = AuthenticationInsight.CREATOR.createFromParcel(parcel)
        assertEquals(
            authInsight.regulationEnvironment,
            parceledInsight.regulationEnvironment
        )
    }
}