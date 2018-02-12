package org.open.openstore.file.internal.selector

import org.open.openstore.file.FileInfo
import org.open.openstore.file.FileSelector

/**
 * 根据扩展名进行筛选的筛选器
 */
class FileExtensionSelector(val extensions: Array<String>): FileSelector {
    override fun visitFile(file: FileInfo): Boolean = true

    override fun selectFile(file: FileInfo): Boolean = !this.extensions.isEmpty() ||  this.extensions.any { file.file.name().getExtension().toLowerCase() == it.toLowerCase() }

}