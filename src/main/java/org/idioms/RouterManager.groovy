package org.idioms

import groovy.io.FileType
import io.vertx.core.Vertx
import io.vertx.ext.web.Router

/**
 * 使用org.idioms.controller目录下的控制器的对应方法进行处理【默认异步，如果是同步处理器，则名称中具有*SyncController】。
 * 控制器类中，可以在Action方法上添加[Http(url="....", method="Post")]\[Get]\[Put]\[Delete] Http方法
 */
class RouterManager {

    /**
     * 初始化router方法。使用org.idioms.controller目录下的控制器的对应方法进行处理【默认同步，如果是异步处理器，则使用@async元注释】
     */
    def static initRouter(Router router) {
        //将上下文加入controller中,因此控制器中能够随时访问到router上下文
        //controller.metaClass.context = context
        def config = getConfig()
        config.each {conf ->
                /*
                * 1、实例化配置中的类对象，将类对象的metaClass元类添加context方法
                * 2、根据类型（get/post),从context中解析参数
                * 3、如果为异步控制器，则调用异步闭包块，运行原始controller中的方法，并将result结果【目前支持三种结果对象，模板页、json、数据流】写入response，并end响应
                * 4、否则如果为同步控制器，使用同步闭包块调用controller方法，将result结果写入写入response，并end响应
                */
            if (conf.sync) {
                router.route(conf.path).blockingHandler({ context ->

                })
            } else {
                router.route(conf.path).handler({context ->
                    //异步的controller方法，都将在自己的回调方法中使用contex对象end响应
                })
            }

        }

    }

    /**
     * 遍历所有org.idioms.controllers目录下的控制器类，如果所有public方法上没有Http元注释，
     * 则使用/ClassName/MethodName作为path路径路由映射，方法中将注入路由的上下文参数。
     * 返回config对象数组，config对象的path属性为url，method为post等http方法类型，handler为一个闭包，
     * 此闭包将实例化class一个实例并调用相对应的方法，并将参数传入【简单参数以参数名对应【值对象】】
     */
    def getConfig() {
//        def vertx = Vertx.vertx()
        //遍历org.idioms.controllers目录下的*Controller脚本类
        def classpath = new File(this.class.getClassLoader().getResource("org/idioms/controllers"))
        def engineer = new GroovyScriptEngine(classpath)
        classpath.traverse type: FileType.FILES, nameFilter:~/.*Controller.groovy/, maxDepth:0, {
            //深度遍历*Controller.groovy的控制器脚本，加载类
            def controller = engineer.loadScriptByName(it).newInstance()


            //获取类上的所有public方法，如果使用@Http注解，则使用@Http注解构造config。如果没有@Http注解，则使用className/MethodName作为映射路径
            //默认method=get，从url中解析参数名一样的参数设置值；如果method=post（put，delete），则判断参数是否为简单类型，如果简单类型则从url中解析，如果为复杂对象类型，则从body中的json参数中解析对象


        }


    }

}
