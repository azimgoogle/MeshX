package com.w3engineers.core.libmeshx.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import com.w3engineers.core.libmeshx.discovery.MeshXListener;

import timber.log.Timber;

/**
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <br>----------------------------------------------------------------------------
 * <br>Created by: Ahmed Mohmmad Ullah (Azim) on [2019-03-28 at 12:57 PM].
 * <br>----------------------------------------------------------------------------
 * <br>Project: MeshX.
 * <br>Code Responsibility: <Purpose of code>
 * <br>----------------------------------------------------------------------------
 * <br>Edited by :
 * <br>1. <First Editor> on [2019-03-28 at 12:57 PM].
 * <br>2. <Second Editor>
 * <br>----------------------------------------------------------------------------
 * <br>Reviewed by :
 * <br>1. <First Reviewer> on [2019-03-28 at 12:57 PM].
 * <br>2. <Second Reviewer>
 * <br>============================================================================
 **/
public class WiFiClient implements WiFiClientState {

    private Context mContext;
    private String mTargetSSID, mPassPhrase;
    private WifiManager mWifiManager;
    private int mNetworkId;
    private WiFiClientStateReceiver mWiFiClientStateReceiver;
    public MeshXListener mMeshXListener;

    public WiFiClient(Context context, String ssid, String passPhrase) {
        this.mContext = context;
        this.mTargetSSID = ssid;
        this.mPassPhrase = passPhrase;
        mWifiManager = (WifiManager) mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        mWiFiClientStateReceiver = new WiFiClientStateReceiver(context, this);
    }

    /**
     * Start connecting with {@link #mTargetSSID} using {@link #mPassPhrase}
     * @return
     */
    public boolean connect() {


        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", mTargetSSID);
        wifiConfig.preSharedKey = String.format("\"%s\"", mPassPhrase);

        this.mNetworkId = this.mWifiManager.addNetwork(wifiConfig);
        mWifiManager.enableNetwork(mNetworkId, false);
        mWifiManager.reconnect();

        return true;
    }

    public void destroy() {
        if(mWiFiClientStateReceiver != null) {
            mWiFiClientStateReceiver.destroy();
        }
    }

    @Override
    public void onConnected() {
        Timber.d("Connected to WiFi");
        if(mWifiManager != null) {
            WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
            String connectedSSID = wifiInfo.getSSID();
            if(mMeshXListener != null) {
                mMeshXListener.onConnectedWith(connectedSSID);
            }
        }
    }

    @Override
    public void onDisconnected() {
        Timber.d("Disonnected from WiFi");
        if(mMeshXListener != null) {
            mMeshXListener.onDisConnected();
        }
    }
}
