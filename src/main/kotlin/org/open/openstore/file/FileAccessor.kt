package org.open.openstore.file

import java.io.*

/**
 * 文件访问器对象用于描述对文件内容的访问，通过访问器对象进行文件的数据实际读写。此接口将文件内容相关的信息，如大小、属性、编码、修改时间、输入输出流
 * 等针对文件内容的访问方式从文件对象中剥离出来，专门负责文件内容的读写，例如文件随机内容的seek定位等
 */
interface FileAccessor : Closeable {
    /**
     * 拥有的文件对象
     */
    fun getFile(): FileObject

    /**
     * 文件字节大小
     */
    fun getSize(): Long

    /**
     * 文件最后修改时间
     */
    fun getLastModifiedTime(): Long

    /**
     * 设置最后修改时间
     */
    fun setLastModifiedTime(modTime: Long)

    /**
     * 获取文件属性
     */
    fun hasAttribute(attrName: String): Boolean

    /**
     * 获取文件所有属性
     */
    fun getAttributes(): Map<String, Any>

    /**
     * 获取文件所有属性名
     */
    fun getAttributeNames(): Array<String>

    /**
     * 获取指定属性值
     */
    fun getAttribute(attrName: String): Any

    /**
     * 设置指定属性
     */
    fun setAttribute(attrName: String, value: Any)

    /**
     * 删除指定属性
     */
    fun removeAttribute(attrName: String)


    /**
     * 获取文件输入流
     */
    fun getInputStream(): InputStream


    /**
     * 获取随机内容的读写器
     * @param mode r为随机读，rw为随机读写
     */
    fun getRandomAccessor(mode: String = "r"): RandomAccessor

    /**
     * 获取最佳模式的写入流
     */
    fun getOutputStream(bAppend: Boolean = false): OutputStream


    override fun close()

    /**
     * 获取文件内容编码
     */
    fun getEncoding(): String

    /**
     * 获取文件内容类型
     */
    fun getType(): String

    /**
     * 文件流是否打开
     */
    fun isOpen(): Boolean

    /**
     * 将此文件内容写入到指定的内容
     */
    fun write(output: FileAccessor): Long

    /**
     * 将此文件内容写入到指定的文件对象中
     */
    fun write(file: FileObject): Long

    /**
     * 将此文件内容写入指定的输出流
     */
    fun write(output: OutputStream): Long

    /**
     * 将此文件内容按照bufferSize分块写入指定的输出流
     */
    fun write(output: OutputStream, bufferSize: Int): Long

    /**
     * 提供随机内容读写的方法，能够随机定位指定位置进行文件的读写操作
     */
    interface RandomAccessor : DataOutput, DataInput {

        fun close()

        /**
         * 获取文件的指针位置
         */
        fun getFilePointer(): Long

        /**
         * 获取文件随机定位后的输入流
         */
        fun getInputStream(): InputStream

        /**
         * 获取随机部分文件大小
         */
        fun length(): Long

        /**
         * 设置文件指针的位置
         */
        fun seek(pos: Long)

        /**
         * 设置文件随机内容部分大小
         */
        fun setLength(newLength: Long)
    }
}