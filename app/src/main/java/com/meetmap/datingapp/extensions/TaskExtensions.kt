package com.meetmap.datingapp.extensions

import com.google.android.gms.tasks.Task
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

suspend fun <T> Task<T>.await(): T {
    return suspendCancellableCoroutine { continuation ->
        addOnCompleteListener { task ->
            if (task.isSuccessful) {
                continuation.resume(task.result)
            } else {
                val exception = task.exception ?: RuntimeException("Unknown task exception")
                continuation.resumeWithException(exception)
            }
        }

        continuation.invokeOnCancellation {
            if (!isComplete) {

            }
        }
    }
}