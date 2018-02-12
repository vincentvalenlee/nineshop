package org.open.openstore.file

import kotlin.reflect.KClass

/**
 * 可扩展接口用于为对象增加将来的“不可预见”的行为，而不用改变原有接口。通常，IAdaptable将结合FileSystemRepository一起使用
 */
interface IAdaptable {

    /**
     * 获取指定类型的扩展对象，如果不支持指定接口的扩展，则返回null
     */
    fun getAdapter(clazz: KClass<*>): Any?

}