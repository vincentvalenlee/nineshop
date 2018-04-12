package org.open.openstore.file.contributors

import com.google.protobuf.ByteString
import org.open.openstore.file.*
import org.open.openstore.file.FileObject.Companion.EMPTY_CHILDREN
import org.open.openstore.file.FileObject.Companion.EMPTY_LIST
import org.open.openstore.file.internal.AbstractFileName
import org.open.openstore.file.internal.DefaultFileOperations
import org.open.openstore.file.internal.selector.Selectors
import org.slf4j.Logger
import org.slf4j.LoggerFactory
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import com.sun.org.apache.xerces.internal.util.DOMUtil.getParent
import org.open.openstore.file.FileType
import com.sun.org.apache.xerces.internal.util.DOMUtil.getParent
import org.open.openstore.file.FileObject.Companion.EMPTY_OUT

/**
 * 抽象类型的文件对象，他实现部分文件对象功能。子类实现其模板方法
 */
abstract class AbstractFileObject<AFS: AbstractFileSystem>(var fileName:AbstractFileName, val fs:AFS): FileObject {

    companion object {

        val logger:Logger = LoggerFactory.getLogger(AbstractFileObject::class.java)

        /**
         *根据选择器，深度或广度访问指定的文件集合
         */
        private fun traverse(fileInfo:DefaultFileInfo,  selector:FileSelector, depthwise:Boolean, selected: MutableList<FileObject> ) {
            val file:FileObject = fileInfo.file
            val index = selected.size

            // 如果文件是目录则继续访问
            if (file.type().hasChildren() && selector.visitFile(fileInfo)) {
                val curDepth = fileInfo.depth
                fileInfo.depth = curDepth + 1

                // 访问子文件
                file.children().forEach {
                    fileInfo.file = it
                    traverse(fileInfo, selector, depthwise, selected)
                }
                fileInfo.file = file
                fileInfo.depth = curDepth
            }

            if (selector.selectFile(fileInfo)) {
                if (depthwise) {
                    //深度优先
                    selected + file
                } else {
                    //广度优先
                    selected.add(index, file)
                }
            }
        }
    }

    private val INITIAL_LIST_SIZE = 5

//    private var fileName: AbstractFileName? = null
//    private var fs: AFS? = null

    private var accessor: FileAccessor? = null

    private var attached: Boolean = false
    private var type: FileType? = null

    private var parent: FileObject? = null

    private var children: List<IFileName> = EMPTY_CHILDREN

    private var objects: List<Any> = EMPTY_LIST

    /**
     * 文件额外操作
     */
    private var operations: FileOperations? = null


    init {
        //处理的文件引用增加
        fs.fileObjectHanded(this)
    }

     /**
     * 关联文件资源
     */
    private fun attach() {
        synchronized (fs) {
            if (!attached) {
                try {
                    // 实际执行资源关联
                    doAttach()
                    attached = true
                } catch (e: Exception) {
                    throw FileSystemException("get-type.error", e, info = arrayOf(fileName))
                }
            }
        }
    }

    /**
     * 关联文件对象到其实际的文件资源。子类可以重写此方法，执行特定的行为，如：懒加载资源等
     */
    open protected fun doAttach() {}

    override fun canRenameTo(newfile: FileObject): Boolean = newfile.fileSystem() == this

     /**
     * 子文件改变的通知，children删除或添加已改变的文件，然后调用子类重写的通知模板方法
     * @param childName 子文件名称
     * @param newType 子文件类型
     */
     open protected fun childrenChanged(childName:IFileName, newType: FileType) {
         children?.let {
             if (newType == FileType.IMAGINARY) {
                 children -= childName
             } else {
                 children += childName
             }
         }
        onChildrenChanged(childName, newType)
    }

    /**
     * 当此文件对象的子文件改变时执行的模板方法，子类可以重写（刷新此文件的子文件的缓存）
     */
    open protected fun onChildrenChanged(child: IFileName, newType: FileType) {}

