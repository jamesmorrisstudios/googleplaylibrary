package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.Games;
import com.google.android.gms.games.Player;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleViewHolder;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlay;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlayCalls;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.LeaderboardMetaItem;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.LeaderboardMetaVariantItem;

/**
 * Created by James on 6/6/2015.
 */
public class LeaderboardMetaViewHolder extends BaseRecycleViewHolder {
    private ImageManager imageManager;

    private ImageView icon;
    private TextView title, score, rank;

    public LeaderboardMetaViewHolder(View view, boolean isHeader, cardClickListener mListener, ImageManager imageManager) {
        super(view, isHeader, mListener);
        this.imageManager = imageManager;
    }

    @Override
    protected void initHeader(View view) {
        //Unused
    }

    @Override
    protected void initItem(View view) {
        icon = (ImageView) view.findViewById(R.id.leaderboard_icon);
        title = (TextView) view.findViewById(R.id.leaderboard_title);
        score = (TextView) view.findViewById(R.id.leaderboard_score);
        rank = (TextView) view.findViewById(R.id.leaderboard_rank);
    }

    @Override
    protected void bindHeader(BaseRecycleItem baseRecycleItem, boolean b) {
        //Unused
    }

    @Override
    protected void bindItem(BaseRecycleItem baseRecycleItem, boolean b) {
        LeaderboardMetaItem item = (LeaderboardMetaItem) baseRecycleItem;
        imageManager.loadImage(icon, item.imageUri);
        title.setText(item.displayName);
        LeaderboardMetaVariantItem variant = item.getVariant(GooglePlayCalls.getInstance().getLeaderboardCollection(), GooglePlayCalls.getInstance().getLeaderboardSpan());
        if(variant != null && variant.displayPlayerScore != null && variant.displayPlayerRank != null) {
            score.setText(variant.displayPlayerScore);
            rank.setText(variant.displayPlayerRank);
        } else {
            score.setText("N/A");
            rank.setText("");
        }
    }
}
