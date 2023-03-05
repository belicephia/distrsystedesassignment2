package org.example;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import org.example.Body;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;


public class Matches {
    private static ConcurrentHashMap<String, ArrayList<String>> potMatches = new ConcurrentHashMap<>();


//    private Channel channel;

    protected static final String QUEUE_NAME = "input_queue2";
    private static final Integer Threads = 10;

    public static void main(String[] args) throws IOException{
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
                Gson g = new Gson();
                Body cur_body = g.fromJson(new String(body), Body.class);
                String swiper_ID = cur_body.getSwiper();
                String swipee_ID = cur_body.getSwipee();
                if (cur_body.isLikeornot()) {
                    add_to_lsit(swiper_ID, swipee_ID);
                }
            }
        };
        try {
            channel.basicConsume("input_queue2", true, consumer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


        public static void add_to_lsit(String swiper_Id, String swipee_Id){
        if (potMatches.containsKey(swiper_Id)) {
            ArrayList<String> cur_list = potMatches.get(swiper_Id);
            if (cur_list.size() < 100) {
                cur_list.add(swipee_Id);
                potMatches.put(swiper_Id, cur_list);
            }
        } else {
            ArrayList<String> new_list = new ArrayList<String>();
            new_list.add(swipee_Id);
            potMatches.put(swiper_Id, new_list);
        }
    }


        public ArrayList<String> returnPotMatches (Integer swiper_Id){
        if (potMatches.containsKey(swiper_Id)) {
            return potMatches.get(swiper_Id);
        } else {
            return new ArrayList<String>();
        }
    }


    }

