package org.open.openstore.file

/**
 * 封装文件对象的文件名schema、路径、扩展名等机制的接口
 */
interface IFileName: Comparable<IFileName> {

    companion object {
        /**
         * 文件路径的分隔char
         */
        val SEPARATOR_CHAR = '/'

        /**
         * 字符串形式的文件路径分隔符
         */
        val SEPARATOR = "/"

        /**
         * 文件系统根路径："/"
         */
        val ROOT_PATH = "/"

        /**
         * 对于文件系统根目录，没有父文件名称的null object
         */
        val NO_NAME = object: IFileName {
//            override fun setType(newType: FileType) {}

            val EMPTY_STR = ""

            override fun getBaseName(): String = EMPTY_STR

            override fun getPath(): String = EMPTY_STR

            override fun getExtension(): String = EMPTY_STR

            override fun getDepth(): Int = 0

            override fun getScheme(): String = EMPTY_STR

            override fun getURI(): String = EMPTY_STR

            override fun getRootURI(): String = EMPTY_STR

            override fun getRoot(): IFileName = this

            override fun getParent(): IFileName = this

            override fun getRelativeName(name: IFileName): String = EMPTY_STR

            override fun isAncestor(ancestor: IFileName): Boolean = false

            override fun isDescendent(descendent: IFileName, nameScope: NameScope): Boolean = false

            override fun isFile(): Boolean = false

            override fun getType(): FileType = FileType.IMAGINARY

            override fun compareTo(other: IFileName): Int = -1

            override fun getFriendlyURI(): String = EMPTY_STR
        }

        /**
         * 检测指定路径（绝对）是否适合指定的路径范围
         */
        fun checkName(basePath: String, path: String, scope: NameScope): Boolean {
            if (scope == NameScope.FILE_SYSTEM) {
                // All good
                return true
            }

            if (!path.startsWith(basePath)) {
                return false
            }

            var baseLen = basePath.length
            if (FilePlatform.URL_STYPE) {
                // strip the trailing "/"
                baseLen--
            }

            if (scope == NameScope.CHILD) {
                if (path.length == baseLen || baseLen > 1 && path[baseLen] != IFileName.SEPARATOR_CHAR
                        || path.indexOf(IFileName.SEPARATOR_CHAR, baseLen + 1) != -1) {
                    return false
                }
            } else if (scope == NameScope.DESCENDENT) {
                if (path.length == baseLen || baseLen > 1 && path[baseLen] != IFileName.SEPARATOR_CHAR) {
                    return false
                }
            } else if (scope == NameScope.DESCENDENT_OR_SELF) {
                if (baseLen > 1 && path.length > baseLen && path[baseLen] != IFileName.SEPARATOR_CHAR) {
                    return false
                }
            } else if (scope != NameScope.FILE_SYSTEM) {
                throw IllegalArgumentException()
            }

            return true
        }
    }

    /**
     * 文件对象的基本名称为文件名的最后一个元素的名称，例如：/somefolder/somefile的basename为`somefile`.
     */
     fun getBaseName(): String

    /**
     * 获取文件系统下文件的绝对路径。绝对路径下不会存在.或者..
     */
     fun getPath(): String

    /**
     * 获取文件扩展名
     */
     fun getExtension(): String

    /**
     * 获取文件名的深度
     */
     fun getDepth(): Int

    /**
     * 获取文件的schema
     */
     fun getScheme(): String

    /**
     * 获取文件对象的完整URI
     */
     fun getURI(): String

    /**
     * 获取友好uri形式，某些协议，uri中带有密码信息，例如ftp，此方法将过滤uri中的密码信息
     */
    fun getFriendlyURI(): String

    /**
     * 获取此文件所属的文件系统根URI
     */
     fun getRootURI(): String

    /**
     * 获取文件系统的根文件名
     */
     fun getRoot(): IFileName

    /**
     * 获取父文件名
     */
     fun getParent(): IFileName

    /**
     * 获取指定文件名的相对于此文件名的相对路径
     */
     fun getRelativeName(name: IFileName): String

    /**
     * 判断指定的文件名是否为本文件名的祖先
     */
     fun isAncestor(ancestor: IFileName): Boolean

    /**
     * 指定文件名是否为本文件的后代
     */
     fun isDescendent(descendent: IFileName, nameScope: NameScope = NameScope.FILE_SYSTEM): Boolean

    /**
     * 检测文件名是否为标准文件的名称
     */
     fun isFile(): Boolean

    /**
     * 获取当前文件名的类型：
     * 1.如果文件为链接文件，则直接返回链接类型
     * 2.如果以/结尾，则为Folder，否则为file
     */
     fun getType(): FileType
//


}