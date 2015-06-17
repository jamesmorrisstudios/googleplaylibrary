package com.jamesmorrisstudios.googleplaylibrary.util;

import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.utilitieslibrary.app.AppUtil;
import com.jamesmorrisstudios.utilitieslibrary.preferences.Prefs;

/**
 * Created by James on 5/11/2015.
 */
public class AdUsage {
    private static boolean adsEnabled = true;
    private static boolean bannerAdHide = false;
    private static boolean alreadyRunning = false;
    private static long lastInterstitialShownTimeStamp = 0;
    private static long minTimeBetweenRare = AppUtil.getContext().getResources().getInteger(R.integer.interstitial_timeout_rare) * 1000;
    private static long minTimeBetweenCommon = AppUtil.getContext().getResources().getInteger(R.integer.interstitial_timeout_common) * 1000;

    /**
     * @return True if we are ready to show another full page ad
     */
    public static boolean allowInterstitial() {
        if(bannerAdHide) {
            return adsEnabled && System.currentTimeMillis() - lastInterstitialShownTimeStamp >= minTimeBetweenCommon;
        } else {
            return adsEnabled && System.currentTimeMillis() - lastInterstitialShownTimeStamp >= minTimeBetweenRare;
        }
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
            updateHideBanner();
        }
    }

    public static void updateHideBanner() {
        String pref = AppUtil.getContext().getString(R.string.settings_pref);
        String keyHideBanner = AppUtil.getContext().getString(R.string.pref_hide_banner);
        bannerAdHide = Prefs.getBoolean(pref, keyHideBanner, false);
        if(bannerAdHide) {
            lastInterstitialShownTimeStamp = 0;
        }
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

    public static boolean getBannerAdHide() {
        return bannerAdHide;
    }

}
