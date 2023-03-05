package org.example;

import com.google.gson.Gson;
import com.rabbitmq.client.*;
import com.rabbitmq.client.ConnectionFactory;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.DefaultConsumer;
import org.example.Body;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeoutException;


public class LikeAndDislikeCount {
    private static ConcurrentHashMap<Integer, Integer> likeCount = new ConcurrentHashMap<>();
    private static ConcurrentHashMap<Integer, Integer> dislikeCount = new ConcurrentHashMap<>();

    private Channel channel;



    public LikeAndDislikeCount(Channel channel){
        this.channel = channel;
    }

    {
        ConnectionFactory factory = new ConnectionFactory();
        Connection connection = null;
        try {
            connection = factory.newConnection();
        } catch (IOException e) {
            throw new RuntimeException(e);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
        Channel channel = null;
        try {
            channel = connection.createChannel();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        try {
            this.channel.queueDeclare("input_queue1",false,false,false,null);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        Consumer consumer = new DefaultConsumer(this.channel){
            @Override
            public void handleDelivery(String consumerTag, Envelope envelope, AMQP.BasicProperties properties, byte[] body) throws IOException {
                Gson g  =new Gson();
                Body cur_body = g.fromJson(new String(body),Body.class);

                String swiper_ID = cur_body.getSwiper();
                if (cur_body.isLikeornot()){
                    checkifexist(likeCount,swiper_ID);
                }else{
                    checkifexist(dislikeCount,swiper_ID);
                }
            }
        };


        try {
            this.channel.basicConsume("input_queue1",true, consumer);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }


    public static void checkifexist(ConcurrentHashMap table, String swiper){

        if (table.containsKey(swiper)){
            int cur_count = (int) table.get(swiper);
            table.put(swiper, cur_count+1);
        }else{
            table.put(swiper,1);
        }
    }

    public int likeCountRes(Integer swiper){
        return this.likeCount.get(swiper);
    }

    public int dislikeCountRes(Integer swiper){
        return this.dislikeCount.get(swiper);
    }




}
