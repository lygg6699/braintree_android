package com.braintreepayments.api;

import androidx.annotation.NonNull;
import androidx.annotation.VisibleForTesting;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.Lifecycle;

import com.paypal.checkout.PayPalCheckout;
import com.paypal.checkout.approve.ApprovalData;
import com.paypal.checkout.config.CheckoutConfig;
import com.paypal.checkout.config.Environment;
import com.paypal.checkout.order.Address;
import com.paypal.checkout.order.Options;
import com.paypal.checkout.order.patch.OrderUpdate;
import com.paypal.checkout.order.patch.PatchOperation;
import com.paypal.checkout.order.patch.PatchOrderRequest;
import com.paypal.checkout.shipping.ShippingChangeData;
import com.paypal.checkout.shipping.ShippingChangeType;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

/**
 * Used to tokenize PayPal accounts. For more information see the
 * <a href="https://developer.paypal.com/braintree/docs/guides/paypal/overview/android/v4">documentation</a>
 */
public class PayPalNativeCheckoutClient {

    private final BraintreeClient braintreeClient;
    private final PayPalNativeCheckoutInternalClient internalPayPalClient;

    private PayPalNativeCheckoutListener listener;

    /**
     * Create a new instance of {@link PayPalNativeCheckoutClient} from within a Fragment using a {@link BraintreeClient}.
     *
     * @param fragment        a {@link Fragment
     * @param braintreeClient a {@link BraintreeClient}
     */
    public PayPalNativeCheckoutClient(@NonNull Fragment fragment, @NonNull BraintreeClient braintreeClient) {
        this(fragment.getActivity(), fragment.getLifecycle(), braintreeClient, new PayPalNativeCheckoutInternalClient(braintreeClient));
    }

    @VisibleForTesting
    PayPalNativeCheckoutClient(FragmentActivity activity, Lifecycle lifecycle, BraintreeClient braintreeClient, PayPalNativeCheckoutInternalClient internalPayPalClient) {
        this.braintreeClient = braintreeClient;
        this.internalPayPalClient = internalPayPalClient;
    }

    /**
     * Add a {@link PayPalNativeCheckoutListener} to your client to receive results or errors from the PayPal flow.
     * This method must be invoked on a {@link PayPalNativeCheckoutClient (Fragment, BraintreeClient)} or
     * {@link PayPalNativeCheckoutClient (FragmentActivity, BraintreeClient)} in order to receive results.
     *
     * @param listener a {@link PayPalNativeCheckoutListener}
     */
    public void setListener(PayPalNativeCheckoutListener listener) {
        this.listener = listener;
    }

    /**
     * Tokenize a PayPal account for vault or checkout.
     * <p>
     * This method must be invoked on a {@link PayPalNativeCheckoutClient (Fragment, BraintreeClient)} or
     * {@link PayPalNativeCheckoutClient (FragmentActivity, BraintreeClient)} in order to receive results.
     *
     * @param activity      Android FragmentActivity
     * @param payPalRequest a {@link PayPalNativeRequest} used to customize the request.
     */
    public void tokenizePayPalAccount(@NonNull final FragmentActivity activity, @NonNull final PayPalNativeRequest payPalRequest) throws Exception {
        if (payPalRequest instanceof PayPalNativeCheckoutRequest) {
            sendCheckoutRequest(activity, (PayPalNativeCheckoutRequest) payPalRequest);
        } else if (payPalRequest instanceof PayPalNativeCheckoutVaultRequest) {
            sendVaultRequest(activity, (PayPalNativeCheckoutVaultRequest) payPalRequest);
        } else {
            throw new Exception("Unsupported request type");
        }
    }

    private void sendCheckoutRequest(final FragmentActivity activity, final PayPalNativeCheckoutRequest payPalCheckoutRequest) {
        braintreeClient.sendAnalyticsEvent("paypal-native.single-payment.selected");
        if (payPalCheckoutRequest.getShouldOfferPayLater()) {
            braintreeClient.sendAnalyticsEvent("paypal-native.single-payment.paylater.offered");
        }

        braintreeClient.getConfiguration((configuration, error) -> {
            sendPayPalRequest(
                activity,
                payPalCheckoutRequest,
                configuration
            );
        });
    }

    private void sendVaultRequest(final FragmentActivity activity, final PayPalNativeCheckoutVaultRequest payPalVaultRequest) {
        braintreeClient.sendAnalyticsEvent("paypal-native.billing-agreement.selected");
        if (payPalVaultRequest.getShouldOfferCredit()) {
            braintreeClient.sendAnalyticsEvent("paypal-native.billing-agreement.credit.offered");
        }

        braintreeClient.getConfiguration((configuration, error) -> {
            sendPayPalRequest(
                activity,
                payPalVaultRequest,
                configuration
            );
        });
    }

