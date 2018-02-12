package org.open.openstore.file.contributors

import org.open.openstore.file.FileSystemOptions
import org.open.openstore.file.FileSystemOptions.Companion.EMPTY_OPTIONS


/**
 * 标识一个文件系统的key
 */
class FileSystemKey(private val key: Comparable<*>, private val fileSystemOptions: FileSystemOptions = EMPTY_OPTIONS):Comparable<FileSystemKey> {

    override fun compareTo(o: FileSystemKey): Int {
        val comparable = key as Comparable<Comparable<*>>// Keys must implement comparable, and be comparable to themselves
        val ret = comparable.compareTo(o.key)
        if (ret != 0) {
            // other filesystem
            return ret
        }

        return fileSystemOptions.compareTo(o.fileSystemOptions)
    }

    override fun toString(): String {
        return super.toString() + " [key=" + key + ", fileSystemOptions=" + fileSystemOptions + "]"
    }
}