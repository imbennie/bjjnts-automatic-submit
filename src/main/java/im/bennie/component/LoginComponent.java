package im.bennie.component;

import cn.hutool.json.JSONUtil;
import im.bennie.Config;
import im.bennie.consts.RedisConst;
import im.bennie.model.UserInfo;
import im.bennie.util.RedisUtil;
import im.bennie.util.RequestUtil;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;

/**
 * Created on 11/19 2021.
 * <p>
 * @author Bennie
 */
@Slf4j
@Data
public class LoginComponent {

    private static final Config config = Config.getInstance();

    public static void main(String[] args) {
        UserInfo userInfo = new LoginComponent().getUserInfo();
        System.out.println("userInfo = " + userInfo);
    }

    private UserInfo userInfo;

    /**
     * Trying to retrieve from cache first if it's {@code null}
     * or via a logging if we need to do so.
     */
    public UserInfo getUserInfo() {
        if (userInfo == null) {
            UserInfo u = retrieveUserInfo();
            return u != null ? u : login();
        }
        return userInfo;
    }

    public UserInfo retrieveUserInfo() {
        try {
            String s = (String) RedisUtil.get(getUserKey());
            if (s != null) {
                this.userInfo = JSONUtil.toBean(s, UserInfo.class);
            }
            return this.userInfo;
        } catch (Exception e) {
            throw new RuntimeException("Retrieving user info failed.", e);
        }
    }

    public UserInfo login() {
        String resp = doLogin();
        this.userInfo = JSONUtil.toBean(resp, UserInfo.class);
        return this.userInfo;
    }

    private String doLogin() {
        String respBody = RequestUtil.loginAccount(
                config.getUsername(),
                config.getPassword()
        );
        cacheUserInfo(respBody);
        return respBody;
    }

    private void cacheUserInfo(String userInfoJson) {
        log.info("Caching user info...");
        try {
            RedisUtil.set(getUserKey(), userInfoJson);
        } catch (Exception e) {
            throw new RuntimeException("Caching user info failed.", e);
        }
        log.info("Finished Caching.");
    }

    private static String getUserKey() {
        return RedisUtil.getKey(RedisConst.USER_INFO_KEY, config.getUserId());
    }

}
