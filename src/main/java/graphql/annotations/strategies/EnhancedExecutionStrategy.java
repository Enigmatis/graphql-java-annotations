/**
 * Copyright 2016 Yurii Rashkovskii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package graphql.annotations.strategies;

import graphql.ExecutionResult;
import graphql.execution.*;
import graphql.language.*;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;

import java.util.HashMap;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class EnhancedExecutionStrategy extends AsyncSerialExecutionStrategy {

    private static final String CLIENT_MUTATION_ID = "clientMutationId";

    @Override
    protected CompletableFuture<ExecutionResult> resolveField(ExecutionContext executionContext, ExecutionStrategyParameters parameters) {
        GraphQLObjectType parentType = (GraphQLObjectType) parameters.getExecutionStepInfo().getUnwrappedNonNullType();
        GraphQLFieldDefinition fieldDef = getFieldDef(executionContext.getGraphQLSchema(), parentType, parameters.getField().getSingleField());
        if (fieldDef == null) return null;

        if (fieldDef.getName().contentEquals(CLIENT_MUTATION_ID)) {
            Field field = (Field) executionContext.getOperationDefinition().getSelectionSet().getSelections().get(0);
            Argument argument = field.getArguments().get(0);

            Object clientMutationId;
            if (argument.getValue() instanceof VariableReference) {
                VariableReference ref = (VariableReference) argument.getValue();
                HashMap mutationInputVariables = (HashMap) executionContext.getVariables().get(ref.getName());
                clientMutationId = mutationInputVariables.get(CLIENT_MUTATION_ID);
            } else {
                ObjectValue value = (ObjectValue) field.getArguments().get(0).getValue();
                StringValue clientMutationIdVal = (StringValue) value.getObjectFields().stream()
                        .filter(f -> f.getName().contentEquals(CLIENT_MUTATION_ID))
                        .findFirst().get().getValue();
                clientMutationId = clientMutationIdVal.getValue();
            }

            ExecutionStepInfo fieldTypeInfo = ExecutionStepInfo.newExecutionStepInfo().type(fieldDef.getType()).parentInfo(parameters.getExecutionStepInfo()).build();
            ExecutionStrategyParameters newParameters = ExecutionStrategyParameters.newParameters()
                    .arguments(parameters.getArguments())
                    .fields(parameters.getFields())
                    .nonNullFieldValidator(parameters.getNonNullFieldValidator())
                    .executionStepInfo(fieldTypeInfo)
                    .source(clientMutationId)
                    .build();


            return completeValue(executionContext, newParameters).getFieldValue();
        } else {
            return super.resolveField(executionContext, parameters);
        }
    }

    @Override
    protected FieldValueInfo completeValue(ExecutionContext executionContext, ExecutionStrategyParameters parameters) throws NonNullableFieldWasNullException {
        graphql.schema.GraphQLType fieldType = parameters.getExecutionStepInfo().getType();
        Object result = parameters.getSource();
        if (result instanceof Enum && fieldType instanceof GraphQLEnumType) {
            Object value = ((GraphQLEnumType) fieldType).parseValue(((Enum) result).name());
            return super.completeValue(executionContext, withSource(parameters, value));
        }
        if (result instanceof Optional) {
            Object value = ((Optional<?>) result).orElse(null);
            return completeValue(executionContext, withSource(parameters, value));
        }
        return super.completeValue(executionContext, parameters);
    }

    /*
      Creates a new parameters with the specified object as its source
     */
    private ExecutionStrategyParameters withSource(ExecutionStrategyParameters parameters, Object source) {
        return ExecutionStrategyParameters.newParameters()
                .arguments(parameters.getArguments())
                .fields(parameters.getFields())
                .nonNullFieldValidator(parameters.getNonNullFieldValidator())
                .executionStepInfo(parameters.getExecutionStepInfo())
                .source(source)
                .build();
    }

}
