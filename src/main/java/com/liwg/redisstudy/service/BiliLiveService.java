package com.liwg.redisstudy.service;


import cn.hutool.json.JSONObject;

import java.util.List;

public interface BiliLiveService {
    List getLiveRooms();

    Object getRedis(String key);

    Object getRedisListSize(String key);
}
