package com.braintreepayments.demo;

import static com.braintreepayments.demo.PayPalNativeCheckoutRequestFactory.createPayPalCheckoutRequest;

import android.os.Build;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.fragment.NavHostFragment;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import com.braintreepayments.api.BraintreeClient;
import com.braintreepayments.api.DataCollector;
import com.braintreepayments.api.PatchOrderRequest;
import com.braintreepayments.api.PayPalNativeCheckoutAccountNonce;
import com.braintreepayments.api.PayPalNativeCheckoutListener;
import com.braintreepayments.api.PayPalNativeCheckoutClient;
import com.braintreepayments.api.PayPalNativeOrderUpdate;
import com.braintreepayments.api.PayPalNativePatchShippingAddress;
import com.braintreepayments.api.PayPalNativePatchShippingOptions;
import com.braintreepayments.api.PayPalNativeShippingCallbacks;
import com.braintreepayments.api.PayPalNativeShippingChangeActions;
import com.braintreepayments.api.PaymentMethodNonce;
import com.braintreepayments.api.PostalAddress;

import java.util.ArrayList;
import java.util.List;

public class PayPalNativeCheckoutFragment extends BaseFragment implements PayPalNativeCheckoutListener {

    private final String TAG = PayPalNativeCheckoutFragment.class.getName();
    private String deviceData;
    private BraintreeClient braintreeClient;
    private PayPalNativeCheckoutClient payPalClient;
    private DataCollector dataCollector;

    public Button launchPayPalNativeCheckoutButton;

    public PayPalNativeCheckoutFragment() {
    }

    @Override
    public View onCreateView(
            LayoutInflater inflater,
            ViewGroup container,
            Bundle savedInstanceState
    ) {
        View view = inflater.inflate(R.layout.fragment_paypal_native_checkout, container, false);

        launchPayPalNativeCheckoutButton = view.findViewById(R.id.paypal_native_checkout_launch);
        launchPayPalNativeCheckoutButton.setOnClickListener(v -> launchPayPalNativeCheckout(false));
        braintreeClient = getBraintreeClient();
        payPalClient = new PayPalNativeCheckoutClient(this, braintreeClient);
        payPalClient.setListener(this);
        return view;
    }

    private void launchPayPalNativeCheckout(boolean isBillingAgreement) {
        FragmentActivity activity = getActivity();
        activity.setProgressBarIndeterminateVisibility(true);

        dataCollector = new DataCollector(braintreeClient);

        braintreeClient.getConfiguration((configuration, configError) -> {
            PayPalNativeShippingCallbacks callbacks = (data, orderUpdateFunction, rejectFunction) -> {
                PayPalNativeShippingChangeActions actions = new PayPalNativeShippingChangeActions();
                actions.setOnPatchComplete(() -> {
                    showText();
                });
                final Handler handler = new Handler(Looper.getMainLooper());
                handler.postDelayed(() -> {
                    callShippingCallback(actions);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        orderUpdateFunction.apply(actions);
                    }
                }, 5_000);
            };

            if (Settings.shouldCollectDeviceData(requireActivity())) {
                dataCollector.collectDeviceData(requireActivity(), (deviceDataResult, error) -> {
                    if (deviceDataResult != null) {
                        deviceData = deviceDataResult;
                    }
                    try {
                        payPalClient.tokenizePayPalAccount(activity, createPayPalCheckoutRequest(activity, "1.00", callbacks));
                    } catch (Exception e) {
                        Log.i(TAG, "Unsupported type");
                    }
                });
            } else {
                try {
                    payPalClient.tokenizePayPalAccount(activity, createPayPalCheckoutRequest(activity, "1.00", callbacks));
                } catch (Exception e) {
                    Log.i(TAG, "Unsupported type");
                }

            }
        });
    }

    public void callShippingCallback(PayPalNativeShippingChangeActions actions) {
        PostalAddress shippingAddress = new PostalAddress();
        shippingAddress.setRecipientName("Brian Tree");
        shippingAddress.setStreetAddress("123 Fake Street");
        shippingAddress.setExtendedAddress("Floor A");
        shippingAddress.setLocality("San Francisco");
        shippingAddress.setRegion("CA");
        shippingAddress.setCountryCodeAlpha2("US");

        PayPalNativePatchShippingAddress amount = new PayPalNativePatchShippingAddress(
                shippingAddress,
                PatchOrderRequest.PatchOperation.REPLACE,
                "default"
        );
        List<PayPalNativeOrderUpdate> list = new ArrayList<>();
        list.add(amount);
        PatchOrderRequest patchOrderRequest = new PatchOrderRequest();
        patchOrderRequest.setUpdates(list);
        actions.setPatchOrderRequest(patchOrderRequest);
    }

    public void showText() {
        Toast.makeText(getContext(), "Invoked onComplete", Toast.LENGTH_SHORT).show();
    }

    private void handlePayPalResult(PaymentMethodNonce paymentMethodNonce) {
        if (paymentMethodNonce != null) {
            super.onPaymentMethodNonceCreated(paymentMethodNonce);

            PayPalNativeCheckoutFragmentDirections.ActionPayPalNativeCheckoutFragmentToDisplayNonceFragment action =
                PayPalNativeCheckoutFragmentDirections.actionPayPalNativeCheckoutFragmentToDisplayNonceFragment(paymentMethodNonce);
            action.setDeviceData(deviceData);

            NavHostFragment.findNavController(this).navigate(action);
        }
    }

    @Override
    public void onPayPalSuccess(@NonNull PayPalNativeCheckoutAccountNonce payPalAccountNonce) {
        handlePayPalResult(payPalAccountNonce);
    }

    @Override
    public void onPayPalFailure(@NonNull Exception error) {
        handleError(error);
    }
}
