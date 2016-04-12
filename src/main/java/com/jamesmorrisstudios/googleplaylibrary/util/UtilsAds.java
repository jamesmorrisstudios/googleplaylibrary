package com.jamesmorrisstudios.googleplaylibrary.util;

import android.support.annotation.Nullable;

import com.jamesmorrisstudios.appbaselibrary.UtilsVersion;

/**
 * AdUsage Manager.
 * <p/>
 * Created by James on 5/11/2015.
 */
public final class UtilsAds {
    private static String mopubNativeAdId;
    private static String mopubNativeAdIdFull;
    private static String mopubInterstitialAdId;
    private static String mopubRewardAdId;
    private static boolean alreadyRunning = false;
    private static long lastInterstitialShownTimeStamp = 0;

    /**
     * @return True if we are ready to show another full page ad
     */
    public static boolean allowInterstitial() {
        return !UtilsVersion.isPro() && System.currentTimeMillis() - lastInterstitialShownTimeStamp >= 1000 * 60 * 2; //2 minutes
    }

    /**
     * Updates to this being the last time a full page ad was shown
     */
    public static void updateAdShowTimeStamp() {
        lastInterstitialShownTimeStamp = System.currentTimeMillis();
    }

    /**
     * Call this from the activity onCreate function to ensure the ad manager is ready.
     */
    public static void onCreate() {
        if (!alreadyRunning) {
            updateAdShowTimeStamp();
            alreadyRunning = true;
        }
    }

    /**
     * @return The mopub native ad Id. Null if none set.
     */
    @Nullable
    public static String getMopubNativeAdId() {
        return mopubNativeAdId;
    }

    /**
     * @param mopubAdId The mopub native ad id.
     */
    public static void setMopubNativeAdId(@Nullable String mopubAdId) {
        UtilsAds.mopubNativeAdId = mopubAdId;
    }

    /**
     * @return The mopub native ad id for full view. Null if none set.
     */
    @Nullable
    public static String getMopubNativeAdIdFull() {
        return mopubNativeAdIdFull;
    }

    /**
     * @param mopubAdIdFull The mopub native ad id for full view
     */
    public static void setMopubNativeAdIdFull(@Nullable String mopubAdIdFull) {
        UtilsAds.mopubNativeAdIdFull = mopubAdIdFull;
    }

    /**
     * @return The mopub interstitial ad id. Null if none.
     */
    @Nullable
    public static String getMopubInterstitialAdId() {
        return mopubInterstitialAdId;
    }

    /**
     * @param mopubInterstitialAdId The mopub interstitial ad id.
     */
    public static void setMopubInterstitialAdId(@Nullable String mopubInterstitialAdId) {
        UtilsAds.mopubInterstitialAdId = mopubInterstitialAdId;
    }

    /**
     * @return The mopub reward ad id. Null if none.
     */
    @Nullable
    public static String getMopubRewardAdId() {
        return mopubRewardAdId;
    }

    /**
     * @param mopubRewardAdId The mopub reward ad id.
     */
    public static void setMopubRewardAdId(@Nullable String mopubRewardAdId) {
        UtilsAds.mopubRewardAdId = mopubRewardAdId;
    }

    /**
     * @return True if already initialized.
     */
    public static boolean isAlreadyRunning() {
        return alreadyRunning;
    }

}
