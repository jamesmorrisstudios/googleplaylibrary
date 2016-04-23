package com.jamesmorrisstudios.googleplaylibrary.activities;

import android.app.Activity;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.annotation.CallSuper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.ActionBar;
import android.support.v7.widget.AppCompatSpinner;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import com.android.vending.billing.IInAppBillingService;
import com.chartboost.sdk.Chartboost;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.multiplayer.Multiplayer;
import com.google.android.gms.games.multiplayer.realtime.RoomConfig;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatch;
import com.google.android.gms.games.multiplayer.turnbased.TurnBasedMatchConfig;
import com.google.android.gms.games.snapshot.SnapshotMetadata;
import com.google.android.gms.games.snapshot.Snapshots;
import com.jamesmorrisstudios.appbaselibrary.AutoLockOrientation;
import com.jamesmorrisstudios.appbaselibrary.Bus;
import com.jamesmorrisstudios.appbaselibrary.UtilsDisplay;
import com.jamesmorrisstudios.appbaselibrary.UtilsVersion;
import com.jamesmorrisstudios.appbaselibrary.activities.BaseActivity;
import com.jamesmorrisstudios.appbaselibrary.activityHandlers.SnackbarRequest;
import com.jamesmorrisstudios.appbaselibrary.app.AppBase;
import com.jamesmorrisstudios.appbaselibrary.preferences.Prefs;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.googleplaylibrary.activityHandlers.GooglePlayActivityResultManager;
import com.jamesmorrisstudios.googleplaylibrary.activityHandlers.GooglePlayDialogBuildManager;
import com.jamesmorrisstudios.googleplaylibrary.fragments.AchievementFragment;
import com.jamesmorrisstudios.googleplaylibrary.fragments.LeaderboardFragment;
import com.jamesmorrisstudios.googleplaylibrary.fragments.LeaderboardMetaFragment;
import com.jamesmorrisstudios.googleplaylibrary.fragments.OnlineLoadGameFragment;
import com.jamesmorrisstudios.googleplaylibrary.fragments.PlayerPickerFragment;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GameHelper;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlay;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlayCalls;
import com.jamesmorrisstudios.googleplaylibrary.iab.IabHelper;
import com.jamesmorrisstudios.googleplaylibrary.iab.IabResult;
import com.jamesmorrisstudios.googleplaylibrary.iab.Inventory;
import com.jamesmorrisstudios.googleplaylibrary.iab.Purchase;
import com.jamesmorrisstudios.googleplaylibrary.util.UtilsAds;
import com.mopub.common.MoPub;
import com.mopub.common.MoPubReward;
import com.mopub.mobileads.MoPubErrorCode;
import com.mopub.mobileads.MoPubInterstitial;
import com.mopub.mobileads.MoPubRewardedVideoListener;

import java.util.ArrayList;
import java.util.Set;

/**
 * Activity that handles all the google play services calls and ad handling
 * <p/>
 * Created by James on 5/11/2015.
 */
