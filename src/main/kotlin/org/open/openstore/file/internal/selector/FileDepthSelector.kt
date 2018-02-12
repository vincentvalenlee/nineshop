package org.open.openstore.file.internal.selector

import org.open.openstore.file.FileInfo
import org.open.openstore.file.FileSelector

/**
 * 根据深度指定的范围选择文件对象的选择器
 */
class FileDepthSelector(val depthRange: IntRange = IntRange(0, 0)): FileSelector {

    constructor(minDepth:Int, maxDepth:Int): this(IntRange(minDepth, maxDepth)) {}

    constructor(minmaxDepth:Int): this(IntRange(minmaxDepth, minmaxDepth)) {}

    override fun visitFile(file: FileInfo): Boolean = file.depth <= depthRange.endInclusive

    override fun selectFile(file: FileInfo): Boolean =  file.depth in depthRange
}