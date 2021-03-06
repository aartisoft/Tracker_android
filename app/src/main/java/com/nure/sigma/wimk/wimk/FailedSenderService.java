package com.nure.sigma.wimk.wimk;

import android.app.IntentService;
import android.content.Intent;
import android.content.Context;
import android.content.SharedPreferences;
import android.location.Location;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.util.Pair;

import com.nure.sigma.wimk.wimk.logic.DataSender;
import com.nure.sigma.wimk.wimk.logic.Info;
import com.nure.sigma.wimk.wimk.logic.MyHttpResponse;
import com.nure.sigma.wimk.wimk.logic.MyNotification;
import com.nure.sigma.wimk.wimk.logic.Util;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class FailedSenderService extends IntentService {

    public FailedSenderService() {
        super("FailedSenderService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        ConnectivityManager connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();
        if (networkInfo != null && networkInfo.isConnected()) {
            MyNotification.cancelNotificationOfDroppedInternet(this);
            Util.logRecord(networkInfo.getTypeName());
            Util.fillFileList(getApplicationContext());

            Info info = Info.getInstance();

            Util.logRecord(String.valueOf(info.getFailedLocations().size()));
            if (info.getFailedLocations() != null && !info.getFailedLocations().isEmpty()) {
                Util.logRecord("Sending failed locations");
                Util.logRecord(networkInfo.getTypeName());

                SharedPreferences temp = getApplicationContext().getSharedPreferences(Info.PASSWORD, 0);
                ArrayList<Pair<Location, String>> tempList = new ArrayList<>();

                for (Pair<Location, String> pair : info.getFailedLocations()) {
                    Util.logRecord("Current item is " + pair.toString());
                    List<Pair<String, String>> pairs = info.getParentAndChildLoginsListForHttp();
                    pairs.add(new Pair<>(Info.LONGITUDE, String.valueOf(pair.first.getLongitude())));
                    pairs.add(new Pair<>(Info.LATITUDE, String.valueOf(pair.first.getLatitude())));
                    pairs.add(new Pair<>(Info.TIME, (new SimpleDateFormat("yyyy-MM-dd HH:mm:ss"))
                            .format(new Date(pair.first.getTime()))));
                    pairs.add(new Pair<>(Info.BATTERY_LEVEL, pair.second));
                    pairs.add(new Pair<>(Info.POINT_TYPE, Info.STORED));
                    DataSender dataSender = new DataSender();
                    MyHttpResponse myHttpResponse = dataSender.httpPostQuery(
                            Info.MOBILE_GET_POINT_URL, pairs, Info.WAIT_TIME);
                    Util.logRecord(String.valueOf(myHttpResponse.getErrorCode()));
                    if (myHttpResponse.getErrorCode() != MyHttpResponse.OK) {
                        tempList.add(new Pair<Location, String>(pair.first, pair.second));
                    }
                }
                info.clearFailedLocations();
                for (Pair<Location, String> pair : tempList) {
                    Util.addToFileList(pair, getApplicationContext());
                }
                Util.logRecord("Stop sending failed locations");
            }
        }
        else {
            if (Info.getInstance().isBackgroundserviceRunning(this))
            MyNotification.showNotificationOfDroppedInternet(this);
        }
    }


}
