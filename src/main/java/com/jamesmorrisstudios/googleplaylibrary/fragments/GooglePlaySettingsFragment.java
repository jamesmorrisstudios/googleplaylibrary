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

import com.google.android.gms.common.SignInButton;
import com.jamesmorrisstudios.appbaselibrary.fragments.SettingsFragment;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlay;
import com.jamesmorrisstudios.googleplaylibrary.util.AdUsage;
import com.jamesmorrisstudios.utilitieslibrary.Bus;
import com.jamesmorrisstudios.utilitieslibrary.Utils;
import com.nineoldandroids.view.ViewHelper;
import com.squareup.otto.Subscribe;

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

    @Override
    public void onStart() {
        super.onStart();
        Bus.register(this);
    }

    @Override
    public void onStop() {
        super.onStop();
        Bus.unregister(this);
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
        addLoginButton(view);
        addRemoveAdsButton(view);
        addSettingsOptions(view);
        return view;
    }

    private void addLoginButton(View view) {
        if(!googlePlaySettingsListener.isGooglePlayServicesEnabled()){
            return;
        }
        View item = getActivity().getLayoutInflater().inflate(R.layout.layout_google_play_sign, null);
        updateLoginButton(item);
        getSettingsContainer(view).addView(item);
    }

    private void updateLoginButton(View view) {
        final SignInButton signIn = (SignInButton)view.findViewById(R.id.sign_in_button);
        final Button signOut = (Button)view.findViewById(R.id.sign_out_button);

        if(GooglePlay.getInstance().isSignedIn()) {
            signIn.setVisibility(View.GONE);
            signOut.setVisibility(View.VISIBLE);
            signOut.setEnabled(true);
            signOut.setTextColor(getResources().getColor(R.color.textLightMain));
            ViewHelper.setAlpha(signOut, 1.0f);
            signOut.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Utils.toastShort("Signed out of Google Play Games");
                    GooglePlay.getInstance().signOut();
                    signOut.setEnabled(false);
                    signOut.setTextColor(getResources().getColor(R.color.textLightMain));
                    ViewHelper.setAlpha(signOut, 0.5f);
                }
            });
        } else {
            signIn.setVisibility(View.VISIBLE);
            signIn.setEnabled(true);
            ViewHelper.setAlpha(signIn, 1.0f);
            signIn.setSize(SignInButton.SIZE_WIDE);
            signIn.setColorScheme(SignInButton.COLOR_DARK);
            signOut.setVisibility(View.GONE);
            signIn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GooglePlay.getInstance().beginUserInitiatedSignIn();
                    signIn.setEnabled(false);
                    ViewHelper.setAlpha(signIn, 0.5f);
                }
            });
        }
    }

    private void addRemoveAdsButton(View view) {
        if(!googlePlaySettingsListener.isGooglePlayServicesEnabled()){
            return;
        }
        if(AdUsage.getAdsEnabled()) {
            View item = getActivity().getLayoutInflater().inflate(R.layout.layout_remove_ads, null);
            Button button = (Button)item.findViewById(R.id.btn_remove_ads);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    googlePlaySettingsListener.purchaseRemoveAds();
                }
            });
            button.setEnabled(true);
            button.setTextColor(getResources().getColor(R.color.textLightMain));
            ViewHelper.setAlpha(button, 1.0f);
            getSettingsContainer(view).addView(item);
        } else {
            View item = getActivity().getLayoutInflater().inflate(R.layout.layout_remove_ads_gone, null);
/*
            TextView text = (TextView)item.findViewById(R.id.ads_removed);
            text.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    googlePlaySettingsListener.testingConsumePurchase();
                }
            });
*/
            getSettingsContainer(view).addView(item);
        }
    }

    @Subscribe
    public final void onGooglePlayEvent(GooglePlay.GooglePlayEvent event) {
        switch(event) {
            case SIGN_IN_SUCCESS:
                View view = getView();
                if(view != null) {
                    updateLoginButton(view);
                }
                break;
            case SIGN_IN_FAIL:
                View view2 = getView();
                if(view2 != null) {
                    updateLoginButton(view2);
                }
                break;
            case SIGN_OUT:
                View view3 = getView();
                if(view3 != null) {
                    updateLoginButton(view3);
                }
                break;
        }
    }

    /**
     *
     */
    public interface OnGooglePlaySettingsListener {

        void testingConsumePurchase();

        /**
         *
         */
        void purchaseRemoveAds();

        boolean isGooglePlayServicesEnabled();
    }

}
