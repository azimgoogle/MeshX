package com.w3engineers.core.libmeshx.wifid;

/**
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <br>----------------------------------------------------------------------------
 * <br>Created by: Ahmed Mohmmad Ullah (Azim) on [2019-03-28 at 1:36 PM].
 * <br>----------------------------------------------------------------------------
 * <br>Project: MeshX.
 * <br>Code Responsibility: <Purpose of code>
 * <br>----------------------------------------------------------------------------
 * <br>Edited by :
 * <br>1. <First Editor> on [2019-03-28 at 1:36 PM].
 * <br>2. <Second Editor>
 * <br>----------------------------------------------------------------------------
 * <br>Reviewed by :
 * <br>1. <First Reviewer> on [2019-03-28 at 1:36 PM].
 * <br>2. <Second Reviewer>
 * <br>============================================================================
 **/

import android.content.Context;

import com.w3engineers.core.libmeshx.discovery.MeshXListener;
import com.w3engineers.core.libmeshx.discovery.MeshXLogListener;
import com.w3engineers.core.libmeshx.wifi.WiFiClient;

/**
 * Will use this Manager class for all WiFi direct or WiFiP2P related tasks
 */
public class WiFiDirectManagerLegacy {

    private Context mContext;
    private static WiFiDirectManagerLegacy sWiFiDirectManagerLegacy;
    private SoftAccessPoint mSoftAccessPoint;
    private SoftAccessPointSearcher mSoftAccessPointSearcher;
    private WiFiClient mWiFiClient;
    private MeshXLogListener mMeshXLogListener;
    private MeshXListener mMeshXListener;
    public WiFiDirectConfig mWiFiDirectConfig;
    private SoftAccessPointSearcher.ServiceFound mServiceFound = new SoftAccessPointSearcher.ServiceFound() {
        @Override
        public void onServiceFoundSuccess(String ssid, String passPhrase, String mac) {
            if(mMeshXLogListener != null) {
                mMeshXLogListener.onLog(ssid + ":: "+passPhrase+"::"+mac);
            }

            mWiFiClient = new WiFiClient(mContext, ssid, passPhrase);
            mWiFiClient.mMeshXListener = mMeshXListener;

            if(mSoftAccessPoint != null) {
                mSoftAccessPoint.Stop();
            }

            if(mSoftAccessPointSearcher != null) {
                mSoftAccessPointSearcher.Stop();
            }

            mWiFiClient.connect();

            if(mMeshXListener != null) {
                mMeshXListener.onMac(mac, ssid);
            }
        }
    };

    public synchronized static WiFiDirectManagerLegacy getInstance(Context context) {
        if(sWiFiDirectManagerLegacy == null) {
            synchronized (WiFiDirectManagerLegacy.class) {
                if(sWiFiDirectManagerLegacy == null) {
                    sWiFiDirectManagerLegacy = new WiFiDirectManagerLegacy(context);
                }
            }
        }
        return sWiFiDirectManagerLegacy;
    }

    /**
     * You must ensure to call {@link #getInstance(Context)} before this method. Otherwise it will
     * return null
     * @return
     */
    public static WiFiDirectManagerLegacy getInstance() {
        return sWiFiDirectManagerLegacy;
    }

    public WiFiDirectManagerLegacy(Context context) {
        mContext = context;
    }

    public void start() {

        if(mWiFiDirectConfig.mIsGO) {
            mSoftAccessPoint = new SoftAccessPoint(mContext);
            mSoftAccessPoint.start();
        }

        if(mWiFiDirectConfig.mIsLC) {
            mSoftAccessPointSearcher = new SoftAccessPointSearcher(mContext);
            mSoftAccessPointSearcher.setServiceFound(mServiceFound);

            mSoftAccessPointSearcher.start();
        }
    }

    public void destroy() {
        mContext = null;
        mSoftAccessPoint.Stop();
        mSoftAccessPointSearcher.Stop();
        mWiFiClient.destroy();
    }

    public void setMeshXLogListener(MeshXLogListener meshXLogListener) {
        mMeshXLogListener = meshXLogListener;
    }

    public void setMeshXListener(MeshXListener meshXListener) {
        mMeshXListener = meshXListener;
    }

    public void searchFor(String mac) {
        if(mSoftAccessPointSearcher != null) {
            mSoftAccessPointSearcher.mSearchingForMac = mac;
            mSoftAccessPointSearcher.Stop();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }

            mSoftAccessPointSearcher.start();
        }
    }

    public void stopBroadCast() {
        if(mSoftAccessPoint != null) {
            mSoftAccessPoint.stopLocalServices();
        }
    }
}
