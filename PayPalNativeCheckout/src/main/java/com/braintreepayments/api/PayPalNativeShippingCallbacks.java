package com.braintreepayments.api;

import java.util.function.Function;

/**
 * This interface definition is to provide a callback for when a buyer makes a shipping change.
 */
public interface PayPalNativeShippingCallbacks {

    /**
     * Called when a buyer makes a shipping change in the paysheet.
     * This can either be updating a shipping address or selecting a different shipping option.
     *
     * @param data data containing the updated shipping information
     */
    void onShippingChange(
        PayPalNativeShippingChangeData data,
        Function<PayPalNativeShippingChangeActions.PatchOrderRequest, Void> orderUpdateFunction
    );
}
