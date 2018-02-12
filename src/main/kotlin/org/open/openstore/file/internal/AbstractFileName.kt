package org.open.openstore.file.internal

import org.open.openstore.file.FilePlatform
import org.open.openstore.file.FileType
import org.open.openstore.file.IFileName
import org.open.openstore.file.NameScope


/**
 * 默认的文件名实现
 */
abstract class AbstractFileName(private val scheme: String, private var absPath:String?, private var type:FileType): IFileName {

    private var uri: String? = null
    private var baseName: String? = null
    private var rootUri: String? = null
    private var extension: String? = null
    private var decodedAbsPath: String? = null

    private var key: String? = null

    init {
        absPath?.let {
            if (it.isNotEmpty()) {
                if (it.length > 1 && it.endsWith("/")) {
                    this.absPath = it.substring(0, it.length - 1)
                } else {
                    this.absPath = it
                }
            } else {
                this.absPath = IFileName.ROOT_PATH
            }
        }
    }



    override fun equals(o: Any?): Boolean {
        if (this === o) {
            return true
        }
        if (o == null || javaClass != o.javaClass) {
            return false
        }

        val that = o as AbstractFileName?

        return getKey() == that!!.getKey()
    }

    override fun hashCode(): Int {
        return getKey().hashCode()
    }

    override operator fun compareTo(obj: IFileName): Int {
        return getKey().compareTo((obj as AbstractFileName)?.getKey())
    }

    override fun toString(): String {
        return getURI()
    }

    /**
     * 创建名称实例的工厂模板方法
     */
    abstract fun createName(absolutePath: String, fileType: FileType): IFileName

    /**
     * 为此文件名实例构建根URI的模板方法，root URI不以分隔符结尾
     */
    protected abstract fun appendRootUri(buffer: StringBuilder, addPassword: Boolean)

    override fun getBaseName(): String {
        if (baseName == null) {
            val idx = getPath().lastIndexOf(IFileName.SEPARATOR_CHAR)
            if (idx == -1) {
                baseName = getPath()
            } else {
                baseName = getPath().substring(idx + 1)
            }
        }
        return baseName!!
    }

    override fun getPath(): String {
        if (FilePlatform.URL_STYPE) {
            return absPath + getUriTrailer()
        }
        return absPath!!
    }

    protected fun getUriTrailer(): String {
        return if (getType().hasChildren()) "/" else ""
    }

    /**
     * 获取编码后的path
     */
    fun getPathDecoded(): String {
        if (decodedAbsPath == null) {
            decodedAbsPath = FilePlatform.UriParser.decode(getPath())
        }

        return decodedAbsPath!!
    }

    override fun getParent(): IFileName {
        val parentPath: String
        val idx = getPath().lastIndexOf(IFileName.SEPARATOR_CHAR)
        if (idx == -1 || idx == getPath().length - 1) {
            // No parent
            return IFileName.NO_NAME
        } else if (idx == 0) {
            // Root is the parent
            parentPath = IFileName.SEPARATOR
        } else {
            parentPath = getPath().substring(0, idx)
        }
        return createName(parentPath, FileType.FOLDER)
    }

    override fun getRoot(): IFileName {
        var root: IFileName = this
        while (root.getParent() != IFileName.NO_NAME) {
            root = root.getParent()
        }

        return root
    }


    override fun getScheme(): String {
        return scheme
    }


    override fun getURI(): String {
        if (uri == null) {
            uri = createURI()
        }
        return uri!!
    }

    protected fun createURI(): String {
        return createURI(false, true)
    }

    private fun getKey(): String {
        if (key == null) {
            key = getURI()
        }
        return key!!
    }

    override fun getFriendlyURI(): String {
        return createURI(false, false)
    }

    private fun createURI(useAbsolutePath: Boolean, usePassword: Boolean): String {
        val buffer = StringBuilder()
        appendRootUri(buffer, usePassword)
        buffer.append(if (useAbsolutePath) absPath else getPath())
        return buffer.toString()
    }

