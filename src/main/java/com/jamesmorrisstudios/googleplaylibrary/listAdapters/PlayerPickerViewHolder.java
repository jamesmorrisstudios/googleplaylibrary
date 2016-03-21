package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.images.ImageManager;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleViewHolder;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.PlayerHeader;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.PlayerItem;

/**
 * Created by James on 8/11/2015.
 */
public class PlayerPickerViewHolder extends BaseRecycleViewHolder {
    private ImageManager imageManager;

    //Header
    private TextView title;

    //Item
    private ImageView icon;
    private TextView name;

    public PlayerPickerViewHolder(View view, boolean isHeader, cardClickListener mListener, ImageManager imageManager) {
        super(view, isHeader, mListener);
        this.imageManager = imageManager;
    }

    @Override
    protected void initHeader(View view) {
        title = (TextView) view.findViewById(R.id.title);
    }

    @Override
    protected void initItem(View view) {
        icon = (ImageView) view.findViewById(R.id.icon);
        name = (TextView) view.findViewById(R.id.name);
    }

    @Override
    protected void bindHeader(BaseRecycleItem baseRecycleItem, boolean b) {
        PlayerHeader header = (PlayerHeader) baseRecycleItem;
        title.setText(header.title);
    }

    @Override
    protected void bindItem(BaseRecycleItem baseRecycleItem, boolean b) {
        PlayerItem item = (PlayerItem) baseRecycleItem;
        if(item.player.hasIconImage()) {
            imageManager.loadImage(icon, item.player.getIconImageUri(), R.drawable.ic_player);
        } else {
            imageManager.loadImage(icon, R.drawable.ic_player);
        }
        name.setText(item.player.getDisplayName());
    }
}
