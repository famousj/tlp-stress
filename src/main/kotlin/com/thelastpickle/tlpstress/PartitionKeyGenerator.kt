package com.thelastpickle.tlpstress

/**
 * We need to generate partition keys in every test
 * This class will manage the generation of partition keys
 * options:
 * sealed class
 * pass a generation algorithm + companions
 */

class PartitionKeyGenerator(prefix: String) {
    /**
     *
     */
    fun generateNextKey(iteration: Int) : String {
        return ""
    }

}