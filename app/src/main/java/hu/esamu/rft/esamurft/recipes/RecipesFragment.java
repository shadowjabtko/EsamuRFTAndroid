package hu.esamu.rft.esamurft.recipes;

import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import hu.esamu.rft.esamurft.R;

/**
 * Created by ShadowJabtko on 2016.12.04..
 */

public class RecipesFragment extends Fragment{

    public static RecipesFragment newInstance(){
        return new RecipesFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_recipes, container, false);
    }
}
