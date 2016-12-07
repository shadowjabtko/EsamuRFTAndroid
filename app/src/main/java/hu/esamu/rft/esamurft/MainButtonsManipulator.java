package hu.esamu.rft.esamurft;

import android.media.Image;
import android.util.TypedValue;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.logging.Logger;

/**
 * Created by ShadowJabtko on 2016.12.05..
 */

public class MainButtonsManipulator {

    public static final int NUMBER_OF_BUTTONS=6;
    public static final int MODIFIED_HEIGHT_IN_DP=60;

    public static final int INVENTORY_POSITION=0;
    public static final int MAP_POSITION=1;
    public static final int BASE_POSITION=2;
    public static final int CAMERA_POSITION=3;
    public static final int QUEST_POSITION=4;
    public static final int RECIPES_POSITION=5;

    private ArrayList<ImageButton> imageButtons;
    private ArrayList<String> imageButtonTexts;
    private TextView textView;

    private int pixelHeight;

    public MainButtonsManipulator(int pixelHeight, TextView textView){
        imageButtons = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_BUTTONS; i++) {
            imageButtons.add(null);
        }

        imageButtonTexts = new ArrayList<>();
        for (int i = 0; i < NUMBER_OF_BUTTONS; i++) {
            imageButtonTexts.add(null);
        }

        this.pixelHeight=pixelHeight;
        this.textView=textView;
    }

    public void addImageButton(ImageButton imageButton,String imageButtonText,int position) throws Exception {
        if (position < NUMBER_OF_BUTTONS){
            imageButtons.set(position,imageButton);
            imageButtonTexts.set(position,imageButtonText);
        } else {
            throw new Exception("Something wrong with main buttons!");
        }
    }

    public void setButtonsLayoutToDefault(){
        for (ImageButton imageButton : imageButtons) {
            setButtonLayoutToDefault(imageButton);
        }
    }

    private void setButtonLayoutToDefault(ImageButton imageButton){
        RelativeLayout.LayoutParams currentLayoutParams = (RelativeLayout.LayoutParams) imageButton.getLayoutParams();
        currentLayoutParams.height=RelativeLayout.LayoutParams.WRAP_CONTENT;
        currentLayoutParams.width=RelativeLayout.LayoutParams.WRAP_CONTENT;
        imageButton.setLayoutParams(currentLayoutParams);
    }

    public void setButtonLayoutToHigher(int position){
        ImageButton imageButton = imageButtons.get(position);
        RelativeLayout.LayoutParams currentLayoutParams = (RelativeLayout.LayoutParams) imageButton.getLayoutParams();
        currentLayoutParams.height=pixelHeight;
        currentLayoutParams.width=RelativeLayout.LayoutParams.WRAP_CONTENT;
        imageButton.setLayoutParams(currentLayoutParams);

    }

    public void setTextViewTitle(int position) {
        textView.setText(imageButtonTexts.get(position));
    }
}
