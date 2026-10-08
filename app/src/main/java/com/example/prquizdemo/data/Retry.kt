package com.example.prquizdemo.data

import java.io.IOException
import kotlinx.coroutines.delay

/**
 * Runs [block] and retries it on [IOException] with exponential backoff.
 *
 * Any other exception is rethrown immediately, because only connectivity problems are expected
 * to go away on their own.
 */
suspend fun <T> retryOnIoError(
  attempts: Int = 3,
  initialDelayMillis: Long = 500,
  factor: Double = 2.0,
  block: suspend () -> T,
): T {
  var delayMillis = initialDelayMillis
  repeat(attempts - 1) {
    try {
      return block()
    } catch (e: IOException) {
      delay(delayMillis)
      delayMillis = (delayMillis * factor).toLong()
    }
  }
  return block()
}
