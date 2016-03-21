package com.jamesmorrisstudios.googleplaylibrary.googlePlay;

import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;

import java.util.ArrayList;

/**
 * The metadata for an individual ic_leaderboard
 *
 * Created by James on 6/6/2015.
 */
public class LeaderboardMetaItem extends BaseRecycleItem {
    public final String displayName;
    public final Uri imageUri;
    public final String leaderboardId;
    public final ArrayList<LeaderboardMetaVariantItem> variants;

    public LeaderboardMetaItem(@NonNull String displayName, @NonNull Uri imageUri, @NonNull String leaderboardId, @NonNull ArrayList<LeaderboardMetaVariantItem> variants) {
        this.displayName = displayName;
        this.imageUri = imageUri;
        this.leaderboardId = leaderboardId;
        this.variants = variants;
    }

    @Nullable
    public final LeaderboardMetaVariantItem getVariant(@NonNull GooglePlay.Collection collection, @NonNull GooglePlay.Span span) {
        for(LeaderboardMetaVariantItem item : variants) {
            if(item.collection == collection && item.span == span) {
                return item;
            }
        }
        return null;
    }

    public final void updateVariant(@NonNull GooglePlay.Collection collection, @NonNull GooglePlay.Span span, @NonNull String displayPlayerRank, @NonNull String displayPlayerScore, long playerRank, long playerScore) {
        LeaderboardMetaVariantItem item = getVariant(collection, span);
        if(item != null) {
            item.updateScore(displayPlayerRank, displayPlayerScore, playerRank, playerScore);
        }
    }

}
