package com.jamesmorrisstudios.googleplaylibrary.data;

import android.support.annotation.NonNull;

import com.google.android.gms.games.Player;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;

/**
 * Created by James on 8/11/2015.
 */
public class PlayerPickerItem extends BaseRecycleItem {
    public final Player player;

    public PlayerPickerItem(@NonNull Player player) {
        this.player = player;
    }

}
