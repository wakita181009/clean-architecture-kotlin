package com.wakita181009.cleanarchitecture.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtThrowExpression
import java.net.URI

/**
 * Forbids `throw` in domain, application, and infrastructure layers.
 *
 * All errors in these layers must be expressed as `Either<XxxError, T>`.
 * The return type alone must fully specify all possible errors —
 * throwing makes errors invisible to callers inspecting only the signature.
 *
 * Allowed: `throw` in presentation layer (e.g., ResponseStatusException for Spring).
 */
class NoThrowOutsidePresentationRule(config: Config) : Rule(
    config,
    "throw is forbidden in domain, application, and infrastructure layers. Return Either<XxxError, T> instead.",
    URI("https://github.com/wakita181009/clean-architecture-kotlin"),
) {

    private val projectBase = "com.wakita181009.cleanarchitecture"
    private val forbiddenLayers = setOf("domain", "application", "infrastructure")

    override fun visitThrowExpression(expression: KtThrowExpression) {
        super.visitThrowExpression(expression)

        val file = expression.containingKtFile
        if (file.virtualFilePath.contains("/test/")) return

        val packageName = file.packageFqName.asString()
        val layer = packageName.removePrefix("$projectBase.").substringBefore(".")

        if (layer in forbiddenLayers) {
            report(
                Finding(
                    Entity.from(expression),
                    "throw detected in package '$packageName'. Use Either<XxxError, T> to express errors instead of throwing.",
                ),
            )
        }
    }
}
