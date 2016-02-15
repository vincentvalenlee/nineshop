package org.idioms

import groovy.io.FileType

/**
 * 使用org.idioms.controller目录下的控制器的对应方法进行处理【默认异步，如果是同步处理器，则名称中具有*SyncController】。
 * 控制器类中，可以在Action方法上添加[Http(url="....", method="Post")]\[Get]\[Put]\[Delete] Http方法
 */
class RouterManager {

    /**
     * 初始化router方法。使用org.idioms.controller目录下的控制器的对应方法进行处理【默认异步，如果是同步处理器，则使用@Sync元注释】
     */
    def static initRouter(router) {

    }

    /**
     * 遍历所有org.idioms.controllers目录下的控制器类，如果所有public方法上没有Http元注释，
     * 则使用/ClassName/MethodName作为path路径路由映射，方法中将注入路由的上下文参数。
     * 返回config对象数组，config对象的path属性为url，method为post等http方法类型，handler为一个闭包，
     * 此闭包将实例化class一个实例并调用相对应的方法，并将参数传入【简单参数以参数名对应【值对象】】
     */
    def getConfig() {

        //遍历org.idioms.controllers目录下的*Controller脚本类
        def classpath = new File(this.class.getClassLoader().getResource(""))
        classpath.traverse type: FileType.FILES, maxDepth:0, {
            //深度遍历*Controller.groovy的控制器脚本，加载类

        }


    }

}
