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
            val prefix = context.mainArguments.id + "." + context.thread + "."
            val partitionKeyGenerator = PartitionKeyGenerator.random(prefix)
            return ProfileRunner(context, profile, partitionKeyGenerator)
        }
    }

    /**

     */
    fun run() {

        profile.prepare(context.session)

        logger.info { "Starting up runner" }

        // populate


        // we're going to (for now) only keep 1000 in flight queries per session
        // might move this to the context if i share the session

        executeOperations(context.mainArguments.iterations)

        verify()
    }

    /**
     * Used for both pre-populating data and for performing the actual runner
     */
    private fun executeOperations(iterations: Int) {
        var sem = Semaphore(1000)


        var operations = 0

        val sampler = profile.getSampler()
        val runner = profile.getRunner()

        for (key in partitionKeyGenerator.generateKey(iterations, context.mainArguments.partitionValues)) {

            // get next thing from the profile
            // thing could be a statement, or it could be a failure command
            // certain profiles will want to deterministically inject failures
            // others can be randomly injected by the runner
            // I should be able to just tell the runner to inject gossip failures in any test
            // without having to write that code in the profile

            val op = runner.getNextOperation(key)
            // TODO: instead of using the context request & errors, pass them in
            // that way this can be reused for the pre-population
            when (op) {
                is Operation.Mutation -> {
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
                            // if the key returned in the Mutation exists in the sampler, store the fields
                            // if not, use the sampler frequency
                            // need to be mindful of memory, frequency is a stopgap
                            context.requests.mark()
                            sampler.maybePut(op.partitionKey, op.fields)

                        }
                    })
                }
            }
            // TODO: it's possible for operations to stay open, need to ensure they get closed out before continuing
            operations++
        }

        println("Operations: $operations")
    }


    fun verify() {
        println("Verifying dataset")

    }

    fun prepare() {
        profile.prepare(context.session)
    }


}