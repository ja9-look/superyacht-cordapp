package com.template

import com.template.flows.CreateYachtFlow
import net.corda.testing.node.*
import org.junit.After
import org.junit.Before
import org.junit.Test
import com.template.states.YachtState
import java.util.concurrent.Future;
import net.corda.core.node.services.vault.QueryCriteria
import net.corda.core.transactions.SignedTransaction
import net.corda.core.contracts.Amount
import net.corda.core.contracts.UniqueIdentifier
import net.corda.core.identity.CordaX500Name
import net.corda.core.node.services.Vault.StateStatus
import net.corda.testing.core.TestIdentity
import org.junit.Assert
import java.math.BigDecimal
import java.util.Currency
import java.util.Date
import java.util.concurrent.ExecutionException


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
        network.runNetwork()
    }
    private val name = "World Traveller"
    private val type = "Motor Yacht"
    private val length = 80
    private val beam = 20
    private val builderName = "Burgess"
    private val yearOfManufacture = Date(2018)
    private val grossTonnage = 30
    private val maxSpeed = 15
    private val cruiseSpeed = 12
    private val imageUrls = listOf("https://images.unsplash.com/photo-1528154291023-a6525fabe5b4?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=1064&q=80", "https://images.unsplash.com/photo-1567899378494-47b22a2ae96a?ixlib=rb-1.2.1&ixid=MnwxMjA3fDB8MHxwaG90by1wYWdlfHx8fGVufDB8fHx8&auto=format&fit=crop&w=2340&q=80")
    private val price = Amount(6000000, BigDecimal("1"), Currency.getInstance("USD"))
    private val forSale = true
    @After
    fun tearDown() {
        network.stopNodes()
    }

    @Test
    @Throws (ExecutionException::class, InterruptedException::class)
    fun createYachtFlowSuccessfullyCreateAYachtStateAndRecordsTheStateInTheLedger(){
        val flow = CreateYachtFlow(name, type, length, beam, builderName, yearOfManufacture,grossTonnage, maxSpeed, cruiseSpeed, imageUrls, price, forSale)
        val future = a.startFlow(flow)
        network.runNetwork()
        val signedTransaction = future.get()
        val yacht = signedTransaction.tx.getOutput(0) as YachtState
        Assert.assertNotNull(yacht)
    }
}