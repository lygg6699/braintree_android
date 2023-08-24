package com.braintreepayments.api

import android.os.Parcel
import android.os.Parcelable
import org.json.JSONObject
import java.util.Locale

/**
 * Information pertaining to the regulatory environment for a credit card if authentication insight
 * is requested during tokenization.
 */
class AuthenticationInsight : Parcelable {
    /**
     *
     * @return The regulation environment for the associated nonce to help determine the need
     * for 3D Secure.
     *
     * @see [Documentation](https://developer.paypal.com/braintree/docs/guides/3d-secure/advanced-options/android/v4.authentication-insight)
     * for possible values.
     */
    val regulationEnvironment: String

    internal constructor(regulationEnvironment: String) {
        this.regulationEnvironment = regulationEnvironment
    }

    override fun describeContents(): Int {
        return 0
    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(regulationEnvironment)
    }

    private constructor(parcel: Parcel) {
        regulationEnvironment = parcel.readString()!!
    }

    companion object {
        private const val GRAPHQL_REGULATION_ENVIRONMENT_KEY =
            "customerAuthenticationRegulationEnvironment"
        private const val REST_REGULATION_ENVIRONMENT_KEY = "regulationEnvironment"
        @JvmStatic
        fun fromJson(json: JSONObject?): AuthenticationInsight? {
            if (json == null) {
                return null
            }
            var regulationEnv: String
            regulationEnv =
                if (json.has(GRAPHQL_REGULATION_ENVIRONMENT_KEY)) {
                    Json.optString(
                        json,
                        GRAPHQL_REGULATION_ENVIRONMENT_KEY,
                        ""
                    )
                } else {
                    Json.optString(
                        json,
                        REST_REGULATION_ENVIRONMENT_KEY,
                        ""
                    )
                }
            if ("psdtwo".equals(regulationEnv, ignoreCase = true)) {
                regulationEnv = "psd2"
            }
            regulationEnv = regulationEnv.lowercase(Locale.getDefault())
            return AuthenticationInsight(regulationEnv)
        }


        @JvmField
        val CREATOR: Parcelable.Creator<AuthenticationInsight> =
            object : Parcelable.Creator<AuthenticationInsight> {
                override fun createFromParcel(source: Parcel): AuthenticationInsight {
                    return AuthenticationInsight(source)
                }

                override fun newArray(size: Int): Array<AuthenticationInsight?> {
                    return arrayOfNulls(size)
                }
            }
    }
}