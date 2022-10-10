package com.template.contracts

import com.template.states.YachtState
import net.corda.core.contracts.CommandData
import net.corda.core.contracts.Contract
import net.corda.core.contracts.requireSingleCommand
import net.corda.core.transactions.LedgerTransaction
import net.corda.core.contracts.requireThat
// ************
// * Contract *
// ************
class YachtContract : Contract {
    companion object {
        // Used to identify our contract when building a transaction.
        const val ID = "com.template.contracts.YachtContract"
    }

    // A transaction is valid if the verify() function of the contract of all the transaction's input and output states
    // does not throw an exception.
    override fun verify(tx: LedgerTransaction) {
        // Verification logic goes here.
        val command = tx.commands.requireSingleCommand<Commands>()
        val input = tx.inputsOfType<YachtState>().first()
        val output = tx.outputsOfType<YachtState>().first()
        when (command.value) {
            is Commands.Create -> requireThat {
                "No inputs should be consumed when creating a new YachtState.".using(tx.inputStates.isEmpty())
//                "There should only be one output when creating a new YachtState.".using(tx.outputStates.size == 1)
//                "The owner must be a signer".using(command.signers.containsAll(listOf(input.owner.owningKey)))
            }
//            is Commands.Purchase -> requireThat{
//
//            }
        }
    }

    // Used to indicate the transaction's intent.
    interface Commands : CommandData {
        class Create : Commands
        class Purchase : Commands
    }
}