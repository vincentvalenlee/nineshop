/**
 * nineshop商城系统的服务器请求类，这里初始化vert.x的上下文，启动http服务，监听端口。加载扩展模块
 */

import io.vertx.groovy.ext.web.handler.StaticHandler
import org.idioms.RouterManager

def server = vertx.createHttpServer()
def router = Router.router(vertx)

//处理所有css、js静态文件
router.route("/static/*").handler(StaticHandler.create())

//使用org.idioms.controller目录下的控制器的对应方法进行处理【默认异步，如果是同步处理器，则名称中具有*SyncController】
RouterManager.initRouter(router)

//监听8080端口
server.requestHandler(router.&accept).listen(8080)