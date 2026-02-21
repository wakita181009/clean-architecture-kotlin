package com.wakita181009.cleanarchitecture.detekt

import dev.detekt.api.Config
import dev.detekt.api.Entity
import dev.detekt.api.Finding
import dev.detekt.api.Rule
import org.jetbrains.kotlin.psi.KtImportDirective
import java.net.URI

/**
 * Enforces import whitelist in domain and application layers.
 *
 * Only explicitly allowed libraries may be imported. Any import not matching
 * the whitelist is rejected, preventing accidental framework or utility library usage.
 *
 * Cross-layer import violations are already prevented by Gradle module dependencies.
 *
 * Allowed in both layers:
 * - kotlin.*, java.*  (stdlib)
 * - arrow.core.*, arrow.fx.coroutines.*
 * - kotlinx.coroutines.*
 * - org.slf4j.*
 * - own layer packages
 *
 * Application layer additionally allows:
 * - com.wakita181009.cleanarchitecture.domain.*
 */
class ForbiddenLayerImportRule(config: Config) : Rule(
    config,
    "Import not allowed in this layer. Only kotlin.*, java.*, arrow.core.*, arrow.fx.coroutines.*, kotlinx.coroutines.*, org.slf4j.*, and own layer packages are permitted.",
    URI("https://github.com/wakita181009/clean-architecture-kotlin"),
) {

    private val projectBase = "com.wakita181009.cleanarchitecture"

    private val commonAllowedPrefixes = listOf(
        "kotlin.",
        "java.",
        "arrow.core.",
        "arrow.fx.coroutines.",
        "kotlinx.coroutines.",
        "org.slf4j.",
    )

    override fun visitImportDirective(importDirective: KtImportDirective) {
        super.visitImportDirective(importDirective)

        val file = importDirective.containingKtFile
        if (file.virtualFilePath.contains("/test/")) return

        val packageName = file.packageFqName.asString()
        val importPath = importDirective.importedFqName?.asString() ?: return

        val layer = packageName.removePrefix("$projectBase.").substringBefore(".")
        val layerOwnPackages = when (layer) {
            "domain" -> listOf("$projectBase.domain.")
            "application" -> listOf("$projectBase.domain.", "$projectBase.application.")
            else -> return
        }

        val allowed = commonAllowedPrefixes + layerOwnPackages

        if (allowed.none { importPath.startsWith(it) }) {
            report(Finding(Entity.from(importDirective), "Import '$importPath' is not allowed in $layer layer."))
        }
    }
}
