package com.braintreepayments.api;

import androidx.annotation.Nullable;

import com.paypal.checkout.order.Amount;

import java.util.Objects;

/**
 *  Replaces the amount on a PurchaseUnit
 */
public class PayPalNativePatchAmount extends PayPalNativeOrderUpdate {
    private PayPalNativeAmount amount;

    public PayPalNativeAmount getAmount() {
        return amount;
    }

    public void setAmount(PayPalNativeAmount amount) {
        this.amount = amount;
    }

    public PayPalNativePatchAmount(
            PayPalNativeAmount amount,
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