    private void sendPayPalRequest(
        final FragmentActivity activity,
        final PayPalNativeRequest payPalRequest,
        final Configuration configuration
    ) {
        internalPayPalClient.sendRequest(activity, payPalRequest, (payPalResponse, error) -> {
            if (payPalResponse != null) {
                String analyticsPrefix = getAnalyticsEventPrefix(payPalRequest);
                braintreeClient.sendAnalyticsEvent(String.format("%s.app-switch.started", analyticsPrefix));

                Environment environment;
                if ("sandbox".equals(configuration.getEnvironment())) {
                    environment = Environment.SANDBOX;
                } else {
                    environment = Environment.LIVE;
                }

                // Start PayPalCheckout flow
                PayPalCheckout.setConfig(
                    new CheckoutConfig(
                        activity.getApplication(),
                        configuration.getPayPalClientId(),
                        environment
                    )
                );

                registerCallbacks(configuration, payPalRequest, payPalResponse);

                PayPalCheckout.startCheckout(createOrderActions -> {
                    if (payPalRequest instanceof PayPalNativeCheckoutRequest) {
                        createOrderActions.set(payPalResponse.getPairingId());
                    } else if (payPalRequest instanceof PayPalNativeCheckoutVaultRequest) {
                        createOrderActions.setBillingAgreementId(payPalResponse.getPairingId());
                    }
                });
            }
        });
    }

    private void registerCallbacks(
        final Configuration configuration,
        final PayPalNativeRequest payPalRequest,
        final PayPalNativeCheckoutResponse payPalResponse
    ) {
        PayPalCheckout.registerCallbacks(
                approval -> {
                    PayPalNativeCheckoutAccount payPalAccount = setupAccount(configuration, payPalRequest, payPalResponse, approval.getData());
                    internalPayPalClient.tokenize(payPalAccount, (payPalAccountNonce, error) -> {
                        if (payPalAccountNonce != null) {
                            listener.onPayPalSuccess(payPalAccountNonce);
                        } else {
                            listener.onPayPalFailure(new Exception("PaypalAccountNonce is null"));
                        }
                    });
                },
                (shippingChangeData, shippingChangeActions) -> payPalRequest.getShippingCallbacks().onShippingChange(
                    mapShippingData(shippingChangeData),
                    (orderUpdate) -> {
                        shippingChangeActions.patchOrder(mapOrderRequest(orderUpdate), () -> {});
                        return null;
                    }
                ),
                () -> listener.onPayPalFailure(new Exception("User has canceled")),
                errorInfo -> listener.onPayPalFailure(new Exception(errorInfo.getError().getMessage()))
        );
    }

    private PatchOrderRequest mapOrderRequest(PayPalNativeShippingChangeActions.PatchOrderRequest patchOrderRequest) {
        List<OrderUpdate> updatedList = new ArrayList<>();

        for (PayPalNativeShippingChangeActions.OrderUpdate orderUpdate: patchOrderRequest.getUpdates()) {
            OrderUpdate newOrderUpdate = new OrderUpdate(
                    orderUpdate.getPurchaseUnitReferenceId(),
                    PatchOperation.valueOf(orderUpdate.getPatchOperation().name()),
                    orderUpdate.getValue()
            ) {
                @Override
                public String getPath$pyplcheckout_externalThreedsRelease() {
                    return null;
                }
            };
            updatedList.add(newOrderUpdate);
        }
        return new PatchOrderRequest(updatedList);
    }

//    private PayPalNativeShippingChangeActions.PatchOrderRequest mapOrderRequest(PatchOrderRequest patchOrderRequest) {
//        PayPalNativeShippingChangeActions.PatchOrderRequest updatedRequest = new PayPalNativeShippingChangeActions.PatchOrderRequest();
//        List<PayPalNativeShippingChangeActions.OrderUpdate> updatedList = new ArrayList<>();
//
//        for (OrderUpdate orderUpdate: patchOrderRequest.getOrderUpdates()) {
//            PayPalNativeShippingChangeActions.OrderUpdate newOrderUpdate = new PayPalNativeShippingChangeActions.OrderUpdate();
//            newOrderUpdate.setValue(orderUpdate.getValue());
//            newOrderUpdate.setPurchaseUnitReferenceId(orderUpdate.getPurchaseUnitReferenceId());
//            // Double check logic
//            newOrderUpdate.setPatchOperation(
//                PayPalNativeShippingChangeActions.PatchOperation.valueOf(
//                    orderUpdate.getPatchOperation().name().toUpperCase(Locale.ROOT)
//                )
//            );
//            updatedList.add(newOrderUpdate);
//        }
//        updatedRequest.setUpdates(updatedList);
//
//        return updatedRequest;
//    }

