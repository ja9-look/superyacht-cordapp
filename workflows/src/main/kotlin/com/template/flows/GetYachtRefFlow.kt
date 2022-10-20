package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import net.corda.core.flows.*
import net.corda.core.identity.*
import com.template.states.YachtRef

import net.corda.core.contracts.StateAndRef
import net.corda.core.contracts.UniqueIdentifier
import java.util.Date

// *********
// * Flows *
// *********
class GetYachtRefFlow {

    @InitiatingFlow
    @StartableByRPC

    class Initiator(
        private val owner: AbstractParty,
        private val yachtLinearId: UniqueIdentifier
    ) : FlowLogic <List<StateAndRef<YachtRef>>>() {

        @Suspendable
        override fun call(): List<StateAndRef<YachtRef>> {
                val listYachtRefStateAndRefs = serviceHub.vaultService.queryBy(YachtRef::class.java).states
                if (listYachtRefStateAndRefs.isEmpty()){
                    return emptyList()
                } else {
                    listYachtRefStateAndRefs.filter{
                        it.state.data.owner == owner &&
                        it.state.data.linearId == yachtLinearId
                    }
                    return listYachtRefStateAndRefs
                }
        }

    }
}
