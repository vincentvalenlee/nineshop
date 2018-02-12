package org.open.openstore.file

/**
 * 文件元信息的默认选项（常用属性，额外元属性请自定义key，且不要以双下划线开头）：
 */
enum class MetaOptions {
    /**
     * 内容可读
     */
    __READ_CONTENT,

    /**
     * 内容可写
     */
    __WRITE_CONTENT,

    /**
     *内容可随机访问
     */
    __RANDOM_ACCESS_READ,

    /**
     * 内容长度可随机设置
     */
    __RANDOM_ACCESS_SET_LENGTH,

    /**
     * 内容可随机写
     */
    __RANDOM_ACCESS_WRITE,

    /**
     * 内容可追加
     */
    __APPEND_CONTENT,

    /**
     * 支持文件属性
     */
    __ATTRIBUTES,

    /**
     * 支持文件最后修改时间
     */
    __LAST_MODIFIED,

    /**
     * 支持获取文件最后修改时间
     */
    __GET_LAST_MODIFIED,

    /**
     * 支持设置文件最后修改时间
     */
    __SET_LAST_MODIFIED_FILE,

    /**
     * 支持设置目录最后修改时间
     */
    __SET_LAST_MODIFIED_FOLDER,

    /**
     * 支持文件内容签名
     */
    __SIGNING,

    /**
     * 文件能被创建
     */
    __CREATE,

    /**
     * 文件能被删除
     */
    __DELETE,

    /**
     * 文件能被重命名
     */
    __RENAME,

    /**
     * 支持获取类型
     */
    __GET_TYPE,

    /**
     * 支持列表子文件
     */
    __LIST_CHILDREN,

    /**
     * 支持URI的文件系统，不能全局唯一标识文件
     */
    __URI,

    /**
     * 支持挂载点
     */
    __MOUNTED,

    /**
     * 支持manifest清单文件属性
     */
    __MANIFEST_ATTRIBUTES,


    /**
     * 是否支持压缩
     */
    __COMPRESS,

    /**
     * 是否支持虚拟文件（tar或zip）
     */
    __VIRTUAL,

    /**
     * 是否支持目录内容的读取
     */
    __DIRECTORY_READ_CONTENT
}