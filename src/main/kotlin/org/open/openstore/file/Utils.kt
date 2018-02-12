package org.open.openstore.file

import java.io.OutputStream
import org.open.openstore.file.contributors.AbstractFileObject
import org.open.openstore.file.internal.ProxyFile


/**
 * 提供各种扩展方法的实现
 */

/**
 * 获取文件内容的方便方法（小文件字节）
 */
fun FileObject.getContent(): ByteArray {
    val assessor:FileAccessor = this.contentAccessor()
    val size = assessor.getSize().toInt()
    val input = assessor.getInputStream()
    input.use {
        var read = 0
        var buff = ByteArray(size)
        var pos = 0
        while(pos < size && read >= 0) {
            read = it.read(buff, pos, size - pos)
            pos += read
        }
        return buff
    }
}

/**
 * 将文件内容写入到流中
 */
fun FileObject.writeContent(output: OutputStream) {
    this.contentAccessor().write(output)
}

/**
 * 从指定的文件对象中复制内容
 */
fun FileObject.copyContentFrom(src:FileObject) {
    src.contentAccessor().write(this)
}

/**
 * 获取内部被代理的实际的对象
 */
fun FileObject.getProxyedAbstractFileObject():AbstractFileObject<*>? {
    var searchObject = this
    while (searchObject is ProxyFile) {
        searchObject = searchObject.proxied
    }
    return searchObject as AbstractFileObject<*>
}
