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
     *
     * @param orderUpdateFunction Call patchOrder() to update an order. The patch action will
     * only work for orders with the CREATED or APPROVED status. You cannot update an order with the COMPLETED status.
     *
     * @param rejectFunction Call reject when a buyer selects a shipping option that is not supported
     * or has entered a shipping address that is not supported. The paysheet will require the buyer
     * to fix the issue before continuing with the order.
     */
    void onShippingChange(
        PayPalNativeShippingChangeData data,
        Function<PayPalNativeShippingChangeActions, Void> orderUpdateFunction,
        Function<Object, Void> rejectFunction
    );
}
