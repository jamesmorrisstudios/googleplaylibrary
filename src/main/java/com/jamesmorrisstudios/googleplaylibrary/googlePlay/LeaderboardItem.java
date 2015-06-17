package com.jamesmorrisstudios.googleplaylibrary.googlePlay;

import android.net.Uri;
import android.support.annotation.NonNull;

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
    public final long playerScore;

    public LeaderboardItem(@NonNull String displayName, @NonNull Uri icon, @NonNull String displayPlayerRank, @NonNull String displayPlayerScore, long playerRank, long playerScore) {
        this.displayName = displayName;
        this.icon = icon;
        this.displayPlayerRank = displayPlayerRank;
        this.displayPlayerScore = displayPlayerScore;
        this.playerRank = playerRank;
        this.playerScore = playerScore;
    }
}
