package com.dailystudio.weex

import android.app.Application
import android.content.Context
import org.apache.weex.InitConfig
import org.apache.weex.WXSDKEngine
import org.apache.weex.commons.adapter.DefaultWebSocketAdapterFactory
import org.apache.weex.commons.adapter.ImageAdapter
import org.apache.weex.commons.adapter.JSExceptionAdapter
import org.apache.weex.commons.adapter.PicassoBasedDrawableLoader

object WEEXPlayground {

    fun initWeexPlayground(application: Application) {
        val builder =
            InitConfig.Builder() //.setImgAdapter(new FrescoImageAdapter())// use fresco adapter
                .setImgAdapter(ImageAdapter())
                .setDrawableLoader(PicassoBasedDrawableLoader(application.applicationContext))
                .setWebSocketAdapterFactory(DefaultWebSocketAdapterFactory())
                .setJSExceptionAdapter(JSExceptionAdapter())
        WXSDKEngine.initialize(application, builder.build())
    }

}