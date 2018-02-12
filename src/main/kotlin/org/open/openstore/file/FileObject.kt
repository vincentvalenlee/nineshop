package org.open.openstore.file

import java.io.Closeable
import java.net.URL

/**
 * 文件对象抽象和封装了任何一个系统通信、数据的结构化、半结构化实体。文件是按照特定层次路径的方式进行组织的，这种组织方式，由一个
 * “文件系统”来描述，每一种文件系统都按照自己的方式组织并读写文件，例如：本地os文件系统、http服务器以及zip文件等
 * <h2>读取和写入一个文件</h2>
 * 读取和写入一个文件的内容，都是通过FileAccessor接口，此接口通过getContent方法返回
 * <h2>创建和删除一个文件</h2>
 * 一个文件可以通过createFolder或者createFile创建，也可以通过文件访问器的相关方法创建
 * <p>文件对象，也定义了相关的“贡献点”。外部开发者，可以通过贡献点为文件系统增加额外的贡献行为
 * <p>
 *     注意：文件对象并不提供元数据信息（包括自定义属性）的获取和设置方法，但可以通过IAdaptable获取：getAdatper(FileMeta.class)
 */
interface FileObject:  Comparable<FileObject>, Iterable<FileObject>, Closeable, IAdaptable {

    companion object {

        val EMPTY_CHILDREN = emptyList<IFileName>()

        val EMPTY_LIST = emptyList<Any>()

        //标准文件元数据KEY

        val META_FILE_SIZE = "__FILE_SIZE"

        val META_FILE_BLOCK = "__FILE_BLOCK"

        val META_FILE_BLOCK_SIZE = "__FILE_BLOCK_SIZE"

        /**
         * 内容类型元数据，例如：doc文件、pdf文件等
         */
        val META_FILE_CONTENT_TYPE = "__FILE_CONTENT_TYPE"

        val META_FILE_INODE = "__FILE_INODE"

        /**
         * 权限元数据是一个FilePrivilige数组，第一个指定OWNER的rwx权限、第二个指定GROUP的rwx权限、第三个指定任意rwx权限
         */
        val META_OWNER_RIVILIGE = "__FILE_OWNER_PRIVILIGES"
        val META_GROUP_RIVILIGE = "__FILE_GROUP_PRIVILIGES"
        val META_ANY_RIVILIGE = "__FILE_ANY_PRIVILIGES"

        val META_OWNER = "__FILE_OWNER"

        val META_GROUP = "__FILE_GROUP"

        val META_LAST_ACCESS_TIME = "__FILE_LAST_ACCESS_TIME"

        val META_LAST_MODIFIED_CONTENT_TIME = "__FILE_LAST_MODIFIED_CONTENT_TIME"

        val META_LAST_MODIFIED_META_TIME = "__FILE_LAST_MODIFIED_META_TIME"

    }

    fun canRenameTo(newfile: FileObject): Boolean

    /**
     * 关闭并释放所有此文件对象关联的资源以及内容
     */
    override fun close()

    /**
     * 从指定源文件对象中符合选择器选定的文件拷贝
     */
    fun copyFrom(srcFile: FileObject, selector: FileSelector)

    /**
     * 创建文件对象（或文件夹），如果文件已存在，不做任何动作
     */
    fun create(isFold: Boolean = false)

    /**
     * 删除此文件对象，如果文件对象为目录，且包含子文件对象，则不做任何事，并返回false
     */
    fun delete(): Boolean

    /**
     * 删除此文件对象下符合条件的子文件
     */
    fun delete(selector: FileSelector): Int

    /**
     * 删除此文件对象及其所有子文件
     */
    fun deleteAll(): Int

    fun exists(): Boolean

    /**
     * 查找所有符合条件的子文件
     * @param selector 查找条件
     * @param depthwise 是否深度优先
     */
    fun findFiles(selector: FileSelector, depthwise: Boolean = false): Array<FileObject>

    /**
     * 此方法用于扩展文件对象的行为。行为由外部对象自定义
     */
    fun accept(visitor: FileVisitor):Unit

    /**
     * 获取指定名称的子文件对象
     */
    fun child(name: String): FileObject?

    /**
     * 获取所有子文件对象
     */
    fun children(): Array<FileObject>

    /**
     * 获取文件内容访问器，文件内容对象用于文件的实际读写
     */
    fun contentAccessor(): FileAccessor

    /**
     * 获取文件对象的文件操作接口，文件操作接口用于对文件对象的额外操作，例如：版本控制，权限控制等
     */
    fun fileOperations(): FileOperations

