package com.template.contracts

import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import net.corda.testing.node.MockServices
import net.corda.testing.node.ledger
import org.junit.Test
import com.template.states.YachtRef
import com.template.states.YachtState
import net.corda.core.contracts.Amount
import net.corda.core.contracts.Contract
import net.corda.core.contracts.UniqueIdentifier
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertTrue

class ContractTests {
    private val ledgerServices: MockServices = MockServices()

    private val boatIntl = TestIdentity(CordaX500Name("Boat International", "London", "GB") )
    private val alice = TestIdentity(CordaX500Name("Alice", "London", "GB") )
    private val bob = TestIdentity(CordaX500Name("Bob", "London", "GB") )

    private val mockYachtRef = YachtRef(
        boatIntl.party,
        alice.party,
        "World Traveller",
        "Motor Yacht",
        12.15,
        3.90,
        "Burgess",
        Date(2008),
        17.06,
        15,
        12,
        listOf("https://images.unsplash.com/photo-1528154291023-a6525fabe5b4?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1064&q=80", "https://images.unsplash.com/photo-1567899378494-47b22a2ae96a?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=2340&q=80"),
        UniqueIdentifier()
    )

    private val mockYachtState = YachtState(
        alice.party,
        Amount(6000000, BigDecimal("1"), Currency.getInstance("USD")),
        true,
        UniqueIdentifier(),
        listOf(alice.party)
    )

    private val mockYachtStateBob = YachtState(
        bob.party,
        Amount(6000000, BigDecimal("1"), Currency.getInstance("USD")),
        true,
        UniqueIdentifier(),
        listOf(bob.party)
    )

    private val mockYachtStateNotForSale = YachtState(
        bob.party,
        Amount(6000000, BigDecimal("1"), Currency.getInstance("USD")),
        true,
        UniqueIdentifier(),
        listOf(bob.party)
    )

    /* YACHT REF TESTS */

    @Test
    fun yachtRefContractImplementsContract(){
        assertTrue(YachtRefContract() is Contract)
    }

    // Tests for Create Command
    @Test
    fun yachtRefContractCreateCommandShouldHaveNoInputs() {
        ledgerServices.ledger {
            transaction {
                input(YachtRefContract.ID, mockYachtRef)
                output(YachtRefContract.ID, mockYachtRef)
                command(listOf(boatIntl.publicKey, alice.publicKey), YachtRefContract.Commands.Create())
                fails()
            }
            transaction {
                output(YachtRefContract.ID, mockYachtRef)
                command(listOf(boatIntl.publicKey, alice.publicKey), YachtRefContract.Commands.Create())
                verifies()
            }
        }
    }

    @Test
    fun yachtRefContractCreateCommandShouldOnlyHaveOneOutput(){
        ledgerServices.ledger{
            transaction{
                output(YachtRefContract.ID, mockYachtRef)
                output(YachtRefContract.ID, mockYachtRef)
                command(listOf(boatIntl.publicKey, alice.publicKey), YachtRefContract.Commands.Create())
                fails()
            }
            transaction{
                output(YachtRefContract.ID, mockYachtRef)
                command(listOf(boatIntl.publicKey, alice.publicKey), YachtRefContract.Commands.Create())
                verifies()
            }
        }
    }

    @Test
    fun yachtRefContractCreateCommandRequiresOneCommand(){
        ledgerServices.ledger{
            transaction{
                output(YachtRefContract.ID, mockYachtRef)
                command(listOf(boatIntl.publicKey, alice.publicKey), YachtRefContract.Commands.Create())
                command(listOf(boatIntl.publicKey, alice.publicKey), YachtRefContract.Commands.Create())
                fails()
            }
            transaction{
                output(YachtRefContract.ID, mockYachtRef)
                command(listOf(boatIntl.publicKey, alice.publicKey), YachtRefContract.Commands.Create())
                verifies()
            }
        }
    }

    @Test
    fun yachtRefContractCreateCommandMustHaveTheIssuerAndTheOwnerAsRequiredSigners(){
        ledgerServices.ledger{
            transaction{
                output(YachtRefContract.ID, mockYachtRef)
                command(listOf(alice.publicKey, alice.publicKey), YachtRefContract.Commands.Create())
                fails()
            }
            transaction{
                output(YachtRefContract.ID, mockYachtRef)
                command(listOf(bob.publicKey, bob.publicKey), YachtRefContract.Commands.Create())
                fails()
            }
            transaction{
                output(YachtRefContract.ID, mockYachtRef)
                command(listOf(boatIntl.publicKey, alice.publicKey), YachtRefContract.Commands.Create())
                verifies()
            }
        }
    }

    /* YACHT STATE TESTS */

    @Test
    fun yachtContractImplementsContract(){
        assertTrue(YachtContract() is Contract)
    }

