package com.dailystudio.weex.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.weex.R
import com.dailystudio.weex.WEEXContainer

class SingleWEEXFragment: Fragment() {

    private var mContainer: ViewGroup? = null
    private var mProgressBar: ProgressBar? = null

    private var container: WEEXContainer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_single_weex, null)

        setupViews(view)

        return view
    }

    private fun setupViews(fragmentView: View) {
        mContainer = fragmentView.findViewById(R.id.container)
        mProgressBar = fragmentView.findViewById(R.id.progress)

        mContainer?.let {
            container = WEEXContainer(it,
                mProgressBar,
                lifecycle)
        }
    }

    fun loadPageByUrl(uri: Uri) {
        Logger.debug("[WXLifecycle] load by url: fragment view = $view")
        container?.loadPageByUrl(uri)
    }

    fun loadPageFromAsset(uri: Uri) {
        Logger.debug("[WXLifecycle] load from asset: fragment view = $view")
        container?.loadFromAsset(uri)
    }
/*
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String?>,
        grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        mInstance?.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    override fun onActivityResult(
        requestCode: Int,
        resultCode: Int,
        data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        mInstance?.onActivityResult(requestCode, resultCode, data)
    }

*/
}