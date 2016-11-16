package com.warrior.classification_workflow.meta_learning.metafeatures

import weka.core.Instances

abstract class AbstractMetaFeature : MetaFeature {

    open lateinit var instances: Instances

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
        fun forName(className: String): AbstractMetaFeature? {
            val clazz: Class<*>
            try {
                clazz = Class.forName(className)
            } catch (e: ClassNotFoundException) {
                e.printStackTrace()
                return null
            }

            if (!AbstractMetaFeature::class.java.isAssignableFrom(clazz)) {
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

            return newInstance as AbstractMetaFeature
        }
    }
}
