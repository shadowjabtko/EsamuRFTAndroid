package hu.esamu.rft.esamurft;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.app.FragmentManager;

import hu.esamu.rft.esamurft.base.BaseFragment;
import hu.esamu.rft.esamurft.camera.CameraFragment;
import hu.esamu.rft.esamurft.inventory.InventoryFragment;
import hu.esamu.rft.esamurft.map.MapFragment;
import hu.esamu.rft.esamurft.quests.QuestsFragment;
import hu.esamu.rft.esamurft.recipes.RecipesFragment;

/**
 * Created by ShadowJabtko on 2016.12.04..
 */

public class GamePageAdapter extends FragmentPagerAdapter {

    private static int NUM_ITEMS = MainButtonsManipulator.NUMBER_OF_BUTTONS;


    public GamePageAdapter(FragmentManager fragmentManager){
        super(fragmentManager);
    }

    @Override
    public int getCount() {
        return NUM_ITEMS;
    }


    @Override
    public Fragment getItem(int position) {
        switch (position){
            case MainButtonsManipulator.INVENTORY_POSITION:
                return InventoryFragment.newInstance();
            case MainButtonsManipulator.MAP_POSITION:
                return MapFragment.newInstance();
            case MainButtonsManipulator.BASE_POSITION:
                return BaseFragment.newInstance();
            case MainButtonsManipulator.CAMERA_POSITION:
                return CameraFragment.newInstance();
            case MainButtonsManipulator.QUEST_POSITION:
                return QuestsFragment.newInstance();
            case MainButtonsManipulator.RECIPES_POSITION:
                return RecipesFragment.newInstance();
            default:
                return null;
        }
    }

}
