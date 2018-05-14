package com.thelastpickle.tlpstress

import com.datastax.driver.core.ResultSet
import com.google.common.util.concurrent.FutureCallback
import com.google.common.util.concurrent.Futures
import com.thelastpickle.tlpstress.profiles.IStressProfile
import com.thelastpickle.tlpstress.profiles.Operation
import mu.KotlinLogging
import java.util.concurrent.Semaphore

private val logger = KotlinLogging.logger {}


/**
 * Single threaded profile runner.
 * One profile runner should be created per thread
 * Logs all errors along the way
 * Keeps track of useful metrics, per thread
 */
class ProfileRunner(val context: StressContext,
                    val profile: IStressProfile,
                    val partitionKeyGenerator: PartitionKeyGenerator) {

    companion object {
        fun create(context: StressContext, profile: IStressProfile) : ProfileRunner {
            val prefix = context.mainArguments.id + "."
            val partitionKeyGenerator = PartitionKeyGenerator.random(prefix)
            return ProfileRunner(context, profile, partitionKeyGenerator)
        }
    }

    /**

     */
    fun run() {

        profile.prepare(context.session)

        logger.info { "Starting up runner" }

        // we're going to (for now) only keep 1000 in flight queries per session
        // might move this to the context if i share the session
        var sem = Semaphore(1000)

        for(key in partitionKeyGenerator.generateKey(context.mainArguments.iterations, 10000)) {

            // get next thing from the profile
            // thing could be a statement, or it could be a failure command
            // certain profiles will want to deterministically inject failures
            // others can be randomly injected by the runner
            // I should be able to just tell the runner to inject gossip failures in any test
            // without having to write that code in the profile

            val runner = profile.getRunner()
            val op = runner.getNextOperation(key)

            when(op) {
                is Operation.InsertStatement -> {
                    logger.debug { op }

                    sem.acquire()

                    val future = context.session.executeAsync(op.bound)

                    Futures.addCallback(future, object : FutureCallback<ResultSet> {
                        override fun onFailure(t: Throwable?) {
                            sem.release()
                            context.errors.mark()
                        }

                        override fun onSuccess(result: ResultSet?) {
                            sem.release()
                            context.requests.mark()

                        }
                    } )
                }
            }
        }
    }

    fun verify() {

    }

    fun prepare() {
        profile.prepare(context.session)
    }


}