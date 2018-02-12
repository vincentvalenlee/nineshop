package org.open.openstore.file

import java.util.*
import kotlin.reflect.KClass

/**
 * 各种文件系统的选项配置。每种文件系统，都可以拥有自己的特定的选项，这些选项将影响所有从文件系统中resovle的文件对象
 */
class FileSystemOptions:Cloneable {

    companion object {
       val EMPTY_OPTIONS = FileSystemOptions()
    }

    private var options: MutableMap<FileSystemOptionKey, Any>

    constructor():this(TreeMap<FileSystemOptionKey, Any>()) {}

    constructor(options: MutableMap<FileSystemOptionKey, Any>) {
        this.options = options
    }

    class FileSystemOptionKey constructor(
            private val fileSystemClass: KClass<out FileSystem>,
            private val name: String) : Comparable<FileSystemOptionKey> {

        override fun compareTo(o: FileSystemOptionKey): Int {
            val ret = fileSystemClass.qualifiedName!!.compareTo(o.fileSystemClass.qualifiedName!!)
            if (ret != 0) {
                return ret
            }
            return name.compareTo(o.name)
        }

        override fun equals(o: Any?): Boolean {
            if (this === o) {
                return true
            }
            if (o == null || javaClass != o.javaClass) {
                return false
            }

            val that = o as FileSystemOptionKey?

            if (fileSystemClass != that!!.fileSystemClass) {
                return false
            }
            if (name != that.name) {
                return false
            }

            return true
        }

        override fun hashCode(): Int {
            return HASH * fileSystemClass.hashCode() + name.hashCode()
        }

        override fun toString(): String {
            return fileSystemClass.qualifiedName!! + "." + name
        }

        companion object {
            private val HASH = 29
        }
    }

    fun setOption(fileSystemClass: KClass<out FileSystem>, name: String, value: Any) {
        options + (FileSystemOptionKey(fileSystemClass, name) to value)
    }

    fun getOption(fileSystemClass: KClass<out FileSystem>, name: String): Any? {
        val key = FileSystemOptionKey(fileSystemClass, name)
        return options[key]
    }

    fun hasOption(fileSystemClass: KClass<out FileSystem>, name: String): Boolean {
        val key = FileSystemOptionKey(fileSystemClass, name)
        return options.containsKey(key)
    }

    operator fun compareTo(other: FileSystemOptions): Int {
        if (this === other) {
            // the same instance
            return 0
        }

        val propsSz = options?.size ?: 0
        val propsFkSz = if (other.options == null) 0 else other.options.size
        if (propsSz < propsFkSz) {
            return -1
        }
        if (propsSz > propsFkSz) {
            return 1
        }
        if (propsSz == 0) {
            // props empty
            return 0
        }

        // ensure proper sequence of options
        val myOptions = if (options is SortedMap<*, *>)
            options as SortedMap<FileSystemOptionKey, Any>?
        else
            TreeMap(options)
        val theirOptions = if (other.options is SortedMap<*, *>)
            other.options as SortedMap<FileSystemOptionKey, Any>?
        else
            TreeMap(other.options)
        val optKeysIter = myOptions!!.keys.iterator()
        val otherKeysIter = theirOptions!!.keys.iterator()
        while (optKeysIter.hasNext()) {
            val comp = optKeysIter.next().compareTo(otherKeysIter.next())
            if (comp != 0) {
                return comp
            }
        }

        val array = arrayOfNulls<Any>(propsSz)
        val hash = Arrays.deepHashCode(myOptions.values.toTypedArray())
        val hashFk = Arrays.deepHashCode(theirOptions.values.toTypedArray())
        if (hash < hashFk) {
            return -1
        }
        if (hash > hashFk) {
            return 1
        }

        return 0
    }

    override fun hashCode(): Int {
        val prime = 31
        var result = 1
        if (options == null) {
            result = prime * result
        } else {
            val myOptions = if (options is SortedMap<*, *>)
                options as SortedMap<FileSystemOptionKey, Any>?
            else
                TreeMap(options)
            result = prime * result + myOptions!!.keys.hashCode()
            result = prime * result + Arrays.deepHashCode(myOptions!!.values.toTypedArray())
        }
        return result
    }

    override fun equals(obj: Any?): Boolean {
        if (this === obj) {
            return true
        }
        if (obj == null) {
            return false
        }
        if (javaClass != obj.javaClass) {
            return false
        }
        val other = obj as FileSystemOptions
        return compareTo(other) == 0
    }

    /**
     * {@inheritDoc}

     * @since 2.0
     */
    public override fun clone(): Any {
        return FileSystemOptions(TreeMap(options))
    }

    override fun toString(): String {
        return options!!.toString()
    }
}