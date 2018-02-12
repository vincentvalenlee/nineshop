package org.open.openstore.file

import java.io.InputStream
import java.util.*

/**
 * 贡献者注册表接口，平台在每个贡献者模块加载时，将贡献者注册
 */
interface IContributRegistry {

    /**
     * 在注册表中添加贡献者内容
     * @param contributContent 包含IContributPoint和IContributor配置信息的贡献者内容文件（类似eclipse的plugin.xml）
     * @param persist 是否需要将贡献内容持久化，如果为false，则为零时贡献者，当系统重启时候将丢失
     * @param token 用于检查贡献权限的标识
     */
    fun addContribution(contributContent: InputStream, persist:Boolean, token: Any, name:String? = null, translationBundle: ResourceBundle? = null ):Boolean

    /**
     * 删除给定的贡献者扩展
     */
    fun removeContributor(contributor: IContributor, token:Any): Boolean

    /**
     * 删除指定的扩展贡献点
     */
    fun removeContributPoint(point:IContributPoint, token:Any): Boolean

    /**
     * 获取指定贡献点上的所有贡献者的配置
     */
    fun getConfigs(pointId: String):Array<IContributConfig>

    fun getConfigs(nameSpace: String, pointName:String):Array<IContributConfig>

    /**
     * 获取指定贡献点指定贡献者的配置
     */
    fun getConfigs(nameSpace: String, pointName:String, extensionId:String):Array<IContributConfig>

    /**
     * 获取指定贡献者
     */
    fun getContributor(extensionId:String):IContributor

    /**
     * 获取指定贡献者
     */
    fun getContributor(extensionPointId:String, extensionId:String):IContributor

    /**
     * 根据名称空间获取贡献者
     */
    fun getContributor(nameSpace: String, pointName:String, extensionId:String):IContributor

    /**
     * 获取名称空间下所有贡献者
     */
    fun getContributors(nameSpace: String):Array<IContributor>

    /**
     * 获取指定贡献点
     */
    fun getContributPoint(extensionPointId:String): IContributPoint

    /**
     * 获取指定贡献点
     */
    fun getContributPoint(nameSpace: String, pointName:String): IContributPoint

    /**
     * 获取指定名称空间下的所有贡献点
     */
    fun getContributPoints(nameSpace: String): Array<IContributPoint>

    /**
     * 获取所有贡献点
     */
    fun getPoints():Array<IContributPoint>

    /**
     * 获取注册表中所有贡献点的名称空间
     */
    fun getNameSpaces():Array<String>

    /**
     * 停止此贡献者注册表，停止事件触发、停止缓存等
     */
    fun stop(token:Any):Unit

    /**
     * 添加贡献者监听器
     */
    fun addListener(nameSpace:String, listener: ContributRegistryListener):Unit
    fun addListener(listener: ContributRegistryListener):Unit
    fun removeListener(listener: ContributRegistryListener):Unit


}

/**
 * 监听变化的监听器
 */
interface ContributRegistryListener {
    fun changed(event: ContributRegistryChangeEvent):Unit
}

/**
 * 能获取所有贡献者改变的事件接口
 */
interface ContributRegistryChangeEvent {
    /**
     * 获取发生改变的所有change信息
     * 1.如果nameSpace为空，则获取所有
     * 2.如果pointName为空，则获取nameSpace下所有
     * 3.如果extensionId为空，则获取point下所有
     */
    fun getChanges(nameSpace: String = "", pointName:String = "", extensionId:String = ""): IContributChange
}