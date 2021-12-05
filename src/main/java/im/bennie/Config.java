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

    private static Config instance;

    public static Config getInstance() {
        return instance != null ? instance : loadConfig();
    }


    private static Config loadConfig() {
        instance = new Config();
        instance.setCourseId(Integer.parseInt(System.getProperty("courseId")));

        String kp;
        try {
            kp = System.getProperty("keyPrefix");
        } catch (Exception e) {
            kp = String.valueOf(System.currentTimeMillis());
        }
        instance.setKeyPrefix(kp);

        boolean lg = Boolean.parseBoolean(System.getProperty("login"));
        instance.setDoLogin(lg);
        if (lg) {
            instance.setUsername(System.getProperty("username"));
            instance.setPassword(System.getProperty("password"));

            UserInfo user  = new LoginComponent().login();
            String   token = user.getAccess_token();
            instance.setUserId(String.valueOf(user.getId()));
            instance.setAccessToken(token);
        } else {
            instance.setUserId(System.getProperty("userId"));
            instance.setAccessToken(System.getProperty("token"));
        }

        log.info(instance.toString());
        return instance;
    }


    public void setAccessToken(String accessToken) {
        this.accessToken = BEARER + accessToken;
    }
}
