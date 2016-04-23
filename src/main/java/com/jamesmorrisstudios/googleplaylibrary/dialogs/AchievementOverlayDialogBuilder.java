package com.jamesmorrisstudios.googleplaylibrary.dialogs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;

import com.google.android.gms.common.images.ImageManager;
import com.jamesmorrisstudios.appbaselibrary.app.AppBase;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleViewHolder;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.AchievementContainer;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.AchievementViewHolder;

/**
 * Created by James on 8/10/2015.
 */
public class AchievementOverlayDialogBuilder {
    private AlertDialog.Builder builder;
    private CardView view;
    private AchievementContainer item;
    private AlertDialog dialog;

    private AchievementOverlayDialogBuilder(@NonNull Context context, final int style) {
        builder = new AlertDialog.Builder(context, style);
        view = (CardView) LayoutInflater.from(context).inflate(R.layout.fragment_achievement_item, null);
        builder.setView(view);
    }

    public static AchievementOverlayDialogBuilder with(@NonNull Context context, final int style) {
        return new AchievementOverlayDialogBuilder(context, style);
    }

    public AchievementOverlayDialogBuilder setAchievement(@NonNull AchievementContainer item) {
        this.item = item;
        return this;
    }

    public AlertDialog build() {
        AchievementViewHolder overlayHolder = new AchievementViewHolder(view, false, new BaseRecycleViewHolder.cardClickListener() {
            @Override
            public void cardClicked(int i) {

            }

            @Override
            public void toggleExpanded(int i) {

            }
        }, ImageManager.create(AppBase.getContext()));
        overlayHolder.bindItem(item, false);
        dialog = builder.create();
        return dialog;
    }

}
