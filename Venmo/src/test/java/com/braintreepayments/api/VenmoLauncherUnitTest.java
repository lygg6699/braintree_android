package com.braintreepayments.api;

import static junit.framework.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.same;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import android.content.Context;
import android.content.Intent;

import androidx.activity.ComponentActivity;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.ActivityResultRegistry;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.fragment.app.FragmentActivity;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.robolectric.RobolectricTestRunner;

@RunWith(RobolectricTestRunner.class)
public class VenmoLauncherUnitTest {

    @Mock
    ActivityResultLauncher<VenmoPaymentAuthRequestParams> activityResultLauncher;
    private VenmoLauncherCallback callback;

    @Before
    public void beforeEach() {
        MockitoAnnotations.openMocks(this);
        callback = mock(VenmoLauncherCallback.class);
    }

    @Test
    public void constructor_createsActivityLauncher() {
        String expectedKey = "com.braintreepayments.api.Venmo.RESULT";
        ActivityResultRegistry activityResultRegistry = mock(ActivityResultRegistry.class);
        FragmentActivity lifecycleOwner = new FragmentActivity();

        VenmoLauncher sut = new VenmoLauncher(activityResultRegistry, lifecycleOwner, callback);

        verify(activityResultRegistry).register(eq(expectedKey), same(lifecycleOwner),
                Mockito.<ActivityResultContract<VenmoPaymentAuthRequestParams, VenmoPaymentAuthResult>>any(),
                Mockito.any());
    }

    @Test
    public void launch_launchesAuthChallenge() throws JSONException {
        VenmoPaymentAuthRequestParams params =
                new VenmoPaymentAuthRequestParams(
                        Configuration.fromJson(Fixtures.CONFIGURATION_WITH_PAY_WITH_VENMO),
                        "profile-id", "payment-context-id", "session-id", "custom");
        ActivityResultRegistry activityResultRegistry = mock(ActivityResultRegistry.class);
        FragmentActivity lifecycleOwner = new FragmentActivity();
        VenmoLauncher sut = new VenmoLauncher(activityResultRegistry, lifecycleOwner, callback);
        sut.activityLauncher = activityResultLauncher;

        sut.launch(new VenmoPaymentAuthRequest.ReadyToLaunch(params));
        verify(activityResultLauncher).launch(params);

    }

    @Test
    public void showVenmoInGooglePlayStore_opensVenmoAppStoreURL() {
        ActivityResultRegistry activityResultRegistry = mock(ActivityResultRegistry.class);
        ComponentActivity activity = mock(ComponentActivity.class);
        VenmoLauncher sut = new VenmoLauncher(activityResultRegistry, activity, callback);

        sut.showVenmoInGooglePlayStore(activity);

        ArgumentCaptor<Intent> captor = ArgumentCaptor.forClass(Intent.class);

        verify(activity).startActivity(captor.capture());
        assertEquals(captor.getValue().getData().toString(),
                "https://play.google.com/store/apps/details?id=com.venmo");
    }
}
