package com.dailystudio.weex.activity

import android.net.Uri
import android.os.Bundle
import com.dailystudio.devbricksx.app.activity.DevBricksActivity
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.weex.R
import com.dailystudio.weex.fragment.WEEXFragment

class WEEXPageActivity: DevBricksActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_weex_page)

        val uri = intent.data
        Logger.debug("weex page: [$uri]")

        loadPage(uri)
    }

    private fun loadPage(uri: Uri?) {
        if (uri == null) {
            Logger.error("uri is empty!")

            return
        }

        val fragment1 = findFragment(R.id.fragment_weex1)
        if (fragment1 is WEEXFragment) {
            fragment1.loadPageByUrl(uri)
        }

        val fragment2 = findFragment(R.id.fragment_weex2)
        if (fragment2 is WEEXFragment) {
            fragment2.loadPageByUrl(uri)
        }

        val fragment3 = findFragment(R.id.fragment_weex3)
        if (fragment3 is WEEXFragment) {
            fragment3.loadPageByUrl(uri)
        }
    }

}