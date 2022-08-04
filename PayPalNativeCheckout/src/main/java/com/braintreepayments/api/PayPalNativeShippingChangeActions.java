package com.braintreepayments.api;

public class PayPalNativeShippingChangeActions {

    private OnPatchComplete onPatchComplete;
    private PatchOrderRequest patchOrderRequest;

    public OnPatchComplete getOnPatchComplete() {
        return onPatchComplete;
    }

    public void setOnPatchComplete(OnPatchComplete onPatchComplete) {
        this.onPatchComplete = onPatchComplete;
    }

    public PatchOrderRequest getPatchOrderRequest() {
        return patchOrderRequest;
    }

    public void setPatchOrderRequest(PatchOrderRequest patchOrderRequest) {
        this.patchOrderRequest = patchOrderRequest;
    }

    public interface OnPatchComplete {

        /**
         * Called when a patch order request has completed. This will only be called when a patch
         * request has succeeded
         */
        void onPatchComplete();
    }
}
