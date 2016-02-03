package org.idioms

/**
 * 使用org.idioms.controller目录下的控制器的对应方法进行处理【默认异步，如果是同步处理器，则名称中具有*SyncController】。
 * 控制器类中，可以在Action方法上添加[Http(url="....", method="Post")]\[Get]\[Put]\[Delete] Http方法
 */
class RouterManager {

    /**
     * 初始化router方法
     */
    def static initRouter(router) {

    }

    /**
     * 遍历所有org.idioms.controller目录下的控制器类，如果所有public方法上没有Http元注释，
     * 则使用/ClassName/MethodName作为path路径路由映射，方法中将注入路由的上下文参数。
     * 返回config对象数组，config对象的path属性为url，method为post等http方法类型，简单参数
     * 以参数名对应【值对象】
     */
    def getConfig() {

    }

}
