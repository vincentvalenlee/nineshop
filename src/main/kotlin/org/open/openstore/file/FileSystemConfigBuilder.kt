package org.open.openstore.file

import kotlin.reflect.KClass

/**
 * 文件系统配置的构建器接口。每种文件系统都具有特定的配置构建器，
 * 构建器对象根据自己的策略和构建过程，构建选项。子类只需要实现：getConfigClass方法即可。
 * 子类应该从贡献点：org.xctrl.xfilesystem(core插件id）.config.builders上的获取配置
 */
abstract class FileSystemConfigBuilder:IContributor {

    companion object {
        /**
         * 默认的构建器贡献者扩展id
         */
        val ID_CORE_FILESYSTEM_CONFIG_BUILDER = "org.open.openstore.file.internal.DefaultFileSystemConfigBuilder"

    }


    /** 用于解析系统属性的默认前缀 */
    private val PREFIX = "vfs."

    /**文件系统根uri  */
    private val ROOTURI = "rootURI"

    /** 用于解析系统属性的前缀  */
    private val prefix: String


    protected constructor() {
        this.prefix = PREFIX
    }

    protected constructor(component: String) {
        this.prefix = PREFIX + component
    }

    fun setRootURI(opts: FileSystemOptions, rootURI: String) {
        setParam(opts, ROOTURI, rootURI)
    }

    fun getRootURI(opts: FileSystemOptions): String? {
        return getString(opts, ROOTURI)
    }

    protected fun setParam(opts: FileSystemOptions, name: String, value: Any) {
        opts.setOption(getConfigClass(), name, value)
    }

    protected fun getParam(opts: FileSystemOptions?, name: String): Any? {
        if (opts == null) {
            return null
        }

        return opts.getOption(getConfigClass(), name)
    }

    protected fun hasParam(opts: FileSystemOptions?, name: String): Boolean {
        return opts != null && opts.hasOption(getConfigClass(), name)
    }

    protected fun hasObject(opts: FileSystemOptions, name: String): Boolean {
        return hasParam(opts, name) || System.getProperties().containsKey(toPropertyKey(name))
    }

    protected fun getBoolean(opts: FileSystemOptions, name: String, defaultValue: Boolean = true): Boolean? {
        var value = getParam(opts, name) as Boolean?
        if (value == null) {
            val str = getProperty(name) ?: return defaultValue
            value = str.toBoolean()
        }
        return value
    }

    protected fun getByte(opts: FileSystemOptions, name: String, defaultValue: Byte = 0): Byte? {
        var value = getParam(opts, name) as Byte?
        if (value == null) {
            val str = getProperty(name) ?: return defaultValue
            value = str.toByte()
        }
        return value
    }

    protected fun getCharacter(opts: FileSystemOptions, name: String, defaultValue: Char = ' '): Char? {
        var value = getParam(opts, name) as Char?
        if (value == null) {
            val str = getProperty(name)
            if (str == null || str.length <= 0) {
                return defaultValue
            }
            value = str[0]
        }
        return value
    }

    protected fun getDouble(opts: FileSystemOptions, name: String, defaultValue: Double = 0.0): Double? {
        var value = getParam(opts, name) as Double?
        if (value == null) {
            val str = getProperty(name)
            if (str == null || str.length <= 0) {
                return defaultValue
            }
            value = str.toDouble()
        }
        return value
    }

    protected fun getFloat(opts: FileSystemOptions, name: String, defaultValue: Float = 0.0F): Float? {
        var value = getParam(opts, name) as Float?
        if (value == null) {
            val str = getProperty(name)
            if (str == null || str.length <= 0) {
                return defaultValue
            }
            value = str.toFloat()
        }
        return value
    }


    protected fun getInteger(opts: FileSystemOptions, name: String, defaultValue: Int = 0): Int? {
        var value = getParam(opts, name) as Int?
        if (value == null) {
            val str = getProperty(name) ?: return defaultValue
            value = str.toInt()
        }
        return value
    }

    protected fun getLong(opts: FileSystemOptions, name: String, defaultValue: Long = 0): Long? {
        var value = getParam(opts, name) as Long?
        if (value == null) {
            val str = getProperty(name) ?: return defaultValue
            value = str.toLong()
        }
        return value
    }

    protected fun getShort(opts: FileSystemOptions, name: String, defaultValue: Short = 0): Short? {
        var value = getParam(opts, name) as Short?
        if (value == null) {
            val str = getProperty(name) ?: return defaultValue
            value = str.toShort()
        }
        return value
    }

    protected fun getString(opts: FileSystemOptions, name: String, defaultValue: String = ""): String? {
        var value = getParam(opts, name) as String?
        if (value == null) {
            value = getProperty(name)
            if (value == null) {
                return defaultValue
            }
        }
        return value
    }

    protected abstract fun getConfigClass(): KClass<out FileSystem>


    private fun toPropertyKey(name: String): String {
        return this.prefix + name
    }

    /**
     * 首先从扩展中<builder>节点上获取，否则从系统属性中获取
     */
    protected fun getProperty(name: String): String? {
//        FileSystemRepository().getSystemContributPoint("org.xctrl.xfilesystem.config.builders")
//                .getContributor("org.xctrl.xfilesystem.config.builders.${prefix}")
        return this.getConfig().firstOrNull {
                    it.getName() == "builder"
        }?.let {
            it.getAttr(name)
        }?: System.getProperty(toPropertyKey(name))
    }
}