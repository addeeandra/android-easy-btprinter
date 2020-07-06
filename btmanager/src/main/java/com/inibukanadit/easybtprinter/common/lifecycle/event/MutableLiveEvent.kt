package com.inibukanadit.easybtprinter.common.lifecycle.event

/**
 * Refs : https://gist.github.com/inibukanadit/5cafbbd00646393276c2b89856c11bdc
 */
class MutableLiveEvent<T> : LiveEvent<T>() {

    var value
        get() = action.value
        set(newValue) {
            action.postValue(newValue)
        }

    fun put(value: T) {
        this.value = ActionEvent(value)
    }

}