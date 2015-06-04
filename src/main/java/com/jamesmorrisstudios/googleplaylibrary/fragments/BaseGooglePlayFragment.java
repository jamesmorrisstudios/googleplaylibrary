package com.jamesmorrisstudios.googleplaylibrary.fragments;

import android.app.Activity;
import android.support.annotation.NonNull;

import com.jamesmorrisstudios.appbaselibrary.fragments.BaseFragment;

/**
 * Created by James on 6/2/2015.
 */
public abstract class BaseGooglePlayFragment extends BaseFragment {
    protected OnGooglePlayListener googlePlayListener;

    /**
     * @param activity Activity to attach to
     */
    @Override
    public void onAttach(@NonNull Activity activity) {
        super.onAttach(activity);
        try {
            googlePlayListener = (OnGooglePlayListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnGooglePlayListener");
        }
    }

    /**
     * Detach from activity
     */
    @Override
    public void onDetach() {
        super.onDetach();
        googlePlayListener = null;
    }

    /**
     *
     */
    public interface OnGooglePlayListener {

        boolean isGooglePlayServicesEnabled();
    }

}
