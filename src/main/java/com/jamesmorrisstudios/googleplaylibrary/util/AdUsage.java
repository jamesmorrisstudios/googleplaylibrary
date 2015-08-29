package com.jamesmorrisstudios.googleplaylibrary.util;

/**
 * Created by James on 5/11/2015.
 */
public class AdUsage {
    private static String mopubAdId, mopubAdIdFull;
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

    public static String getMopubAdId() {
        return mopubAdId;
    }

    public static String getMopubAdIdFull() {
        return mopubAdIdFull;
    }

    public static void setMopubAdId(String mopubAdId1) {
        mopubAdId = mopubAdId1;
    }

    public static void setMopubAdIdFull(String mopubAdIdFull1) {
        mopubAdIdFull = mopubAdIdFull1;
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
