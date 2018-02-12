package org.open.openstore.file

/**
 * 贡献点接口，提供了一种扩展点的机制，他能够让IContributor贡献者对象接入自身，进行扩展功能的定义
 */
interface IContributPoint {

    /**
     * 全局唯一id
     */
    fun getId():String

    /**
     * 获取所在名称空间：名称空间+"." + name为唯一id
     */
    fun getNamespace():String

    /**
     * 显示名称
     */
    fun getName():String

    /**
     * 获取贡献点上所有贡献者上的配置
     */
    fun getConfig():Array<IContributConfig>

    /**
     * 获取指定的贡献者
     */
    fun getContributor(contributor: String): IContributor

    /**
     * 获取此端点下所有的贡献者
     */
    fun contributors():Array<IContributor>

}