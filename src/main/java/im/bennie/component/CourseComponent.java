package im.bennie.component;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import im.bennie.consts.RedisConst;
import im.bennie.consts.UnitTypeEnum;
import im.bennie.model.Course;
import im.bennie.model.Sections;
import im.bennie.model.Unit;
import im.bennie.model.Video;
import im.bennie.util.RedisUtil;
import im.bennie.util.RequestUtil;
import lombok.*;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created on 11/20 2021.
 * @author Bennie
 */
@Data
@Slf4j
@ToString(doNotUseGetters = true)
@NoArgsConstructor
public class CourseComponent {

    @Setter(value = AccessLevel.PRIVATE)
    private List<Course> courses;

    @Setter(value = AccessLevel.PRIVATE)
    private Map<String, Video> unitVideoMap;

    private int classId;
    private int courseId;

    public CourseComponent(int classId, int courseId) {
        this.classId  = classId;
        this.courseId = courseId;
    }

    /**
     * List out courses info by requesting API.
     */
    public List<Course> requestCourses() {
        courses = RequestUtil.listCourseInfo(courseId, classId);
        return courses;
    }

    /**
     * Cache unit info of the course.
     */
    // public void cacheCourseVideoUnit() {
    //     rmCourseUnitCache();
    //     log.info("Caching course units info..");
    //     List<Unit> units = listUnits();
    //     String     key   = getVideoUnitKey();
    //     log.debug("key: {}", key);
    //
    //     Map<String, Object> unitMap = units.stream()
    //             .collect(Collectors.toMap(
    //                     Unit::getId,
    //                     JSONUtil::toJsonStr
    //             ));
    //     try {
    //         RedisUtil.hSet(key, unitMap);
    //     } catch (Exception e) {
    //         log.error("Error while caching units info", e);
    //     }
    //     log.info("Caching finished.");
    // }

    // public void rmCourseUnitCache() {
    //     try {
    //         String key = getVideoUnitKey();
    //         if (RedisUtil.hasKeys(key)) RedisUtil.del(key);
    //     } catch (Exception e) {
    //         log.error("Error while removing unit key.", e);
    //     }
    // }
    public Map<String, Video> getUnitVideoMap() {
        return unitVideoMap != null ? unitVideoMap : retrieveUnitVideoFromCache();
    }

    private Map<String, Video> retrieveUnitVideoFromCache() {
        String key = getUnitVideoMappingKey();
        try {
            if (!RedisUtil.hasKeys(key)) {
                log.info("There's no unit-video mapping in cache.");
            }
        } catch (Exception e) {
            // ignore.
        }

        Map<String, Object> unitVideoMap = null;
        try {
            unitVideoMap = RedisUtil.entries(key);
        } catch (Exception e) {
            log.error("Error while retrieve unit video.", e);
        }
        return CollectionUtil.isEmpty(unitVideoMap) ? Collections.emptyMap() :
                unitVideoMap.entrySet()
                        .stream()
                        .collect(Collectors.toMap(
                                Map.Entry::getKey,
                                entry -> JSONUtil.toBean((String) entry.getValue(), Video.class)
                        ));
    }

    /**
     * Cache if and only if key doesn't exist.
     */
    public void cacheUnitVideoInfoIfNeed() {
        try {
            if (!RedisUtil.hasKeys(getUnitVideoMappingKey())) cacheUnitVideoInfo();
        } catch (Exception e) {
            log.error("error", e);
        }
    }

