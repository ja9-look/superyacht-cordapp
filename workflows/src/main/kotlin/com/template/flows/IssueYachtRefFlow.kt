package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.*
import net.corda.core.identity.*
import net.corda.core.utilities.ProgressTracker.Step
import net.corda.core.flows.FinalityFlow

import net.corda.core.transactions.SignedTransaction

import com.template.contracts.YachtRefContract

import net.corda.core.transactions.TransactionBuilder

import com.template.states.YachtRef
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.contracts.requireThat
import net.corda.core.utilities.ProgressTracker
import java.util.Date


// *********
// * Flows *
// *********
object IssueYachtRefFlow{
    @InitiatingFlow
    @StartableByRPC
    class Initiator (
        private val owner: AbstractParty,
        private val name: String,
        private val type: String,
        private val length: Double,
        private val beam: Double,
        private val builderName: String,
        private val yearOfBuild: Date,
        private val grossTonnage: Double,
        private val maxSpeed: Int,
        private val cruiseSpeed: Int,
        private val imageUrls: List<String>,
        private val linearId: UniqueIdentifier
    ) : FlowLogic<StateAndRef<YachtRef>>() {
        companion object {
            object GENERATING_TRANSACTION : Step("Generating Transaction")
            object SIGNING_TRANSACTION : Step("Signing transaction with our private key.")
            object GATHERING_SIGS : Step("Gathering signature from owner.") {
                override fun childProgressTracker() = CollectSignaturesFlow.tracker()
            }
            object FINALISING_TRANSACTION : Step("Recording transaction.") {
                override fun childProgressTracker() = FinalityFlow.tracker()
            }

            fun tracker() = ProgressTracker(
                GENERATING_TRANSACTION,
                SIGNING_TRANSACTION,
                GATHERING_SIGS,
                FINALISING_TRANSACTION
            )
        }

        override val progressTracker = tracker()

        @Suspendable
        override fun call(): StateAndRef<YachtRef> {
            // Get a reference to the notary service on our network and our key pair.
            val notary = serviceHub.networkMapCache.getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"))

            // Compose the output state
            val outputState = YachtRef(ourIdentity, owner, name, type, length, beam, builderName, yearOfBuild, grossTonnage, maxSpeed, cruiseSpeed, imageUrls, linearId )

            // Create a new TransactionBuilder object.
            progressTracker.currentStep = GENERATING_TRANSACTION
            val builder = TransactionBuilder(notary)
                .addOutputState(outputState)
                .addCommand(YachtRefContract.Commands.Create(), listOf(ourIdentity.owningKey, owner.owningKey))

            // Verify the transaction
            builder.verify(serviceHub)
            // Sign the transaction (issuer)
            progressTracker.currentStep = SIGNING_TRANSACTION
            val partSignedTx = serviceHub.signInitialTransaction(builder)

            // Send the state to the  counterparty (owner) and receive it back with their signature
            progressTracker.currentStep = GATHERING_SIGS
            val counterpartySession = initiateFlow(owner)
            val fullySignedTx = subFlow(CollectSignaturesFlow(partSignedTx, setOf(counterpartySession), GATHERING_SIGS.childProgressTracker()))

            // Notarise the transaction and record the state in the ledger
            progressTracker.currentStep = FINALISING_TRANSACTION
            return subFlow(
                FinalityFlow(
                    transaction = fullySignedTx,
                    sessions = listOf(counterpartySession),
                    progressTracker = FINALISING_TRANSACTION.childProgressTracker()
                )
            ).tx.outRefsOfType(YachtRef::class.java).single()
        }


    }
    @InitiatedBy(Initiator::class)
    class Responder(val counterpartySession: FlowSession): FlowLogic<SignedTransaction>(){
        @Suspendable
        override fun call(): SignedTransaction {
            val signTransactionFlow = object: SignTransactionFlow(counterpartySession) {
                override fun checkTransaction(stx: SignedTransaction) = requireThat {
                    val output = stx.tx.outputs.single().data
                    "This must be an issue yacht ref transaction." using (output is YachtRef)
                }
            }
            val txId = subFlow(signTransactionFlow).id

            return subFlow(ReceiveFinalityFlow(counterpartySession, expectedTxId = txId))
        }
    }
}
