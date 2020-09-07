package com.dailystudio.weex.examples

import android.content.Intent
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import com.dailystudio.devbricksx.app.activity.ActivityLauncher
import com.dailystudio.devbricksx.development.Logger
import com.dailystudio.weex.activity.CaptureActivityPortrait
import com.google.zxing.integration.android.IntentIntegrator
import org.apache.weex.commons.AbstractWeexActivity

class MainActivity : AbstractWeexActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_main)

        setupViews()
    }

    private fun setupViews() {
        val fab: View? = findViewById(R.id.fab_qrcode)
        fab?.setOnClickListener {
            IntentIntegrator(this)
                .setPrompt(getString(R.string.prompt_qr_code_scan))
                .setOrientationLocked(true)
                .setBeepEnabled(true)
                .setCaptureActivity(CaptureActivityPortrait::class.java)
                .initiateScan()
        }

        val container: ViewGroup? = findViewById(R.id.weex_container)
        container?.let {
            setContainer(it)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        val scanResult =
            IntentIntegrator.parseActivityResult(requestCode, resultCode, data)
        if (scanResult != null) {
            val urlFromQRCode: String? = scanResult.contents
            Logger.debug("url from QR code: $urlFromQRCode")
            urlFromQRCode?.let {
//                renderPageByURL(it)
                val intent = Intent("com.taobao.android.intent.action.WEEX")
                intent.data = Uri.parse(urlFromQRCode)

                ActivityLauncher.launchActivity(this, intent)
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

}