    private PayPalNativeShippingChangeData mapShippingData(ShippingChangeData shippingChangeData) {
        PayPalNativeShippingChangeData data = new PayPalNativeShippingChangeData();

        data.setPaymentId(shippingChangeData.getPaymentId());
        data.setToken(shippingChangeData.getPayToken());

        PayPalNativeShippingChangeData.ShippingChangeType shippingChangeType;
        if (shippingChangeData.getShippingChangeType() == ShippingChangeType.ADDRESS_CHANGE) {
           shippingChangeType = PayPalNativeShippingChangeData.ShippingChangeType.ADDRESS_CHANGE;
        } else {
           shippingChangeType = PayPalNativeShippingChangeData.ShippingChangeType.OPTION_CHANGE;
        }
        data.setShippingChangeType(shippingChangeType);

        PostalAddress shippingAddress = new PostalAddress();
        Address address = shippingChangeData.getShippingAddress();
        shippingAddress.setStreetAddress(address.getAddressLine1());
        shippingAddress.setExtendedAddress(address.getAddressLine2());
        shippingAddress.setLocality(address.getAdminArea1());
        shippingAddress.setRegion(address.getAdminArea2());
        shippingAddress.setPostalCode(address.getPostalCode());
        data.setShippingAddress(shippingAddress);

        List<PayPalNativeShippingChangeData.Options> options = new ArrayList<>();
        for (Options op: shippingChangeData.getShippingOptions()) {
            PayPalNativeShippingChangeData.Options updatedOptions = new PayPalNativeShippingChangeData.Options();
            updatedOptions.setId(op.getId());
            updatedOptions.setSelected(op.getSelected());
            updatedOptions.setLabel(op.getLabel());

            PayPalNativeShippingChangeData.Options.UnitAmount unitAmount = new PayPalNativeShippingChangeData.Options.UnitAmount();
            if (op.getAmount() != null) {
                unitAmount.setValue(op.getAmount().getValue());
                unitAmount.setCurrencyCode(PayPalNativeShippingChangeData.CurrencyCode.valueOf(op.getAmount().getCurrencyCode().name()));
            }
            updatedOptions.setUnitAmount(unitAmount);
            options.add(updatedOptions);
        }
        data.setShippingOptions(options);

        return data;
    }

    private PayPalNativeCheckoutAccount setupAccount(
            final Configuration configuration,
            final PayPalNativeRequest payPalRequest,
            final PayPalNativeCheckoutResponse payPalResponse,
            final ApprovalData approvalData
    ) {
        PayPalNativeCheckoutAccount payPalAccount = new PayPalNativeCheckoutAccount();

        String merchantAccountId = payPalRequest.getMerchantAccountId();
        String paymentType = payPalRequest instanceof PayPalNativeCheckoutVaultRequest ? "billing-agreement" : "single-payment";
        payPalAccount.setClientMetadataId(configuration.getPayPalClientId());
        payPalAccount.setSource("paypal-browser");
        payPalAccount.setPaymentType(paymentType);

        try {
            JSONObject client = new JSONObject();
            client.put("platform", "android");
            client.put("product_name", "PayPal");
            client.put("paypal_sdk_version", "version");
            payPalAccount.setClient(client);

            JSONObject urlResponseData = new JSONObject();
            JSONObject response = new JSONObject();

            if (approvalData.getCart() != null && approvalData.getCart().getReturnUrl() != null) {
                response.put("webURL", approvalData.getCart().getReturnUrl().getHref());
            }
            urlResponseData.put("response", response);
            urlResponseData.put("response_type", "web");
            payPalAccount.setUrlResponseData(response);
        } catch (JSONException jsonException) {
            listener.onPayPalFailure(jsonException);
        }

        if (merchantAccountId != null) {
            payPalAccount.setMerchantAccountId(merchantAccountId);
        }

        return payPalAccount;
    }

    private static String getAnalyticsEventPrefix(PayPalNativeRequest request) {
        return request instanceof PayPalNativeCheckoutVaultRequest ? "paypal-native.billing-agreement" : "paypal-native.single-payment";
    }
}
