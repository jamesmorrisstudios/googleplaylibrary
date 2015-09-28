package com.jamesmorrisstudios.googleplaylibrary.house_ads;

import android.app.Activity;
import android.content.DialogInterface;
import android.media.Image;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.appbaselibrary.Utils;

/**
 * Created by James on 7/24/2015.
 */
public class HouseAdInterstitial extends DialogFragment {

    private View topView;
    private ImageView logo;
    private TextView title, text;
    private ImageButton close;
    private Button button;

    private HouseAd houseAd;

    public HouseAdInterstitial() {
        // Empty constructor required for DialogFragment
    }

    public void onPause() {
        dismiss();
        super.onPause();
    }

    @Override
    public void onDismiss(final DialogInterface dialog) {
        super.onDismiss(dialog);
        final Activity activity = getActivity();
        if (activity instanceof DialogInterface.OnDismissListener) {
            ((DialogInterface.OnDismissListener) activity).onDismiss(dialog);
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        topView = inflater.inflate(R.layout.layout_house_ad_interstitial, container);

        close = (ImageButton) topView.findViewById(R.id.close);

        logo = (ImageView) topView.findViewById(R.id.ad_logo);
        title = (TextView) topView.findViewById(R.id.ad_title);
        text = (TextView) topView.findViewById(R.id.ad_text);
        button = (Button) topView.findViewById(R.id.button);

        if(houseAd != null) {
            logo.setImageResource(houseAd.logoRes);
            title.setText(houseAd.titleRes);
            text.setText(houseAd.textRes);
            button.setText(getString(R.string.download) + " " + getString(houseAd.cost));
        }

        topView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.openLink(getString(R.string.base_store_link) + getString(houseAd.packageRes));
            }
        });

        close.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Utils.openLink(getString(R.string.base_store_link) + getString(houseAd.packageRes));
            }
        });



        return topView;
    }

    public final void setData(HouseAd houseAd) {
        this.houseAd = houseAd;
    }

}
