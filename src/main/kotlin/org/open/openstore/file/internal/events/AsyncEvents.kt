package org.open.openstore.file.internal.events


import org.open.openstore.file.FileChangeEvent
import org.open.openstore.file.FileListener
import org.open.openstore.file.FileObject



/**
 * 文件创建事件
 */
class CreateEvent(file: FileObject) : AbstractEvent("create", file) {
    override fun doAction(listener: FileListener, event: FileChangeEvent) {
        listener.fileCreated(event)
    }
}

/**
 * 文件改变事件
 */
class ChangedEvent(file: FileObject) : AbstractEvent("changed", file) {
    override fun doAction(listener: FileListener, event: FileChangeEvent) {
        listener.fileCreated(event)
    }
}

/**
 * 文件删除事件
 */
class DeleteEvent(file: FileObject) : AbstractEvent("delete", file) {
    override fun doAction(listener: FileListener, event: FileChangeEvent) {
        listener.fileDeleted(event)
    }
}

/**
 * 文件resolve事件,通常同步文件。通常在文件的resolve以及各种方法调用之后执行refresh
 */
class ResolvedEvent(file: FileObject) : AbstractEvent("resolved", file) {
    override fun doAction(listener: FileListener, event: FileChangeEvent) {
        listener.fileResolved(event)
    }
}