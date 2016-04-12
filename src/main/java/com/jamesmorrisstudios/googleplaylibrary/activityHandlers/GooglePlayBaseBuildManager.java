package com.jamesmorrisstudios.googleplaylibrary.activityHandlers;

import android.support.annotation.NonNull;

import com.jamesmorrisstudios.appbaselibrary.activityHandlers.BaseBuildManager;
import com.jamesmorrisstudios.googleplaylibrary.activities.GooglePlayActivity;

/**
 * Abstract based for builder classes
 * <p/>
 * Created by James on 12/9/2015.
 */
public class GooglePlayBaseBuildManager extends BaseBuildManager {

    /**
     * NEVER store the result from this function.
     *
     * @return The containing activity
     */
    @NonNull
    protected final GooglePlayActivity getGooglePlayActivity() {
        return (GooglePlayActivity) getActivity();
    }
}
