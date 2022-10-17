//package com.template.flows
//
//import co.paralleluniverse.fibers.Suspendable
//import net.corda.core.flows.*
//import net.corda.core.identity.*
//import net.corda.core.utilities.ProgressTracker
//import net.corda.core.flows.FinalityFlow
//
//import net.corda.core.transactions.SignedTransaction
//
//import com.template.contracts.YachtContract
//
//import net.corda.core.transactions.TransactionBuilder
//
//import com.template.states.YachtState
//import net.corda.core.contracts.Amount
//import net.corda.core.contracts.UniqueIdentifier
//import java.util.Currency
//import java.util.Date
//
//
//// *********
//// * Flows *
//// *********
//@InitiatingFlow
//@StartableByRPC
//class CreateYachtFlow(private val name: String,
//                      private val type: String,
//                      private val length: Int,
//                      private val beam: Int,
//                      private val builderName: String,
//                      private val yearOfManufacture: Date,
//                      private val grossTonnage: Int,
//                      private val maxSpeed: Int,
//                      private val cruiseSpeed: Int,
//                      private val imageUrls: List<String>,
//                      private val price: Amount<Currency>,
//                      private val forSale: Boolean
//                      ) : FlowLogic<SignedTransaction>() {
//    override val progressTracker = ProgressTracker()
//
//    @Suspendable
//    override fun call(): SignedTransaction {
//        //Hello World message
//
//        // Step 1. Get a reference to the notary service on our network and our key pair.
//        // Note: ongoing work to support multiple notary identities is still in progress.
//        val notary = serviceHub.networkMapCache.getNotary( CordaX500Name.parse("O=Notary,L=London,C=GB"))
//
//        //Compose the State that carries the Hello World message
//        val output = YachtState(ourIdentity, name, type, length, beam, builderName, yearOfManufacture,grossTonnage, maxSpeed, cruiseSpeed, imageUrls, price, forSale, UniqueIdentifier(),listOf(ourIdentity))
//
//        // Step 3. Create a new TransactionBuilder object.
//        val builder = TransactionBuilder(notary)
//                .addCommand(YachtContract.Commands.Create(), listOf(ourIdentity.owningKey))
//                .addOutputState(output)
//
//        // Step 4. Verify and sign it with our KeyPair.
//        builder.verify(serviceHub)
//        // Sign the transaction
//        val stx = serviceHub.signInitialTransaction(builder)
//
//        // Notarise the transaction and record the state in the ledger
//        return subFlow(FinalityFlow(stx, listOf()))
//    }
//}
