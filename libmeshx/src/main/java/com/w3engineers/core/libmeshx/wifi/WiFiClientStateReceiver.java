package com.w3engineers.core.libmeshx.wifi;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;

/**
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <br>----------------------------------------------------------------------------
 * <br>Created by: Ahmed Mohmmad Ullah (Azim) on [2019-03-28 at 1:02 PM].
 * <br>----------------------------------------------------------------------------
 * <br>Project: MeshX.
 * <br>Code Responsibility: <Purpose of code>
 * <br>----------------------------------------------------------------------------
 * <br>Edited by :
 * <br>1. <First Editor> on [2019-03-28 at 1:02 PM].
 * <br>2. <Second Editor>
 * <br>----------------------------------------------------------------------------
 * <br>Reviewed by :
 * <br>1. <First Reviewer> on [2019-03-28 at 1:02 PM].
 * <br>2. <Second Reviewer>
 * <br>============================================================================
 **/
public class WiFiClientStateReceiver {

    private enum ConnectionState {
        CONNECTED,
        CONNECTING,
        DISCONNECTED
    }

    private WiFiClientState mWiFiClientState;
    private ConnectionState mConnectionState = ConnectionState.DISCONNECTED;
    private ConnectivityManager mConnectivityManager;
    private ConnectivityManager.NetworkCallback mNetworkCallback;

    public WiFiClientStateReceiver(Context context, final WiFiClientState wiFiClientState) {

        mWiFiClientState = wiFiClientState;

        mConnectivityManager = (ConnectivityManager)
                context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);

        mNetworkCallback = new ConnectivityManager.NetworkCallback() {
            /**
             * @param network
             */
            @Override
            public void onAvailable(Network network) {

                if(mConnectionState != ConnectionState.CONNECTED &&
                        mConnectivityManager.getActiveNetworkInfo().getType() == ConnectivityManager.TYPE_WIFI) {

                    mConnectionState = ConnectionState.CONNECTED;
                    mWiFiClientState.onConnected();
                }
            }

            /**
             * @param network
             */
            @Override
            public void onLost(Network network) {
                if(mConnectionState != ConnectionState.DISCONNECTED) {
                    mConnectionState = ConnectionState.DISCONNECTED;
                    mWiFiClientState.onDisconnected();
                }
            }
        };

        mConnectivityManager.registerNetworkCallback(
                builder.build(),
                mNetworkCallback
        );
    }

    public void destroy() {
        mConnectivityManager.unregisterNetworkCallback(mNetworkCallback);
    }
}
