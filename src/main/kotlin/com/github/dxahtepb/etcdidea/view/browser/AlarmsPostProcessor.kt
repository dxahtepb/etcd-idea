package com.github.dxahtepb.etcdidea.view.browser

import com.github.dxahtepb.etcdidea.EtcdBundle
import com.github.dxahtepb.etcdidea.model.EtcdAlarms
import com.intellij.notification.Notification
import com.intellij.notification.NotificationType
import com.intellij.notification.Notifications

internal class AlarmsPostProcessor(private val etcdAlarms: EtcdAlarms) {
    fun process() {
        if (etcdAlarms.alarms.isEmpty()) {
            Notifications.Bus.notify(
                Notification(
                    "Etcd Browser",
                    EtcdBundle.getMessage("browser.notification.healthCheck"),
                    EtcdBundle.getMessage("browser.notification.healthCheck.noAlarms"),
                    NotificationType.INFORMATION
                )
            )
        } else {
            Notifications.Bus.notify(
                Notification(
                    "Etcd Browser",
                    EtcdBundle.getMessage("browser.notification.healthCheck"),
                    EtcdBundle.getMessage("browser.notification.healthCheck.countAlarms", etcdAlarms.alarms.size),
                    NotificationType.WARNING
                )
            )
        }
    }
}
