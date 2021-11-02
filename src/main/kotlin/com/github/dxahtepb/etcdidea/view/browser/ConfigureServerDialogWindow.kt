package com.github.dxahtepb.etcdidea.view.browser

import com.github.dxahtepb.etcdidea.EtcdBundle
import com.github.dxahtepb.etcdidea.model.EtcdServerConfiguration
import com.github.dxahtepb.etcdidea.model.EtcdServerHosts
import com.github.dxahtepb.etcdidea.model.EtcdTimeoutConfiguration
import com.github.dxahtepb.etcdidea.service.auth.CredentialsService
import com.github.dxahtepb.etcdidea.service.auth.PasswordKey
import com.github.dxahtepb.etcdidea.view.browser.model.EtcdSslViewModel
import com.github.dxahtepb.etcdidea.view.isSelectedPredicate
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.components.JBTabbedPane
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.Row
import com.intellij.ui.layout.enableIf
import com.intellij.ui.layout.panel
import com.intellij.util.text.nullize
import java.time.Duration
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
    private var hosts: String = oldConfiguration?.hosts?.asString() ?: "http://localhost:2379"
    private var username: String = oldConfiguration?.user.orEmpty()
    private lateinit var passwordUi: JBPasswordField
    private lateinit var timeoutField: JBTextField
    private var timeoutMillis: Int = oldConfiguration?.timeouts
        ?.applicationTimeout
        ?.toMillis()
        ?.toInt() ?: 10_000 // 10 seconds

    private lateinit var isSslEnabled: JBCheckBox
    private val sslConfiguration = EtcdSslViewModel.fromConfiguration(oldConfiguration?.sslConfiguration)

    /**
     * Only valid after doOkAction() is called
     */
    private lateinit var myConfiguration: EtcdServerConfiguration

    init {
        super.init()
        title = EtcdBundle.getMessage("browser.configuration.dialogTitle")
    }

    override fun createCenterPanel(): JComponent {
        val tabbedPanel = JBTabbedPane().apply {
            addTab(EtcdBundle.getMessage("browser.configuration.tab.general"), createGeneralTab())
            addTab(EtcdBundle.getMessage("browser.configuration.tab.ssl"), createSslTab())
        }
        dialogPanel = panel {
            row(EtcdBundle.getMessage("browser.configuration.field.label")) { textField(::labelText, 20) }
            row { tabbedPanel() }
        }
        return dialogPanel
    }

    private fun createGeneralTab(): DialogPanel {
        generalPanel = panel {
            row(EtcdBundle.getMessage("browser.configuration.general.field.hosts")) { textField(::hosts) }
            row(EtcdBundle.getMessage("browser.configuration.general.field.user")) { textField(::username) }
            row(EtcdBundle.getMessage("browser.configuration.general.field.password")) {
                passwordUi = JBPasswordField().apply {
                    oldConfiguration?.let {
                        val hasPassword = credentialsService.getPassword(PasswordKey(it.id)) != null
                        setPasswordIsStored(hasPassword)
                    }
                }
                passwordUi(growX, pushX)
            }
            row(EtcdBundle.getMessage("browser.configuration.general.field.timeout")) {
                cell {
                    timeoutField = intTextField(::timeoutMillis, 10).component
                    commentNoWrap(EtcdBundle.getMessage("browser.configuration.general.field.timeout.ms"))
                }
            }
        }
        return generalPanel
    }

    private fun createSslTab(): DialogPanel {
        sslPanel = panel {
            row {
                checkBox(
                    EtcdBundle.getMessage("browser.configuration.ssl.field.isEnableSsl"),
                    sslConfiguration::sslEnabled
                ).also {
                    isSslEnabled = it.component
                }
            }
            row(EtcdBundle.getMessage("browser.configuration.ssl.field.caFile")) {
                textFieldWithBrowseButton(
                    sslConfiguration::rootCertificate,
                    EtcdBundle.getMessage("browser.configuration.ssl.field.caFile.dialog"),
                    project
                )
            }.enableIfSsl()
            row(EtcdBundle.getMessage("browser.configuration.ssl.field.clientKeyFile")) {
                textFieldWithBrowseButton(
                    sslConfiguration::clientKey,
                    EtcdBundle.getMessage("browser.configuration.ssl.field.clientKeyFile.dialog"),
                    project
                )
            }.enableIfSsl()
            row(EtcdBundle.getMessage("browser.configuration.ssl.field.clientCertificateFile")) {
                textFieldWithBrowseButton(
                    sslConfiguration::clientKeyChain,
                    EtcdBundle.getMessage("browser.configuration.ssl.field.clientCertificateFile.dialog"),
                    project
                )
            }.enableIfSsl()
        }
        return sslPanel
    }

    override fun doValidate(): ValidationInfo? {
        generalPanel.apply()
        dialogPanel.apply()
        if (timeoutMillis <= 0) {
            return ValidationInfo(EtcdBundle.getMessage("browser.configuration.validation.timeout.fail"), timeoutField)
        }
        return null
    }

    override fun doOKAction() {
        generalPanel.apply()
        sslPanel.apply()
        dialogPanel.apply()
        val duration = Duration.ofMillis(timeoutMillis.toLong())
        myConfiguration = oldConfiguration?.copy(
            hosts = EtcdServerHosts.create(hosts),
            user = username,
            label = labelText,
            timeouts = EtcdTimeoutConfiguration(duration),
            sslConfiguration = sslConfiguration.toConfiguration()
        ) ?: EtcdServerConfiguration(
            EtcdServerHosts.create(hosts),
            username,
            labelText,
            EtcdTimeoutConfiguration(duration),
            sslConfiguration = sslConfiguration.toConfiguration()
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
        if (password != null) {
            credentialsService.storePassword(passwordKey, password)
        }
        password?.fill(0.toChar())
    }

    private fun Row.enableIfSsl() = enableIf(isSslEnabled.isSelectedPredicate())
}
