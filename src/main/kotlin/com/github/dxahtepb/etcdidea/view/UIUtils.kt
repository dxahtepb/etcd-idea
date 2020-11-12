package com.github.dxahtepb.etcdidea.view

import com.intellij.ui.components.JBScrollPane
import com.intellij.uiDesigner.core.GridConstraints
import com.intellij.util.ui.JBUI
import java.awt.BorderLayout
import java.awt.Component
import javax.swing.JComponent
import javax.swing.JPanel

internal fun <T : JComponent> T.withNoBorder(): T = this.apply { border = JBUI.Borders.empty() }

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
internal fun JPanel.addWest(content: JComponent) = add(content, BorderLayout.WEST)
internal fun JPanel.addEast(content: JComponent) = add(content, BorderLayout.EAST)

fun Component.getScrollComponent() = JBScrollPane(this)
