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

import com.android.vending.billing.IInAppBillingService;
import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.games.Games;
import com.jamesmorrisstudios.appbaselibrary.activities.BaseLauncherNoViewActivity;
import com.jamesmorrisstudios.appbaselibrary.fragments.SettingsFragment;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.googleplaylibrary.fragments.AchievementsFragment;
import com.jamesmorrisstudios.googleplaylibrary.fragments.BaseGooglePlayFragment;
import com.jamesmorrisstudios.googleplaylibrary.fragments.BaseGooglePlayMainFragment;
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
import com.jamesmorrisstudios.utilitieslibrary.preferences.Prefs;
import com.squareup.otto.Subscribe;

/**
 * Created by James on 5/11/2015.
 */
public abstract class BaseAdLauncherActivity extends BaseLauncherNoViewActivity implements
        GooglePlaySettingsFragment.OnGooglePlaySettingsListener,
        GooglePlay.GameHelperListener,
        BaseGooglePlayFragment.OnGooglePlayListener,
        BaseGooglePlayMainFragment.OnGooglePlayListener {
    private static final String TAG = "BaseAdLauncherActivity";
    private static final String REMOVE_ADS_SKU = "remove_ads_1";

    private boolean playServicesEnabled = false;

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
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode == ConnectionResult.SUCCESS){
            playServicesEnabled = true;
        }
        if(playServicesEnabled) {
            if(AdUsage.isAlreadyRunning()) {
                //App is already open
                if(AdUsage.getAdsEnabled()) {
                    //Ads enabled
                    setContentView(R.layout.layout_main_ad);
                    enableAds();
                } else {
                    //Ads disabled
                    setContentView(R.layout.layout_main);
                    disableAds();
                }
            } else {
                //First launch
                AdUsage.onCreate();
                //Check the cache if ads are enabled or not. If the user used root to change this we will
                //overwrite it after checking the IAP helper
                if(getCacheEnableAds()) {
                    //Ads Enabled
                    setContentView(R.layout.layout_main_ad);
                    enableAds();
                } else {
                    //Ads Disabled
                    setContentView(R.layout.layout_main);
                    disableAds();
                }
            }
            startIABHelper();
            GooglePlay.getInstance().init(this, GooglePlay.CLIENT_GAMES);
            if (GooglePlay.getInstance().isFirstLaunch()) {
                GooglePlay.getInstance().setup(this);
                GooglePlay.getInstance().setHasLaunched();
            }
        } else {
            //Ads Disabled as we have no Google Play Services
            setContentView(R.layout.layout_main);
            disableAds();
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
        GooglePlay.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                // Handle error
                Utils.toastShort("Failed to purchase item");
            } else if (purchase.getSku().equals(REMOVE_ADS_SKU)) {
                //TODO inspect that the payload and signature match!
                Prefs.putString(getResources().getString(R.string.settings_pref), "ORDERID", purchase.getOrderId());
                Utils.toastShort("Ads Removed");
                disableAds();
                restartActivity();
            }
        }
    };

    // Get already purchased response
    private IabHelper.QueryInventoryFinishedListener mGotInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (result.isFailure()) {
                // handle error here
                Log.v("TAG", "Error checking inventory: " + result);
                if(!AdUsage.getAdsEnabled()) {
                    enableAds();
                    restartActivity();
                }
            } else {
                // does the user have the premium upgrade?
                if(inventory.hasPurchase(REMOVE_ADS_SKU)) {
                    Log.v("TAG", "onQueryInventoryFinished GOT A RESPONSE YES Premium");
                    if(AdUsage.getAdsEnabled()) {
                        disableAds();
                        restartActivity();
                    }
                } else {
                    Log.v("TAG", "onQueryInventoryFinished GOT A RESPONSE No Premium");
                    if(!AdUsage.getAdsEnabled()) {
                        enableAds();
                        restartActivity();
                    }
                }
            }
        }
    };

    private void restartActivity() {
        Intent i = getBaseContext().getPackageManager().getLaunchIntentForPackage(getBaseContext().getPackageName());
        i.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(i);
    }

    @Override
    public void onResume() {
        super.onResume();

    }

    @Override
    public void onStart() {
        super.onStart();
        Bus.register(busListener);
        if(playServicesEnabled) {
            if (!GooglePlay.getInstance().isSignedIn()) {
                GooglePlay.getInstance().onStart(this);
            }
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        Bus.unregister(busListener);
    }

    @Override
      public void onDestroy() {
        super.onDestroy();
        if(playServicesEnabled) {
            if (mService != null) {
                unbindService(mServiceConn);
            }
            if (mHelper != null) {
                mHelper.dispose();
            }
            mHelper = null;
        }
    }

    private void enableAds() {
        Log.v("TAG", "Showing ads");
        Prefs.putBoolean(getResources().getString(R.string.settings_pref), "ENABLED", true);
        AdUsage.setAdsEnabled(true);
        //Init Banner
        mAdView = (AdView) findViewById(R.id.adView);
        if (mAdView != null) {
            // Create an ad request. Check logcat output for the hashed device ID to
            // get test ads on a physical device. e.g.
            AdRequest adRequest = new AdRequest.Builder()
                    .addTestDevice("9C2F1643B5D281A922A7275B214895BD") //Nexus 5 Android M
                    .addTestDevice("5FE0C6962C9C4F8DD6F30B9B11CC0E42") //transformer prime
                    .addTestDevice(AdRequest.DEVICE_ID_EMULATOR) //Emulator
                    .build();

            // Start loading the ad in the background.
            mAdView.resume();
            mAdView.loadAd(adRequest);
        }
        //Init Interstitial
        if(getResources().getBoolean(R.bool.interstitial_enable)) {
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
    }

    private void disableAds() {
        Log.v("TAG", "Hiding ads");
        Prefs.putBoolean(getResources().getString(R.string.settings_pref), "ENABLED", false);
        AdUsage.setAdsEnabled(false);
        if(mAdView != null) {
            mAdView.pause();
        }
    }

    @Override
    public void purchaseRemoveAds() {
        Utils.lockOrientationCurrent(this);
        mHelper.launchPurchaseFlow(this, REMOVE_ADS_SKU, 10001, new IabHelper.OnIabPurchaseFinishedListener() {
            public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                if (result.isFailure()) {
                    // Handle error
                    Utils.toastShort("Failed to purchase item");
                } else if (purchase.getSku().equals(REMOVE_ADS_SKU)) {
                    //TODO inspect that the payload and signature match!
                    Prefs.putString(getResources().getString(R.string.settings_pref), "ORDERID", purchase.getOrderId());
                    Utils.toastShort("Ads Removed");
                    disableAds();
                    restartActivity();
                }
                Utils.unlockOrientation(BaseAdLauncherActivity.this);
            }
        }, "REMOVE_ADS_PURCHASE_TOKEN");
    }

    @Override
    public void testingConsumePurchase() {
        Utils.toastShort("Testing consume purchase");
        enableAds();
        consumeItem();
    }

    public void consumeItem() {
        mHelper.queryInventoryAsync(mReceivedInventoryListener);
    }

    IabHelper.QueryInventoryFinishedListener mReceivedInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (result.isFailure()) {
                // Handle failure
            } else {
                mHelper.consumeAsync(inventory.getPurchase(REMOVE_ADS_SKU), mConsumeFinishedListener);
            }
        }
    };

    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (result.isSuccess()) {
                restartActivity();
            } else {
                // handle error
            }
        }
    };

    private boolean getCacheEnableAds() {
        return Prefs.getBoolean(getResources().getString(R.string.settings_pref), "ENABLED", true);
    }

    public final void onGooglePlayEvent(GooglePlay.GooglePlayEvent event) {
        switch(event) {
            case SHOW_INTERSTITIAL:
                showInterstitialAd();
                break;
        }
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

    /**
     * Gets the achievements fragment from the fragment manager.
     * Creates the fragment if it does not exist yet.
     *
     * @return The fragment
     */
    @NonNull
    protected final AchievementsFragment getAchievementsFragment() {
        FragmentManager fragmentManager = getSupportFragmentManager();
        AchievementsFragment fragment = (AchievementsFragment) fragmentManager.findFragmentByTag(AchievementsFragment.TAG);
        if (fragment == null) {
            fragment = new AchievementsFragment();
        }
        return fragment;
    }

    /**
     * Loads the achievements fragment into the main view
     */
    protected final void loadAchievementsFragment() {
        AchievementsFragment fragment = getAchievementsFragment();
        loadFragment(fragment, AchievementsFragment.TAG, true);
        getSupportFragmentManager().executePendingTransactions();
    }

    /**
     * Loads the leaderboard fragment into the main view
     */
    protected final void loadLeaderboardsFragment() {
        if(GooglePlay.getInstance().isSignedIn()) {
            Intent intent = Games.Leaderboards.getAllLeaderboardsIntent(GooglePlay.getInstance().getApiClient());
            startActivityForResult(intent, 1234);
        }
    }

    @Override
    public void onSignInFailed() {
        Log.v("Activity", "Sign in failed: ");// + GooglePlay.getInstance().getSignInError().getActivityResultCode()+" "+GooglePlay.getInstance().getSignInError().getServiceErrorCode());
        Bus.postEnum(GooglePlay.GooglePlayEvent.SIGN_IN_FAIL);
    }

    @Override
    public void onSignInSucceeded() {
        Log.v("Activity", "Sign in Succeeded");
        Bus.postEnum(GooglePlay.GooglePlayEvent.SIGN_IN_SUCCESS);
        Games.setViewForPopups(GooglePlay.getInstance().getApiClient(), findViewById(R.id.toolbarContainer));
    }

    @Override
    public final boolean isGooglePlayServicesEnabled() {
        return playServicesEnabled;
    }

}
