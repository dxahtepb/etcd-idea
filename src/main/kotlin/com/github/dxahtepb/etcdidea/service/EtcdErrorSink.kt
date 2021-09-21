package com.github.dxahtepb.etcdidea.service

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.diagnostic.logger
import io.etcd.jetcd.Client
import java.util.concurrent.ExecutionException

private val LOG = logger<EtcdService>()

internal suspend fun <T> notificationErrorSink(f: suspend (Client) -> T?) = f.withNotificationErrorSink()

internal suspend fun <T> (suspend (Client) -> T?).withNotificationErrorSink(): suspend (Client) -> T? {
    return this.withErrorSink(notificationErrorSink)
}

internal suspend fun <T> (suspend (Client) -> T?).withErrorSink(sink: (Exception) -> Unit): suspend (Client) -> T? = {
    try {
        this.invoke(it)
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        sink(e)
        null
    }
}

internal suspend fun <V> executeWithErrorSink(sink: (Exception) -> Unit, action: suspend () -> V?): V? {
    try {
        return action()
    } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
        sink(e)
    }
    return null
}

internal val notificationErrorSink = { e: Throwable ->
    val cause = getCause(e)
    if (!ApplicationManager.getApplication().isUnitTestMode) {
        LOG.warn(cause)
        Notifications.Bus.notify(
            Notification(
                "Etcd Browser",
                "Etcd error",
                cause.message ?: "${e.message}: Unknown error",
                NotificationType.ERROR
            )
        )
    } else {
        throw cause
    }
}

private fun getCause(e: Throwable) = if (e is ExecutionException) e.cause ?: e else e
