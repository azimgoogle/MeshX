package com.w3engineers.core.libmeshx.wifid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pManager;

import timber.log.Timber;

import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION;


/**
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <br>----------------------------------------------------------------------------
 * <br>Created by: Ahmed Mohmmad Ullah (Azim) on [2019-03-18 at 3:03 PM].
 * <br>----------------------------------------------------------------------------
 * <br>Project: MeshX.
 * <br>Code Responsibility: <Purpose of code>
 * <br>----------------------------------------------------------------------------
 * <br>Edited by :
 * <br>1. <First Editor> on [2019-03-18 at 3:03 PM].
 * <br>2. <Second Editor>
 * <br>----------------------------------------------------------------------------
 * <br>Reviewed by :
 * <br>1. <First Reviewer> on [2019-03-18 at 3:03 PM].
 * <br>2. <Second Reviewer>
 * <br>============================================================================
 **/
public class PeerReceiver extends BroadcastReceiver {

    private P2PStateListener mP2PStateListener;

    public PeerReceiver(P2PStateListener p2PStateListener) {

        this.mP2PStateListener = p2PStateListener;

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        String action = intent.getAction();
        Timber.d("Received intent: %s", action);

        if (mP2PStateListener == null)
            return;


        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);
            mP2PStateListener.onP2PStateChange(state);

        } else if (WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {

            mP2PStateListener.onP2PPeersStateChange();

        } else if (WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {
            //WifiP2pDevice device = intent.getParcelableExtra(EXTRA_WIFI_P2P_DEVICE);
            //addText("Local device: " + MyP2PHelper.deviceToString(device));
        } else if (WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION.equals(action)) {

            int state = intent.getIntExtra(WifiP2pManager.EXTRA_DISCOVERY_STATE, WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED);
            String persTatu = "Discovery state changed to ";

            if (state == WifiP2pManager.WIFI_P2P_DISCOVERY_STOPPED) {
                persTatu = persTatu + "Stopped.";
                mP2PStateListener.onP2PPeersDiscoveryStopped();

            } else if (state == WifiP2pManager.WIFI_P2P_DISCOVERY_STARTED) {

                mP2PStateListener.onP2PPeersDiscoveryStarted();

            } else {
                persTatu = persTatu + "unknown  " + state;
            }

        } else if (WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);
            if (networkInfo.isConnected()) {

                mP2PStateListener.onP2PConnected();

            } else {

                mP2PStateListener.onP2PDisconnected();


            }
        }
    }
}
