package com.jamesmorrisstudios.googleplaylibrary.fragments;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import com.jamesmorrisstudios.appbaselibrary.Bus;
import com.jamesmorrisstudios.appbaselibrary.UtilsVersion;
import com.jamesmorrisstudios.appbaselibrary.activityHandlers.SnackbarRequest;
import com.jamesmorrisstudios.appbaselibrary.app.AppBase;
import com.jamesmorrisstudios.appbaselibrary.fragments.BaseRecycleListFragment;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleAdapter;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleContainer;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.googleplaylibrary.dialogRequests.AchievementOverlayDialogRequest;
import com.jamesmorrisstudios.googleplaylibrary.data.AchievementHeader;
import com.jamesmorrisstudios.googleplaylibrary.data.AchievementItem;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlay;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlayCalls;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.AchievementAdapter;
import com.jamesmorrisstudios.googleplaylibrary.listAdapters.AchievementContainer;
import com.jamesmorrisstudios.googleplaylibrary.util.UtilsAds;
import com.mopub.nativeads.MoPubRecyclerAdapter;
import com.mopub.nativeads.MoPubStaticNativeAdRenderer;
import com.mopub.nativeads.ViewBinder;
import com.squareup.otto.Subscribe;

import java.util.ArrayList;

/**
 * Created by James on 5/27/2015.
 */
public class AchievementFragment extends BaseRecycleListFragment {
    public static final String TAG = "AchievementsFragment";

    private String[] achievementIds = null;
    private MoPubRecyclerAdapter myMoPubAdapter;
    private BaseRecycleAdapter adapter;

    @NonNull
    @Override
    protected BaseRecycleAdapter getAdapter(@NonNull BaseRecycleAdapter.OnRecycleAdapterEventsListener mListener) {
        adapter = new AchievementAdapter(mListener);
        if (!UtilsVersion.isPro()) {
            Log.v("AcievementFragment", "Creating ad adapter");
            // Pass the recycler Adapter your original adapter.
            myMoPubAdapter = new MoPubRecyclerAdapter(getActivity(), adapter);
            // Create a view binder that describes your native ad layout.
            ViewBinder myViewBinder = new ViewBinder.Builder(R.layout.list_native_ad_full)
                    .titleId(R.id.title)
                    .textId(R.id.text)
                    .mainImageId(R.id.image)
                    .iconImageId(R.id.icon)
                    .callToActionId(R.id.call_to_action)
                    .build();

            MoPubStaticNativeAdRenderer myRenderer = new MoPubStaticNativeAdRenderer(myViewBinder);
            myMoPubAdapter.registerAdRenderer(myRenderer);
        }
        return adapter;
    }

    @NonNull
    @Override
    protected final RecyclerView.Adapter getAdapterToSet() {
        if (myMoPubAdapter != null && !UtilsVersion.isPro()) {
            return myMoPubAdapter;
        }
        return adapter;
    }

    @Override
    public void itemClicked(int position) {
        if (myMoPubAdapter != null && !UtilsVersion.isPro()) {
            itemClicker(adapter.getItems().get(myMoPubAdapter.getOriginalPosition(position)).data);
        } else {
            itemClicker(adapter.getItems().get(position).data);
        }
    }

    @Override
    protected boolean includeSearch() {
        return false;
    }

    @Override
    public void onStop() {
        super.onStop();
        GooglePlayCalls.getInstance().clearAchievementsCache();
    }

    @Override
    public void onDestroy() {
        if (myMoPubAdapter != null) {
            myMoPubAdapter.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void saveState(@NonNull Bundle bundle) {
        if (achievementIds != null) {
            bundle.putStringArray("achievementIds", achievementIds);
        }
    }

    @Override
    protected void restoreState(@NonNull Bundle bundle) {
        if (bundle != null) {
            if (bundle.containsKey("achievementIds")) {
                achievementIds = bundle.getStringArray("achievementIds");
            }
        }
    }

    /**
     * Event subscriber for checking if achievements are ready
     *
     * @param event Event
     */
    @Subscribe
    public final void onGooglePlayEvent(@NonNull GooglePlay.GooglePlayEvent event) {
        switch (event) {
            case ACHIEVEMENTS_ITEMS_READY:
                applyData();
                break;
            case ACHIEVEMENTS_ITEMS_FAIL:
                new SnackbarRequest(AppBase.getContext().getString(R.string.loading_failed), SnackbarRequest.SnackBarDuration.SHORT).execute();
                applyData();
                break;
        }
    }

    private void applyData() {
        ArrayList<BaseRecycleContainer> data = new ArrayList<>();
        if (GooglePlayCalls.getInstance().hasAchievements()) {
            int complete = GooglePlayCalls.getInstance().getNumberCompletedAchievements();
            int total = GooglePlayCalls.getInstance().getNumberAchievements();
            AchievementContainer header = new AchievementContainer(new AchievementHeader(getString(R.string.achievements), complete, total));
            data.add(header);
            ArrayList<AchievementItem> items = GooglePlayCalls.getInstance().getAchievements();
            for (AchievementItem item : items) {
                data.add(new AchievementContainer(item));
            }
        }
        applyData(data);
    }

    @Override
    protected void startDataLoad(boolean forced) {
        if (achievementIds != null) {
            GooglePlayCalls.getInstance().loadAchievements(forced, achievementIds);
        }
    }


    @Override
    protected void startMoreDataLoad() {

    }


    @Override
    protected void itemClick(@NonNull BaseRecycleContainer baseRecycleContainer) {

    }

    protected void itemClicker(@NonNull BaseRecycleContainer baseRecycleContainer) {
        Log.v("AchievementsFragment", "Item clicked");
        AchievementContainer item = (AchievementContainer) baseRecycleContainer;
        Bus.postObject(new AchievementOverlayDialogRequest(item));
    }

    @Override
    protected void itemMove(int i, int i1) {

    }

    @Override
    protected boolean supportsHeaders() {
        return true;
    }

    @Override
    protected boolean allowReording() {
        return false;
    }

    @Override
    protected void setStartData(@Nullable Bundle bundle, int i) {
        if (bundle != null && bundle.containsKey("achievementIds")) {
            achievementIds = bundle.getStringArray("achievementIds");
        }
    }

    @Override
    protected int getOptionsMenuRes() {
        return 0;
    }

    @Override
    protected boolean usesOptionsMenu() {
        return false;
    }

    @Override
    public boolean showToolbarTitle() {
        return true;
    }

    @Override
    protected void registerBus() {
        Bus.register(this);
    }

    @Override
    protected void unregisterBus() {
        Bus.unregister(this);
    }

    @Override
    public void onResume() {
        super.onResume();
        String adId = UtilsAds.getMopubNativeAdIdFull();
        if (myMoPubAdapter != null && adId != null && !UtilsVersion.isPro()) {
            Log.v("AchievementFragment", "Loading Ads");
            myMoPubAdapter.loadAds(adId);
        }
    }

    @Override
    protected void afterViewCreated() {
        setEnablePullToRefresh(true);

    }

}
