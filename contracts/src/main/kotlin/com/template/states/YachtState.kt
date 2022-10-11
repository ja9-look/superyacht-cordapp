package com.template.states

import com.template.contracts.YachtContract
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import java.util.*

// *********
// * State *
// *********
@BelongsToContract(YachtContract::class)

data class YachtState(
    override val owner: AbstractParty,
    val name: String,
    val type: String,
    val length: Int,
    val beam: Int,
    val builderName: String,
    val yearOfManufacture: Date,
    val grossTonnage: Int,
    val maxSpeed: Int,
    val cruiseSpeed: Int,
    val imageUrls: List<String>,
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
                this.name,
                this.type,
                this.length,
                this.beam,
                this.builderName,
                this.yearOfManufacture,
                this.grossTonnage,
                this.maxSpeed,
                this.cruiseSpeed,
                this.imageUrls,
                this.price,
                this.forSale,
                this.linearId,
                listOf(this.owner, newOwner)
            )
        )
    }
}

