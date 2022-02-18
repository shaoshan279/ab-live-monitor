package com.liwg.redisstudy.service.impl;

import cn.hutool.json.JSONArray;
import cn.hutool.json.JSONObject;
import com.liwg.redisstudy.component.BilibiliDelegete;
import com.liwg.redisstudy.service.BiliLiveService;
import com.liwg.redisstudy.utils.RedisUtil;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@Log4j2
public class BiliLiveServiceImpl implements BiliLiveService {

    private int areaIds[] = { 1 , 2, 3, 4, 5 ,6};

    private static volatile List<String> roomsOfNoFilter;

    @Autowired
    private BilibiliDelegete bilibiliDelegete;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public List getLiveRooms() {
        int page ;
        int pageSize = 100;
        int areaCount[] = getAreaCount();
        for (int i = 0; i < areaIds.length; i++) {
            page = 1;
            do{
                JSONObject livingList = bilibiliDelegete.onLivingList(String.valueOf(i), String.valueOf(page), String.valueOf(pageSize), "online");
                if (livingList.get("code").equals(0)){
                    JSONObject data = (JSONObject) livingList.get("data");
                    JSONArray jsonArray = (JSONArray) data.get("list");
                    for (int j = 0; j < jsonArray.size(); j++) {
                        String roomid = (String) jsonArray.getJSONObject(j).get("roomid");
                        roomsOfNoFilter.add(roomid);
                    }
                    page++;
                }
            }while (page<Math.ceil(areaCount[i]/pageSize));
            redisUtil.lSet("roomsNoFilter",roomsOfNoFilter);
        }
        return roomsOfNoFilter;
    }

    /**
     * live是否在线
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
     * 获取各分区直播数量
     * @return
     */
    public int[] getAreaCount(){
        int areaCount[] = new int[6];
        for (int i = 0; i < areaCount.length; i++) {
            JSONObject countResult = bilibiliDelegete.livingCount(String.valueOf(areaIds[i]));
            JSONObject data = (JSONObject) countResult.get("data");
            int count = (int) data.get("count");
            areaCount[i] = count;
        }
        return areaCount;
    }
}
