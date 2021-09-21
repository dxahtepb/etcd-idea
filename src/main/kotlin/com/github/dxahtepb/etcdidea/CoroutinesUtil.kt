package com.github.dxahtepb.etcdidea

import com.intellij.openapi.application.AppUIExecutor
import com.intellij.openapi.application.ModalityState
import com.intellij.openapi.application.impl.coroutineDispatchingContext
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.withTimeoutOrNull
import java.time.Duration
import kotlin.coroutines.ContinuationInterceptor

val UI_DISPATCHER = AppUIExecutor.onUiThread().coroutineDispatchingContext()

fun uiDispatcher(modalityState: ModalityState? = null): ContinuationInterceptor {
    return if (modalityState != null) {
        AppUIExecutor.onUiThread(modalityState)
    } else {
        AppUIExecutor.onUiThread()
    }.coroutineDispatchingContext()
}

suspend fun <T> Deferred<T>.await(timeout: Duration, defaultValue: T) =
    withTimeoutOrNull(timeout.toMillis()) { await() } ?: defaultValue

suspend fun <T> Deferred<T>.awaitOrThrow(timeout: Duration) =
    withTimeoutOrNull(timeout.toMillis()) { await() } ?: throw AwaitTimeoutException()

class AwaitTimeoutException : Exception("Cannot connect to the etcd service")
