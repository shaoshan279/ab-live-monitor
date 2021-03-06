package com.liwg.redisstudy.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.liwg.redisstudy.component.BilibiliDelegete;
import com.liwg.redisstudy.service.BiliLiveService;
import com.liwg.redisstudy.utils.RedisUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

@Service
@Log4j2
public class BiliLiveServiceImpl implements BiliLiveService {

    public static final String ROOMS = "roomsNoFilter";

    public static final String ANCHOR = "roomsOfAnchor";

    private int areaIds[] = {1, 2, 3, 5, 6, 9, 10, 11, 13};

    private static volatile List<Integer> list = new ArrayList<>();

    @Autowired
    private BilibiliDelegete bilibiliDelegete;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List getLiveRooms() {
        int pageSize = 50;
        int areaCount[] = getAreaCount();
        ExecutorService pool = Executors.newFixedThreadPool(6);
        for (int i = 0; i < areaIds.length; i++) {
            final int ii = i;
            pool.execute(()->
                    {
                        int page = 1;
                        do {
                            JSONObject livingList = bilibiliDelegete.onLivingList(String.valueOf(ii), String.valueOf(page), String.valueOf(pageSize), "online");
                            JSONObject data = (JSONObject) livingList.get("data");
                            JSONArray jsonArray = (JSONArray) data.get("list");
                            for (int j = 0; j < jsonArray.size(); j++) {
                                int roomid = (int) jsonArray.getJSONObject(j).get("roomid");
//                    System.out.println(Thread.currentThread().getName()+ "=>" + roomid);
                                list.add(roomid);
                            }

                            if (livingList.get("code").equals(0)) {
                                page++;
                            }
                        } while (page < Math.ceil(areaCount[ii] / pageSize));
                    }
            );
        }
        pool.shutdown();
        while(true){
            if(pool.isTerminated()){
                System.out.println("?????????????????????????????????");
                break;
            }
            try {
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
        if (!list.isEmpty()) {
            //list ??????
            List<Integer> collect = list.stream().distinct().collect(Collectors.toList());
            redisUtil.del(ROOMS);
            redisUtil.lSet(ROOMS, collect);
            return list;
        }
        else {
            return null;
        }

    }

    @Override
    public Object getRedis(String key) {
        // 0 ??? -1 ???????????????
        return redisUtil.lGet(key,0,-1);
    }

    @Override
    public Object getRedisListSize(String key) {
        // 0 ??? -1 ???????????????
        List o = (List) redisUtil.lGet(key, 0, -1).get(0);
        int size = o.size();
        return size;
    }

    /**
     * live????????????
     * @param roomid
     * @return
     */
    public Boolean isLive(String roomid){
        if (roomid.isEmpty()){
            return false;
        }
        JSONObject roomInfo = bilibiliDelegete.getRoomInfo(roomid);
        JSONObject data= (JSONObject) roomInfo.get("data");
        JSONObject room_info = (JSONObject) data.get("room_info");
        boolean live_status = room_info.get("live_status").equals("1");
        return live_status;
    }


    /**
     *
     * @param roomid
     * @return
     */
    public Boolean getInfo(String roomid){
        if (roomid.isEmpty()){
            return false;
        }
        JSONObject roomInfo = bilibiliDelegete.getRoomInfo(roomid);
        JSONObject data= (JSONObject) roomInfo.get("data");
        JSONObject room_info = (JSONObject) data.get("room_info");
        boolean live_status = room_info.get("live_status").equals("1");
        return live_status;
    }


    /**
     * ???????????????????????????
     *
     * @return
     */
    public int[] getAreaCount() {
        int areaCount[] = new int[9];
        for (int i = 0; i < areaCount.length; i++) {
            JSONObject countResult = bilibiliDelegete.livingCount(String.valueOf(areaIds[i]));
            JSONObject data = (JSONObject) countResult.get("data");
            int count = (int) data.get("count");
            areaCount[i] = count;
        }
        return areaCount;
    }
}