    /**
     * Build a map of unit and video.
     * This should be cache only once.
     */
    public void cacheUnitVideoInfo() {
        String k = getUnitVideoMappingKey();
        log.info("Caching unit-video mapping, key: {}", k);
        List<Unit> videoUnits = getUnits(UnitTypeEnum::isVideo);
        Map<String, Video> map = videoUnits.stream()
                .collect(Collectors.toMap(
                        Unit::getId,
                        unit -> RequestUtil.getUnitVideoInfo(unit.getId(), classId)
                ));


        this.unitVideoMap = map;

        Map<String, Object> cacheMap = map.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> JSONUtil.toJsonStr(entry.getValue())
                ));

        try {
            RedisUtil.hSet(k, cacheMap);
        } catch (Exception e) {
            log.error("Error on caching", e);
        }
        log.info("Caching finished.");
    }


    public List<Course> getCourses() {
        return courses != null ? courses : requestCourses();
    }

    // private List<Unit> retrieveVideoUnitsFromCache() {
    //     String key = getVideoUnitKey();
    //     try {
    //         if (!RedisUtil.hasKeys(key)) {
    //             return Collections.emptyList();
    //         }
    //     } catch (Exception e) {
    //         // ignore.
    //     }
    //
    //     LinkedHashMap<String, Object> unitMap = null;
    //     try {
    //         unitMap = RedisUtil.entries(key);
    //     } catch (Exception e) {
    //         log.error("Error while retrieve course units.", e);
    //     }
    //     return CollectionUtil.isEmpty(unitMap) ? Collections.emptyList() :
    //             unitMap.values()
    //                     .stream()
    //                     .map(e -> JSONUtil.toBean((String) e, Unit.class))
    //                     .collect(Collectors.toList());
    // }


    public List<Unit> getUnits(Predicate<Unit> p) {
        return getCourses()
                .stream()
                .filter(Objects::nonNull)
                .flatMap(unitStream())
                .filter(p)
                .collect(Collectors.toList());
    }

    public List<Unit> getUnits() {
        return getCourses()
                .stream()
                .filter(Objects::nonNull)
                .flatMap(unitStream())
                .collect(Collectors.toList());
    }

    private Function<Course, Stream<Unit>> unitStream() {
        return course -> {
            List<Sections> sections = course.getSections();
            Stream<Unit>   us;
            if (CollectionUtil.isNotEmpty(sections)) {
                us = sections.stream()
                        .filter(Objects::nonNull)
                        .flatMap(section -> section.getUnits().stream());
            } else {
                us = JSONUtil.parseArray(course.getUnits()).toList(Unit.class).stream();
            }
            return us;
        };
    }

    private String getUnitVideoMappingKey() {
        return RedisUtil.getKey0("course", courseId, RedisConst.UNIT_VIDEO_MAPPING_KEY);
    }

    // private String getVideoUnitKey() {
    //     return RedisUtil.getKey(courseId, RedisConst.VIDEO_UNIT_INFO_KEY);
    // }

    private String getArticleUnitKey() {
        return RedisUtil.getKey("course", courseId, RedisConst.ARTICLE_UNIT_INFO_KEY);
    }

    public List<Unit> filterUnits(List<Unit> units, Predicate<Unit> p) {
        return units.stream().filter(p).collect(Collectors.toList());
    }

    public List<Unit> filterUnFinishedVideoUnits(List<Unit> units) {
        return filterUnits(units, u ->
                UnitTypeEnum.type(u) == UnitTypeEnum.Video &&
                        u.getTotal_time() != null &&
                        u.getProgress_time() != Integer.parseInt(u.getTotal_time())
        );
    }

    public List<Unit> filterArticleUnits(List<Unit> units) {
        return filterUnits(units, UnitTypeEnum::isArticle);
    }

    /**
     * Cache unfinished article units.
     */
    public void cacheArticleUnitProgress(Map<String, Object> articleUnitProgressMap) {

        String key = getArticleUnitKey();
        try {
            if (RedisUtil.hasKeys(key)) RedisUtil.del(key);
            RedisUtil.hSet(key, articleUnitProgressMap);
        } catch (Exception e) {
            throw new RuntimeException("Error while caching course article units.", e);
        }
    }

    public Map<String, Boolean> getArticleUnitProgressMap(List<Unit> units) {
        String key = getArticleUnitKey();
        try {
            if (RedisUtil.hasKeys(key)) {
                LinkedHashMap<String, Object> entries = RedisUtil.entries(key);
                return entries.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> (boolean) entry.getValue()));
            }
        } catch (Exception e) {
            log.error("Error while retrieve from redis", e);
        }
        return filterArticleUnits(units).stream().collect(Collectors.toMap(Unit::getId, u -> false));
    }
}