    override fun close() {
         //关闭访问器
        var exc: FileSystemException? = null
        accessor?.let {
            try {
                it.close()
                accessor = null
            } catch (e:FileSystemException) {
                exc = e
            }
        }

        try {
            detach()
        } catch (e: Exception) {
            exc =  FileSystemException("close.error", e, info = arrayOf(fileName))
        }

        if (exc != null) {
            throw exc!!
        }
    }

    private fun detach() {
        synchronized(fs) {
            if (attached) {
                try {
                    doDetach()
                } finally {
                    attached = false
                    parent = null
                    removeChildrenCache()
                }
            }
        }
    }

    private fun removeChildrenCache() {
        children = EMPTY_CHILDREN
    }

    /**
     * 接触底层资源关联，当关闭时调用。子类重写
     */
    open protected fun doDetach() {}

    override operator fun compareTo(file: FileObject): Int = this.toString().compareTo(file.toString(), ignoreCase = true)

    /**
     * 拷贝其他文件
     */
    override fun copyFrom(srcFile: FileObject, selector: FileSelector) {
         if (srcFile.exists()) {
             var files =  srcFile.findFiles(selector, false)
             files.forEach {
                 val relPath = srcFile.name().getRelativeName(it.name())
                 val destFile = resolveFile(relPath, NameScope.DESCENDENT_OR_SELF)
                 //目标文件已经存在，但类型不同
                 if (destFile.exists() && destFile.type() != it.type()) {
                     destFile.deleteAll()
                 }
                 try {
                     if (it.type().hasContent()) {
                         destFile.copyContentFrom(srcFile)
                     } else if (it.type().hasChildren()) {
                         destFile.create(true) //创建文件夹
                     }
                 } catch (e: IOException) {
                     throw FileSystemException("copy-file.error", throwable = e, info = arrayOf(it, destFile))
                 }
             }
        }
    }

    private fun createFile() {
        synchronized (fs) {
            try {
                // 文件已存在
                if (exists() && !isFile()) {
                    throw FileSystemException("create-file.error", info = arrayOf(fileName))
                }
                if (!exists()) { //文件不存在,准备输出流写入
                    prepareWriting().close()
                    endOutput()
                }
            } catch (e: Exception) {
                throw FileSystemException("create-file.error", e, info = arrayOf(fileName))
            }
        }
    }

    /**
     * 当此文件的输出流关闭时调用，子类应该实现handleCreate以及onChange模板事件方法
     */
    open protected fun endOutput() {
        if (type() == FileType.IMAGINARY) {
            //文件新建
            handleCreate(FileType.FILE)
        } else {
            // 文件改变
            onChange()
        }
    }

    /**
     * 返回输入流准备将内容实际写入此文件。调用此方法时，文件以及其父目录必须存在。子类应该重写getOutputStream实际
     * 获取文件对象内部的输出流对象
     * @param append 是否以追加模式写入
     * @return 要写入文件新内容的输入流
     * @throws FileSystemException 如果发生错误则抛出异常,例如：append为true，但底层文件系统不支持追加
     */
    fun prepareWriting(append: Boolean = false):OutputStream {
        val fileAttrStore = this.getAdapter(FileAttributeStore::class) as FileAttributeStore
        if (append && !(fileAttrStore.getAttr(this, MetaOptions.__APPEND_CONTENT.name) as Boolean)) {
            //文件不支持追加模式
            throw  FileSystemException("write-append-not-supported.error", info = arrayOf(fileName))
        }

        if (type() == FileType.IMAGINARY) {
            //文件不真实存在，创建父目录
            parent()?.let {
                it.create(true)
            }
        }

        // 获取原始的输出流，子类实际实现
        return try {
             getOutputStream(append)
        } catch (e: Exception) {
            throw FileSystemException("write.error", e, info = arrayOf(fileName))
        }
    }

    /**
     * 子类需要实现的创建要写入文件内容的输出流（默认返回空输出流，不支持写）。仅仅在下面条件下调用：
     * 1.doIsWriteable返回true
     * 2.doGetType为File类型或者IMAGINARY不存在状态，且父对象存在或者为目录
     */
    open protected fun doGetOutputStream(bAppend: Boolean): OutputStream = EMPTY_OUT

