package com.jamesmorrisstudios.googleplaylibrary.activities;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.support.annotation.NonNull;
import android.support.v4.app.FragmentManager;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.jamesmorrisstudios.appbaselibrary.activities.BaseLauncherNoViewActivity;
import com.jamesmorrisstudios.appbaselibrary.fragments.SettingsFragment;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.googleplaylibrary.fragments.GooglePlaySettingsFragment;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlay;
import com.jamesmorrisstudios.googleplaylibrary.util.AdUsage;
import com.jamesmorrisstudios.googleplaylibrary.util.IabHelper;
import com.jamesmorrisstudios.googleplaylibrary.util.IabResult;
import com.jamesmorrisstudios.googleplaylibrary.util.Inventory;
import com.jamesmorrisstudios.googleplaylibrary.util.Purchase;
import com.jamesmorrisstudios.utilitieslibrary.Bus;
import com.jamesmorrisstudios.utilitieslibrary.Logger;
import com.jamesmorrisstudios.utilitieslibrary.Utils;
import com.squareup.otto.Subscribe;

/**
 * Created by James on 5/11/2015.
 */
public abstract class BaseAdLauncherActivity extends BaseLauncherNoViewActivity implements
    GooglePlaySettingsFragment.OnGooglePlaySettingsListener {
    private static final String TAG = "BaseAdLauncherActivity";

    private static final String REMOVE_ADS_SKU = "android.test.purchased";

    //ad management
    private AdView mAdView;
    private InterstitialAd mInterstitialAd = null;
    private Handler handler = new Handler();
    private int retryCount = 0;
    private boolean retryRunning = false;

    private final Object busListener = new Object() {
        @Subscribe
        public void onSettingEvent(final GooglePlay.GooglePlayEvent event) {
            BaseAdLauncherActivity.this.onGooglePlayEvent(event);
        }
    };

    protected IInAppBillingService mService;
    protected IabHelper mHelper;

    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name,
                                       IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(AdUsage.isAlreadyRunning()) {
            if(AdUsage.getAdsEnabled()) {
                setContentView(R.layout.layout_main_ad);
                startIABHelper();
            } else {
                setContentView(R.layout.layout_main);
            }
        } else {
            setContentView(R.layout.layout_main_ad);
            startIABHelper();
            AdUsage.onCreate();
        }
        initOnCreate();
    }

    private void startIABHelper() {
        //Start the IAP service connection
        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
        // compute your public key and store it in base64EncodedPublicKey
        mHelper = new IabHelper(this, getPublicKey());
        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.d(TAG, "Problem setting up In-app Billing: " + result);
                }
                Log.d(TAG, "In app billing is setup and working: " + result);
                mHelper.queryInventoryAsync(false, mGotInventoryListener);
            }
        });
    }

    protected abstract String getPublicKey();

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (!mHelper.handleActivityResult(requestCode, resultCode, data)) {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                // Handle error
                Utils.toastShort("Failed to purchase item");
            } else if (purchase.getSku().equals(REMOVE_ADS_SKU)) {
                Utils.toastShort("Ads Removed");
                hideAds();
            }
        }
    };

    // Get already purchased response
    private IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (result.isFailure()) {
                // handle error here
                Log.v("TAG", "Error checking inventory: " + result);
                initAds();
            } else {
                // does the user have the premium upgrade?
                if(inventory.hasPurchase(REMOVE_ADS_SKU)) {
                    Log.v("TAG", "onQueryInventoryFinished GOT A RESPONSE YES Premium");
                    hideAds();
                } else {
                    Log.v("TAG", "onQueryInventoryFinished GOT A RESPONSE No Premium");
                    initAds();
                }

            }
        }
    };

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onStart() {
        super.onStart();
        Bus.register(busListener);
    }

    @Override
    public void onStop() {
        super.onStop();
        Bus.unregister(busListener);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (mService != null) {
            unbindService(mServiceConn);
        }
        if (mHelper != null) {
            mHelper.dispose();
        }
        mHelper = null;
    }

    private void initAds() {
        Log.v("TAG", "Showing ads");
        AdUsage.setAdsEnabled(true);
        initBannerAd();
        if(getResources().getBoolean(R.bool.interstitial_enable)) {
            initInterstitialAd();
        }
    }

    private void hideAds() {
        Log.v("TAG", "Hiding ads");
        AdUsage.setAdsEnabled(false);
        if(mAdView != null) {
            mAdView.pause();
            //LinearLayout adContainer = (LinearLayout) findViewById(R.id.adViewContainer);
            //adContainer.removeAllViews();
            //adContainer.requestLayout();
            Log.v("TAG", "Ad container gone");
            //LinearLayout toolbarContainer = (LinearLayout) findViewById(R.id.toolbarContainer);
            //toolbarContainer.requestLayout();
            //FrameLayout container = (FrameLayout) findViewById(R.id.container);
            //container.requestLayout();
        }
    }

    @Override
    public void purchaseRemoveAds() {
        mHelper.launchPurchaseFlow(this, REMOVE_ADS_SKU, 10001, mPurchaseFinishedListener, "REMOVE_ADS_PURCHASE_TOKEN");
    }

    public final void onGooglePlayEvent(GooglePlay.GooglePlayEvent event) {
        switch(event) {
            case SHOW_INTERSTITIAL:
                showInterstitialAd();
                break;
        }
    }

    private void initBannerAd() {
        mAdView = (AdView) findViewById(R.id.adView);
        if (mAdView != null) {
            // Create an ad request. Check logcat output for the hashed device ID to
            // get test ads on a physical device. e.g.
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("1DF30F3FB16CD733C2937A5531598396") //Nexus 5
                    .addTestDevice("5FE0C6962C9C4F8DD6F30B9B11CC0E42") //transformer prime
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR) //Emulator
                    .build();

            // Start loading the ad in the background.
            mAdView.resume();
            mAdView.loadAd(adRequest);
        }
    }

    private void initInterstitialAd() {
        mInterstitialAd = new InterstitialAd(getApplicationContext());
        mInterstitialAd.setAdUnitId(getResources().getString(R.string.interstitial_id));
        mInterstitialAd.setAdListener(new AdListener() {
            @Override
            public void onAdClosed() {
                super.onAdClosed();
                requestNewInterstitial();
            }
        });
        requestNewInterstitial();
    }

    /**
     * Request an interstitial ad be loaded (not shown)
     */
    private void requestNewInterstitial() {
        if (mInterstitialAd != null) {
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("1DF30F3FB16CD733C2937A5531598396") //Nexus 5
                    .addTestDevice("5FE0C6962C9C4F8DD6F30B9B11CC0E42") //transformer prime
                    .build();

            mInterstitialAd.loadAd(adRequest);
        }
    }

    /**
     * Show the interstitial ad if we have one loaded and retry if not
     */
    protected final void showInterstitialAd() {
        //Make sure we are using the interstitial ad and that its loaded
        Log.v(TAG, "Requested interstitial");
        if (mInterstitialAd == null) {
            Log.v(TAG, "No interstitial loaded");
            return;
        }
        //See if its too shown to show another ad
        if(!AdUsage.allowInterstitial()) {
            Log.v(TAG, "Not enough time since last shown an add");
            return;
        }
        //Try and load the ad or retry as needed
        if (mInterstitialAd.isLoaded()) {
            Logger.v(Logger.LoggerCategory.MAIN, TAG, "Showing the interstitial ad");
            mInterstitialAd.show();
            AdUsage.updateAdShowTimeStamp();
        } else {
            if (!retryRunning) {
                Logger.v(Logger.LoggerCategory.MAIN, TAG, "interstitial ad not ready. Starting retry timer");
                initRetry();
            } else {
                retry();
                Logger.v(Logger.LoggerCategory.MAIN, TAG, "interstitial ad not ready. Retry Count: " + retryCount);
            }
        }
    }

    /**
     * Increment our retries
     */
    private void retry() {
        retryCount++;
        if (retryCount <= 3) {
            handler.postDelayed(retryRun, 1000);
        } else {
            retryRunning = false;
        }
    }

    /**
     * Start retrying to show the ad
     */
    private void initRetry() {
        retryCount = 0;
        retryRunning = true;
        handler.postDelayed(retryRun, 1000);
    }

    /**
     * Runnable that handles retries
     */
    @NonNull
    private Runnable retryRun = new Runnable() {
        public void run() {
            showInterstitialAd();
        }
    };

    /**
     * Gets the help fragment from the fragment manager.
     * Creates the fragment if it does not exist yet.
     *
     * @return The fragment
     */
    @Override @NonNull
    protected GooglePlaySettingsFragment getSettingsFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        GooglePlaySettingsFragment fragment = (GooglePlaySettingsFragment) fragmentManager.findFragmentByTag(SettingsFragment.TAG);
        if (fragment == null) {
            fragment = new GooglePlaySettingsFragment();
        }
        return fragment;
    }

}
