package com.wakita181009.cleanarchitecture.presentation.graphql

import com.netflix.graphql.dgs.DgsComponent
import com.netflix.graphql.dgs.DgsRuntimeWiring
import graphql.scalars.ExtendedScalars
import graphql.schema.idl.RuntimeWiring

@DgsComponent
class ScalarConfig {
    @DgsRuntimeWiring
    fun addScalar(builder: RuntimeWiring.Builder): RuntimeWiring.Builder =
        builder
            .scalar(ExtendedScalars.GraphQLLong)
            .scalar(ExtendedScalars.DateTime)
}
