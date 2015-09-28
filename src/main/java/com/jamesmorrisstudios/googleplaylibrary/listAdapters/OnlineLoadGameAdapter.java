package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import android.support.annotation.NonNull;
import android.view.View;

import com.google.android.gms.common.images.ImageManager;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleAdapter;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleViewHolder;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.appbaselibrary.Utils;
import com.jamesmorrisstudios.appbaselibrary.app.AppBase;

/**
 * Created by James on 8/3/2015.
 */
public class OnlineLoadGameAdapter extends BaseRecycleAdapter {

    public OnlineLoadGameAdapter(OnItemClickListener mListener) {
        super(mListener);
    }

    @Override
    protected BaseRecycleViewHolder getViewHolder(@NonNull View view, boolean b, boolean b1, BaseRecycleViewHolder.cardClickListener cardClickListener) {
        return new OnlineLoadGameViewHolder(view, b, b1, cardClickListener);
    }

    @Override
    protected int getHeaderResId() {
        return R.layout.online_load_game_header;
    }

    @Override
    protected int getItemResId() {
        return R.layout.online_load_game_item;
    }
}
