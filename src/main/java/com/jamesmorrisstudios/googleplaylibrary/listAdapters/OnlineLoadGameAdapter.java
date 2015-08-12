package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import android.support.annotation.NonNull;
import android.view.View;

import com.google.android.gms.common.images.ImageManager;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleAdapter;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleViewHolder;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.utilitieslibrary.Utils;
import com.jamesmorrisstudios.utilitieslibrary.app.AppUtil;

/**
 * Created by James on 8/3/2015.
 */
public class OnlineLoadGameAdapter extends BaseRecycleAdapter {


    public OnlineLoadGameAdapter(int headerMode, OnItemClickListener mListener) {
        super(headerMode, mListener);
    }

    @Override
    protected BaseRecycleViewHolder getViewHolder(@NonNull View view, boolean b, boolean b1, BaseRecycleViewHolder.cardClickListener cardClickListener) {
        return new OnlineLoadGameViewHolder(view, b, b1, cardClickListener);
    }

    @Override
    protected int getColumnWidth() {
        return Utils.getDipInt(300); //TODO at least 288 depending upon timestamp style
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
