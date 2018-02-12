package org.open.openstore.file.contributors

import org.open.openstore.file.*


/**
 * 每一个文件贡献者，都是外部接入文件系统的一个入口。他负责处理特定的文件以及特定的URI schema，例如：zip文件系统、http、ftp文件系统等。
 * 其贡献者扩展点名称为：
 */
interface FileContributor:IContributor {

    companion object {

    }

    /**
     * 定位一个文件
     * @param baseFile 用于解析URI特定部分
     * @param uri 要定位的文件uri
     */
    fun findFile(baseFile: FileObject, uri: String, fileSystemOptions: FileSystemOptions): FileObject

    /**
     * 创建文件系统
     * @param scheme uri模式
     * @param file 用于构建文件系统的文件
     */
    fun buildFileSystem(scheme: String, file: FileObject, fileSystemOptions: FileSystemOptions): FileObject

    /**
     * 获取用于收集文件系统选项的构建器
     */
    fun getConfigBuilder(): FileSystemConfigBuilder

    /**
     * 将URI解析为文件名对象
     */
    fun parseUri(root: IFileName, uri: String): IFileName
}