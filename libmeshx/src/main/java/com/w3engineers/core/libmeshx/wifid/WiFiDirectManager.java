package com.w3engineers.core.libmeshx.wifid;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceInfo;
import android.net.wifi.p2p.nsd.WifiP2pDnsSdServiceRequest;
import android.os.Handler;
import android.text.TextUtils;

import com.w3engineers.core.libmeshx.discovery.MeshXListener;
import com.w3engineers.core.libmeshx.discovery.Peer;
import com.w3engineers.core.util.AddressUtil;

import java.util.HashMap;
import java.util.Map;

import static android.content.Context.WIFI_P2P_SERVICE;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION;
import static android.net.wifi.p2p.WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION;
import static android.os.Looper.getMainLooper;
import static com.w3engineers.core.util.LoggerUtil.addText;

/**
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <br>----------------------------------------------------------------------------
 * <br>Created by: Ahmed Mohmmad Ullah (Azim) on [2019-03-18 at 10:32 AM].
 * <br>----------------------------------------------------------------------------
 * <br>Project: MeshX.
 * <br>Code Responsibility: <Purpose of code>
 * <br>----------------------------------------------------------------------------
 * <br>Edited by :
 * <br>1. <First Editor> on [2019-03-18 at 10:32 AM].
 * <br>2. <Second Editor>
 * <br>----------------------------------------------------------------------------
 * <br>Reviewed by :
 * <br>1. <First Reviewer> on [2019-03-18 at 10:32 AM].
 * <br>2. <Second Reviewer>
 * <br>============================================================================
 **/
@Deprecated
public class WiFiDirectManager {

    private final long SERVICE_DISCOVERY_TIME_DELAY = 1000;

    private final String NULL_CONTEXT_MESSAGE = "Context can not be null";
    private final String SERVICE_TYPE = Constants.Service.TYPE;
    private final String GROUP_NAME = "meshX";
    private final String TXT_RECORD_PROP_AVAILABLE = "meshX";

    private Context mContext;
    private Handler mHandler;

    private WifiP2pManager mP2P;
    private WifiP2pManager.Channel mChannel;
    private BroadcastReceiver mReceiver;

    private WifiP2pManager.DnsSdServiceResponseListener serviceListener;
    private WifiP2pManager.PeerListListener peerListListener;
    private P2PConnectionListener mP2PConnectionListener;

    private MeshXListener mMeshXListener;

    private ServiceState mServiceState = ServiceState.NONE;
    private String mLastConnectedGO;
    private enum ServiceState {
        NONE,
        DiscoverPeer,
        DiscoverService,
        ConnectingWifi,
        QueryConnection,
        ConnectedAsOwner,
        ConnectedAsClient
    }

    private static WiFiDirectManager ourInstance;

    /**
     * Would return null if not initialized by {@link #getInstance(Context)}
     * @return
     */
    public static WiFiDirectManager getInstance() {
        return ourInstance;
    }

    public static synchronized WiFiDirectManager getInstance(Context context) {

        if (ourInstance == null) {
            synchronized (WiFiDirectManager.class) {
                if (ourInstance == null) {
                    ourInstance = new WiFiDirectManager(context);
                }
            }
        }
        return ourInstance;
    }

    private WiFiDirectManager(Context context) {
        mContext = context;
        mHandler = new Handler();

        init(mContext);
    }

    public void setMeshXListener(MeshXListener meshXListener) {
        mMeshXListener = meshXListener;
    }

    private void init(Context context) {

        if (context == null)
            throw new NullPointerException(NULL_CONTEXT_MESSAGE);//Hard string as no context

        mContext = context;
        mP2P = (WifiP2pManager) context.getSystemService(WIFI_P2P_SERVICE);
        p2pInit();
    }

