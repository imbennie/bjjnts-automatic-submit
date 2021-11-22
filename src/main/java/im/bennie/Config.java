package im.bennie;

import im.bennie.component.LoginComponent;
import im.bennie.model.UserInfo;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Created on 11/20 2021.
 * @author Bennie
 */
@Slf4j
@Data
public class Config {
    public static final  int    CLASS_ID = 27779;
    private static final String BEARER   = "Bearer ";

    public  int     courseId;
    public  String  keyPrefix;
    private boolean doLogin;

    public  String userId;
    public  String username;
    public  String password;
    private String accessToken;

    private Config() {}

    private static Config c;

    public static Config getInstance() {
        return c != null ? c : loadConfig();
    }


    private static Config loadConfig() {
        c = new Config();
        c.setCourseId(Integer.parseInt(System.getProperty("courseId")));

        String kp;
        try {
            kp = System.getProperty("keyPrefix");
        } catch (Exception e) {
            kp = String.valueOf(System.currentTimeMillis());
        }
        c.setKeyPrefix(kp);

        boolean lg = Boolean.parseBoolean(System.getProperty("login"));
        c.setDoLogin(lg);
        if (lg) {
            c.setUsername(System.getProperty("username"));
            c.setPassword(System.getProperty("password"));

            UserInfo user  = new LoginComponent().login();
            String   token = user.getAccess_token();
            c.setUserId(String.valueOf(user.getId()));
            c.setAccessToken(token);
        } else {
            c.setUserId(System.getProperty("userId"));
            c.setAccessToken(System.getProperty("token"));
        }

        log.info(c.toString());
        return c;
    }


    public void setAccessToken(String accessToken) {
        this.accessToken = BEARER + accessToken;
    }
}
