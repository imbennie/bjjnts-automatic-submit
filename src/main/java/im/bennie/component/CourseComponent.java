package im.bennie.component;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.json.JSONUtil;
import im.bennie.consts.RedisConst;
import im.bennie.model.Course;
import im.bennie.model.Sections;
import im.bennie.model.Unit;
import im.bennie.model.Video;
import im.bennie.util.RedisUtil;
import im.bennie.util.RequestUtil;
import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Created on 11/20 2021.
 * @author Bennie
 */
@Data
@Slf4j
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
    public List<Course> listCourses() {
        courses = RequestUtil.listCourseInfo(courseId, classId);
        return courses;
    }

    /**
     * Cache unit info of the course.
     */
    public void cacheCourseUnit() {
        rmCourseUnitCache();
        log.info("Caching course units info..");
        List<Unit> units = listUnits();
        String     key   = getCourseUnitKey();
        log.debug("key: {}", key);

        Map<String, Object> unitMap = units.stream()
                .collect(Collectors.toMap(
                        Unit::getId,
                        JSONUtil::toJsonStr
                ));
        try {
            RedisUtil.hSet(key, unitMap);
        } catch (Exception e) {
            log.error("Error while caching units info", e);
        }
        log.info("Caching finished.");
    }

    public void rmCourseUnitCache() {
        try {
            if (RedisUtil.hasKeys(getCourseUnitKey())) RedisUtil.del();
        } catch (Exception e) {
            log.error("Error while removing unit key.");
        }
    }

    public Map<String, Video> getUnitVideoMap() {
        return unitVideoMap != null ? unitVideoMap : retrieveUnitVideoFromCache();
    }

    private Map<String, Video> retrieveUnitVideoFromCache() {
        String key = getUnitVideoKey();
        try {
            if (!RedisUtil.hasKeys(key)) {
                throw new RuntimeException("Can't find unit-video info from cache.");
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
    public void cacheVideoInfoIfNeed() {
        try {
            if (!RedisUtil.hasKeys(getUnitVideoKey())) cacheVideoInfo();
        } catch (Exception e) {
            log.error("error", e);
        }
    }

    /**
     * Build a map of unit and video.
     */
    public void cacheVideoInfo() {
        log.info("Caching video info mapped by unit id.");
        List<Unit> units = listUnits();
        Map<String, Video> map = units.stream()
                .collect(Collectors.toMap(
                        Unit::getId,
                        unit -> RequestUtil.getUnitVideoInfo(unit.getId(), classId)
                ));


        this.unitVideoMap = map;

        Map<String, Object> cacheMap = map.entrySet().stream()
                .collect(Collectors.toMap(Map.Entry::getKey, JSONUtil::toJsonStr));

        String k = getUnitVideoKey();
        log.debug("key: {}", k);
        try {
            RedisUtil.hSet(k, cacheMap);
        } catch (Exception e) {
            log.error("Error on caching", e);
        }
        log.info("Caching finished.");
    }

    private String getUnitVideoKey() {
        return RedisUtil.getKey(courseId, RedisConst.UNIT_VIDEO_KEY);
    }

    public List<Course> getCourses() {
        return courses != null ? courses : listCourses();
    }

    private List<Unit> retrieveCourseUnitsFromCache() {
        String key = getCourseUnitKey();
        try {
            if (!RedisUtil.hasKeys(key)) {
                return Collections.emptyList();
            }
        } catch (Exception e) {
            // ignore.
        }

        LinkedHashMap<String, Object> unitMap = null;
        try {
            unitMap = RedisUtil.entries(key);
        } catch (Exception e) {
            log.error("Error while retrieve course units.", e);
        }
        return CollectionUtil.isEmpty(unitMap) ? Collections.emptyList() :
                unitMap.values()
                        .stream()
                        .map(e -> JSONUtil.toBean((String) e, Unit.class))
                        .collect(Collectors.toList());
    }

    public List<Unit> getUnits() {
        List<Unit> units;
        if (this.courses != null) {
            units = listUnits();
        } else {
            units = retrieveCourseUnitsFromCache();
        }
        return units != null ? units : listUnits();
    }

    private List<Unit> listUnits() {
        return getCourses()
                .stream()
                .filter(Objects::nonNull)
                .flatMap(c -> {
                             List<Sections> sections = c.getSections();
                             Stream<Unit>   us;
                             if (CollectionUtil.isNotEmpty(sections)) {
                                 us = sections.stream()
                                         .filter(Objects::nonNull)
                                         .flatMap(s -> s.getUnits().stream());
                             } else {
                                 String units = c.getUnits();
                                 us = JSONUtil.parseArray(units).toList(Unit.class)
                                         .stream();
                             }
                             return us.filter(unit -> Integer.parseInt(unit.getTotal_time()) != 0);
                         }
                )
                .collect(Collectors.toList());
    }

    private String getCourseUnitKey() {
        return RedisUtil.getKey(courseId, RedisConst.UNIT_INFO_KEY);
    }
}