    private void p2pInit() {

        if (mP2P == null) {
            addText("This device does not support Wi-Fi Direct");
        } else {{

            mP2PConnectionListener = new P2PConnectionListener();

            mChannel = mP2P.initialize(mContext, getMainLooper(), new WifiP2pManager.ChannelListener() {
                @Override
                public void onChannelDisconnected() {
                    addText("onChannelDisconnected");
                }
            });

            mReceiver = new PeerReceiver(new P2PState());

            IntentFilter filter;
            filter = new IntentFilter();
            filter.addAction(WIFI_P2P_STATE_CHANGED_ACTION);
            filter.addAction(WIFI_P2P_PEERS_CHANGED_ACTION);
            filter.addAction(WIFI_P2P_CONNECTION_CHANGED_ACTION);
            filter.addAction(WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);
            filter.addAction(WifiP2pManager.WIFI_P2P_DISCOVERY_CHANGED_ACTION);
            mContext.registerReceiver(mReceiver, filter);

            peerListListener = new WifiP2pManager.PeerListListener() {

                public void onPeersAvailable(WifiP2pDeviceList peers) {
                    addText("Discovered peers:");

                    final WifiP2pDeviceList pers = peers;
                    StringBuilder allInOne = new StringBuilder();
                    int num = 0;
                    for (WifiP2pDevice peer : pers.getDeviceList()) {
                        num++;
                        allInOne.append(AddressUtil.deviceToString(peer)).append(", ");
                        addText("\t" + AddressUtil.deviceToString(peer));
                    }
                    addText(num + " peers discovered.");

                    if (num > 0) {
                        startServiceDiscovery();
                        //stopPeerDiscovery(); //TODO: See if this is needed later
                    } else {
                        //TODO, add timer here to start peer discovery
                        startPeerDiscovery();
                    }
                }
            };
            serviceListener = new WifiP2pManager.DnsSdServiceResponseListener() {

                public void onDnsSdServiceAvailable(String instanceName, String serviceType, WifiP2pDevice device) {

                    addText("Service discovered, " + instanceName + " " + serviceType + " : " + AddressUtil.deviceToString(device));
                    if (serviceType.startsWith(SERVICE_TYPE)) {

                        WifiP2pConfig config = new WifiP2pConfig();
                        config.deviceAddress = device.deviceAddress;
                        config.wps.setup = WpsInfo.PBC;

                        mP2P.connect(mChannel, config, new WifiP2pManager.ActionListener() {

                            @Override
                            public void onSuccess() {
                                addText("Connecting to service");
                                mServiceState = ServiceState.ConnectingWifi;
                            }

                            @Override
                            public void onFailure(int errorCode) {
                                addText("Failed connecting to service : " + errorCode);
                                startPeerDiscovery();
                            }
                        });
                    } else {
                        addText("Not our Service, :" + SERVICE_TYPE + "!=" + serviceType + ":");
                        startPeerDiscovery();
                    }
                }
            };

            mP2P.setDnsSdResponseListeners(mChannel, serviceListener, null);

            startLocalService();
            startPeerDiscovery();
        }
        }
    }

    /**
     * Adding my service to service list
     */
    private void startLocalService() {

        Map<String, String> record = new HashMap<String, String>();
        record.put(TXT_RECORD_PROP_AVAILABLE, "visible");

        WifiP2pDnsSdServiceInfo service = WifiP2pDnsSdServiceInfo.newInstance(GROUP_NAME, SERVICE_TYPE, record);

        addText("Add local service");
        mP2P.addLocalService(mChannel, service, new WifiP2pManager.ActionListener() {
            public void onSuccess() {
                addText("Added local service");
            }

            public void onFailure(int reason) {
                addText("Adding local service failed, error code " + reason);
            }
        });
    }

    /**
     * Asks to discover peers
     */
    private void startPeerDiscovery() {

        mP2P.discoverPeers(mChannel, new WifiP2pManager.ActionListener() {
            public void onSuccess() {
                mServiceState = ServiceState.DiscoverPeer;
                addText("Started peer discovery");
            }

            public void onFailure(int reason) {
                addText("Starting peer discovery failed, error code " + reason);
            }
        });
    }

    private void startServiceDiscovery() {

        // multiple active request appear to mess things up, thus checking whether cancellation always
        // would ease the task. We need this since, otherwise we get into situations where we either
        // nmake multiple requests or end up into situation where we don't have active discovery on.
        stopServiceDiscovery();

        WifiP2pDnsSdServiceRequest request = WifiP2pDnsSdServiceRequest.newInstance(SERVICE_TYPE);
        final Handler handler = new Handler();
        mP2P.addServiceRequest(mChannel, request, new WifiP2pManager.ActionListener() {

            public void onSuccess() {
                addText("Added service request");

                handler.postDelayed(new Runnable() {
                    // There are supposedly a possible race-condition bug with the service discovery
                    // thus to avoid it, we are delaying the service discovery start here
                    public void run() {

                        mP2P.discoverServices(mChannel, new WifiP2pManager.ActionListener() {

                            public void onSuccess() {
                                mServiceState = ServiceState.DiscoverService;
                                addText("Started service discovery");
                            }

                            public void onFailure(int reason) {
                                addText("Starting service discovery failed, error code " + reason);

                                if (reason == WifiP2pManager.NO_SERVICE_REQUESTS
                                        // If we start getting this error, we either got the race condition
                                        // or we are client, that just got disconnected when group owner removed the group
                                        // anyways, sometimes only way, and 'nearly' always working fix is to
                                        // toggle Wifi off/on, it appears to reset what ever is blocking there.
                                        || reason == WifiP2pManager.ERROR) {
                                    // this happens randomly with Kitkat-to-Kitkat connections on client side.

                                    if (reason == WifiP2pManager.NO_SERVICE_REQUESTS) {
                                        addText("Service Discovery error 3");
                                    } else {
                                        addText("Service Discovery generic zero error");
                                    }

                                    // It appears that with KitKat, this event also sometimes does corrupt
                                    // our local services advertising, so stopping & restarting (once connected)
                                    // to make sure we are discoverable still
                                    stopLocalServices();

                                    WifiManager wifiManager = (WifiManager)
                                            mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                    wifiManager.setWifiEnabled(false);
                                    //wait for WIFI_P2P_STATE_CHANGED_ACTION & do the re-connection
                                }
                            }
                        });
                    }
                }, SERVICE_DISCOVERY_TIME_DELAY);
            }

            public void onFailure(int reason) {
                addText("Adding service request failed, error code " + reason);
                // No point starting service discovery
            }
        });

    }

