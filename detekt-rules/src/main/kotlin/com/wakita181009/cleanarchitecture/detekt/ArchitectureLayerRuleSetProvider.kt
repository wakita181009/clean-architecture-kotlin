package com.wakita181009.cleanarchitecture.detekt

import dev.detekt.api.Config
import dev.detekt.api.RuleName
import dev.detekt.api.RuleSet
import dev.detekt.api.RuleSetId
import dev.detekt.api.RuleSetProvider

class ArchitectureLayerRuleSetProvider : RuleSetProvider {

    override val ruleSetId = RuleSetId("architecture-layer-rules")

    override fun instance(): RuleSet =
        RuleSet(
            ruleSetId,
            mapOf(
                RuleName("NoThrowOutsidePresentation") to { config: Config ->
                    NoThrowOutsidePresentationRule(config)
                },
                RuleName("ForbiddenLayerImport") to { config: Config ->
                    ForbiddenLayerImportRule(config)
                },
            ),
        )
}
