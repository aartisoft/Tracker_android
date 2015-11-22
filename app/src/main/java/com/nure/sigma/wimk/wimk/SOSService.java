package com.nure.sigma.wimk.wimk;

import android.app.IntentService;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import com.nure.sigma.wimk.wimk.logic.Info;
import com.nure.sigma.wimk.wimk.logic.LocationSender;
import com.nure.sigma.wimk.wimk.logic.MyHttpResponse;


public class SOSService extends IntentService {

    private Handler handler;

    public SOSService() {
        super(SOSService.class.getName());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        handler = new Handler();
        SharedPreferences settings = getSharedPreferences(Info.PASSWORD, 0);
        int idChild = settings.getInt(Info.ID_CHILD, -1);
        if (idChild != -1) {
            Log.i(Info.SERVICE_TAG, "Service Starting!");
            LocationSender locationSender = new LocationSender(Info.SOS, this);
            MyHttpResponse myHttpResponse = locationSender.sendLocation();
            Log.i(Info.SERVICE_TAG, "Service Stopping!");
        }
        else {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Toast.makeText(getApplicationContext(), "Cann`t find ID of child. Please, login in application, before using SOS button.", Toast.LENGTH_LONG).show();
                }
            });
        }
    }
}
