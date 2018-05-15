package com.thelastpickle.tlpstress

import com.thelastpickle.tlpstress.samplers.ISampler
import java.util.concurrent.ThreadLocalRandom


/**
 * data sampler for holding onto stuff we want
 * to verify later
 * Rules:
 * if a key is already held, we updated it
 */
class PrimaryKeySampler(val rate: Double) : ISampler {
    var data = mutableMapOf<Any, Any>()

    override fun maybePut(primaryKey: Any, fields: Any) {
        var shouldAdd = false

        if(data.contains(primaryKey))
            shouldAdd = true

        if(ThreadLocalRandom.current().nextInt(0, 100) < (rate * 100).toInt() )
            shouldAdd = true

        if(shouldAdd) {
            data[primaryKey] = fields
        }
    }

}