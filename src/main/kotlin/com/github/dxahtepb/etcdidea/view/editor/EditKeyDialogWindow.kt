package com.github.dxahtepb.etcdidea.view.editor

import com.github.dxahtepb.etcdidea.model.EtcdKeyValue
import com.github.dxahtepb.etcdidea.model.EtcdServerConfiguration
import com.github.dxahtepb.etcdidea.service.EtcdService
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.panel
import javax.swing.JComponent

private const val TEXT_FIELD_SIZE = 15

class EditKeyDialogWindow(
    private val project: Project,
    private val hosts: EtcdServerConfiguration,
    keyValue: EtcdKeyValue
) : DialogWrapper(project, true) {

    private var key = keyValue.key
    private var value = keyValue.value
    private lateinit var dialogPanel: DialogPanel
    private lateinit var keyField: JBTextField

    init {
        title = "Edit Key"
        setOKButtonText("Submit")
        super.init()
    }

    override fun createCenterPanel(): JComponent {
        dialogPanel = panel {
            row {
                keyField = textField(::key, TEXT_FIELD_SIZE).component
                textField(::value, TEXT_FIELD_SIZE)
            }
        }
        keyField.isEnabled = false
        keyField.isEditable = false
        return dialogPanel
    }

    override fun doValidate(): ValidationInfo? {
        dialogPanel.apply()
        if (key == "") {
            return ValidationInfo("Key should not be empty", keyField)
        }
        return null
    }

    override fun doOKAction() {
        dialogPanel.apply()
        EtcdService.getInstance(project).putNewEntry(hosts, EtcdKeyValue(key, value))
        super.doOKAction()
    }
}
