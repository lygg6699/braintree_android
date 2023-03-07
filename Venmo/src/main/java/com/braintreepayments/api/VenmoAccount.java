package com.braintreepayments.api;

import androidx.annotation.RestrictTo;

import org.json.JSONException;
import org.json.JSONObject;

class VenmoAccount extends PaymentMethod {

    private static final String VENMO_ACCOUNT_KEY = "venmoAccount";
    private static final String NONCE_KEY = "nonce";

    private String nonce;

    VenmoAccount() {}

    /**
     * @hide
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @Override
    public JSONObject buildJSON() throws JSONException {
        JSONObject json = super.buildJSON();
        JSONObject paymentMethodNonceJson = new JSONObject();
        paymentMethodNonceJson.put(NONCE_KEY, nonce);

        json.put(VENMO_ACCOUNT_KEY, paymentMethodNonceJson);
        return json;
    }

    void setNonce(String nonce) {
        this.nonce = nonce;
    }

    /**
     * @hide
     */
    @RestrictTo(RestrictTo.Scope.LIBRARY_GROUP)
    @Override
    public String getApiPath() {
        return "venmo_accounts";
    }
}
