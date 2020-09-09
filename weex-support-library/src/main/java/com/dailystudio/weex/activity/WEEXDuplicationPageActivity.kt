package com.dailystudio.weex.activity

import android.net.Uri
import android.os.Bundle
import com.dailystudio.devbricksx.app.activity.DevBricksActivity
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.weex.R
import com.dailystudio.weex.fragment.MultipleWEEXFragment

class WEEXDuplicationPageActivity: DevBricksActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_weex_duplicate_page)

        val uri = intent.data
        Logger.debug("weex page: [$uri]")

        loadPage(uri)
    }

    private fun loadPage(uri: Uri?) {
        if (uri == null) {
            Logger.error("uri is empty!")

            return
        }

        duplicateWeexContainers(uri, 50)
    }

    private fun duplicateWeexContainers(uri: Uri,
                                        count: Int) {
        val fragment = findFragment(R.id.fragment_weexs) ?: return
        if (fragment !is MultipleWEEXFragment) {
            return
        }

        for (i in 0 until count) {
            fragment.addWeexByUri("weex_$i", uri)
        }
    }

}