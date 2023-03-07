package com.braintreepayments.api

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class PaymentMethodNonceUnitTest {

    @Test
    fun constructor() {
        val nonce = PaymentMethodNonce("fake-nonce", true)
        assertEquals("fake-nonce", nonce.string)
        assertTrue(nonce.isDefault)
    }
}