package com.thelastpickle

import com.thelastpickle.tlpstress.PartitionKeyGenerator
import org.junit.jupiter.api.Test

internal class PartitionKeyGeneratorTest {
    @Test
    fun basicKeyGenerationTest() {
        var p = PartitionKeyGenerator("test")
        p.generateNextKey(1)
    }
}