package graphql.annotations.processor.retrievers;

import graphql.GraphQLException;
import graphql.annotations.annotationTypes.GraphQLTypeResolver;
import graphql.annotations.processor.ProcessingElementsContainer;
import graphql.annotations.processor.searchAlgorithms.SearchAlgorithm;
import graphql.schema.GraphQLOutputType;
import graphql.schema.GraphQLType;
import org.clapper.util.classutil.ClassFilter;
import org.clapper.util.classutil.ClassFinder;
import org.clapper.util.classutil.ClassInfo;
import org.clapper.util.classutil.SubclassClassFilter;
import org.osgi.service.component.annotations.Component;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static graphql.annotations.processor.util.ObjectUtil.getAllFields;

@SuppressWarnings("ConstantConditions")
@Component(service = GraphQLExtensionsHandler.class, immediate = true)
public class GraphQLAdditionalTypesHandler {

    private GraphQLInterfaceRetriever graphQLInterfaceRetriever;
    private GraphQLObjectInfoRetriever graphQLObjectInfoRetriever;
    private SearchAlgorithm methodSearchAlgorithm;
    private SearchAlgorithm fieldSearchAlgorithm;
    private GraphQLObjectInfoRetriever infoRetriever;
    private ClassFinder classFinder;

    public GraphQLAdditionalTypesHandler() {
        classFinder = new ClassFinder();
        classFinder.addClassPath();
        infoRetriever = new GraphQLObjectInfoRetriever();
    }

    public Set<GraphQLType> getAdditionalInterfacesImplementations(Class<?> root, ProcessingElementsContainer container) {
        Set<GraphQLType> additionalFields = new HashSet<>();

        additionalFields.addAll(getNewImplementations(container, classFinder, root));
        for (Method method : graphQLObjectInfoRetriever.getOrderedMethods(root)) {
            if (method.isBridge() || method.isSynthetic()) {
                continue;
            }
            if (methodSearchAlgorithm.isFound(method)) {
                additionalFields.addAll(getAdditionalInterfacesImplementations(method.getReturnType(), container));
            }
        }

        for (Field field : getAllFields(root).values()) {
            if (Modifier.isStatic(field.getModifiers())) {
                continue;
            }
            if (fieldSearchAlgorithm.isFound(field)) {
                additionalFields.addAll(getAdditionalInterfacesImplementations(field.getType(), container));
            }
        }

        return additionalFields;
    }

    private Set<GraphQLType> getNewImplementations(ProcessingElementsContainer container, ClassFinder classFinder, Class<?> aClass) {
        Set<GraphQLType> additionalFields = new HashSet<>();
        if (container.getTypeRegistry().containsKey(infoRetriever.getTypeName(aClass)) && aClass.isInterface() && aClass.isAnnotationPresent(GraphQLTypeResolver.class)) {
            ClassFilter classFilter = new SubclassClassFilter(aClass);
            List<ClassInfo> foundClasses = new ArrayList<>();
            classFinder.findClasses(foundClasses, classFilter);
            for (ClassInfo classInfo : foundClasses) {
                try {
                    if (!(container.getTypeRegistry().containsKey(infoRetriever.getTypeName(Class.forName(classInfo.getClassName()))))) {
                        GraphQLOutputType additionalObject = graphQLInterfaceRetriever.getInterface(Class.forName(classInfo.getClassName()), container);
                        additionalFields.add(additionalObject);
                    }
                } catch (ClassNotFoundException e) {
                    throw new GraphQLException(e);
                }
            }
        }
        return additionalFields;
    }

    public void setGraphQLObjectInfoRetriever(GraphQLObjectInfoRetriever graphQLObjectInfoRetriever) {
        this.graphQLObjectInfoRetriever = graphQLObjectInfoRetriever;
    }

    public void setMethodSearchAlgorithm(SearchAlgorithm methodSearchAlgorithm) {
        this.methodSearchAlgorithm = methodSearchAlgorithm;
    }

    public void setFieldSearchAlgorithm(SearchAlgorithm fieldSearchAlgorithm) {
        this.fieldSearchAlgorithm = fieldSearchAlgorithm;
    }

    public void setGraphQLInterfaceRetriever(GraphQLInterfaceRetriever graphQLInterfaceRetriever) {
        this.graphQLInterfaceRetriever = graphQLInterfaceRetriever;
    }
}
