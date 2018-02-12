package org.open.openstore.file

/**
 * 监听文件创建、删除、改变的监听器接口。由各自的文件系统定义并监听
 */
interface FileListener {

    fun fileCreated(event: FileChangeEvent)

    fun fileDeleted(event: FileChangeEvent)

    fun fileChanged(event: FileChangeEvent)

    fun fileResolved(event:FileChangeEvent)
}

open class FileChangeEvent(val name: String, val file: FileObject) {}