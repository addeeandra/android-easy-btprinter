package com.inibukanadit.easybtprinter.common.lifecycle.event

import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer

/**
 * Refs : https://gist.github.com/inibukanadit/5cafbbd00646393276c2b89856c11bdc
 */
open class LiveEvent<T> {

    protected val action: MutableLiveData<ActionEvent<T>> = MutableLiveData()

    fun observe(lifecycleOwner: LifecycleOwner, callback: (data: T) -> Unit) {
        action.observe(lifecycleOwner, Observer {
            action.value?.let {
                if (!it.hasBeenUsed) {
                    callback(it.getContentIfNotUsed() ?: it.peekContent())
                }
            }
        })
    }

}