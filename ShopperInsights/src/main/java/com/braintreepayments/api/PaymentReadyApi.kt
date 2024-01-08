package com.braintreepayments.api

import PaymentMethodDetails
import PaymentMethods
import ShopperInsightApiResult
import org.json.JSONObject

// TODO: Implementation, documentation and interface.
internal class PaymentReadyApi {
    fun processRequest(request: ShopperInsightsApiRequest): ShopperInsightApiResult {

        request.toJson()

        // TODO: Network call

        // Hardcoded result
        return ShopperInsightApiResult(
            eligible_methods = PaymentMethods(
                paypal = PaymentMethodDetails(
                    can_be_vaulted = true,
                    eligible_in_paypal_network = true,
                    recommended = true,
                    recommended_priority = 1
                ),
                venmo = PaymentMethodDetails(
                    can_be_vaulted = true,
                    eligible_in_paypal_network = true,
                    recommended = true,
                    recommended_priority = 1
                )
            )
        )

    }

    private fun ShopperInsightsApiRequest.toJson(): String {
        return JSONObject().apply {
            put(KEY_CUSTOMER, JSONObject().apply {
                putOpt(KEY_EMAIL, request.email)
                put(KEY_COUNTRY_CODE, countryCode)
                request.phone?.let {
                    put(KEY_PHONE, JSONObject().apply {
                        put(KEY_COUNTRY_CODE, it.countryCode)
                        put(KEY_NATIONAL_NUMBER, it.nationalNumber)
                    })
                }
            })
            put(KEY_PURCHASE_UNITS, JSONObject().apply {
                put(KEY_PAYEE, JSONObject().apply {
                    put(KEY_MERCHANT_ID, merchantId)
                })
                put(KEY_AMOUNT, JSONObject().apply {
                    put(KEY_CURRENCY_CODE, currencyCode)
                })
            })
        }.toString()
    }

    companion object {
        internal const val KEY_COUNTRY_CODE = "country_code"
        internal const val KEY_NATIONAL_NUMBER = "national_number"
        internal const val KEY_CUSTOMER = "customer"
        internal const val KEY_EMAIL = "email"
        internal const val KEY_PHONE = "phone"
        internal const val KEY_PURCHASE_UNITS = "purchase_units"
        internal const val KEY_PAYEE = "payee"
        internal const val KEY_AMOUNT = "amount"
        internal const val KEY_MERCHANT_ID = "merchant_id"
        internal const val KEY_CURRENCY_CODE = "currency_code"
    }
}
