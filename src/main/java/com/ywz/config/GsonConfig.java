package com.ywz.config;

import com.google.gson.*;
import com.google.gson.stream.JsonReader;
import com.google.gson.stream.JsonToken;
import com.google.gson.stream.JsonWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Random;

/**
 * @author 于汶泽
 * @Description: Gson配置类
 * @DateTime: 2025/4/30 17:08
 */
@Configuration
public class GsonConfig {

    final static JsonSerializer<LocalDateTime> jsonSerializerDateTime = (localDateTime, type, jsonSerializationContext)
            -> new JsonPrimitive(localDateTime.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    final static JsonSerializer<LocalDate> jsonSerializerDate = (localDate, type, jsonSerializationContext)
            -> new JsonPrimitive(localDate.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
    //反序列化
    final static JsonDeserializer<LocalDateTime> jsonDeserializerDateTime = (jsonElement, type, jsonDeserializationContext)
            -> LocalDateTime.parse(jsonElement.getAsJsonPrimitive().getAsString(),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    final static JsonDeserializer<LocalDate> jsonDeserializerDate = (jsonElement, type, jsonDeserializationContext)
            -> LocalDate.parse(jsonElement.getAsJsonPrimitive().getAsString(),
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    
    // 为Random类创建一个自定义的TypeAdapter
    final static TypeAdapter<Random> randomTypeAdapter = new TypeAdapter<Random>() {
        @Override
        public void write(JsonWriter out, Random value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                // 只序列化Random对象，不包含其内部状态
                out.beginObject().endObject();
            }
        }

        @Override
        public Random read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            // 跳过对象内容
            in.skipValue();
            return new Random();
        }
    };
    
    // 为Thread类创建一个自定义的TypeAdapter
    final static TypeAdapter<Thread> threadTypeAdapter = new TypeAdapter<Thread>() {
        @Override
        public void write(JsonWriter out, Thread value) throws IOException {
            if (value == null) {
                out.nullValue();
            } else {
                // 只序列化Thread的基本信息，不包含其内部状态
                out.beginObject();
                out.name("name").value(value.getName());
                out.name("id").value(value.getId());
                out.name("priority").value(value.getPriority());
                out.name("state").value(value.getState().name());
                out.endObject();
            }
        }

        @Override
        public Thread read(JsonReader in) throws IOException {
            if (in.peek() == JsonToken.NULL) {
                in.nextNull();
                return null;
            }
            // 跳过对象内容
            in.skipValue();
            // 返回当前线程，因为无法通过反序列化创建特定的线程
            return Thread.currentThread();
        }
    };

    @Bean
    public Gson gson() {
        return new GsonBuilder()
                .setPrettyPrinting()
                /* 更改先后顺序没有影响 */
                .registerTypeAdapter(LocalDateTime.class, jsonSerializerDateTime)
                .registerTypeAdapter(LocalDate.class, jsonSerializerDate)
                .registerTypeAdapter(LocalDateTime.class, jsonDeserializerDateTime)
                .registerTypeAdapter(LocalDate.class, jsonDeserializerDate)
                // 添加Random类的TypeAdapter
                .registerTypeAdapter(Random.class, randomTypeAdapter)
                // 添加Thread类的TypeAdapter
                .registerTypeAdapter(Thread.class, threadTypeAdapter)
                // 使用ReflectionAccessFilter禁用对Java标准库类的反射访问
                .addReflectionAccessFilter(ReflectionAccessFilter.BLOCK_INACCESSIBLE_JAVA)
                .create();
    }
}
