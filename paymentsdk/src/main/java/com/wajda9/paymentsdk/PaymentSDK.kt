package com.wajda9.paymentsdk

import android.util.Log
import kotlinx.coroutines.delay

object MockPaymentSdk {

    private var isInitialized = false
    private var apiKey: String? = null

// Enable or disable mock processing delays.
    var simulateDelay: Boolean = true

    var logger: ((String, String) -> Unit)? = { tag, message ->
        Log.d(tag, message)
    }

    fun init(apiKey: String) {
        this.apiKey = apiKey
        this.isInitialized = true
        logger?.invoke("MockPaymentSdk", "SDK Initialized with key: ${apiKey.take(4)}****")
    }


     // Represents the result of a payment operation.

    sealed class PaymentResult {
        object Success : PaymentResult()
        data class Failure(val errorCode: String, val message: String) : PaymentResult()
    }

    /**
     * Simulates processing a payment.
     *
     * @param amount The amount to charge.
     * @param currency The currency code (e.g., "USD").
     * @return [PaymentResult] indicating success or failure.
     */
    suspend fun processPayment(amount: Double, currency: String): PaymentResult {
        if (!isInitialized) {
            logger?.invoke("MockPaymentSdk", "Error: SDK not initialized")
            return PaymentResult.Failure("NOT_INITIALIZED", "Call init() before processing payments")
        }

        logger?.invoke("MockPaymentSdk", "Processing payment of $amount $currency...")

        if (simulateDelay) {
            delay(2000) // Simulate network latency
        }
         if (amount < 1000) {
           return PaymentResult.Failure("PAYMENT_REJECTED", "Amount is too low")
        }
        // Simulate a success rate (e.g., 90% success)
        return if (amount > 0 && (1..100).random() > 10) {
            logger?.invoke("MockPaymentSdk", "Payment successful!")
            PaymentResult.Success
        } else {
            val reason = if (amount <= 0) "Invalid amount" else "Insufficient funds"
            logger?.invoke("MockPaymentSdk", "Payment failed: $reason")
            PaymentResult.Failure("PAYMENT_REJECTED", reason)
        }
    }


    fun reset() {
        isInitialized = false
        apiKey = null
        logger?.invoke("MockPaymentSdk", "SDK state reset")
    }
}