package hu.esamu.rft.esamurft.utility;

import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import hu.esamu.rft.esamurft.R;

/**
 * Class that helps in the configuration of the application.
 * @author Jakab Ádám
 */
public class ConfigHelper {
    private static final String TAG = "ConfigHelper";

    /**
     * Reads the 'res/raw/config.properties' and returns the proper element.
     * */
    public static String getConfigValue(Context context, String name) {
        Resources resources = context.getResources();

        try {
            Properties properties = new Properties();
            InputStream rawResource = resources.openRawResource(R.raw.config);
            properties.load(rawResource);
            return properties.getProperty(name);
        } catch (Resources.NotFoundException e) {
            Log.e(TAG, "Unable to find the config file: " + e.getMessage());
        } catch (IOException e) {
            Log.e(TAG, "Failed to open config file.");
        }

        return null;
    }

}
