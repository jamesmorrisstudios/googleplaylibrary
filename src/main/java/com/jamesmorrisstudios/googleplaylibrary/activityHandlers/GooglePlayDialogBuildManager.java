package com.jamesmorrisstudios.googleplaylibrary.activityHandlers;

import android.content.Intent;
import android.support.annotation.NonNull;

import com.google.android.gms.games.Games;
import com.jamesmorrisstudios.appbaselibrary.UtilsTheme;
import com.jamesmorrisstudios.googleplaylibrary.dialogRequests.AchievementOverlayDialogRequest;
import com.jamesmorrisstudios.googleplaylibrary.dialogRequests.CompareProfilesRequest;
import com.jamesmorrisstudios.googleplaylibrary.dialogRequests.PlayerDetailsDialogRequest;
import com.jamesmorrisstudios.googleplaylibrary.dialogs.AchievementOverlayDialogBuilder;
import com.jamesmorrisstudios.googleplaylibrary.dialogs.PlayerDetailsDialogBuilder;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlay;
import com.squareup.otto.Subscribe;

/**
 * Created by James on 4/6/2016.
 */
public class GooglePlayDialogBuildManager extends GooglePlayBaseBuildManager {

    @Subscribe
    public final void onPlayerDetailsDialogRequest(@NonNull PlayerDetailsDialogRequest request) {
        PlayerDetailsDialogBuilder builder = PlayerDetailsDialogBuilder.with(getGooglePlayActivity(), UtilsTheme.getAlertDialogStyle());
        builder.setPlayer(request.player);
        builder.build().show();
    }

    @Subscribe
    public final void onAchievementOverlayDialogRequest(@NonNull AchievementOverlayDialogRequest request) {
        AchievementOverlayDialogBuilder builder = AchievementOverlayDialogBuilder.with(getGooglePlayActivity(), UtilsTheme.getAlertDialogStyle());
        builder.setAchievement(request.item);
        builder.build().show();
    }

    @Subscribe
    public final void onCompareProfilesRequest(@NonNull CompareProfilesRequest request) {
        if (getGooglePlayActivity().isGooglePlayReady(false, false)) {
            Intent intent = Games.Players.getCompareProfileIntent(GooglePlay.getInstance().getApiClient(), request.player);
            getGooglePlayActivity().startActivityForResult(intent, 2000);
        }
    }

}
