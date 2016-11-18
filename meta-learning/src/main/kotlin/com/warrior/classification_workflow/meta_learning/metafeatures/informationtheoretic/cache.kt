package com.warrior.classification_workflow.meta_learning.metafeatures.informationtheoretic

import weka.core.Attribute

/**
 * Created by warrior on 11/18/16.
 */
interface MutualInformationCache {
    fun setMutualInformationCache(cache: MutableMap<Attribute, Double>)
}

interface EntropyCache {
    fun setEntropyCache(cache: MutableMap<Attribute, EntropyResult>)
}
