package org.idioms

import java.lang.annotation.ElementType
import java.lang.annotation.Retention
import java.lang.annotation.RetentionPolicy
import java.lang.annotation.Target

/**
 * [@Http(method=get/post/put/delete， url=“请求地址/:标识参数”]
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Http {

    public final static String METHOD_GET = "get";

    public final static String METHOD_POST = "post";

    public final static String METHOD_PUT = "put";

    public final static String METHOD_DELETE = "delete";
    /**
     * 指定http请求方法：get、post、put、delete
     */
    public String method() default "get";

    /**
     * 标识请求的路由匹配地址，默认为空 {标识对应的命名参数}
     * @return
     */
    public String url() default "";

    /**
     * 是否正则匹配的路径
     * @return
     */
    public boolean regex() default false;

}