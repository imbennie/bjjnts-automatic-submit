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
 *
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

    public Map<String, Video> getUnitVideoMap() {
        return unitVideoMap != null ? unitVideoMap : retrieveUnitVideoFromCache();
    }

    private Map<String, Video> retrieveUnitVideoFromCache() {
        log.info("Retrieving unit-video mapping from cache.");
        String key = getUnitVideoMappingKey();
        try {
            if (!RedisUtil.hasKeys(key)) {
                throw new RuntimeException("Can't find unit-video mapping in cache.");
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
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
     * Build a mapping of unit and video.
     * This should be cached only once.
     */
    public void cacheUnitVideoInfo() {
        String key = getUnitVideoMappingKey();
        log.info("Caching unit-video relation mapping. Key: {}", key);
        List<Unit> videoUnits = getUnits(UnitTypeEnum::isVideo);
        Map<String, Video> vm = videoUnits.stream()
                .collect(Collectors.toMap(
                        Unit::getId,
                        unit -> RequestUtil.getUnitVideoInfo(unit.getId(), classId)
                ));

        this.unitVideoMap = vm;

        Map<String, Object> cache = vm.entrySet().stream()
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        entry -> JSONUtil.toJsonStr(entry.getValue())
                ));

        try {
            RedisUtil.hSet(key, cache);
        } catch (Exception e) {
            log.error("Error on caching", e);
        }
        log.info("Caching finished.");
    }


    public List<Course> getCourses() {
        return courses != null ? courses : requestCourses();
    }

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
     * Save unfinished article units to cache, We can find out
     * which are unfinished to be continued to deal with it later.
     *
     * @param map key: article unit id, value: true/false.
     */
    public void cacheArticleUnitProgress(Map<String, Object> map) {
        log.info("Caching article unit study progress.");
        String key = getArticleUnitKey();
        try {
            if (RedisUtil.hasKeys(key)) RedisUtil.del(key);
            RedisUtil.hSet(key, map);
        } catch (Exception e) {
            log.error("Error while caching course article units.", e);
        }
        log.info("Caching finished.");
    }

    public Map<String, Boolean> getArticleUnitProgressMap(List<Unit> units) {
        log.info("Getting article unit study progress.");
        String key = getArticleUnitKey();
        try {
            if (RedisUtil.hasKeys(key)) {
                LinkedHashMap<String, Object> entries = RedisUtil.entries(key);
                return entries.entrySet().stream()
                        .collect(Collectors.toMap(Map.Entry::getKey, entry -> (boolean) entry.getValue()));
            }
        } catch (Exception e) {
            log.error("Error while retrieve from cache.", e);
        }
        // Consider all article units are unfinished.
        return filterArticleUnits(units).stream().collect(Collectors.toMap(Unit::getId, u -> false));
    }
}
