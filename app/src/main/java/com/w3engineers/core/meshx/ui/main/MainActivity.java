package com.w3engineers.core.meshx.ui.main;

import android.content.Context;
import android.content.Intent;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;
import android.view.View;

import com.w3engineers.core.libmeshx.discovery.MeshXListener;
import com.w3engineers.core.libmeshx.discovery.MeshXLogListener;
import com.w3engineers.core.libmeshx.discovery.Message;
import com.w3engineers.core.libmeshx.discovery.Peer;
import com.w3engineers.core.libmeshx.http.MeshHttpServer;
import com.w3engineers.core.meshx.R;
import com.w3engineers.core.meshx.data.helper.ConnectingServiceHelper;
import com.w3engineers.core.meshx.databinding.ActivityMainBinding;
import com.w3engineers.core.meshx.ui.settings.SettingsActivity;
import com.w3engineers.ext.strom.application.ui.base.BaseActivity;
import com.w3engineers.ext.strom.application.ui.base.ItemClickListener;
import com.w3engineers.ext.strom.util.Text;
import com.w3engineers.ext.strom.util.helper.Toaster;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;

import timber.log.Timber;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

public class MainActivity extends BaseActivity implements MeshXListener, MeshXLogListener,
        ItemClickListener<String> {

    private ActivityMainBinding mActivityMainBinding;
    private MeshHttpServer mMeshHttpServer;
    public static final int PORT = 5645;
    private Map<String, String> mMacSSIDMap;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void startUI() {

        mActivityMainBinding = (ActivityMainBinding) getViewDataBinding();

        MainAdapter mainAdapter = new MainAdapter(this);

        DividerItemDecoration dividerItemDecorationVertical = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        dividerItemDecorationVertical.setDrawable(getResources().getDrawable(R.drawable.separator));
        mActivityMainBinding.nodeRecyclerView.addItemDecoration(dividerItemDecorationVertical);

        mActivityMainBinding.nodeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mActivityMainBinding.nodeRecyclerView.setAdapter(mainAdapter);
//        mainAdapter.addItem(Arrays.asList("abc", "def", "ghi"));

        mMacSSIDMap = new HashMap<>();

        mMeshHttpServer = MeshHttpServer.on(getApplicationContext());
        mMeshHttpServer.setHttpDataListener(this);
        mMeshHttpServer.start(PORT);

        ConnectingServiceHelper.init(getApplicationContext(), this, this);

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        ConnectingServiceHelper.destroy();
    }

    @Override
    protected int getMenuId() {
        return R.menu.menu_settings;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        switch (id) {
            case R.id.settings:
                Intent settingsIntent = new Intent(this, SettingsActivity.class);
                startActivity(settingsIntent);
                break;
        }

        return super.onOptionsItemSelected(item);
    }

    private MainAdapter getAdapter() {
        return (MainAdapter) mActivityMainBinding.nodeRecyclerView.getAdapter();
    }

    @Override
    public void onPeer(Peer peer) {
        /*Timber.d(peer.toString());
        Toaster.showLong(peer.toString());
        getAdapter().addItem(peer.toString());*/
    }

    @Override
    public void onPeerGone(Peer peer) {
//        getAdapter().removeItem(peer.toString());
    }

    @Override
    public void onMessage(Message message) {
        runOnUiThread(() -> Toaster.showShort(""+message));
    }

    @Override
    public void onMac(String mac, String ssid) {
        runOnUiThread(() -> {
            mMacSSIDMap.put(mac, ssid);

            Timber.d("Found:%s", mac);
            Toaster.showLong("Found: "+mac);
            getAdapter().addItem(mac);
        });
    }

    @Override
    public void onConnectedWith(String ssid) {
    }

    @Override
    public void onDisConnected() {
    }

    @Override
    public void onLog(String logMessage) {

        if (Text.isNotEmpty(logMessage)) {
            mActivityMainBinding.logTextView.append(logMessage + System.lineSeparator());
        }
    }

    @Override
    public void onItemClick(View view, String item) {

        String ssid = mMacSSIDMap.get(item);
        if(Text.isNotEmpty(ssid)) {

            WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
            WifiInfo wifiInfo = wifiManager.getConnectionInfo();
            String connectedSSID = wifiInfo.getSSID().replaceAll("\"", "");
            if(connectedSSID.equals(ssid)) {

                try {
                    mMeshHttpServer.sendMessage("192.168.49.1", "Hello from client!!!".getBytes());
                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            } else {
                //Search for particular mac and send message based on a flag
            }

        }
    }
}
