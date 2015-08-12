package com.jamesmorrisstudios.googleplaylibrary.googlePlay;

import android.support.annotation.NonNull;

import com.google.android.gms.games.Player;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;

/**
 * Created by James on 8/11/2015.
 */
public class PlayerItem extends BaseRecycleItem {
    public final Player player;

    public PlayerItem(@NonNull Player player) {
        this.player = player;
    }

}
