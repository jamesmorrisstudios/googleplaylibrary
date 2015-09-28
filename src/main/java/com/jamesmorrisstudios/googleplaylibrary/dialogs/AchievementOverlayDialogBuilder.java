package com.jamesmorrisstudios.googleplaylibrary.dialogs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.CardView;
import android.view.LayoutInflater;

import com.google.android.gms.common.images.ImageManager;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.AchievementContainer;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.AchievementViewHolder;
import com.jamesmorrisstudios.appbaselibrary.app.AppBase;

/**
 * Created by James on 8/10/2015.
 */
public class AchievementOverlayDialogBuilder {
    private AlertDialog.Builder builder;
    private CardView view;
    private AchievementContainer item;
    private AlertDialog dialog;

    private AchievementOverlayDialogBuilder(@NonNull Context context) {
        builder = new AlertDialog.Builder(context, R.style.alertDialogTransparent);
        LayoutInflater li = LayoutInflater.from(context);
        view = (CardView) li.inflate(R.layout.achievement_item, null);
        builder.setView(view);
    }

    public static AchievementOverlayDialogBuilder with(@NonNull Context context) {
        return new AchievementOverlayDialogBuilder(context);
    }

    public AchievementOverlayDialogBuilder setAchievement(@NonNull AchievementContainer item) {
        this.item = item;
        return this;
    }

    public AlertDialog build() {
        Context context = builder.getContext();
        AchievementViewHolder overlayHolder = new AchievementViewHolder(view, false, false, null, ImageManager.create(AppBase.getContext()));
        overlayHolder.bindItem(item, false);
        dialog = builder.create();
        return dialog;
    }

}
