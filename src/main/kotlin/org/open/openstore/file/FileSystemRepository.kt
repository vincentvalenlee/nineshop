package org.open.openstore.file

import org.apache.commons.logging.Log
import java.io.File
import java.lang.reflect.Constructor
import java.net.URI
import java.net.URL
import java.net.URLStreamHandlerFactory
import kotlin.reflect.KClass

/**
 * 文件系统仓库对象，负责管理所有注册的文件系统。他可以通过名称调用resolveFile方法从一个文件系统中定位一个文件对象（例如：http://, ftp:/）
 * <h2><a name="naming">文件名</a></h2>
 * 可以识别以下各种类型的名称：
 * <ul>
 * <li>
 *     绝对URI，开始于scheme，例如：file:/c:/somefile，ftp://somewhere.org/somefile
 * </li>
 * <li>
 *     绝对的本地文件系统：/home/someuser/a-file，c:\dir\somefile.html
 * </li>
 * <li>
 *     相对路径。例如： ../somefile，somedir/file.txt。文件系统仓库基于base文件来解析相对路径。
 *  </li>
 * </ul>
 * <p>
 *     仓库对象通常使用FileProvider提供者对象解析文件系统，同时也提供文件系统的缓存、缓存策略的配置。
 */
interface FileSystemRepository {
    companion object {
        operator fun invoke():FileSystemRepository {
            TODO()
        }
    }
    /**
     * 获取用于解析相对路径的基文件对象
     */
    fun getBaseFile(): FileObject

    fun resolveFile(name: String): FileObject

    fun resolveFile(name: String, fileSystemOptions: FileSystemOptions): FileObject

    fun resolveFile(baseFile: FileObject, name: String): FileObject

    fun resolveFile(baseFile: File, name: String): FileObject

    fun resolveName(root: IFileName, name: String): IFileName

    fun resolveName(root: IFileName, name: String, scope: NameScope): IFileName

    fun toFileObject(file: File): FileObject

    /**
     * 创建一个分层的文件系统。一个分层的文件系统类似zip和tar文件
     * @param provider 使用的文件系统提供者
     * @param file 用于创建文件系统的文件对象
     * @return 新文件系统的根文件对象
     */
    fun createFileSystem(provider: String, file: FileObject): FileObject

    fun closeFileSystem(filesystem: FileSystem)

    fun createFileSystem(file: FileObject): FileObject

    /**
     * 在挂载点上创建一个空的虚拟文件系统
     */
    fun createVirtualFileSystem(rootUri: String): FileObject

    /**
     * 在指定的根文件对象上创建文件系统
     */
    fun createVirtualFileSystem(rootFile: FileObject): FileObject

    /**
     * 创建一个URL流处理器工厂以便允许使用此文件系统仓库查找URL
     */
    fun getURLStreamHandlerFactory(): URLStreamHandlerFactory


    fun canCreateFileSystem(file: FileObject): Boolean

//    /**
//     * 获取文件缓存对象
//     */
//    fun getFilesCache(): FilesCache

    /**
     * 获取缓存提供者对象，缓存提供者可以获取缓存以及缓存的策略配置
     */
    fun getCacheProvider(): CacheProvider

    /**
     * 获取文件对象的修饰器，可以通过扩展点获取贡献者对象配置构造
     */
    fun getFileObjectDecorator(): KClass<*>?

    /**
     * 获取文件访问者对象，以及文件内容信息，例如mime-type
     */
    fun getFileAccessorInfoFactory(): FileAccessorInfoFactory

    fun hasContributor(scheme: String): Boolean

    fun getSchemes(): Array<String>

    fun setLogger(log: Log)

    /**
     * 获取文件系统配置构建器
     */
    fun getFileSystemConfigBuilder(scheme: String): FileSystemConfigBuilder

    fun resolveURI(uri: String): IFileName

    fun addOperationFactory(scheme: String, factory: FileOperationFactory)

    fun addOperationFactory(schemes: Array<String>, factory: FileOperationFactory)

    fun getOperationFactory(scheme: String): Array<FileOperationFactory>

    fun resolveFile(uri: URI): FileObject

    fun resolveFile(url: URL): FileObject

    /**
     * 获取平台级的贡献点（其他贡献点，由各自文件对象的文件系统清单自定义）
     */
    fun getSystemContributPoint(pointid:String):IContributPoint

    /**
     * 获取适配器管理器，用于适配工厂的注册
     */
    fun getAdapterManager():IAdapterManager
}