package com.dailystudio.weex

import android.net.Uri
import android.text.TextUtils
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.OnLifecycleEvent
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.devbricksx.utils.FileUtils
import com.taobao.weex.IWXRenderListener
import com.taobao.weex.RenderContainer
import com.taobao.weex.WXSDKInstance
import com.taobao.weex.common.WXRenderStrategy
import com.taobao.weex.render.WXAbstractRenderContainer
import org.apache.weex.WXPreLoadManager
import org.apache.weex.constants.Constants
import org.apache.weex.https.WXHttpManager
import org.apache.weex.https.WXHttpTask
import org.apache.weex.https.WXRequestListener
import java.io.UnsupportedEncodingException
import java.lang.Boolean
import java.util.*

class WEEXContainer(
    private val weexHolder: ViewGroup,
    private val progressBar: ProgressBar? = null,
    private val lifecycle: Lifecycle? = null
) {

    private val lifecycleObserver = object : LifecycleObserver {

        @OnLifecycleEvent(Lifecycle.Event.ON_CREATE)
        fun onCreated(source: LifecycleOwner?) {
            Logger.debug("[WXLifecycle] lifecycle[$source] created: $sdkInstance")

            sdkInstance?.onActivityCreate()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_START)
        fun onStart(source: LifecycleOwner?) {
            Logger.debug("[WXLifecycle] lifecycle[$source] started: $sdkInstance")
            sdkInstance?.onActivityStart()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
        fun onStop(source: LifecycleOwner?) {
            Logger.debug("[WXLifecycle] lifecycle[$source] stopped: $sdkInstance")
            sdkInstance?.onActivityStop()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_RESUME)
        fun onResume(source: LifecycleOwner?) {
            Logger.debug("[WXLifecycle] lifecycle[$source] resumed: $sdkInstance")
            sdkInstance?.onActivityResume()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_PAUSE)
        fun onPause(source: LifecycleOwner?) {
            Logger.debug("[WXLifecycle]  lifecycle[$source] paused: $sdkInstance")
            sdkInstance?.onActivityPause()
        }

        @OnLifecycleEvent(Lifecycle.Event.ON_DESTROY)
        fun onDestroy(source: LifecycleOwner?) {
            Logger.debug("[WXLifecycle]  lifecycle[$source] destroyed: $sdkInstance")
            sdkInstance?.onActivityDestroy()
        }

    }

    private var sdkInstance: WXSDKInstance? = null
    private val configMap = HashMap<String, Any>()
    private var renderContainer: WXAbstractRenderContainer? = null

    init {
        lifecycle?.let {
            it.addObserver(lifecycleObserver)
        }
    }

    fun loadPageByUrl(uri: Uri) {
        val context = weexHolder.context

        progressBar?.visibility = View.VISIBLE

        sdkInstance?.destroy()
        weexHolder.removeAllViews()

        val renderContainer = RenderContainer(context)
        weexHolder.addView(renderContainer)
        this.renderContainer = renderContainer

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

        sdkInstance = createWeexInstance(renderContainer, url)

        if (sdkInstance?.isPreDownLoad == true) {
            return
        }

        requestUrl(url)
        Logger.debug("[WXLifecycle]: URL requested: $sdkInstance")
    }

    fun loadFromAsset(uri: Uri) {
        val context = weexHolder.context

        progressBar?.visibility = View.GONE

        sdkInstance?.destroy()
        weexHolder.removeAllViews()

        val renderContainer = RenderContainer(context)
        weexHolder.addView(renderContainer)
        this.renderContainer = renderContainer

        val assetFile: String? = "${uri.path?.replaceFirst("/", "")}"
        if (assetFile == null) {
            Logger.error("failed to parse asset path from uri: $uri")

            return
        }

        sdkInstance = createWeexInstance(renderContainer, null)
        Logger.debug("[WXLifecycle]: load from asset: $sdkInstance, asset = [$assetFile]")
        sdkInstance?.render(
            assetFile, FileUtils.assetToString(context, assetFile),
            configMap, null,
            WXRenderStrategy.APPEND_ASYNC
        )
    }

    fun release() {
        sdkInstance?.destroy()
        sdkInstance = null
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
                    configMap["bundleUrl"] = url

                    when {
                        TextUtils.equals(
                            uri.getQueryParameter("__eagle"),
                            Boolean.TRUE.toString()
                        ) -> {
                            sdkInstance?.render(
                                pageName,
                                task.response.data, configMap, null
                            )
                        }

                        TextUtils.equals(
                            uri.getQueryParameter("__data_render"),
                            Boolean.TRUE.toString()
                        ) -> {
                            sdkInstance?.render(
                                pageName,
                                String(task.response.data, charsetUtf8),
                                configMap,
                                null,
                                WXRenderStrategy.DATA_RENDER
                            )
                        }

                        TextUtils.equals(
                            uri.getQueryParameter("__json_render"),
                            Boolean.TRUE.toString()
                        ) -> {
                            sdkInstance?.render(
                                pageName,
                                String(task.response.data, charsetUtf8),
                                configMap,
                                null,
                                WXRenderStrategy.JSON_RENDER
                            )
                        }

                        else -> {
                            Logger.debug("render rest")
                            val template = String(task.response.data, charsetUtf8)
                            Logger.debug("page content: [$template]")
                            sdkInstance!!.render(
                                pageName,
                                template,
                                configMap,
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
                progressBar?.visibility = View.GONE
            }

        }

        WXHttpManager.getInstance().sendRequest(httpTask)
    }

    private fun createWeexInstance(
        renderContainer: WXAbstractRenderContainer,
        url: String?
    ): WXSDKInstance {
        val context = weexHolder.context

        val sdkInstance = if (url == null) {
            WXSDKInstance(context)
        } else {
            WXPreLoadManager.getInstance()
                .offerPreInitInstance(url)?.apply {
                    init(context)
                } ?: WXSDKInstance(context)
        }

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
            Logger.debug("[WXLifecycle] view created: $sdkInstance")

            weexHolder.requestLayout()
        }

        override fun onRenderSuccess(instance: WXSDKInstance?, width: Int, height: Int) {
            Logger.debug("[WXLifecycle] view rendered: $sdkInstance, container = $renderContainer dimen: [$width x $height]")

            progressBar?.visibility = View.GONE

            ensureLayoutOnSize(width, height)
        }

        override fun onRefreshSuccess(instance: WXSDKInstance?, width: Int, height: Int) {
            Logger.debug("[WXLifecycle] refreshed: $sdkInstance")

            progressBar?.visibility = View.GONE

            ensureLayoutOnSize(width, height)
       }

        override fun onException(
            instance: WXSDKInstance?,
            errCode: String,
            msg: String
        ) {
            Logger.debug("[WXLifecycle] exception occurred: $sdkInstance")

            progressBar?.visibility = View.GONE

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

        private fun ensureLayoutOnSize(width: Int, height: Int) {
            Logger.debug("[WXLifecycle] request layout: container = $renderContainer")

            val lp = renderContainer?.layoutParams ?: return

            lp.width = width
            lp.height = height

            renderContainer?.layoutParams = lp
            renderContainer?.requestLayout()
        }
    }

}