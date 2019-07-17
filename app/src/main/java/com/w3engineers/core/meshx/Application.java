package com.w3engineers.core.meshx;

import com.w3engineers.ext.strom.App;

import timber.log.Timber;


/**
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <br>----------------------------------------------------------------------------
 * <br>Created by: Ahmed Mohmmad Ullah (Azim) on [2019-03-19 at 1:00 PM].
 * <br>----------------------------------------------------------------------------
 * <br>Project: MeshX.
 * <br>Code Responsibility: <Purpose of code>
 * <br>----------------------------------------------------------------------------
 * <br>Edited by :
 * <br>1. <First Editor> on [2019-03-19 at 1:00 PM].
 * <br>2. <Second Editor>
 * <br>----------------------------------------------------------------------------
 * <br>Reviewed by :
 * <br>1. <First Reviewer> on [2019-03-19 at 1:00 PM].
 * <br>2. <Second Reviewer>
 * <br>============================================================================
 **/
public class Application extends App {


    @Override
    protected void plantTimber() {
        Timber.plant(new Timber.DebugTree() {
            //Add line number and method name with tag
            @Override
            protected String createStackElementTag(StackTraceElement element) {
                //The brace will generate clickable link in Logcat window
                //Stability depends on developers comfort level
                return "(" + element.getFileName() + ':' + element.getLineNumber() + "):"+element.getMethodName();
            }
        });
    }
}
