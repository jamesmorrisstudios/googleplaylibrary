package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import android.support.annotation.NonNull;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.images.ImageManager;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleViewHolder;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.googleplaylibrary.data.PlayerPickerHeader;
import com.jamesmorrisstudios.googleplaylibrary.data.PlayerPickerItem;

/**
 * Player picker view holder that manages the view for all player picker items and headers.
 *
 * Created by James on 8/11/2015.
 */
public class PlayerPickerViewHolder extends BaseRecycleViewHolder {
    private ImageManager imageManager;
    private TextView title, name;
    private ImageView icon;

    /**
     * Constructor
     * @param view Top View
     * @param isHeader True if header, false if item
     * @param mListener Click listener
     * @param imageManager Image manager for downloading images
     */
    public PlayerPickerViewHolder(@NonNull View view, boolean isHeader, @NonNull cardClickListener mListener, @NonNull ImageManager imageManager) {
        super(view, isHeader, mListener);
        this.imageManager = imageManager;
    }

    /**
     * Init the header view
     * @param view Top header view
     */
    @Override
    protected void initHeader(@NonNull View view) {
        title = (TextView) view.findViewById(R.id.title);
    }

    /**
     * Init the item view
     * @param view Top item view
     */
    @Override
    protected void initItem(@NonNull View view) {
        icon = (ImageView) view.findViewById(R.id.icon);
        name = (TextView) view.findViewById(R.id.name);
    }

    /**
     * Bind the header data
     * @param baseRecycleItem Base header data
     * @param expanded True if expanded form, false if normal
     */
    @Override
    protected void bindHeader(@NonNull BaseRecycleItem baseRecycleItem, boolean expanded) {
        PlayerPickerHeader header = (PlayerPickerHeader) baseRecycleItem;
        title.setText(header.title);
    }

    /**
     * Bind the item data
     * @param baseRecycleItem Base item data
     * @param expanded True if expanded form, false if normal
     */
    @Override
    protected void bindItem(@NonNull BaseRecycleItem baseRecycleItem, boolean expanded) {
        PlayerPickerItem item = (PlayerPickerItem) baseRecycleItem;
        if (item.player.hasIconImage()) {
            imageManager.loadImage(icon, item.player.getIconImageUri(), R.drawable.ic_player);
        } else {
            imageManager.loadImage(icon, R.drawable.ic_player);
        }
        name.setText(item.player.getDisplayName());
    }
}
