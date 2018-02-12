package org.open.openstore.file.internal.selector

import org.open.openstore.file.FileInfo
import org.open.openstore.file.FileSelector

/**
 * 排除指定条件选择器中的文件的选择器,此选择器将访问代理的选择器可访问的文件，并选择代理选择器不选择的文件！
 */
class ExcludeFileSelector(val selector: FileSelector): FileSelector {
    override fun visitFile(file: FileInfo): Boolean = selector.visitFile(file)

    override fun selectFile(file: FileInfo): Boolean = !selector.selectFile(file)

}