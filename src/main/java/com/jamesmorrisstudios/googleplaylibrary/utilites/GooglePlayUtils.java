package com.jamesmorrisstudios.googleplaylibrary.utilites;

import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.utilitieslibrary.app.AppUtil;

/**
 * Created by James on 5/13/2015.
 */
public class GooglePlayUtils {

    /**
     * Gets the height of the smart banner style ad
     *
     * @return Height of the ad view
     */
    public static float getAdHeight() {
        return AppUtil.getContext().getResources().getDimension(R.dimen.ad_height);
    }

}
