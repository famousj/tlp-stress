package com.thelastpickle.tlpstress

typealias Cell = Pair<Any, Int>

/**
 * data sampler for holding onto stuff we want
 * to verify later
 * Rules:
 * if a key is already held, we updated it
 */
class Sampler(val rate: Double) {
    var data = mutableMapOf<Any, Any>()

    fun maybePut(partition: Any, clustering: Any?, fields: Any) {
        data[partition] = fields
    }
}