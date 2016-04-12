package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import android.support.annotation.LayoutRes;
import android.support.annotation.NonNull;
import android.view.View;

import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleAdapter;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleViewHolder;
import com.jamesmorrisstudios.googleplaylibrary.R;

/**
 * Online Load Game list adapter
 *
 * Created by James on 8/3/2015.
 */
public class OnlineLoadGameAdapter extends BaseRecycleAdapter {

    /**
     * Adapter Constructor.
     * @param mListener Adapter Event Listener
     */
    public OnlineLoadGameAdapter(OnRecycleAdapterEventsListener mListener) {
        super(mListener);
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
        return new OnlineLoadGameViewHolder(view, isHeader, cardClickListener);
    }

    /**
     * @return Header resource ID
     */
    @LayoutRes
    @Override
    protected int getHeaderResId() {
        return R.layout.fragment_online_load_game_header;
    }

    /**
     * @return Item resource Id
     */
    @LayoutRes
    @Override
    protected int getItemResId() {
        return R.layout.fragment_online_load_game_item;
    }

}
