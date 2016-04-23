package com.jamesmorrisstudios.googleplaylibrary.activityHandlers;

import android.support.annotation.NonNull;
import android.util.Log;

import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlay;
import com.squareup.otto.Subscribe;

/**
 * Created by James on 4/6/2016.
 */
public class GooglePlayActivityResultManager extends GooglePlayBaseBuildManager {

    @Subscribe
    public void onAdEvent(@NonNull final GooglePlay.AdEvent event) {
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
