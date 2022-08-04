package com.braintreepayments.api;

public class PayPalNativeAmount {
    private PayPalNativeShippingChangeData.CurrencyCode currencyCode;
    private String value;
    private BreakDown breakDown;

    public PayPalNativeShippingChangeData.CurrencyCode getCurrencyCode() {
        return currencyCode;
    }

    public void setCurrencyCode(PayPalNativeShippingChangeData.CurrencyCode currencyCode) {
        this.currencyCode = currencyCode;
    }

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    public BreakDown getBreakDown() {
        return breakDown;
    }

    public void setBreakDown(BreakDown breakDown) {
        this.breakDown = breakDown;
    }

    public static class BreakDown {
        PayPalNativeShippingChangeData.Options.UnitAmount itemTotal;
        PayPalNativeShippingChangeData.Options.UnitAmount shipping;
        PayPalNativeShippingChangeData.Options.UnitAmount handling;
        PayPalNativeShippingChangeData.Options.UnitAmount taxTotal;
        PayPalNativeShippingChangeData.Options.UnitAmount shippingDiscount;
        PayPalNativeShippingChangeData.Options.UnitAmount discount;

        public PayPalNativeShippingChangeData.Options.UnitAmount getItemTotal() {
            return itemTotal;
        }

        public void setItemTotal(PayPalNativeShippingChangeData.Options.UnitAmount itemTotal) {
            this.itemTotal = itemTotal;
        }

        public PayPalNativeShippingChangeData.Options.UnitAmount getShipping() {
            return shipping;
        }

        public void setShipping(PayPalNativeShippingChangeData.Options.UnitAmount shipping) {
            this.shipping = shipping;
        }

        public PayPalNativeShippingChangeData.Options.UnitAmount getHandling() {
            return handling;
        }

        public void setHandling(PayPalNativeShippingChangeData.Options.UnitAmount handling) {
            this.handling = handling;
        }

        public PayPalNativeShippingChangeData.Options.UnitAmount getTaxTotal() {
            return taxTotal;
        }

        public void setTaxTotal(PayPalNativeShippingChangeData.Options.UnitAmount taxTotal) {
            this.taxTotal = taxTotal;
        }

        public PayPalNativeShippingChangeData.Options.UnitAmount getShippingDiscount() {
            return shippingDiscount;
        }

        public void setShippingDiscount(PayPalNativeShippingChangeData.Options.UnitAmount shippingDiscount) {
            this.shippingDiscount = shippingDiscount;
        }

        public PayPalNativeShippingChangeData.Options.UnitAmount getDiscount() {
            return discount;
        }

        public void setDiscount(PayPalNativeShippingChangeData.Options.UnitAmount discount) {
            this.discount = discount;
        }
    }
}
