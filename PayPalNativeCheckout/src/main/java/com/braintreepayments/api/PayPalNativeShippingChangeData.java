package com.braintreepayments.api;

import java.util.List;

public class PayPalNativeShippingChangeData {
    private String token;
    private String paymentId;
    private ShippingChangeType shippingChangeType;
    private PostalAddress shippingAddress;
    private List<Options> shippingOptions;

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }

    public String getPaymentId() {
        return paymentId;
    }

    public void setPaymentId(String paymentId) {
        this.paymentId = paymentId;
    }

    public ShippingChangeType getShippingChangeType() {
        return shippingChangeType;
    }

    public void setShippingChangeType(ShippingChangeType shippingChangeType) {
        this.shippingChangeType = shippingChangeType;
    }

    public PostalAddress getShippingAddress() {
        return shippingAddress;
    }

    public void setShippingAddress(PostalAddress shippingAddress) {
        this.shippingAddress = shippingAddress;
    }

    public List<Options> getShippingOptions() {
        return shippingOptions;
    }

    public void setShippingOptions(List<Options> shippingOptions) {
        this.shippingOptions = shippingOptions;
    }

    enum ShippingChangeType {
        // The buyer has updated their shipping address.
        ADDRESS_CHANGE,

        // The buyer has selected a different shipping or pickup option
        OPTION_CHANGE
    }

    static class Options {
        private String id;
        private boolean selected;
        private String label;
        private ShippingType shippingType;
        private UnitAmount unitAmount;

        public ShippingType getShippingType() {
            return shippingType;
        }

        public void setShippingType(ShippingType shippingType) {
            this.shippingType = shippingType;
        }

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public boolean isSelected() {
            return selected;
        }

        public void setSelected(boolean selected) {
            this.selected = selected;
        }

        public String getLabel() {
            return label;
        }

        public void setLabel(String label) {
            this.label = label;
        }

        public UnitAmount getUnitAmount() {
            return unitAmount;
        }

        public void setUnitAmount(UnitAmount unitAmount) {
            this.unitAmount = unitAmount;
        }

        static class UnitAmount {
            private CurrencyCode currencyCode;
            private String value;

            public CurrencyCode getCurrencyCode() {
                return currencyCode;
            }

            public void setCurrencyCode(CurrencyCode currencyCode) {
                this.currencyCode = currencyCode;
            }

            public String getValue() {
                return value;
            }

            public void setValue(String value) {
                this.value = value;
            }
        }

        enum ShippingType {
            /**
             * The payer intends to receive the items at a specified address.
             */
            SHIPPING,

            /**
             * The payer intends to pick up the items at a specified address. For example, a store address.
             */
            PICKUP
        }
    }

    enum CurrencyCode {

        /**
         * Currency Code for: Australian dollar
         */
        AUD,

        /**
         * Currency Code for: Brazilian real
         *
         * Note: This currency is supported as a payment currency and a currency balance for in-country
         * PayPal accounts only. If the receiver of funds is not from Brazil, then PayPal converts funds
         * into the primary holding currency of the account with the applicable currency conversion
         * rate. The currency conversion rate includes PayPal's applicable spread or fee.
         */
        BRL,

        /**
         * Currency Code for: Canadian dollar
         */
        CAD,

        /**
         * Currency Code for: Chinese Renmenbi
         *
         * Note: This currency is supported as a payment currency and a currency balance for in-country
         * PayPal accounts only.
         */
        CNY,

        /**
         * Currency Code for: Czech koruna
         */
        CZK,

        /**
         * Currency Code for: Danish krone
         */
        DKK,

        /**
         * Currency Code for: Euro
         */
        EUR,

        /**
         * Currency Code for: Hong Kong dollar
         */
        HKD,

        /**
         * Currency Code for: Hungarian forint
         *
         * Note: This currency does not support decimals. If you pass a decimal amount, an error occurs.
         */
        HUF,

        /**
         * Currency Code for: Indian rupee
         *
         * Note: This currency is supported as a payment currency and a currency balance for in-country
         * PayPal India accounts only.
         */
        INR,

        /**
         * Currency Code for: Israeli new shekel
         */
        ILS,

        /**
         * Currency Code for: Japanese yen
         *
         * Note: This currency does not support decimals. If you pass a decimal amount, an error occurs.
         */
        JPY,

        /**
         * Currency Code for: Malaysian ringgit
         *
         * Note: This currency is supported as a payment currency and a currency balance for in-country
         * PayPal accounts only.
         */
        MYR,

        /**
         * Currency Code for: Mexican peso
         */
        MXN,

        /**
         * Currency Code for: New Taiwan dollar
         *
         * Note: This currency does not support decimals. If you pass a decimal amount, an error occurs.
         */
        TWD,

        /**
         * Currency Code for: New Zealand dollar
         */
        NZD,

        /**
         * Currency Code for: Norwegian krone
         */
        NOK,

        /**
         * Currency Code for: Philippine peso
         */
        PHP,

        /**
         * Currency Code for: Polish złoty
         */
        PLN,

        /**
         * Currency Code for: Pound Sterling
         */
        GBP,

        /**
         * Currency Code for: Russian ruble
         */
        RUB,

        /**
         * Currency Code for: Singapore dollar
         */
        SGD,

        /**
         * Currency Code for: Swedish krona
         */
        SEK,

        /**
         * Currency Code for: Swiss franc
         */
        CHF,

        /**
         * Currency Code for: Thai baht
         */
        THB,

        /**
         * Currency Code for: United States dollar
         */
        USD
    }

}