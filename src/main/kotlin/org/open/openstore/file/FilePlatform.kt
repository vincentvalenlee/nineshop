package org.open.openstore.file

import org.open.openstore.file.contributors.FileNameParser
import org.apache.commons.configuration2.builder.fluent.Configurations
import java.io.File
import java.util.*


/**
 * 文件平台，是x文件系统全局的对象，负责创建各类信息、配置等
 */
object FilePlatform {

    val CONF = Configurations().properties("application.properties")

    val URL_STYPE: Boolean = CONF.getBoolean("uriStyle")?: false

    val NAMESPACE_CORE = "org.xctrl.xfilesystem"

    val POINT_NAME_CORE_FILE = "file.context"

    val POINT_NAME_CONFIG_BUILDER = "config.builders"

    /**
     * 核心模块定义的文件系统FileContributor扩展点id，应用从此扩展点提供自己的
     */
    val CONTRIBUT_POINT_CORE_FILE = NAMESPACE_CORE + "." + POINT_NAME_CORE_FILE

    /**
     * 核心模块定义的FileSystemConfigBuilder的扩展点id
     */
    val CONTRIBUT_POINT_CONFIG_BUILDER = NAMESPACE_CORE +  "." + POINT_NAME_CONFIG_BUILDER

    fun getContributRegistry():IContributRegistry {
        TODO("从平台中[osgi运行环境]获取贡献注册表")
    }

    /**
     * RFC 2396规范，处理URI的解析器
     */
    object UriParser {

        val TRANS_SEPARATOR = '\\'

        private val SEPARATOR_CHAR = IFileName.SEPARATOR_CHAR

        private val HEX_BASE = 16

        private val BITS_IN_HALF_BYTE = 4

        private val LOW_MASK: Char = 0x0F.toChar()

        /**
         * 解析路径的第一个元素
         */
        fun extractFirstElement(name: StringBuilder): String? {
            val len = name.length
            if (len < 1) {
                return null
            }
            var startPos = 0
            if (name[0] == SEPARATOR_CHAR) {
                startPos = 1
            }
            for (pos in startPos..len - 1) {
                if (name[pos] == SEPARATOR_CHAR) {
                    // Found a separator
                    val elem = name.substring(startPos, pos)
                    name.delete(startPos, pos + 1)
                    return elem
                }
            }

            // No separator
            val elem = name.substring(startPos)
            name.setLength(0)
            return elem
        }

        /**
         * 规范化路径：
         * 1.删除空路径元素
         * 2.处理.以及..元素
         * 3.删除尾部的分隔符
         */
        fun normalisePath(path: StringBuilder): FileType {
            var fileType = FileType.FOLDER
            if (path.length == 0) {
                return fileType
            }

            if (path[path.length - 1] != '/') {
                fileType = FileType.FILE
            }

            // Adjust separators
            // fixSeparators(path);

            // Determine the start of the first element
            var startFirstElem = 0
            if (path[0] == SEPARATOR_CHAR) {
                if (path.length == 1) {
                    return fileType
                }
                startFirstElem = 1
            }

            // Iterate over each element
            var startElem = startFirstElem
            var maxlen = path.length
            while (startElem < maxlen) {
                // Find the end of the element
                var endElem = startElem
                while (endElem < maxlen && path[endElem] != SEPARATOR_CHAR) {
                    endElem++
                }

                val elemLen = endElem - startElem
                if (elemLen == 0) {
                    // An empty element - axe it
                    path.delete(endElem, endElem + 1)
                    maxlen = path.length
                    continue
                }
                if (elemLen == 1 && path[startElem] == '.') {
                    // A '.' element - axe it
                    path.delete(startElem, endElem + 1)
                    maxlen = path.length
                    continue
                }
                if (elemLen == 2 && path[startElem] == '.' && path[startElem + 1] == '.') {
                    // A '..' element - remove the previous element
                    if (startElem == startFirstElem) {
                        // Previous element is missing
                        throw FileSystemException("无效的相对路径")
                    }

                    // Find start of previous element
                    var pos = startElem - 2
                    while (pos >= 0 && path[pos] != SEPARATOR_CHAR) {
                        pos--
                    }
                    startElem = pos + 1

                    path.delete(startElem, endElem + 1)
                    maxlen = path.length
                    continue
                }

                // A regular element
                startElem = endElem + 1
            }

            // Remove trailing separator
            if (!FilePlatform.URL_STYPE && maxlen > 1 && path[maxlen - 1] == SEPARATOR_CHAR) {
                path.delete(maxlen - 1, maxlen)
            }

            return fileType
        }

