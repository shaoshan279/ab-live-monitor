package com.liwg.redisstudy.constant;

public interface BilibiliAPI {

    /**
     * 获取直播分区
     */
    String LIVE_AREAS = "https://api.live.bilibili.com/room/v1/Area/getList";

    /**
     * AREA_ID转ROOM_ID
     */
    String AREA_TO_RID = "https://api.live.bilibili.com/room/v1/area/getRoomList";

    /**
     * 获取随机直播房间号
     */
    String USER_RECOMMEND = "https://api.live.bilibili.com/room/v1/Area/getListByAreaID";

    /**
     * 获取直播间信息
     */
    String ROOM_INFO_V2 = "https://api.live.bilibili.com/room/v1/Room/get_info_by_id";

    /**
     * 获取直播间信息
     */
    String ROOM_INFO_WEB_V1 = "https://api.live.bilibili.com/xlive/web-room/v1/index/getInfoByRoom";

    /**
     * web获取直播间信息
     */
    String WEB_GET_ROOM_INFO ="https://api.live.bilibili.com/xlive/web-room/v1/index/getInfoByRoom";

    /**
     * 获取直播列表数据
     */
    String GET_LIVE_LIST = "https://api.live.bilibili.com/room/v3/area/getRoomList";
}
