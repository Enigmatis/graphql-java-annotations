package graphql.annotations.processor.util;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;

/**
 * @author danieltaub on 07/05/2018.
 */
public class DataFetcherMock implements DataFetcher {
    private String[] args;

    public DataFetcherMock(String... args){
        this.args = args;
    }

    public  DataFetcherMock(){ }

    @Override
    public Object get(DataFetchingEnvironment environment) {
        return null;
    }

    public String[] getArgs() {
        return args;
    }
}
