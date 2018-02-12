package org.open.openstore.file

/**
 * 配置对象用于配置贡献点以及贡献者。使用者可以通过贡献点获取到指定的贡献者，然后从
 * 贡献者中获取指定的配置信息，构造可运行对象等
 */
interface IContributConfig {

    /**
     * 获取指定的属性
     */
    fun getAttr(name:String): String?

    /**
     * 获取此配置的所有配置属性名
     */
    fun getAttrNames():Array<String>

    /**
     * 获取配置名称
     */
    fun getName(): String

    /**
     * 获取唯一id（可用namespace指定）
     */
    fun getId():String

    /**
     * 获取所有子配置
     */
    fun getChildren():Array<IContributConfig>

    /**
     * 获取指定名称的所有子配置
     */
    fun getChild(name:String):Array<IContributConfig>

    /**
     * 获取此配置所在的贡献者
     */
    fun getContributor():IContributor

    /**
     * 获取此配置的父对象：如果此配置直接在贡献者之下声明，则返回IContributor，否则强转成IContributConfig
     */
    fun getParent():Any?

    /**
     * Contributor将调用此方法创建attr指定的可运行对象
     */
    fun createExecutable(attr:String):Any?
}