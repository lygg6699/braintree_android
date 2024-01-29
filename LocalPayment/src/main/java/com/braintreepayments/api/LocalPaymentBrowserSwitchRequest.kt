package com.braintreepayments.api

import androidx.annotation.RestrictTo

/**
 * Request parameters associated with a [LocalPaymentPendingRequest.Started]
 */
class LocalPaymentBrowserSwitchRequest internal constructor(
    browserSwitchPendingRequest: BrowserSwitchPendingRequest.Started
) {

    private var browserSwitchPendingRequest: BrowserSwitchPendingRequest.Started

    init {
        this.browserSwitchPendingRequest = browserSwitchPendingRequest
    }

    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    fun getBrowserSwitchPendingRequest(): BrowserSwitchPendingRequest.Started {
        return browserSwitchPendingRequest
    }
}
