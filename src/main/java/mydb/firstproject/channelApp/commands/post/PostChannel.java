package mydb.firstproject.channelApp.commands.post;

import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import mydb.firstproject.channelApp.commands.Command;
import java.io.IOException;
import java.util.HashMap;

import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Envelope;
import mydb.firstproject.channelApp.model.UserChannel;

public class PostChannel extends Command {

    public void execute() {
        HashMap<String, Object> props = parameters;

        Channel channel = (Channel) props.get("channel");
        JSONParser parser = new JSONParser();
        int userID = 0;
        String content = "";
        try {
            System.out.println("Fetching ... ");
            JSONObject body = (JSONObject) parser.parse((String) props.get("body"));
            System.out.println(body.toString());
            JSONObject params = (JSONObject) parser.parse(body.get("parameters").toString());
            JSONObject c = (JSONObject) parser.parse(body.get("body").toString());
            userID = Integer.parseInt(params.get("user_id").toString());
            content = c.get("content").toString();
            System.out.println("user_id : " + userID);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        AMQP.BasicProperties properties = (AMQP.BasicProperties) props.get("properties");
        AMQP.BasicProperties replyProps = (AMQP.BasicProperties) props.get("replyProps");
        Envelope envelope = (Envelope) props.get("envelope");
        String response = UserChannel.createChannel(userID,content);
//        String response = (String)props.get("body");
        try {
            channel.basicPublish("", properties.getReplyTo(), replyProps, response.getBytes("UTF-8"));
            channel.basicAck(envelope.getDeliveryTag(), false);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
