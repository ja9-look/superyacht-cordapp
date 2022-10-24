package com.template

import com.template.flows.CreateYachtStateFlow
import com.template.flows.IssueYachtRefFlow
import com.template.flows.PurchaseYachtDvPFlow
import com.template.states.YachtRef
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
                TestCordapp.findCordapp("com.template.flows")
        ),
            notarySpecs = listOf(MockNetworkNotarySpec(CordaX500Name("Notary","London","GB")))))
        yachtIssuer = network.createPartyNode()
        yachtOwner1 = network.createPartyNode()
        yachtOwner2 = network.createPartyNode()
        bank = network.createPartyNode()

        // For real nodes this happens automatically, but we have to manually register the flow for tests.
        listOf(yachtIssuer, yachtOwner1, yachtOwner2, bank).forEach { it.registerInitiatedFlow(IssueYachtRefFlow.Responder::class.java) }
        network.runNetwork()
    }

    private val name = "World Traveller"
    private val type = "Motor Yacht"
    private val length = 12.15
    private val beam = 3.90
    private val builderName = "Burgess"
    private val yearOfBuild = Date(2018)
    private val grossTonnage = 17.06
    private val maxSpeed = 15
    private val cruiseSpeed = 12
    private val imageUrls = listOf("https://images.unsplash.com/photo-1528154291023-a6525fabe5b4?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1064&q=80", "https://images.unsplash.com/photo-1567899378494-47b22a2ae96a?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=2340&q=80")
    private val price = Amount(6000000, BigDecimal("1"), Currency.getInstance("USD"))
    private val forSale = true
    @After
    fun tearDown() {
        network.stopNodes()
    }

    /* ISSUE YACHT REF FLOW */

    @Test
    fun issueYachtRefFlowIssuesYachtRefWithExpectedIssuerAndOwner() {
        val flow = IssueYachtRefFlow.Initiator(yachtOwner2.info.legalIdentities.first(), name, type, length, beam, builderName, yearOfBuild, grossTonnage, maxSpeed, cruiseSpeed, imageUrls, UniqueIdentifier())
        val future = yachtIssuer.startFlow(flow)
        network.runNetwork()

        Assert.assertEquals(yachtIssuer.info.legalIdentities.first(), future.get().state.data.issuer)
        Assert.assertEquals(yachtOwner2.info.legalIdentities.first(), future.get().state.data.owner)
    }

    @Test
    fun issueYachtRefFlowIssuesOutputOfExpectedTypeYachtRef() {
        val flow = IssueYachtRefFlow.Initiator(yachtOwner2.info.legalIdentities.first(), name, type, length, beam, builderName, yearOfBuild, grossTonnage, maxSpeed, cruiseSpeed, imageUrls, UniqueIdentifier())
        val future = yachtIssuer.startFlow(flow)
        network.runNetwork()

        Assert.assertTrue(future.get().state.data is YachtRef)
    }

    /* CREATE YACHT STATE FLOW */
    @Test
    fun createYachtStateFlowCreateYachtStateSuccessfullyWithExpectedOwnerPriceForSaleAndLinearId(){
        val issueFlow = IssueYachtRefFlow.Initiator(yachtOwner1.info.legalIdentities.first(), name, type, length, beam, builderName, yearOfBuild, grossTonnage, maxSpeed, cruiseSpeed, imageUrls, UniqueIdentifier())
        val issueYachtRefFuture = yachtIssuer.startFlow(issueFlow)
        network.runNetwork()
        Assert.assertEquals(yachtIssuer.info.legalIdentities.first(), issueYachtRefFuture.get().state.data.issuer)

        val createYachtStateFlow = CreateYachtStateFlow.Initiator(yachtOwner1.info.legalIdentities.first(), price, forSale, issueYachtRefFuture.get().state.data.linearId)
        val createYachtStateFuture = yachtOwner1.startFlow(createYachtStateFlow)
        network.runNetwork()

        val issuedYachtRefData = issueYachtRefFuture.get().state.data
        val createdYachtStateData = createYachtStateFuture.get().tx.getOutput(0) as YachtState

        Assert.assertEquals(issuedYachtRefData.owner, createdYachtStateData.owner)
        Assert.assertEquals(price, createdYachtStateData.price)
        Assert.assertEquals(forSale, createdYachtStateData.forSale)
        Assert.assertEquals(issuedYachtRefData.linearId, createdYachtStateData.linearId)
    }

    /* PURCHASE YACHT STATE FLOW */
//    @Test
//    fun purchaseYachtDvPFlowUpdatesTheYachtStateWithNewOwner(){
//        // Issue Yacht Ref
//        val issueFlow = IssueYachtRefFlow.Initiator(yachtOwner1.info.legalIdentities.first(), name, type, length, beam, builderName, yearOfBuild, grossTonnage, maxSpeed, cruiseSpeed, imageUrls, UniqueIdentifier())
//        val issueYachtRefFuture = yachtIssuer.startFlow(issueFlow)
//        network.runNetwork()
//
//        val issuedYachtRefData = issueYachtRefFuture.get().state.data
//        Assert.assertEquals(yachtIssuer.info.legalIdentities.first(), issuedYachtRefData.issuer)
//
//        // Create Yacht State
//        val createYachtStateFlow = CreateYachtStateFlow.Initiator(yachtOwner1.info.legalIdentities.first(), price, forSale, issuedYachtRefData.linearId)
//        val createYachtStateFuture = yachtOwner1.startFlow(createYachtStateFlow)
//        network.runNetwork()
//
//        val createdYachtStateData = createYachtStateFuture.get().tx.getOutput(0) as YachtState
//        Assert.assertEquals(issueYachtRefFuture.get().state.data.owner, createdYachtStateData.owner)
//
//        // Purchase Yacht State
//
//        val purchaseYachtStateFlow = PurchaseYachtDvPFlow.Initiator(yachtOwner2.info.legalIdentities.first(), createdYachtStateData.linearId, createdYachtStateData.price)
//        val purchaseYachtDvPFlowFuture = yachtOwner1.startFlow(purchaseYachtStateFlow)
//        network.runNetwork()
//
//        val purchasedYachtDvPData = purchaseYachtDvPFlowFuture.get().tx.getOutput(0) as YachtState
//
//        // Check that the new owner is correct
//        Assert.assertEquals(yachtOwner2.info.legalIdentities.first(), purchasedYachtDvPData.owner)
//
//    }
}