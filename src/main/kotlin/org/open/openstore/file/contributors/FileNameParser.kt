package org.open.openstore.file.contributors

import org.open.openstore.file.FileSystemRepository
import org.open.openstore.file.IFileName

/**
 * 用于将文件名，uri解析成IFileName封装对象的解析器，外部贡献者可以提供自己的实现
 */
interface FileNameParser {
    /**
     * 判断字符是否需要编码为%nn
     */
    fun encodeCharacter(ch: Char): Boolean

    /**
     * 解析指定的名字为文件名称对象
     */
    fun parseUri(context: FileSystemRepository, base: IFileName, uri: String): IFileName
}

/**
 * 如果字符为%，则编码的抽象文件名解析器实现
 */
abstract class AbstractFileNameParser : FileNameParser {
    override fun encodeCharacter(ch: Char): Boolean {
        return ch == '%'
    }
}