        /**
         * 规范化分隔符
         */
        fun fixSeparators(name: StringBuilder): Boolean {
            var changed = false
            val maxlen = name.length
            for (i in 0..maxlen - 1) {
                val ch = name[i]
                if (ch == TRANS_SEPARATOR) {
                    name.setCharAt(i, SEPARATOR_CHAR)
                    changed = true
                }
            }
            return changed
        }

        /**
         * 从URI中解析scheme
         */
        fun extractScheme(uri: String): String? {
            return extractScheme(uri, null)
        }

        /**
         * 解析URI中的scheme，从URI删除schem以及:分隔符
         */
        fun extractScheme(uri: String, buffer: StringBuilder?): String? {
            if (buffer != null) {
                buffer.setLength(0)
                buffer.append(uri)
            }

            val maxPos = uri.length
            for (pos in 0..maxPos - 1) {
                val ch = uri[pos]

                if (ch == ':') {
                    // Found the end of the scheme
                    val scheme = uri.substring(0, pos)
                    if (scheme.length <= 1 && Os.isFamily(Os.OS_FAMILY_WINDOWS)) {
                        // This is not a scheme, but a Windows drive letter
                        return null
                    }
                    buffer?.delete(0, pos + 1)
                    return scheme.intern()
                }

                if (ch >= 'a' && ch <= 'z' || ch >= 'A' && ch <= 'Z') {
                    // A scheme character
                    continue
                }
                if (pos > 0 && (ch >= '0' && ch <= '9' || ch == '+' || ch == '-' || ch == '.')) {
                    // A scheme character (these are not allowed as the first
                    // character of the scheme, but can be used as subsequent
                    // characters.
                    continue
                }

                // Not a scheme character
                break
            }

            // No scheme in URI
            return null
        }

        fun decode(encodedStr: String?): String? {
            if (encodedStr == null) {
                return null
            }
            if (encodedStr.indexOf('%') < 0) {
                return encodedStr
            }
            val buffer = StringBuilder(encodedStr)
            decode(buffer, 0, buffer.length)
            return buffer.toString()
        }

        /**
         * 从字符中删除%nn编码
         * @param buffer 包含编码的字符缓存
         * @param offset 开始编码的字符位置
         * @param length 编码的字符数目
         */
        fun decode(buffer: StringBuilder, offset: Int, length: Int) {
            var index = offset
            var count = length
            while (count > 0) {
                val ch = buffer[index]
                if (ch != '%') {
                    count--
                    index++
                    continue
                }
                if (count < 3) {
                    throw FileSystemException("无效的编码序列", null,
                            arrayOf(buffer.substring(index, index + count)))
                }

                // Decode
                val dig1 = Character.digit(buffer[index + 1], HEX_BASE)
                val dig2 = Character.digit(buffer[index + 2], HEX_BASE)
                if (dig1 == -1 || dig2 == -1) {
                    throw FileSystemException("无效的编码序列", null,
                            arrayOf(buffer.substring(index, index + 3)))
                }
                val value = (dig1 shl BITS_IN_HALF_BYTE or dig2).toChar()

                // Replace
                buffer.setCharAt(index, value)
                buffer.delete(index + 1, index + 3)
                count -= 2
                count--
                index++
            }
        }


        fun appendEncoded(buffer: StringBuilder, unencodedValue: String, reserved: CharArray) {
            val offset = buffer.length
            buffer.append(unencodedValue)
            encode(buffer, offset, unencodedValue.length, reserved)
        }


        fun encode(buffer: StringBuilder, offset: Int, length: Int, reserved: CharArray?) {
            var index = offset
            var count = length
            while (count > 0) {
                val ch = buffer[index]
                var match = ch == '%'
                if (reserved != null) {
                    var i = 0
                    while (!match && i < reserved.size) {
                        if (ch == reserved[i]) {
                            match = true
                        }
                        i++
                    }
                }
                if (match) {
                    // Encode
                    val digits = charArrayOf(Character.forDigit(ch.toInt() shr BITS_IN_HALF_BYTE and LOW_MASK.toInt(), HEX_BASE), Character.forDigit(ch.toInt() and LOW_MASK.toInt(), HEX_BASE))
                    buffer.setCharAt(index, '%')
                    buffer.insert(index + 1, digits)
                    index += 2
                }
                index++
                count--
            }
        }


