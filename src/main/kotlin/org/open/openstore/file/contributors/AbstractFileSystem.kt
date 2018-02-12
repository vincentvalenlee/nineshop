package org.open.openstore.file.contributors

import org.open.openstore.file.*
import org.open.openstore.file.internal.DefaultFileSystemConfigBuilder
import java.util.concurrent.atomic.AtomicInteger
import java.util.concurrent.atomic.AtomicLong
import org.slf4j.LoggerFactory
import org.open.openstore.file.internal.AbstractFileName
import org.open.openstore.file.FilesCache
import java.lang.reflect.InvocationTargetException
import org.open.openstore.file.FileSystemOptions
import kotlin.reflect.full.primaryConstructor
import java.util.ArrayList
import org.open.openstore.file.FileListener
import org.open.openstore.file.internal.events.*


/**
 * 文件系统抽象实现
 */
abstract class AbstractFileSystem: AbstractLifeCycle, FileSystem {

    companion object {
        private val logger = LoggerFactory.getLogger(AbstractFileSystem::class.java)
    }


    private val listenerMap = mutableMapOf<IFileName, List<FileListener>>()

    /**
     * 记录文件系统中持有的文件对象
     */
    private val useCount = AtomicLong(0)

    private var cacheKey: FileSystemKey? = null

    /**
     * 文件系统打开的流数目
     */
    private val openStreams = AtomicInteger(0)

    private val  rootname: IFileName

    private var parentlayer: FileObject?

    private val options: FileSystemOptions

    private val rooturi:String


    protected constructor(rootname: IFileName, parentlayer: FileObject, options: FileSystemOptions) {
        this.rootname = rootname
        this.parentlayer = parentlayer
        this.options = options
        val builder = DefaultFileSystemConfigBuilder.instance
        var uri = builder.getRootURI(options)
        if (uri == null) {
            uri = rootname.getURI()
        }
        this.rooturi = uri!!
    }

    override fun init() {
        synchronized(this) {
            doInit()
        }
    }

    /**
     * 关闭此文件系统，子类应该实现doClose模板方法，用以实际关闭底层文件系统的连接
     */
    override fun close() {
        synchronized(this) {
            doClose()
        }
        parentlayer = null
    }

    /**
     * 实际关闭底层文件系统的连接，默认空实现，子类可以重写
     */
    open protected fun doClose() { }

    /**
     * 实际初始化底层文件系统的方法，默认空实现，子类可以重写
     */
    open protected fun doInit() {}

    /**
     * 实际创建一个文件对象。此方法不会使用缓存，直接创建文件
     */
    protected abstract fun createFile(name: AbstractFileName): FileObject

    override fun getRootName(): IFileName = rootname

    override fun getRootURI(): String = rooturi

    /**
     * 将文件放入缓存，如果缓存已存在，则覆盖
     */
    open protected fun putFileToCache(file: FileObject) = getCache().add(file, true)

    private fun getCache(): FilesCache  = getRepository().getCacheProvider().getCache()

    open fun getFileFromCache(name: IFileName): FileObject = getCache().getFile(this, name)

    open fun removeFileFromCache(name: IFileName) = getCache().removeFile(this, name)

    /**
     * 获取文件系统中指定的属性对象
     */
    override fun getAttribute(attrName: String): Any? {
       val attrStore: FileAttributeStore? =  getAdapter(FileAttributeStore::class) as FileAttributeStore?
       return attrStore?.let {
           it.getAttr(this.getRoot(), attrName)
       }
    }

    /**
     * 设置文件系统的属性
     */
    override fun setAttribute(attrName: String, value: Any, readOnly:Boolean) {
        val attrStore: FileAttributeStore? =  getAdapter(FileAttributeStore::class) as FileAttributeStore?
        attrStore?.let {
            it.setAttr(this.getRoot(), attrName, readOnly)
        }
    }

    override fun getParentLayer(): FileObject? = parentlayer

    /**
     * 获取文件系统的根文件对象
     */
    override fun getRoot(): FileObject {
        return resolveFile(rootname)
    }

    /**
     * 在文件系统中查找指定名称的文件
     */
    override fun resolveFile(nameStr: String): FileObject {
        val name = getFileSystemManager().resolveName(rootname, nameStr)
        return resolveFile(name)
    }

    override fun resolveFile(name: IFileName): FileObject {
        return resolveFile(name, true)
    }

