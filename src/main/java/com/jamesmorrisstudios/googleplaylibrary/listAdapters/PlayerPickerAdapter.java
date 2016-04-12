package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.google.android.gms.common.images.ImageManager;
import com.jamesmorrisstudios.appbaselibrary.app.AppBase;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleAdapter;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleViewHolder;
import com.jamesmorrisstudios.googleplaylibrary.R;

/**
 * Player Picker list adapter
 *
 * Created by James on 8/11/2015.
 */
public class PlayerPickerAdapter extends BaseRecycleAdapter {
    private ImageManager imageManager;

    /**
     * Adapter Constructor.
     * @param mListener Adapter Event Listener
     */
    public PlayerPickerAdapter(OnRecycleAdapterEventsListener mListener) {
        super(mListener);
        imageManager = ImageManager.create(AppBase.getContext());
    }

    /**
     * Retrieve the custom view holder for this view.
     * @param view Top view
     * @param isHeader True if header
     * @param cardClickListener Click listener
     * @return The custom view holder
     */
    @NonNull
    @Override
    protected BaseRecycleViewHolder getViewHolder(@NonNull View view, boolean isHeader, @NonNull BaseRecycleViewHolder.cardClickListener cardClickListener) {
        return new PlayerPickerViewHolder(view, isHeader, cardClickListener, imageManager);
    }

    /**
     * @return Header resource ID
     */
    @LayoutRes
    @Override
    protected int getHeaderResId() {
        return R.layout.fragment_player_picker_header;
    }

    /**
     * @return Item resource Id
     */
    @LayoutRes
    @Override
    protected int getItemResId() {
        return R.layout.fragment_player_picker_item;
    }

}
