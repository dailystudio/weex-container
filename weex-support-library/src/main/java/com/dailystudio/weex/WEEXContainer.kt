package com.dailystudio.weex

import android.content.Context
import android.net.Uri
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroupOverlay
import android.widget.ProgressBar
import com.dailystudio.devbricksx.development.Logger
import org.apache.weex.IWXRenderListener
import org.apache.weex.RenderContainer
import org.apache.weex.WXPreLoadManager
import org.apache.weex.WXSDKInstance
import org.apache.weex.common.WXRenderStrategy
import org.apache.weex.commons.WXAnalyzerDelegate
import org.apache.weex.constants.Constants
import org.apache.weex.https.WXHttpManager
import org.apache.weex.https.WXHttpTask
import org.apache.weex.https.WXRequestListener
import java.io.UnsupportedEncodingException
import java.lang.Boolean
import java.util.HashMap

class WEEXContainer(val mContainer: ViewGroup,
                    val mContainerParent: ViewGroup? = null,
                    val mProgressBar: ProgressBar? = null) {

    private var mInstance: WXSDKInstance? = null
    private val mWxAnalyzerDelegate: WXAnalyzerDelegate? = null

    private val mConfigMap = HashMap<String, Any>()


    fun loadPageByUrl(uri: Uri) {
        val context = mContainer.context

        mProgressBar?.visibility = View.VISIBLE

        mInstance?.destroy()
        mContainer.removeAllViews()

        val renderContainer = RenderContainer(context)
        mContainer.addView(renderContainer)

        var url: String? = null

        if (TextUtils.equals("http", uri.scheme)
            || TextUtils.equals("https", uri.scheme)) {
            val weexTpl = uri.getQueryParameter(Constants.WEEX_TPL_KEY)

            if (weexTpl?.isNotEmpty() == true) {
                url = weexTpl
            } else {
                url = uri.toString()
            }
        }

        if (url == null) {
            Logger.error("failed to parse uri: $uri")

            return
        }

        mInstance = createWeexInstance(url, renderContainer)

        if (mInstance?.isPreDownLoad == true) {
            return
        }

        requestUrl(url)
        Logger.debug("[WXLifecycle]: URL requested: $mInstance")
    }

    private fun requestUrl(url: String) {
        val httpTask = WXHttpTask()
        httpTask.url = url

        httpTask.requestListener = object : WXRequestListener {

            override fun onSuccess(task: WXHttpTask) {
                Logger.info("download success: url = [$url]")
                try {
                    val uri = Uri.parse(url)
                    val charsetUtf8 = Charsets.UTF_8
                    val pageName = "WeexPage"
                    mConfigMap["bundleUrl"] = url

                    when {
                        TextUtils.equals(uri.getQueryParameter("__eagle"),
                            Boolean.TRUE.toString()) -> {
                            mInstance?.render(pageName,
                                task.response.data, mConfigMap, null)
                        }

                        TextUtils.equals(uri.getQueryParameter("__data_render"),
                            Boolean.TRUE.toString()) -> {
                            mInstance?.render(
                                pageName,
                                String(task.response.data, charsetUtf8),
                                mConfigMap,
                                null,
                                WXRenderStrategy.DATA_RENDER
                            )
                        }

                        TextUtils.equals(uri.getQueryParameter("__json_render"),
                            Boolean.TRUE.toString()) -> {
                            mInstance?.render(
                                pageName,
                                String(task.response.data, charsetUtf8),
                                mConfigMap,
                                null,
                                WXRenderStrategy.JSON_RENDER
                            )
                        }

                        else -> {
                            Logger.debug("render rest")
                            val template = String(task.response.data, charsetUtf8)
                            Logger.debug("page content: [$template]")
                            mInstance!!.render(
                                pageName,
                                template,
                                mConfigMap,
                                null,
                                WXRenderStrategy.APPEND_ASYNC
                            )
                        }
                    }
                } catch (e: UnsupportedEncodingException) {
                    Logger.error("unsupported encoding: $e")
                }
            }

            override fun onError(task: WXHttpTask) {
                Logger.error("download page failed: ${task.response}")
                mProgressBar?.visibility = View.GONE
            }

        }

        WXHttpManager.getInstance().sendRequest(httpTask)
    }

    private fun createWeexInstance(url: String,
                                   renderContainer: RenderContainer
    ): WXSDKInstance {
        val context = mContainer.context

        val sdkInstance = WXPreLoadManager.getInstance()
            .offerPreInitInstance(url)?.apply {
                init(context)
            } ?: WXSDKInstance(context)

        sdkInstance.apply {
            setWXAbstractRenderContainer(renderContainer)
            registerRenderListener(renderListener)
            setNestedInstanceInterceptor(nestedInstanceInterceptor)
            bundleUrl = url
            isTrackComponent = true
        }

        return sdkInstance
    }

    private val nestedInstanceInterceptor =
        WXSDKInstance.NestedInstanceInterceptor { instance, _ -> Logger.debug("new instance created: $instance") }

    private val renderListener = object: IWXRenderListener {

        override fun onViewCreated(instance: WXSDKInstance?, view: View) {
            Logger.debug("[WXLifecycle] view created: $mInstance")
            var view = view
            Logger.debug("renderSuccess: instance = $instance, view = $view")
            var wrappedView: View? =
                mWxAnalyzerDelegate?.onWeexViewCreated(instance, view)

            if (wrappedView != null) {
                view = wrappedView
            }

            if (view.parent == null) {
                mContainer?.addView(view)
            }
            requestLayout()
        }

        override fun onRenderSuccess(instance: WXSDKInstance?, width: Int, height: Int) {
            Logger.debug("[WXLifecycle] view rendered: $mInstance")
            mWxAnalyzerDelegate?.onWeexRenderSuccess(instance)

            mProgressBar?.visibility = View.GONE
            requestLayout()
        }

        override fun onRefreshSuccess(instance: WXSDKInstance?, width: Int, height: Int) {
            Logger.debug("[WXLifecycle] refreshed: $mInstance")

            mProgressBar?.visibility = View.GONE
            requestLayout()
        }

        override fun onException(instance: WXSDKInstance?,
                                 errCode: String,
                                 msg: String) {
            Logger.debug("[WXLifecycle] exception occurred: $mInstance")

            mWxAnalyzerDelegate?.onException(instance, errCode, msg)

            mProgressBar?.visibility = View.GONE

            if (!TextUtils.isEmpty(errCode) && errCode.contains("|")) {
                val errCodeList =
                    errCode.split("\\|".toRegex()).toTypedArray()
                val code = errCodeList[1]
                val codeType = errCode.substring(0, errCode.indexOf("|"))
                if (TextUtils.equals("1", codeType)) {
                    Logger.debug(buildString {
                        append("codeType: $codeType,")
                        append("errCode: $code,")
                        append("ErrorInfo: $msg")
                    })
                    return
                } else {
                    Logger.debug(buildString {
                        append("errCode: $errCode,")
                        append("Render ERROR: $msg")
                    })
                }
            }
        }

        private fun requestLayout() {
            mContainerParent?.requestLayout()
        }
    }

}