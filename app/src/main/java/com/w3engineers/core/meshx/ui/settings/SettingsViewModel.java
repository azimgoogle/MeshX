package com.w3engineers.core.meshx.ui.settings;

import android.arch.lifecycle.ViewModel;

import com.w3engineers.core.meshx.data.helper.constant.Constants;
import com.w3engineers.ext.strom.util.helper.data.local.SharedPref;

/**
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <br>----------------------------------------------------------------------------
 * <br>Created by: Ahmed Mohmmad Ullah (Azim) on [2019-03-12 at 12:02 PM].
 * <br>----------------------------------------------------------------------------
 * <br>Project: MeshX.
 * <br>Code Responsibility: <Purpose of code>
 * <br>----------------------------------------------------------------------------
 * <br>Edited by :
 * <br>1. <First Editor> on [2019-03-12 at 12:02 PM].
 * <br>2. <Second Editor>
 * <br>----------------------------------------------------------------------------
 * <br>Reviewed by :
 * <br>1. <First Reviewer> on [2019-03-12 at 12:02 PM].
 * <br>2. <Second Reviewer>
 * <br>============================================================================
 **/
public class SettingsViewModel extends ViewModel {

    private SharedPref mSharedPref;

    public SettingsViewModel(SharedPref sharedPref) {

        mSharedPref = sharedPref;

    }

    public boolean getWiFiDirect() {
        return mSharedPref.readBooleanDefaultTrue(Constants.NetworkInterface.WiFiDirect);
    }

    public void onWiFiDirectCheckedChanged(boolean checked) {
        mSharedPref.write(Constants.NetworkInterface.WiFiDirect, checked);
    }

    public boolean getWiFiDirectGO() {
        return mSharedPref.readBooleanDefaultTrue(Constants.NetworkInterface.WiFiDirectGO);
    }

    public void onWiFiDirectGOCheckedChanged(boolean checked) {
        mSharedPref.write(Constants.NetworkInterface.WiFiDirectGO, checked);
    }

    public boolean getWiFiDirectLC() {
        return mSharedPref.readBooleanDefaultTrue(Constants.NetworkInterface.WiFiDirectLC);
    }

    public void onWiFiDirectLCCheckedChanged(boolean checked) {
        mSharedPref.write(Constants.NetworkInterface.WiFiDirectLC, checked);
    }

}
