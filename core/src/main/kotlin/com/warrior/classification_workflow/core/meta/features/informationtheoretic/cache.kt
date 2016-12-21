package com.warrior.classification_workflow.core.meta.features.informationtheoretic

import weka.core.Attribute
import java.util.concurrent.ConcurrentMap

/**
 * Created by warrior on 11/18/16.
 */
interface MutualInformationCache {
    fun setMutualInformationCache(cache: ConcurrentMap<Attribute, Double>)
}

interface EntropyCache {
    fun setEntropyCache(cache: ConcurrentMap<Attribute, EntropyResult>)
}
