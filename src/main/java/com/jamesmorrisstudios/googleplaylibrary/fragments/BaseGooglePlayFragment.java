package com.jamesmorrisstudios.googleplaylibrary.fragments;

import android.content.Context;
import android.support.annotation.NonNull;

import com.jamesmorrisstudios.appbaselibrary.fragments.BaseFragment;

/**
 * Created by James on 6/2/2015.
 */
public abstract class BaseGooglePlayFragment extends BaseFragment {
    protected OnGooglePlayListener googlePlayListener;

    /**
     * @param context Context to attach to
     */
    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            googlePlayListener = (OnGooglePlayListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
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
