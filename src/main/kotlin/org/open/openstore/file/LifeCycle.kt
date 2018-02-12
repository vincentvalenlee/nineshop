package org.open.openstore.file

import kotlin.reflect.KClass

/**
 * 文件系统中所有具备生命周期的对象，都应该实现此接口。生命周期接口具有init/close方法
 */
interface LifeCycle: IAdaptable {
    /**
     * 生命周期初始化时调用
     */
    fun init()

    /**
     * 结束生命周期时候调用
     */
    fun close()

    /**
     * 获取生命周期对象所在的文件系统库
     */
    fun getRepository():FileSystemRepository?
}

/**
 * 抽象生命周期对象实现，实现此声明周期的对象，都可以通过平台的适配器管理器获取指定的扩展对象
 */
abstract class AbstractLifeCycle:LifeCycle {

    lateinit private var context: FileSystemRepository

    open fun setReposotory(repository:FileSystemRepository) {
        this.context = repository
    }

    override fun init() {}

    override fun close() {}

    override fun getRepository(): FileSystemRepository {
        return this.context
    }

    override fun getAdapter(clazz: KClass<*>): Any? = getRepository().getAdapterManager().getAdapter(this, clazz)

}