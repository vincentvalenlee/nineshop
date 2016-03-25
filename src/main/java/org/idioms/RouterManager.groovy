package org.idioms

import groovy.io.FileType
import io.vertx.ext.web.Router

import java.lang.reflect.Modifier

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
                * 3、使用Rxjava块调用controller方法，将result结果写入写入response，并end响应【控制器中都是同步代码，但vert.x将同步代码包装成了异步处理】
                */
            if (conf.nosync) {
                //显式使用@nosync注解，controller方法自己使用context，非阻塞的自己处理请求响应，则直接调用
                router.route(conf.method, conf.path).handler({context ->
                    //TODO:直接调用控制器对应的action方法【自己负责end响应】，通常这些方法不返回ViewResult，只返回Void
                })
            } else {
                router.route(conf.method, conf.path).blockingHandler({ context ->
                    //异步的controller方法，使用Rxjava处理，在所有方法中都会添加一个闭包，用户将处理结果返回给响应流
                    Observer<ViewResult> observer = Observers.create({ ViewResult ->
                        //TODO:将结果写入响应流的观察者实现
                    })
                    // TODO:运行控制器类的action方法，获取到ViewResult结果(如果有异常，则包装成exception result）
                    Observable.just().subscribe(observer)
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
            def contClass = engineer.loadScriptByName(it.absolutePath)
            //查找所有public公共方法
            def pubMethods = contClass.methods.grep {
                Modifier.isPublic(it.getModifiers())
            }
            pubMethods.each {
                def config = new Expando()
                //首先找到第一个复杂对象类型的参数
                def objParams = it.parameters.findResult {
                    if (!isPrimitive(it))
                        return it
                }
                if (objParams != null)
                    config.objparam = objParams

                if (it.annotations.any {
                    it.annotationType() == Http.class
                }) {
                    def httpAnno = it.annotations.find {
                        it.annotationType() == Http.class;
                    }
                    config.method = httpAnno.method
                    config.regex = httpAnno.regex
                    config.path = httpAnno.url
                    if (!httpAnno.regex) {
                        //正则表达式参数，不解析url中的:标识参数，参数将依据顺序解析,非正则表达式，解析:标识参数，将根据名字进行参数匹配
                        config.urlparams = []
                        def m = httpAnno.url =~ /\/:\w+\//
                        m.each {
                            it -= "/:"
                            it -= "/"
                            config.urlparams << it
                        }
                    }
                } else {
                    //如果没有@Http注解，则使用className/MethodName作为映射路径,
                    // 且方法中如果具有复杂对象参数，则方法为post，否则为get,且复杂对象参数只能是最后一个
                    config.regex = false;
                    if (it.parameterTypes.any() {
                        !isPrimitive(it)
                    }) {
                        config.method = Http.METHOD_POST
                    } else {
                        config.method = Http.METHOD_GET
                    }
                    def url = contClass - "Controller"
                    url += "/" + it.name
                    it.parameters.each {
                        if (isPrimitive(it)) {
                            config.urlparams << it.name
                        }
                    }
                }
            }

        }


    }

    def isPrimitive(it) {
        it.class == int.class || it.class == long.class || it.class == float.class \
        || it.class == double.class || it.class == boolean.class || it.class == char.class \
        || it.class == shot.class || it.class == byte.class
    }

}
