package com.github.dxahtepb.etcdidea.view.browser

import com.github.dxahtepb.etcdidea.model.EtcdServerConfiguration
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.layout.panel
import javax.swing.JComponent

class ConfigureServerDialogWindow(
    project: Project?,
    configuration: EtcdServerConfiguration? = null
) : DialogWrapper(project) {
    lateinit var dialogPanel: DialogPanel

    private val id: String = configuration?.id ?: EtcdServerConfiguration.generateNewUniqueId()
    var labelText: String = configuration?.label.orEmpty()
    var hosts: String = configuration?.hosts ?: "http://192.168.99.100:2379"
    var username: String = configuration?.user.orEmpty()
    val passwordUi = JBPasswordField().apply {
        setPasswordIsStored(configuration?.password.orEmpty().isNotEmpty())
    }

    init {
        super.init()
        title = "Add Server Configuration"
    }

    override fun createCenterPanel(): JComponent {
        dialogPanel = panel {
            row("Label") { textField(::labelText) }
            row("Hosts") { textField(::hosts, 20) }
            row("User") { textField(::username) }
            row("Password") { passwordUi(growX, pushX) }
        }
        return dialogPanel
    }

    override fun doOKAction() {
        dialogPanel.apply()
        super.doOKAction()
    }

    fun getConfiguration(): EtcdServerConfiguration {
        return EtcdServerConfiguration(id, hosts, username, labelText, String(passwordUi.password))
    }
}
