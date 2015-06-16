package com.jamesmorrisstudios.googleplaylibrary.googlePlay;

import android.support.annotation.NonNull;

/**
 * Created by James on 6/6/2015.
 */
public class LeaderboardMetaVariantItem {
    public final GooglePlay.Collection collection;
    public final GooglePlay.Span span;
    public final long numberScores;
    public String displayPlayerRank;
    public String displayPlayerScore;
    public long playerRank;
    public long playerScore;

    public void updateScore(@NonNull String displayPlayerRank, @NonNull String displayPlayerScore, long playerRank, long playerScore) {
        this.displayPlayerRank = displayPlayerRank;
        this.displayPlayerScore = displayPlayerScore;
        this.playerRank = playerRank;
        this.playerScore = playerScore;
    }

    public LeaderboardMetaVariantItem(@NonNull GooglePlay.Collection collection, @NonNull GooglePlay.Span span, long numberScores) {
        this.collection = collection;
        this.span = span;
        this.numberScores = numberScores;
        this.displayPlayerRank = null;
        this.displayPlayerScore = null;
        this.playerRank = -1;
        this.playerScore = -1;
    }

}
