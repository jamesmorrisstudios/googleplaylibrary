package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.images.ImageManager;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleViewHolder;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.LeaderboardItem;

/**
 * Created by James on 6/6/2015.
 */
public class LeaderboardViewHolder extends BaseRecycleViewHolder {
    private ImageManager imageManager;

    private ImageView icon;
    private TextView name, score, rank, level;

    public LeaderboardViewHolder(View view, boolean isHeader, boolean isDummyItem, cardClickListener mListener, ImageManager imageManager) {
        super(view, isHeader, isDummyItem, mListener);
        this.imageManager = imageManager;
    }

    @Override
    protected void initHeader(View view) {
        //Unused
    }

    @Override
    protected void initItem(View view) {
        CardView topLayout = (CardView) view.findViewById(R.id.leaderboard_card);
        topLayout.setOnClickListener(this);
        icon = (ImageView) view.findViewById(R.id.leaderboard_icon);
        name = (TextView) view.findViewById(R.id.leaderboard_name);
        score = (TextView) view.findViewById(R.id.leaderboard_score);
        rank = (TextView) view.findViewById(R.id.leaderboard_rank);
        level = (TextView) view.findViewById(R.id.leaderboard_level);
    }

    @Override
    protected void bindHeader(BaseRecycleItem baseRecycleItem, boolean b) {
        //Unused
    }

    @Override
    protected void bindItem(BaseRecycleItem baseRecycleItem, boolean b) {
        LeaderboardItem item = (LeaderboardItem) baseRecycleItem;
        if(item.icon != null) {
            imageManager.loadImage(icon, item.icon, R.drawable.leaderboard_blank);
        } else {
            imageManager.loadImage(icon, R.drawable.leaderboard_blank);
        }
        name.setText(item.displayName);
        rank.setText(Long.toString(item.playerRank));
        score.setText(item.displayPlayerScore);
        level.setText(Integer.toString(item.player.getLevelInfo().getCurrentLevel().getLevelNumber()));
    }
}
