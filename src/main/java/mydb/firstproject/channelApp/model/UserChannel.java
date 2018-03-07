package mydb.firstproject.channelApp.model;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.arangodb.util.MapBuilder;
import org.json.simple.JSONObject;

import java.sql.*;
import java.util.Collection;
import java.util.Map;
import java.util.Properties;

public class UserChannel {
    //example
    public static String getUserChannelById(int id) {
        final ArangoDB arangoDB = new ArangoDB.Builder().build();
        final String dbName = "mydb";
        final String channelsCollection = "Channels";
        JSONObject channelObject = new JSONObject();
        try{
            System.out.println("Starting fetching a channel from query with id " + id);
            String query = "FOR t IN " + channelsCollection + " FILTER t.channel_id == @id  RETURN t";
            Map<String, Object> bindVars = new MapBuilder().put("id", 10).get();
            ArangoCursor<BaseDocument> cursor = arangoDB.db(dbName).query(query, bindVars, null,
                    BaseDocument.class);
            cursor.forEachRemaining(aDocument -> {
                channelObject.put("content", aDocument.getAttribute("content"));
                channelObject.put("Videos", aDocument.getAttribute("Videos"));
                System.out.println("Using AQL: " + aDocument.getProperties());
                System.out.println("id: " + aDocument.getId());
            });

        } catch (ArangoDBException e) {
            System.err.println(e.getMessage());
        }


//        String url = "jdbc:postgresql://localhost/scalable";
//        System.out.println("ID is: "+id);
//        Properties props = new Properties();
//        props.setProperty("user", "postgres");
//        props.setProperty("password", "passw0rd");
//        Connection conn = null;
//        JSONObject userObject = new JSONObject();
//        try {
//            conn = DriverManager.getConnection(url, props);
//            Statement st = conn.createStatement();
//            ResultSet rs = st.executeQuery("SELECT * FROM app_user WHERE id="+id);
//            while (rs.next()) {
//                userObject.put("user_name",rs.getString(2));
//                userObject.put("email",rs.getString(3));
//                userObject.put("date_of_birth",rs.getString(5));
//                userObject.put("gender",rs.getString(6));
//            }
//            rs.close();
//            st.close();
//        } catch (SQLException e) {
//            e.printStackTrace();
//        }
        return channelObject.toString();
    }
}
