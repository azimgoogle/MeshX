package com.w3engineers.core.libmeshx.wifid;

import android.content.Context;

/**
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <br>----------------------------------------------------------------------------
 * <br>Created by: Ahmed Mohmmad Ullah (Azim) on [2019-03-28 at 12:39 PM].
 * <br>----------------------------------------------------------------------------
 * <br>Project: MeshX.
 * <br>Code Responsibility: <Purpose of code>
 * <br>----------------------------------------------------------------------------
 * <br>Edited by :
 * <br>1. <First Editor> on [2019-03-28 at 12:39 PM].
 * <br>2. <Second Editor>
 * <br>----------------------------------------------------------------------------
 * <br>Reviewed by :
 * <br>1. <First Reviewer> on [2019-03-28 at 12:39 PM].
 * <br>2. <Second Reviewer>
 * <br>============================================================================
 **/
public class SoftAccessPointSearcher extends P2PServiceSearcher {

    public interface ServiceFound {
        void onServiceFoundSuccess(String ssid, String passPhrase);
    }

    private ServiceFound mServiceFound;

    public void setServiceFound(ServiceFound serviceFound) {
        mServiceFound = serviceFound;
    }

    public SoftAccessPointSearcher(Context context) {
        super(context);
        mServiceType = Constants.Service.TYPE;
    }

    @Override
    protected void onDesiredServiceFound(String ssid, String passPhrase) {
        if(mServiceFound != null) {
            mServiceFound.onServiceFoundSuccess(ssid, passPhrase);
        }
    }
}
