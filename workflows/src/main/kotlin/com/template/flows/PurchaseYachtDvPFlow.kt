//package com.template.flows
//
//import co.paralleluniverse.fibers.Suspendable
//import net.corda.core.flows.*
//import net.corda.core.identity.*
//import net.corda.core.utilities.ProgressTracker
//
//import net.corda.core.transactions.SignedTransaction
//import net.corda.core.transactions.TransactionBuilder
//import net.corda.finance.workflows.asset.CashUtils
//
//import com.template.states.YachtState
//import net.corda.core.contracts.Amount
//import net.corda.core.contracts.UniqueIdentifier
//import net.corda.core.identity.AbstractParty
//import net.corda.core.node.services.queryBy
//import java.util.Currency
//
//
//// *********
//// * Flows *
//// *********
//@InitiatingFlow
//@StartableByRPC
//class PurchaseYachtDvPFlow(private val newOwner: AbstractParty,
//                           private val yachtId: UniqueIdentifier,
//                           private val price: Amount<Currency>): FlowLogic<SignedTransaction>() {
//    override val progressTracker = ProgressTracker()
//
//    @Suspendable
//    override fun call():SignedTransaction {
//        // Query to vault to find the corresponding YachtState - this YachtState will be used as input to the transaction.
//        val yachtStateAndRefs = serviceHub.vaultService.queryBy<YachtState>().states
//        val filteredYachtStateAndRef = yachtStateAndRefs.filter{ it.state.data.linearId == this.yachtId }[0]
//        val yachtState = filteredYachtStateAndRef.state.data
//
//        // Use the withNewOwner() of the Ownable state, get the command and the output state which will be used in the transaction
//        val commandAndState = yachtState.withNewOwner(newOwner)
//
//        // Create the transaction builder
//
//        // Obtain a reference from a notary
//        val notary = serviceHub.networkMapCache.getNotary(CordaX500Name.parse("0=Notary, L=London, C=GB"))
//
//        val txBuilderWithNotary = TransactionBuilder(notary)
//
//        // Generate Spend for the Cash
//         val txAndKeysPair = CashUtils.generateSpend(
//             serviceHub, txBuilderWithNotary,
//             price, ourIdentityAndCert,
//             yachtState.owner!!, emptySet()
//         )
//
//        val txBuilder = txAndKeysPair.first
//
//        txBuilder.addInputState(filteredYachtStateAndRef)
//                 .addOutputState(commandAndState.ownableState)
//                 .addCommand(commandAndState.command, listOf(yachtState.owner.owningKey, newOwner.owningKey))
//
//        txBuilder.verify(serviceHub)
//
//        // Sign the transaction with new keyPair generated for Cash and the node's owningKey
//        val keysToSign = txAndKeysPair.second.plus(ourIdentity.owningKey)
//        val stx = serviceHub.signInitialTransaction(txBuilder, keysToSign)
//
//        // Collect counterparty signature
//        val buyerFlow = initiateFlow(yachtState.owner!!)
//        val ftx = subFlow(CollectSignaturesFlow(stx, listOf(buyerFlow)))
//        return subFlow(FinalityFlow(ftx, (buyerFlow)))
//    }
//}
//
//@InitiatedBy(PurchaseYachtDvPFlow::class)
//class PurchaseYachtDvPFlowResponder(val counterpartySession: FlowSession): FlowLogic<SignedTransaction>(){
//    @Suspendable
//    override fun call():SignedTransaction {
//        subFlow(object : SignTransactionFlow(counterpartySession) {
//            @Throws(FlowException::class)
//            override fun checkTransaction(stx: SignedTransaction) {
//            }
//        })
//        return subFlow(ReceiveFinalityFlow(counterpartySession))
//    }
//}