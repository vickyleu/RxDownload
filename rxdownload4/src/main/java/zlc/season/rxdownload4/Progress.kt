package zlc.season.rxdownload4

import androidx.room.Ignore
import zlc.season.rxdownload4.utils.formatSize
import zlc.season.rxdownload4.utils.ratio


class Progress {

    /**
     * 用于标识一个链接是否是分块下载, 如果该值为true, 那么totalSize为-1
     */
    var downloadSize: Long = 0
    var totalSize: Long = 0
    var isChunked: Boolean = false

    @Ignore
    constructor(downloadSize: Long = 0, totalSize: Long = 0, isChunked: Boolean = false) {
        this.downloadSize = downloadSize
        this.totalSize = totalSize
        this.isChunked = isChunked
    }

    constructor() : this(0, 0, false)

    /**
     * Return total size str. eg: 10M
     */
    fun totalSizeStr(): String {
        return totalSize.formatSize()
    }

    /**
     * Return download size str. eg: 3M
     */
    fun downloadSizeStr(): String {
        return downloadSize.formatSize()
    }

    /**
     * Return percent number.
     */
    fun percent(): Double {
        check(!isChunked) { "Chunked can not get percent!" }

        return downloadSize ratio totalSize
    }

    /**
     * Return percent string.
     */
    fun percentStr(): String {
        return "${percent()}%"
    }
}