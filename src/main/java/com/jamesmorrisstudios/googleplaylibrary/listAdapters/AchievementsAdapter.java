package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import android.support.annotation.NonNull;
import android.view.View;

import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleAdapter;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleViewHolder;
import com.jamesmorrisstudios.googleplaylibrary.R;

/**
 * Created by James on 5/27/2015.
 */
public class AchievementsAdapter extends BaseRecycleAdapter {

    public AchievementsAdapter(int headerMode, OnItemClickListener mListener) {
        super(headerMode, mListener);
    }

    @Override
    protected BaseRecycleViewHolder getViewHolder(@NonNull View view, boolean b, BaseRecycleViewHolder.cardClickListener cardClickListener) {
        return new AchievementsViewHolder(view, b, cardClickListener);
    }

    @Override
    protected int getHeaderResId() {
        return R.layout.achievements_header;
    }

    @Override
    protected int getItemResId() {
        return R.layout.achievements_item;
    }
}
