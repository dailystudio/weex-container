package com.dailystudio.weex

import android.app.Application
import android.util.Log
import com.dailystudio.devbricksx.development.Logger
import org.apache.weex.InitConfig
import org.apache.weex.WXSDKEngine
import org.apache.weex.commons.adapter.DefaultWebSocketAdapterFactory
import org.apache.weex.commons.adapter.ImageAdapter
import org.apache.weex.commons.adapter.JSExceptionAdapter
import org.apache.weex.commons.adapter.PicassoBasedDrawableLoader

object WEEXPlayground {

    const val ACTION_VIEW_WEEX: String = "com.dailystudio.intent.action.VIEW_WEEX"
    const val CATEGORY_WEEX: String = "com.dailystudio.intent.category.WEEX"

    fun initWeexPlayground(application: Application) {
        val builder =
            InitConfig.Builder() //.setImgAdapter(new FrescoImageAdapter())// use fresco adapter
                .setImgAdapter(ImageAdapter())
                .setDrawableLoader(PicassoBasedDrawableLoader(application.applicationContext))
                .setWebSocketAdapterFactory(DefaultWebSocketAdapterFactory())
                .setJSExceptionAdapter(JSExceptionAdapter())
        WXSDKEngine.initialize(application, builder.build())

        startHeron(application)
    }

    private fun startHeron(application: Application) {
        try {
            val heronInitClass: Class<*> =
                application.classLoader.loadClass(
                    "com/taobao/weex/heron/picasso/RenderPicassoInit")

            val method =
                heronInitClass.getMethod("initApplication",
                    Application::class.java)
            method.isAccessible = true
            method.invoke(null, this)
            Logger.info("Weex Heron Render Init Success")
        } catch (e: Exception) {
            Logger.error("Weex Heron Render Mode Not Found: $e")
        }
    }

}