    /**
     * 准备写这个文件。 确保它是一个文件或其父文件夹存在。 返回用于将文件内容写入的输出流
     * @throws FileSystemException
     */
    open protected fun getOutputStream(append: Boolean = false): OutputStream {
        val fileAttrStore = this.getAdapter(FileAttributeStore::class) as FileAttributeStore
        if (append && !(fileAttrStore.getAttr(this, MetaOptions.__APPEND_CONTENT.name) as Boolean)) {
            throw FileSystemException("write-append-not-supported.error", info= arrayOf(fileName))
        }

        if (type() === FileType.IMAGINARY) {
            parent()?.let {
                it.create(true)
            }
        }

        try {
            return doGetOutputStream(append)
        } catch (re: RuntimeException) {
            throw re
        } catch (exc: Exception) {
            throw FileSystemException("vfs.provider/write.error", exc, arrayOf(fileName))
        }

    }

    private fun createFolder() {
        synchronized (fs) {
            // 不存在时才创建目录
            if (type().hasChildren()) {
                // 已经存在，无需做任何事
                return
            }
            if (type() != FileType.IMAGINARY) {
                throw FileSystemException("create-folder-mismatched-type.error", info = arrayOf(fileName))
            }

            parent()?.let {
                it.create(true) //父目录必须存在
            }
            try {
                //实际创建目录
                doCreateFolder()

                // 更新引用
                handleCreate(FileType.FOLDER)
            } catch(e: Exception) {
                throw FileSystemException("create-folder.error", e, info = arrayOf(fileName))
            }
        }
    }

    /**
     * 当文件（目录）新建时触发的处理方法。默认将缓存信息以及通知父对象和文件系统
     */
    open protected fun handleCreate(newType: FileType) {
        synchronized(fs) {
            if (attached) { //文件已经关联实际资源
                //设置文件名封装对象类型
                changeType(newType)
                //清除子对象缓存
                removeChildrenCache()
                // 通知改变
                onChange()
            }
            // 通知父对象
            notifyParent(this.name(), newType)
            // 通知文件系统此文件已经创建
            fs.fireFileCreated(this)
        }
    }

    /**
     * 默认直接设置文件名类型，子类可重写
     */
    open protected fun changeType(fileType: FileType) {
        setFileType(fileType)
    }

    private fun setFileType(type: FileType) {
        if (type != null && type != FileType.IMAGINARY) {
            try {
                fileName.setType(type)
            } catch (e:FileSystemException) {
                throw  RuntimeException(e.message)
            }
        }
        this.type = type
    }

    /**
     * 当子对象创建或删除、改变时，通知父对象的方法
     * @param childName 改变的子对象
     * @param newType 改变的新类型
     */
    private fun notifyParent(childName: IFileName, newType:FileType) {
        if (parent == null) {
            fileName.getParent()?.let {
                parent = fs.getFileFromCache(it)
            }
        }
        parent?.let {
            parent!!.getProxyedAbstractFileObject()?.let {
                it.childrenChanged(childName, newType)
            }
        }
    }

    /**
     * 子类需要重写实现的，实际创建目录的方法（默认为空）
     */
    open protected fun doCreateFolder() {
        //do nothing
    }

    override fun create(isFold: Boolean) {
        when (isFold) {
            false -> {
                createFile()
            }
            else -> {
                createFolder()
            }
        }
    }

    /**
     * 文件类型或者内容改变时触发的事件，子类需要重写实现，默认为空
     */
    open protected fun onChange() {}

    override fun delete(selector: FileSelector): Int {
        var nuofDeleted = 0
        //定位要删除的文件
        val files:Array<FileObject>  = findFiles(selector, true)

        files.filter  {
            //不能删除具有子元素的文件
            !it.type().hasChildren() || it.children().size == 0
        }.forEach {
            if ((it as AbstractFileObject<*>).deleteSelf())
                nuofDeleted++
        }
        return nuofDeleted
    }

