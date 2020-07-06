package com.inibukanadit.easybtprinter.common.lifecycle.event

/**
 * Refs : https://gist.github.com/inibukanadit/5cafbbd00646393276c2b89856c11bdc
 */
class ActionEvent<out T>(private val content: T) {

    var hasBeenUsed = false
        private set

    fun getContentIfNotUsed(): T? {
        return if (hasBeenUsed) {
            null
        } else {
            hasBeenUsed = true
            content
        }
    }

    fun peekContent(): T = content

}