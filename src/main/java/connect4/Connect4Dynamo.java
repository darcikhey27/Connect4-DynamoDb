package connect4;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDB;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.services.dynamodbv2.document.DynamoDB;
import com.amazonaws.services.dynamodbv2.document.Item;
import com.amazonaws.services.dynamodbv2.document.PrimaryKey;
import com.amazonaws.services.dynamodbv2.document.Table;
import com.amazonaws.services.dynamodbv2.model.AttributeValue;
import com.amazonaws.services.dynamodbv2.model.GetItemRequest;

import java.util.*;

public class Connect4Dynamo {
    private static AmazonDynamoDB CLIENT = AmazonDynamoDBClientBuilder.standard()
            .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration("dynamodb.us-west-2.amazonaws.com", "us-west-2"))
            .build();

    private static DynamoDB DYNAMO_DB = new DynamoDB(CLIENT);
    private static Table table;
    private static String TABLE_NAME = "board";

    // connect to DynamoDB PlayerTest table
    public void connectToTable() {
       table = DYNAMO_DB.getTable(TABLE_NAME);
        System.out.println(table.getDescription());
    }

    // upload a test item
    public void uploadItem(Item item) {
        System.out.println("adding item to table");
       table.putItem(item);
    }


    public static void main(String ... args) {

//        Connect4Dynamo connect4Dynamo = new Connect4Dynamo();
//        connect4Dynamo.connectToTable();
//        //connect4Dynamo.uploadItem();
//        connect4Dynamo.getItem();

    }

    private void getItem() {
        HashMap<String, AttributeValue> keyToGet = new HashMap<String, AttributeValue>();
        keyToGet.put("location", new AttributeValue("0,0"));

        GetItemRequest getItemRequest = new GetItemRequest().withKey(keyToGet).withTableName(TABLE_NAME);

        try {
            Map<String, AttributeValue> returned_item = CLIENT.getItem(getItemRequest).getItem();
            if (returned_item != null) {
                Set<String> keys = returned_item.keySet();
                for (String key : keys) {
                    System.out.format("%s: %s\n", key, returned_item.get(key).toString());
                }
            } else {
                System.out.format("No item found with the key %s!\n", "somekey");
            }
        } catch (AmazonServiceException e) {
            System.err.println(e.getErrorMessage());
            System.out.println("Error happend");
            System.exit(1);
        }
    }

    public void sendModifiedLocation(String location, String myMark) {
        String message = "";
        if(myMark.equals("X")) {
             message = "Player 2 turn";
        }
        else {
            message = "Player 1 turn";
        }
        Item status = new Item().withPrimaryKey("rowcol", location)
                .withBoolean("gameOver", false)
                .withBoolean("modified", true)
                .withString("mark", myMark)
                .withString("modifiedLocation", location)
                .withString("message", message);
        this.uploadItem(status);
    }

    public boolean checkIsGameOver(String rowcolStatus) {
       // System.out.println("rowColStatus is "+ rowcolStatus);
        PrimaryKey key = new PrimaryKey("rowcol", rowcolStatus);
        Item item = table.getItem(key);
        boolean isGameOver = item.getBOOL("gameOver");

        if(isGameOver) {
            return true;
        }
        return false;
    }
    public boolean checkDidPlayerMove(String rowColStatus) {


        return false;
    }

    public String getMessage(String status) {
        PrimaryKey key = new PrimaryKey("rowcol", status);
        Item item = table.getItem(key);
        String message = item.getString("message");
        if(message == null) {
            throw new NullPointerException("message is null:");
        }
        return message;
    }

    public int[] getLocation(String status) {
        PrimaryKey key = new PrimaryKey("rowcol", status);
        Item item = table.getItem(key);
        String locationString = item.getString("location");
        if(locationString == null) {
            throw new NullPointerException("message is null:");
        }
        String[] nums = locationString.split(",");
        int[] location = new int[2];
        location[0] = Integer.parseInt(nums[0]);
        location[1] = Integer.parseInt(nums[1]);
        return location;
    }
}
