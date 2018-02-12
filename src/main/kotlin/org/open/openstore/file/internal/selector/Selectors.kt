package org.open.openstore.file.internal.selector

import org.open.openstore.file.FileSelector
import org.open.openstore.file.FileType

/**
 * 默认的选择器集
 */
object Selectors {
    /**
     * 只选择自身的选择器
     */
    val SELECT_SELF: FileSelector = FileDepthSelector()

    /**
     * 选择自身以及一级子对象
     */
    val SELECT_SELF_AND_CHILDREN: FileSelector = FileDepthSelector(0, 1)

    /**
     * 选择一级子对象
     */
    val SELECT_CHILDREN: FileSelector = FileDepthSelector(1)

    /**
     * 选择所有的子对象，但不包括自己
     */
    val EXCLUDE_SELF: FileSelector = FileDepthSelector(1, Integer.MAX_VALUE)

    /**
     * 只选择文件
     */
    val SELECT_FILES: FileSelector = FileTypeSelector(FileType.FILE)

    /**
     * 只选择文件夹
     */
    val SELECT_FOLDERS: FileSelector = FileTypeSelector(FileType.FOLDER)

    /**
     * 所有的选择器
     */
    val SELECT_ALL: FileSelector = AllFileSelector()
}