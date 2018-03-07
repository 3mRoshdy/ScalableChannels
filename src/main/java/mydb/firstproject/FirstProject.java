package mydb.firstproject;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.HashMap;

import com.arangodb.ArangoCollection;
import com.arangodb.ArangoCursor;
import com.arangodb.ArangoDB;
import com.arangodb.ArangoDBException;
import com.arangodb.entity.BaseDocument;
import com.arangodb.entity.CollectionEntity;
import com.arangodb.model.AqlQueryOptions;
import com.arangodb.util.MapBuilder;
import com.arangodb.velocypack.VPackSlice;
import com.arangodb.velocypack.exception.VPackException;
import com.sun.xml.internal.rngom.parse.host.Base;

public class FirstProject {
    public static void main(String[] args) {
        // create database
        final ArangoDB arangoDB = new ArangoDB.Builder().build();
        final String dbName = "mydb";
        try {
            arangoDB.createDatabase(dbName);
            System.out.println("Database created: " + dbName);
        } catch (ArangoDBException e){
            System.err.println("Failed to create database: " + dbName + "; " + e.getMessage());
        }

        // create collection
        final String collectionName = "firstCollection";
        try {
            final CollectionEntity myArangoCollection = arangoDB.db(dbName)
                    .createCollection(collectionName);
            System.out.println("Collection created: " + collectionName);
        }catch (final ArangoDBException e){
            System.err.println("Failed to create collection: " + collectionName + "; " + e.getMessage());
        }

        // create Document
        BaseDocument myObject = new BaseDocument();
        myObject.setKey("myKey");
        myObject.addAttribute("name", "john");
        myObject.addAttribute("age", 22);

        try {
            arangoDB.db(dbName).collection(collectionName).insertDocument(myObject);
            System.out.println("Document created");
        } catch (ArangoDBException e){
            System.err.println("Failed to connect to documnent." + e.getMessage());
        }


        // reading from document
        try {
            System.out.println("Using Base Document");
            BaseDocument myDocument = arangoDB.db(dbName).collection(collectionName).getDocument("myKey",
                    BaseDocument.class);
            System.out.println("Props: " + myDocument.getProperties());
            System.out.println("Key: " + myDocument.getKey());
            System.out.println("Attribute name: " + myDocument.getAttribute("name"));
            System.out.println("Attribute b: " + myDocument.getAttribute("age"));
        } catch (ArangoDBException e) {
            System.err.println("Failed to get document: myKey; " + e.getMessage());
        }

        try {
            System.out.println("Using VPack");
            VPackSlice myDocument = arangoDB.db(dbName).collection(collectionName).getDocument("myKey",
                    VPackSlice.class);
            System.out.println("Key: " + myDocument.get("_key").getAsString());
            System.out.println("Attribute name: " + myDocument.get("name").getAsString());
            System.out.println("Attribute age: " + myDocument.get("age").getAsInt());
        } catch (ArangoDBException e) {
            System.err.println("Failed to get document: myKey; " + e.getMessage());
        } catch (VPackException e) {
            System.err.println("Failed to get document: myKey; " + e.getMessage());
        }

        // update a document
        myObject.addAttribute("gender", "male");
        try {
            arangoDB.db(dbName).collection(collectionName).updateDocument("myKey", myObject);
            System.out.println("Added property.");
        } catch (ArangoDBException e) {
            System.err.println("Failed to get document: myKey; " + e.getMessage());
        }

        // inserting channels
        final String ChannelsCollection = "Channels";

        try {
            arangoDB.db(dbName).createCollection(ChannelsCollection);
            ArangoCollection collection = arangoDB.db(dbName).collection(ChannelsCollection);
            String[] videos = {"Video1", "Video2"};
            for (int i = 1; i < 15; i++) {
                BaseDocument value = new BaseDocument();
                value.setKey(String.valueOf(i));
                value.addAttribute("channel_id",value.getKey());
                if (i % 2 == 0) {
                    value.addAttribute("Videos", videos);
                }
                value.addAttribute("content", i * 7);
                collection.insertDocument(value);
            }
            System.out.println("Created channels ... ");
        }
        catch (ArangoDBException e){
            System.err.println("Creating channels failed :" + e.getErrorMessage());
        }

        //getting all channels
        try {
            System.out.println();
            System.out.println("Starting fetching all channels from query");
            String query = "FOR t IN " + ChannelsCollection + " RETURN t";
            ArangoCursor<BaseDocument> cursor = arangoDB.db(dbName).query(query, null, null,
                    BaseDocument.class);
            cursor.forEachRemaining(aDocument -> {
                System.out.println("Using AQL: " + aDocument.getProperties());
            });
            System.out.println();
        } catch (ArangoDBException e){
            System.err.println(e.getMessage());
        }

        // getting a certain channel
        try {
            System.out.println("Starting fetching a channel from query with id 10");
            String query = "FOR t IN " + ChannelsCollection + " FILTER t.channel_id == @id  RETURN t";
            Map<String, Object> bindVars = new MapBuilder().put("id", 10).get();
            ArangoCursor<BaseDocument> cursor = arangoDB.db(dbName).query(query, bindVars, null,
                    BaseDocument.class);
            cursor.forEachRemaining(aDocument -> {
                System.out.println("Using AQL: " + aDocument.getProperties());
                System.out.println("id: " + aDocument.getId());
            });
        } catch (ArangoDBException e) {
            System.err.println("Failed to execute query. " + e.getMessage());
        }
        /*
        // deleting users
        try {
            String query = "FOR t IN firstCollection FILTER t.name == @name "
                    + "REMOVE t IN firstCollection LET removed = OLD RETURN removed";
            Map<String, Object> bindVars = new MapBuilder().put("name", "User").get();
            ArangoCursor<BaseDocument> cursor = arangoDB.db(dbName).query(query, bindVars, null,
                    BaseDocument.class);
            cursor.forEachRemaining(aDocument -> {
                System.out.println("Removed document " + aDocument.getKey());
            });
        } catch (ArangoDBException e) {
            System.err.println("Failed to execute query. " + e.getMessage());
        }
        */
    }
}
