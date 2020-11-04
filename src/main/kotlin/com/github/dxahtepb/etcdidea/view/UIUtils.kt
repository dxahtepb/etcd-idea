package com.github.dxahtepb.etcdidea.view

import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import javax.swing.JComponent
import javax.swing.JPanel

internal fun JComponent.withNoBorder() = this.apply { border = JBUI.Borders.empty() }

internal fun gridConstraints(row: Int, column: Int) =
    GridConstraints().apply {
        this.row = row
        this.column = column
    }

internal fun GridConstraints.leftAlignedLabel() =
    this.apply {
        vSizePolicy = GridConstraints.SIZEPOLICY_FIXED
        hSizePolicy = GridConstraints.SIZEPOLICY_FIXED
        anchor = GridConstraints.ANCHOR_WEST
    }

internal fun GridConstraints.textField() =
    this.apply {
        fill = GridConstraints.FILL_HORIZONTAL
    }

internal fun JPanel.addNorth(content: JComponent) = add(content, BorderLayout.NORTH)
internal fun JPanel.addCenter(content: JComponent) = add(content, BorderLayout.CENTER)
