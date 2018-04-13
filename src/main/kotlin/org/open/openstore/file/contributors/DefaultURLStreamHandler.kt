package org.open.openstore.file.contributors

import org.open.openstore.file.*
import java.net.URL
import java.net.URLConnection
import java.net.URLStreamHandler
import org.open.openstore.file.FilePlatform.UriParser

import java.io.InputStream
import java.io.OutputStream

/**
 * 默认的用于文件系统的url处理器
 */
class DefaultURLStreamHandler(val context: FileSystemRepository, val fileSystemOptions: FileSystemOptions?): URLStreamHandler() {

    constructor(context: FileSystemRepository):this(context, null) {}


    override fun openConnection(url: URL?): URLConnection {
        val entry = context.resolveFile(url!!.toExternalForm(), fileSystemOptions!!)
        return DefaultURLConnection(url, entry.contentAccessor())
    }

    override fun parseURL(u: URL?, spec: String?, start: Int, limit: Int) {
        try {
            val old = context.resolveFile(u!!.toExternalForm(), fileSystemOptions!!)

            val newURL: FileObject = if (start > 0 && spec!![start - 1] === ':') {
                context.resolveFile(old, spec!!, fileSystemOptions)
            } else {
                if (old.isFile() && old.parent() != null) {
                    // 相对解析
                    old.parent()!!.resolveFile(spec!!)
                } else {
                    old.resolveFile(spec!!)
                }
            }

            val url = newURL.name().getURI()
            val filePart = StringBuilder()
            val protocolPart = UriParser.extractScheme(url, filePart)

            setURL(u, protocolPart, "", -1, null, null, filePart.toString(), null, null)
        } catch (fse: FileSystemException) {
            throw RuntimeException(fse.message)
        }

    }

    override fun toExternalForm(u: URL?): String {
        return u!!.protocol + ":" + u.file
    }

}

/**
 * 默认的能够工作在大部分文件系统下的URL连接对象
 */
class DefaultURLConnection(url:URL, val content: FileAccessor):URLConnection(url) {
    override fun connect() {
        connected = true
    }

    override fun getInputStream(): InputStream =  content.getInputStream()

    override fun getOutputStream(): OutputStream = content.getOutputStream()

    override fun getLastModified(): Long = try {
         content.getLastModifiedTime()
    } catch (ignored: FileSystemException) {
         -1
    }

    override fun getContentLength(): Int =  try {
        content.getSize() as Int
    } catch (fse: FileSystemException) {
        -1
    }

    override fun getContentType(): String = try {
        content.getType()
    } catch (e: FileSystemException) {
        throw RuntimeException(e.message)
    }

    override fun getContentEncoding(): String = try {
        content.getEncoding()
    } catch (e: FileSystemException) {
        throw RuntimeException(e.message)
    }

}