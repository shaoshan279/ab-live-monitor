package com.liwg.redisstudy;

import com.liwg.redisstudy.service.BiliLiveService;
import com.liwg.redisstudy.utils.RedisUtil;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.List;

@SpringBootTest
class RedisStudyApplicationTests {

    @Autowired
    private RedisTemplate redisTemplate;

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private BiliLiveService biliLiveService;

    @Test
    void contextLoads() {
//        redisTemplate.opsForValue().set("name","yes");
//        System.out.println(redisTemplate.opsForValue().get("name"));
        List<Object> roomsNoFilter = redisUtil.lGet("roomsNoFilter", 0, -1);
        System.out.println(roomsNoFilter);
    }

    @Test
    void test(){
        biliLiveService.getLiveRooms();
    }
}
