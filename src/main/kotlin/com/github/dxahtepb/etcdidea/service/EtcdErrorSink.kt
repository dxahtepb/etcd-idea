package com.github.dxahtepb.etcdidea.service

import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications
import com.intellij.openapi.application.ApplicationManager
import io.etcd.jetcd.Client
import java.util.concurrent.ExecutionException

internal fun <T> notificationErrorSink(f: (Client) -> T?) = f.withNotificationErrorSink()

internal fun <T> ((Client) -> T?).withNotificationErrorSink(): (Client) -> T? {
    return this.withErrorSink(notificationErrorSink)
}

internal fun <T> ((Client) -> T?).withErrorSink(sink: (Exception) -> Unit): (Client) -> T? {
    return decorated@{
        return@decorated try {
            this.invoke(it)
        } catch (@Suppress("TooGenericExceptionCaught") e: Exception) {
            sink(e)
            null
        }
    }
}

internal val notificationErrorSink = { e: Throwable ->
    val cause = getCause(e)
    if (!ApplicationManager.getApplication().isUnitTestMode) {
        Notifications.Bus.notify(
            Notification(
                "Etcd Browser",
                "Etcd error",
                cause.message ?: "Unknown error",
                NotificationType.ERROR
            )
        )
    } else {
        throw cause
    }
}

private fun getCause(e: Throwable) = if (e is ExecutionException) e.cause ?: e else e
