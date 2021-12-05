package im.bennie;

import cn.hutool.core.collection.CollectionUtil;
import im.bennie.component.CourseComponent;
import im.bennie.consts.UnitTypeEnum;
import im.bennie.model.ResponseObject;
import im.bennie.model.Unit;
import im.bennie.model.Video;
import im.bennie.util.RequestUtil;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static im.bennie.consts.UnitTypeEnum.Video;

/**
 * Created on 11/17 2021.
 * <p>
 *
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
                        // playVideo(u);
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

        log.info("Program running over, time to exit.");
    }

    private static ResponseObject markArticleUnitOK(String unitId) {
        log.info("Working on article unit(id: {}).", unitId);
        return RequestUtil.markArticleUnitFinished(Config.CLASS_ID, config.getCourseId(), unitId);
    }

    private static void logVideoUnitProgress(List<Unit> units) {
        Map<Boolean, List<Unit>> videoMap = units.stream()
                .filter(UnitTypeEnum::isVideo)
                .collect(Collectors.partitioningBy(u -> u.getProgress_time() != 0));
        log.info("Video units, Finished: {}，Unfinished: {}.", videoMap.get(true).size(), videoMap.get(false).size());
    }

    private static void logArticleUnitProgress(Map<String, Boolean> map) {
        long count = map.values().stream().filter(e -> e).count();
        log.info("Article units, Finished: {}, Unfinished: {}.", count, map.size() - count);
    }



}
