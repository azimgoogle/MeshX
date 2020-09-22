package com.w3engineers.core.libmeshx.discovery;

/**
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <br>----------------------------------------------------------------------------
 * <br>Created by: Ahmed Mohmmad Ullah (Azim) on [2019-03-19 at 11:35 AM].
 * <br>----------------------------------------------------------------------------
 * <br>Project: MeshX.
 * <br>Code Responsibility: <Purpose of code>
 * <br>----------------------------------------------------------------------------
 * <br>Edited by :
 * <br>1. <First Editor> on [2019-03-19 at 11:35 AM].
 * <br>2. <Second Editor>
 * <br>----------------------------------------------------------------------------
 * <br>Reviewed by :
 * <br>1. <First Reviewer> on [2019-03-19 at 11:35 AM].
 * <br>2. <Second Reviewer>
 * <br>============================================================================
 **/
public class Message {


    public String mData;
    public String mFrom;

    public Message(String from, String data) {
        this.mFrom = from;
        this.mData = data;
    }

    @Override
    public String toString() {
        return "Message{" +
                "From='" + mFrom + '\'' +
                ", Data='" + mData + '\'' +
                '}';
    }
}
