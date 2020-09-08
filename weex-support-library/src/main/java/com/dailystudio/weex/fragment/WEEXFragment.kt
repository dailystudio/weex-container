package com.dailystudio.weex.fragment

import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.fragment.app.Fragment
import com.dailystudio.weex.R
import com.dailystudio.weex.WEEXContainer

class WEEXFragment: Fragment() {


    private var mContainer: ViewGroup? = null
    private var mProgressBar: ProgressBar? = null

    private var container: WEEXContainer? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_weex, null)

        setupViews(view)

        return view
    }

    private fun setupViews(fragmentView: View) {
        mContainer = fragmentView.findViewById(R.id.container)
        mProgressBar = fragmentView.findViewById(R.id.progress)

        mContainer?.let {
            container = WEEXContainer(it, fragmentView as ViewGroup, mProgressBar)
        }
    }

    fun loadPageByUrl(uri: Uri) {
        container?.loadPageByUrl(uri)
    }

//
//    override fun onActivityCreated(savedInstanceState: Bundle?) {
//        super.onActivityCreated(savedInstanceState)
//
//        Logger.debug("[WXLifecycle] activity created: $mInstance")
//
//        mInstance?.onActivityCreate()
//    }
//
//    override fun onStart() {
//        super.onStart()
//
//        Logger.debug("[WXLifecycle] activity started: $mInstance")
//        mInstance?.onActivityStart()
//        mWxAnalyzerDelegate?.onStart()
//    }
//
//    override fun onStop() {
//        super.onStop()
//
//        Logger.debug("[WXLifecycle] activity stopped: $mInstance")
//        mInstance?.onActivityStop()
//        mWxAnalyzerDelegate?.onStop()
//    }
//
//    override fun onResume() {
//        super.onResume()
//
//        Logger.debug("[WXLifecycle] activity resumed: $mInstance")
//        mInstance?.onActivityResume()
//        mWxAnalyzerDelegate?.onResume()
//    }
//
//    override fun onPause() {
//        super.onPause()
//
//        Logger.debug("[WXLifecycle] activity paused: $mInstance")
//        mInstance?.onActivityPause()
//        mWxAnalyzerDelegate?.onPause()
//    }
//
//    override fun onDestroy() {
//        super.onDestroy()
//
//        Logger.debug("[WXLifecycle] activity destroyed: $mInstance")
//        mInstance?.onActivityDestroy()
//        mWxAnalyzerDelegate?.onDestroy()
//
//        mContainer = null
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<String?>,
//        grantResults: IntArray) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//
//        mInstance?.onRequestPermissionsResult(requestCode, permissions, grantResults)
//    }
//
//    override fun onActivityResult(
//        requestCode: Int,
//        resultCode: Int,
//        data: Intent?) {
//        super.onActivityResult(requestCode, resultCode, data)
//
//        mInstance?.onActivityResult(requestCode, resultCode, data)
//    }
//
}