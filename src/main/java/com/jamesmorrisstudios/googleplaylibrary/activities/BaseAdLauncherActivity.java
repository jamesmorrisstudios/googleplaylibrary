package com.jamesmorrisstudios.googleplaylibrary.activities;

import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.NonNull;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.InterstitialAd;
import com.jamesmorrisstudios.appbaselibrary.activities.BaseLauncherNoViewActivity;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.googleplaylibrary.utilites.AdUsage;
import com.jamesmorrisstudios.utilitieslibrary.Logger;

/**
 * Created by James on 5/11/2015.
 */
public abstract class BaseAdLauncherActivity extends BaseLauncherNoViewActivity {
    private static final String TAG = "BaseAdLauncherActivity";

    //ad management
    private AdView mAdView;
    private InterstitialAd mInterstitialAd = null;
    private Handler handler = new Handler();
    private int retryCount = 0;
    private boolean retryRunning = false;

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_main_ad);
        initBannerAd();
        if(getResources().getBoolean(R.bool.interstitial_enable)) {
            initInterstitialAd();
        }
        initOnCreate();
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
        if (mInterstitialAd == null) {
            return;
        }
        //See if its too shown to show another ad
        if(!AdUsage.allowInterstitial()) {
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

}