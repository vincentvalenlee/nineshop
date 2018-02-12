package org.open.openstore.file.internal

import org.open.openstore.file.FilePlatform
import org.open.openstore.file.FilePlatform.CONTRIBUT_POINT_CONFIG_BUILDER
import org.open.openstore.file.FilePlatform.NAMESPACE_CORE
import org.open.openstore.file.FilePlatform.POINT_NAME_CONFIG_BUILDER
import org.open.openstore.file.FileSystem
import org.open.openstore.file.FileSystemConfigBuilder
import org.open.openstore.file.IContributConfig
import kotlin.reflect.KClass


/**
 * 文件系统配置构建器默认实现，本默认实现直接实现贡献者接口，意味着他将
 * 直接由平台core插件获取EXTENTION_POINT_CONFIG_BUILDER扩展点上的固定扩展上的固定id或元素的配置
 */
class DefaultFileSystemConfigBuilder: FileSystemConfigBuilder() {

    companion object {
        val instance: DefaultFileSystemConfigBuilder = DefaultFileSystemConfigBuilder()
        val ATTR_SYSTEM_CLASS_NAME = "systemClass"
    }

    /**
     * core模块中的构建器ID默认为：ID_CORE_FILESYSTEM_CONFIG_BUILDER
     */
    override fun getId(): String = FileSystemConfigBuilder.ID_CORE_FILESYSTEM_CONFIG_BUILDER

    override fun getName(): String = getConfig().first().getName()

    /**
     * core模块中的构建器贡献点默认为：CONTRIBUT_POINT_CONFIG_BUILDER
     */
    override fun getPoint(): String = CONTRIBUT_POINT_CONFIG_BUILDER

    override fun getConfig(): Array<IContributConfig> = FilePlatform.getContributRegistry().getConfigs(NAMESPACE_CORE, POINT_NAME_CONFIG_BUILDER, FileSystemConfigBuilder.ID_CORE_FILESYSTEM_CONFIG_BUILDER)

    override fun createExecutable(property: String): Any? = null

    /**
     * Dummy class that implements FileSystem.
     */
    internal abstract class DefaultFileSystem : FileSystem

    override fun getConfigClass(): KClass<out FileSystem> {
        return  getConfig().first().getAttr(ATTR_SYSTEM_CLASS_NAME)?.let {
            //使用当前core所在的类加载器加载核心模块的文件系统类
            this.javaClass.classLoader.loadClass(it).kotlin as KClass<out FileSystem>
        } ?: DefaultFileSystem::class
    }
}