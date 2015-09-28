package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import android.support.annotation.NonNull;
import android.view.View;

import com.google.android.gms.common.images.ImageManager;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleAdapter;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleViewHolder;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.appbaselibrary.app.AppBase;

/**
 * Created by James on 8/11/2015.
 */
public class PlayerPickerAdapter extends BaseRecycleAdapter {
    private ImageManager imageManager;

    public PlayerPickerAdapter(OnItemClickListener mListener) {
        super(mListener);
        imageManager = ImageManager.create(AppBase.getContext());
    }

    @Override
    protected BaseRecycleViewHolder getViewHolder(@NonNull View view, boolean b, boolean b1, BaseRecycleViewHolder.cardClickListener cardClickListener) {
        return new PlayerPickerViewHolder(view, b, b1, cardClickListener, imageManager);
    }

    @Override
    protected int getHeaderResId() {
        return R.layout.player_header;
    }

    @Override
    protected int getItemResId() {
        return R.layout.player_item;
    }
}
