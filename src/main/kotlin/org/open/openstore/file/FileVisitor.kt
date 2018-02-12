package org.open.openstore.file

/**
 * 用于遍历FileObject对象的访问者接口，可以用于在遍历过程中选择遍历的文件对象
 */
interface FileVisitor {

    /**
     * 访问指定文件对象，如果返回true则继续访问
     */
    fun visitFile(file:FileInfo): Boolean

}

/**
 * 根据自己实现的规则，选择指定文件对象的选择器
 */
interface FileSelector:FileVisitor {

    /**
     * 决定指定file是否被选择,此方法将在visitFile返回true时，调用
     */
    fun selectFile(file: FileInfo): Boolean
}


interface FileInfo {
    /**
     * 基目录
     */
    var baseFolder: FileObject?

    /**
     * 当前遍历的文件对象
     */
    var file: FileObject

    /**
     * 相对于基目录的当前访问深度
     */
    var depth: Int
}

data class DefaultFileInfo(override var baseFolder: FileObject?, override var file: FileObject, override var depth:Int): FileInfo