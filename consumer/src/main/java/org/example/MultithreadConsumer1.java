package org.example;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.rabbitmq.client.*;
import org.example.Body;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;

public class MultithreadConsumer1 {

    private static ConcurrentHashMap<Integer, Integer> likeCount = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Integer, Integer> dislikeCount = new ConcurrentHashMap<>();

//    private Channel channel;

    protected static final String QUEUE_NAME = "input_queue1";
    protected static final Gson gson = new GsonBuilder().setPrettyPrinting().create();
    private static final Integer Threads = 10;



    public static void main(String[] args) throws IOException, TimeoutException {
        ConnectionFactory factory = new ConnectionFactory();
        factory.setHost("52.26.174.58");
        factory.setUsername("berry1");
        factory.setPassword("berry1");
        Connection connection;
        try {
            connection = factory.newConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }


        Channel channel = connection.createChannel();
        Consumer consumer = new DefaultConsumer(channel) {
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
//                System.out.println("logged");
                Gson g = new Gson();
                Body cur_body = g.fromJson(new String(body), Body.class);
                String swiper_ID = cur_body.getSwiper();

                if (cur_body.isLikeornot()) {
                    checkifexist(likeCount, swiper_ID);
                } else {
                    checkifexist(dislikeCount, swiper_ID);
                }
            }
        };

        for (int i = 0; i < Threads; i++) {
            try {
                channel.basicConsume("input_queue1", false, consumer);
//                System.out.println(i);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        ;
    }

    public static void checkifexist(ConcurrentHashMap table, String swiper) {

        if (table.containsKey(swiper)) {
            int cur_count = (int) table.get(swiper);
            table.put(swiper, cur_count + 1);
        } else {
            table.put(swiper, 1);
        }
    }

    public int likeCountRes(Integer swiper) {
        return this.likeCount.get(swiper);
    }

    public int dislikeCountRes(Integer swiper) {
        return this.dislikeCount.get(swiper);
    }


}
