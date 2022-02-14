package com.liwg.redisstudy.controller;

import com.liwg.redisstudy.component.BilibiliDelegete;
import lombok.extern.log4j.Log4j2;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("live")
@Log4j2
public class BiliLive {

    private BilibiliDelegete bilibiliDelegete;

    @GetMapping("ping")
    public Object ping(){
        return "pang";
    }

}
