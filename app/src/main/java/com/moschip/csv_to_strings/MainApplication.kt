package com.moschip.csv_to_strings

import android.content.Context
import androidx.multidex.MultiDex
import androidx.multidex.MultiDexApplication


/**
 * Created by Manohar on 21/10/20.
 */
class MainApplication: MultiDexApplication() {

    override fun onCreate() {
        super.onCreate()

    }

    override fun attachBaseContext(base: Context?) {
        super.attachBaseContext(base)
        MultiDex.install(this)
    }
}