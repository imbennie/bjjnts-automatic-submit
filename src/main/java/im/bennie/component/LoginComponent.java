package im.bennie.component;

import cn.hutool.json.JSONUtil;
import im.bennie.Config;
import im.bennie.model.UserInfo;
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

    private UserInfo userInfo;

    public UserInfo login() {
        String resp = doLogin();
        this.userInfo = JSONUtil.toBean(resp, UserInfo.class);
        return this.userInfo;
    }

    private String doLogin() {
        return RequestUtil.loginAccount(
                config.getUsername(),
                config.getPassword()
        );
    }
}
