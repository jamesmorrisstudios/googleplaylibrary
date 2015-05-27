package com.jamesmorrisstudios.googleplaylibrary.fragments;

import android.app.Activity;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.jamesmorrisstudios.appbaselibrary.fragments.SettingsFragment;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.googleplaylibrary.util.AdUsage;

/**
 * Created by James on 5/26/2015.
 */
public class GooglePlaySettingsFragment extends SettingsFragment {
    protected OnGooglePlaySettingsListener googlePlaySettingsListener;

    /**
     * @param activity Activity to attach to
     */
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            googlePlaySettingsListener = (OnGooglePlaySettingsListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnGooglePlaySettingsListener");
        }
    }

    /**
     * Detach from activity
     */
    @Override
    public void onDetach() {
        super.onDetach();
        googlePlaySettingsListener = null;
    }

    /**
     * Create view
     *
     * @param inflater           Inflater object
     * @param container          Container view
     * @param savedInstanceState Saved instance state
     * @return The top view for this fragment
     */
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_settings, container, false);
        Log.v("GooglePlaySettingsFragm", "On Create View");
        addRemoveAdsButton(view);
        addSettingsOptions(view);
        return view;
    }

    private void addRemoveAdsButton(View view) {
        View item = getActivity().getLayoutInflater().inflate(R.layout.layout_remove_ads, null);
        Button button = (Button)item.findViewById(R.id.btn_remove_ads);
        if(AdUsage.getAdsEnabled()) {
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    googlePlaySettingsListener.purchaseRemoveAds();
                }
            });
            button.setEnabled(true);
            button.setTextColor(getResources().getColor(R.color.textLightMain));
            button.setAlpha(1.0f);
        } else {
            button.setEnabled(false);
            button.setTextColor(getResources().getColor(R.color.textLightMain));
            button.setAlpha(0.5f);
        }
        getSettingsContainer(view).addView(item);
    }

    /**
     *
     */
    public interface OnGooglePlaySettingsListener {

        /**
         *
         */
        void purchaseRemoveAds();
    }

}
