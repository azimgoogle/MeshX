package com.w3engineers.core.libmeshx.wifi;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.text.TextUtils;

import com.w3engineers.core.libmeshx.discovery.MeshXListener;

import java.util.List;

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

        WifiInfo wifiInfo = mWifiManager.getConnectionInfo();
        String ssid = wifiInfo.getSSID().replace("\"", "");
        if(mTargetSSID.equals(ssid)) {
            return true;
        }

        mWifiManager.disconnect();

        Timber.d("Connection_log Initial-%s", mTargetSSID);

        WifiConfiguration wifiConfig = new WifiConfiguration();
        wifiConfig.SSID = String.format("\"%s\"", mTargetSSID);
        wifiConfig.preSharedKey = String.format("\"%s\"", mPassPhrase);

        //// TODO: 7/17/2019
        //It automates SSID connection whenever available so it gives us huge performance benefit.
        //To leverage the benefit we need to have according support. Would add later
//        wifiConfig.hiddenSSID = true;

        int networkId = getConfiguredWiFiNetworkId(mTargetSSID);
        if (networkId != -1) {
            wifiConfig.networkId = networkId;
            Timber.d("Connection_log %s-%s", networkId, mTargetSSID);
            networkId = mWifiManager.updateNetwork(wifiConfig);
            Timber.d("Connection_log %s", networkId);
            if (networkId == -1) {
                networkId = this.mWifiManager.addNetwork(wifiConfig);
                Timber.d("Connection_log %s", networkId);
            }
        } else {
            networkId = this.mWifiManager.addNetwork(wifiConfig);
            Timber.d("Connection_log %s-%s", networkId, mTargetSSID);
        }
        mWifiManager.enableNetwork(networkId, true);
        return mWifiManager.reconnect();
    }


    public int getConfiguredWiFiNetworkId(String SSID) {
        if (TextUtils.isEmpty(SSID)) {
            return -1;
        }
        List<WifiConfiguration> configuredNetworks = mWifiManager.getConfiguredNetworks();

        if (configuredNetworks != null) {

            for (WifiConfiguration wifiConfiguration : configuredNetworks) {
                if (wifiConfiguration != null && wifiConfiguration.networkId != -1) {
                    if (SSID.equals(wifiConfiguration.SSID)) {
                        return wifiConfiguration.networkId;
                    }
                }
            }
        }

        return -1;
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
                mMeshXListener.onConnectedWith(connectedSSID.replaceAll("\"",""));
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
