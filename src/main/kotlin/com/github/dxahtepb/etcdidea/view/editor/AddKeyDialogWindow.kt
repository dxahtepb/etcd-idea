package com.github.dxahtepb.etcdidea.view.editor

import com.github.dxahtepb.etcdidea.model.EtcdKeyValue
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.panel
import javax.swing.JComponent

private const val TEXT_FIELD_SIZE = 15

class AddKeyDialogWindow(
    project: Project
) : DialogWrapper(project, true) {

    private var key = "key"
    private var value = "value"
    private lateinit var dialogPanel: DialogPanel
    private lateinit var keyField: JBTextField

    init {
        title = "Add Key"
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
        super.doOKAction()
    }

    fun getKv() = EtcdKeyValue(key, value)
}
