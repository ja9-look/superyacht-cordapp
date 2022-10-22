package com.template.states

//import com.template.contracts.YachtRefContract
import com.template.contracts.YachtRefContract
import net.corda.core.contracts.*
import net.corda.core.identity.AbstractParty
import java.util.*

// *********
// * State *
// *********
@BelongsToContract(YachtRefContract::class)
data class YachtRef(
    val issuer: AbstractParty,
    val owner: AbstractParty,
    val name: String,
    val type: String,
    val length: Double,
    val beam: Double,
    val builderName: String,
    val yearOfBuild: Date,
    val grossTonnage: Double,
    val maxSpeed: Int,
    val cruiseSpeed: Int,
    val imageUrls: List<String>,
    override val linearId: UniqueIdentifier
):
    LinearState {
    override val participants: List<AbstractParty> get() = listOf(issuer, owner)
    }

