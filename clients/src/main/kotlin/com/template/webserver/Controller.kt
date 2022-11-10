package net.corda.samples.example.webserver

import com.template.states.YachtState
import net.corda.core.contracts.StateAndRef
import net.corda.core.identity.CordaX500Name
import net.corda.core.messaging.vaultQueryBy
import org.slf4j.LoggerFactory
import org.springframework.context.annotation.Bean
import org.springframework.http.MediaType
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import net.corda.client.jackson.JacksonSupport
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter
import com.r3.corda.lib.tokens.contracts.states.FungibleToken

val SERVICE_NAMES = listOf("Notary", "Network Map Service")

/**
 * Define your API endpoints here.
 */
@RestController
@RequestMapping("/") // The paths for HTTP requests are relative to this base path.
class Controller(rpc: NodeRPCConnection) {


    companion object {
        private val logger = LoggerFactory.getLogger(RestController::class.java)
    }
    @Bean
    open fun mappingJackson2HttpMessageConverter(@Autowired rpcConnection: NodeRPCConnection): MappingJackson2HttpMessageConverter {
        val mapper = JacksonSupport.createDefaultMapper(rpcConnection.proxy)
        val converter = MappingJackson2HttpMessageConverter()
        converter.objectMapper = mapper
        return converter
    }


    private val myLegalName = rpc.proxy.nodeInfo().legalIdentities.first().name
    private val proxy = rpc.proxy

    /**
     * Returns the node's name.
     */
    @GetMapping(value = ["me"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun whoami() = mapOf("me" to myLegalName)

    /**
     * Returns all parties registered with the network map service. These names can be used to look up identities using
     * the identity service.
     */
    @GetMapping(value = ["peers"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getPeers(): Map<String, List<CordaX500Name>> {
        val nodeInfo = proxy.networkMapSnapshot()
        return mapOf("peers" to nodeInfo
            .map { it.legalIdentities.first().name }
            //filter out myself, notary and eventual network map started by driver
            .filter { it.organisation !in (SERVICE_NAMES + myLegalName.organisation) })
    }

    /**
     * Displays all Yacht states that exist in the node's vault.
     */
    @GetMapping(value = ["yachts"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getYachts() : ResponseEntity<List<StateAndRef<YachtState>>> {
        return ResponseEntity.ok(proxy.vaultQueryBy<YachtState>().states)
    }

    /**
     * Displays all the Token states that exist in the node's vault.
     */
    @GetMapping(value = ["tokens"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getTokens() : ResponseEntity<List<StateAndRef<FungibleToken>>>{
        return ResponseEntity.ok(proxy.vaultQueryBy<FungibleToken>().states)
    }

    /**
     * Displays all Yacht states that only this node has been involved in.
     */
    @GetMapping(value = ["my-yachts"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getMyYachtss(): ResponseEntity<List<StateAndRef<YachtState>>> {
        val myYachts = proxy.vaultQueryBy<YachtState>().states.filter { it.state.data.owner == (proxy.nodeInfo().legalIdentities.first()) }
        return ResponseEntity.ok(myYachts)
    }


    /**
     * Displays all the Token states that this node is a holder of.
     */
    @GetMapping(value = ["my-tokens"], produces = [MediaType.APPLICATION_JSON_VALUE])
    fun getMyTokens() : ResponseEntity<List<StateAndRef<FungibleToken>>>{
        val myTokens = proxy.vaultQueryBy<FungibleToken>().states.filter { it.state.data.holder == (proxy.nodeInfo().legalIdentities.first()) }
        return ResponseEntity.ok(myTokens)
    }
}
