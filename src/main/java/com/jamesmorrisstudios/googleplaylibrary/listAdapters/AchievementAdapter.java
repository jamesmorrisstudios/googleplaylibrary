package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.AdapterView;

import com.google.android.gms.common.images.ImageManager;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleAdapter;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleViewHolder;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.appbaselibrary.app.AppBase;

/**
 * Created by James on 5/27/2015.
 */
public class AchievementAdapter extends BaseRecycleAdapter {
    private ImageManager imageManager;

    public AchievementAdapter(OnRecycleAdapterEventsListener mListener) {
        super(mListener);
        imageManager = ImageManager.create(AppBase.getContext());
    }

    @NonNull
    @Override
    protected BaseRecycleViewHolder getViewHolder(@NonNull View view, boolean isHeader, @NonNull BaseRecycleViewHolder.cardClickListener cardClickListener) {
        return new AchievementViewHolder(view, isHeader, cardClickListener, imageManager);
    }

    @Override
    protected int getHeaderResId() {
        return R.layout.achievement_header;
    }



    @Override
    protected int getItemResId() {
        return R.layout.achievement_item;
    }
}
