@file:Suppress("TooGenericExceptionCaught")

package com.github.dxahtepb.etcdidea

@Suppress("RedundantSuspendModifier")
internal suspend inline fun <T : AutoCloseable?, R> T.useAsync(block: (T) -> R): R {
    var exception: Throwable? = null
    try {
        return block(this)
    } catch (e: Throwable) {
        exception = e
        throw e
    } finally {
        this.closeFinally(exception)
    }
}

private fun AutoCloseable?.closeFinally(cause: Throwable?) = when {
    this == null -> {}
    cause == null -> close()
    else ->
        try {
            close()
        } catch (closeException: Throwable) {
            cause.addSuppressed(closeException)
        }
}
