package com.braintreepayments.api

import android.os.Parcel
import androidx.annotation.RestrictTo
import org.json.JSONException
import org.json.JSONObject

/**
 * An abstract class to extend when creating a payment method. Contains logic and
 * implementations shared by all payment methods.
 */
abstract class PaymentMethod {

    private var integration: String? = DEFAULT_INTEGRATION
    private var sessionId: String? = null
    private var source: String? = DEFAULT_SOURCE

    abstract val apiPath: String?

    internal constructor()

    /**
     * @suppress
     * Sets the integration method associated with the tokenization call for analytics use.
     * Defaults to custom and does not need to ever be set.
     * @param integration the current integration style.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun setIntegration(integration: String?) {
        this.integration = integration
    }

    /**
     * @suppress
     * Sets the source associated with the tokenization call for analytics use. Set automatically.
     *
     * @param source the source of the payment method.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun setSource(source: String?) {
        this.source = source
    }

    /**
     * @suppress
     * @param sessionId sets the session id associated with this request. The session is a uuid.
     * This field is automatically set at the point of tokenization, and any previous
     * values ignored.
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun setSessionId(sessionId: String?) {
        this.sessionId = sessionId
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun buildMetadataJSON(): JSONObject {
        return MetadataBuilder()
            .sessionId(sessionId)
            .source(source)
            .integration(integration)
            .build()
    }

    @Throws(JSONException::class)
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    open fun buildJSON(): JSONObject? {
        val base = JSONObject()
        base.put(MetadataBuilder.META_KEY, buildMetadataJSON())
        return base
    }

    internal constructor(`in`: Parcel) {
        integration = `in`.readString()
        source = `in`.readString()
        sessionId = `in`.readString()
    }

    open fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(integration)
        dest.writeString(source)
        dest.writeString(sessionId)
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    companion object {
        const val OPERATION_NAME_KEY = "operationName"
        const val OPTIONS_KEY = "options"
        const val VALIDATE_KEY = "validate"

        const val DEFAULT_SOURCE: String = "form"
        const val DEFAULT_INTEGRATION: String = "custom"
    }
}