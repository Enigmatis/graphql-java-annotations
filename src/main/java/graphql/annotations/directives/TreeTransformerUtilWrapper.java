package graphql.annotations.directives;

import graphql.util.TraversalControl;
import graphql.util.TraverserContext;
import graphql.util.TreeTransformerUtil;

public class TreeTransformerUtilWrapper {
    public <T> TraversalControl changeNode(TraverserContext<T> context, T changedNode) {
        return TreeTransformerUtil.changeNode(context, changedNode);
    }
}
