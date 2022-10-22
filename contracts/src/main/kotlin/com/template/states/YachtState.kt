package com.template.states

import com.template.contracts.YachtContract
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import net.corda.core.identity.Party
import java.util.*

// *********
// * State *
// *********
@BelongsToContract(YachtContract::class)

data class YachtState(
    override val owner: AbstractParty,
    val price: Amount<Currency>,
    val forSale: Boolean,
    override val linearId: UniqueIdentifier,
    override val participants: List<AbstractParty> = listOf(owner)
) : OwnableState, LinearState {
    override fun withNewOwner(newOwner: AbstractParty): CommandAndState {
        return CommandAndState(
            YachtContract.Commands.Purchase(),
            YachtState(
                newOwner,
                this.price,
                this.forSale,
                this.linearId,
                listOf(this.owner, newOwner)
            )
        )
    }
}

