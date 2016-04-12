package com.jamesmorrisstudios.googleplaylibrary.listAdapters;

import android.support.annotation.NonNull;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleItem;
import com.jamesmorrisstudios.appbaselibrary.listAdapters.BaseRecycleViewHolder;
import com.jamesmorrisstudios.appbaselibrary.time.DateTimeItem;
import com.jamesmorrisstudios.appbaselibrary.time.UtilsTime;
import com.jamesmorrisstudios.googleplaylibrary.R;
import com.jamesmorrisstudios.googleplaylibrary.game.GameDetails;
import com.jamesmorrisstudios.googleplaylibrary.data.OnlineLoadHeader;
import com.jamesmorrisstudios.googleplaylibrary.data.OnlineSaveItem;

/**
 * Online load game view holder that manages the view for all Online load game items and headers.
 *
 * Created by James on 8/3/2015.
 */
public class OnlineLoadGameViewHolder extends BaseRecycleViewHolder {
    private TextView headerTitle, timestamp, name1, name2, name3, name4, name5, name6;
    private Toolbar toolbar;
    private ImageView gameImage, variant, teams, addon1, addon2, addon3, addon4, addon5, addon6;
    private LinearLayout playerRow1, playerRow2;

    /**
     * Constructor
     * @param view Top View
     * @param isHeader True if header, false if item
     * @param mListener Click listener
     */
    public OnlineLoadGameViewHolder(@NonNull View view, boolean isHeader, @NonNull cardClickListener mListener) {
        super(view, isHeader, mListener);
    }

    /**
     * Init the header view
     * @param view Top header view
     */
    @Override
    protected void initHeader(@NonNull View view) {
        headerTitle = (TextView) view.findViewById(R.id.title);
    }

    /**
     * Init the item view
     * @param view Top item view
     */
    @Override
    protected void initItem(@NonNull View view) {
        toolbar = (Toolbar) view.findViewById(R.id.toolbar);
        gameImage = (ImageView) view.findViewById(R.id.game_image);
        variant = (ImageView) view.findViewById(R.id.match_variant);
        teams = (ImageView) view.findViewById(R.id.match_teams);
        addon1 = (ImageView) view.findViewById(R.id.match_addon_1);
        addon2 = (ImageView) view.findViewById(R.id.match_addon_2);
        addon3 = (ImageView) view.findViewById(R.id.match_addon_3);
        addon4 = (ImageView) view.findViewById(R.id.match_addon_4);
        addon5 = (ImageView) view.findViewById(R.id.match_addon_5);
        addon6 = (ImageView) view.findViewById(R.id.match_addon_6);
        playerRow1 = (LinearLayout) view.findViewById(R.id.names_row_1);
        playerRow2 = (LinearLayout) view.findViewById(R.id.names_row_2);
        timestamp = (TextView) view.findViewById(R.id.timestamp);
        name1 = (TextView) view.findViewById(R.id.name_1);
        name2 = (TextView) view.findViewById(R.id.name_2);
        name3 = (TextView) view.findViewById(R.id.name_3);
        name4 = (TextView) view.findViewById(R.id.name_4);
        name5 = (TextView) view.findViewById(R.id.name_5);
        name6 = (TextView) view.findViewById(R.id.name_6);
    }

    /**
     * Bind the header data
     * @param baseRecycleItem Base header data
     * @param expanded True if expanded form, false if normal
     */
    @Override
    protected void bindHeader(@NonNull BaseRecycleItem baseRecycleItem, boolean expanded) {
        OnlineLoadHeader item = (OnlineLoadHeader) baseRecycleItem;
        headerTitle.setText(item.title);
    }

