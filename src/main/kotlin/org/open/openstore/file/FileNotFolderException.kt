package org.open.openstore.file

/**
 * 非文件夹异常
 */
class FileNotFolderException(info: Any, throwable: Throwable?): FileSystemException(code = "not-folder.error", info = arrayOf(info), throwable = throwable) {
    constructor(info: Any): this(info, null) {}
}

/**
 * 文件未找到异常
 */
class FileNotFoundException(info: Any, throwable: Throwable?): FileSystemException(code = "file-not-found.error", info = arrayOf(info), throwable = throwable) {
    constructor(info: Any): this(info, null) {}
}

/**
 * 文件无内容
 */
class FileTypeHasNoContentException(info: Any, throwable: Throwable?): FileSystemException(code = "file-no-contentAccessor.error", info = arrayOf(info), throwable = throwable) {
    constructor(info: Any): this(info, null) {}
}
