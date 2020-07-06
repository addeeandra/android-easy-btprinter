package com.inibukanadit.easybtprinter.common.lifecycle

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData

/**
 * Refs : https://gist.github.com/inibukanadit/e632f5c9baea251c2e24c71bf7072881
 */
class CombinedTransformation<T>(
    vararg liveData: LiveData<*>,
    private val onAllChanged: (data: List<Any?>) -> T
) : MediatorLiveData<T>() {

    private val mLiveDataList: MutableList<Any?> = MutableList(liveData.size) { null }

    init {
        liveData.forEachIndexed { index, liveDatum ->
            super.addSource(liveDatum) { datum ->
                mLiveDataList[index] = datum
                value = onAllChanged(mLiveDataList)
            }
        }
    }

}