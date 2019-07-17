package com.w3engineers.core.meshx.ui.main;

import android.content.Intent;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.view.MenuItem;

import com.w3engineers.core.libmeshx.discovery.MeshXListener;
import com.w3engineers.core.libmeshx.discovery.MeshXLogListener;
import com.w3engineers.core.libmeshx.discovery.Message;
import com.w3engineers.core.libmeshx.discovery.Peer;
import com.w3engineers.core.meshx.R;
import com.w3engineers.core.meshx.data.helper.ConnectingServiceHelper;
import com.w3engineers.core.meshx.databinding.ActivityMainBinding;
import com.w3engineers.core.meshx.ui.settings.SettingsActivity;
import com.w3engineers.ext.strom.application.ui.base.BaseActivity;
import com.w3engineers.ext.strom.util.Text;
import com.w3engineers.ext.strom.util.helper.Toaster;

import timber.log.Timber;

import static android.support.v7.widget.DividerItemDecoration.VERTICAL;

public class MainActivity extends BaseActivity implements MeshXListener, MeshXLogListener {

    private ActivityMainBinding mActivityMainBinding;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_main;
    }

    @Override
    protected void startUI() {

        mActivityMainBinding = (ActivityMainBinding) getViewDataBinding();

        MainAdapter mainAdapter = new MainAdapter();

        DividerItemDecoration dividerItemDecorationVertical = new DividerItemDecoration(getApplicationContext(), VERTICAL);
        dividerItemDecorationVertical.setDrawable(getResources().getDrawable(R.drawable.separator));
        mActivityMainBinding.nodeRecyclerView.addItemDecoration(dividerItemDecorationVertical);

        mActivityMainBinding.nodeRecyclerView.setLayoutManager(new LinearLayoutManager(this));
        mActivityMainBinding.nodeRecyclerView.setAdapter(mainAdapter);
//        mainAdapter.addItem(Arrays.asList("abc", "def", "ghi"));

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
        Timber.d(peer.toString());
        Toaster.showLong(peer.toString());
        getAdapter().addItem(peer.toString());
    }

    @Override
    public void onPeerGone(Peer peer) {
        getAdapter().removeItem(peer.toString());
    }

    @Override
    public void onMessage(Message message) {

    }

    @Override
    public void onLog(String logMessage) {

        if (Text.isNotEmpty(logMessage)) {
            mActivityMainBinding.logTextView.append(logMessage + System.lineSeparator());
        }
    }
}
