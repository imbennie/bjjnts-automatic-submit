package im.bennie;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONObject;
import im.bennie.component.CourseComponent;
import im.bennie.consts.UnitTypeEnum;
import im.bennie.model.ResponseObject;
import im.bennie.model.Unit;
import im.bennie.model.Video;
import im.bennie.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static im.bennie.consts.UnitTypeEnum.Video;

/**
 * Created on 11/17 2021.
 * <p>
 * @author Bennie
 */
@Slf4j
public class Main {

    private static Config config;

    public static void main(String[] args) {
        config = Config.getInstance();
        run();
    }

    private static void run() {

        CourseComponent cc = new CourseComponent(Config.CLASS_ID, config.getCourseId());
        cc.cacheUnitVideoInfoIfNeed();

        List<Unit> units = cc.getUnits();

        log.info("Total units is: {}", units.size());
        logVideoUnitProgress(units);

        List<Unit> videoUnits = cc.filterUnFinishedVideoUnits(units);
        if (CollectionUtil.isNotEmpty(videoUnits)) {
            Map<String, Video> videoMap = cc.getUnitVideoMap();
            for (Unit u : videoUnits) {
                try {
                    if (UnitTypeEnum.type(u) == Video) {
                        u.setVideoId(videoMap.get(u.getId()).getId());
                        playVideo(u);
                    }
                } catch (Exception e) {
                    log.error("Error while playing video.", e);
                }
            }
        }

        Map<String, Boolean> articleUnits = cc.getArticleUnitProgressMap(units);
        if (CollectionUtil.isNotEmpty(articleUnits)) {
            logArticleUnitProgress(articleUnits);
            Map<String, Object> map = articleUnits.entrySet().stream()
                    .filter(entry -> !entry.getValue())
                    .collect(Collectors.toMap(
                            Map.Entry::getKey, entry -> markArticleUnitOK(entry.getKey()).getCode() != 200
                    ));
            cc.cacheArticleUnitProgress(map);
        }
        logVideoUnitProgress(units);
    }

    private static ResponseObject markArticleUnitOK(String unitId) {
        log.info("Working on article unit [id: {}].", unitId);
        return RequestUtil.markArticleUnitFinished(Config.CLASS_ID, config.getCourseId(), unitId);
    }

    private static void logVideoUnitProgress(List<Unit> units) {
        Map<Boolean, List<Unit>> videoMap = units.stream()
                .filter(UnitTypeEnum::isVideo)
                .collect(Collectors.partitioningBy(u -> u.getProgress_time() != 0));
        log.info("已播放视频单元数：{}，未播放数：{}", videoMap.get(true).size(), videoMap.get(false).size());
    }

    private static void logArticleUnitProgress(Map<String, Boolean> map) {
        long count = map.values().stream().filter(e -> e).count();
        log.info("已标记图文单元数：{}，未标记数：{}", count, map.size() - count);
    }

    public static void playVideo(Unit unit) throws Exception {
        log.info("Working on video unit " +
                         "[id = {}, videoId = {}, title = {}, totalTime = {}, progress = {}, progressTime = {}]. \n",
                 unit.getId(),
                 unit.getVideoId(),
                 unit.getTitle(),
                 unit.getTotal_time(),
                 unit.getProgress(),
                 unit.getProgress_time()
        );

        int total_time = Integer.parseInt(unit.getTotal_time());

        if (unit.getProgress_time() != 0) {
            int progress_time = unit.getProgress_time();
            int remain        = total_time - progress_time;
            if (remain != 0) {
                if (remain < 120) {
                    RequestUtil.updateStudyTime(unit.getVideoId(), total_time, unit.getId(), false, true);
                } else {
                    continuePlayVideo(unit, total_time, progress_time, remain);
                }
            }
        } else {
            playNewVideo(unit, total_time);
        }
    }

    public static void continuePlayVideo(Unit unit, int totalTime, int processTime, int remainTime) throws InterruptedException {
        int count = (int) Math.ceil(remainTime / 60.0);
        if (remainTime < 60) count = 1;
        log.info("There are {}s left to play yet, submit count will be {} times.", remainTime, (int) Math.ceil(count / 2.0));

        int            time = processTime;
        ResponseObject resp = null;
        for (int i = 0; i <= count; i++) {
            if (i % 2 == 0 || i == count) {
                resp = playVideo(unit, totalTime, time, i, count, false);
                if (finishedPlay(resp)) break;
                waitFor2Minutes();
            }
            time = getVideoTime(resp, time) + 60;
        }
    }

    public static void playNewVideo(Unit unit, int totalTime) throws InterruptedException {
        int            count = (int) Math.ceil(totalTime / 60.0);
        ResponseObject resp;
        for (int i = 0, time; i <= count; i++) {
            time = i * 60;
            if (i % 2 == 0 || i == count) {
                resp = playVideo(unit, totalTime, time, i, count, i == 0);
                if (finishedPlay(resp)) break;
                waitFor2Minutes();
            }
        }
    }

    /**
     * Video time as a next submit time.
     */
    public static int getVideoTime(ResponseObject responseObject, int submitTime) {
        if (responseObject == null) return submitTime;
        try {
            return responseObject.getBody().getInt("video_time");
        } catch (Exception e) {
            return submitTime;
        }
    }

    private static void waitFor2Minutes() {
        try {
            TimeUnit.SECONDS.sleep(120);
        } catch (InterruptedException e) {
            // ignore.
        }
    }

    /**
     * @return {@code true} if we finished on video.
     */
    private static boolean finishedPlay(ResponseObject resp) {
        return resp.getCode() == 201 &&
                Integer.parseInt(resp.getBody().getStr("count_time")) ==
                        resp.getBody().getInt("video_time");
    }

    private static void checkResponse(ResponseObject resp) {
        JSONObject body = resp.getBody();
        Integer    code = body.getInt("code", 0);
        if (code == 2002 || code == 2003) {
            log.info("System is going to shutdown.");
            System.exit(0);
        }
    }

    public static ResponseObject playVideo(Unit unit, int totalTime, int submitTime, int num, int count, boolean firstPlay) {
        if (submitTime > totalTime) submitTime = totalTime;
        log.info("Submitting study time at {}s for {} times.", (int) (Math.ceil(num / 2.0)), submitTime);
        ResponseObject resp = RequestUtil.updateStudyTime(unit.getVideoId(), submitTime, unit.getId(), firstPlay, num == count);
        checkResponse(resp);
        return resp;
    }


}
