package com.project.sproutling.utils

import android.util.Log
import kotlinx.coroutines.delay

class RetryPolicy(
    private val maxAttempts: Int = 3,
    private val initialDelayMs: Long = 1000,
    private val maxDelayMs: Long = 5000,
    private val backoffMultiplier: Double = 2.0
) {
    suspend fun <T> retry(
        operation: String,
        action: suspend () -> T?
    ): T? {
        var currentDelay = initialDelayMs
        repeat(maxAttempts) { attempt ->
            try {
                val result = action()
                if (result != null) {
                    if (attempt > 0) {
                        Log.d("RetryPolicy", "Successfully completed $operation after ${attempt + 1} attempts")
                    }
                    return result
                }
                // If we get null, for watering operation we consider it ok
                if (operation == "water plant command") {
                    Log.d("RetryPolicy", "Water plant operation completed with expected disconnect")
                    return null
                }
            } catch (e: Exception) {
                val isLastAttempt = attempt == maxAttempts - 1
                if (isLastAttempt) {
                    Log.d("RetryPolicy", "Final attempt for $operation completed: ${e.message}")
                    return null  // Return null instead of throwing
                }

                Log.d("RetryPolicy", "Attempt ${attempt + 1} failed for $operation, retrying after $currentDelay ms")
                delay(currentDelay)
                currentDelay = (currentDelay * backoffMultiplier).toLong().coerceAtMost(maxDelayMs)
            }
        }
        return null
    }
}