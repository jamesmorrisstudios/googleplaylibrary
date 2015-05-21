package com.jamesmorrisstudios.googleplaylibrary.googlePlay;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.games.GamesActivityResultCodes;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.utilitieslibrary.app.AppUtil;

/**
 * Created by James on 5/11/2015.
 */
public class GooglePlayUtils {
    public static final int R_UNKNOWN_ERROR = 0;
    public static final int R_SIGN_IN_FAILED = 1;
    public static final int R_APP_MISCONFIGURED = 2;
    public static final int R_LICENSE_FAILED = 3;

    public final static int CLIENT_GAMES = 0x01;
    public final static int CLIENT_SNAPSHOT = 0x08;

    // Request code we use when invoking other Activities to complete the
    // sign-in flow.
    public final static int RC_RESOLVE = 9001;
    // Request code when invoking Activities whose result we don't care about.
    public final static int RC_UNUSED = 9002;
    // Should we start the flow to sign the user in automatically on startup? If
    // so, up to
    // how many times in the life of the application?
    public final static int DEFAULT_MAX_SIGN_IN_ATTEMPTS = 3;
    public final static String GAMEHELPER_SHARED_PREFS = "GAMEHELPER_SHARED_PREFS";
    public final static String KEY_SIGN_IN_CANCELLATIONS = "KEY_SIGN_IN_CANCELLATIONS";

    @NonNull
    private final static int[] RES_IDS = {
            R.string.gamehelper_unknown_error, R.string.gamehelper_sign_in_failed,
            R.string.gamehelper_app_misconfigured, R.string.gamehelper_license_failed
    };

    @NonNull
    static String activityResponseCodeToString(int respCode) {
        switch (respCode) {
            case Activity.RESULT_OK:
                return "RESULT_OK";
            case Activity.RESULT_CANCELED:
                return "RESULT_CANCELED";
            case GamesActivityResultCodes.RESULT_APP_MISCONFIGURED:
                return "RESULT_APP_MISCONFIGURED";
            case GamesActivityResultCodes.RESULT_LEFT_ROOM:
                return "RESULT_LEFT_ROOM";
            case GamesActivityResultCodes.RESULT_LICENSE_FAILED:
                return "RESULT_LICENSE_FAILED";
            case GamesActivityResultCodes.RESULT_RECONNECT_REQUIRED:
                return "RESULT_RECONNECT_REQUIRED";
            case GamesActivityResultCodes.RESULT_SIGN_IN_FAILED:
                return "SIGN_IN_FAILED";
            default:
                return String.valueOf(respCode);
        }
    }

    @NonNull
    static String errorCodeToString(int errorCode) {
        switch (errorCode) {
            case ConnectionResult.DEVELOPER_ERROR:
                return "DEVELOPER_ERROR(" + errorCode + ")";
            case ConnectionResult.INTERNAL_ERROR:
                return "INTERNAL_ERROR(" + errorCode + ")";
            case ConnectionResult.INVALID_ACCOUNT:
                return "INVALID_ACCOUNT(" + errorCode + ")";
            case ConnectionResult.LICENSE_CHECK_FAILED:
                return "LICENSE_CHECK_FAILED(" + errorCode + ")";
            case ConnectionResult.NETWORK_ERROR:
                return "NETWORK_ERROR(" + errorCode + ")";
            case ConnectionResult.RESOLUTION_REQUIRED:
                return "RESOLUTION_REQUIRED(" + errorCode + ")";
            case ConnectionResult.SERVICE_DISABLED:
                return "SERVICE_DISABLED(" + errorCode + ")";
            case ConnectionResult.SERVICE_INVALID:
                return "SERVICE_INVALID(" + errorCode + ")";
            case ConnectionResult.SERVICE_MISSING:
                return "SERVICE_MISSING(" + errorCode + ")";
            case ConnectionResult.SERVICE_VERSION_UPDATE_REQUIRED:
                return "SERVICE_VERSION_UPDATE_REQUIRED(" + errorCode + ")";
            case ConnectionResult.SIGN_IN_REQUIRED:
                return "SIGN_IN_REQUIRED(" + errorCode + ")";
            case ConnectionResult.SUCCESS:
                return "SUCCESS(" + errorCode + ")";
            default:
                return "Unknown error code " + errorCode;
        }
    }

    @NonNull
    static String getString(@NonNull Context ctx, int whichString) {
        whichString = whichString >= 0 && whichString < RES_IDS.length ? whichString : 0;
        int resId = RES_IDS[whichString];
        return ctx.getString(resId);
    }

    /**
     * Gets the height of the smart banner style ad
     *
     * @return Height of the ad view
     */
    public static float getAdHeight() {
        return AppUtil.getContext().getResources().getDimension(R.dimen.ad_height);
    }

}
