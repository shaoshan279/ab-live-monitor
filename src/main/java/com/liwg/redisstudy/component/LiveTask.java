package com.liwg.redisstudy.component;

import cn.hutool.core.util.ObjectUtil;
import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.liwg.redisstudy.service.impl.BiliLiveServiceImpl;
import com.liwg.redisstudy.utils.RedisUtil;
import lombok.extern.log4j.Log4j2;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


@EnableScheduling
@Component
@Async
@Log4j2
public class LiveTask {

    private static volatile List<Integer> list = new ArrayList<>();

    private static volatile List<Integer> roomsOfFilter = new ArrayList<Integer>();

    private int areaIds[] = {1, 2, 3, 4, 5, 6};

    private static final Logger LOGGER = LoggerFactory.getLogger("Bili直播定时任务");

    @Autowired
    private RedisUtil redisUtil;

    @Autowired
    private BilibiliDelegete bilibiliDelegete;

    @Autowired
    private BiliLiveServiceImpl biliLiveService;

    @Scheduled(fixedDelay  = 2400000)
    public void liveRoomsList(){
        // 开始时间
        final Instant start = Instant.now();
        int pageSize = 50;
        int areaCount[] = biliLiveService.getAreaCount();
        ExecutorService pool = Executors.newFixedThreadPool(6);
        for (int i = 0; i < areaIds.length; i++) {
            final int ii = i;
            for (int j = 1; j <Math.ceil(areaCount[ii] / pageSize) ; j++) {
                final int jj = j;
                pool.execute(()-> {
                JSONObject livingList = bilibiliDelegete.onLivingList(String.valueOf(ii), String.valueOf(jj), String.valueOf(pageSize), "online");
                JSONObject data = (JSONObject) livingList.get("data");
                JSONArray jsonArray = (JSONArray) data.get("list");
                for (int x = 0; x < jsonArray.size(); x++) {
                    int roomid = (int) jsonArray.getJSONObject(x).get("roomid");
//                    System.out.println(Thread.currentThread().getName()+ "=>" + roomid);
                    list.add(roomid);
                }

                });
            }
        }
        pool.shutdown();
        while(true){
            if(pool.isTerminated()){
                System.out.println("所有的子线程都结束了！");
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!list.isEmpty()) {
            //list 去重
            List<Integer> collect = list.stream().distinct().collect(Collectors.toList());
            redisUtil.del(BiliLiveServiceImpl.ROOMS);
            redisUtil.lSet(BiliLiveServiceImpl.ROOMS, collect);
        }
        final Instant end = Instant.now();
        final Duration duration = Duration.between(start, end);
        LOGGER.info("全区直播房间查询耗时为:{}",duration);
    }

    @Scheduled(fixedDelay  = 600000)
    public void checkAnchor(){
        // 开始时间
        final Instant start = Instant.now();
        List<Object> roomsNoFilter= redisUtil.lGet(BiliLiveServiceImpl.ROOMS, 0, -1);
        if (roomsNoFilter.size()==0){
            return;
        }
        List arrList = (List) roomsNoFilter.get(0);
        ExecutorService pool = Executors.newFixedThreadPool(1000);
        for (int i = 0; i < arrList.size(); i++) {
            int ii = i;
            pool.execute(()->
            {
                Integer room = (Integer) arrList.get(ii);
                JSONObject res = bilibiliDelegete.checkAnchor(room.toString());
                if (((Integer) res.get("code") == 0) && !res.get("data").toString().equals("null")) {
                    roomsOfFilter.add(room);
                }
            });
        }
        pool.shutdown();
        while(true){
            if(pool.isTerminated()){
                System.out.println("所有的子线程都结束了！");
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        redisUtil.del(BiliLiveServiceImpl.ANCHOR);
        redisUtil.lSet(BiliLiveServiceImpl.ANCHOR,roomsOfFilter);
        final Instant end = Instant.now();
        final Duration duration = Duration.between(start, end);
        LOGGER.info("检查天选查询耗时为:{}",duration);
    }

}
