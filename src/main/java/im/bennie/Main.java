package im.bennie;

import cn.hutool.core.lang.Assert;
import im.bennie.component.CourseComponent;
import im.bennie.model.ResponseObject;
import im.bennie.model.Unit;
import im.bennie.model.Video;
import im.bennie.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * Created on 11/17 2021.
 * <p>
 * @author Bennie
 */
@Slf4j
public class Main {

    // private static final Integer courseId = 446; // 大数据
    // private static final Integer courseId = Integer.valueOf(System.getProperty("course_id")); //406 物联网工程导论
    private static final Integer courseId = 406; //406 物联网工程导论

    private static Config config;

    public static void main(String[] args) {
        config = Config.getInstance();
        run();
    }


    private static void run() {

        CourseComponent cc = new CourseComponent(Config.CLASS_ID, config.getCourseId());
        cc.cacheCourseUnit();
        cc.cacheVideoInfoIfNeed();

        List<Unit> unitList = cc.getUnits();
        logoutProgress(unitList);

        List<Unit> units = unitList
                .stream()
                .filter(n -> n.getProgress_time() != Integer.parseInt(n.getTotal_time()))
                .collect(Collectors.toList());

        Map<String, Video> videoMap = cc.getUnitVideoMap();

        for (Unit u : units) {
            try {
                u.setVideoId(videoMap.get(u.getId()).getId());
                playVideo(u);
            } catch (Exception e) {
                log.error("Error while play video.", e);
            }
        }

    }

    private static void logoutProgress(List<Unit> unitList) {
        Assert.notEmpty(unitList);
        Map<Boolean, List<Unit>> partitionMap = unitList.stream().collect(Collectors.partitioningBy(u -> u.getProgress_time() != 0));
        log.info("总视频数：{}，已播放视频数：{}，未播放视频数：{}", unitList.size(), partitionMap.get(true).size(), partitionMap.get(false).size());
    }


    public static void playVideo(Unit unit) throws Exception {
        System.out.println("================================================================");
        log.info("正在处理 unit id: {}, video id: {}, title: {}, totalTime: {}, progress: {}, progressTime: {}",
                 unit.getId(),
                 unit.getVideoId(),
                 unit.getTitle(),
                 unit.getTotal_time(),
                 unit.getProgress(),
                 unit.getProgress_time()
        );

        int total_time = Integer.parseInt(unit.getTotal_time());

        if (total_time == unit.getProgress_time()) {
            log.info("当前视频已播放完成，跳过处理。");
        }

        if (unit.getProgress_time() != 0) {
            int progress_time = unit.getProgress_time();
            int remain        = total_time - progress_time;
            if (remain != 0) {
                if (remain < 60) {
                    // 1分钟内的视频，直接提交视频最后的时间。
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
        log.info("还剩{}秒待播放，还需要提交{}次更新时长请求。", remainTime, (int) Math.ceil(count / 2.0));

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
            log.error("Error while getting video time", e);
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
        if (resp.getCode() == 2002 || resp.getCode() == 2003) {
            System.exit(0);
        }
    }

    public static ResponseObject playVideo(Unit unit, int totalTime, int submitTime, int num, int count, boolean firstPlay) {
        if (submitTime > totalTime) submitTime = totalTime;
        log.info("第{}次提交学习时长请求，当前提交播放时间第{}秒处。", (int) (Math.ceil(num / 2.0)), submitTime);
        ResponseObject resp = RequestUtil.updateStudyTime(unit.getVideoId(), submitTime, unit.getId(), firstPlay, num == count);
        checkResponse(resp);
        return resp;
    }


}
