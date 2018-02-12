package org.open.openstore.file

import kotlin.reflect.KClass

/**
 * 适配器工厂
 */
interface IAdapterFactory {

    /**
     * 获取注册到指定对象上的所有扩展类
     */
    fun getAdapterList(): KClass<*>

    /**
     * 获取指定对象上的指定类型的扩展对象
     */
    fun getAdapter(adaptableObject:Any, adapterType: KClass<*>)

}

/**
 * 适配器管理接口，用于工厂的注册
 */
interface IAdapterManager {

    /**
     * 注册扩展工厂
     */
    fun register(factory:IAdapterFactory, adaptable:KClass<*>)

    /**
     * 注销
     */
    fun unregister(factory: IAdapterFactory, adaptable:KClass<*>? = null)

    /**
     * 解析扩展对象的名称，用于getAdapter
     */
    fun resolveAdapterTypes(adaptableClass:KClass<*>): Array<String>

    /**
     * 获取指定扩展对象，如果没加载则返回null
     */
    fun getAdapter(adaptable: Any, adapterType:KClass<*>): Any?

    /**
     * 根据注册的名称，获取指定扩展对象
     */
    fun getAdapter(adaptable: Any, adapterTypeName:String): Any?

    /**
     * 是否具有指定扩展适配器
     */
    fun hasAdapter(adaptable:Any, adapterTypeName:String): Boolean

}

