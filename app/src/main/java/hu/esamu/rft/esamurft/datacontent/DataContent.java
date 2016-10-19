package hu.esamu.rft.esamurft.datacontent;

import android.location.Location;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import hu.esamu.rft.esamurft.EsamuRFTMessages;

public class DataContent {

    public static final List<DataItem> ITEMS = new ArrayList<DataItem>();

    public static final Map<String, DataItem> ITEM_MAP = new HashMap<String, DataItem>();

    private static final int COUNT = 25;

    static {
        for (int i = 1; i <= COUNT; i++) {
            addItem(createDataItem(i));
        }
    }

    private static void addItem(DataItem item) {
        ITEMS.add(item);
        ITEM_MAP.put(item.id, item);
    }

    private static DataItem createDataItem(int position) {

        Location location = new Location("loc");
        location.setLongitude(45);
        location.setLatitude(20);
        return new DataItem(String.valueOf(position), "Item " + position, makeDetails(position), location);
    }

    private static String makeDetails(int position) {
        StringBuilder builder = new StringBuilder();
        builder.append("Details about Item: ").append(position);
        for (int i = 0; i < position; i++) {
            builder.append("\nMore details information here.");
        }
        return builder.toString();
    }

    public static class DataItem {
        public final String id;
        public final String content;
        public final String details;
        public final Location location;

        public DataItem(String id, String content, String details, Location location) {
            this.id = id;
            this.content = content;
            this.details = details;
            this.location = location;
        }

        @Override
        public String toString() {
            return content;
        }
    }
}
