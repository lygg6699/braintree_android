package com.braintreepayments.api;

import androidx.annotation.Nullable;

import java.util.List;
import java.util.Objects;

/**
 *  Replaces the shipping address on a PurchaseUnit
 */
public class PayPalNativePatchShippingOptions extends PayPalNativeOrderUpdate {
    private List<PayPalNativeShippingChangeData.Options> optionsList;

    public List<PayPalNativeShippingChangeData.Options> getOptionsList() {
        return optionsList;
    }

    public PayPalNativePatchShippingOptions(
        List<PayPalNativeShippingChangeData.Options> optionsList,
        @Nullable String purchaseUnitReferenceId
    ) {
        this.optionsList = optionsList;
        setPurchaseUnitReferenceId(Objects.requireNonNullElse(purchaseUnitReferenceId, "default"));
        setPatchOperation(PatchOrderRequest.PatchOperation.REPLACE);
        setValue(optionsList);
    }

    @Override
    String getPath() {
        return "/purchase_units/@reference_id=='"+ getPurchaseUnitReferenceId() + "'/shipping/options";
    }
}
