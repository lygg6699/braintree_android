package com.braintreepayments.api;


public abstract class PayPalNativeOrderUpdate {
    String purchaseUnitReferenceId;
    PatchOrderRequest.PatchOperation patchOperation;
    Object value;

    abstract String getPath();

    public String getPurchaseUnitReferenceId() {
        return purchaseUnitReferenceId;
    }

    public void setPurchaseUnitReferenceId(String purchaseUnitReferenceId) {
        this.purchaseUnitReferenceId = purchaseUnitReferenceId;
    }

    public PatchOrderRequest.PatchOperation getPatchOperation() {
        return patchOperation;
    }

    public void setPatchOperation(PatchOrderRequest.PatchOperation patchOperation) {
        this.patchOperation = patchOperation;
    }

    public Object getValue() {
        return value;
    }

    public void setValue(Object value) {
        this.value = value;
    }
}