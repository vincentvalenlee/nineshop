package org.open.openstore.file.internal.selector

import org.open.openstore.file.FileInfo
import org.open.openstore.file.FileSelector


/**
 * 只选择文件夹或文件或全部的文件选择器
 */
class AllFileSelector(val type: AllFileType = AllFileType.ALL): FileSelector {
    override fun visitFile(file: FileInfo): Boolean = true

    override fun selectFile(file: FileInfo): Boolean = when (type) {
                AllFileType.ALL ->
                    true
                AllFileType.FOLDER ->
                    file.file.isFolder()
                else ->
                    file.file.isFile()
    }

    enum class AllFileType {
        ALL,
        FILE,
        FOLDER
    }
}