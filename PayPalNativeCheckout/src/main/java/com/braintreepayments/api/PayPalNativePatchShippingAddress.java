package com.braintreepayments.api;

import androidx.annotation.Nullable;

import java.util.Objects;

/**
 *  Replaces the shipping Options on a PurchaseUnit
 */
public class PayPalNativePatchShippingAddress extends PayPalNativeOrderUpdate {
    private PostalAddress address;

    public PostalAddress getAddress() {
        return address;
    }

    public void setAddress(PostalAddress address) {
        this.address = address;
    }

    public PayPalNativePatchShippingAddress(
        PostalAddress address,
        PatchOrderRequest.PatchOperation patchOperation,
        @Nullable String purchaseUnitReferenceId
    ) {
        this.address = address;
        setPurchaseUnitReferenceId(Objects.requireNonNullElse(purchaseUnitReferenceId, "default"));
        setPatchOperation(patchOperation);
        setValue(address);
    }

    @Override
    String getPath() {
        return "/purchase_units/@reference_id=='"+ getPurchaseUnitReferenceId() + "'/shipping/address";
    }
}
