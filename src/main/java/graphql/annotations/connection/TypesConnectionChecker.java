package graphql.annotations.connection;

public class TypesConnectionChecker {

    private boolean isConnection = false;
    private boolean isSimpleConnection = false;

    public boolean isConnection() {
        return isConnection;
    }

    public boolean isSimpleConnection() {
        return isSimpleConnection;
    }

    public void setConnection(boolean connection) {
        isConnection = connection;
    }

    public void setSimpleConnection(boolean simpleConnection) {
        isSimpleConnection = simpleConnection;
    }
}
