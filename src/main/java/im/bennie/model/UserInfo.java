/**
 * Copyright 2021 json.cn
 */
package im.bennie.model;

import lombok.Data;

/**
 * Auto-generated: 2021-11-19 13:52:1
 * @author json.cn (i@json.cn)
 */
@Data
public class UserInfo {

    private long   id;
    private String sid;
    private int    type;
    private String mobile;
    private String email;
    private String username;
    private String name;
    private String avatar;
    private String access_token;
    private String app_token;
    private int    status;
    private long   create_time;
    private long   update_time;
    private Info   info;
    private int    jx_uid;
    private String jx_password;
    private String jx_salt;
    private int    rid;
    private String idcard_front;
    private String idcard_back;
    private String seller_platform_token;
    private int    is_password;

}