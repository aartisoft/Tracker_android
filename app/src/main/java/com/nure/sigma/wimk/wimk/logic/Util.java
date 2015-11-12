package com.nure.sigma.wimk.wimk.logic;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.Location;
import android.location.LocationManager;
import android.os.BatteryManager;
import android.util.Log;
import android.util.Pair;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;


public class Util {

    public static void fillFileList(Context context) {
        ObjectInputStream in = null;
        Info.FILE_LIST = new ArrayList<>();
        try {
            JSONParser parser = new JSONParser();

            try {

                Object obj = parser.parse(new FileReader(
                        context.getFilesDir().getPath() + Info.LOCATIONS_FILE));

                JSONObject jsonObject = (JSONObject) obj;
                JSONArray array = (JSONArray) jsonObject.get("Locations list");
                Iterator<JSONObject> iterator = array.iterator();
                while (iterator.hasNext()) {
                    JSONObject object = iterator.next();
                    Location location = new Location(LocationManager.PASSIVE_PROVIDER);
                    location.setLatitude(Double.valueOf(object.get("latitude").toString()));
                    location.setTime(Long.valueOf(object.get("time").toString()));
                    location.setLongitude(Double.valueOf(object.get("longitude").toString()));
                    Info.FILE_LIST.add(new Pair<Location, String>(location, object.get("battery_level").toString()));
                    array.remove(object);
                }
                System.out.println("Successfully Read JSON Object from File...");
                System.out.println("\nJSON Object: " + jsonObject);
            } catch (Exception e) {
                e.printStackTrace();
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    public static void writeListToFile(Context context) {
        ObjectOutputStream out = null;
        try {
            File file = new File(context.getFilesDir().getPath() + Info.LOCATIONS_FILE);
            if (!file.exists()) {
                file.createNewFile();
            }
            JSONObject obj = new JSONObject();
            JSONArray locations = new JSONArray();
            for (Pair<Location, String> pair : Info.FILE_LIST) {
                JSONObject location = new JSONObject();
                location.put("latitude", pair.first.getLatitude());
                location.put("longitude", pair.first.getLongitude());
                location.put("time", pair.first.getTime());
                location.put("battery_level", pair.second);
                locations.add(location);
            }
            obj.put("Locations list", locations);
            FileWriter fileWriter = new FileWriter(file);
            fileWriter.write(obj.toJSONString());
            System.out.println("Successfully Copied JSON Object to File...");
            System.out.println("\nJSON Object: " + obj);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    public static void addToFileList(Pair<Location, String> pair, Context context) {
        fillFileList(context);
        Info.FILE_LIST.add(pair);
        writeListToFile(context);
    }


    //Getting battery level.
    public static int getBatteryLevel(Context context) {
        Intent batteryIntent = context.registerReceiver(null, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
        int level = batteryIntent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
        int scale = batteryIntent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
        if (level == -1 || scale == -1) {
            return 50;
        }

        return (int) (((float) level / (float) scale) * 100.0f);
    }

    public static String formatLocation(Location location) {
        if (location == null) {
            return "";
        }
        return String.format(
                "Coordinates: lat = %1$.4f, lon = %2$.4f, time = %3$tF %3$tT",
                location.getLatitude(), location.getLongitude(), new Date(
                        location.getTime()));
    }

    public static String formatBatteryLevel(float level) {
        if (level == 0)
            return "";
        return String.format("Battery level = %1$.2f", level);
    }

    public static void log(String record) {
        Log.e("andstepko", record);
    }

    public static void logRecord(String record) {
        Log.i(Info.getInstance().SERVICE_TAG, record);
    }

    public static boolean isGPSMoreAccurateLocation(Location locationGPS,
                                                    Location locationNETWORK, Location locationPASSIVE) {
        try {

            return (locationGPS.getAccuracy() > locationNETWORK.getAccuracy())
                    && (locationGPS.getAccuracy() > locationPASSIVE.getAccuracy());
        } catch (NullPointerException e) {
            if (locationGPS != null) {
                return true;
            } else {
                return false;
            }
        }
    }

    public static boolean isNetworkMoreAccurateLocation(Location locationGPS,
                                                        Location locationNETWORK, Location locationPASSIVE) {
        try {

            return (locationNETWORK.getAccuracy() > locationGPS.getAccuracy())
                    && (locationNETWORK.getAccuracy() > locationPASSIVE.getAccuracy());
        } catch (NullPointerException e) {
            if (locationNETWORK != null) {
                return true;
            } else {
                return false;
            }
        }
    }
}
