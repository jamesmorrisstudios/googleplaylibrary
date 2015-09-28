package com.jamesmorrisstudios.googleplaylibrary.dialogs;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.common.images.ImageManager;
import com.google.android.gms.games.Player;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.googleplaylibrary.googlePlay.GooglePlayCalls;
import com.jamesmorrisstudios.appbaselibrary.app.AppBase;

/**
 * Created by James on 8/10/2015.
 */
public class PlayerDetailsDialogBuilder {
    private AlertDialog.Builder builder;
    private LinearLayout view;
    private Player player;
    private AlertDialog dialog;
    private ImageManager imageManager;

    private PlayerDetailsDialogBuilder(@NonNull Context context) {
        builder = new AlertDialog.Builder(context, R.style.alertDialog);
        LayoutInflater li = LayoutInflater.from(context);
        view = (LinearLayout) li.inflate(R.layout.player_overlay, null);
        builder.setView(view);
    }

    public static PlayerDetailsDialogBuilder with(@NonNull Context context) {
        return new PlayerDetailsDialogBuilder(context);
    }

    public PlayerDetailsDialogBuilder setView(@NonNull LinearLayout view) {
        this.view = view;
        builder.setView(view);
        return this;
    }

    public PlayerDetailsDialogBuilder setPlayer(@NonNull Player player) {
        this.player = player;
        return this;
    }

    public AlertDialog build() {
        Context context = builder.getContext();
        imageManager = ImageManager.create(context);
        buildPlayerDetails();
        dialog = builder.create();
        return dialog;
    }

    private void buildPlayerDetails() {
        ImageView icon = (ImageView) view.findViewById(R.id.icon);
        TextView name = (TextView) view.findViewById(R.id.name);
        TextView level = (TextView) view.findViewById(R.id.level);
        TextView levelName = (TextView) view.findViewById(R.id.level_name);
        Button btn = (Button) view.findViewById(R.id.profile_btn);

        if (player.hasIconImage()) {
            imageManager.loadImage(icon, player.getIconImageUri());
        } else {
            imageManager.loadImage(icon, R.drawable.leaderboard_blank);
        }
        name.setText(player.getDisplayName());
        level.setText(Integer.toString(player.getLevelInfo().getCurrentLevel().getLevelNumber()));
        levelName.setText(player.getTitle());

        Player me = GooglePlayCalls.getInstance().getCurrentPlayer();

        if(player.getPlayerId().equals(me.getPlayerId())) {
            btn.setText(AppBase.getContext().getString(R.string.view_profile));
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GooglePlayCalls.getInstance().launchGPSProfile();
                }
            });
        } else {
            btn.setText(AppBase.getContext().getString(R.string.compare_profiles));
            btn.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    GooglePlayCalls.getInstance().launchGPSProfileCompare(player);
                }
            });
        }
    }

}
