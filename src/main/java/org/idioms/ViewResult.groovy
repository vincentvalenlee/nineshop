package org.idioms

import io.vertx.ext.web.RoutingContext

/**
 * 系统支持三种视图：JsonResult、HtmlResult、StreamResult。分别对应json数据视图、html网页视图（支持handlerbar模板）、数据流视图
 * @author vincent
 */
public interface ViewResult {
    /**
     * 运行结果，并将结果输出
     * @param context
     */
    public void ExecuteResult(RoutingContext context);
}