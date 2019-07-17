package com.w3engineers.core.libmeshx.discovery;

/**
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <br>----------------------------------------------------------------------------
 * <br>Created by: Ahmed Mohmmad Ullah (Azim) on [2019-03-19 at 11:37 AM].
 * <br>----------------------------------------------------------------------------
 * <br>Project: MeshX.
 * <br>Code Responsibility: <Purpose of code>
 * <br>----------------------------------------------------------------------------
 * <br>Edited by :
 * <br>1. <First Editor> on [2019-03-19 at 11:37 AM].
 * <br>2. <Second Editor>
 * <br>----------------------------------------------------------------------------
 * <br>Reviewed by :
 * <br>1. <First Reviewer> on [2019-03-19 at 11:37 AM].
 * <br>2. <Second Reviewer>
 * <br>============================================================================
 **/

/**
 * Contains method to communicate at App layer
 */
public interface MeshXListener {

    /**
     * When a new Peer receive
     * @param peer
     */
    void onPeer(Peer peer);

    /**
     * on remove a peer
     * @param peer
     */
    void onPeerGone(Peer peer);

    /**
     * When a new message arrive
     * @param message
     */
    void onMessage(Message message);
}
