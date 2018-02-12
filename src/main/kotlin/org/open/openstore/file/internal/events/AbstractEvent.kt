package org.open.openstore.file.internal.events

import org.open.openstore.file.FileChangeEvent
import org.open.openstore.file.FileListener
import org.open.openstore.file.FileObject
import io.reactivex.Observable
import io.reactivex.functions.Consumer

/**
 * 通知器，使用某种方式触发文件监听器
 */
interface IFileNotifier {
    fun notify(listener: FileListener)
}

/**
 * 抽象事件，记录发生的文件系统信息，同时触发监听器的相关方法
 */
abstract class AbstractEvent(name:String, file: FileObject): FileChangeEvent(name, file), IFileNotifier {
    override fun notify(listener: FileListener) {
        Observable.just(this).subscribe(Consumer<FileChangeEvent> {
            doAction(listener, it)
        })
    }

    abstract fun doAction(listener: FileListener, event: FileChangeEvent)
}