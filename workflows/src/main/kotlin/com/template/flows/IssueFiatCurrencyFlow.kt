package com.template.flows

import co.paralleluniverse.fibers.Suspendable
import com.r3.corda.lib.tokens.contracts.states.FungibleToken
import com.r3.corda.lib.tokens.contracts.utilities.issuedBy
import net.corda.core.flows.FlowLogic
import net.corda.core.flows.StartableByRPC
import net.corda.core.identity.Party
import net.corda.core.utilities.ProgressTracker
import com.r3.corda.lib.tokens.money.FiatCurrency
import com.r3.corda.lib.tokens.workflows.flows.rpc.IssueTokens
import net.corda.core.contracts.Amount


// *********
// * Flows *
// *********

@StartableByRPC
class IssueFiatCurrencyFlow(
    private val currency: String,
    private val amount: Long,
    private val recipient: Party) : FlowLogic<String>() {
    override val progressTracker = ProgressTracker()

    @Suspendable
    override fun call():String {
        /* Create an instance of the fiat currency token */
        val token = FiatCurrency.Companion.getInstance(currency)

        /* Create an instance of IssuedTokenType for the fiat currency */
        val issuedTokenType = token issuedBy ourIdentity

        /* Create an instance of FungibleToken for the fiat currency to be issued */
        val fungibleToken = FungibleToken(Amount(amount,issuedTokenType),recipient)

        val stx = subFlow(IssueTokens(listOf(fungibleToken), listOf(recipient)))
        return "Issued $amount $currency token(s) to ${recipient.name.organisation}"
    }
}