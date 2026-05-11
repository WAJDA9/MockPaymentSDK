package com.wajda9.paymentsdk

import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Bundle
import android.view.Gravity
import android.view.View
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

        // Full screen UI layout
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(0xFFFFFFFF.toInt())
            setPadding(64, 64, 64, 64)
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
        }

        val title = TextView(this).apply {
            text = "Ready to Scan"
            textSize = 28f
            setTextColor(0xFF000000.toInt())
            setPadding(0, 0, 0, 32)
            gravity = Gravity.CENTER
        }

        val details = TextView(this).apply {
            text = "Hold your card or phone near the back of this device\n\nTotal: $amount $currency"
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(0xFF666666.toInt())
        }

        val nfcStatus = TextView(this).apply {
            text = if (nfcAdapter == null) "NFC not supported on this device" 
                   else if (!nfcAdapter!!.isEnabled) "Please enable NFC in settings"
                   else "Waiting for tap..."
            textSize = 16f
            setPadding(0, 100, 0, 0)
            setTextColor(0xFF2196F3.toInt())
            gravity = Gravity.CENTER
        }

        // Keep the manual tap area for testing/fallback
        val manualTap = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(200, 200).apply {
                setMargins(0, 100, 0, 0)
            }
            setBackgroundColor(0xFFEEEEEE.toInt())
            setOnClickListener {
                finishWithResult(MockPaymentSdk.PaymentResult.Success)
            }
        }

        root.addView(title)
        root.addView(details)
        root.addView(nfcStatus)
        root.addView(manualTap)
        
        setContentView(root)
    }

    override fun onResume() {
        super.onResume()
        // Enable NFC Reader Mode
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

    /**
     * Called when an NFC tag is discovered.
     */
    override fun onTagDiscovered(tag: Tag?) {
        // In a real SDK, you'd process the APDUs here.
        // For this mock, any tap is a success.
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
        // If finished by back button/dismissal without a result
        if (!isFinished) {
            MockPaymentSdk.onPaymentFinished(
                MockPaymentSdk.PaymentResult.Failure("CANCELLED", "User exited payment screen")
            )
        }
    }
}
