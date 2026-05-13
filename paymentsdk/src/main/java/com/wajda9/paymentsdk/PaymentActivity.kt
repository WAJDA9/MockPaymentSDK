package com.wajda9.paymentsdk

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class PaymentActivity : AppCompatActivity(), NfcAdapter.ReaderCallback {

    private var nfcAdapter: NfcAdapter? = null
    private var isFinished = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        
        val amount = intent.getDoubleExtra("amount", 0.0)
        val currency = intent.getStringExtra("currency") ?: "USD"

        // Root container - no global gravity to allow logo at top
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setBackgroundColor(0xFFFFFFFF.toInt())
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        // 1. Logo at the top
        val logoContainer = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(0, 80, 0, 0)
        }
        
        val logoImageView = ImageView(this).apply {
            // Placeholder for logo - replace with R.drawable.your_logo
            setImageResource(android.R.drawable.app_logo_blue)
            layoutParams = LinearLayout.LayoutParams(120, 120)
            alpha = 0.5f
        }
        logoContainer.addView(logoImageView)

        // 2. Center Content Container (Weighted to fill space)
        val centerContainer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                0,
                1.0f
            )
            setPadding(64, 0, 64, 0)
        }

        val title = TextView(this).apply {
            text = "Ready to Scan"
            textSize = 28f
            setTextColor(0xFF000000.toInt())
            setPadding(0, 0, 0, 16)
            gravity = Gravity.CENTER
        }

        val details = TextView(this).apply {
            text = "Total: $amount $currency"
            textSize = 20f
            gravity = Gravity.CENTER
            setTextColor(0xFF333333.toInt())
            setPadding(0, 0, 0, 48)
        }

        // Center Illustration
        val illustrationImageView = ImageView(this).apply {
            // Placeholder for illustration - replace with R.drawable.nfc_tap_icon
            setImageResource(android.R.drawable.nfc_tap)
            layoutParams = LinearLayout.LayoutParams(450, 450).apply {
                gravity = Gravity.CENTER
            }
            scaleType = ImageView.ScaleType.FIT_CENTER
        }

        val nfcStatus = TextView(this).apply {
            text = if (nfcAdapter == null) "NFC not supported" 
                   else if (!nfcAdapter!!.isEnabled) "Please enable NFC in settings"
                   else "Hold your card near the back of the device"
            textSize = 16f
            setPadding(0, 64, 0, 0)
            setTextColor(0xFF2196F3.toInt())
            gravity = Gravity.CENTER
        }

        // Invisible test area (Manual Tap fallback)
        val manualTap = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                setMargins(0, 32, 0, 0)
            }
            setBackgroundColor(0x05000000.toInt()) // Nearly invisible
            setOnClickListener {
                finishWithResult(MockPaymentSdk.PaymentResult.Success)
            }
        }

        centerContainer.addView(title)
        centerContainer.addView(details)
        centerContainer.addView(illustrationImageView)
        centerContainer.addView(nfcStatus)
        centerContainer.addView(manualTap)

        // Add both to root
        root.addView(logoContainer)
        root.addView(centerContainer)
        
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        nfcAdapter?.enableReaderMode(
            this,
            this,
            NfcAdapter.FLAG_READER_NFC_A or NfcAdapter.FLAG_READER_NFC_B or 
            NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK,
            null
        )
    }

    override fun onPause() {
        super.onPause()
        nfcAdapter?.disableReaderMode(this)
    }

    override fun onTagDiscovered(tag: Tag?) {
        runOnUiThread {
            finishWithResult(MockPaymentSdk.PaymentResult.Success)
        }
    }

    private fun finishWithResult(result: MockPaymentSdk.PaymentResult) {
        if (!isFinished) {
            isFinished = true
            MockPaymentSdk.onPaymentFinished(result)
            finish()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        if (!isFinished) {
            MockPaymentSdk.onPaymentFinished(
                MockPaymentSdk.PaymentResult.Failure("CANCELLED", "User exited payment screen")
            )
        }
    }
}
