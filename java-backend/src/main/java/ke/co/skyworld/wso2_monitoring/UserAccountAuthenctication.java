package ke.co.skyworld.wso2_monitoring;

import io.undertow.server.HttpHandler;
import io.undertow.server.HttpServerExchange;
import io.undertow.util.Headers;
import io.undertow.util.PathTemplateMatch;
import io.undertow.util.StatusCodes;
import io.undertow.util.URLUtils;

import java.nio.charset.StandardCharsets;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;

public class UserAccountAuthenctication implements HttpHandler {

    @Override
    public void handleRequest(HttpServerExchange exchange) throws Exception {

        String username = getPathVar(exchange, "username");

        UserAccounts userAccounts = getUser(username);

        if (userAccounts==null){

            class Errors{
                private String error;
                private int errorCode;

                public String getError() {
                    return error;
                }

                public void setError(String error) {
                    this.error = error;
                }

                public int getErrorCode() {
                    return errorCode;
                }

                public void setErrorCode(int errorCode) {
                    this.errorCode = errorCode;
                }
            }

            Errors errors = new Errors();
            errors.setError("Account with the provided username not found");
            errors.setErrorCode(404);

            exchange.setStatusCode(StatusCodes.NOT_FOUND);
            exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "application/json");
            exchange.getResponseSender().send(JavaToJSONAndXML.convertToJson(errors));

            return;
        }

        String json = JavaToJSONAndXML.convertToJson(userAccounts);
        exchange.getResponseHeaders().add(Headers.CONTENT_TYPE, "application/json");
        exchange.getResponseSender().send(json);
    }

    public static UserAccounts getUser(String username) {
        Connection myConn = null;

        NamedPreparedStatement myPreparedStatement = null;

        ResultSet myResult = null;

        try {

            myConn = DriverManager.getConnection("jdbc:mysql://localhost:3306/wso2-monitoring-database", "root", "Pa55w0rd");

            String mySql = "select * from user_accounts WHERE username=:username";

            myPreparedStatement = NamedPreparedStatement.prepareStatement(myConn,mySql);

            myPreparedStatement.setString("username", username);
            //myPreparedStatement.setString("user_pwd", user_accounts.getUser_pwd());


            myResult = myPreparedStatement.executeQuery();


            if (myResult.next()){
                UserAccounts userAccounts =new UserAccounts();
                int user_id = myResult.getInt("user_id");

                userAccounts.setUser_id(user_id);

                System.out.println("ID"+user_id);

                return userAccounts;
            }

            myResult.close();
            myPreparedStatement.close();
            myConn.close();


            myConn = null;
            myPreparedStatement = null;
            myResult = null;

            return null;

        } catch (Exception e) {
            e.printStackTrace();
            if (myResult != null) try {
                myResult.close();
            } catch (SQLException ignore) {
            }
            if (myPreparedStatement != null) try {
                myPreparedStatement.close();
            } catch (SQLException ignore) {
            }
            if (myConn != null) try {
                myConn.close();
            } catch (SQLException ignore) {
            }

            myConn = null;
            myPreparedStatement = null;
            myResult = null;
        } finally {
            if (myResult != null) try {
                myResult.close();
            } catch (SQLException ignore) {
            }
            if (myPreparedStatement != null) try {
                myPreparedStatement.close();
            } catch (SQLException ignore) {
            }
            if (myConn != null) try {
                myConn.close();
            } catch (SQLException ignore) {
            }

            myConn = null;
            myPreparedStatement = null;
            myResult = null;
        }

        return null;
    }

    public static String getPathVar(HttpServerExchange exchange, String pathVarId) {
        PathTemplateMatch pathMatch = (PathTemplateMatch)exchange.getAttachment(PathTemplateMatch.ATTACHMENT_KEY);
        StringBuilder builder = new StringBuilder();
        URLUtils.decode((String)pathMatch.getParameters().get(pathVarId), StandardCharsets.UTF_8.name(), true, builder);
        return builder.toString();
    }

}