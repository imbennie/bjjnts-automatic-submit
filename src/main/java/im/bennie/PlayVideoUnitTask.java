package im.bennie;

import cn.hutool.json.JSONObject;
import im.bennie.model.ResponseObject;
import im.bennie.model.Unit;
import im.bennie.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.TimeUnit;

/**
 * Created on 12/05 2021.
 *
 * @author Bennie
 */
@Slf4j
public class PlayVideoUnitTask implements Runnable {

    private final Unit unit;

    public PlayVideoUnitTask(Unit u) {
        this.unit = u;
    }

    @Override
    public void run() {
        try {
            playVideo(unit);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void playVideo(Unit unit) throws Exception {
        log.info("Working on video unit " +
                        "[id = {}, videoId = {}, title = {}, " +
                        "totalTime = {}, progress = {}, progressTime = {}]. \n",
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

    public void continuePlayVideo(Unit unit, int totalTime, int processTime, int remainTime) {
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

    public void playNewVideo(Unit unit, int totalTime) throws InterruptedException {
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
    public int getVideoTime(ResponseObject responseObject, int submitTime) {
        if (responseObject == null) return submitTime;
        try {
            return responseObject.getBody().getInt("video_time");
        } catch (Exception e) {
            return submitTime;
        }
    }

    private void waitFor2Minutes() {
        try {
            TimeUnit.SECONDS.sleep(120);
        } catch (InterruptedException e) {
            // ignore.
        }
    }

    /**
     * @return {@code true} if we finished on video.
     */
    private boolean finishedPlay(ResponseObject resp) {
        return resp.getCode() == 201 &&
                Integer.parseInt(resp.getBody().getStr("count_time")) ==
                        resp.getBody().getInt("video_time");
    }

    private void shouldExit(ResponseObject resp) {
        JSONObject body = resp.getBody();
        Integer    code = body.getInt("code", 0);

        if (code == 2002 || code == 2003) {
            try {
                wait();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }

        if (code == 3003)
            systemExit();
    }

    private void systemExit() {
        log.info("System is going to shutdown.");
        System.exit(0);
    }

    public ResponseObject playVideo(Unit unit, int totalTime, int submitTime, int num, int count, boolean firstPlay) {
        if (submitTime > totalTime) submitTime = totalTime;
        log.info("Submitting study time at {}s for {} times.", submitTime, ((int) (Math.ceil(num / 2.0)) + 1));
        ResponseObject resp = RequestUtil.updateStudyTime(unit.getVideoId(), submitTime, unit.getId(), firstPlay, num == count);
        shouldExit(resp);
        return resp;
    }
}