    // Tests for Create Command
    @Test
    fun yachtContractCreateCommandShouldHaveNoInputs() {
        ledgerServices.ledger {
            transaction {
                input(YachtContract.ID, mockYachtState)
                output(YachtContract.ID, mockYachtState)
                command(listOf(alice.publicKey), YachtContract.Commands.Create())
                fails()
            }
            transaction {
                output(YachtContract.ID, mockYachtState)
                command(listOf(alice.publicKey), YachtContract.Commands.Create())
                verifies()
            }
        }
    }

    @Test
    fun yachtContractCreateCommandShouldOnlyHaveOneOutput(){
        ledgerServices.ledger{
            transaction{
                output(YachtContract.ID, mockYachtState)
                output(YachtContract.ID, mockYachtState)
                command(listOf(alice.publicKey), YachtContract.Commands.Create())
                fails()
            }
            transaction{
                output(YachtContract.ID, mockYachtState)
                command(listOf(alice.publicKey), YachtContract.Commands.Create())
                verifies()
            }
        }
    }

    @Test
    fun yachtContractCreateCommandMustHaveTheOwnerAsARequiredSigner(){
        ledgerServices.ledger{
            transaction{
                output(YachtContract.ID, mockYachtState)
                command(listOf(boatIntl.publicKey), YachtContract.Commands.Create())
                fails()
            }
            transaction{
                output(YachtContract.ID, mockYachtState)
                command(listOf(bob.publicKey), YachtContract.Commands.Create())
                fails()
            }
            transaction{
                output(YachtContract.ID, mockYachtState)
                command(listOf(alice.publicKey), YachtContract.Commands.Create())
                verifies()
            }
        }
    }
    // Tests for Purchase Command

    @Test
    fun yachtContractPurchaseCommandShouldOnlyHaveOneInput(){
        ledgerServices.ledger{
            transaction{
                output(YachtContract.ID, mockYachtStateBob)
                command(bob.publicKey, YachtContract.Commands.Purchase())
                fails()
            }
            transaction{
                input(YachtContract.ID, mockYachtState)
                output(YachtContract.ID, mockYachtStateBob)
                command(listOf(alice.publicKey, bob.publicKey), YachtContract.Commands.Purchase())
                verifies()
            }
        }
    }

    @Test
    fun yachtContractPurchaseCommandShouldOnlyHaveOneOutput(){
        ledgerServices.ledger{
            transaction{
                input(YachtContract.ID, mockYachtState)
                command(alice.publicKey, YachtContract.Commands.Purchase())
                fails()
            }
            transaction{
                input(YachtContract.ID, mockYachtState)
                output(YachtContract.ID, mockYachtStateBob)
                command(listOf(alice.publicKey, bob.publicKey), YachtContract.Commands.Purchase())
                verifies()
            }
        }
    }

    @Test
    fun yachtContractPurchaseCommandYachtMustBeMarkedForSale(){
        ledgerServices.ledger{
            transaction{
                input(YachtContract.ID, mockYachtStateNotForSale)
                output(YachtContract.ID, mockYachtStateNotForSale)
                command(listOf(alice.publicKey, bob.publicKey), YachtContract.Commands.Purchase())
                fails()
            }
            transaction{
                input(YachtContract.ID, mockYachtState)
                output(YachtContract.ID, mockYachtStateBob)
                command(listOf(alice.publicKey, bob.publicKey), YachtContract.Commands.Purchase())
                verifies()
            }
        }
    }

    @Test
    fun yachtContractPurchaseCommandTheSellerAndTheBuyerMustBeRequiredSigners(){
        ledgerServices.ledger{
            transaction{
                input(YachtContract.ID, mockYachtState)
                output(YachtContract.ID, mockYachtStateBob)
                command(listOf(bob.publicKey), YachtContract.Commands.Purchase())
                fails()
            }
            transaction{
                input(YachtContract.ID, mockYachtState)
                output(YachtContract.ID, mockYachtStateBob)
                command(listOf(alice.publicKey, bob.publicKey), YachtContract.Commands.Purchase())
                verifies()
            }
        }
    }

    @Test
    fun yachtContractPurchaseCommandTheSellerAndTheBuyerCannotBeTheSameEntity(){
        ledgerServices.ledger{
            transaction{
                input(YachtContract.ID, mockYachtState)
                output(YachtContract.ID, mockYachtState)
                command(listOf(alice.publicKey, alice.publicKey), YachtContract.Commands.Purchase())
                fails()
            }
            transaction{
                input(YachtContract.ID, mockYachtState)
                output(YachtContract.ID, mockYachtStateBob)
                command(listOf(alice.publicKey, bob.publicKey), YachtContract.Commands.Purchase())
                verifies()
            }
        }
    }

}
