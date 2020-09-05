package com.dailystudio.weex.examples

import com.dailystudio.devbricksx.app.DevBricksApplication
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.weex.WEEXPlayground
import com.facebook.stetho.Stetho
import com.nostra13.universalimageloader.core.ImageLoader
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration

class WeexExamplesApplication : DevBricksApplication() {

    override fun onCreate() {
        super.onCreate()

        if (BuildConfig.USE_STETHO) {
            Stetho.initializeWithDefaults(this)
        }

        val config = ImageLoaderConfiguration.Builder(this).build()

        ImageLoader.getInstance().init(config)

        Logger.info("application is running in %s mode.",
            if (BuildConfig.DEBUG) "DEBUG" else "RELEASE")

        initWeex()
    }

    override fun isDebugBuild(): Boolean {
        return BuildConfig.DEBUG
    }

    private fun initWeex() {
        WEEXPlayground.initWeexPlayground(this)
    }

}
