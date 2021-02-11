package com.github.dxahtepb.etcdidea.view.browser

import com.github.dxahtepb.etcdidea.model.EtcdServerConfiguration
import com.github.dxahtepb.etcdidea.service.auth.CredentialsService
import com.github.dxahtepb.etcdidea.service.auth.PasswordKey
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.layout.panel
import com.intellij.util.text.nullize
import javax.swing.JComponent

class ConfigureServerDialogWindow(
    project: Project?,
    private val oldConfiguration: EtcdServerConfiguration? = null
) : DialogWrapper(project) {
    private val credentialsService = CredentialsService.instance

    lateinit var dialogPanel: DialogPanel

    var labelText: String = oldConfiguration?.label.orEmpty()
    var hosts: String = oldConfiguration?.hosts ?: "http://192.168.99.100:2379"
    var username: String = oldConfiguration?.user.orEmpty()
    val passwordUi = JBPasswordField().apply {
        oldConfiguration?.let {
            val hasPassword = credentialsService.getPassword(PasswordKey(it.id)) != null
            setPasswordIsStored(hasPassword)
        }
    }

    /**
     * Only valid after doOkAction() is called
     */
    private lateinit var myConfiguration: EtcdServerConfiguration

    init {
        super.init()
        title = "Add Server Configuration"
    }

    override fun createCenterPanel(): JComponent {
        dialogPanel = panel {
            row("Label") { textField(::labelText, 20) }
            row("Hosts") { textField(::hosts, 20) }
            row("User") { textField(::username, 20) }
            row("Password") { passwordUi(growX, pushX) }
        }
        return dialogPanel
    }

    override fun doOKAction() {
        dialogPanel.apply()
        myConfiguration = oldConfiguration?.copy(
            hosts = hosts,
            user = username,
            label = labelText
        ) ?: EtcdServerConfiguration(hosts, username, labelText)
        savePassword()
        super.doOKAction()
    }

    fun getConfiguration(): EtcdServerConfiguration {
        return myConfiguration
    }

    private fun savePassword() {
        val passwordKey = PasswordKey(myConfiguration.id)
        val password = passwordUi.password.nullize()
        if (password == null) {
            credentialsService.forgetPassword(passwordKey)
        } else {
            credentialsService.storePassword(passwordKey, password)
        }
    }
}
