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
import net.corda.core.transactions.SignedTransaction
import net.corda.core.utilities.ProgressTracker
import java.util.Currency


// *********
// * Flows *
// *********
object CreateYachtStateFlow{
    @InitiatingFlow
    @StartableByRPC
    class Initiator (
        private val owner: Party,
        private val price: Amount<Currency>,
        private val forSale: Boolean,
        private val yachtLinearId: UniqueIdentifier
    ) : FlowLogic<SignedTransaction>() {
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
        override fun call(): SignedTransaction {
            // Get a reference to the notary service on our network and our key pair.
            val notary = serviceHub.networkMapCache.getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"))

            // Check if the owner of the respective yacht ref is the same as the proposed owner of the Yacht State
//            val listYachtRefStateAndRefs = serviceHub.vaultService.queryBy(YachtRef::class.java).states
//
//            if (listYachtRefStateAndRefs.isEmpty()){
//                return emptyList()
//            } else {
//                val filteredYachtStateAndRef = listYachtRefStateAndRefs.filter{
//                    it.state.data.owner == owner &&
//                            it.state.data.linearId == yachtLinearId
//                }
//            }
            // Check that the owner of Yacht Ref matches the proposed owner of the Yacht State

            // Compose the output state
            val outputState = YachtState(owner, price, forSale, yachtLinearId)

            // Create a new TransactionBuilder object.
            progressTracker.currentStep = GENERATING_TRANSACTION
            val builder = TransactionBuilder(notary)
                .addOutputState(outputState)
                .addCommand(YachtContract.Commands.Create(), listOf(owner.owningKey))

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
            )
        }


    }

}
