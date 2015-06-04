package com.jamesmorrisstudios.googleplaylibrary.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.CardView;
import android.util.Log;
import android.view.View;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.google.android.gms.common.images.ImageManager;
import com.jamesmorrisstudios.appbaselibrary.fragments.BaseRecycleListFragment;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleAdapter;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleContainer;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.AchievementHeader;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.AchievementItem;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlay;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlayCalls;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.AchievementContainer;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.AchievementsAdapter;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.AchievementsViewHolder;
import com.jamesmorrisstudios.utilitieslibrary.Bus;
import com.jamesmorrisstudios.utilitieslibrary.Utils;
import com.jamesmorrisstudios.utilitieslibrary.animator.AnimatorControl;
import com.jamesmorrisstudios.utilitieslibrary.animator.AnimatorEndListener;
import com.nineoldandroids.animation.Animator;
import com.nineoldandroids.view.ViewHelper;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

/**
 * Created by James on 5/27/2015.
 */
public class AchievementsFragment extends BaseRecycleListFragment {
    public static final String TAG = "AchievementsFragment";
    private View overlayBackground;
    private CardView overlayCard;
    private AchievementsViewHolder overlayHolder;

    @Override
    protected BaseRecycleAdapter getAdapter(int i, BaseRecycleAdapter.OnItemClickListener onItemClickListener) {
        return new AchievementsAdapter(i, onItemClickListener);
    }

    /**
     * View creation done
     *
     * @param view               This fragments main view
     * @param savedInstanceState Saved instance state
     */
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        if(!(view instanceof ViewGroup)) {
            return;
        }
        ViewGroup viewGroup = (ViewGroup) view;
        RelativeLayout parent = null;
        if(viewGroup instanceof RelativeLayout) {
            parent = (RelativeLayout) view;
        } else if(viewGroup.getChildCount() == 1 && viewGroup.getChildAt(0) instanceof RelativeLayout) {
            parent = (RelativeLayout) viewGroup.getChildAt(0);
        }
        if(parent == null) {
            return;
        }
        overlayBackground = new View(getActivity().getApplicationContext());
        RelativeLayout.LayoutParams paramsBackground = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        overlayBackground.setLayoutParams(paramsBackground);
        overlayBackground.setBackgroundColor(getResources().getColor(R.color.background_material_light));
        ViewHelper.setAlpha(overlayBackground, 0.0f);

        overlayCard = (CardView) getActivity().getLayoutInflater().inflate(R.layout.achievements_item, null);
        RelativeLayout.LayoutParams paramsCard = new RelativeLayout.LayoutParams(Utils.getDipInt(300), Utils.getDipInt(102));
        paramsCard.addRule(RelativeLayout.CENTER_IN_PARENT, RelativeLayout.TRUE);


        overlayCard.setLayoutParams(paramsCard);
        ViewHelper.setAlpha(overlayCard, 0.0f);
        overlayHolder = new AchievementsViewHolder(overlayCard, false, null, ImageManager.create(getActivity().getApplicationContext()));

        overlayBackground.setVisibility(View.GONE);
        overlayCard.setVisibility(View.GONE);

        parent.addView(overlayBackground);
        parent.addView(overlayCard);
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        Bus.register(this);
    }

    public void onDestroy() {
        super.onDestroy();
        Bus.unregister(this);
    }

    /**
     * Event subscriber for checking if achievements are ready
     * @param event Event
     */
    @Subscribe
    public final void onGooglePlayEvent(@NonNull GooglePlay.GooglePlayEvent event) {
        switch(event) {
            case ACHIEVEMENTS_ITEMS_READY:
                applyData();
                break;
            case ACHIEVEMENTS_ITEMS_FAIL:
                Utils.toastShort("Failed to load achievements");
                applyData();
                break;
        }
    }

    private void applyData() {
        ArrayList<BaseRecycleContainer> data = new ArrayList<>();
        if(GooglePlayCalls.getInstance().hasAchievements()) {
            int complete = GooglePlayCalls.getInstance().getNumberCompletedAchievements();
            int total = GooglePlayCalls.getInstance().getNumberAchievements();
            AchievementContainer header = new AchievementContainer(new AchievementHeader("Achievements", complete, total));
            data.add(header);
            ArrayList<AchievementItem> items = GooglePlayCalls.getInstance().getAchievements();
            for(AchievementItem item : items) {
                data.add(new AchievementContainer(item));
            }
        }
        applyData(data);
    }

    @Override
    protected void startDataLoad(boolean forced) {
        GooglePlayCalls.getInstance().loadAchievements(forced);
    }

    @Override
    protected void itemClick(@NonNull BaseRecycleContainer baseRecycleContainer) {
        Log.v("AchievementsFragment", "Item clicked");
        AchievementContainer item = (AchievementContainer)baseRecycleContainer;
        overlayHolder.bindItem(item, false);

        AnimatorControl.alphaAutoStart(overlayBackground, 0.0f, 0.7f, 100, 0, null);
        AnimatorControl.alphaAutoStart(overlayCard, 0.0f, 1.0f, 100, 0, null);

        overlayBackground.setVisibility(View.VISIBLE);
        overlayCard.setVisibility(View.VISIBLE);

        overlayBackground.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                overlayBackground.setOnClickListener(null);
                AnimatorControl.alphaAutoStart(overlayBackground, 0.7f, 0.0f, 100, 0, null);
                AnimatorControl.alphaAutoStart(overlayCard, 1.0f, 0.0f, 100, 0, new AnimatorEndListener() {
                    @Override
                    public void onAnimationEnd(Animator animation) {
                        overlayBackground.setVisibility(View.GONE);
                        overlayCard.setVisibility(View.GONE);
                    }
                });
            }
        });
    }

    @Override
    public void onBack() {

    }

    @Override
    protected void afterViewCreated() {
        setEnablePullToRefresh(true);
    }

}
