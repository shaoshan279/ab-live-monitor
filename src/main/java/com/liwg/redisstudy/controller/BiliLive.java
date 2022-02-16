package com.liwg.redisstudy.controller;

import cn.hutool.json.JSONObject;
import com.liwg.redisstudy.component.BilibiliDelegete;
import com.liwg.redisstudy.service.BiliLiveService;
import lombok.extern.log4j.Log4j2;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("live")
@Log4j2
public class BiliLive {

    private BilibiliDelegete bilibiliDelegete;

    @Autowired
    private  BiliLiveService biliLiveService;

    @GetMapping("ping")
    public Object ping(){
        return "pang";
    }

    @GetMapping("ces")
    public Object ces(){
        JSONObject liveRooms = biliLiveService.getLiveRooms();
        return "pang";
    }



}
