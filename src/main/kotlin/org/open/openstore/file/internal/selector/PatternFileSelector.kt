package org.open.openstore.file.internal.selector

import org.open.openstore.file.FileInfo
import org.open.openstore.file.FileSelector
import java.util.regex.Pattern

/**
 * 使用正则表达式选择文件
 */
class PatternFileSelector(val pattern: Pattern): FileSelector {

    constructor(regex:String):this(Pattern.compile(regex)) {}

    override fun visitFile(file: FileInfo): Boolean = true

    override fun selectFile(file: FileInfo): Boolean = pattern.matcher(file.file.name().getPath()).matches();

}