package graphql.annotations.processor.retrievers.fieldBuilders.method;

import graphql.annotations.annotationTypes.GraphQLDefaultValue;
import graphql.annotations.annotationTypes.GraphQLDescription;
import graphql.annotations.annotationTypes.GraphQLName;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.exceptions.GraphQLAnnotationsException;
import graphql.annotations.processor.retrievers.GraphQLInputObjectRetriever;
import graphql.annotations.processor.retrievers.fieldBuilders.Builder;
import graphql.annotations.processor.typeFunctions.TypeFunction;
import graphql.schema.*;

import java.lang.reflect.Method;
import java.lang.reflect.Parameter;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import static graphql.annotations.processor.util.NamingKit.toGraphqlName;
import static graphql.annotations.processor.util.ReflectionKit.newInstance;
import static graphql.schema.GraphQLArgument.newArgument;
import static graphql.schema.GraphQLInputObjectField.newInputObjectField;

public class ArgumentBuilder implements Builder<List<GraphQLArgument>> {
    private static final String DEFAULT_INPUT_PREFIX = "Input";

    private Method method;
    private TypeFunction typeFunction;
    private GraphQLInputObjectRetriever graphQLInputObjectRetriever;
    private GraphQLFieldDefinition.Builder builder;
    private ProcessingElementsContainer container;
    private GraphQLOutputType outputType;

    public ArgumentBuilder(Method method, TypeFunction typeFunction, GraphQLInputObjectRetriever graphQLInputObjectRetriever, GraphQLFieldDefinition.Builder builder, ProcessingElementsContainer container, GraphQLOutputType outputType) {
        this.method = method;
        this.typeFunction = typeFunction;
        this.graphQLInputObjectRetriever = graphQLInputObjectRetriever;
        this.builder = builder;
        this.container = container;
        this.outputType = outputType;
    }

    @Override
    public List<GraphQLArgument> build() {
        TypeFunction finalTypeFunction = typeFunction;
        List<GraphQLArgument> args = Arrays.asList(method.getParameters()).stream().
                filter(p -> !DataFetchingEnvironment.class.isAssignableFrom(p.getType())).
                map(parameter -> {
                    Class<?> t = parameter.getType();
                    graphql.schema.GraphQLInputType graphQLType = graphQLInputObjectRetriever.getInputObject(finalTypeFunction.buildType(t, parameter.getAnnotatedType(), container), DEFAULT_INPUT_PREFIX, container.getTypeRegistry());
                    return getArgument(parameter, graphQLType);
                }).collect(Collectors.toList());

        return args;
    }

    private GraphQLArgument getArgument(Parameter parameter, graphql.schema.GraphQLInputType t) throws
            GraphQLAnnotationsException {
        GraphQLArgument.Builder builder = newArgument().type(t);
        GraphQLDescription description = parameter.getAnnotation(GraphQLDescription.class);
        if (description != null) {
            builder.description(description.value());
        }
        GraphQLDefaultValue defaultValue = parameter.getAnnotation(GraphQLDefaultValue.class);
        if (defaultValue != null) {
            builder.defaultValue(newInstance(defaultValue.value()).get());
        }
        GraphQLName name = parameter.getAnnotation(GraphQLName.class);
        if (name != null) {
            builder.name(toGraphqlName(name.value()));
        } else {
            builder.name(toGraphqlName(parameter.getName()));
        }
        return builder.build();
    }

}
