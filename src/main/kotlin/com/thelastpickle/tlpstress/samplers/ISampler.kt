package com.thelastpickle.tlpstress.samplers

interface ISampler {
    fun maybePut(primaryKey: Any, fields: Any)
}