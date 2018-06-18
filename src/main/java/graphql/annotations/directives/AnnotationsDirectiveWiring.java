/**
 * Copyright 2016 Yurii Rashkovskii
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 */
package graphql.annotations.directives;

import graphql.schema.*;

public interface AnnotationsDirectiveWiring {
    /**
     * This is called when an object is encountered, which gives the schema directive a chance to modify the shape and behaviour
     * of that DSL  element
     *
     * @param environment the wiring element
     *
     * @return a non null element based on the original one
     */
    default GraphQLObjectType onObject(AnnotationsWiringEnvironment<GraphQLObjectType> environment) {
        return environment.getElement();
    }

    /**
     * This is called when a field is encountered, which gives the schema directive a chance to modify the shape and behaviour
     * of that DSL  element
     *
     * @param environment the wiring element
     *
     * @return a non null element based on the original one
     */
    default GraphQLFieldDefinition onField(AnnotationsWiringEnvironment<GraphQLFieldDefinition> environment) {
        return environment.getElement();
    }

    /**
     * This is called when an argument is encountered, which gives the schema directive a chance to modify the shape and behaviour
     * of that DSL  element
     *
     * @param environment the wiring element
     *
     * @return a non null element based on the original one
     */
    default GraphQLArgument onArgument(AnnotationsWiringEnvironment<GraphQLArgument> environment) {
        return environment.getElement();
    }

    /**
     * This is called when an interface is encountered, which gives the schema directive a chance to modify the shape and behaviour
     * of that DSL  element
     *
     * @param environment the wiring element
     *
     * @return a non null element based on the original one
     */
    default GraphQLInterfaceType onInterface(AnnotationsWiringEnvironment<GraphQLInterfaceType> environment) {
        return environment.getElement();
    }

    /**
     * This is called when a union is encountered, which gives the schema directive a chance to modify the shape and behaviour
     * of that DSL  element
     *
     * @param environment the wiring element
     *
     * @return a non null element based on the original one
     */
    default GraphQLUnionType onUnion(AnnotationsWiringEnvironment<GraphQLUnionType> environment) {
        return environment.getElement();
    }

    /**
     * This is called when an enum is encountered, which gives the schema directive a chance to modify the shape and behaviour
     * of that DSL  element
     *
     * @param environment the wiring element
     *
     * @return a non null element based on the original one
     */
    default GraphQLEnumType onEnum(AnnotationsWiringEnvironment<GraphQLEnumType> environment) {
        return environment.getElement();
    }

    /**
     * This is called when an enum value is encountered, which gives the schema directive a chance to modify the shape and behaviour
     * of that DSL  element
     *
     * @param environment the wiring element
     *
     * @return a non null element based on the original one
     */
    default GraphQLEnumValueDefinition onEnumValue(AnnotationsWiringEnvironment<GraphQLEnumValueDefinition> environment) {
        return environment.getElement();
    }

    /**
     * This is called when a custom scalar is encountered, which gives the schema directive a chance to modify the shape and behaviour
     * of that DSL  element
     *
     * @param environment the wiring element
     *
     * @return a non null element based on the original one
     */
    default GraphQLScalarType onScalar(AnnotationsWiringEnvironment<GraphQLScalarType> environment) {
        return environment.getElement();
    }

    /**
     * This is called when an input object is encountered, which gives the schema directive a chance to modify the shape and behaviour
     * of that DSL  element
     *
     * @param environment the wiring element
     *
     * @return a non null element based on the original one
     */
    default GraphQLInputObjectType onInputObjectType(AnnotationsWiringEnvironment<GraphQLInputObjectType> environment) {
        return environment.getElement();
    }

    /**
     * This is called when an input object field is encountered, which gives the schema directive a chance to modify the shape and behaviour
     * of that DSL  element
     *
     * @param environment the wiring element
     *
     * @return a non null element based on the original one
     */
    default GraphQLInputObjectField onInputObjectField(AnnotationsWiringEnvironment<GraphQLInputObjectField> environment) {
        return environment.getElement();
    }
}
