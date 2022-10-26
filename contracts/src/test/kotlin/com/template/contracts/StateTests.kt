package com.template.contracts

import com.template.states.YachtState
import net.corda.core.contracts.Amount
import net.corda.core.contracts.ContractState
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.CordaX500Name
import net.corda.testing.core.TestIdentity
import org.junit.Test
import java.math.BigDecimal
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertTrue


class StateTests {

    private val boatIntl = TestIdentity(CordaX500Name("Boat International", "London", "GB") )
    private val alice = TestIdentity(CordaX500Name("Alice", "London", "GB") )
    private val bob = TestIdentity(CordaX500Name("Bob", "London", "GB") )

    private val mockYachtState = YachtState(
        boatIntl.party,
        alice.party,
        "World Traveller",
        "Motor Yacht",
        12.15,
        "Burgess",
        Date(2008),
        17.06,
        15,
        12,
        listOf("https://images.unsplash.com/photo-1528154291023-a6525fabe5b4?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1064&q=80", "https://images.unsplash.com/photo-1567899378494-47b22a2ae96a?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=2340&q=80"),
        Amount(6000000, BigDecimal("1"), Currency.getInstance("USD")),
        true,
        UniqueIdentifier(),
        listOf(alice.party)
    )

    private val mockYachtState1 = YachtState(
        boatIntl.party,
        bob.party,
        "World Traveller",
        "Motor Yacht",
        12.15,
        "Burgess",
        Date(2008),
        17.06,
        15,
        12,
        listOf("https://images.unsplash.com/photo-1528154291023-a6525fabe5b4?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1064&q=80", "https://images.unsplash.com/photo-1567899378494-47b22a2ae96a?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=2340&q=80"),
        Amount(6000000, BigDecimal("1"), Currency.getInstance("USD")),
        true,
        UniqueIdentifier(),
        listOf(bob.party)
    )

    /* YACHT STATE TESTS */

    @Test
    fun yachtStateIsAnInstanceOfContractState(){
        assertTrue(mockYachtState is ContractState)
        assertTrue(mockYachtState1 is ContractState)
    }

    @Test
    fun yachtStateHasTheCorrectFieldsOfTheCorrectTypeInConstructor() {
        YachtState::class.java.getDeclaredField("issuer")
        assertEquals(YachtState::class.java.getDeclaredField("issuer").type, AbstractParty::class.java)
        YachtState::class.java.getDeclaredField("owner")
        assertEquals(YachtState::class.java.getDeclaredField("owner").type, AbstractParty::class.java)
        YachtState::class.java.getDeclaredField("name")
        assertEquals(YachtState::class.java.getDeclaredField("name").type, String::class.java)
        YachtState::class.java.getDeclaredField("type")
        assertEquals(YachtState::class.java.getDeclaredField("type").type, String::class.java)
        YachtState::class.java.getDeclaredField("length")
        assertEquals(YachtState::class.java.getDeclaredField("length").type, Double::class.java)
        YachtState::class.java.getDeclaredField("builderName")
        assertEquals(YachtState::class.java.getDeclaredField("builderName").type, String::class.java)
        YachtState::class.java.getDeclaredField("yearOfBuild")
        assertEquals(YachtState::class.java.getDeclaredField("yearOfBuild").type, Date::class.java)
        YachtState::class.java.getDeclaredField("grossTonnage")
        assertEquals(YachtState::class.java.getDeclaredField("grossTonnage").type, Double::class.java)
        YachtState::class.java.getDeclaredField("maxSpeed")
        assertEquals(YachtState::class.java.getDeclaredField("maxSpeed").type, Int::class.java)
        YachtState::class.java.getDeclaredField("cruiseSpeed")
        assertEquals(YachtState::class.java.getDeclaredField("cruiseSpeed").type, Int::class.java)
        YachtState::class.java.getDeclaredField("imageUrls")
        assertEquals(YachtState::class.java.getDeclaredField("imageUrls").type, List::class.java)
        YachtState::class.java.getDeclaredField("linearId")
        assertEquals(YachtState::class.java.getDeclaredField("linearId").type, UniqueIdentifier::class.java)
    }
    @Test
    fun yachtStateAlwaysHasTheOwnerAsParticipants(){
        assertTrue(mockYachtState.participants.containsAll(listOf(alice.party)))
        assertTrue(mockYachtState1.participants.containsAll(listOf(bob.party)))
    }

}