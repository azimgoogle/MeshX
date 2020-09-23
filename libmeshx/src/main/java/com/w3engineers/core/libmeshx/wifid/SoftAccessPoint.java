package com.w3engineers.core.libmeshx.wifid;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.text.TextUtils;

import com.w3engineers.core.libmeshx.http.nanohttpd.util.AndroidUtil;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import timber.log.Timber;

import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION;

/**
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <br>----------------------------------------------------------------------------
 * <br>Created by: Ahmed Mohmmad Ullah (Azim) on [2019-03-28 at 11:13 AM].
 * <br>----------------------------------------------------------------------------
 * <br>Project: MeshX.
 * <br>Code Responsibility: <Purpose of code>
 * <br>----------------------------------------------------------------------------
 * <br>Edited by :
 * <br>1. <First Editor> on [2019-03-28 at 11:13 AM].
 * <br>2. <Second Editor>
 * <br>----------------------------------------------------------------------------
 * <br>Reviewed by :
 * <br>1. <First Reviewer> on [2019-03-28 at 11:13 AM].
 * <br>2. <Second Reviewer>
 * <br>============================================================================
 **/
public class SoftAccessPoint implements WifiP2pManager.ConnectionInfoListener, WifiP2pManager.ChannelListener {

    private final int DELAY = 60 * 1000;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;
    private Context mContext;
    private PeerReceiver mPeerReceiver;
    private String mNetworkName, mPassphrase, mInetAddress;
    private String mInstanceName;
    private Runnable mBroadcastScheduler = () -> {
        stopLocalServices();
        AndroidUtil.sleep(5 * 1000);
        startLocalService(mInstanceName);
    };

    public SoftAccessPoint(Context context) {
        mContext = context;
    }

    private WifiP2pManager.GroupInfoListener mGroupInfoListener = new WifiP2pManager.GroupInfoListener() {
        @Override
        public void onGroupInfoAvailable(WifiP2pGroup group) {
            try {
                Collection<WifiP2pDevice> devices = group.getClientList();

                int numm = 0;
                for (WifiP2pDevice peer : devices) {
                    numm++;
                    Timber.d("Client " + numm + " : "  + peer.deviceName + " " + peer.deviceAddress);
                }

                if(!TextUtils.isEmpty(mNetworkName) && !TextUtils.isEmpty(mPassphrase) &&
                        mNetworkName.equals(group.getNetworkName()) && mPassphrase.equals(group.getPassphrase())){

                    Timber.d("Already have local service for " + mNetworkName + " ," + mPassphrase);

                } else {

                    mNetworkName = group.getNetworkName();
                    mPassphrase = group.getPassphrase();
                    mInstanceName = group.getNetworkName().replace("DIRECT-",
                            "") + ":" + group.getPassphrase() /*+
                            ":1-loremepsumlorempsumloremepsum" + ":2-loremepsumlorempsumloremepsum"+
                            ":3-loremepsumlorempsumloremepsum" + ":4-loremepsumlorempsumloremepsum"*/;
                    startLocalService(mInstanceName);
                }
            } catch(Exception e) {
                e.printStackTrace();
            }
        }
    };

    public boolean start() {

        mWifiP2pManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);

        if (mWifiP2pManager == null) {
            return false;
        }

        mChannel = mWifiP2pManager.initialize(mContext, mContext.getMainLooper(), this);

        mPeerReceiver = new PeerReceiver(mP2PStateListener);
        IntentFilter filter = new IntentFilter();
        filter.addAction(WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WIFI_P2P_CONNECTION_CHANGED_ACTION);
        mContext.registerReceiver(mPeerReceiver, filter);

        mWifiP2pManager.createGroup(mChannel, new WifiP2pManager.ActionListener() {
            @Override
            public void onSuccess() {
                Timber.d("Creating Soft AP");
            }

            @Override
            public void onFailure(int reason) {
                Timber.d("Soft AP Failed. Reason %d", reason);
            }
        });

        return true;
    }

    public void removeGroup() {
        mWifiP2pManager.removeGroup(mChannel,new WifiP2pManager.ActionListener() {
            public void onSuccess() {
                Timber.d("Cleared Local Group ");
            }

            public void onFailure(int reason) {
                Timber.d("Clearing Local Group failed, error code %d", reason);
            }
        });
    }

    private void startLocalService(String instance) {

        AndroidUtil.post(mBroadcastScheduler, DELAY);

        Map<String, String> record = new HashMap<>();
        record.put("okay", "available");
//        record.put("available", "Lorem ipsum dolor sit amet, consectetur adipiscing elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua. Ut enim ad minim veniam, quis nostrud exercitation ullamco laboris nisi ut aliquip ex ea commodo consequat. Duis aute irure dolor in reprehenderit in voluptate velit esse cillum dolore eu fugiat nulla pariatur. Excepteur sint occaecat cupidatat non proident, sunt in culpa qui officia deserunt mollit anim id est laborum.");

        WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance( instance,
                Constants.Service.TYPE, record);

        Timber.d("Add local service :%s", instance);
        mWifiP2pManager.addLocalService(mChannel, service, new WifiP2pManager.ActionListener() {
            public void onSuccess() {
                Timber.d("Added local service");
            }

            public void onFailure(int reason) {
                Timber.d("Adding local service failed, error code %s", reason);
            }
        });
    }

    public void stopLocalServices() {

        mWifiP2pManager.clearLocalServices(mChannel, new WifiP2pManager.ActionListener() {
            public void onSuccess() {
                Timber.d("Cleared local services");
            }

            public void onFailure(int reason) {
                Timber.d("Clearing local services failed, error code %d", reason);
            }
        });
    }

    public void Stop() {
        mNetworkName = mPassphrase = null;
        try {
            mContext.unregisterReceiver(mPeerReceiver);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        }
        AndroidUtil.remove(mBroadcastScheduler);
        stopLocalServices();
        removeGroup();
    }

    private P2PStateListener mP2PStateListener = new P2PStateListener() {
        @Override
        public void onP2PStateChange(int state) {

        }

        @Override
        public void onP2PPeersStateChange() {

        }

        @Override
        public void onP2PConnected() {
            mWifiP2pManager.requestConnectionInfo(mChannel, SoftAccessPoint.this);
        }

        @Override
        public void onP2PDisconnected() {

        }

        @Override
        public void onP2PPeersDiscoveryStarted() {

        }

        @Override
        public void onP2PPeersDiscoveryStopped() {

        }
    };


    @Override
    public void onChannelDisconnected() {

    }

    @Override
    public void onConnectionInfoAvailable(WifiP2pInfo info) {

        if (info.isGroupOwner) {

            mInetAddress = info.groupOwnerAddress.getHostAddress();
            mWifiP2pManager.requestGroupInfo(mChannel, mGroupInfoListener);
        }
    }
}
