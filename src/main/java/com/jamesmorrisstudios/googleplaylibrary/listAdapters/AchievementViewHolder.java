package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.images.ImageManager;
import com.jamesmorrisstudios.appbaselibrary.UtilsTheme;
import com.jamesmorrisstudios.appbaselibrary.app.AppBase;
import com.jamesmorrisstudios.appbaselibrary.controls.progress.CircleProgressDeterminate;
import com.jamesmorrisstudios.appbaselibrary.controls.progress.ProgressBarDeterminateHorizontal;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleViewHolder;
import com.jamesmorrisstudios.appbaselibrary.math.UtilsMath;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.googleplaylibrary.data.AchievementHeader;
import com.jamesmorrisstudios.googleplaylibrary.data.AchievementItem;

/**
 * Achievement view holder that manages the view for all achievement items and headers.
 *
 * Created by James on 5/27/2015.
 */
public final class AchievementViewHolder extends BaseRecycleViewHolder {
    private ImageManager imageManager;
    private TextView headerTitle, headerCount, title, description, xp, percent;
    private ProgressBarDeterminateHorizontal progressBar;
    private ImageView image;
    private CircleProgressDeterminate circle;

    /**
     * Constructor
     * @param view Top View
     * @param isHeader True if header, false if item
     * @param mListener Click listener
     * @param imageManager Image manager for downloading images
     */
    public AchievementViewHolder(@NonNull View view, boolean isHeader, @NonNull cardClickListener mListener, @NonNull ImageManager imageManager) {
        super(view, isHeader, mListener);
        this.imageManager = imageManager;
    }

    /**
     * Init the header view
     * @param view Top header view
     */
    @Override
    protected void initHeader(@NonNull View view) {
        headerTitle = (TextView) view.findViewById(R.id.achievement_main_text);
        headerCount = (TextView) view.findViewById(R.id.achievement_count_text);
        progressBar = (ProgressBarDeterminateHorizontal) view.findViewById(R.id.achievement_progress);
    }

    /**
     * Init the item view
     * @param view Top item view
     */
    @Override
    protected void initItem(@NonNull View view) {
        CardView topLayout = (CardView) view.findViewById(R.id.achievement_card);
        topLayout.setOnClickListener(this);
        title = (TextView) view.findViewById(R.id.achievement_title_text);
        description = (TextView) view.findViewById(R.id.achievement_description_text);
        xp = (TextView) view.findViewById(R.id.achievement_xp_text);
        image = (ImageView) view.findViewById(R.id.achievement_icon);
        percent = (TextView) view.findViewById(R.id.achievement_percentage);
        circle = (CircleProgressDeterminate) view.findViewById(R.id.achievement_percentage_image);
    }

    /**
     * Bind the header data
     * @param baseRecycleItem Base header data
     * @param expanded True if expanded form, false if normal
     */
    @Override
    protected void bindHeader(@NonNull BaseRecycleItem baseRecycleItem, boolean expanded) {
        AchievementHeader header = (AchievementHeader) baseRecycleItem;
        this.headerTitle.setText(header.title);
        this.headerCount.setText(Integer.toString(header.numberComplete) + AppBase.getContext().getResources().getString(R.string.separator) + Integer.toString(header.numberTotal));
        this.progressBar.setMin(0);
        this.progressBar.setMax(header.numberTotal);
        this.progressBar.setProgress(header.numberComplete);
        this.progressBar.setColors(new int[]{UtilsTheme.getPrimaryDarkColor()});
    }

    /**
     * Bind the item data
     * @param baseRecycleItem Base item data
     * @param expanded True if expanded form, false if normal
     */
    @Override
    protected void bindItem(@NonNull BaseRecycleItem baseRecycleItem, boolean expanded) {
        AchievementItem item = (AchievementItem) baseRecycleItem;
        //Common non header
        this.title.setText(item.title);
        this.description.setText(item.description);
        if (item.xp != 0) {
            this.xp.setText(UtilsMath.formatDisplayNumber(item.xp) + " " + AppBase.getContext().getResources().getString(R.string.xp));
        } else {
            this.xp.setText("");
        }
        if (item.totalSteps == -1 || item.state == AchievementItem.AchievementState.UNLOCKED) {
            //Unlocked
            percent.setVisibility(View.GONE);
            circle.setVisibility(View.GONE);
            if (item.state == AchievementItem.AchievementState.REVEALED) {
                imageManager.loadImage(this.image, item.imageRevealedUri);
            } else if (item.state == AchievementItem.AchievementState.UNLOCKED) {
                imageManager.loadImage(this.image, item.imageUnlockedUri);
            }
            this.image.setVisibility(View.VISIBLE);
        } else {
            //Incremental
            this.image.setVisibility(View.GONE);
            int per = Math.round(100 * (item.currentSteps * 1.0f / item.totalSteps));
            percent.setText(per + AppBase.getContext().getResources().getString(R.string.percent_char));
            circle.setProgress(per);
            percent.setVisibility(View.VISIBLE);
            circle.setVisibility(View.VISIBLE);
        }
    }
}
