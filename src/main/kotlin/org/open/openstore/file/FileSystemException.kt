package org.open.openstore.file

import java.io.IOException
import java.util.regex.Pattern

/**
 * 文件系统异常封装
 */
open class FileSystemException:IOException {

    private val URL_PATTERN = Pattern.compile("[a-z]+://.*")

    private val PASSWORD_PATTERN = Pattern.compile(":(?:[^/]+)@")

    protected val info: Array<String>

    protected val code: String

    constructor(code: String, throwable: Throwable? = null,  info: Array<Any> = emptyArray()):super(throwable) {
        this.code = code
        if (info == null) {
            this.info = arrayOf<String>()
        } else {
            this.info = Array(info.size){ "" }
            for (i in info.indices) {
                var value = info[i].toString()
                val urlMatcher = URL_PATTERN.matcher(value)
                if (urlMatcher.find()) {
                    val pwdMatcher = PASSWORD_PATTERN.matcher(value)
                    value = pwdMatcher.replaceFirst(":***@")
                }
                this.info[i] = value
            }
        }
    }

    /**
     * 默认返回code，子类可以重写此方法，从code注册表中获取code映射的异常message
     */
    override val message: String?
        get() = this.code
}