    /**
     * 将指定文件名转换为相对此文件名的相对名称
     */
    override fun getRelativeName(name: IFileName): String {
        val path = name.getPath()

        // Calculate the common prefix
        val basePathLen = getPath().length
        val pathLen = path.length

        // Deal with root
        if (basePathLen == 1 && pathLen == 1) {
            return "."
        } else if (basePathLen == 1) {
            return path.substring(1)
        }

        val maxlen = Math.min(basePathLen, pathLen)
        var pos = 0
        while (pos < maxlen && getPath()[pos] == path.get(pos)) {
            pos++
        }

        if (pos == basePathLen && pos == pathLen) {
            // Same names
            return "."
        } else if (pos == basePathLen && pos < pathLen && path.get(pos) == IFileName.SEPARATOR_CHAR) {
            // A descendent of the base path
            return path.substring(pos + 1)
        }

        // Strip the common prefix off the path
        val buffer = StringBuilder()
        if (pathLen > 1 && (pos < pathLen || getPath()[pos] != IFileName.SEPARATOR_CHAR)) {
            // Not a direct ancestor, need to back up
            pos = getPath().lastIndexOf(IFileName.SEPARATOR_CHAR, pos)
            buffer.append(path.substring(pos))
        }

        // Prepend a '../' for each element in the base path past the common
        // prefix
        buffer.insert(0, "..")
        pos = getPath().indexOf(IFileName.SEPARATOR_CHAR, pos + 1)
        while (pos != -1) {
            buffer.insert(0, "../")
            pos = getPath().indexOf(IFileName.SEPARATOR_CHAR, pos + 1)
        }

        return buffer.toString()
    }

    override fun getRootURI(): String {
        if (rootUri == null) {
            val buffer = StringBuilder()
            appendRootUri(buffer, true)
            buffer.append(IFileName.SEPARATOR_CHAR)
            rootUri = buffer.toString().intern()
        }
        return rootUri!!
    }

    override fun getDepth(): Int {
        val len = getPath().length
        if (len == 0 || len == 1 && getPath()[0] == IFileName.SEPARATOR_CHAR) {
            return 0
        }
        var depth = 1
        var pos = 0
        while (pos > -1 && pos < len) {
            pos = getPath().indexOf(IFileName.SEPARATOR_CHAR, pos + 1)
            depth++
        }
        return depth
    }

    override fun getExtension(): String {
        if (extension == null) {
            getBaseName()
            val pos = baseName!!.lastIndexOf('.')
            // if ((pos == -1) || (pos == baseName.length() - 1))
            // imario@ops.co.at: Review of patch from adagoubard@chello.nl
            // do not treat filenames like
            // .bashrc c:\windows\.java c:\windows\.javaws c:\windows\.jedit c:\windows\.appletviewer
            // as extension
            if (pos < 1 || pos == baseName!!.length - 1) {
                // No extension
                extension = ""
            } else {
                extension = baseName!!.substring(pos + 1).intern()
            }
        }
        return extension!!
    }

    override fun isAncestor(ancestor: IFileName): Boolean {
        if (!ancestor.getRootURI().equals(getRootURI())) {
            return false
        }
        return IFileName.checkName(ancestor.getPath(), getPath(), NameScope.DESCENDENT)
    }


    override fun isDescendent(descendent: IFileName, scope: NameScope): Boolean {
        if (!descendent.getRootURI().equals(getRootURI())) {
            return false
        }
        return IFileName.checkName(getPath(), descendent.getPath(), scope)
    }

    override fun isFile(): Boolean {
        // Use equals instead of == to avoid any class loader worries.
        return FileType.FILE.equals(this.getType())
    }

    override fun getType(): FileType {
        return type
    }

     /**
     * 文件名的状态可设置
     */
     fun setType(type:FileType) {
        if (type != FileType.FOLDER && type != FileType.FILE && type != FileType.FILE_OR_FOLDER && type != FileType.LINK) {
            throw org.open.openstore.file.FileSystemException("filename-type.error")
        }
        this.type = type
     }


}