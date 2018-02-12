package org.open.openstore.file.internal.selector

import org.open.openstore.file.FileInfo
import org.open.openstore.file.FileSelector
import org.open.openstore.file.FileType

/**
 * 根据文件类型选择文件的选择器
 */
class FileTypeSelector(val type: FileType): FileSelector {
    override fun visitFile(file: FileInfo): Boolean = true

    override fun selectFile(file: FileInfo): Boolean = file.file.type() === type

}