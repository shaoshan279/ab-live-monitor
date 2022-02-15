package com.liwg.redisstudy.service.impl;

import cn.hutool.json.JSONObject;
import com.liwg.redisstudy.component.BilibiliDelegete;
import com.liwg.redisstudy.service.BiliLiveService;
import lombok.extern.log4j.Log4j2;
import org.springframework.stereotype.Service;

@Service
@Log4j2
public class BiliLiveServiceImpl implements BiliLiveService {

    int areaIds[] = {0 , 1 , 2, 3, 4, 5};

    private BilibiliDelegete bilibiliDelegete;

    @Override
    public JSONObject getLiveRooms() {
        String areaCount[] = getAreaCount();


        return null;
    }

    /**
     * 获取各分区直播数量
     * @return
     */
    public String[] getAreaCount(){
        String areaCount[] = new String[6];
        for (int i = 0; i < areaCount.length; i++) {
            JSONObject countResult = bilibiliDelegete.livingCount(String.valueOf(areaIds[i]));
            JSONObject data = (JSONObject) countResult.get("data");
            String count = (String) data.get("count");
            areaCount[i] = count;
        }
        return areaCount;
    }
}