    override fun delete(): Boolean {
        return delete(Selectors.SELECT_SELF) > 0
    }

    override fun deleteAll(): Int {
        return delete(Selectors.SELECT_ALL)
    }

     /**
     * 实际删除此文件的方法,子类需要重写doDelete模板方法执行实际的删除
     */
    private fun deleteSelf():Boolean {
        synchronized (fs) {
            try {
                // 模板方法中子类根据需要判断是否删除权限
                doDelete()
                // 处理删除事件
                handleDelete()
            } catch (e: Exception) {
                throw FileSystemException("delete.error", e, arrayOf(fileName))
            }
            return true
        }
    }

    /**
     * 实际删除文件的方法，子类需要重写（默认不执行任何动作），仅仅在以下条件下实际删除：
     * 1. doGetType不为FileType.IMAGINARY
     * 2. doIsWriteable返回true
     */
    open protected fun doDelete() {
        //do noting
    }

    /**
     * 删除之后的事件处理
     */
    protected fun handleDelete() {
        if (attached) {
            changeType(FileType.IMAGINARY)
            removeChildrenCache()
            // 触发通知
            onChange()
        }
        //通知父对象
        notifyParent(this.name(), FileType.IMAGINARY)
        fs.fireFileDeleted(this)
    }

    /**
     * 实际创建文件访问器
     */
    open protected fun doCreateFileAssessor(): FileAccessor {
        return getFileContentInfoFactory().create(this)
    }

    /**
     * 从文件系统中获取文件访问器工厂
     */
    open protected fun getFileContentInfoFactory(): FileAccessorInfoFactory {
        return fs.getFileSystemManager().getFileAccessorInfoFactory()
    }

    override fun contentAccessor(): FileAccessor {
        synchronized (fs) {
            attach()
            if (accessor == null) {
                accessor = doCreateFileAssessor()
            }
            return accessor!!
        }
    }


    /**
     * 获取文件对象大小，抽象方法，子类必须实现
     */
    protected abstract fun doGetContentSize(): Long

    /**
     * 创建一个文件输入流用于读取文件内容。抽象方法，子类必须实现
     */
    protected abstract fun doGetInputStream(): InputStream

    /**
     * 获取文件的最后修改时间
     */
    open protected fun doGetLastModifiedTime(): Long = getAttribute(FileObject.META_LAST_MODIFIED_CONTENT_TIME) as Long? ?:0L

    /**
     * 获取文件类型（注：FileType并非文件内容类型如DOC、pdf等）
     */
    protected abstract fun doGetType(): FileType

    /**
     * 创建文件的随机访问器，子类必须实现的抽象方法
     */
    abstract protected fun doGetRandomAccessor(mode: String): FileAccessor.RandomAccessor

    /**
     * 文件是否可运行
     */
    open protected fun doIsExecutable(): Boolean =  false

    /**
     * 文件是否隐藏
     */
    open protected fun doIsHidden(): Boolean = false

    /**
     * 文件是否可读
     */
    open protected fun doIsReadable(): Boolean = true

    /**
     * 判断两个文件是否相同，例如不区分大小写的windows文件
     */
    open protected fun doIsSameFile(destFile: FileObject): Boolean = false

    /**
     * 文件是否可写
     */
    protected fun doIsWriteable(): Boolean = true

    /**
     * 获取子文件名，子类必须实现的抽象方法
     */
    protected abstract fun doListChildren(): Array<String>

    /**
     * 获取子文件，并解析为文件对象。这个方法非常耗时，因此实现需要缓冲返回的列表
     */
    protected fun doListChildrenResolved(): Array<FileObject> {
        return emptyArray()
    }

    /**
     * 实际重命名文件，子类重写，默认为空
     */
    open protected fun doRename(newFile: FileObject) {
        //do nothing
    }

    /**
     * 设置最后修改时间
     */
    open protected fun doSetLastModifiedTime(modtime: Long): Boolean {
       return try {
            setAttribute(FileObject.META_LAST_MODIFIED_CONTENT_TIME, modtime)
            true
        } catch (e: Exception){
            logger.error("set last modified time attr error!", e)
            false
        }
    }

