package com.w3engineers.core.libmeshx.wifid;

import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Handler;
import android.text.TextUtils;

import com.w3engineers.core.libmeshx.http.nanohttpd.util.AndroidUtil;

import java.util.Map;

import timber.log.Timber;

import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION;

/**
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <br>----------------------------------------------------------------------------
 * <br>Created by: Ahmed Mohmmad Ullah (Azim) on [2019-03-28 at 12:19 PM].
 * <br>----------------------------------------------------------------------------
 * <br>Project: MeshX.
 * <br>Code Responsibility: <Purpose of code>
 * <br>----------------------------------------------------------------------------
 * <br>Edited by :
 * <br>1. <First Editor> on [2019-03-28 at 12:19 PM].
 * <br>2. <Second Editor>
 * <br>----------------------------------------------------------------------------
 * <br>Reviewed by :
 * <br>1. <First Reviewer> on [2019-03-28 at 12:19 PM].
 * <br>2. <Second Reviewer>
 * <br>============================================================================
 **/

/**
 * Discovers peers and provided service type. If found then call {@link #onDesiredServiceFound(String, String, String)}
 */
public abstract class P2PServiceSearcher implements WifiP2pManager.ChannelListener {

    private enum ServiceState{
        NONE,
        DiscoverPeer,
        DiscoverService
    }
    private Context mContext;
    private WifiP2pManager mWifiP2pManager;
    private WifiP2pManager.Channel mChannel;
    private PeerReceiver mPeerReceiver;
    private WifiP2pManager.PeerListListener mPeerListListener;
    private WifiP2pManager.DnsSdServiceResponseListener mDnsSdServiceResponseListener;
    private ServiceState mServiceState = ServiceState.NONE;
    String mServiceType = Constants.Service.TYPE;
    public String mSearchingForMac;
    protected Runnable mSearcherRescheduler = () -> {
        stopInternal();
        AndroidUtil.sleep(5 * 1000);
        start();
    };

    public P2PServiceSearcher(Context context) {
        mContext = context;
    }

    protected abstract void onDesiredServiceFound(String ssid, String passPhrase, String mac);

    public boolean start() {

        mWifiP2pManager = (WifiP2pManager) mContext.getSystemService(Context.WIFI_P2P_SERVICE);

        if(mWifiP2pManager == null) {
            return false;
        }

        mChannel = mWifiP2pManager.initialize(mContext, mContext.getMainLooper(), this);

        mPeerReceiver = new PeerReceiver(mP2PStateListener);
        IntentFilter filter = new IntentFilter();
        filter.addAction(WIFI_P2P_STATE_CHANGED_ACTION);
        filter.addAction(WIFI_P2P_CONNECTION_CHANGED_ACTION);
        filter.addAction(WIFI_P2P_DISCOVERY_CHANGED_ACTION);
        filter.addAction(WIFI_P2P_PEERS_CHANGED_ACTION);
        mContext.registerReceiver(mPeerReceiver, filter);

        mPeerListListener = new WifiP2pManager.PeerListListener() {
            @Override
            public void onPeersAvailable(WifiP2pDeviceList peers) {

                final WifiP2pDeviceList pers = peers;
                int numm = 0;
                for (WifiP2pDevice peer : pers.getDeviceList()) {
                    numm++;
                    Timber.d("\t" + numm + ": "  + peer.deviceName + " " + peer.deviceAddress);
                }

                if(numm > 0){
                    startServiceDiscovery();
                }else{
                    startPeerDiscovery();
                }
            }
        };

        mDnsSdServiceResponseListener = new WifiP2pManager.DnsSdServiceResponseListener() {

            public void onDnsSdServiceAvailable(String instanceName, String serviceType, WifiP2pDevice device) {

                Timber.d("[Lazy-Network]instance:%s-service:%s", instanceName, serviceType);
                if (serviceType.startsWith(mServiceType) && (TextUtils.isEmpty(mSearchingForMac) ||
                        mSearchingForMac.equals(device.deviceAddress))) {


                    String[] separated = instanceName.split(":");
                    if(separated.length > 1) {
                        final String networkSSID = separated[0];
                        final String networkPass = separated[1];

                        onDesiredServiceFound("DIRECT-"+networkSSID, networkPass,
                                device.deviceAddress);
                    }

                } else {
                    Timber.d("Not our Service, :" + Constants.Service.TYPE + "!=" + serviceType + ":");
                }

                startPeerDiscovery();
            }
        };

        mWifiP2pManager.setDnsSdResponseListeners(mChannel, mDnsSdServiceResponseListener, new WifiP2pManager.DnsSdTxtRecordListener() {
            @Override
            public void onDnsSdTxtRecordAvailable(String fullDomainName, Map<String, String> txtRecordMap, WifiP2pDevice srcDevice) {
                Timber.d("[Lazy-Network]DomainName:%s-Map:%s", fullDomainName, txtRecordMap);
            }
        });

        /*mWifiP2pManager.setUpnpServiceResponseListener(mChannel, new WifiP2pManager.UpnpServiceResponseListener() {
            @Override
            public void onUpnpServiceAvailable(List<String> uniqueServiceNames, WifiP2pDevice srcDevice) {

            }
        });*/
        startPeerDiscovery();

        return true;
    }

