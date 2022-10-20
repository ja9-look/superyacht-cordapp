package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.template.contracts.YachtContract
import net.corda.core.flows.*
import net.corda.core.identity.*
import net.corda.core.utilities.ProgressTracker.Step
import net.corda.core.flows.FinalityFlow

import net.corda.core.transactions.TransactionBuilder

import com.template.states.YachtRef
import com.template.states.YachtState
import net.corda.core.contracts.Amount
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.utilities.ProgressTracker
import java.util.Currency


// *********
// * Flows *
// *********
object CreateYachtStateFlow{
    @InitiatingFlow
    @StartableByRPC
    class Initiator (
        private val price: Amount<Currency>,
        private val forSale: Boolean,
        private val yachtLinearId: UniqueIdentifier
    ) : FlowLogic<StateAndRef<YachtState>>() {
        companion object {
            object GENERATING_TRANSACTION : Step("Generating Transaction")
            object SIGNING_TRANSACTION : Step("Signing transaction with our private key.")
            object FINALISING_TRANSACTION : Step("Recording transaction.") {
                override fun childProgressTracker() = FinalityFlow.tracker()
            }

            fun tracker() = ProgressTracker(
                GENERATING_TRANSACTION,
                SIGNING_TRANSACTION,
                FINALISING_TRANSACTION
            )
        }

        override val progressTracker = tracker()

        @Suspendable
        override fun call(): StateAndRef<YachtState> {
            // Get a reference to the notary service on our network and our key pair.
            val notary = serviceHub.networkMapCache.getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"))

            // Compose the output state
            val outputState = YachtState(ourIdentity, price, forSale, yachtLinearId)

            // Create a new TransactionBuilder object.
            progressTracker.currentStep = GENERATING_TRANSACTION
            val builder = TransactionBuilder(notary)
                .addOutputState(outputState)
                .addCommand(YachtContract.Commands.Create(), listOf(ourIdentity.owningKey))

            // Verify the transaction
            builder.verify(serviceHub)
            // Sign the transaction (issuer)
            progressTracker.currentStep = SIGNING_TRANSACTION
            val stx = serviceHub.signInitialTransaction(builder)

            // Notarise the transaction and record the state in the ledger
            progressTracker.currentStep = FINALISING_TRANSACTION
            return subFlow(
                FinalityFlow(
                    transaction = stx,
                    sessions = listOf(),
                    progressTracker = FINALISING_TRANSACTION.childProgressTracker()
                )
            ).tx.outRefsOfType(YachtState::class.java).single()
        }


    }

}