        fun encode(decodedStr: String): String {
            return encode(decodedStr, null)!!
        }

        /**
         * 转换特殊的字符到%nn值
         */
        fun encode(decodedStr: String?, reserved: CharArray?): String? {
            if (decodedStr == null) {
                return null
            }
            val buffer = StringBuilder(decodedStr)
            encode(buffer, 0, buffer.length, reserved)
            return buffer.toString()
        }

        fun encode(strings: Array<String>?): Array<String>? {
            if (strings == null) {
                return null
            }
            for (i in strings.indices) {
                strings[i] = encode(strings[i])
            }
            return strings
        }

        fun checkUriEncoding(uri: String) {
            decode(uri)
        }

        fun canonicalizePath(buffer: StringBuilder, offset: Int, length: Int, fileNameParser: FileNameParser) {
            var index = offset
            var count = length
            while (count > 0) {
                val ch = buffer[index]
                if (ch == '%') {
                    if (count < 3) {
                        throw FileSystemException("无效的占位序列",null,
                                arrayOf(buffer.substring(index, index + count)))
                    }

                    // Decode
                    val dig1 = Character.digit(buffer[index + 1], HEX_BASE)
                    val dig2 = Character.digit(buffer[index + 2], HEX_BASE)
                    if (dig1 == -1 || dig2 == -1) {
                        throw FileSystemException("无效的占位序列",null,
                                arrayOf(buffer.substring(index, index + 3)))
                    }
                    val value = (dig1 shl BITS_IN_HALF_BYTE or dig2).toChar()

                    val match = value == '%' || fileNameParser.encodeCharacter(value)

                    if (match) {
                        // this is a reserved character, not allowed to decode
                        index += 2
                        count -= 2
                        count--
                        index++
                        continue
                    }

                    // Replace
                    buffer.setCharAt(index, value)
                    buffer.delete(index + 1, index + 3)
                    count -= 2
                } else if (fileNameParser.encodeCharacter(ch)) {
                    // Encode
                    val digits = charArrayOf(Character.forDigit(ch.toInt() shr BITS_IN_HALF_BYTE and LOW_MASK.toInt(), HEX_BASE), Character.forDigit(ch.toInt() and LOW_MASK.toInt(), HEX_BASE))
                    buffer.setCharAt(index, '%')
                    buffer.insert(index + 1, digits)
                    index += 2
                }
                count--
                index++
            }
        }

        fun extractQueryString(name: StringBuilder): String? {
            for (pos in 0..name.length - 1) {
                if (name[pos] == '?') {
                    val queryString = name.substring(pos + 1)
                    name.delete(pos, name.length)
                    return queryString
                }
            }

            return null
        }
    }

    data class OsFamily(val name:String, val families: Array<OsFamily> = arrayOf<OsFamily>())

    object Os {

        val OS_FAMILY_WINDOWS = OsFamily("windows")

        val OS_FAMILY_DOS = OsFamily("dos")

        val OS_FAMILY_WINNT = OsFamily("nt", arrayOf(OS_FAMILY_WINDOWS))

        val OS_FAMILY_WIN9X = OsFamily("win9x",arrayOf(OS_FAMILY_WINDOWS, OS_FAMILY_DOS))

        val OS_FAMILY_OS2 = OsFamily("os/2", arrayOf(OS_FAMILY_DOS))

        val OS_FAMILY_NETWARE = OsFamily("netware")

        val OS_FAMILY_UNIX = OsFamily("unix")

        val OS_FAMILY_MAC = OsFamily("mac")

        val OS_FAMILY_OSX = OsFamily("osx", arrayOf(OS_FAMILY_UNIX, OS_FAMILY_MAC))

