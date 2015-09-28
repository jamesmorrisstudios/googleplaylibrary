package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.images.ImageManager;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleViewHolder;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlayCalls;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.LeaderboardMetaItem;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.LeaderboardMetaVariantItem;
import com.jamesmorrisstudios.appbaselibrary.app.AppBase;

/**
 * Created by James on 6/6/2015.
 */
public class LeaderboardMetaViewHolder extends BaseRecycleViewHolder {
    private ImageManager imageManager;

    private ImageView icon;
    private TextView title, score, rank;

    public LeaderboardMetaViewHolder(View view, boolean isHeader, boolean isDummyItem, cardClickListener mListener, ImageManager imageManager) {
        super(view, isHeader, isDummyItem, mListener);
        this.imageManager = imageManager;
    }

    @Override
    protected void initHeader(View view) {
        //Unused
    }

    @Override
    protected void initItem(View view) {
        CardView topLayout = (CardView) view.findViewById(R.id.leaderboard_meta_card);
        topLayout.setOnClickListener(this);
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
            score.setText(AppBase.getContext().getString(R.string.n_a));
            rank.setText("");
        }
    }
}