    @Synchronized
    private fun resolveFile(name: IFileName, useCache: Boolean): FileObject {
        if (!rootname.getRootURI().equals(name.getRootURI())) {
            throw FileSystemException("mismatched-fs-for-name.error", info = arrayOf(name, rootname, name.getRootURI()))
        }

        var file: FileObject? = if (useCache) {
            getFileFromCache(name)
        } else {
            null
        }

        if (file == null) {
            try {
                file = createFile(name as AbstractFileName)
            } catch (e: Exception) {
                throw FileSystemException("resolve-file.error", e, info = arrayOf(name))
            }

            file = decorateFileObject(file)

            if (useCache) {
                putFileToCache(file)
            }
        }
        //触发
        fireFileResolved(file)
        return file
    }

    /**
     * 修饰原始的文件对象，例如：在每个文件对象方法调用之后，触发sync事件（可能执行refresh方法刷新）
     */
    open protected fun decorateFileObject(file: FileObject): FileObject {
        if (getFileSystemManager().getFileObjectDecorator() != null) {
            try {
               return getFileSystemManager().getFileObjectDecorator()!!.primaryConstructor!!.call(file) as FileObject
            } catch (e: InstantiationException) {
                throw FileSystemException("invalid-decorator.error", e)
            } catch (e: IllegalAccessException) {
                throw FileSystemException("invalid-decorator.error", e)
            } catch (e: InvocationTargetException) {
                throw FileSystemException("invalid-decorator.error", e)
            }

        }
        return file
    }

    override fun getFileSystemOptions(): FileSystemOptions = options

    override fun getFileSystemManager(): FileSystemRepository = getRepository()

    /**
     * 最后修改时间的精度：单位秒；小数点3位为毫秒
     */
    override fun getLastModTimeAccuracy(): Double = 0.0

    override fun addMountPoint(mountpoint: String, targetFile: FileObject) {
        //do noting
    }

    override fun removeMountPoint(mountpoint: String) {
        //do noting
    }

    override fun addListener(file: FileObject, listener: FileListener) {
        synchronized(listenerMap) {
            var listeners = listenerMap[file.name()]
            if (listeners == null) {
                listeners = ArrayList<FileListener>()
                listenerMap + (file.name() to listeners)
            }
            listeners + listener
        }
    }

    override fun removeListener(file: FileObject, listener: FileListener) {
        synchronized(listenerMap) {
            listenerMap[file.name()]?.let {
                it - listener
                if (it.isEmpty()) {
                    listenerMap - file.name()
                }
            }
        }
    }

    /**
     * 触发文件创建事件
     */
    fun fireFileCreated(file: FileObject) {
        fireEvent(CreateEvent(file))
    }

    /**
     * 触发文件删除事件
     */
    fun fireFileDeleted(file: FileObject) {
        fireEvent(DeleteEvent(file))
    }

    /**
     * 触发文件改变事件
     */
    fun fireFileChanged(file: FileObject) {
        fireEvent(ChangedEvent(file))
    }

    /**
     * 触发文件resolved事件
     */
    fun fireFileResolved(file:FileObject) {
        fireEvent(ResolvedEvent(file))
    }

    /**
     * 是否释放
     */
    open fun isReleaseable(): Boolean {
        return useCount.get() < 1
    }

    open fun freeResources() {}

    /**
     * 触发文件事件
     */
    private fun fireEvent(event: AbstractEvent) {
        var fileListeners: Array<FileListener>? = null
        val file = event.file

        synchronized(listenerMap) {
            val listeners = listenerMap[file.name()]
            if (listeners != null) {
                fileListeners = listeners.toTypedArray()
            }
        }

        if (fileListeners != null) {
            fileListeners!!.forEach {
                try {
                    event.doAction(it, event)
                } catch (e: Exception) {
                    logger.warn("执行文件事件错误！${event.name}")
                }

            }
        }
    }

    /**
     * 文件引用增加
     */
    open fun fileObjectHanded(fileObject: FileObject) = useCount.incrementAndGet()

    open fun fileObjectDestroyed(fileObject: FileObject) = useCount.decrementAndGet()

    open fun setCacheKey(cacheKey: FileSystemKey) {
        this.cacheKey = cacheKey
    }

    open fun getCacheKey(): FileSystemKey? = this.cacheKey

    open fun streamOpened() = openStreams.incrementAndGet()

    open fun streamClosed() {
        if (openStreams.decrementAndGet() === 0) {
            notifyAllStreamsClosed()
        }
    }

    /**
     * 当文件对象关闭实际的流时调用，默认为空，子类可以重写
     */
    open protected fun notifyAllStreamsClosed() {}

    /**
     * 检测文件系统是否具有流
     */
    open fun isOpen(): Boolean = openStreams.get() > 0


}