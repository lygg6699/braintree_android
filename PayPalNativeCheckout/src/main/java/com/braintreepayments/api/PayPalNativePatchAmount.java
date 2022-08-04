package com.braintreepayments.api;

import androidx.annotation.Nullable;

import com.paypal.checkout.order.Amount;

import java.util.Objects;

/**
 *  Replaces the amount on a PurchaseUnit
 */
public class PayPalNativePatchAmount extends PayPalNativeOrderUpdate {
    private Amount amount;

    public Amount getAmount() {
        return amount;
    }

    public void setAmount(Amount amount) {
        this.amount = amount;
    }

    public PayPalNativePatchAmount(
        Amount amount,
        @Nullable String purchaseUnitReferenceId
    ) {
        this.amount = amount;
        setPurchaseUnitReferenceId(Objects.requireNonNullElse(purchaseUnitReferenceId, "default"));
        setPatchOperation(PatchOrderRequest.PatchOperation.REPLACE);
        setValue(amount);
    }

    @Override
    String getPath() {
        return "/purchase_units/@reference_id=='"+ getPurchaseUnitReferenceId() + "'/amount";
    }
}
