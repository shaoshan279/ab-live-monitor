package com.liwg.redisstudy.component;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.io.IORuntimeException;
import cn.hutool.http.Header;
import cn.hutool.http.HttpException;
import cn.hutool.http.HttpRequest;
import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import com.github.rholder.retry.*;
import com.liwg.redisstudy.constant.BilibiliAPI;
import lombok.Getter;
import lombok.extern.log4j.Log4j2;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.util.UriComponentsBuilder;

import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;

@Log4j2
@Service
public class BilibiliDelegete {
    private static final String UA = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/86.0.4240.198 Safari/537.36";

    @Getter
    private HttpRequest httpRequest;


    //   API 代码调用区
    //------------------------------------------------------------>


    /**
     * 获取各个直播分区正在直播的数量
     * @param areaId  分区ID
     * @return
     */
    public JSONObject livingCount(String areaId) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("parent_area_id", CollUtil.newArrayList(areaId));
        params.put("page", CollUtil.newArrayList("1"));
        params.put("page_size", CollUtil.newArrayList("1"));
        params.put("sort_type", CollUtil.newArrayList("online"));
        return doGet(BilibiliAPI.GET_LIVE_LIST, params);
    }


    /**
     * 获取直播列表
     * @param areaId 分区ID
     * @param page  页码
     * @param pageSize  单页数量
     * @param sortType  排序类型  1. online ~待添加
     * @return
     */
    public JSONObject onLivingList(String areaId,String page,String pageSize,String sortType) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("parent_area_id", CollUtil.newArrayList(areaId));
        params.put("page", CollUtil.newArrayList(page));
        params.put("page_size", CollUtil.newArrayList(pageSize));
        params.put("sort_type", CollUtil.newArrayList(sortType));
        return doGet(BilibiliAPI.GET_LIVE_LIST, params);
    }


    /**
     * 获取直播间详情
     * @param roomid
     * @return
     */
    public JSONObject getRoomInfo(String roomid) {
        MultiValueMap<String, String> params = new LinkedMultiValueMap<>();
        params.put("room_id", CollUtil.newArrayList(roomid));
        return doGet(BilibiliAPI.ROOM_INFO_WEB_V1, params);
    }




    //<------------------------------------------------------------


    private final Retryer<String> retryer = RetryerBuilder.<String>newBuilder()
            .retryIfExceptionOfType(HttpException.class)
            .retryIfExceptionOfType(IORuntimeException.class)
            .withStopStrategy(StopStrategies.stopAfterAttempt(4))
            .withRetryListener(new RetryListener() {
                @Override
                public <V> void onRetry(Attempt<V> attempt) {
                    if (attempt.hasException()) {
                        log.error("第{}次调用失败: {}, 进行重试", attempt.getAttemptNumber(), attempt.getExceptionCause().getMessage());
                    }
                }
            })
            .build();


    private JSONObject doGet(String url) {
        return doGet(url, null);
    }

    /**
     * 实际处理B站API访问
     *
     * @param url    访问API地址
     * @param params 查询字符串参数 {@link MultiValueMap}
     * @return 解析后的JSON对象 {@link JSONObject}
     */
    private JSONObject doGet(String url, MultiValueMap<String, String> params) {
        url = UriComponentsBuilder.fromHttpUrl(url)
                .queryParams(params)
                .build().toUriString();
        httpRequest = HttpRequest.get(url)
                .timeout(30000)
                .header(Header.CONNECTION, "keep-alive");

        return retryableCall(httpRequest);
    }

    private JSONObject doPost(String url, String requestBody) {
        return doPost(url, requestBody, null);
    }

    private JSONObject doPost(String url, String requestBody, Map<String, String> headers) {
        httpRequest = HttpRequest.post(url)
                .timeout(30000)
                .header(Header.CONTENT_TYPE, JSONUtil.isJson(requestBody) ?
                        MediaType.APPLICATION_JSON_VALUE : MediaType.APPLICATION_FORM_URLENCODED_VALUE)
                .header(Header.CONNECTION, "keep-alive")
                .header(Header.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/96.0.4664.110 Safari/537.36")
                .header(Header.REFERER, "https://www.bilibili.com/")
                .addHeaders(headers);
        return retryableCall(httpRequest.body(requestBody));
    }


    private JSONObject retryableCall(HttpRequest httpRequest) {
        Callable<String> task = () -> httpRequest.execute().body();
        String responseBody = null;
        try {
            responseBody = retryer.call(task);
        } catch (ExecutionException e) {
            log.error("重试调用接口[{}]失败, {}", httpRequest.getUrl(), e.getMessage());
        } catch (RetryException e) {
            log.error("调用接口[{}]超过执行次数, {}", httpRequest.getUrl(), e.getMessage());
        }
        return JSONUtil.parseObj(responseBody);
    }
}