    private fun setPrivilige(privilige:FilePrivilige):Boolean {
        return try {
            val scope = when(privilige.scope) {
                PriviligeScope.OWNER -> FileObject.META_OWNER_RIVILIGE
                PriviligeScope.GROUP -> FileObject.META_GROUP_RIVILIGE
                PriviligeScope.ANY -> FileObject.META_ANY_RIVILIGE
                else -> FileObject.META_ANY_RIVILIGE
            }
            setAttribute(scope, privilige.value)
            true
        } catch (e: Exception){
            logger.error("set privilige attr error!", e)
            false
        }
    }

    private fun getPrivilige(scope: PriviligeScope): Int {
        val scope = when(scope) {
            PriviligeScope.OWNER -> FileObject.META_OWNER_RIVILIGE
            PriviligeScope.GROUP -> FileObject.META_GROUP_RIVILIGE
            PriviligeScope.ANY -> FileObject.META_ANY_RIVILIGE
            else -> FileObject.META_ANY_RIVILIGE
        }
       return getAttribute(scope) as Int? ?: FilePrivilige.EMPTY_PRIVILIGE.value
    }

    /**
     * 设置文件可运行属性
     */
    open protected fun doSetExecutable(executable: Boolean, ownerOnly: Boolean): Boolean {
        var s: Int = if (ownerOnly) getPrivilige(PriviligeScope.OWNER) else getPrivilige(PriviligeScope.ANY)
        s = if (executable) s or FilePrivilige.PRIVILIGE_X else s and FilePrivilige.PRIVILIGE_X.inv()
        val property = if (ownerOnly) FileObject.META_OWNER_RIVILIGE else FileObject.META_ANY_RIVILIGE
        setAttribute(property, s)
        return true
    }

    /**
     * 设置文件可读属性
     */
    open protected fun doSetReadable(readable: Boolean, ownerOnly: Boolean): Boolean {
        var s: Int = if (ownerOnly) getPrivilige(PriviligeScope.OWNER) else getPrivilige(PriviligeScope.ANY)
        s = if (readable) s or FilePrivilige.PRIVILIGE_R else s and FilePrivilige.PRIVILIGE_R.inv()
        val property = if (ownerOnly) FileObject.META_OWNER_RIVILIGE else FileObject.META_ANY_RIVILIGE
        setAttribute(property, s)
        return true
    }

    /**
     * 设置文件可写属性
     */
    protected fun doSetWritable(writable: Boolean, ownerOnly: Boolean): Boolean {
        var s: Int = if (ownerOnly) getPrivilige(PriviligeScope.OWNER) else getPrivilige(PriviligeScope.ANY)
        s = if (writable) s or FilePrivilige.PRIVILIGE_W else s and FilePrivilige.PRIVILIGE_W.inv()
        val property = if (ownerOnly) FileObject.META_OWNER_RIVILIGE else FileObject.META_ANY_RIVILIGE
        setAttribute(property, s)
        return true
    }

    override fun exists(): Boolean = type() != FileType.IMAGINARY

    private fun extractNames(files: Array<FileObject>): Array<IFileName> = files.map {
        it.name()
    }.toTypedArray()

    protected fun finalize() {
        fs.fileObjectDestroyed(this)
    }

    override fun findFiles(selector: FileSelector, depthwise: Boolean): Array<FileObject> {
      return try {
           var list:MutableList<FileObject> = mutableListOf()
            if (exists()) {
                val info = DefaultFileInfo(this, this, 0)
                traverse(info, selector, depthwise, list)
            }
           list.toTypedArray()
        } catch (e:Exception) {
            throw  FileSystemException("find-files.error", e, arrayOf(fileName))
        }
    }

    private fun resolveFile(child: IFileName): FileObject {
        return fs.resolveFile(child)
    }

