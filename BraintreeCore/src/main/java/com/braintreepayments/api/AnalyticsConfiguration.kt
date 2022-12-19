package com.braintreepayments.api

import android.text.TextUtils
import org.json.JSONObject

/**
 * Contains configuration for Braintree analytics calls
 * @property url url of the Braintree analytics service.
 */
internal class AnalyticsConfiguration private constructor(val url: String?) {

    /**
     * @return `true` if analytics are enabled, `false` otherwise.
     */
    val isEnabled: Boolean = !TextUtils.isEmpty(url)

    companion object {
        private const val URL_KEY = "url"

        /**
         * Parse an [AnalyticsConfiguration] from json.
         *
         * @param json The [JSONObject] to parse.
         * @return An [AnalyticsConfiguration] instance with data that was able to be parsed from
         * the [JSONObject].
         */
        @JvmStatic
        fun fromJson(json: JSONObject?): AnalyticsConfiguration {
            val url = Json.optString(json, URL_KEY, null)
            return AnalyticsConfiguration(url)
        }
    }
}