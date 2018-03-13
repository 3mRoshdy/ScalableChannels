package mydb.firstproject.channelApp;

import mydb.firstproject.channelApp.commands.Command;
import mydb.firstproject.channelApp.commands.delete.DeleteChannel;
import mydb.firstproject.channelApp.commands.get.GetChannel;

import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import com.rabbitmq.client.AMQP;
import com.rabbitmq.client.Envelope;
import mydb.firstproject.channelApp.commands.post.PostChannel;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;

import java.io.IOException;
import java.util.HashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeoutException;

public class ChannelService {

    private static final String RPC_QUEUE_NAME = "channel-request";

    public static void main(String [] argv) {

        //initialize thread pool of fixed size
        final ThreadPoolExecutor executor = (ThreadPoolExecutor) Executors.newFixedThreadPool(4);

        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("localhost");
        Connection connection = null;
        try {
            connection = factory.newConnection();
            final Channel channel = connection.createChannel();

            channel.queueDeclare(RPC_QUEUE_NAME, false, false, false, null);

            channel.basicQos(1);

            System.out.println(" [x] Awaiting RPC requests");

            Consumer consumer = new DefaultConsumer(channel) {
                @Override
                public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                    AMQP.BasicProperties replyProps = new AMQP.BasicProperties
                            .Builder()
                            .correlationId(properties.getCorrelationId())
                            .build();
                    System.out.println("Responding to corrID: "+ properties.getCorrelationId());

                    String response = "";
                    String type = "";
                    try {
                        String message = new String(body, "UTF-8");
                        for (String m: message.split(",")){
                            if(m.contains("type")){
                                type = m.split(":")[1];
                                System.out.println("Type fetched : " + type);
                            }
                        }

                        HashMap<String, Object> props = new HashMap<String, Object>();
                        props.put("channel", channel);
                        props.put("properties", properties);
                        props.put("replyProps", replyProps);
                        props.put("envelope", envelope);
                        props.put("body", message);

                        if(type.equals("\"GET\"")){
                            Command cmd = new GetChannel();
                            cmd.init(props);
                            executor.submit(cmd);
                        } else if(type.equals("\"POST\"")){
                            Command cmd = new PostChannel();
                            cmd.init(props);
                            executor.submit(cmd);
                        } else if(type.equals("\"DELETE\"")){
                            Command cmd = new DeleteChannel();
                            cmd.init(props);
                            executor.submit(cmd);
                        }

                    } catch (RuntimeException e) {
                        System.out.println(" [.] " + e.toString());
                    } finally {
                        synchronized (this) {
                            this.notify();
                        }
                    }
                }
            };

            channel.basicConsume(RPC_QUEUE_NAME, false, consumer);
        } catch (IOException | TimeoutException e) {
            e.printStackTrace();
        }
//        finally {
//            if (connection != null)
//                try {
//                    connection.close();
//                } catch (IOException _ignore) {
//                }
//        }

    }
}