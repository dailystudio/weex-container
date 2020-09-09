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

class MultipleWEEXFragment: Fragment() {

    private var weexsContainer: ViewGroup? = null

    private val containers: MutableMap<String, Pair<View, WEEXContainer>> = mutableMapOf()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_multiple_weex, null)

        setupViews(view)

        return view
    }

    private fun setupViews(fragmentView: View) {
        weexsContainer = fragmentView.findViewById(R.id.weexs)
    }

    fun addWeexByUri(name: String,
                     uri: Uri) {
        if (weexsContainer == null) {
            return
        }

        Logger.debug("[WXLifecycle] load by url: fragment view = $view")

        val weexHolderView = createHolderView()
        val weexContainer: WEEXContainer = createWeexContainer(weexHolderView) ?: return

        weexsContainer?.addView(weexHolderView)

        weexContainer.loadPageByUrl(uri)

        containers[name] = Pair(weexHolderView, weexContainer)
    }

    fun removeWeex(name: String) {
        if (weexsContainer == null) {
            return
        }

        Logger.debug("[WXLifecycle] release weex for url: fragment view = $view")
        if (!containers.containsKey(name)) {
            return
        }

        val removed = containers.remove(name) ?: return

        weexsContainer?.removeView(removed.first)
        removed.second.release()
    }

    private fun createWeexContainer(holderView: View): WEEXContainer? {
        val container: ViewGroup = holderView.findViewById(R.id.container)
            ?: return null
        val progress : ProgressBar = holderView.findViewById(R.id.progress)

        return WEEXContainer(container, progress, lifecycle)
    }

    private fun createHolderView(): View {
        val context = requireContext()

        return LayoutInflater.from(context).inflate(
            R.layout.layout_weex_card, null)
    }

}