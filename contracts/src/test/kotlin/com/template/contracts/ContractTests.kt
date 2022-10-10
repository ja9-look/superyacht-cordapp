package com.template.contracts

import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test
import com.template.states.YachtState
import net.corda.core.contracts.Amount
import net.corda.core.contracts.Contract
import net.corda.core.contracts.UniqueIdentifier
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertTrue

class ContractTests {
    private val ledgerServices: MockServices = MockServices(listOf("com.template"))
    private var alice = TestIdentity(CordaX500Name("Alice", "TestLand", "US"))
    private var bob = TestIdentity(CordaX500Name("Bob", "TestLand", "US"))

    private val mockYachtState = YachtState(alice.party,
        "World Traveller",
        "Motor Yacht",
        80,
        20,
        "Burgess",
        Date(2018),
        30,
        15,
        12,
        listOf("https://images.unsplash.com/photo-1528154291023-a6525fabe5b4?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1064&q=80", "https://images.unsplash.com/photo-1567899378494-47b22a2ae96a?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=2340&q=80"),
        Amount(6000000, BigDecimal("1"), Currency.getInstance("USD")),
        true,
        UniqueIdentifier(),
        listOf(alice.party)
    )

    private val mockYachtState1 = YachtState(bob.party,
        "World Traveller",
        "Motor Yacht",
        80,
        20,
        "Burgess",
        Date(2018),
        30,
        15,
        12,
        listOf("https://images.unsplash.com/photo-1528154291023-a6525fabe5b4?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1064&q=80", "https://images.unsplash.com/photo-1567899378494-47b22a2ae96a?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=2340&q=80"),
        Amount(6000000, BigDecimal("1"), Currency.getInstance("USD")),
        true,
        UniqueIdentifier(),
        listOf(alice.party, bob.party)
    )

    @Test
    fun yachtContractImplementsContract(){
        assertTrue(YachtContract() is Contract)
    }

    // Tests for Create Command //
    @Test
    fun yachtContractCreateCommandShouldHaveNoInputs() {
        ledgerServices.ledger {
//            transaction {
//                input(YachtContract.ID, mockYachtState)
//                output(YachtContract.ID, mockYachtState)
//                command(listOf(alice.publicKey), YachtContract.Commands.Create())
//                fails()
//            }
            transaction {
                output(YachtContract.ID, mockYachtState)
                command(listOf(alice.publicKey), YachtContract.Commands.Create())
                verifies()
            }
        }
    }
//
//    @Test
//    fun yachtContractCreateCommandShouldOnlyHaveOneOutput(){
//        ledgerServices.ledger{
//            transaction{
//                output(YachtContract.ID, mockYachtState)
//                output(YachtContract.ID, mockYachtState)
//                command(alice.publicKey, YachtContract.Commands.Create())
//                fails()
//            }
//            transaction{
//                output(YachtContract.ID, mockYachtState)
//                command(alice.publicKey, YachtContract.Commands.Create())
//                verifies()
//            }
//        }
//    }
//
//    @Test
//    fun yachtContractCreateCommandRequiresOneCommand(){
//        ledgerServices.ledger{
//            transaction{
//                output(YachtContract.ID, mockYachtState)
//                command(alice.publicKey, YachtContract.Commands.Create())
//                command(alice.publicKey, YachtContract.Commands.Create())
//                fails()
//            }
//            transaction{
//                output(YachtContract.ID, mockYachtState)
//                command(alice.publicKey, YachtContract.Commands.Create())
//                verifies()
//            }
//        }
//    }
//
//    @Test
//    fun yachtContractCreateCommandMustHaveTheOwnerAsARequiredSigner(){
//        ledgerServices.ledger{
//            transaction{
//                output(YachtContract.ID, mockYachtState)
//                command(bob.publicKey, YachtContract.Commands.Create())
//                fails()
//            }
//            transaction{
//                output(YachtContract.ID, mockYachtState)
//                command(alice.publicKey, YachtContract.Commands.Create())
//                verifies()
//            }
//        }
//    }

    // Tests for Purchase Command //
    // Only 1 input
    // Only 1 output
    // Requires the output to be a YachtState
    // Requires 1 command
    // Yacht must be marked as forSale
    // Owner and newOwner must be required signers
    // Owner and newOwner can't be the same entity
}