    /**
     * 获取文件对象的根文件系统，根文件系统类似于挂载点上挂载的各种特定的文件系统，它组织了特定文件系统的文件层次以及特定的读写、块组织方法等
     */
     fun fileSystem(): FileSystem

    /**
     * 获取封装的文件名对象，文件名对象描述了文件对象的schema模式、路径、扩展名等信息
     */
     fun name(): IFileName

    fun parent(): FileObject?

    /**
     * 获取文件对象的公共uri形式
     */
    fun publicuri(): String

    /**
     * 文件类型枚举
     */
    fun type(): FileType

    /**
     *获取文件对象url形式
     */
    fun url(): URL

    /**
     * 判断文件对象是否被关联
     */
    fun isAttached(): Boolean

    /**
     * 文件是否正在读写
     */
    fun isContentOpen(): Boolean

    /**
     * 判断文件是否为可运行文件
     */
    fun isExecutable(): Boolean

    /**
     * 判断文件是否为标准文件
     */
    fun isFile(): Boolean

    /**
     * 判断文件是否为目录
     */
    fun isFolder(): Boolean

    /**
     * 文件是否隐藏
     */
    fun isHidden(): Boolean

    /**
     * 文件是否能被读取
     */
    fun isReadable(): Boolean

    /**
     * 文件是否能被写入
     */
    fun isWriteable(): Boolean

    /**
     * 判断文件是否为链接文件
     */
    fun isLink():Boolean

    /**
     * 获取链接的文件对象
     */
    fun link():FileObject?

    /**
     * 移动文件
     */
    fun moveTo(destFile: FileObject)

    /**
     * 刷新文件对象，根据底层的文件系统，此函数将准备文件对象的同步状态
     */
    fun refresh()

    /**
     * 相对此文件解析指定名称范围下，指定名称的文件对象
     */
    fun resolveFile(name: String, scope: NameScope = NameScope.FILE_SYSTEM): FileObject

    /**
     * 设置文件可运行，ownerOnly：指定仅允许拥有者权限
     */
    fun setExecutable(executable: Boolean, ownerOnly: Boolean): Boolean

    /**
     * 设置文件可读，ownerOnly：指定仅允许拥有者权限
     */
    fun setReadable(readable: Boolean, ownerOnly: Boolean): Boolean

    /**
     * 设置文件可写，ownerOnly：指定仅允许拥有者权限
     */
    fun setWritable(writable: Boolean, ownerOnly: Boolean): Boolean

    /**
     * 获取文件对象上的扩展点
     * @param pointid 相对扩展点id，前缀会加上文件系统的名称，通常以“.”号分隔
     */
    fun getContributPoint(pointid:String):IContributPoint
}

/**
 * 文件类型的枚举
 */
enum class FileType private constructor(val fname:String, val children:Boolean, val content:Boolean, val attrs:Boolean){

    /**
     * 文件夹有子文件，有属性，但没内容
     */
    FOLDER("folder", true, false, true),

    /**
     * 标准文件
     */
    FILE("file", false, true, true),

    /**
     * 链接文件，由于链接文件指向一个文件对象，因此可以有子文件，有内容，有属性（目标文件对象的属性）
     */
    LINK("link",true, true, true),

    /**
     * 不确定
     */
    FILE_OR_FOLDER("fileOrFolder", true, true, true),

    /**
     * 不存在的枚举
     */
    IMAGINARY("imaginary", false, false, false);


    override fun toString(): String {
        return fname
    }


    fun hasChildren(): Boolean {
        return children
    }


    fun hasContent(): Boolean {
        return content
    }

    fun hasAttributes(): Boolean {
        return attrs
    }

}

/**
 * 用于相对文件对象解析文件的名称范围，会根据挂载点，进入到不同的文件系统进行解析
 */
enum class NameScope private constructor(val sname: String) {
    /**
     * 根据基文件的子文件（第一层）进行解析
     */
    CHILD("child"),

    /**
     * 根据基文件的后代进行解析
     */
    DESCENDENT("descendent"),

    /**
     * 根据子文件以及后台，包括自身进行解析
     */
    DESCENDENT_OR_SELF("descendent_or_self"),

    /**
     * 根据基文件的文件系统进行解析：绝对路径或相对路径（不进入挂载点）
     * <p>如果路径开始于分隔符（如："/"），则为绝对路径，否则为相对路径
     */
    FILE_SYSTEM("filesystem");

    override fun toString(): String {
        return this.name
    }
}
