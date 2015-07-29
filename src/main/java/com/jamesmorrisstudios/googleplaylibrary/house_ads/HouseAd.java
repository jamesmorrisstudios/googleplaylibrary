package com.jamesmorrisstudios.googleplaylibrary.house_ads;

import android.support.annotation.DrawableRes;
import android.support.annotation.StringRes;

/**
 * Created by James on 7/24/2015.
 */
public class HouseAd {
    @DrawableRes public final int logoRes;
    @StringRes public final int titleRes;
    @StringRes public final int textRes;
    @StringRes public final int cost;
    @StringRes public final int packageRes;

    public HouseAd(@DrawableRes final int logoRes, @StringRes final int titleRes, @StringRes final int textRes, @StringRes final int cost, @StringRes final int packageRes) {
        this.logoRes = logoRes;
        this.titleRes = titleRes;
        this.textRes = textRes;
        this.cost = cost;
        this.packageRes = packageRes;
    }
}
