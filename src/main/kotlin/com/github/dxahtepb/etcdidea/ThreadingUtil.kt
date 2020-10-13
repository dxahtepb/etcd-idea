package com.github.dxahtepb.etcdidea

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ModalityState
import com.intellij.ui.GuiUtils
import java.util.concurrent.Executor

@JvmField val POOL_EXECUTOR = Executor { command: Runnable ->
    ApplicationManager.getApplication().executeOnPooledThread(command)
}
@JvmField val EDT_EXECUTOR = Executor { command: Runnable ->
    GuiUtils.invokeLaterIfNeeded(command, ModalityState.defaultModalityState())
}
@JvmField val NON_EDT_EXECUTOR = Executor { command: Runnable ->
    if (ApplicationManager.getApplication().isDispatchThread) POOL_EXECUTOR.execute(command)
    else command.run()
}
