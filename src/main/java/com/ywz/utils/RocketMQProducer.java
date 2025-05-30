package com.ywz.utils;

import com.google.gson.Gson;
import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.SendCallback;
import org.apache.rocketmq.client.producer.SendResult;
import org.apache.rocketmq.spring.core.RocketMQTemplate;
import org.springframework.messaging.support.MessageBuilder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import javax.annotation.Resource;

/**
 * @author 于汶泽
 * @Description: TODO
 * @DateTime: 2025/5/30 13:55
 */
@Slf4j
@Component
public class RocketMQProducer {

    @Resource
    private  RocketMQTemplate rocketMQTemplate;
    @Resource
    private Gson gson;

    private static final String VOUCHER_TOPIC = "voucher-order-topic";

    public void send(Object message) {
        rocketMQTemplate.convertAndSend(VOUCHER_TOPIC,message);
    }

    @Transactional(rollbackFor = Exception.class)
    public void asyncSend(Object message){
        try {
            String json = gson.toJson(message);
            rocketMQTemplate.asyncSend(VOUCHER_TOPIC, MessageBuilder.withPayload(json).build(),
                    new SendCallback() {
                        @Override
                        public void onSuccess(SendResult sendResult) {
                            log.info("RocketMQ消息发送成功，消息ID: {}, 发送结果: {}", sendResult.getMsgId(), sendResult);
                        }

                        @Override
                        public void onException(Throwable throwable) {
                            log.error("RocketMQ消息发送失败，异常信息: {}", throwable.getMessage(), throwable);
                        }
                    });
        } catch (Exception e) {
            log.error("RocketMQ消息发送异常，异常信息: {}", e.getMessage(), e);
            throw new RuntimeException("RocketMQ消息发送失败", e);
        }
    }
}
