package com.jamesmorrisstudios.googleplaylibrary.utilites;

import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.utilitieslibrary.app.AppUtil;

/**
 * Created by James on 5/11/2015.
 */
public class AdUsage {
    private static long lastInterstitialShownTimeStamp = 0;
    private static long minTimeBetween = AppUtil.getContext().getResources().getInteger(R.integer.interstitial_timeout) * 1000;

    /**
     * @return True if we are ready to show another full page ad
     */
    public static boolean allowInterstitial() {
        return System.currentTimeMillis() - lastInterstitialShownTimeStamp >= minTimeBetween;
    }

    /**
     * Updates to this being the last time a full page ad was shown
     */
    public static void updateAdShowTimeStamp() {
        lastInterstitialShownTimeStamp = System.currentTimeMillis();
    }

    public static void reset() {
        lastInterstitialShownTimeStamp = 0;
    }

}
