package org.open.openstore.file

/**
 * 贡献者是从贡献点接口中扩展的具体扩展对象。
 */
interface IContributor {

    /**
     * 获取全局唯一id
     */
    fun getId():String

    /**
     * 获取贡献者元素名
     */
    fun getName():String

    /**
     * 获取贡献点id
     */
    fun getPoint():String

    /**
     * 获取此贡献点上的所有贡献者
     */
    fun getConfig():Array<IContributConfig>

    /**
     * 创建指定名称的可运行对象。指定的属性，必须在IConfig中进行配置。
     * 通常可运行的属性名称为：class(根据类名创建一个实例）、script（运行脚本）、messageExchange（消息容器）等
     */
    fun createExecutable(property: String): Any?
}