package com.example.harsh.testdemo.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * Created by harsh on 1/21/2017.
 */

public class NetworkUtils {

    /**
     * Return true if the device has an active data connection
     */
    public static boolean isOnline(Context context) {
        ConnectivityManager connManager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo activeNetwork = connManager.getActiveNetworkInfo();
        return (activeNetwork != null) && activeNetwork.isConnected();
    }
}
