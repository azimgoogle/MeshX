package com.w3engineers.core.meshx.ui.settings;

import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.arch.lifecycle.ViewModelProviders;
import android.support.annotation.NonNull;

import com.w3engineers.core.meshx.R;
import com.w3engineers.core.meshx.databinding.ActivitySettingsBinding;
import com.w3engineers.ext.strom.application.ui.base.BaseActivity;
import com.w3engineers.ext.strom.util.helper.data.local.SharedPref;


/**
 * ============================================================================
 * Copyright (C) 2019 W3 Engineers Ltd - All Rights Reserved.
 * Unauthorized copying of this file, via any medium is strictly prohibited
 * Proprietary and confidential
 * <br>----------------------------------------------------------------------------
 * <br>Created by: Ahmed Mohmmad Ullah (Azim) on [2019-03-08 at 12:22 PM].
 * <br>----------------------------------------------------------------------------
 * <br>Project: MeshX.
 * <br>Code Responsibility: <Purpose of code>
 * <br>----------------------------------------------------------------------------
 * <br>Edited by :
 * <br>1. <First Editor> on [2019-03-08 at 12:22 PM].
 * <br>2. <Second Editor>
 * <br>----------------------------------------------------------------------------
 * <br>Reviewed by :
 * <br>1. <First Reviewer> on [2019-03-08 at 12:22 PM].
 * <br>2. <Second Reviewer>
 * <br>============================================================================
 **/
public class SettingsActivity extends BaseActivity {

    private ActivitySettingsBinding mActivitySettingsBinding;
    private SettingsViewModel mSettingsViewModel;

    @Override
    protected int getLayoutId() {
        return R.layout.activity_settings;
    }

    @Override
    protected void startUI() {
        mSettingsViewModel = getViewModel();
        mActivitySettingsBinding = (ActivitySettingsBinding) getViewDataBinding();

        mActivitySettingsBinding.setSettingsViewModel(mSettingsViewModel);
    }

    @SuppressWarnings("unchecked")
    private SettingsViewModel getViewModel() {
        return ViewModelProviders.of(this, new ViewModelProvider.Factory() {
            @NonNull
            @Override
            public <T extends ViewModel> T create(@NonNull Class<T> modelClass) {

                SharedPref sharedPref = SharedPref.getSharedPref(SettingsActivity.this);
                return (T) new SettingsViewModel(sharedPref);
            }
        }).get(SettingsViewModel.class);
    }
}