    override fun resolveFile(name: String, scope: NameScope): FileObject {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    private fun resolveFiles(children: Array<IFileName>): Array<FileObject> {
        if (children == null) {
            return emptyArray()
        }
        return children.map {
            resolveFile(it)
        }.toTypedArray()
    }

    override fun child(name: String): FileObject? {
        children().forEach {
           if (it.name().getBaseName() == name) {
               return resolveFile(it as IFileName)
           }
        }
        return null
    }

    override fun children(): Array<FileObject> {
        synchronized (fs) {
            val fileAttrStore = this.getAdapter(FileAttributeStore::class) as FileAttributeStore
            if (!(fileAttrStore.getAttr(this, MetaOptions.__LIST_CHILDREN.name) as Boolean)) {
                //文件不支持列举
                throw  FileSystemException("file-not-list", info = arrayOf(fileName))
            }

            attach()

            if (children != null) {
                return resolveFiles(children.toTypedArray())
            }

            try {
                val childrenObjects = doListChildrenResolved()
                children = extractNames(childrenObjects).toList()
                if (childrenObjects.size > 0) {
                    return childrenObjects
                }
                val files = doListChildren()
                if (files == null) {
                    throw FileNotFolderException(fileName)
                } else if (files.size == 0) {
                    children = EMPTY_CHILDREN
                } else {
                    var cache: MutableList<IFileName> = mutableListOf()
                    files.forEach {
                        fs.getRepository().resolveName(fileName, it, NameScope.CHILD)
                    }
                    children = cache
                }

                return resolveFiles(children.toTypedArray())
            } catch (exc:Exception) {
                throw  FileSystemException("list-children.error", exc, arrayOf(fileName))
            }

        }
    }

    override fun fileOperations(): FileOperations {
        if (this.operations == null){
            this.operations = DefaultFileOperations(this)
        }
        return this.operations!!
    }

    override fun fileSystem(): FileSystem = fs

    /**
     * 用户读取文件内容的原始输入流
     * @throws FileSystemException 如果发生错误
     */
    fun getInputStream(): InputStream = try {
             doGetInputStream()
        } catch (exc: org.open.openstore.file.FileNotFoundException) {
            throw org.open.openstore.file.FileNotFoundException(fileName, exc)
        } catch (exc: java.io.FileNotFoundException) {
            throw org.open.openstore.file.FileNotFoundException(fileName, exc)
        } catch (exc: FileSystemException) {
            throw exc
        } catch (exc: Exception) {
            throw FileSystemException("read.error", exc, arrayOf(fileName))
        }

    override fun name(): IFileName = this.fileName

    override fun publicuri(): String = this.fileName.getFriendlyURI()

    override fun parent(): FileObject? {
        if (this.compareTo(fs.getRoot()) == 0)
        {
            return fs.getParentLayer()?.parent()
        }

        synchronized (fs) {
            // Locate the parent of this file
            if (parent == null) {
                val name = fileName.getParent()
                name?.let {
                    parent = fs.resolveFile(name)
                }?: return null
            }
            return parent
        }
    }

    /**
     * 返回随机读写器
     * @param mode 通常为r-读，w-写
     */
    open fun getRandomAccessContent(mode: String): FileAccessor.RandomAccessor {
        val fileAttrStore = this.getAdapter(FileAttributeStore::class) as FileAttributeStore
        if (mode == "r") {
            if (!(fileAttrStore.getAttr(this, MetaOptions.__RANDOM_ACCESS_READ.name) as Boolean)) {
                throw FileSystemException("random-access-read-not-supported.error")
            }
            if (!isReadable()) {
                throw FileSystemException("vread-not-readable.error", info = arrayOf(fileName))
            }
        }

        if (mode == "w") {
            if (!(fileAttrStore.getAttr(this, MetaOptions.__RANDOM_ACCESS_READ.name) as Boolean)) {
                throw FileSystemException("random-access-write-not-supported.error")
            }
            if (!isWriteable()) {
                throw FileSystemException("write-read-only.error", info = arrayOf(fileName))
            }
        }

        return try {
             doGetRandomAccessor(mode)
        } catch (exc: Exception) {
            throw FileSystemException("vfs.provider/random-access.error", exc, info = arrayOf(fileName))
        }

    }

}