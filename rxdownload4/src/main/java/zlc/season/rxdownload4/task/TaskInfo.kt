package zlc.season.rxdownload4.task

import io.reactivex.Flowable
import io.reactivex.disposables.Disposable
import zlc.season.rxdownload4.Progress

class TaskInfo(
        val flowable: Flowable<Progress>,
        val disposable: Disposable) {
}