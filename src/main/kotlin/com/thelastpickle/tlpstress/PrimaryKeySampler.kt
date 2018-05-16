package com.thelastpickle.tlpstress

import com.thelastpickle.tlpstress.samplers.Fields
import com.thelastpickle.tlpstress.samplers.ISampler
import java.util.concurrent.ThreadLocalRandom

/**
 * data sampler for holding onto stuff we want
 * to verify later
 * Rules:
 * if a key is already held, we updated it
 */
class PrimaryKeySampler(val rate: Double) : ISampler {
    override fun iterator(): Iterator<Fields> {
        TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
    }

    var data = mutableMapOf<Any, Any>()

    override fun maybePut(primaryKey: Any, fields: Fields) {
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