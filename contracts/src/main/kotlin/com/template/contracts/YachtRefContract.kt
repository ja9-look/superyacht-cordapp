package com.template.contracts

import com.template.states.YachtRef
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.contracts.requireThat
// ************
// * Contract *
// ************
class YachtRefContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.contracts.YachtRefContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Verification logic goes here.
        val command = tx.commands.requireSingleCommand<Commands>()

        when (command.value) {
            is Commands.Create -> requireThat {
                "No inputs should be consumed when creating a new YachtRef.".using(tx.inputStates.isEmpty())
                "There should only be one output when creating a new YachtState.".using(tx.outputStates.size == 1)
                "The issuer and owner must be required signers".using(command.signers.containsAll(tx.outputsOfType<YachtRef>().single().participants.map {it.owningKey}))
            }
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Create : Commands
    }
}