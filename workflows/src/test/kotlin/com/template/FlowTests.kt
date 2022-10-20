package com.template

import com.template.flows.GetYachtRefFlow
import com.template.flows.IssueYachtRefFlow
import com.template.states.YachtRef
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
    private lateinit var a: StartedMockNode
    private lateinit var b: StartedMockNode
    private lateinit var c: StartedMockNode



    @Before
    fun setup() {
        network = MockNetwork(MockNetworkParameters(cordappsForAllNodes = listOf(
                TestCordapp.findCordapp("com.template.contracts"),
                TestCordapp.findCordapp("com.template.flows")
        ),
            notarySpecs = listOf(MockNetworkNotarySpec(CordaX500Name("Notary","London","GB")))))
        a = network.createPartyNode()
        b = network.createPartyNode()
        c = network.createPartyNode()

        // For real nodes this happens automatically, but we have to manually register the flow for tests.
        listOf(a, b, c).forEach { it.registerInitiatedFlow(IssueYachtRefFlow.Responder::class.java) }
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

    /* ISSUE YACHT REF FLOW TESTS */

    @Test
    fun issueYachtRefFlowIssuesYachtRefWithExpectedIssuerAndOwner() {
        val flow = IssueYachtRefFlow.Initiator(c.info.legalIdentities.first(), name, type, length, beam, builderName, yearOfBuild, grossTonnage, maxSpeed, cruiseSpeed, imageUrls, UniqueIdentifier())
        val future = a.startFlow(flow)
        network.runNetwork()

        Assert.assertEquals(a.info.legalIdentities.first(), future.get().state.data.issuer)
        Assert.assertEquals(c.info.legalIdentities.first(), future.get().state.data.owner)
    }

    @Test
    fun issueYachtRefFlowIssuesOutputOfExpectedTypeYachtRef() {
        val flow = IssueYachtRefFlow.Initiator(c.info.legalIdentities.first(), name, type, length, beam, builderName, yearOfBuild, grossTonnage, maxSpeed, cruiseSpeed, imageUrls, UniqueIdentifier())
        val future = a.startFlow(flow)
        network.runNetwork()

        Assert.assertTrue(future.get().state.data is YachtRef)
    }

    /* GET YACHT REF TESTS */

    @Test
    fun getYachtRefFlowFetchesTheLatestYachtRef(){
        val issueFlow = IssueYachtRefFlow.Initiator(b.info.legalIdentities.first(), name, type, length, beam, builderName, yearOfBuild, grossTonnage, maxSpeed, cruiseSpeed, imageUrls, UniqueIdentifier())
        val issueYachtRefFuture = a.startFlow(issueFlow)
        network.runNetwork()
        Assert.assertEquals(a.info.legalIdentities.first(), issueYachtRefFuture.get().state.data.issuer)

        val getFlow = GetYachtRefFlow.Initiator(b.info.legalIdentities.first(), issueYachtRefFuture.get().state.data.linearId)
        val getYachtRefFuture = b.startFlow(getFlow)
        network.runNetwork()

        Assert.assertTrue(getYachtRefFuture.get().single().state.data == issueYachtRefFuture.get().state.data)
    }

}