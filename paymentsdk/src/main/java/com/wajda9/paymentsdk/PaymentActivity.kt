package com.wajda9.paymentsdk

import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import android.view.Gravity
import android.view.View

class PaymentActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val amount = intent.getDoubleExtra("amount", 0.0)
        val currency = intent.getStringExtra("currency") ?: "USD"

        // Create a simple UI programmatically to avoid resource issues in this environment
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setBackgroundColor(0xFFF5F5F5.toInt())
            setPadding(32, 32, 32, 32)
        }

        val title = TextView(this).apply {
            text = "NFC Payment Simulation"
            textSize = 24f
            setTextColor(0xFF000000.toInt())
            setPadding(0, 0, 0, 64)
        }

        val details = TextView(this).apply {
            text = "Amount: $amount $currency\n\nTap your phone on the reader"
            textSize = 18f
            gravity = Gravity.CENTER
            setTextColor(0xFF333333.toInt())
        }

        val tapArea = View(this).apply {
            layoutParams = LinearLayout.LayoutParams(400, 400).apply {
                setMargins(0, 64, 0, 0)
            }
            setBackgroundColor(0xFF2196F3.toInt())
            setOnClickListener {
                // Simulate success on tap
                MockPaymentSdk.onPaymentFinished(MockPaymentSdk.PaymentResult.Success)
                finish()
            }
        }

        val tapText = TextView(this).apply {
            text = "TAP HERE"
            setTextColor(0xFFFFFFFF.toInt())
            gravity = Gravity.CENTER
        }
        
        root.addView(title)
        root.addView(details)
        root.addView(tapArea)
        
        setContentView(root)
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // Ensure we don't leave the SDK waiting if the activity is dismissed
        MockPaymentSdk.onPaymentFinished(
            MockPaymentSdk.PaymentResult.Failure("CANCELLED", "User closed the payment screen")
        )
    }
}
