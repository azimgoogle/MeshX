package com.w3engineers.core.libmeshx.http;

import android.content.Context;
import android.util.Pair;

import com.w3engineers.core.libmeshx.discovery.MeshXListener;
import com.w3engineers.core.libmeshx.discovery.Message;

import java.io.IOException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

/**
 * NANO http server initializer
 */
public class MeshHttpServer implements NanoHTTPServer.HttpDataListener {

    private MeshXListener mMmeshXListener;
    public static final int DEFAULT_CONNECTION_TIMEOUT = 60 * 1000;
    private static MeshHttpServer server;
    private NanoHTTPServer nanoHTTPServer;
    private ExecutorService callableExecutor;
    private int APP_PORT;
    private Context mContext;
    /**
     * To temporarily store missing discovery data which was paused during server pausing
     */
    private ConcurrentLinkedQueue<Pair<String, String>> mBufferedDiscoveryData;
    private volatile boolean mIsDirectDiscoveryPause;
    private volatile boolean mIsAdhocDiscoveryPause;

    /**
     * To temporarily store missing discovery data which was paused during server pausing
     */

    private MeshHttpServer(Context context) {
        mContext = context;
        callableExecutor = Executors.newFixedThreadPool(1);
        mBufferedDiscoveryData = new ConcurrentLinkedQueue<>();
    }

    public static MeshHttpServer on(Context context) {
        if (server == null) {
            server = new MeshHttpServer(context);
        }

        return server;
    }

    public void start(int appPort) {
        stop();
        this.APP_PORT = appPort;

        try {
            nanoHTTPServer = new NanoHTTPServer(appPort);
            nanoHTTPServer.setHttpDataListener(this::receivedData);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public void stop() {
        if (nanoHTTPServer != null) {
            nanoHTTPServer.stop();
        }

    }
    //.url("http://" + ip + ":8080/hellopacket?data=" + dataStr)

    public Integer sendMessage(String ip, byte[] data) throws ExecutionException, InterruptedException {
        return sendMessage(ip, data, DEFAULT_CONNECTION_TIMEOUT);
    }

    public Integer sendMessage(String ip, byte[] data, int connectionTimeOutInMillis)
            throws ExecutionException, InterruptedException {

        String dataStr = new String(data);
        RequestBody formBody = new FormBody.Builder()
                .add("data", dataStr)
                .build();
        Future<Integer> future = callableExecutor.submit(() -> {
            OkHttpClient client;
            OkHttpClient.Builder builder = new OkHttpClient.Builder();

            //This logic is working fine for now. If any device show a behavior of being GO and a
            //Adhoc or HS Client at the same time then outgoing request of Adhoc or Client might
            //need separate handle
            /*if (!P2PUtil.isMeGO()) {
                if (!WiFiUtil.isHotSpotEnabled()) {
                    //FIXME first decide me go or client while building the packet
                    builder.socketFactory(getNetwork().getSocketFactory());
                }
            }*/
            builder.connectTimeout(connectionTimeOutInMillis, TimeUnit.MILLISECONDS);
            builder.retryOnConnectionFailure(true);
            client = builder.build();

            int responseCode = 0;
            Request request = new Request.Builder()
                    .url("http://" + ip + ":" + APP_PORT)
                    .post(formBody)
                    .addHeader("cache-control", "no-cache")
                    .addHeader("Connection", "close")
                    .build();

            try {

                Response response = client.newCall(request).execute();
                responseCode = response.code() == 200 ? 1 : 0;
            } catch (IOException e) {
                e.printStackTrace();
            }
            return responseCode;
        });

        return future.get();
    }

    @Override
    public void receivedData(String ipAddress, String data) {
        if(mMmeshXListener != null) {
            mMmeshXListener.onMessage(new Message(ipAddress, data));
        }
    }

    public void setHttpDataListener(MeshXListener meshXListener) {
        this.mMmeshXListener = meshXListener;
    }
}
