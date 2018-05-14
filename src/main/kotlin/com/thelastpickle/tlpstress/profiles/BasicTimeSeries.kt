package com.thelastpickle.tlpstress.profiles

import com.datastax.driver.core.PreparedStatement
import com.datastax.driver.core.Session
import com.thelastpickle.tlpstress.randomString

/**
 * Create a simple time series use case with some number of partitions
 * TODO make it use TWCS
 */
class BasicTimeSeries : IStressProfile {
    override fun schema(): List<String> {
        val query = """CREATE TABLE IF NOT EXISTS sensor_data (
                            sensor_id text,
                            timestamp timeuuid,
                            data text,
                            primary key(sensor_id, timestamp))
                            WITH CLUSTERING ORDER BY (timestamp DESC)
                           """.trimIndent()

        return listOf(query)
    }

    lateinit var prepared: PreparedStatement

    // jcommander arguments
    class Arguments {

    }

    override fun getArguments() : Any {
        return Arguments()
    }


    override fun prepare(session: Session) {
        prepared = session.prepare("INSERT INTO sensor_data (sensor_id, timestamp, data) VALUES (?, now(), ?)")
    }

    class TimeSeriesRunner(val insert: PreparedStatement) : IStressRunner {
        override fun getNextOperation(partitionKey: String) : Operation {
            val data = randomString(100)
            val bound = insert.bind(partitionKey, data)
            val fields = mapOf("data" to data)
            return Operation.Mutation(bound, fields)
        }

    }


    override fun getRunner(): IStressRunner {
        return TimeSeriesRunner(prepared)
    }


}