    private void startServiceDiscovery() {

//        WifiP2pDnsSdServiceRequest request = WifiP2pDnsSdServiceRequest.newInstance();
        WifiP2pDnsSdServiceRequest request = WifiP2pDnsSdServiceRequest.newInstance(Constants.Service.TYPE);
        final Handler handler = new Handler();
        mWifiP2pManager.addServiceRequest(mChannel, request, new WifiP2pManager.ActionListener() {

            public void onSuccess() {
                Timber.d("Added service request");
                handler.postDelayed(new Runnable() {
                    //There are supposedly a possible race-condition bug with the service discovery
                    // thus to avoid it, we are delaying the service discovery start here
                    public void run() {
                        mWifiP2pManager.discoverServices(mChannel, new WifiP2pManager.ActionListener() {
                            public void onSuccess() {
                                Timber.d("Started service discovery");
                                mServiceState = ServiceState.DiscoverService;
                            }
                            public void onFailure(int reason) {
                                Timber.d("Starting service discovery failed, error code %s", reason);}
                        });
                    }
                }, Constants.Service.DISCOVERY_DELAY);
            }

            public void onFailure(int reason) {
                Timber.d("Adding service request failed, error code %s", reason);
                // No point starting service discovery
            }
        });

    }

    private void startPeerDiscovery() {
        mWifiP2pManager.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            public void onSuccess() {
                mServiceState = ServiceState.DiscoverPeer;
                Timber.d("Started peer discovery");
            }
            public void onFailure(int reason) {
                Timber.d("Starting peer discovery failed, error code %s", reason);}
        });
    }

    private void stopPeerDiscovery() {
        mWifiP2pManager.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            public void onSuccess() {
                Timber.d("Stopped peer discovery");}
            public void onFailure(int reason) {
                Timber.d("Stopping peer discovery failed, error code %d", reason);}
        });
    }

    private void stopServiceDiscovery() {
        mWifiP2pManager.clearServiceRequests(mChannel, new WifiP2pManager.ActionListener() {
            public void onSuccess() {
                Timber.d("Cleared service requests");}
            public void onFailure(int reason) {
                Timber.d("Clearing service requests failed, error code %d", reason);}
        });
    }


    public void Stop() {

        AndroidUtil.removeBackground(mSearcherRescheduler);
        stopInternal();
    }
    private void stopInternal() {
        try {
            mContext.unregisterReceiver(mPeerReceiver);
        } catch (IllegalArgumentException ex) {
            ex.printStackTrace();
        }
        stopServiceDiscovery();
        stopPeerDiscovery();
    }


    private P2PStateListener mP2PStateListener = new P2PStateListener() {
        @Override
        public void onP2PStateChange(int state) {

        }

        @Override
        public void onP2PPeersStateChange() {
            if(mServiceState != ServiceState.DiscoverService) {
                mWifiP2pManager.requestPeers(mChannel, mPeerListListener);
            }
        }

        @Override
        public void onP2PConnected() {
            startPeerDiscovery();
        }

        @Override
        public void onP2PDisconnected() {
            startPeerDiscovery();
        }

        @Override
        public void onP2PPeersDiscoveryStarted() {

        }

        @Override
        public void onP2PPeersDiscoveryStopped() {
            startPeerDiscovery();
        }
    };

    @Override
    public void onChannelDisconnected() {

    }
}
