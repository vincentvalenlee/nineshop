package org.open.openstore.file.internal.selector

import com.google.gson.JsonObject
import org.open.openstore.file.FileInfo
import org.open.openstore.file.FileMeta
import org.open.openstore.file.FileSelector

/**
 * 根据指定元数据值进行选择文件的选择器
 */
class FileMetaSelector(val meta: JsonObject, val maxDepth: Int = -1): FileSelector {
    override fun visitFile(file: FileInfo): Boolean = if (maxDepth == -1) true else file.depth <= maxDepth

    override fun selectFile(file: FileInfo): Boolean {
        val fileMeta = file.file.getAdapter(FileMeta::class)
        return meta.equals(fileMeta)
    }

}