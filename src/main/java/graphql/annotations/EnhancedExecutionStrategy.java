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
package graphql.annotations;

import graphql.ExecutionResult;
import graphql.execution.ExecutionContext;
import graphql.execution.ExecutionParameters;
import graphql.execution.SimpleExecutionStrategy;
import graphql.execution.TypeInfo;
import graphql.language.Argument;
import graphql.language.Field;
import graphql.language.ObjectValue;
import graphql.language.StringValue;
import graphql.language.VariableReference;
import graphql.schema.GraphQLEnumType;
import graphql.schema.GraphQLFieldDefinition;
import graphql.schema.GraphQLObjectType;
import graphql.schema.GraphQLType;

import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static graphql.execution.ExecutionParameters.newParameters;
import static graphql.execution.TypeInfoWorkaround.newTypeInfo;

public class EnhancedExecutionStrategy extends SimpleExecutionStrategy {

    private static final String CLIENT_MUTATION_ID = "clientMutationId";

    @Override
    protected ExecutionResult resolveField(ExecutionContext executionContext, ExecutionParameters parameters, List<Field> fields) {
        GraphQLObjectType parentType = (GraphQLObjectType) parameters.typeInfo().type();
        GraphQLFieldDefinition fieldDef = getFieldDef(executionContext.getGraphQLSchema(), parentType, fields.get(0));
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

            TypeInfo fieldTypeInfo = newTypeInfo(fieldDef.getType(), parameters.typeInfo());
            ExecutionParameters newParameters = newParameters()
                    .arguments(parameters.arguments())
                    .fields(parameters.fields())
                    .typeInfo(fieldTypeInfo)
                    .source(clientMutationId)
                    .build();


            return completeValue(executionContext, newParameters, fields);
        } else {
            return super.resolveField(executionContext, parameters, fields);
        }
    }

    @Override
    protected ExecutionResult completeValue(ExecutionContext executionContext, ExecutionParameters parameters, List<Field> fields) {
        GraphQLType fieldType = parameters.typeInfo().type();
        Object result = parameters.source();
        if (result instanceof Enum && fieldType instanceof GraphQLEnumType) {
            Object value = ((GraphQLEnumType) fieldType).getCoercing().parseValue(((Enum) result).name());
            return super.completeValue(executionContext, withSource(parameters, value), fields);
        }
        if (result instanceof Optional) {
            Object value = ((Optional<?>) result).orElse(null);
            return completeValue(executionContext, withSource(parameters, value), fields);
        }
        return super.completeValue(executionContext, parameters, fields);
    }

    /*
      Creates a new parameters with the specified object as its source
     */
    private ExecutionParameters withSource(ExecutionParameters parameters, Object source) {
        return newParameters()
                .arguments(parameters.arguments())
                .fields(parameters.fields())
                .typeInfo(parameters.typeInfo())
                .source(source)
                .build();
    }
}
