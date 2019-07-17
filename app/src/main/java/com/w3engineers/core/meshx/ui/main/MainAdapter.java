package com.w3engineers.core.meshx.ui.main;

import android.databinding.ViewDataBinding;
import android.view.ViewGroup;

import com.w3engineers.core.meshx.R;
import com.w3engineers.core.meshx.databinding.ItemMeshNodeBinding;
import com.w3engineers.ext.strom.application.ui.base.BaseAdapter;


/**
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <br>----------------------------------------------------------------------------
 * <br>Created by: Ahmed Mohmmad Ullah (Azim) on [2019-03-11 at 4:14 PM].
 * <br>----------------------------------------------------------------------------
 * <br>Project: MeshX.
 * <br>Code Responsibility: <Purpose of code>
 * <br>----------------------------------------------------------------------------
 * <br>Edited by :
 * <br>1. <First Editor> on [2019-03-11 at 4:14 PM].
 * <br>2. <Second Editor>
 * <br>----------------------------------------------------------------------------
 * <br>Reviewed by :
 * <br>1. <First Reviewer> on [2019-03-11 at 4:14 PM].
 * <br>2. <Second Reviewer>
 * <br>============================================================================
 **/
public class MainAdapter extends BaseAdapter<String> {

    private final int NODE_ITEM_TYPE = 1;
    private final int DEFAULT_ITEM_TYPE = 2;

    @Override
    public boolean isEqual(String left, String right) {
        return left != null && left.equals(right);
    }

    @Override
    public int getItemViewType(int position) {
        return getItem(position) != null ? NODE_ITEM_TYPE : DEFAULT_ITEM_TYPE;
    }

    @Override
    public BaseAdapterViewHolder<String> newViewHolder(ViewGroup parent, int viewType) {

        switch (viewType) {
            case NODE_ITEM_TYPE:
                return new NodeViewHolder(inflate(parent, R.layout.item_mesh_node));
            default:
                break;
        }

        return null;
    }

    private class NodeViewHolder extends BaseAdapterViewHolder<String> {

        private ItemMeshNodeBinding mItemMeshNodeBinding;

        public NodeViewHolder(ViewDataBinding viewDataBinding) {
            super(viewDataBinding);
            mItemMeshNodeBinding =(ItemMeshNodeBinding) mViewDataBinding;
        }

        @Override
        public void bind(String item) {
            mItemMeshNodeBinding.nodeId.setText(item);
        }
    }
}