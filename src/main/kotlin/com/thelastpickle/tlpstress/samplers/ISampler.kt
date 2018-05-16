package com.thelastpickle.tlpstress.samplers

typealias Fields = Map<String, Any>

interface ISampler : Iterable<Fields> {
    fun maybePut(primaryKey: Any, fields: Fields)
}