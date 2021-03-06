package com.warrior.classification_workflow.core.meta.features

import weka.core.Instances

abstract class AbstractMetaFeatureExtractor : MetaFeatureExtractor {

    open lateinit var instances: Instances

    fun compute(instances: Instances): Double {
        this.instances = instances
        return compute()
    }

    protected fun isNonClassAttributeWithType(instances: Instances, attributeIndex: Int, vararg types: Int): Boolean {
        val attribute = instances.attribute(attributeIndex)
        val type = attribute.type()
        return attributeIndex != instances.classIndex() && types.any { t -> t == type }
    }

    companion object {

        /**
         * Creates a new instance of a meta feature given it's class name.

         * @param className the fully qualified class name of the meta feature
         * *
         * @return new instance of the meta feature. If the name is invalid return null
         */
        @JvmStatic
        fun forName(className: String): AbstractMetaFeatureExtractor? {
            val clazz: Class<*>
            try {
                clazz = Class.forName(className)
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
                return null
            }

            if (!AbstractMetaFeatureExtractor::class.java.isAssignableFrom(clazz)) {
                return null
            }
            val newInstance: Any
            try {
                newInstance = clazz.newInstance()
            } catch (e: InstantiationException) {
                e.printStackTrace()
                return null
            } catch (e: IllegalAccessException) {
                e.printStackTrace()
                return null
            }

            return newInstance as AbstractMetaFeatureExtractor
        }
    }
}
