package com.thelastpickle.tlpstress.profiles

import com.beust.jcommander.Parameter
import com.datastax.driver.core.PreparedStatement
import com.datastax.driver.core.Session
import com.thelastpickle.tlpstress.randomString
import java.util.concurrent.ThreadLocalRandom

/**
 * Create a simple time series use case with some number of partitions
 * TODO make it use TWCS
 */
class BasicTimeSeries : IStressProfile {
    override fun schema(): List<String> {
        val query = """CREATE TABLE IF NOT EXISTS sensor_data (
                            |sensor_id text,
                            |timestamp timeuuid,
                            |data text,
                            |primary key(sensor_id, timestamp))
                            |WITH CLUSTERING ORDER BY (timestamp DESC)
                            |
                            |""".trimMargin()

        return listOf(query)
    }

    lateinit var prepared: PreparedStatement

    class Arguments {
        @Parameter(names=["max_id"], description = "Max id of the sensor")
        var maxId = 100000
    }

    override fun getArguments() : Any {
        return Arguments()
    }


    override fun prepare(session: Session) {
        prepared = session.prepare("INSERT INTO sensor_data (sensor_id, timestamp, data) VALUES (?, now(), ?)")
    }

    class TimeSeriesRunner(val insert: PreparedStatement) : IStressRunner {
        override fun getNextOperation(partitionKey: String) : Operation {
            // TODO: instead of getting the sensor id from random lets grab it from the PartitionKeyGenerator

            val bound = insert.bind(partitionKey, randomString(100))
            return Operation.Statement(bound)
        }

    }


    override fun getRunner(): IStressRunner {
        return TimeSeriesRunner(prepared)
    }


}