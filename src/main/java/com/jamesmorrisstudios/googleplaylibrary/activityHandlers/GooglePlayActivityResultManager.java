package com.jamesmorrisstudios.googleplaylibrary.activityHandlers;

import android.util.Log;

import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlay;
import com.squareup.otto.Subscribe;

/**
 * Created by James on 4/6/2016.
 */
public class GooglePlayActivityResultManager extends GooglePlayBaseBuildManager {

    @Subscribe
    public void onSettingEvent(final GooglePlay.GooglePlayEvent event) {
        switch (event) {
            case SHOW_INTERSTITIAL:
                Log.v("BaseAdLauncherActivity", "Showing interstitial ad");
                getGooglePlayActivity().showInterstitialAd();
                break;
            case SHOW_REWARD_AD:
                Log.v("BaseAdLauncherActivity", "Showing reward ad");
                getGooglePlayActivity().showRewardAd();
                break;
        }
    }

}
