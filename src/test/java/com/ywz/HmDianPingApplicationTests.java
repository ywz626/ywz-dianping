package com.ywz;

import cn.hutool.core.bean.BeanUtil;
import cn.hutool.core.bean.copier.CopyOptions;
import cn.hutool.core.lang.UUID;
import com.ywz.dto.UserDTO;
import com.ywz.entity.User;
import com.ywz.service.IUserService;
import com.ywz.utils.RedisConstants;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;

import javax.annotation.Resource;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
class HmDianPingApplicationTests {

    @Resource
    private IUserService userService;

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    private String path = "D:\\data.txt";

    @Test
    public void contextLoads()  {
        for(int i = 0; i<1000;i++){
            User user = userService.createUser(i + "");
            String token = UUID.randomUUID().toString(true);
            try (FileWriter writer = new FileWriter(path,true);){
                writer.write(token+"\n");
            } catch (IOException e) {
                System.out.println("出错");
                throw new RuntimeException(e);
            }
            //复制到用户信息类
            UserDTO userDTO = BeanUtil.copyProperties(user, UserDTO.class);
            //将用户信息存入map中
            Map<String, Object> userMap = BeanUtil.beanToMap(userDTO,
                    new HashMap<>(),
                    CopyOptions.create().setIgnoreNullValue(true).setFieldValueEditor((field, value) -> value.toString()));
            //将map存入redis中
            stringRedisTemplate.opsForHash().putAll(RedisConstants.LOGIN_USER_KEY + token, userMap);
            //设置token的过期时间
            stringRedisTemplate.expire(RedisConstants.LOGIN_USER_KEY + token, RedisConstants.LOGIN_USER_TTL, TimeUnit.SECONDS);
        }
    }
}
