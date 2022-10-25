package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.money.FiatCurrency.Companion.getInstance
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.selection.database.selector.DatabaseTokenSelection
import com.r3.corda.lib.tokens.workflows.flows.move.addMoveTokens
import com.r3.corda.lib.tokens.workflows.internal.flows.distribution.UpdateDistributionListFlow
import net.corda.core.flows.*
import net.corda.core.identity.*
import net.corda.core.utilities.ProgressTracker

import net.corda.core.transactions.SignedTransaction
import net.corda.core.transactions.TransactionBuilder

import com.template.states.YachtState
import net.corda.core.contracts.Amount
import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.node.services.queryBy
import net.corda.core.utilities.unwrap
import java.util.Currency


// *********
// * Flows *
// *********

class PurchaseYachtFlow {
    @InitiatingFlow
    @StartableByRPC
    class Initiator(
        private val newOwner: AbstractParty,
        private val yachtLinearId: UniqueIdentifier
    ) : FlowLogic<String>() {
        override val progressTracker = ProgressTracker()

        @Suspendable
        override fun call(): String {
            // Query to vault to find the corresponding YachtState
            val yachtStateAndRefs = serviceHub.vaultService.queryBy<YachtState>().states
            val filteredYachtStateAndRef = yachtStateAndRefs.filter { it.state.data.linearId == this.yachtLinearId }[0]
            val yachtState = filteredYachtStateAndRef.state.data

            // Check that the owner of the respective Yacht State is the Party initialising this flow
            if (yachtState.owner != ourIdentity){
                throw FlowException("You are not permitted to initiate a Purchase Yacht Flow for this Yacht.")
            } else {
                // Use the withNewOwner() of the Ownable state, get the command and the output state which will be used in the transaction
                val commandAndState = yachtState.withNewOwner(newOwner)

                // Obtain a reference from a notary and create the transaction builder
                val notary = serviceHub.networkMapCache.getNotary(CordaX500Name.parse("O=Notary,L=London,C=GB"))
                val txBuilder = TransactionBuilder(notary)

                // Initiate a flow session with the buyer to send the price and transfer of the fiat currency
                val counterpartySession = initiateFlow(newOwner)
                counterpartySession.send(yachtState.price)

                val inputs = subFlow(ReceiveStateAndRefFlow<FungibleToken>(counterpartySession))

                // Receive output for the fiat currency from the buyer
                val moneyReceived: List<FungibleToken> = counterpartySession.receive<List<FungibleToken>>().unwrap { it }

                // Create a fiat currency proposal for the Yacht
                addMoveTokens(txBuilder, inputs, moneyReceived)

                txBuilder.addInputState(filteredYachtStateAndRef)
                    .addOutputState(commandAndState.ownableState)
                    .addCommand(commandAndState.command, listOf(ourIdentity.owningKey))

                // Sign the transaction with ourIdentity's private keys
                val initialSignedTx = serviceHub.signInitialTransaction(txBuilder)

                /* Call the CollectSignaturesFlow to receive signature of the buyer */
                val ftx= subFlow(CollectSignaturesFlow(initialSignedTx, listOf(counterpartySession)))

                /* Call finality flow to notarise the transaction */
                val stx = subFlow(FinalityFlow(ftx, listOf(counterpartySession)))

                /* Distribution list is a list of identities that should receive updates. For this mechanism to behave correctly we call the UpdateDistributionListFlow flow */
                subFlow(UpdateDistributionListFlow(stx))

                return ("\nThe yacht is sold to " + newOwner + "\nTransaction ID: " + stx.id)
                }
            }
        }
        @InitiatedBy(Initiator::class)
        class Responder(val counterpartySession: FlowSession): FlowLogic<SignedTransaction>(){
            @Suspendable
            override fun call():SignedTransaction {

                // Receive price of the yacht
                val price = counterpartySession.receive<Amount<Currency>>().unwrap { it }

                // Create an instance of the fiat currency token amount
                val priceToken = Amount(price.quantity, getInstance(price.token.currencyCode))

                // Generate the move proposal - it returns the input-output pair for the fiat currency transfer, which we need to send to the Initiator
                val inputsAndOutputs : Pair<List<StateAndRef<FungibleToken>>, List<FungibleToken>> =
                    DatabaseTokenSelection(serviceHub).generateMove(listOf(Pair(counterpartySession.counterparty,priceToken)),ourIdentity)

                /* Call SendStateAndRefFlow to send the inputs to the Initiator*/
                subFlow(SendStateAndRefFlow(counterpartySession, inputsAndOutputs.first))
                /* Send the output generated from the fiat currency move proposal to the initiator */
                counterpartySession.send(inputsAndOutputs.second)

                // Sign the transaction
                subFlow(object : SignTransactionFlow(counterpartySession) {
                    @Throws(FlowException::class)
                    override fun checkTransaction(stx: SignedTransaction) {
                    }
                })
                return subFlow(ReceiveFinalityFlow(counterpartySession))
            }
        }

}

