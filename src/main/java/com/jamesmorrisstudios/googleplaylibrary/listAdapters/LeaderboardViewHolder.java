package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.images.ImageManager;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleViewHolder;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.googleplaylibrary.data.LeaderboardItem;

/**
 * Leaderboard view holder that manages the view for all Leaderboard items and headers.
 *
 * Created by James on 6/6/2015.
 */
public class LeaderboardViewHolder extends BaseRecycleViewHolder {
    private ImageManager imageManager;
    private ImageView icon;
    private TextView name, score, rank, level;

    /**
     * Constructor
     * @param view Top View
     * @param isHeader True if header, false if item
     * @param mListener Click listener
     * @param imageManager Image manager for downloading images
     */
    public LeaderboardViewHolder(@NonNull View view, boolean isHeader, @NonNull cardClickListener mListener, @NonNull ImageManager imageManager) {
        super(view, isHeader, mListener);
        this.imageManager = imageManager;
    }

    /**
     * UNUSED
     * Init the header view
     * @param view Top header view
     */
    @Override
    protected void initHeader(@NonNull View view) {

    }

    /**
     * Init the item view
     * @param view Top item view
     */
    @Override
    protected void initItem(@NonNull View view) {
        CardView topLayout = (CardView) view.findViewById(R.id.leaderboard_card);
        topLayout.setOnClickListener(this);
        icon = (ImageView) view.findViewById(R.id.leaderboard_icon);
        name = (TextView) view.findViewById(R.id.leaderboard_name);
        score = (TextView) view.findViewById(R.id.leaderboard_score);
        rank = (TextView) view.findViewById(R.id.leaderboard_rank);
        level = (TextView) view.findViewById(R.id.leaderboard_level);
    }

    /**
     * UNUSED
     * Bind the header data
     * @param baseRecycleItem Base header data
     * @param expanded True if expanded form, false if normal
     */
    @Override
    protected void bindHeader(@NonNull BaseRecycleItem baseRecycleItem, boolean expanded) {

    }

    /**
     * Bind the item data
     * @param baseRecycleItem Base item data
     * @param expanded True if expanded form, false if normal
     */
    @Override
    protected void bindItem(@NonNull BaseRecycleItem baseRecycleItem, boolean expanded) {
        LeaderboardItem item = (LeaderboardItem) baseRecycleItem;
        if (item.icon != null) {
            imageManager.loadImage(icon, item.icon, R.drawable.ic_player);
        } else {
            imageManager.loadImage(icon, R.drawable.ic_player);
        }
        name.setText(item.displayName);
        rank.setText(Long.toString(item.playerRank));
        score.setText(item.displayPlayerScore);
        level.setText(Integer.toString(item.player.getLevelInfo().getCurrentLevel().getLevelNumber()));
    }
}
