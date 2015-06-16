package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import android.support.annotation.NonNull;
import android.view.View;

import com.google.android.gms.common.images.ImageManager;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleAdapter;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleViewHolder;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.utilitieslibrary.app.AppUtil;

/**
 * Created by James on 6/6/2015.
 */
public class LeaderboardMetaAdapter extends BaseRecycleAdapter {
    private ImageManager imageManager;

    public LeaderboardMetaAdapter(int headerMode, OnItemClickListener mListener) {
        super(headerMode, mListener);
        imageManager = ImageManager.create(AppUtil.getContext());
    }

    @Override
    protected BaseRecycleViewHolder getViewHolder(@NonNull View view, boolean b, BaseRecycleViewHolder.cardClickListener cardClickListener) {
        return new LeaderboardMetaViewHolder(view, b, cardClickListener, imageManager);
    }

    @Override
    protected int getHeaderResId() {
        return R.layout.leaderboard_meta_item;
    }

    @Override
    protected int getItemResId() {
        return R.layout.leaderboard_meta_item;
    }

}
