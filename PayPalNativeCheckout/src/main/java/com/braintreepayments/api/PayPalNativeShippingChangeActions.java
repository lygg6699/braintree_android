package com.braintreepayments.api;

import java.util.List;

public class PayPalNativeShippingChangeActions {

    static class PatchOrderRequest {
        private List<OrderUpdate> updates;

        public List<OrderUpdate> getUpdates() {
            return updates;
        }

        public void setUpdates(List<OrderUpdate> updates) {
            this.updates = updates;
        }
    }

    static class OrderUpdate {
        private String purchaseUnitReferenceId;
        private PatchOperation patchOperation;
        Object value;

        public String getPurchaseUnitReferenceId() {
            return purchaseUnitReferenceId;
        }

        public void setPurchaseUnitReferenceId(String purchaseUnitReferenceId) {
            this.purchaseUnitReferenceId = purchaseUnitReferenceId;
        }

        public PatchOperation getPatchOperation() {
            return patchOperation;
        }

        public void setPatchOperation(PatchOperation patchOperation) {
            this.patchOperation = patchOperation;
        }

        public Object getValue() {
            return value;
        }

        public void setValue(Object value) {
            this.value = value;
        }
    }

    enum PatchOperation {
        ADD,
        REPLACE,
        REMOVE
    }

}
