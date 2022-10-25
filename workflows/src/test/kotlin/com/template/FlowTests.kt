package com.template

import com.template.flows.CreateAndIssueYachtStateFlow
import com.template.flows.IssueFiatCurrencyFlow
import com.template.flows.PurchaseYachtFlow
import com.template.states.YachtState
import net.corda.testing.node.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import net.corda.core.contracts.Amount
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import org.junit.Assert
import java.math.BigDecimal
import java.util.Currency
import java.util.Date


class FlowTests {
    private lateinit var network: MockNetwork
    private lateinit var yachtIssuer: StartedMockNode
    private lateinit var yachtOwner1: StartedMockNode
    private lateinit var yachtOwner2: StartedMockNode
    private lateinit var bank: StartedMockNode

    @Before
    fun setup() {
        network = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
                TestCordapp.findCordapp("com.template.contracts"),
                TestCordapp.findCordapp("com.template.flows"),
                TestCordapp.findCordapp("com.r3.corda.lib.tokens.contracts"),
                TestCordapp.findCordapp("com.r3.corda.lib.tokens.workflows")
        ),
            notarySpecs = listOf(MockNetworkNotarySpec(CordaX500Name("Notary","London","GB")))))
        yachtIssuer = network.createPartyNode()
        yachtOwner1 = network.createPartyNode()
        yachtOwner2 = network.createPartyNode()
        bank = network.createPartyNode()

        // For real nodes this happens automatically, but we have to manually register the flow for tests.
        listOf(yachtIssuer, yachtOwner1, yachtOwner2, bank).forEach { it.registerInitiatedFlow(
            CreateAndIssueYachtStateFlow.Responder::class.java) }
        network.runNetwork()
    }

    private val name = "World Traveller"
    private val type = "Motor Yacht"
    private val length = 12.15
    private val builderName = "Burgess"
    private val yearOfBuild = Date(2018)
    private val grossTonnage = 17.06
    private val maxSpeed = 15
    private val cruiseSpeed = 12
    private val imageUrls = listOf("https://images.unsplash.com/photo-1528154291023-a6525fabe5b4?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1064&q=80", "https://images.unsplash.com/photo-1567899378494-47b22a2ae96a?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=2340&q=80")
    private val price = Amount.parseCurrency("6000000 USD")
    private val forSale = true
    @After
    fun tearDown() {
        network.stopNodes()
    }

    /* CREATE YACHT STATE FLOW */
    @Test
    fun createYachtStateFlowCreateYachtStateSuccessfullyWithExpectedOwnerPriceForSaleAndLinearId(){

        val createYachtStateFlow = CreateAndIssueYachtStateFlow.Initiator(yachtOwner1.info.legalIdentities.first(), name, type, length, builderName, yearOfBuild, grossTonnage, maxSpeed, cruiseSpeed, imageUrls, price, forSale, UniqueIdentifier())
        val createYachtStateFuture = yachtIssuer.startFlow(createYachtStateFlow)
        network.runNetwork()

        val createdYachtStateData = createYachtStateFuture.get().tx.getOutput(0) as YachtState

        Assert.assertEquals(yachtIssuer.info.legalIdentities.first(), createdYachtStateData.issuer)
        Assert.assertEquals(yachtOwner1.info.legalIdentities.first(), createdYachtStateData.owner)
        Assert.assertEquals(price, createdYachtStateData.price)
        Assert.assertEquals(forSale, createdYachtStateData.forSale)
    }

    @Test
        fun createAndIssueYachtStateFlowCreatesOutputOfExpectedTypeYachtState() {
        val createYachtStateFlow = CreateAndIssueYachtStateFlow.Initiator(yachtOwner1.info.legalIdentities.first(), name, type, length, builderName, yearOfBuild, grossTonnage, maxSpeed, cruiseSpeed, imageUrls, price, forSale, UniqueIdentifier())
        val createYachtStateFuture = yachtIssuer.startFlow(createYachtStateFlow)
        network.runNetwork()

        Assert.assertTrue(createYachtStateFuture.get().tx.getOutput(0) is YachtState)
    }

    /* ISSUE FIAT CURRENCY FLOW */
    @Test
    fun issueFiatCurrencyFlowIssuesTheCorrectAmountOfTheCorrectCurrencyToTheRelevantParty(){
        val issueFiatCurrencyFlow = IssueFiatCurrencyFlow("USD", 6000000, yachtOwner1.info.legalIdentities.first())
        val issueFiatCurrencyFuture = bank.startFlow(issueFiatCurrencyFlow)
        network.runNetwork()

        val issuedFiatCurrencyData = issueFiatCurrencyFuture.get()
        Assert.assertEquals("Issued 6000000 USD token(s) to Mock Company 2", issuedFiatCurrencyData)
    }


    /* PURCHASE YACHT STATE FLOW */
    @Test
    fun purchaseYachtFlowUpdatesTheYachtStateWithNewOwner(){
        // Create Yacht State

        val createYachtStateFlow = CreateAndIssueYachtStateFlow.Initiator(yachtOwner1.info.legalIdentities.first(), name, type, length, builderName, yearOfBuild, grossTonnage, maxSpeed, cruiseSpeed, imageUrls, price, forSale, UniqueIdentifier())
        val createYachtStateFuture = yachtIssuer.startFlow(createYachtStateFlow)
        network.runNetwork()

        val createdYachtStateData = createYachtStateFuture.get().tx.getOutput(0) as YachtState

        Assert.assertEquals(yachtOwner1.info.legalIdentities.first(), createdYachtStateData.owner)

        // Issue Fiat Currency To Buyer
        val issueFiatCurrencyFlow = IssueFiatCurrencyFlow("USD", 6000000, yachtOwner1.info.legalIdentities.first())
        val issueFiatCurrencyFuture = bank.startFlow(issueFiatCurrencyFlow)
        network.runNetwork()

        val issuedFiatCurrencyData = issueFiatCurrencyFuture.get()
        Assert.assertEquals("Issued 6000000 USD token(s) to Mock Company 2", issuedFiatCurrencyData)

        // Purchase Yacht State

        val purchaseYachtStateFlow = PurchaseYachtFlow.Initiator(yachtOwner2.info.legalIdentities.first(), createdYachtStateData.linearId)
        val purchaseYachtDvPFlowFuture = yachtOwner1.startFlow(purchaseYachtStateFlow)
        network.runNetwork()

//        val purchasedYachtDvPData = purchaseYachtDvPFlowFuture.get()

        // Check that the new owner is correct
        Assert.assertEquals("", purchaseYachtDvPFlowFuture)
    }
}