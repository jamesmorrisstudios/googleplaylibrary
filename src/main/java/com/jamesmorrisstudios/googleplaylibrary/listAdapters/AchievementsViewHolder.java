package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import android.support.v7.widget.CardView;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.android.gms.common.images.ImageManager;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleViewHolder;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.AchievementHeader;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.AchievementItem;
import com.jamesmorrisstudios.utilitieslibrary.app.AppUtil;
import com.jamesmorrisstudios.utilitieslibrary.controls.CircleProgressDeterminate;
import com.jamesmorrisstudios.utilitieslibrary.controls.ProgressBarDeterminate;

/**
 * Created by James on 5/27/2015.
 */
public class AchievementsViewHolder extends BaseRecycleViewHolder {
    private ImageManager imageManager;
    //Header
    private TextView headerTitle, headerCount;
    private ProgressBarDeterminate progressBar;
    //Item
    private TextView title, description, xp, percent;
    private ImageView image;
    private CircleProgressDeterminate circle;

    public AchievementsViewHolder(View view, boolean isHeader, cardClickListener mListener, ImageManager imageManager) {
        super(view, isHeader, mListener);
        this.imageManager = imageManager;
    }

    @Override
    protected void initHeader(View view) {
        headerTitle = (TextView) view.findViewById(R.id.achievement_main_text);
        headerCount = (TextView) view.findViewById(R.id.achievement_count_text);
        progressBar = (ProgressBarDeterminate) view.findViewById(R.id.achievement_progress);
    }

    @Override
    protected void initItem(View view) {
        CardView topLayout = (CardView) view.findViewById(R.id.achievement_card);
        topLayout.setOnClickListener(this);
        title = (TextView) view.findViewById(R.id.achievement_title_text);
        description = (TextView) view.findViewById(R.id.achievement_description_text);
        xp = (TextView) view.findViewById(R.id.achievement_xp_text);
        image = (ImageView) view.findViewById(R.id.achievement_icon);
        percent = (TextView) view.findViewById(R.id.achievement_percentage);
        circle = (CircleProgressDeterminate) view.findViewById(R.id.achievement_percentage_image);
    }

    @Override
    protected void bindHeader(BaseRecycleItem baseRecycleItem, boolean expanded) {
        AchievementHeader header = (AchievementHeader) baseRecycleItem;
        this.headerTitle.setText(header.title);
        this.headerCount.setText(Integer.toString(header.numberComplete) + AppUtil.getContext().getResources().getString(R.string.separator) + Integer.toString(header.numberTotal));
        this.progressBar.setMin(0);
        this.progressBar.setMax(header.numberTotal);
        this.progressBar.setProgress(header.numberComplete);
    }

    @Override
    protected void bindItem(BaseRecycleItem baseRecycleItem, boolean expanded) {
        AchievementItem item = (AchievementItem) baseRecycleItem;
        //Common non header
        this.title.setText(item.title);
        this.description.setText(item.description);
        if(item.xp != 0) {
            this.xp.setText(Long.toString(item.xp)+AppUtil.getContext().getResources().getString(R.string.xp));
        } else {
            this.xp.setText("");
        }
        if(item.totalSteps == -1 || item.state == AchievementItem.AchievementState.UNLOCKED) {
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
            percent.setText(per+AppUtil.getContext().getResources().getString(R.string.percent_char));
            circle.setProgress(per);
            percent.setVisibility(View.VISIBLE);
            circle.setVisibility(View.VISIBLE);
        }
    }
}
