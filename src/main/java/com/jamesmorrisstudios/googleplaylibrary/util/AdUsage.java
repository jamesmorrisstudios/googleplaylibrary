package com.jamesmorrisstudios.googleplaylibrary.util;

/**
 * Created by James on 5/11/2015.
 */
public class AdUsage {
    private static String mopubNativeAdId;
    private static String mopubNativeAdIdFull;
    private static String mopubInterstitialAdId;
    private static String mopubRewardAdId;
    private static boolean adsEnabled = true;
    private static boolean alreadyRunning = false;
    private static long lastInterstitialShownTimeStamp = 0;
    private static long minTimeBetween = 1000 * 60 * 2; //2 minutes

    /**
     * @return True if we are ready to show another full page ad
     */
    public static boolean allowInterstitial() {
        return adsEnabled && System.currentTimeMillis() - lastInterstitialShownTimeStamp >= minTimeBetween;
    }

    /**
     * Updates to this being the last time a full page ad was shown
     */
    public static void updateAdShowTimeStamp() {
        lastInterstitialShownTimeStamp = System.currentTimeMillis();
    }

    public static void onCreate() {
        if(!alreadyRunning) {
            updateAdShowTimeStamp();
            alreadyRunning = true;
        }
    }

    public static String getMopubNativeAdId() {
        return mopubNativeAdId;
    }

    public static String getMopubNativeAdIdFull() {
        return mopubNativeAdIdFull;
    }

    public static void setMopubNativeAdId(String mopubAdId) {
        AdUsage.mopubNativeAdId = mopubAdId;
    }

    public static void setMopubNativeAdIdFull(String mopubAdIdFull) {
        AdUsage.mopubNativeAdIdFull = mopubAdIdFull;
    }

    public static String getMopubInterstitialAdId() {
        return mopubInterstitialAdId;
    }

    public static void setMopubInterstitialAdId(String mopubInterstitialAdId) {
        AdUsage.mopubInterstitialAdId = mopubInterstitialAdId;
    }

    public static String getMopubRewardAdId() {
        return mopubRewardAdId;
    }

    public static void setMopubRewardAdId(String mopubRewardAdId) {
        AdUsage.mopubRewardAdId = mopubRewardAdId;
    }

    public static boolean isAlreadyRunning() {
        return alreadyRunning;
    }

    public static void reset() {
        lastInterstitialShownTimeStamp = 0;
    }

    public static void setAdsEnabled(boolean adsEnabled1) {
        adsEnabled = adsEnabled1;
    }

    public static boolean getAdsEnabled() {
        return adsEnabled;
    }

}
