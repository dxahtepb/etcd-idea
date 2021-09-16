package com.github.dxahtepb.etcdidea.view.browser

import com.github.dxahtepb.etcdidea.model.EtcdServerConfiguration
import com.github.dxahtepb.etcdidea.model.EtcdSslConfiguration
import com.github.dxahtepb.etcdidea.service.auth.CredentialsService
import com.github.dxahtepb.etcdidea.service.auth.PasswordKey
import com.github.dxahtepb.etcdidea.view.isSelectedPredicate
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.layout.Row
import com.intellij.ui.layout.enableIf
import com.intellij.ui.layout.panel
import com.intellij.util.text.nullize
import javax.swing.JComponent

class ConfigureServerDialogWindow(
    private val project: Project?,
    private val oldConfiguration: EtcdServerConfiguration? = null
) : DialogWrapper(project) {
    private val credentialsService = CredentialsService.instance

    private lateinit var dialogPanel: DialogPanel
    private lateinit var generalPanel: DialogPanel
    private lateinit var sslPanel: DialogPanel

    private var labelText: String = oldConfiguration?.label.orEmpty()
    private var hosts: String = oldConfiguration?.hosts ?: "http://192.168.99.100:2379"
    private var username: String = oldConfiguration?.user.orEmpty()
    private val passwordUi = JBPasswordField().apply {
        oldConfiguration?.let {
            val hasPassword = credentialsService.getPassword(PasswordKey(it.id)) != null
            setPasswordIsStored(hasPassword)
        }
    }

    private lateinit var isSslEnabled: JBCheckBox
    private val sslConfiguration = EtcdSslConfigurationData()

    /**
     * Only valid after doOkAction() is called
     */
    private lateinit var myConfiguration: EtcdServerConfiguration

    init {
        super.init()
        title = "Add Server Configuration"
    }

    override fun createCenterPanel(): JComponent {
        val tabbedPanel = JBTabbedPane().apply {
            addTab("General", createGeneralTab())
            addTab("SSL", createSslTab())
        }
        dialogPanel = panel {
            row("Label:") { textField(::labelText, 20) }
            row { tabbedPanel() }
        }
        return dialogPanel
    }

    private fun createGeneralTab(): DialogPanel {
        generalPanel = panel {
            row("Hosts:") { textField(::hosts, 20) }
            row("User:") { textField(::username, 20) }
            row("Password:") { passwordUi(growX, pushX) }
        }
        return generalPanel
    }

    private fun createSslTab(): DialogPanel {
        sslPanel = panel {
            row {
                checkBox("Enable SSL").also {
                    isSslEnabled = it.component
                }
            }
            row("Certificate") {
                textFieldWithBrowseButton(sslConfiguration::certificate, "Choose Certificate:", project)
            }.enableIfSsl()
            row("Certificate Key") {
                textFieldWithBrowseButton(sslConfiguration::certificateKey, "Choose Certificate Key:", project)
            }.enableIfSsl()
            row("Certificate Authority") {
                textFieldWithBrowseButton(
                    sslConfiguration::certificateAuthority, "Choose Certificate Authority:", project
                )
            }.enableIfSsl()
        }
        return sslPanel
    }

    override fun doOKAction() {
        generalPanel.apply()
        sslPanel.apply()
        dialogPanel.apply()
        myConfiguration = oldConfiguration?.copy(
            hosts = hosts,
            user = username,
            label = labelText,
            sslConfiguration = sslConfiguration.toConfiguration()
        ) ?: EtcdServerConfiguration(
            hosts,
            username,
            labelText,
            sslConfiguration.toConfiguration()
        )
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

    private fun Row.enableIfSsl() = enableIf(isSslEnabled.isSelectedPredicate())
}

data class EtcdSslConfigurationData(
    var certificate: String = "",
    var certificateKey: String = "",
    var certificateAuthority: String = ""
) {
    fun toConfiguration() = EtcdSslConfiguration(certificate, certificateKey, certificateAuthority)
}
