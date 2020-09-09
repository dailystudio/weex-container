package com.dailystudio.weex.activity

import android.net.Uri
import android.os.Bundle
import android.view.View
import androidx.lifecycle.lifecycleScope
import com.dailystudio.devbricksx.app.activity.DevBricksActivity
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.weex.R
import com.dailystudio.weex.fragment.WEEXFragment
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
        val weexContainers: View? = findViewById(R.id.weex_containers)
        val weexs = arrayListOf<WEEXFragment>()
        weexContainers?.let {
            for (i in 0 until count) {
                val ft = supportFragmentManager.beginTransaction()

                val fragment = WEEXFragment()

                ft.add(it.id, fragment, "weex_fragment_$i")
                ft.commit()

                weexs.add(fragment)
            }
        }

        lifecycleScope.launch {
            delay(2000)
            for (fragment in weexs) {
                fragment.loadPageByUrl(uri)
            }
        }
    }

}