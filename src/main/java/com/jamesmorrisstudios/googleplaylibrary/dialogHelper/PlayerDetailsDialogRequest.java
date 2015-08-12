package com.jamesmorrisstudios.googleplaylibrary.dialogHelper;

import android.support.annotation.NonNull;

import com.google.android.gms.games.Player;

/**
 * Created by James on 8/11/2015.
 */
public class PlayerDetailsDialogRequest {
    public final Player player;

    public PlayerDetailsDialogRequest(@NonNull Player player) {
        this.player = player;
    }

}
