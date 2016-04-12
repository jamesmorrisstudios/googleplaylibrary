package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.images.ImageManager;
import com.jamesmorrisstudios.appbaselibrary.app.AppBase;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleViewHolder;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlayCalls;
import com.jamesmorrisstudios.googleplaylibrary.data.LeaderboardMetaItem;
import com.jamesmorrisstudios.googleplaylibrary.data.LeaderboardMetaVariantItem;

/**
 * Leaderboard Meta view holder that manages the view for all Leaderboard Meta items and headers.
 *
 * Created by James on 6/6/2015.
 */
public class LeaderboardMetaViewHolder extends BaseRecycleViewHolder {
    private ImageManager imageManager;
    private ImageView icon;
    private TextView title, score, rank;

    /**
     * Constructor
     * @param view Top View
     * @param isHeader True if header, false if item
     * @param mListener Click listener
     * @param imageManager Image manager for downloading images
     */
    public LeaderboardMetaViewHolder(@NonNull View view, boolean isHeader, @NonNull cardClickListener mListener, @NonNull ImageManager imageManager) {
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
        CardView topLayout = (CardView) view.findViewById(R.id.leaderboard_meta_card);
        topLayout.setOnClickListener(this);
        icon = (ImageView) view.findViewById(R.id.leaderboard_icon);
        title = (TextView) view.findViewById(R.id.leaderboard_title);
        score = (TextView) view.findViewById(R.id.leaderboard_score);
        rank = (TextView) view.findViewById(R.id.leaderboard_rank);
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
        LeaderboardMetaItem item = (LeaderboardMetaItem) baseRecycleItem;
        imageManager.loadImage(icon, item.imageUri);
        title.setText(item.displayName);
        LeaderboardMetaVariantItem variant = item.getVariant(GooglePlayCalls.getInstance().getLeaderboardCollection(), GooglePlayCalls.getInstance().getLeaderboardSpan());
        if (variant != null && variant.displayPlayerScore != null && variant.displayPlayerRank != null) {
            score.setText(variant.displayPlayerScore);
            rank.setText(variant.displayPlayerRank);
        } else {
            score.setText(AppBase.getContext().getString(R.string.n_a));
            rank.setText("");
        }
    }
}
