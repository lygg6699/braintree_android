package com.braintreepayments.api;

import java.util.List;

public class PatchOrderRequest {
    private List<PayPalNativeOrderUpdate> updates;

    public List<PayPalNativeOrderUpdate> getUpdates() {
        return updates;
    }

    public void setUpdates(List<PayPalNativeOrderUpdate> updates) {
        this.updates = updates;
    }

    public enum PatchOperation {
        ADD,
        REPLACE,
        REMOVE
    }
}