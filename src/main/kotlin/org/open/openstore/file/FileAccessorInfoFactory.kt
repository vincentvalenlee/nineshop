package org.open.openstore.file

/**
 * 根据指定文件对象，创建相应访问者对象的工厂类
 */
interface FileAccessorInfoFactory {
    fun create(file: FileObject): FileAccessor
}