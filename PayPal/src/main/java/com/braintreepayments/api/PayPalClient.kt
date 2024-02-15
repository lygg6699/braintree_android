package com.braintreepayments.api

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import androidx.annotation.VisibleForTesting
import org.json.JSONException
import org.json.JSONObject

/**
 * Used to tokenize PayPal accounts. For more information see the
 * [documentation](https://developer.paypal.com/braintree/docs/guides/paypal/overview/android/v4)
 */
@Suppress("TooManyFunctions")
class PayPalClient @VisibleForTesting internal constructor(
    private val braintreeClient: BraintreeClient,
    private val internalPayPalClient: PayPalInternalClient
) {
    /**
     * Initializes a new [PayPalClient] instance
     *
     * @param context       an Android Context
     * @param authorization a Tokenization Key or Client Token used to authenticate
     */
    constructor(context: Context, authorization: String) : this(
        BraintreeClient(
            context,
            authorization
        )
    )

    @VisibleForTesting
    internal constructor(braintreeClient: BraintreeClient) : this(
        braintreeClient,
        PayPalInternalClient(braintreeClient)
    )

    /**
     * Starts the PayPal payment flow by creating a [PayPalPaymentAuthRequestParams] to be
     * used to launch the PayPal web authentication flow in
     * [PayPalLauncher.launch].
     *
     * @param context       Android Context
     * @param payPalRequest a [PayPalRequest] used to customize the request.
     * @param callback      [PayPalPaymentAuthCallback]
     */
    fun createPaymentAuthRequest(
        context: Context,
        payPalRequest: PayPalRequest,
        callback: PayPalPaymentAuthCallback
    ) {
        braintreeClient.sendAnalyticsEvent(PayPalAnalytics.TOKENIZATION_STARTED)
        if (payPalRequest is PayPalCheckoutRequest) {
            sendCheckoutRequest(context, payPalRequest, callback)
        } else if (payPalRequest is PayPalVaultRequest) {
            sendVaultRequest(context, payPalRequest, callback)
        }
    }

    private fun sendCheckoutRequest(
        context: Context,
        payPalCheckoutRequest: PayPalCheckoutRequest,
        callback: PayPalPaymentAuthCallback
    ) {
        braintreeClient.getConfiguration { configuration: Configuration?, error: Exception? ->
            if (error != null) {
                callbackCreatePaymentAuthFailure(
                    callback,
                    PayPalPaymentAuthRequest.Failure(error)
                )
                return@getConfiguration
            }
            if (payPalConfigInvalid(configuration)) {
                val configInvalidError = createPayPalError()
                callbackCreatePaymentAuthFailure(
                    callback,
                    PayPalPaymentAuthRequest.Failure(configInvalidError)
                )
                return@getConfiguration
            }
            sendPayPalRequest(context, payPalCheckoutRequest, callback)
        }
    }

    private fun sendVaultRequest(
        context: Context,
        payPalVaultRequest: PayPalVaultRequest,
        callback: PayPalPaymentAuthCallback
    ) {
        braintreeClient.getConfiguration { configuration: Configuration?, error: Exception? ->
            if (error != null) {
                callbackCreatePaymentAuthFailure(
                    callback,
                    PayPalPaymentAuthRequest.Failure(error)
                )
                return@getConfiguration
            }
            if (payPalConfigInvalid(configuration)) {
                val configInvalidError = createPayPalError()
                callbackCreatePaymentAuthFailure(
                    callback,
                    PayPalPaymentAuthRequest.Failure(configInvalidError)
                )
                return@getConfiguration
            }
            sendPayPalRequest(context, payPalVaultRequest, callback)
        }
    }

    private fun sendPayPalRequest(
        context: Context, payPalRequest: PayPalRequest,
        callback: PayPalPaymentAuthCallback
    ) {
        internalPayPalClient.sendRequest(
            context,
            payPalRequest
        ) { payPalResponse: PayPalPaymentAuthRequestParams?, error: Exception? ->
            if (payPalResponse != null) {
                try {
                    val options = buildBrowserSwitchOptions(payPalResponse)
                    payPalResponse.browserSwitchOptions = options
                    callback.onPayPalPaymentAuthRequest(
                        PayPalPaymentAuthRequest.ReadyToLaunch(payPalResponse)
                    )
                } catch (exception: JSONException) {
                    callbackCreatePaymentAuthFailure(
                        callback,
                        PayPalPaymentAuthRequest.Failure(exception)
                    )
                }
            } else {
                callbackCreatePaymentAuthFailure(
                    callback,
                    PayPalPaymentAuthRequest.Failure(error!!)
                )
            }
        }
    }

    @Throws(JSONException::class)
    private fun buildBrowserSwitchOptions(
        paymentAuthRequest: PayPalPaymentAuthRequestParams
    ): BrowserSwitchOptions {
        val metadata = JSONObject()
        metadata.put("approval-url", paymentAuthRequest.approvalUrl)
        metadata.put("success-url", paymentAuthRequest.successUrl)
        val paymentType =
            if (paymentAuthRequest.isBillingAgreement) "billing-agreement" else "single-payment"
        metadata.put("payment-type", paymentType)
        metadata.put("client-metadata-id", paymentAuthRequest.clientMetadataId)
        metadata.put("merchant-account-id", paymentAuthRequest.merchantAccountId)
        metadata.put("source", "paypal-browser")
        metadata.put("intent", paymentAuthRequest.intent)
        return BrowserSwitchOptions().requestCode(BraintreeRequestCodes.PAYPAL)
            .url(Uri.parse(paymentAuthRequest.approvalUrl))
            .returnUrlScheme(braintreeClient.getReturnUrlScheme())
            .launchAsNewTask(braintreeClient.launchesBrowserSwitchAsNewTask())
            .metadata(metadata)
    }

    /**
     * After receiving a result from the PayPal web authentication flow via
     * [PayPalLauncher.handleReturnToAppFromBrowser],
     * pass the [PayPalPaymentAuthResult.Success] returned to this method to tokenize the PayPal
     * account and receive a [PayPalAccountNonce] on success.
     *
     * @param paymentAuthResult a [PayPalPaymentAuthResult.Success] received in the callback
     * from  [PayPalLauncher.handleReturnToAppFromBrowser]
     * @param callback          [PayPalTokenizeCallback]
     */
    @Suppress("SwallowedException")
    fun tokenize(
        paymentAuthResult: PayPalPaymentAuthResult.Success,
        callback: PayPalTokenizeCallback
    ) {
        val browserSwitchResult = paymentAuthResult.paymentAuthInfo.browserSwitchResult
        val metadata = browserSwitchResult.requestMetadata
        val clientMetadataId = Json.optString(metadata, "client-metadata-id", null)
        val merchantAccountId = Json.optString(metadata, "merchant-account-id", null)
        val payPalIntent = Json.optString(metadata, "intent", null)
        val approvalUrl = Json.optString(metadata, "approval-url", null)
        val successUrl = Json.optString(metadata, "success-url", null)
        val paymentType = Json.optString(metadata, "payment-type", "unknown")
        val isBillingAgreement = paymentType.equals("billing-agreement", ignoreCase = true)
        val tokenKey = if (isBillingAgreement) "ba_token" else "token"
        try {
            val deepLinkUri = browserSwitchResult.deepLinkUrl
            if (deepLinkUri != null) {
                val urlResponseData = parseUrlResponseData(
                    deepLinkUri, successUrl, approvalUrl,
                    tokenKey
                )
                val payPalAccount = PayPalAccount()
                payPalAccount.setClientMetadataId(clientMetadataId)
                payPalAccount.setIntent(payPalIntent)
                payPalAccount.setSource("paypal-browser")
                payPalAccount.setUrlResponseData(urlResponseData)
                payPalAccount.setPaymentType(paymentType)
                if (merchantAccountId != null) {
                    payPalAccount.setMerchantAccountId(merchantAccountId)
                }
                if (payPalIntent != null) {
                    payPalAccount.setIntent(payPalIntent)
                }
                internalPayPalClient.tokenize(
                    payPalAccount
                ) { payPalAccountNonce: PayPalAccountNonce?, error: Exception? ->
                    if (payPalAccountNonce != null) {
                        callbackTokenizeSuccess(
                            callback,
                            PayPalResult.Success(payPalAccountNonce)
                        )
                    } else if (error != null) {
                        callbackTokenizeFailure(
                            callback,
                            PayPalResult.Failure(error)
                        )
                    }
                }
            } else {
                callbackTokenizeFailure(
                    callback,
                    PayPalResult.Failure(BraintreeException("Unknown error"))
                )
            }
        } catch (e: UserCanceledException) {
            callbackBrowserSwitchCancel(callback, PayPalResult.Cancel)
        } catch (e: JSONException) {
            callbackTokenizeFailure(callback, PayPalResult.Failure(e))
        } catch (e: PayPalBrowserSwitchException) {
            callbackTokenizeFailure(callback, PayPalResult.Failure(e))
        }
    }

    @Throws(JSONException::class, UserCanceledException::class, PayPalBrowserSwitchException::class)
    private fun parseUrlResponseData(
        uri: Uri,
        successUrl: String,
        approvalUrl: String,
        tokenKey: String
    ): JSONObject {
        val status = uri.lastPathSegment
        if (Uri.parse(successUrl).lastPathSegment != status) {
            throw UserCanceledException("User canceled PayPal.", true)
        }
        val requestXoToken = Uri.parse(approvalUrl).getQueryParameter(tokenKey)
        val responseXoToken = uri.getQueryParameter(tokenKey)
        return if (responseXoToken != null && TextUtils.equals(requestXoToken, responseXoToken)) {
            val client = JSONObject()
            client.put("environment", null)
            val urlResponseData = JSONObject()
            urlResponseData.put("client", client)
            val response = JSONObject()
            response.put("webURL", uri.toString())
            urlResponseData.put("response", response)
            urlResponseData.put("response_type", "web")
            urlResponseData
        } else {
            throw PayPalBrowserSwitchException("The response contained inconsistent data.")
        }
    }

    private fun callbackCreatePaymentAuthFailure(
        callback: PayPalPaymentAuthCallback,
        failure: PayPalPaymentAuthRequest.Failure
    ) {
        braintreeClient.sendAnalyticsEvent(PayPalAnalytics.TOKENIZATION_FAILED)
        callback.onPayPalPaymentAuthRequest(failure)
    }

    private fun callbackBrowserSwitchCancel(
        callback: PayPalTokenizeCallback,
        cancel: PayPalResult.Cancel
    ) {
        braintreeClient.sendAnalyticsEvent(PayPalAnalytics.BROWSER_LOGIN_CANCELED)
        callback.onPayPalResult(cancel)
    }

    private fun callbackTokenizeFailure(
        callback: PayPalTokenizeCallback,
        failure: PayPalResult.Failure
    ) {
        braintreeClient.sendAnalyticsEvent(PayPalAnalytics.TOKENIZATION_FAILED)
        callback.onPayPalResult(failure)
    }

    private fun callbackTokenizeSuccess(
        callback: PayPalTokenizeCallback,
        success: PayPalResult.Success
    ) {
        braintreeClient.sendAnalyticsEvent(PayPalAnalytics.TOKENIZATION_SUCCEEDED)
        callback.onPayPalResult(success)
    }

    companion object {
        private fun payPalConfigInvalid(configuration: Configuration?): Boolean {
            return (configuration == null) || !configuration.isPayPalEnabled
        }

        private fun createPayPalError(): Exception {
            return BraintreeException(
                "PayPal is not enabled. " +
                        "See https://developer.paypal.com/braintree/docs/guides/paypal/overview/android/v4 " +
                        "for more information."
            )
        }
    }
}
