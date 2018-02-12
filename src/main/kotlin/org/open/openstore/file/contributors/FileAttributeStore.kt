package org.open.openstore.file.contributors

import org.open.openstore.file.FileObject
import org.open.openstore.file.FileSystemException

/**
 * 负责存储文件对象、文件系统属性的存储器接口
 */
interface FileAttributeStore {

    /**
     * 获取指定文件对象上的属性值
     */
    fun getAttr(file: FileObject, attr:String):Any?

    /**
     * 获取所有文件对象上的属性（包括元信息和自定义属性）
     */
    fun getAttrs(file:FileObject): Map<String, Any>

    /**
     * 设置属性，如果属性存在，且为只读，则抛出异常FileTypeHasNoContentException
     */
    fun setAttr(file: FileObject, attr:String, value:Any, readOnly: Boolean = false)

    /**
     * 删除文件属性，如果attr为空，则全部删除
     */
    fun rmAttr(file:FileObject, attr:String = "")

}

//文件对象获取属性的扩展

fun FileObject.getAttributes(): Map<String, Any> {
    val attrStore: FileAttributeStore? =  getAdapter(FileAttributeStore::class) as FileAttributeStore?
    return attrStore?.let {
        it.getAttrs(this)
    } ?: emptyMap()
}

fun FileObject.getAttribute(attr:String):Any? {
    val attrStore: FileAttributeStore? =  getAdapter(FileAttributeStore::class) as FileAttributeStore?
    return attrStore?.let {
        it.getAttr(this, attr)
    }
}

/**
 * 删除文件的属性
 */
fun FileObject.removeAttribute(attrName: String) {
    val attrStore: FileAttributeStore? =  getAdapter(FileAttributeStore::class) as FileAttributeStore?
    attrStore?.let {
        it.rmAttr(this, attrName)
    }
}

/**
 * 设置属性值，如果属性只读，则抛出FileSystemException异常
 */
fun FileObject.setAttribute(attrName: String, value: Any, readonly:Boolean = false) {
    val attrStore: FileAttributeStore? =  getAdapter(FileAttributeStore::class) as FileAttributeStore?
    attrStore?.let {
        it.setAttr(this, attrName, value, readonly)
    }
}

/**
 * 文件权限的范围：拥有着、同组、所有
 */
enum class PriviligeScope {
    OWNER,
    GROUP,
    ANY,
    NONE
}

data class FilePrivilige(val scope: PriviligeScope, val value: Int) {
    companion object {
        val PRIVILIGE_R:Int = 0x100 //可读权限
        val PRIVILIGE_W:Int = 0x010 //可写权限
        val PRIVILIGE_X:Int = 0x001 //可运行权限
        val EMPTY_PRIVILIGE:FilePrivilige = FilePrivilige(PriviligeScope.NONE, 0x000)
    }
}

/**
 * 文件属性异常
 */
class FileTypeHasNoContentException(info: Any, throwable: Throwable?): FileSystemException(code = "file-attr.error",  throwable = throwable, info = arrayOf(info)) {
    constructor(info: Any): this(info, null) {}
}