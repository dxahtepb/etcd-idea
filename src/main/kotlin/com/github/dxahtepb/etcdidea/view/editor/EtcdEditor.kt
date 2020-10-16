package com.github.dxahtepb.etcdidea.view.editor

import com.github.dxahtepb.etcdidea.service.EtcdService
import com.github.dxahtepb.etcdidea.vfs.EtcdDummyVirtualFile
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorLocation
import com.intellij.openapi.fileEditor.FileEditorState
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.UserDataHolderBase
import java.beans.PropertyChangeListener
import javax.swing.JComponent

class EtcdEditor(project: Project, file: EtcdDummyVirtualFile) :
    FileEditor, UserDataHolderBase() {

    private val editorPanel = EtcdEditorPanel(project, file.configuration, EtcdService.getInstance(project))

    override fun dispose() = Unit

    override fun getComponent(): JComponent = editorPanel.getContent()

    override fun getPreferredFocusedComponent(): JComponent? = editorPanel.getContent()

    override fun getName(): String = "Etcd DataGrid"

    override fun setState(state: FileEditorState) = Unit

    override fun isModified(): Boolean = false

    override fun isValid(): Boolean = true

    override fun addPropertyChangeListener(listener: PropertyChangeListener) = Unit

    override fun removePropertyChangeListener(listener: PropertyChangeListener) = Unit

    override fun getCurrentLocation(): FileEditorLocation? = null
}