        private val OS_NAME = System.getProperty("os.name").toLowerCase(Locale.US)
        private val OS_ARCH = System.getProperty("os.arch").toLowerCase(Locale.US)
        private val OS_VERSION = System.getProperty("os.version").toLowerCase(Locale.US)
        private val PATH_SEP = File.pathSeparator
        private var OS_FAMILY: OsFamily? = null
        private var OS_ALL_FAMILIES: Array<OsFamily>? = null

        private val ALL_FAMILIES = arrayOf(OS_FAMILY_DOS, OS_FAMILY_MAC, OS_FAMILY_NETWARE, OS_FAMILY_OS2, OS_FAMILY_OSX, OS_FAMILY_UNIX, OS_FAMILY_WINDOWS, OS_FAMILY_WINNT, OS_FAMILY_WIN9X)

        init {
            OS_FAMILY = determineOsFamily()
            OS_ALL_FAMILIES = determineAllFamilies()
        }

        fun isVersion(version: String): Boolean {
            return isOs(null as OsFamily?, null, null, version)
        }


        fun isArch(arch: String): Boolean {
            return isOs(null as OsFamily?, null, arch, null)
        }


        fun isFamily(family: String): Boolean {
            return isOs(family, null, null, null)
        }


        fun isFamily(family: OsFamily): Boolean {
            return isOs(family, null, null, null)
        }


        fun isName(name: String): Boolean {
            return isOs(null as OsFamily?, name, null, null)
        }


        fun isOs(family: String, name: String?, arch: String?, version: String?): Boolean {
            return isOs(getFamily(family), name, arch, version)
        }


        fun isOs(family: OsFamily?, name: String?, arch: String?, version: String?): Boolean {
            if (family != null || name != null || arch != null || version != null) {
                val isFamily = familyMatches(family)
                val isName = nameMatches(name)
                val isArch = archMatches(arch)
                val isVersion = versionMatches(version)

                return isFamily && isName && isArch && isVersion
            }
            return false
        }


        fun getFamily(name: String): OsFamily? {
            for (osFamily in ALL_FAMILIES) {
                if (osFamily.name.equals(name, ignoreCase = true)) {
                    return osFamily
                }
            }

            return null
        }

        private fun versionMatches(version: String?): Boolean {
            var isVersion = true
            if (version != null) {
                isVersion = version.equals(OS_VERSION, ignoreCase = true)
            }
            return isVersion
        }

        private fun archMatches(arch: String?): Boolean {
            var isArch = true
            if (arch != null) {
                isArch = arch.equals(OS_ARCH, ignoreCase = true)
            }
            return isArch
        }

        private fun nameMatches(name: String?): Boolean {
            var isName = true
            if (name != null) {
                isName = name.equals(OS_NAME, ignoreCase = true)
            }
            return isName
        }

        private fun familyMatches(family: OsFamily?): Boolean {
           return family?.let {
                OS_ALL_FAMILIES!!.any {
                    it === family
                }
            } ?: false
        }

        private fun  determineAllFamilies():Array<OsFamily> {
            var allFamilies:Set<OsFamily> = mutableSetOf()
            if (OS_FAMILY != null) {
                var queue = mutableListOf<OsFamily>()
                queue + OS_FAMILY
                while (queue.size > 0) {
                    val family = queue.removeAt(0)
                    allFamilies + family
                    val families = family.families
                    families.forEach {
                        queue + it
                    }
                }
            }
            return allFamilies.toTypedArray()
        }

        private fun determineOsFamily(): OsFamily? {
            // Determine the most specific OS family
            if (OS_NAME.indexOf("windows") > -1) {
                if (OS_NAME.indexOf("xp") > -1 || OS_NAME.indexOf("2000") > -1 || OS_NAME.indexOf("nt") > -1) {
                    return OS_FAMILY_WINNT
                }
                return OS_FAMILY_WIN9X
            } else if (OS_NAME.indexOf("os/2") > -1) {
                return OS_FAMILY_OS2
            } else if (OS_NAME.indexOf("netware") > -1) {
                return OS_FAMILY_NETWARE
            } else if (OS_NAME.indexOf("mac") > -1) {
                if (OS_NAME.endsWith("x")) {
                    return OS_FAMILY_OSX
                }
                return OS_FAMILY_MAC
            } else if (PATH_SEP.equals(":")) {
                return OS_FAMILY_UNIX
            } else {
                return null
            }
        }


    }

}