    private void stopServiceDiscovery() {

        mP2P.clearServiceRequests(mChannel, new WifiP2pManager.ActionListener() {
            public void onSuccess() {
                addText("Cleared service requests");
            }

            public void onFailure(int reason) {
                addText("Clearing service requests failed, error code " + reason);
            }
        });

    }

    private void stopLocalServices() {
        mP2P.clearLocalServices(mChannel, new WifiP2pManager.ActionListener() {
            public void onSuccess() {
                addText("Cleared local services");
            }

            public void onFailure(int reason) {
                addText("Clearing local services failed, error code " + reason);
            }
        });
    }

    /**
     * Stopping Peer discovery request
     */
    private void stopPeerDiscovery() {
        mP2P.stopPeerDiscovery(mChannel, new WifiP2pManager.ActionListener() {
            public void onSuccess() {
                addText("Stopped peer discovery");
            }
            public void onFailure(int reason) {
                addText("Stopping peer discovery failed, error code " + reason);
            }
        });
    }

    public void destroy() {

        stopPeerDiscovery();
        stopServiceDiscovery();
        stopLocalServices();

        mContext.unregisterReceiver(mReceiver);
    }

    private class P2PState implements P2PStateListener {

        @Override
        public void onP2PStateChange(int state) {
            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {
                startPeerDiscovery();
            } else {
                    WifiManager wifiManager = (WifiManager)
                            mContext.getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                    wifiManager.setWifiEnabled(true);
            }
        }

        @Override
        public void onP2PPeersStateChange() {

            if (mServiceState == ServiceState.NONE || mServiceState == ServiceState.DiscoverPeer) {

                mP2P.requestPeers(mChannel, peerListListener);
            }

        }

        @Override
        public void onP2PPeersDiscoveryStarted() {

        }

        @Override
        public void onP2PPeersDiscoveryStopped() {

        }

        @Override
        public void onP2PConnected() {
            mServiceState = ServiceState.QueryConnection;
            mP2P.requestConnectionInfo(mChannel, mP2PConnectionListener);
        }

        @Override
        public void onP2PDisconnected() {

            startPeerDiscovery();

            if((mServiceState == ServiceState.ConnectedAsClient ||
                    mServiceState == ServiceState.ConnectedAsOwner)
                    && !TextUtils.isEmpty(mLastConnectedGO)) {

                Peer peer = new Peer();
                peer.mId = mLastConnectedGO;
                mMeshXListener.onPeerGone(peer);
            }
            mServiceState = ServiceState.NONE;
            mLastConnectedGO = null;
        }
    }

    private class P2PConnectionListener implements WifiP2pManager.ConnectionInfoListener {

        @Override
        public void onConnectionInfoAvailable(WifiP2pInfo p2pInfo) {

            try {
                if (p2pInfo.isGroupOwner) {
                    //Me group owner, will wait for client to get connection
                    addText("Connected as group owner, already listening!");
                    mServiceState = ServiceState.ConnectedAsOwner;
                    mLastConnectedGO = null;

                } else {
                    //Me client, will be connecting with GO
                    mServiceState = ServiceState.ConnectedAsClient;
                    mLastConnectedGO = p2pInfo.groupOwnerAddress.getHostAddress();
                    //This should be formed at one layer up
                    if(mMeshXListener != null) {
                        Peer peer = new Peer();
                        peer.mId = mLastConnectedGO;

                        mMeshXListener.onPeer(peer);
                    }
                }

            } catch (Exception e) {
                addText("onConnectionInfoAvailable, error: " + e.toString());
            }
        }
    }
}