    /**
     * Bind the item data
     * @param baseRecycleItem Base item data
     * @param expanded True if expanded form, false if normal
     */
    @Override
    protected void bindItem(@NonNull BaseRecycleItem baseRecycleItem, boolean expanded) {
        toolbar.getMenu().clear();
        OnlineSaveItem item = (OnlineSaveItem) baseRecycleItem;
        if (item.canRematch()) {
            toolbar.inflateMenu(R.menu.load_game_rematch);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    //TODO
                    return false;
                }
            });
        } else {
            toolbar.inflateMenu(R.menu.load_game);
            toolbar.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
                @Override
                public boolean onMenuItemClick(MenuItem item) {
                    //TODO
                    return false;
                }
            });
        }
        if (item.image == null) {
            gameImage.setBackgroundResource(R.drawable.ic_blank_game_image);
        } else {
            gameImage.setImageBitmap(item.image);
        }
        //Addons
        if (item.matchAddons.contains(GameDetails.MatchAddon.ADDON_1)) {
            addon1.setVisibility(View.VISIBLE);
        } else {
            addon1.setVisibility(View.INVISIBLE);
        }
        if (item.matchAddons.contains(GameDetails.MatchAddon.ADDON_2)) {
            addon2.setVisibility(View.VISIBLE);
        } else {
            addon2.setVisibility(View.INVISIBLE);
        }
        if (item.matchAddons.contains(GameDetails.MatchAddon.ADDON_3)) {
            addon3.setVisibility(View.VISIBLE);
        } else {
            addon3.setVisibility(View.INVISIBLE);
        }
        if (item.matchAddons.contains(GameDetails.MatchAddon.ADDON_4)) {
            addon4.setVisibility(View.VISIBLE);
        } else {
            addon4.setVisibility(View.INVISIBLE);
        }
        if (item.matchAddons.contains(GameDetails.MatchAddon.ADDON_5)) {
            addon5.setVisibility(View.VISIBLE);
        } else {
            addon5.setVisibility(View.INVISIBLE);
        }
        if (item.matchAddons.contains(GameDetails.MatchAddon.ADDON_6)) {
            addon6.setVisibility(View.VISIBLE);
        } else {
            addon6.setVisibility(View.INVISIBLE);
        }
        switch (item.matchVariant) {
            case VARIANT_1:
                variant.setBackgroundResource(R.drawable.ic_variant_1);
                break;
            case VARIANT_2:
                variant.setBackgroundResource(R.drawable.ic_variant_2);
                break;
            case VARIANT_3:
                variant.setBackgroundResource(R.drawable.ic_variant_3);
                break;
            case VARIANT_4:
                variant.setBackgroundResource(R.drawable.ic_variant_4);
                break;
            case VARIANT_5:
                variant.setBackgroundResource(R.drawable.ic_variant_5);
                break;
            case VARIANT_6:
                variant.setBackgroundResource(R.drawable.ic_variant_6);
                break;
        }
        switch (item.numberTeams) {
            case FREE_FOR_ALL:
                teams.setBackgroundResource(R.drawable.ic_free_for_all);
                break;
            case TWO:
                teams.setBackgroundResource(R.drawable.ic_two_teams);
                break;
        }

        if (item.numberPlayers == GameDetails.NumberPlayers.ONE) {
            playerRow1.setVisibility(View.GONE);
            playerRow2.setVisibility(View.GONE);
        } else if (item.numberPlayers == GameDetails.NumberPlayers.TWO || item.numberPlayers == GameDetails.NumberPlayers.THREE) {
            playerRow1.setVisibility(View.VISIBLE);
            playerRow2.setVisibility(View.GONE);
            setPlayerNames(item.playerNames);
        } else if (item.numberPlayers == GameDetails.NumberPlayers.FOUR || item.numberPlayers == GameDetails.NumberPlayers.FIVE || item.numberPlayers == GameDetails.NumberPlayers.SIX) {
            playerRow1.setVisibility(View.VISIBLE);
            playerRow2.setVisibility(View.VISIBLE);
            setPlayerNames(item.playerNames);
        }
        timestamp.setText(UtilsTime.getDateTimeFormatted(new DateTimeItem(item.getLastUpdateTimeStamp())));
    }

    /**
     * Apply the player names
     * @param names Array of names
     */
    private void setPlayerNames(@NonNull String[] names) {
        for (int i = 0; i < names.length; i++) {
            switch (i) {
                case 0:
                    name1.setText(names[i]);
                    break;
                case 1:
                    name2.setText(names[i]);
                    break;
                case 2:
                    name3.setText(names[i]);
                    break;
                case 3:
                    name4.setText(names[i]);
                    break;
                case 4:
                    name5.setText(names[i]);
                    break;
                case 5:
                    name6.setText(names[i]);
                    break;
            }

        }
    }

}
