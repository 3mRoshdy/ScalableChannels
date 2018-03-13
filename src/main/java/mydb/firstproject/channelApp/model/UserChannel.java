package mydb.firstproject.channelApp.model;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.arangodb.util.MapBuilder;
import org.json.simple.JSONObject;

import java.sql.*;
import java.util.*;
import java.util.Date;

public class UserChannel {
    //example
    public static String getUserChannelById(int id) {
        final ArangoDB arangoDB = new ArangoDB.Builder().build();
        final String dbName = "mydb";
        final String channelsCollection = "Channels";
        JSONObject channelObject = new JSONObject();
        System.out.println("Getting Channel by id: " + id);
        try{
            System.out.println("Starting fetching a channel from query with id " + id);
            String query = "FOR t IN " + channelsCollection + " FILTER t.id == @id  RETURN t";
            Map<String, Object> bindVars = new MapBuilder().put("id", id+"").get();
            ArangoCursor<BaseDocument> cursor = arangoDB.db(dbName).query(query, bindVars, null,
                    BaseDocument.class);
            cursor.forEachRemaining(aDocument -> {
                System.out.println(aDocument.getProperties());
                channelObject.put("data", aDocument.getProperties());
            });

        } catch (ArangoDBException e) {
            System.err.println(e.getMessage());
        }

        return channelObject.toString();
    }

    public static String deleteChannelById(int channelID) {
        System.out.println("Deleting Channel by id: " + channelID);
        final ArangoDB arangoDB = new ArangoDB.Builder().build();
        final String dbName = "mydb";
        final String channelsCollection = "Channels";
        JSONObject channelObject = new JSONObject();
        try{
            arangoDB.db(dbName).collection(channelsCollection).deleteDocument(channelID + "");
            System.out.println("Deleted Channel with id: " + channelID);
            channelObject.put("message", "success");
        } catch (ArangoDBException e) {
            System.err.println(e.getMessage());
        }

        return channelObject.toString();

    }

    public static String createChannel(int userID, String content){
        System.out.println("Starting creating channel ...");
        final ArangoDB arangoDB = new ArangoDB.Builder().build();
        final String dbName = "mydb";
        final String channelsCollection = "Channels";
        JSONObject channelObject = new JSONObject();
        System.out.println("Creating Channel by user_id: " + userID);

        try{
            ArangoCollection Channels = arangoDB.db(dbName).collection(channelsCollection);
// create Document
            BaseDocument myObject = new BaseDocument();
            java.util.Date d = new Date();
            JSONObject info = new JSONObject();
            info.put("name", content);
            info.put("profile_pic", "http://imgr.com/dd3e32e2");
            info.put("category", "Sport");
            info.put("subscriptions", 100);
            info.put("date_created", d.getDate() + "/" + d.getMonth() + "/" + d.getYear());
            myObject.addAttribute("info", info);

            JSONObject user = new JSONObject();
            user.put("id", userID);
            user.put("first_name", "JOHN");
            user.put("last_name", "CENA");
            user.put("date_of_birth", d.getDate() + "/" + d.getMonth() + "/" + d.getYear());
            user.put("gender", "male");
            myObject.addAttribute("user", user);

            ArrayList<JSONObject> subscriptions = new ArrayList<JSONObject>();
            JSONObject v = new JSONObject();
            JSONObject m = new JSONObject();

            v.put("id",1);
            v.put("name","Medhat");
            v.put("profile_pic","http://imgr.com/dd3e32e2");
            v.put("category","Comedy");

            subscriptions.add(v);
            m.put("id",2);
            m.put("name","CCC");
            m.put("profile_pic","http://imgr.com/dd3e32e2");
            m.put("category","Thriller");
            subscriptions.add(m);

            myObject.addAttribute("subscriptions", subscriptions);

            ArrayList<JSONObject> watched_videos = new ArrayList<JSONObject>();
            JSONObject b = new JSONObject();
            b.put("id", 2);
            b.put("title", "title 1");
            b.put("category", "Music");
            b.put("liked", true);
            watched_videos.add(b);
            myObject.addAttribute("watched_videos", watched_videos);

            ArrayList<JSONObject> blocked_channels = new ArrayList<JSONObject>();
            JSONObject bc = new JSONObject();
            bc.put("id",1);
            bc.put("name","CCCCCC");
            blocked_channels.add(bc);
            myObject.addAttribute("blocked_channels", blocked_channels);

            ArrayList<JSONObject> notifications = new ArrayList<JSONObject>();
            JSONObject n = new JSONObject();
            n.put("text", "New Updates");
            n.put("type", "Announcment");
            n.put("content_id", 123);
            notifications.add(n);
            myObject.addAttribute("notifications", notifications);

            Channels.insertDocument(myObject);

            System.out.println("Key: "  + myObject.getKey());
            String key = myObject.getKey();
            myObject.addAttribute("id",myObject.getKey());
            arangoDB.db(dbName).collection("Channels").updateDocument(key, myObject);

            System.out.println("Channel created!");
            System.out.println("id: " + myObject.getAttribute("id"));
            channelObject.put("id", myObject.getAttribute("id"));
            channelObject.put("info", myObject.getAttribute("info"));
            channelObject.put("user", myObject.getAttribute("user"));
            channelObject.put("subscriptions", myObject.getAttribute("subscriptions"));
            channelObject.put("watched_videos", myObject.getAttribute("watched_videos"));
            channelObject.put("blocked_channels", myObject.getAttribute("blocked_channels"));
            channelObject.put("notifications", myObject.getAttribute("notifications"));

        }catch (ArangoDBException e){
            System.err.println(e.getMessage());
        }

        return channelObject.toString();
    }
}
