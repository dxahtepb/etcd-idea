package com.github.dxahtepb.etcdidea.view.editor

import com.github.dxahtepb.etcdidea.vfs.EtcdDummyVirtualFile
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.fileEditor.FileEditorPolicy
import com.intellij.openapi.fileEditor.FileEditorProvider
import com.intellij.openapi.project.DumbAware
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile

class EtcdEditorProvider : FileEditorProvider, DumbAware {
    override fun accept(project: Project, file: VirtualFile): Boolean = file is EtcdDummyVirtualFile

    override fun createEditor(project: Project, file: VirtualFile): FileEditor {
        return EtcdEditor(project, file as EtcdDummyVirtualFile)
    }

    override fun getEditorTypeId(): String = "EtcdEditorTypeId"

    override fun getPolicy(): FileEditorPolicy = FileEditorPolicy.HIDE_DEFAULT_EDITOR
}
