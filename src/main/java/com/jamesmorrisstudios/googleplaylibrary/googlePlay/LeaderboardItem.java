package com.jamesmorrisstudios.googleplaylibrary.googlePlay;

import android.net.Uri;
import android.os.Parcel;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.google.android.gms.games.Player;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;

/**
 * Created by James on 6/4/2015.
 */
public class LeaderboardItem extends BaseRecycleItem {
    public final String displayName;
    public final Uri icon;
    public final String displayPlayerRank;
    public final String displayPlayerScore;
    public final long playerRank;
    public final Player player;

    public LeaderboardItem(@NonNull String displayName, @Nullable Uri icon, @NonNull String displayPlayerRank, @NonNull String displayPlayerScore, long playerRank, @NonNull Player player) {
        this.displayName = displayName;
        this.icon = icon;
        this.displayPlayerRank = displayPlayerRank;
        this.displayPlayerScore = displayPlayerScore;
        this.playerRank = playerRank;
        this.player = player;
    }
}
