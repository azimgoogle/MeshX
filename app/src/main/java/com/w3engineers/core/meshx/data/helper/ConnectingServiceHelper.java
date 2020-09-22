package com.w3engineers.core.meshx.data.helper;

import android.content.Context;

import com.w3engineers.core.libmeshx.discovery.MeshXListener;
import com.w3engineers.core.libmeshx.discovery.MeshXLogListener;
import com.w3engineers.core.libmeshx.wifid.WiFiDirectManagerLegacy;
import com.w3engineers.core.meshx.data.helper.constant.Constants;
import com.w3engineers.ext.strom.util.helper.data.local.SharedPref;

/**
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <br>----------------------------------------------------------------------------
 * <br>Created by: Ahmed Mohmmad Ullah (Azim) on [2019-03-19 at 11:27 AM].
 * <br>----------------------------------------------------------------------------
 * <br>Project: MeshX.
 * <br>Code Responsibility: <Purpose of code>
 * <br>----------------------------------------------------------------------------
 * <br>Edited by :
 * <br>1. <First Editor> on [2019-03-19 at 11:27 AM].
 * <br>2. <Second Editor>
 * <br>----------------------------------------------------------------------------
 * <br>Reviewed by :
 * <br>1. <First Reviewer> on [2019-03-19 at 11:27 AM].
 * <br>2. <Second Reviewer>
 * <br>============================================================================
 **/
public class ConnectingServiceHelper {

    public static void init(Context applicationContext, MeshXListener meshXListener,
                            MeshXLogListener meshXLogListener) {
        if (applicationContext == null)
            throw new NullPointerException(Constants.Messages.NULL_CONTEXT_MESSAGE);

        applicationContext = applicationContext.getApplicationContext();
        boolean wifiDirectPreferred = SharedPref.getSharedPref(applicationContext).readBooleanDefaultTrue(Constants.NetworkInterface.WiFiDirect);

        //Initiating WiFiD
        if (wifiDirectPreferred) {
            WiFiDirectManagerLegacy wiFiDirectManagerLegacy =
                    WiFiDirectManagerLegacy.getInstance(applicationContext);
            wiFiDirectManagerLegacy.start();
            wiFiDirectManagerLegacy.setMeshXLogListener(meshXLogListener);
            wiFiDirectManagerLegacy.setMeshXListener(meshXListener);
        }

    }

    public static void destroy() {
        WiFiDirectManagerLegacy.getInstance().destroy();
    }

}
