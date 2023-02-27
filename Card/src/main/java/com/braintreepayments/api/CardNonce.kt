package com.braintreepayments.api

import android.os.Parcel
import android.os.Parcelable
import com.braintreepayments.api.AuthenticationInsight
import org.json.JSONException
import org.json.JSONObject

/**
 * [PaymentMethodNonce] representing a credit or debit card.
 * @property cardType Type of this card (e.g. Visa, MasterCard, American Express)
 * @property lastTwo Last two digits of the card, intended for display purposes.
 */
open class CardNonce(
    val cardType: String,
    val lastTwo: String,
    val lastFour: String,
    val threeDSecureInfo: ThreeDSecureInfo,
    val bin: String,
    val binData: BinData,
    val authenticationInsight: AuthenticationInsight,
    val expirationMonth: String,
    val expirationYear: String,
    val cardholderName: String,
    val nonce: String,
    val isDefault: Boolean
) : PaymentMethodNonce(nonce, isDefault) {

    /**
     * @return Last four digits of the card.
     */
    val lastFour: String

    /**
     * @return The 3D Secure info for the current [CardNonce] or
     * `null`
     */
    val threeDSecureInfo: ThreeDSecureInfo

    /**
     * @return BIN of the card.
     */
    val bin: String

    /**
     * @return The BIN data for the card number associated with [CardNonce]
     */
    val binData: BinData

    /**
     * @return [AuthenticationInsight]
     * Details about the regulatory environment and applicable customer authentication regulation
     * for a potential transaction. You may use this to make an informed decision whether to perform
     * 3D Secure authentication.
     */
    val authenticationInsight: AuthenticationInsight?

    /**
     * @return The expiration month of the card.
     */
    val expirationMonth: String

    /**
     * @return The expiration year of the card.
     */
    val expirationYear: String

    /**
     * @return The name of the cardholder.
     */
    val cardholderName: String

//    private constructor(
//        cardType: String,
//        lastTwo: String,
//        lastFour: String,
//        threeDSecureInfo: ThreeDSecureInfo,
//        bin: String,
//        binData: BinData,
//        authenticationInsight: AuthenticationInsight,
//        expirationMonth: String,
//        expirationYear: String,
//        cardholderName: String,
//        nonce: String,
//        isDefault: Boolean
//    ) : super(nonce, isDefault) {
//        this.cardType = cardType
//        this.lastTwo = lastTwo
//        this.lastFour = lastFour
//        this.threeDSecureInfo = threeDSecureInfo
//        this.bin = bin
//        this.binData = binData
//        this.authenticationInsight = authenticationInsight
//        this.expirationMonth = expirationMonth
//        this.expirationYear = expirationYear
//        this.cardholderName = cardholderName
//    }

    override fun writeToParcel(dest: Parcel, flags: Int) {
        super.writeToParcel(dest, flags)
        dest.writeString(cardType)
        dest.writeString(lastTwo)
        dest.writeString(lastFour)
        dest.writeString(bin)
        dest.writeParcelable(binData, flags)
        dest.writeParcelable(threeDSecureInfo, flags)
        dest.writeParcelable(authenticationInsight, flags)
        dest.writeString(expirationMonth)
        dest.writeString(expirationYear)
        dest.writeString(cardholderName)
    }

    protected constructor(`in`: Parcel) : super(`in`) {
        cardType = `in`.readString()!!
        lastTwo = `in`.readString()!!
        lastFour = `in`.readString()!!
        bin = `in`.readString()!!
        binData = `in`.readParcelable(BinData::class.java.classLoader)!!
        threeDSecureInfo = `in`.readParcelable(ThreeDSecureInfo::class.java.classLoader)!!
        authenticationInsight = `in`.readParcelable(AuthenticationInsight::class.java.classLoader)
        expirationMonth = `in`.readString()!!
        expirationYear = `in`.readString()!!
        cardholderName = `in`.readString()!!
    }

    companion object {
        private const val API_RESOURCE_KEY = "creditCards"
        private const val PAYMENT_METHOD_NONCE_KEY = "nonce"
        private const val PAYMENT_METHOD_DEFAULT_KEY = "default"
        private const val DATA_KEY = "data"
        private const val TOKEN_KEY = "token"
        private const val GRAPHQL_TOKENIZE_CREDIT_CARD_KEY = "tokenizeCreditCard"
        private const val GRAPHQL_CREDIT_CARD_KEY = "creditCard"
        private const val GRAPHQL_BRAND_KEY = "brand"
        private const val GRAPHQL_LAST_FOUR_KEY = "last4"
        private const val THREE_D_SECURE_INFO_KEY = "threeDSecureInfo"
        private const val CARD_DETAILS_KEY = "details"
        private const val CARD_TYPE_KEY = "cardType"
        private const val LAST_TWO_KEY = "lastTwo"
        private const val LAST_FOUR_KEY = "lastFour"
        private const val BIN_KEY = "bin"
        private const val AUTHENTICATION_INSIGHT_KEY = "authenticationInsight"
        private const val EXPIRATION_MONTH_KEY = "expirationMonth"
        private const val EXPIRATION_YEAR_KEY = "expirationYear"
        private const val CARDHOLDER_NAME_KEY = "cardholderName"

        /**
         * Parse card nonce from plain JSON object.
         * @param inputJson plain JSON object
         * @return [CardNonce]
         * @throws JSONException if nonce could not be parsed successfully
         */
        @Throws(JSONException::class)
        fun fromJSON(inputJson: JSONObject): CardNonce {
            return if (isGraphQLTokenizationResponse(inputJson)) {
                fromGraphQLJSON(inputJson)
            } else if (isRESTfulTokenizationResponse(inputJson)) {
                fromRESTJSON(inputJson)
            } else {
                fromPlainJSONObject(inputJson)
            }
        }

        private fun isGraphQLTokenizationResponse(inputJSON: JSONObject): Boolean {
            return inputJSON.has(DATA_KEY)
        }

        private fun isRESTfulTokenizationResponse(inputJSON: JSONObject): Boolean {
            return inputJSON.has(API_RESOURCE_KEY)
        }

        /**
         * Parse card nonce from RESTful Tokenization response.
         * @param inputJson plain JSON object
         * @return [CardNonce]
         * @throws JSONException if nonce could not be parsed successfully
         */
        @Throws(JSONException::class)
        private fun fromRESTJSON(inputJson: JSONObject): CardNonce {
            val json = inputJson.getJSONArray(API_RESOURCE_KEY).getJSONObject(0)
            return fromPlainJSONObject(json)
        }

        /**
         * Parse card nonce from RESTful Tokenization response.
         * @param inputJson plain JSON object
         * @return [CardNonce]
         * @throws JSONException if nonce could not be parsed successfully
         */
        @Throws(JSONException::class)
        private fun fromPlainJSONObject(inputJson: JSONObject): CardNonce {
            val nonce = inputJson.getString(PAYMENT_METHOD_NONCE_KEY)
            val isDefault = inputJson.optBoolean(PAYMENT_METHOD_DEFAULT_KEY, false)
            val details = inputJson.getJSONObject(CARD_DETAILS_KEY)
            val lastTwo = details.getString(LAST_TWO_KEY)
            val lastFour = details.getString(LAST_FOUR_KEY)
            val cardType = details.getString(CARD_TYPE_KEY)
            val threeDSecureInfo = ThreeDSecureInfo.fromJson(
                inputJson.optJSONObject(
                    THREE_D_SECURE_INFO_KEY
                )
            )
            val bin = Json.optString(details, BIN_KEY, "")
            val binData = BinData.fromJson(inputJson.optJSONObject(BinData.BIN_DATA_KEY))
            val authenticationInsight = AuthenticationInsight.fromJson(
                inputJson.optJSONObject(
                    AUTHENTICATION_INSIGHT_KEY
                )
            )
            val expirationMonth = Json.optString(details, EXPIRATION_MONTH_KEY, "")
            val expirationYear = Json.optString(details, EXPIRATION_YEAR_KEY, "")
            val cardholderName = Json.optString(details, CARDHOLDER_NAME_KEY, "")
            return CardNonce(
                cardType,
                lastTwo,
                lastFour,
                threeDSecureInfo,
                bin,
                binData,
                authenticationInsight,
                expirationMonth,
                expirationYear,
                cardholderName,
                nonce,
                isDefault
            )
        }

        /**
         * Parse card nonce from GraphQL Tokenization response.
         * @param inputJson plain JSON object
         * @return [CardNonce]
         * @throws JSONException if nonce could not be parsed successfully
         */
        @Throws(JSONException::class)
        private fun fromGraphQLJSON(inputJson: JSONObject): CardNonce {
            val data = inputJson.getJSONObject(DATA_KEY)
            return if (data.has(GRAPHQL_TOKENIZE_CREDIT_CARD_KEY)) {
                val payload = data.getJSONObject(GRAPHQL_TOKENIZE_CREDIT_CARD_KEY)
                val creditCard = payload.getJSONObject(GRAPHQL_CREDIT_CARD_KEY)
                val lastFour = Json.optString(creditCard, GRAPHQL_LAST_FOUR_KEY, "")
                val lastTwo = if (lastFour.length < 4) "" else lastFour.substring(2)
                val cardType = Json.optString(creditCard, GRAPHQL_BRAND_KEY, "Unknown")
                val threeDSecureInfo = ThreeDSecureInfo.fromJson(null)
                val bin = Json.optString(creditCard, "bin", "")
                val binData = BinData.fromJson(creditCard.optJSONObject(BinData.BIN_DATA_KEY))
                val nonce = payload.getString(TOKEN_KEY)
                val authenticationInsight =
                    AuthenticationInsight.fromJson(payload.optJSONObject(AUTHENTICATION_INSIGHT_KEY))
                val expirationMonth =
                    Json.optString(creditCard, EXPIRATION_MONTH_KEY, "")
                val expirationYear = Json.optString(creditCard, EXPIRATION_YEAR_KEY, "")
                val cardholderName = Json.optString(creditCard, CARDHOLDER_NAME_KEY, "")
                CardNonce(
                    cardType,
                    lastTwo,
                    lastFour,
                    threeDSecureInfo,
                    bin,
                    binData,
                    authenticationInsight,
                    expirationMonth,
                    expirationYear,
                    cardholderName,
                    nonce,
                    false
                )
            } else {
                throw JSONException("Failed to parse GraphQL response JSON")
            }
        }

        val CREATOR: Parcelable.Creator<CardNonce> = object : Parcelable.Creator<CardNonce?> {
            override fun createFromParcel(source: Parcel): CardNonce? {
                return CardNonce(source)
            }

            override fun newArray(size: Int): Array<CardNonce?> {
                return arrayOfNulls(size)
            }
        }
    }
}