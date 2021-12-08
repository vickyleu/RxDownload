package zlc.season.rxdownload4.downloader

import io.reactivex.rxjava3.core.Emitter
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Flowable.generate
import io.reactivex.rxjava3.functions.BiConsumer
import io.reactivex.rxjava3.functions.Consumer
import io.reactivex.rxjava3.functions.Supplier
import okhttp3.ResponseBody
import okio.*
import retrofit2.Response
import zlc.season.rxdownload4.Progress
import zlc.season.rxdownload4.task.TaskInfo
import zlc.season.rxdownload4.utils.*
import java.io.File

class NormalDownloader : Downloader {
    private var alreadyDownloaded = false

    private lateinit var file: File
    private lateinit var shadowFile: File

    override fun download(
        taskInfo: TaskInfo,
        response: Response<ResponseBody>
    ): Flowable<Progress> {
        val body = response.body() ?: throw RuntimeException("Response body is NULL")

        file = taskInfo.task.getFile()
        shadowFile = file.shadow()

        beforeDownload(taskInfo, response)

        return if (alreadyDownloaded) {
            Flowable.just(
                Progress(
                    downloadSize = response.contentLength(),
                    totalSize = response.contentLength()
                )
            )
        } else {
            startDownload(
                body, Progress(
                    totalSize = response.contentLength(),
                    isChunked = response.isChunked()
                )
            )
        }
    }

    private fun beforeDownload(taskInfo: TaskInfo, response: Response<ResponseBody>) {
        //make sure dir is exists
        val fileDir = taskInfo.task.getDir()
        if (!fileDir.exists() || !fileDir.isDirectory) {
            fileDir.mkdirs()
        }

        if (file.exists()) {
            if (taskInfo.validator.validate(file, response)) {
                alreadyDownloaded = true
            } else {
                file.delete()
                shadowFile.recreate()
            }
        } else {
            shadowFile.recreate()
        }
    }

    private fun startDownload(body: ResponseBody, progress: Progress): Flowable<Progress> {

        return generate(
            Supplier {
                InternalState(
                    body.source(),
                    shadowFile.sink().buffer()
                )
            },
            BiConsumer<InternalState, Emitter<Progress>> { internalState, emitter ->
                internalState.apply {
                    val readLen = source.read(buffer, 8192L)

                    if (readLen == -1L) {
                        sink.flush()
                        shadowFile.renameTo(file)
                        emitter.onComplete()
                    } else {
                        sink.emit()
                        emitter.onNext(progress.apply {
                            downloadSize += readLen
                        })
                    }
                }
            },
            Consumer {
                it.apply {
                    sink.closeQuietly()
                    source.closeQuietly()
                }
            })
    }

    class InternalState(
        val source: BufferedSource,
        val sink: BufferedSink,
        val buffer: Buffer = sink.buffer
    )
}