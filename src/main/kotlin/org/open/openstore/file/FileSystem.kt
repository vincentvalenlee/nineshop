package org.open.openstore.file

/**
 * 文件系统接口，是所有FileObject的容器。在这里可以获取文件对象所在体系的属性、根对象、以及解析指定名称的文件对象。
 * 同时，也可以挂载其他系统的文件对象到某个挂载点上。
 * <p>文件系统可以根据自身的特性，设置options选项（例如：Http，ftp，用户名密码等）
 */
interface FileSystem {
    /**
     * 获取文件系统的根文件对象,根文件对象也代表了整个文件系统的属性、特性
     */
    fun getRoot(): FileObject

    /**
     * 获取根文件的文件名，总是包含/
     */
    fun getRootName(): IFileName

    /**
     * 获取文件系统根uri形式
     */
    fun getRootURI(): String

    /**
     * 如果文件系统是分层的文件系统，则返回父级文件对象
     */
    fun getParentLayer(): FileObject?

    /**
     * 获取文件系统指定属性
     */
    fun getAttribute(attrName: String): Any?

    fun setAttribute(attrName: String, value: Any, readOnly: Boolean = false)

    fun removeAttribute(attrName: String)

    /**
     * 在文件系统中查找文件
     */
    fun resolveFile(name: IFileName): FileObject

    fun resolveFile(name: String): FileObject

    /**
     * 添加指定文件对象的监听器
     */
    fun addListener(file: FileObject, listener: FileListener)

    fun removeListener(file: FileObject, listener: FileListener)

    /**
     * 将指定文件对象挂载到指定的挂载点，当指定文件对象挂载到挂载点后，将成为此文件系统一部分
     */
    fun addMountPoint(mountpoint: String, targetFile: FileObject)

    /**
     * 删除挂载点
     */
    fun removeMountPoint(mountpoint: String)

    /**
     * 获取文件系统的选项
     */
    fun getFileSystemOptions(): FileSystemOptions

    /**
     * 获取文件系统管理器
     */
    fun getFileSystemManager(): FileSystemRepository

    /**
     * 获取最后修改时间的精度
     */
    fun getLastModTimeAccuracy(): Double
}