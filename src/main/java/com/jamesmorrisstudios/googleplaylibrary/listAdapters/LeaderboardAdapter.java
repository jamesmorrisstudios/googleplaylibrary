package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import android.support.annotation.NonNull;
import android.view.View;

import com.google.android.gms.common.images.ImageManager;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleAdapter;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleViewHolder;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.appbaselibrary.app.AppBase;

/**
 * Created by James on 6/6/2015.
 */
public class LeaderboardAdapter extends BaseRecycleAdapter {
    private ImageManager imageManager;

    public LeaderboardAdapter(OnRecycleAdapterEventsListener mListener) {
        super(mListener);
        imageManager = ImageManager.create(AppBase.getContext());
    }

    @Override
    protected BaseRecycleViewHolder getViewHolder(@NonNull View view, boolean isHeader, BaseRecycleViewHolder.cardClickListener cardClickListener) {
        return new LeaderboardViewHolder(view, isHeader, cardClickListener, imageManager);
    }

    @Override
    protected int getHeaderResId() {
        return R.layout.leaderboard_item;
    }

    @Override
    protected int getItemResId() {
        return R.layout.leaderboard_item;
    }

}
