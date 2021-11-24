package im.bennie.util;

import cn.hutool.json.JSONObject;
import cn.hutool.json.JSONUtil;
import im.bennie.Config;
import im.bennie.model.Course;
import im.bennie.model.ResponseObject;
import im.bennie.model.Video;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.ResponseHandler;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.util.List;

import static im.bennie.Config.CLASS_ID;

/**
 * Created on 11/20 2021.
 *
 * @author Bennie
 */
@Slf4j
public class RequestUtil {

    private static final String SEC_CH_UA = "\"Google Chrome\";v=\"95\", \"Chromium\";v=\"95\", \";Not A Brand\";v=\"99\"";
    private static final String UA        = "Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:94.0) Gecko/20100101 Firefox/94.0";

    private static final String LOGIN_URL             = "https://apif.bjjnts.cn/account/login";
    private static final String LIST_COURSE_URL       = "https://apif.bjjnts.cn/courses/test-preview?course_id=%d&class_id=%d";
    private static final String SUBMIT_STUDY_TIME_URL = "https://apistudy.bjjnts.cn/studies/study?video_id=%s&u=%s&time=%d&unit_id=%s&class_id=%d";
    private static final String MARK_ARTICLE_UNIT_URL = "https://apif.bjjnts.cn/course-unit-maps";

    private static final Config config = Config.getInstance();

    public static ResponseObject markArticleUnitFinished(int classId, int courseId, String unitId) {
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(MARK_ARTICLE_UNIT_URL);
            setHeader(httpPost);
            httpPost.setHeader("Referer", String.format("www.bjjnts.cn/study/courseware?course_id=%s&unit_id=%s&class_id=%s",
                                                        courseId, unitId, classId));
            String data = String.format("{\"course_id\":\"%d\",\"unit_id\":\"%s\",\"class_id\":\"%d\"}",
                                        courseId, unitId, classId);
            httpPost.setEntity(new StringEntity(data, ContentType.APPLICATION_JSON));
            return httpClient.execute(httpPost, responseObjectHandler());
        } catch (IOException e) {
            log.error("Error", e);
            return ResponseObject.onException();
        }
    }

    /**
     * @param firstStart Set true if It's first time to submit study time.
     * @param lastEnd    Set true if It's last time submit study time.
     */
    public static ResponseObject updateStudyTime(String videoId, int time, String unitId, boolean firstStart,
                                                 boolean lastEnd) {

        String urlStr = String.format(SUBMIT_STUDY_TIME_URL, videoId, config.getUserId(), time, unitId, CLASS_ID);

        if (lastEnd) urlStr += "&end=1";
        if (firstStart) urlStr += "&start=1";

        log.info("Requesting to update study time.");
        logRequestUrl(urlStr);

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost post = new HttpPost(urlStr);
            setHeader(post);
            post.setHeader("Referer", String.format("https://www.bjjnts.cn/study/video?course_id=%d&class_id%d&unit_id=%s",
                                                    config.getCourseId(), CLASS_ID, unitId));
            ResponseObject object = httpClient.execute(post, responseObjectHandler());
            log.info("Result: {}", object != null ? object.getBody().toString() : "Abnormal response.");
            return object;
        } catch (IOException e) {
            log.error("Error while request to submit study time", e);
        }
        return ResponseObject.onException();
    }


    public static String loginAccount(String username, String password) {
        String param = "{\"username\":\"" + username + "\",\"password\":\"" + password + "\",\"type\":1}";
        log.info("Logging into your account username = {}, password = {}", username, password);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpPost httpPost = new HttpPost(LOGIN_URL);
            setHeader(httpPost);
            httpPost.removeHeaders("Authorization");
            httpPost.setHeader("Referer", LOGIN_URL);
            httpPost.setEntity(new StringEntity(param, ContentType.APPLICATION_JSON));
            String responseBody = httpClient.execute(httpPost, bodyHandler());
            log.debug("Login response: {}", responseBody);
            return responseBody;
        } catch (Exception e) {
            throw new RuntimeException("Login failed!", e);
        }
    }

    public static List<Course> listCourseInfo(int courseId, int classId) {
        String urlStr = String.format(LIST_COURSE_URL, courseId, classId);
        log.info("Requesting for list courses(id = {}) info", courseId);
        logRequestUrl(urlStr);

        List<Course> courses;

        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(urlStr);
            setHeader(get);
            get.setHeader("Referer", String.format("https://www.bjjnts.cn/study?course_id=%d&class_id%d",
                                                   courseId, classId));

            String     responseBody = httpClient.execute(get, bodyHandler());
            JSONObject jsonObject   = JSONUtil.parseObj(responseBody);
            courses = jsonObject.getJSONArray("course").toList(Course.class);
        } catch (IOException e) {
            throw new RuntimeException("Error", e);
        }
        return courses;
    }

    public static Video getUnitVideoInfo(String unitId, int classId) {
        String url = String.format("https://apif.bjjnts.cn/course-units/%s?class_id%d", unitId, classId);
        try (CloseableHttpClient httpClient = HttpClients.createDefault()) {
            HttpGet get = new HttpGet(url);
            setHeader(get);

            String     body       = httpClient.execute(get, bodyHandler());
            JSONObject jsonObject = JSONUtil.parseObj(body);
            JSONObject jVideo     = jsonObject.getJSONObject("video");
            return jVideo.toBean(Video.class);
        } catch (IOException e) {
            log.error("Error", e);
            return new Video();
        }
    }

    private static ResponseHandler<ResponseObject> responseObjectHandler() {
        return response -> new ResponseObject(response.getStatusLine().getStatusCode(),
                                              JSONUtil.parseObj(RequestUtil.getBody(response)));
    }

    private static String getBody(HttpResponse response) {
        HttpEntity entity = response.getEntity();
        String     body;
        try {
            body = entity != null ? EntityUtils.toString(entity) : null;
            return body;
        } catch (IOException e) {
            throw new RuntimeException("Failed to parse response body.", e);
        }

    }

    private static ResponseHandler<String> bodyHandler() {
        return RequestUtil::getBody;
    }

    private static void setHeader(HttpRequestBase req) {
        req.setHeader("Connection", "keep-alive");
        req.setHeader("sec-ch-ua", SEC_CH_UA);
        req.setHeader("DNT", "1");
        req.setHeader("sec-ch-ua-mobile", "?0");
        req.setHeader("Authorization", config.getAccessToken());
        req.setHeader("Content-Type", "application/json");
        req.setHeader("Accept", "application/json, text/plain, */*");
        req.setHeader("User-Agent", UA);
        req.setHeader("X-Client-Type", "pc");
        req.setHeader("sec-ch-ua-platform", "\"Windows\"");
        req.setHeader("Origin", "https://www.bjjnts.cn");
        req.setHeader("Sec-Fetch-Site", "same-site");
        req.setHeader("Sec-Fetch-Mode", "cors");
        req.setHeader("Sec-Fetch-Dest", "empty");
        req.setHeader("Accept-Language", "zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7,en-GB;q=0.6");
    }

    private static void logRequestUrl(String url) {
        log.debug("Request URL: {}", url);
    }

}
