package com.vleuro.vlshop.adapter;

import android.app.Activity;
import android.content.Intent;
import android.view.View;
import android.support.v7.widget.Toolbar;

import com.mikepenz.materialdrawer.Drawer;
import com.mikepenz.materialdrawer.DrawerBuilder;
import com.mikepenz.materialdrawer.model.DividerDrawerItem;
import com.mikepenz.materialdrawer.model.PrimaryDrawerItem;
import com.mikepenz.materialdrawer.model.SecondaryDrawerItem;
import com.mikepenz.materialdrawer.model.interfaces.IDrawerItem;
import com.vleuro.vlshop.BarcodeCaptureActivity;
import com.vleuro.vlshop.R;
import com.vleuro.vlshop.MainActivity;



public class DrawerUtil {
    public static void getDrawer(final Activity activity, Toolbar toolbar) {
        //if you want to update the items at a later time it is recommended to keep it in a variable
        PrimaryDrawerItem drawerEmptyItem= new PrimaryDrawerItem().withIdentifier(0).withName("");
        drawerEmptyItem.withEnabled(false);

        PrimaryDrawerItem drawerItemManagePlayers = new PrimaryDrawerItem().withIdentifier(1)
                .withName("Bevásárló Lista...").withIcon(R.drawable.ic_star_black_24dp);
        PrimaryDrawerItem drawerItemManagePlayersTournaments = new PrimaryDrawerItem()
                .withIdentifier(2).withName("Árellenőrző...").withIcon(R.drawable.ic_menu_camera);

/*
        SecondaryDrawerItem drawerItemSettings = new SecondaryDrawerItem().withIdentifier(3)
                .withName("Bevásárló Lista").withIcon(R.drawable.bg_circle);
        SecondaryDrawerItem drawerItemAbout = new SecondaryDrawerItem().withIdentifier(4)
                .withName("Árellenőrző").withIcon(R.drawable.bg_circle);
                */
        SecondaryDrawerItem drawerItemHelp = new SecondaryDrawerItem().withIdentifier(5)
                .withName("Beállítások").withIcon(R.drawable.ic_edit_white_24dp);
        SecondaryDrawerItem drawerItemDonate = new SecondaryDrawerItem().withIdentifier(6)
                .withName("Kijelentkezés").withIcon(R.drawable.ic_menu_share);





        //create the drawer and remember the `Drawer` result object
        Drawer result = new DrawerBuilder()
                .withActivity(activity)
                .withToolbar(toolbar)
                .withActionBarDrawerToggle(true)
                .withActionBarDrawerToggleAnimated(true)
                .withCloseOnClick(true)
                .withSelectedItem(-1)
                .addDrawerItems(
                        drawerEmptyItem,drawerEmptyItem,drawerEmptyItem,
                        drawerItemManagePlayers,
                        drawerItemManagePlayersTournaments,
                        new DividerDrawerItem(),
       //                 drawerItemAbout,
         //               drawerItemSettings,
                        drawerItemHelp,
                        drawerItemDonate
                )
                .withOnDrawerItemClickListener(new Drawer.OnDrawerItemClickListener() {
                    @Override
                    public boolean onItemClick(View view, int position, IDrawerItem drawerItem) {
                        if (drawerItem.getIdentifier() == 1 && !(activity instanceof MainActivity)) {
                            // load tournament screen
                            Intent intent = new Intent(activity, MainActivity.class);
                            view.getContext().startActivity(intent);
                        }
                        if (drawerItem.getIdentifier() == 2 ) {
                            //&& !(activity instanceof MainActivity)
                            // load tournament screen
                            Intent intent = new Intent(activity, BarcodeCaptureActivity.class);
                            //Intent intent = new Intent(this, BarcodeCaptureActivity.class);
                            intent.putExtra(BarcodeCaptureActivity.AutoFocus, true);
                            intent.putExtra(BarcodeCaptureActivity.UseFlash, false);
                            view.getContext().startActivity(intent);

                        }
                        return true;
                    }
                })
                .build();
    }
}

