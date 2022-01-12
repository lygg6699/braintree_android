package com.braintreepayments.demo;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentActivity;
import androidx.navigation.fragment.NavHostFragment;

import com.braintreepayments.api.BraintreeClient;
import com.braintreepayments.api.BraintreeRequestCodes;
import com.braintreepayments.api.BrowserSwitchListener;
import com.braintreepayments.api.BrowserSwitchResult;
import com.braintreepayments.api.DataCollector;
import com.braintreepayments.api.PayPalAccountNonce;
import com.braintreepayments.api.PayPalClient;
import com.braintreepayments.api.PayPalListener;

import static com.braintreepayments.demo.PayPalRequestFactory.createPayPalCheckoutRequest;
import static com.braintreepayments.demo.PayPalRequestFactory.createPayPalVaultRequest;

public class PayPalFragment extends BaseFragment implements BrowserSwitchListener, PayPalListener {

    private BraintreeClient braintreeClient;

    private String deviceData;
    private PayPalClient payPalClient;
    private DataCollector dataCollector;

    private Button billingAgreementButton;
    private Button singlePaymentButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_paypal, container, false);
        billingAgreementButton = view.findViewById(R.id.paypal_billing_agreement_button);
        singlePaymentButton = view.findViewById(R.id.paypal_single_payment_button);

        billingAgreementButton.setOnClickListener(this::launchBillingAgreement);
        singlePaymentButton.setOnClickListener(this::launchSinglePayment);

        return view;
    }

    public void launchSinglePayment(View v) {
        launchPayPal(false);
    }

    public void launchBillingAgreement(View v) {
        launchPayPal(true);
    }

    private void launchPayPal(boolean isBillingAgreement) {
        FragmentActivity activity = getActivity();
        activity.setProgressBarIndeterminateVisibility(true);

        Context context = requireContext();
        braintreeClient = new BraintreeClient(context, new DemoAuthorizationProvider(context));

        dataCollector = new DataCollector(braintreeClient);
        payPalClient = new PayPalClient(this, braintreeClient);
        payPalClient.setPayPalListener(this);

        braintreeClient.getConfiguration((configuration, configError) -> {
            if (getActivity().getIntent().getBooleanExtra(MainFragment.EXTRA_COLLECT_DEVICE_DATA, false)) {
                dataCollector.collectDeviceData(activity, (deviceData, dataCollectorError) -> this.deviceData = deviceData);
            }
            if (isBillingAgreement) {
                payPalClient.tokenizePayPalAccount(activity, createPayPalVaultRequest(activity));
            } else {
                payPalClient.tokenizePayPalAccount(activity, createPayPalCheckoutRequest(activity, "1.00"));
            }
        });
    }

    @Override
    public void onBrowserSwitchResult(BrowserSwitchResult result) {
        if (result.getRequestCode() == BraintreeRequestCodes.PAYPAL) {
            payPalClient.onBrowserSwitchResult(result);
        }
    }

    @Override
    public void onPayPalTokenizeSuccess(@NonNull PayPalAccountNonce payPalAccountNonce) {
        super.onPaymentMethodNonceCreated(payPalAccountNonce);

        PayPalFragmentDirections.ActionPayPalFragmentToDisplayNonceFragment action =
                PayPalFragmentDirections.actionPayPalFragmentToDisplayNonceFragment(payPalAccountNonce);
        action.setDeviceData(deviceData);

        NavHostFragment.findNavController(this).navigate(action);
    }

    @Override
    public void onPayPalTokenizeError(@NonNull Exception error) {
        handleError(error);
    }
}