public abstract class GooglePlayActivity extends BaseActivity implements
        GooglePlay.GooglePlayListener,
        GameHelper.GameHelperListener,
        LeaderboardMetaFragment.OnLeaderboardMetaListener,
        LeaderboardFragment.OnLeaderboardListener,
        MoPubInterstitial.InterstitialAdListener {

    //Constants
    private final static String UPGRADE_PRO_SKU = "remove_ads_1";
    private final static int RC_LOOK_AT_MATCHES = 10000;
    private final static int RC_LOOK_AT_SNAPSHOTS = 10001;
    private final static int RC_SELECT_PLAYERS = 11000;
    // The game helper object. This class is mainly a wrapper around this object.
    protected GameHelper mHelper;
    // We expose these constants here because we don't want users of this class
    // to have to know about GameHelper at all.
    public static final int CLIENT_GAMES = GameHelper.CLIENT_GAMES;
    public static final int CLIENT_SNAPSHOT = GameHelper.CLIENT_SNAPSHOT;
    public static final int CLIENT_ALL = GameHelper.CLIENT_ALL;
    // Requested clients. By default, that's just the games client.
    protected boolean mDebugLog = true;
    //In app billing
    protected IInAppBillingService mService;
    protected IabHelper iabHelper;
    //Activity handlers
    GooglePlayDialogBuildManager dialogBuildManager;
    GooglePlayActivityResultManager activityResultManager;
    //In app billing connection listener
    ServiceConnection mServiceConn = new ServiceConnection() {
        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            mService = IInAppBillingService.Stub.asInterface(service);
        }
    };
    IabHelper.OnConsumeFinishedListener consumeProFinishedListener = new IabHelper.OnConsumeFinishedListener() {
        public void onConsumeFinished(Purchase purchase, IabResult result) {
            if (result.isSuccess()) {
                restartApp();
            } else {
                // handle error
            }
        }
    };
    //In app billing consume listener
    IabHelper.QueryInventoryFinishedListener consumeProInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (result.isFailure()) {
                // Handle failure
            } else {
                iabHelper.consumeAsync(inventory.getPurchase(UPGRADE_PRO_SKU), consumeProFinishedListener);
            }
        }
    };
    //Current Play Services enable state
    private boolean playServicesEnabled = false;
    //Leaderboard toolbar spinners
    private AppCompatSpinner spinnerSpan;
    private AppCompatSpinner spinnerCollection;
    //In app billing purchase listener
    private IabHelper.QueryInventoryFinishedListener purchaseProInventoryListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (result.isFailure()) {
                Log.v("TAG", "Error checking inventory: " + result);
                if (UtilsVersion.isPro()) {
                    UtilsVersion.updatePro(false);
                    restartApp();
                }
            } else {
                // does the user have the premium upgrade?
                if (inventory.hasPurchase(UPGRADE_PRO_SKU)) {
                    Log.v("TAG", "onQueryInventoryFinished GOT A RESPONSE YES Premium");
                    if (!UtilsVersion.isPro()) {
                        UtilsVersion.updatePro(true);
                        restartApp();
                    }
                } else {
                    Log.v("TAG", "onQueryInventoryFinished GOT A RESPONSE No Premium");
                    if (UtilsVersion.isPro()) {
                        UtilsVersion.updatePro(false);
                        restartApp();
                    }
                }
            }
        }
    };
    //Ads handling
    private MoPubInterstitial mInterstitial;
    private MoPubRewardedVideoListener rewardedVideoListener = new MoPubRewardedVideoListener() {
        @Override
        public void onRewardedVideoLoadSuccess(@NonNull String adUnitId) {
            rewardAdCached();
        }

        @Override
        public void onRewardedVideoLoadFailure(@NonNull String adUnitId, @NonNull MoPubErrorCode errorCode) {

        }

        @Override
        public void onRewardedVideoStarted(@NonNull String adUnitId) {

        }

        @Override
        public void onRewardedVideoPlaybackError(@NonNull String adUnitId, @NonNull MoPubErrorCode errorCode) {

        }

        @Override
        public void onRewardedVideoClosed(@NonNull String adUnitId) {
            Log.v("Chartboost", "Reward Ad Closed");
            //UtilsDisplay.resetImmersiveMode(GooglePlayActivity.this, true);
            cacheRewardAd();
        }

        @Override
        public void onRewardedVideoCompleted(@NonNull Set<String> adUnitIds, @NonNull MoPubReward reward) {
            if (reward.isSuccessful()) {
                Log.v("Chartboost", "Reward: " + reward);
                rewardAdWatched(reward.getAmount());
                GooglePlay.AdEvent.REWARD_AD_WATCHED.setAmount(reward.getAmount()).post();
            }
        }
    };

    /**
     * @return The app public google from google play dev consol
     */
    @NonNull
    protected abstract String getPublicKey();

    /**
     * @return The bitwise state of google api clients to use
     */
    protected abstract int getGooglePlayClients();

    /**
     * @return Chartboost app id
     */
    @Nullable
    protected abstract String getChartboostAppId();

    /**
     * @return Chartboost app Signature
     */
    @Nullable
    protected abstract String getChartboostAppSignature();

    /**
     * @return Native ad ID for small format
     */
    @Nullable
    protected abstract String getMopubNativeAdId();

    /**
     * @return Native ad ID for large (full) format
     */
    @Nullable
    protected abstract String getMopubNativeAdIdFull();

    /**
     * @return Ad ID of the interstitial ad
     */
    @Nullable
    protected abstract String getMopubInterstitialAdId();

    /**
     * @return A ID of the reward video ad
     */
    @Nullable
    protected abstract String getMopubRewardAdId();

    /**
     * Reward ad was watched fully
     *
     * @param reward Reward amount
     */
    protected abstract void rewardAdWatched(int reward);

    /**
     * Reward ad has been cached and is ready to play
     */
    protected abstract void rewardAdCached();

    /**
     * onCreateComplete for all normal onCreate work
     *
     * @param savedInstanceState saved app instance state
     */
    @Override
    @CallSuper
    protected void onCreateComplete(Bundle savedInstanceState) {
        super.onCreateComplete(savedInstanceState);
        //Add all the fragments specific to this library
        addFragment(AchievementFragment.TAG, AchievementFragment.class, AchievementFragment.TAG_MAIN_FRAGMENT);
        addFragment(LeaderboardFragment.TAG, LeaderboardFragment.class, LeaderboardFragment.TAG_MAIN_FRAGMENT);
        addFragment(LeaderboardMetaFragment.TAG, LeaderboardMetaFragment.class, LeaderboardMetaFragment.TAG_MAIN_FRAGMENT);
        addFragment(OnlineLoadGameFragment.TAG, OnlineLoadGameFragment.class, OnlineLoadGameFragment.TAG_MAIN_FRAGMENT);
        addFragment(PlayerPickerFragment.TAG, PlayerPickerFragment.class, PlayerPickerFragment.TAG_MAIN_FRAGMENT);

        dialogBuildManager = new GooglePlayDialogBuildManager();
        activityResultManager = new GooglePlayActivityResultManager();
        dialogBuildManager.attach(this);
        activityResultManager.attach(this);

        //Set ad ids
        initAdIds();
        MoPub.onCreate(this);
        Chartboost.startWithAppId(this, getChartboostAppId(), getChartboostAppSignature());
        Chartboost.onCreate(GooglePlayActivity.this);
        //Check if google play services are available
        int resultCode = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (resultCode == ConnectionResult.SUCCESS) {
            playServicesEnabled = true;
            if (!UtilsAds.isAlreadyRunning()) {
                UtilsAds.onCreate();
            }
            if (!UtilsVersion.isPro()) {
                enableAds();
            } else {
                disableAds();
            }
            startIABHelper();
            if (mHelper == null) {
                getGameHelper();
            }
            mHelper.setup(this);
        } else {
            //Ads still work without google play services but the user cant remove them with an IAP
            enableAds();
        }
        initSpinners();
    }

    @Override
    public void onStart() {
        super.onStart();
        if (playServicesEnabled) {
            mHelper.setConnectOnStart(getPlayGamesEnabledPref());
            mHelper.onStart(this);
            GooglePlayCalls.getInstance().attach(this);
        }
        MoPub.onStart(this);
        Chartboost.onStart(GooglePlayActivity.this);
    }

    @Override
    public void onResume() {
        super.onResume();
        MoPub.onResume(this);
        Chartboost.onResume(GooglePlayActivity.this);
        cacheRewardAd();
        if (!UtilsVersion.isPro()) {
            cacheInterstitial();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        MoPub.onPause(this);
        Chartboost.onPause(GooglePlayActivity.this);
    }

    @Override
    public void onStop() {
        super.onStop();
        if (playServicesEnabled) {
            mHelper.onStop();
            GooglePlayCalls.getInstance().detach();
        }
        MoPub.onStop(this);
        Chartboost.onStop(GooglePlayActivity.this);
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (playServicesEnabled) {
            if (mService != null) {
                unbindService(mServiceConn);
            }
            if (iabHelper != null) {
                iabHelper.dispose();
            }
            iabHelper = null;
        }
        if (mInterstitial != null) {
            mInterstitial.destroy();
        }
        MoPub.onDestroy(this);
        Chartboost.onDestroy(GooglePlayActivity.this);
        dialogBuildManager.detach();
        activityResultManager.detach();
    }

    @Override
    public void onRestart() {
        super.onRestart();
        Log.v("GooglePlayActivity", "onRestart");
        MoPub.onRestart(this);
    }

    @Override
    public void onBackPressed() {
        if (!Chartboost.onBackPressed()) {
            super.onBackPressed();
            MoPub.onBackPressed(this);
        }
    }

    @NonNull
    public final GameHelper getGameHelper() {
        if (mHelper == null) {
            mHelper = new GameHelper(this, getGooglePlayClients());
            mHelper.enableDebugLog(mDebugLog);
        }
        return mHelper;
    }

    /**
     * Setup the spinners for use on the leaderboards. They are hidden unless the leaderboard displays them.
     */
    private void initSpinners() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar == null) {
            return;
        }
        spinnerSpan = (AppCompatSpinner) findViewById(R.id.spinner_left);
        spinnerCollection = (AppCompatSpinner) findViewById(R.id.spinner_right);
        final ArrayAdapter spinnerTimesAdapter = ArrayAdapter.createFromResource(actionBar.getThemedContext(), R.array.leaderboard_span, R.layout.simple_spinner_title_item);
        final ArrayAdapter spinnerCollectionAdapter = ArrayAdapter.createFromResource(actionBar.getThemedContext(), R.array.leaderboard_collection, R.layout.simple_spinner_title_item);
        spinnerTimesAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinnerCollectionAdapter.setDropDownViewResource(R.layout.support_simple_spinner_dropdown_item);
        spinnerSpan.setAdapter(spinnerTimesAdapter);
        spinnerCollection.setAdapter(spinnerCollectionAdapter);
        spinnerSpan.setSelection(GooglePlayCalls.getInstance().getLeaderboardSpan().ordinal(), false);
        spinnerCollection.setSelection(GooglePlayCalls.getInstance().getLeaderboardCollection().ordinal(), false);
    }

    /**
     * Update the visibility status of the spinners
     *
     * @param visible True for visible, false for hidden
     */
    @Override
    public void setSpinnerVisibility(boolean visible) {
        if (visible) {
            spinnerSpan.setVisibility(View.VISIBLE);
            spinnerCollection.setVisibility(View.VISIBLE);
            spinnerSpan.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    switch (spinnerSpan.getSelectedItemPosition()) {
                        case 0:
                            GooglePlayCalls.getInstance().setLeaderboardSpan(GooglePlay.Span.DAILY);
                            break;
                        case 1:
                            GooglePlayCalls.getInstance().setLeaderboardSpan(GooglePlay.Span.WEEKLY);
                            break;
                        case 2:
                            GooglePlayCalls.getInstance().setLeaderboardSpan(GooglePlay.Span.ALL_TIME);
                            break;
                        default:
                            GooglePlayCalls.getInstance().setLeaderboardSpan(GooglePlay.Span.ALL_TIME);
                            break;
                    }
                    Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARD_SPINNER_CHANGE);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
            spinnerCollection.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                    switch (spinnerCollection.getSelectedItemPosition()) {
                        case 0:
                            GooglePlayCalls.getInstance().setLeaderboardCollection(GooglePlay.Collection.SOCIAL);
                            break;
                        case 1:
                            GooglePlayCalls.getInstance().setLeaderboardCollection(GooglePlay.Collection.PUBLIC);
                            break;
                        default:
                            GooglePlayCalls.getInstance().setLeaderboardCollection(GooglePlay.Collection.PUBLIC);
                            break;
                    }
                    Bus.postEnum(GooglePlay.GooglePlayEvent.LEADERBOARD_SPINNER_CHANGE);
                }

                @Override
                public void onNothingSelected(AdapterView<?> parent) {

                }
            });
        } else {
            spinnerSpan.setOnItemSelectedListener(null);
            spinnerCollection.setOnItemSelectedListener(null);
            spinnerSpan.setVisibility(View.GONE);
            spinnerCollection.setVisibility(View.GONE);
        }
    }

    /**
     * Setup in app billing and upon success query if the user purchased the pro upgrade
     */
    private void startIABHelper() {
        //Start the IAP service connection
        Intent serviceIntent = new Intent("com.android.vending.billing.InAppBillingService.BIND");
        serviceIntent.setPackage("com.android.vending");
        bindService(serviceIntent, mServiceConn, Context.BIND_AUTO_CREATE);
        // compute your public key and store it in base64EncodedPublicKey
        iabHelper = new IabHelper(this, getPublicKey());
        iabHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.d("BaseAdLauncherActivity", "Problem setting up In-app Billing: " + result);
                    iabHelper = null;
                    return;
                }
                Log.d("BaseAdLauncherActivity", "In app billing is setup and working: " + result);
                iabHelper.queryInventoryAsync(false, purchaseProInventoryListener);
            }
        });
    }

    /**
     * Set all the ad ids. Some (or all) of these may be null
     */
    private void initAdIds() {
        UtilsAds.setMopubNativeAdId(getMopubNativeAdId());
        UtilsAds.setMopubNativeAdIdFull(getMopubNativeAdIdFull());
        UtilsAds.setMopubInterstitialAdId(getMopubInterstitialAdId());
        UtilsAds.setMopubRewardAdId(getMopubRewardAdId());
    }

    /**
     * Enable display of ads in app
     */
    private void enableAds() {
        Log.v("BaseAdLauncherActivity", "Showing ads");
        initInterstitialAd();
        initRewardVideoAd();
    }

    /**
     * Disable display of ads in app.
     * Reward video ads are enabled anyway.
     */
    private void disableAds() {
        Log.v("BaseAdLauncherActivity", "Hiding ads");
        initRewardVideoAd();
    }

    /**
     * Init the reward video ad
     */
    private void initRewardVideoAd() {
        if (UtilsAds.getMopubRewardAdId() != null) {
            MoPub.initializeRewardedVideo(this);
            MoPub.setRewardedVideoListener(rewardedVideoListener);
        }
    }

    /**
     * Init and cache the interstitial ad
     */
    private void initInterstitialAd() {
        if (UtilsAds.getMopubInterstitialAdId() != null) {
            mInterstitial = new MoPubInterstitial(this, UtilsAds.getMopubInterstitialAdId());
            mInterstitial.setInterstitialAdListener(this);
            cacheInterstitial();
        }
    }

    /**
     * Show the interstitial ad if we have one loaded.
     * Make no attempt to retry.
     */
    public final void showInterstitialAd() {
        if (!UtilsAds.allowInterstitial()) {
            Log.v("BaseAdLauncherActivity", "Not enough time since last shown an ad");
            return;
        }
        Log.v("BaseAdLauncherActivity", "Requested interstitial");
        if (hasCachedInterstitial()) {
            mInterstitial.show();
            UtilsAds.updateAdShowTimeStamp();
        }
    }

    /**
     * @return True if we have an interstitial cached and ready.
     */
    private boolean hasCachedInterstitial() {
        return mInterstitial.isReady();
    }

    /**
     * Begin caching an interstitial ad for use
     */
    private void cacheInterstitial() {
        mInterstitial.load();
    }

    /**
     * Begin caching a reward ad for use.
     *
     * @return true if caching has begun. false if error.
     */
    private boolean cacheRewardAd() {
        String adId = UtilsAds.getMopubRewardAdId();
        if (adId != null) {
            MoPub.loadRewardedVideo(adId);
            return true;
        }
        return false;
    }

    /**
     * @return True if the reward ad is cached and ready.
     */
    private boolean hasCachedRewardAd() {
        String adId = UtilsAds.getMopubRewardAdId();
        return adId != null && MoPub.hasRewardedVideo(adId);
    }

    /**
     * Shows the reward ad if it is ready/
     *
     * @return True if ad is displayed. False if it is not ready.
     */
    public final boolean showRewardAd() {
        String adId = UtilsAds.getMopubRewardAdId();
        if (hasCachedRewardAd() && adId != null) {
            Log.v("GooglePlayActivity", "Cached: Showing Reward Ad");
            MoPub.showRewardedVideo(adId);
            return true;
        } else {
            new SnackbarRequest(getString(R.string.ad_unavailable), SnackbarRequest.SnackBarDuration.LONG).execute();
        }
        Log.v("GooglePlayActivity", "NOT Cached: Skipping Reward Ad");
        return false;
    }

    /**
     * Interstitial ad has been loaded
     *
     * @param interstitial Interstitial ad
     */
    @Override
    public void onInterstitialLoaded(@NonNull MoPubInterstitial interstitial) {

    }

    /**
     * Failed to display interstitial ad
     *
     * @param interstitial Interstitial ad
     * @param errorCode    Error Code
     */
    @Override
    public void onInterstitialFailed(@NonNull MoPubInterstitial interstitial, @NonNull MoPubErrorCode errorCode) {

    }

    /**
     * Interstitial ad shown
     *
     * @param interstitial Interstitial ad
     */
    @Override
    public void onInterstitialShown(@NonNull MoPubInterstitial interstitial) {

    }

    /**
     * Interstitial ad clicked
     *
     * @param interstitial Interstitial ad
     */
    @Override
    public void onInterstitialClicked(@NonNull MoPubInterstitial interstitial) {

    }

    /**
     * Interstitial ad dismissed
     *
     * @param interstitial Interstitial ad
     */
    @Override
    public void onInterstitialDismissed(@NonNull MoPubInterstitial interstitial) {
        cacheInterstitial();
    }

    /**
     * Start the purchase ad removal
     */
    public final void purchaseProUpgrade() {
        if (UtilsVersion.isPro()) {
            new SnackbarRequest(AppBase.getContext().getString(R.string.pro_unlocked), SnackbarRequest.SnackBarDuration.SHORT).execute();
            return;
        }
        if (iabHelper == null) {
            new SnackbarRequest(AppBase.getContext().getString(R.string.error), SnackbarRequest.SnackBarDuration.SHORT).execute();
            return;
        }
        AutoLockOrientation.enableAutoLock(this);
        iabHelper.launchPurchaseFlow(this, UPGRADE_PRO_SKU, 10001, new IabHelper.OnIabPurchaseFinishedListener() {
            public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
                if (result.isSuccess() && purchase.getSku().equals(UPGRADE_PRO_SKU) && purchase.getDeveloperPayload().equals("REMOVE_ADS_PURCHASE_TOKEN")) {
                    Prefs.putString(getResources().getString(R.string.settings_pref), "ORDERID", purchase.getOrderId());
                    new SnackbarRequest(AppBase.getContext().getString(R.string.ads_removed), SnackbarRequest.SnackBarDuration.SHORT).execute();
                    UtilsVersion.updatePro(true);
                    restartApp();
                } else {
                    // Handle error
                    new SnackbarRequest(AppBase.getContext().getString(R.string.failed), SnackbarRequest.SnackBarDuration.SHORT).execute();
                }
                AutoLockOrientation.disableAutoLock(GooglePlayActivity.this);
            }
        }, "REMOVE_ADS_PURCHASE_TOKEN");
    }

    /**
     * Use With CARE!
     * This will reset the pro upgrade and put the user back on the free tier.
     * It cannot be undone without purchasing pro again.
     */
    public final void consumeProUpgrade() {
        if (iabHelper != null) {
            iabHelper.queryInventoryAsync(consumeProInventoryListener);
        }
    }

    /**
     * Override the normal pro upgrade handler to kick off the in app purchase
     */
    @Override
    public void upgradeToPro() {
        if (UtilsVersion.isPro()) {
            new SnackbarRequest(AppBase.getContext().getString(com.jamesmorrisstudios.appbaselibrary.R.string.pro_unlocked), SnackbarRequest.SnackBarDuration.SHORT).execute();
        } else {
            purchaseProUpgrade();
        }
    }

    /**
     * Checks if google play and google play games are signed in and ready to use
     *
     * @param tryToSignIn         True to attempt sign in if not signed in already.
     * @param showStatusOnFailure True to display an error code if Google Play Services are unavailable.
     * @return True if ready, false otherwise
     */
    public final boolean isGooglePlayReady(boolean tryToSignIn, boolean showStatusOnFailure) {
        if (playServicesEnabled) {
            if(isSignedIn()) {
                return true;
            }
            getGameHelper();
            beginUserInitiatedSignIn();
        } else if (showStatusOnFailure) {
            new SnackbarRequest(AppBase.getContext().getString(R.string.requires_google_play), SnackbarRequest.SnackBarDuration.SHORT).execute();
        }
        return false;
    }

    /**
     * @return True if the user has signed into Google Play Games in the past.
     */
    private boolean getPlayGamesEnabledPref() {
        return Prefs.getBoolean(getString(R.string.settings_pref), "PlayGamesEnabled", true);
    }

    /**
     * @param playGamesEnabled True to enable Google Play Games
     */
    private void setPlayGamesEnabledPref(boolean playGamesEnabled) {
        Prefs.putBoolean(getString(R.string.settings_pref), "PlayGamesEnabled", playGamesEnabled);
    }

    /**
     * Sign in to Google Play Games failed.
     * Sets the pref to prevent retrying.
     */
    @Override
    public final void onSignInFailed() {
        Log.v("Activity", "Sign in failed: ");// + GooglePlay.getInstance().getSignInError().getActivityResultCode()+" "+GooglePlay.getInstance().getSignInError().getServiceErrorCode());
        Bus.postEnum(GooglePlay.GooglePlayEvent.SIGN_IN_FAIL);
        setPlayGamesEnabledPref(false);
    }

    /**
     * Sign in to Google Play Games succeeded.
     * Set the pref to auto sign in in the future.
     */
    @Override
    public final void onSignInSucceeded() {
        Log.v("Activity", "Sign in Succeeded");
        Bus.postEnum(GooglePlay.GooglePlayEvent.SIGN_IN_SUCCESS);
        View view = findViewById(R.id.coordinatorLayout);
        if(view != null) {
            Games.setViewForPopups(getApiClient(), view);
        }
        setPlayGamesEnabledPref(true);
    }

    /**
     * Activity result handler.
     *
     * @param requestCode Request code
     * @param resultCode  Result code
     * @param data        Intent Data
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Check if the iabHelper (in app billing) consumes the result.
        if (iabHelper != null) {
            iabHelper.handleActivityResult(requestCode, resultCode, data);
        }
        //Check if the google play login uses the result
        if(mHelper != null) {
            mHelper.onActivityResult(requestCode, resultCode, data);
        }
        //Check for page callbacks and results. TODO These will be removed once native pages have been created.
        if (requestCode == RC_LOOK_AT_MATCHES) {
            if (data == null || resultCode != Activity.RESULT_OK) {
                Bus.postEnum(GooglePlay.GooglePlayEvent.SELECT_LOAD_MATCH_ONLINE_FAIL);
            } else {
                loadMatchOnline((TurnBasedMatch) data.getParcelableExtra(Multiplayer.EXTRA_TURN_BASED_MATCH));
            }
        } else if (requestCode == RC_LOOK_AT_SNAPSHOTS) {
            if (data == null || resultCode != Activity.RESULT_OK) {
                Bus.postEnum(GooglePlay.GooglePlayEvent.SELECT_LOAD_MATCH_LOCAL_FAIL);
            } else {
                loadMatchLocal((SnapshotMetadata) data.getParcelableExtra(Snapshots.EXTRA_SNAPSHOT_METADATA));
            }
        } else if (requestCode >= RC_SELECT_PLAYERS && requestCode <= RC_SELECT_PLAYERS + 100) {
            if (data == null || resultCode != Activity.RESULT_OK) {
                Bus.postEnum(GooglePlay.GooglePlayEvent.SELECT_PLAYERS_ONLINE_FAIL);
            } else {
                selectPlayersOnline(data, requestCode - RC_SELECT_PLAYERS);
            }
        }
    }

    //TODO
    private void selectPlayersOnline(@NonNull Intent data, int variant) {
        Log.v("BaseActivity", "Select players online");

        // get the invitee list
        final ArrayList<String> invitees = data.getStringArrayListExtra(Games.EXTRA_PLAYER_IDS);

        // get automatch criteria
        Bundle autoMatchCriteria = null;

        int minAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MIN_AUTOMATCH_PLAYERS, 0);
        int maxAutoMatchPlayers = data.getIntExtra(Multiplayer.EXTRA_MAX_AUTOMATCH_PLAYERS, 0);

        if (minAutoMatchPlayers > 0) {
            autoMatchCriteria = RoomConfig.createAutoMatchCriteria(minAutoMatchPlayers, maxAutoMatchPlayers, 0);
        } else {
            autoMatchCriteria = null;
        }

        TurnBasedMatchConfig matchConfig = TurnBasedMatchConfig.builder()
                .setVariant(variant)
                .addInvitedPlayers(invitees)
                .setAutoMatchCriteria(autoMatchCriteria).build();

        GooglePlayCalls.getInstance().startMatchOnline(matchConfig);
    }

    //TODO
    private void loadMatchLocal(@Nullable SnapshotMetadata snapshotMetadata) {
        if (snapshotMetadata != null) {
            Log.v("Activity", "Load Local");
            GooglePlayCalls.getInstance().loadGameLocal(snapshotMetadata.getUniqueName());
        } else {
            Bus.postEnum(GooglePlay.GooglePlayEvent.SELECT_LOAD_MATCH_LOCAL_FAIL);
        }
    }

    //TODO
    private void loadMatchOnline(@Nullable TurnBasedMatch match) {
        if (match != null) {
            if (match.getData() == null) {
                //This is a rematch
                Log.v("Activity", "Load online Rematch");
            } else {
                //We are loading an existing match
                Log.v("Activity", "Load online Existing");
            }
        } else {
            Bus.postEnum(GooglePlay.GooglePlayEvent.SELECT_LOAD_MATCH_ONLINE_FAIL);
        }
    }

    /**
     * Loads the leaderboard meta fragment into the main view
     */
    public final boolean loadLeaderboardMetaFragment(String[] leaderboardIds) {
        GooglePlayCalls.getInstance().clearLeaderboardsMetaCache();
        GooglePlayCalls.getInstance().clearLeaderboardsCache();
        if (isGooglePlayReady(true, true)) {
            Bundle bundle = new Bundle();
            bundle.putStringArray("leaderboardIds", leaderboardIds);
            loadFragment(LeaderboardMetaFragment.TAG, bundle);
            return true;
        }
        return false;
    }

    /**
     * Loads the leaderboard fragment into the main view
     */
    @Override
    public final boolean loadLeaderboardFragment(String leaderboardId) {
        GooglePlayCalls.getInstance().clearLeaderboardsCache();
        if (isGooglePlayReady(true, true)) {
            Bundle bundle = new Bundle();
            bundle.putString("leaderboardId", leaderboardId);
            loadFragment(LeaderboardFragment.TAG, bundle);
            return true;
        }
        return false;
    }

    /**
     * Loads the achievements fragment into the main view
     */
    public final boolean loadAchievementFragment(String[] achievementIds) {
        GooglePlayCalls.getInstance().clearAchievementsCache();
        if (isGooglePlayReady(true, true)) {
            Bundle bundle = new Bundle();
            bundle.putStringArray("achievementIds", achievementIds);
            loadFragment(AchievementFragment.TAG, bundle);
            return true;
        }
        return false;
    }

    /**
     * Loads the online load game fragment into the main view
     */
    public final boolean loadOnlineLoadGameFragment() {
        if (isGooglePlayReady(true, true)) {
            loadFragment(OnlineLoadGameFragment.TAG);
            //startActivityForResult(Games.TurnBasedMultiplayer.getInboxIntent(GooglePlay.getInstance().getApiClient()), RC_LOOK_AT_MATCHES);
            return true;
        }
        return false;
    }

    //Variant max of 100 (normally 1)
    public final boolean loadPlayerPickerFragment(int minPlayers, int maxPlayers, boolean allowAutomatch, int variant) {
        if (isGooglePlayReady(true, true)) {
            //startActivityForResult(Games.TurnBasedMultiplayer.getSelectOpponentsIntent(GooglePlay.getInstance().getApiClient(), minPlayers, maxPlayers, allowAutomatch), RC_SELECT_PLAYERS + variant);
            loadFragment(PlayerPickerFragment.TAG);
            return true;
        }
        return false;
    }

    public final boolean loadOfflineInboxFragment() {
        if (isGooglePlayReady(true, true)) {
            //startActivityForResult(Games.Snapshots.getSelectSnapshotIntent(GooglePlay.getInstance().getApiClient(), getString(R.string.select_save), false, true, Snapshots.DISPLAY_LIMIT_NONE), RC_LOOK_AT_SNAPSHOTS);
            return true;
        }
        return false;
    }

    @CallSuper
    @Override
    public boolean onNavigationItemSelected(@NonNull final MenuItem item) {
        if(item.getItemId() == R.id.navigation_remove_ads) {
            item.setChecked(false);
            if(UtilsVersion.isPro()) {
                new SnackbarRequest(getString(R.string.ads_removed), SnackbarRequest.SnackBarDuration.SHORT).execute();
            } else {
                upgradeToPro();
            }
        } else if(item.getItemId() == R.id.navigation_sign_in) {
            item.setChecked(false);
            Log.v("GooglePlayActivity", "Sign In");
            beginUserInitiatedSignIn();
        } else if(item.getItemId() == R.id.navigation_sign_out) {
            item.setChecked(false);
            Log.v("GooglePlayActivity", "Sign Out");
            if(isGooglePlayReady(false, false)) {
                signOut();
            }
        } else if(item.getItemId() == R.id.navigation_achievements_all) {
            item.setChecked(false);
            if(isGooglePlayReady(true, true)) {
                loadAchievementFragment(getResources().getStringArray(R.array.achievements_all));
            }
        } else if(item.getItemId() == R.id.navigation_achievements_offline) {
            item.setChecked(false);
            if(isGooglePlayReady(true, true)) {
                loadAchievementFragment(getResources().getStringArray(R.array.achievements_offline));
            }
        } else if(item.getItemId() == R.id.navigation_achievements_online) {
            item.setChecked(false);
            if(isGooglePlayReady(true, true)) {
                loadAchievementFragment(getResources().getStringArray(R.array.achievements_online));
            }
        } else if(item.getItemId() == R.id.navigation_leaderboards_all) {
            item.setChecked(false);
            if(isGooglePlayReady(true, true)) {
                loadLeaderboardMetaFragment(getResources().getStringArray(R.array.leaderboards_all));
            }
        } else if(item.getItemId() == R.id.navigation_leaderboards_offline) {
            item.setChecked(false);
            if(isGooglePlayReady(true, true)) {
                loadLeaderboardMetaFragment(getResources().getStringArray(R.array.leaderboards_offline));
            }
        } else if(item.getItemId() == R.id.navigation_leaderboards_online) {
            item.setChecked(false);
            if(isGooglePlayReady(true, true)) {
                loadLeaderboardMetaFragment(getResources().getStringArray(R.array.leaderboards_online));
            }
        }
        return super.onNavigationItemSelected(item);
    }

    protected GoogleApiClient getApiClient() {
        return mHelper.getApiClient();
    }

    protected boolean isSignedIn() {
        return mHelper.isSignedIn();
    }

    protected void beginUserInitiatedSignIn() {
        mHelper.beginUserInitiatedSignIn();
    }

    protected void signOut() {
        mHelper.signOut();
        new SnackbarRequest(AppBase.getContext().getString(R.string.signed_out), SnackbarRequest.SnackBarDuration.SHORT).execute();
        GooglePlay.GooglePlayEvent.SIGN_OUT.post();
    }

    protected void showAlert(String message) {
        mHelper.makeSimpleDialog(message).show();
    }

    protected void showAlert(String title, String message) {
        mHelper.makeSimpleDialog(title, message).show();
    }

    protected void enableDebugLog(boolean enabled) {
        mDebugLog = true;
        if (mHelper != null) {
            mHelper.enableDebugLog(enabled);
        }
    }

    @Deprecated
    protected void enableDebugLog(boolean enabled, String tag) {
        Log.w("GooglePlayActivity", "BaseGameActivity.enabledDebugLog(bool,String) is " +
                "deprecated. Use enableDebugLog(boolean)");
        enableDebugLog(enabled);
    }

    protected String getInvitationId() {
        return mHelper.getInvitationId();
    }

    protected void reconnectClient() {
        mHelper.reconnectClient();
    }

    protected boolean hasSignInError() {
        return mHelper.hasSignInError();
    }

    protected GameHelper.SignInFailureReason getSignInError() {
        return mHelper.getSignInError();
    }

}
