package com.wajda9.paymentsdk

import android.content.Context
import android.content.Intent
import android.util.Log
import kotlinx.coroutines.CompletableDeferred

/**
 * MockPaymentSdk is a lightweight utility for simulating payment processing.
 * It provides a simple API to initialize and execute mock transactions with a UI.
 */
object MockPaymentSdk {

    private var isInitialized = false
    private var apiKey: String? = null
    private var currentPaymentDeferred: CompletableDeferred<PaymentResult>? = null

    /** Enable or disable tracking globally. */
    var isEnabled: Boolean = true

    /**
     * Optional custom logger.
     * Default: logs to Logcat with tag "MockPaymentSdk"
     */
    var logger: ((String, String) -> Unit)? = { tag, message ->
        Log.d(tag, message)
    }

    /**
     * Initializes the SDK with an API key.
     */
    fun init(apiKey: String) {
        this.apiKey = apiKey
        this.isInitialized = true
        logger?.invoke("MockPaymentSdk", "SDK Initialized with key: ${apiKey.take(4)}****")
    }

    /**
     * Represents the result of a payment operation.
     */
    sealed class PaymentResult {
        object Success : PaymentResult()
        data class Failure(val errorCode: String, val message: String) : PaymentResult()
    }

    /**
     * Starts the Payment UI and waits for the user to "Tap".
     * 
     * @param context The context used to start the activity.
     * @param amount The amount to charge.
     * @param currency The currency code (e.g., "USD").
     * @return [PaymentResult] indicating success or failure.
     */
    suspend fun processPayment(context: Context, amount: Double, currency: String): PaymentResult {
        if (!isInitialized) {
            logger?.invoke("MockPaymentSdk", "Error: SDK not initialized")
            return PaymentResult.Failure("NOT_INITIALIZED", "Call init() before processing payments")
        }

        // If a payment is already in progress, fail the new one
        if (currentPaymentDeferred != null) {
            return PaymentResult.Failure("ALREADY_IN_PROGRESS", "A payment is already being processed")
        }

        val deferred = CompletableDeferred<PaymentResult>()
        currentPaymentDeferred = deferred

        logger?.invoke("MockPaymentSdk", "Launching Payment UI for $amount $currency...")

        val intent = Intent(context, PaymentActivity::class.java).apply {
            putExtra("amount", amount)
            putExtra("currency", currency)
            // Ensure we can start it from non-activity contexts if needed
            if (context !is android.app.Activity) {
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }
        }
        context.startActivity(intent)

        return deferred.await()
    }

    /**
     * Internal helper called by PaymentActivity to deliver the result back to the caller.
     */
    internal fun onPaymentFinished(result: PaymentResult) {
        currentPaymentDeferred?.complete(result)
        currentPaymentDeferred = null
    }

    /**
     * Resets the SDK state and cancels any pending payment.
     */
    fun reset() {
        isInitialized = false
        apiKey = null
        currentPaymentDeferred?.complete(PaymentResult.Failure("RESET", "SDK was reset"))
        currentPaymentDeferred = null
        logger?.invoke("MockPaymentSdk", "SDK state reset")
    }
}
