package com.jamesmorrisstudios.googleplaylibrary.dialogRequests;

import android.support.annotation.NonNull;

import com.google.android.gms.games.Player;

/**
 * Created by James on 8/11/2015.
 */
public class CompareProfilesRequest {
    public final Player player;

    public CompareProfilesRequest(@NonNull Player player) {
        this.player = player;
    }

}
