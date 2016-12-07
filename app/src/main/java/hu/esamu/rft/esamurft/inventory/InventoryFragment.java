package hu.esamu.rft.esamurft.inventory;


import android.support.v4.app.Fragment;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.GridView;
import android.widget.Toast;

import java.util.ArrayList;

import hu.esamu.rft.esamurft.R;

/**
 * Created by ShadowJabtko on 2016.12.04..
 */

public class InventoryFragment extends Fragment {

    private GridView gridView;
    private ArrayList<Item> items;

    public static InventoryFragment newInstance(){
        return new InventoryFragment();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_inventory, container, false);

        items = new ArrayList<Item>();
        items.add(new Item(1, "Wood",1, "ic_launcher"));
        items.add(new Item(2, "Stone",2, "ic_launcher"));
        items.add(new Item(3, "Asd",6, "ic_launcher"));
        items.add(new Item(4, "Asd2",3, "ic_launcher"));
        items.add(new Item(5, "Asd3",2, "ic_launcher"));
        items.add(new Item(6, "Asd4",10, "ic_launcher"));
        items.add(new Item(7, "Asd5",20, "ic_launcher"));
        items.add(new Item(8, "Asd6",22, "ic_launcher"));
        items.add(new Item(9, "Asd7",5, "ic_launcher"));
        items.add(new Item(10, "Asd8",2, "ic_launcher"));
        items.add(new Item(11, "Asd9",7, "ic_launcher"));
        items.add(new Item(12, "Asd10",55, "ic_launcher"));
        gridView = (GridView) view.findViewById(R.id.gridViewInventory);
        gridView.setAdapter(new ItemAdapter(this.getContext(), items));

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> parent, View v,
                                    int position, long id) {
                Toast.makeText(
                        getActivity(),
                        items.get(position).getName(), Toast.LENGTH_SHORT).show();

            }
        });

        return view;
    }

}
