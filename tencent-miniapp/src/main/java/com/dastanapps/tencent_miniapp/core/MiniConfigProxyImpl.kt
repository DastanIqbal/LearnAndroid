package com.dastanapps.tencent_miniapp.core

import android.app.Application
import com.dastanapps.tencent_miniapp.App
import com.tencent.tmf.mini.api.bean.MiniInitConfig
import com.tencent.tmf.mini.api.proxy.MiniConfigProxy
import com.tencent.tmfmini.sdk.annotation.ProxyService

@ProxyService(proxy = MiniConfigProxy::class)
class MiniConfigProxyImpl : MiniConfigProxy() {
    /**
     * Application
     *
     * @return
     */
    override fun getApp(): Application {
        return App.INSTANCE
    }

    /**
     * Create initial configuration information
     *
     * @return
     */
    override fun buildConfig(): MiniInitConfig {
        val builder = MiniInitConfig.Builder()
        val config = builder
            .configAssetName("miniapp_configurations.json") //The name of the configuration file in assets
            .imei("IMEI") //Configure the device ID, which is used for grayscale release and use of small programs based on the device identification on the management platform (optional)
            .autoRequestPermission(true) //Configure whether the applet will automatically apply for the corresponding system permissions from the user when it uses an API that requires permissions.
            .debug(true) //Log switch, closed by default
            .verifyPkg(false)
            .build()
        return config
    }
}