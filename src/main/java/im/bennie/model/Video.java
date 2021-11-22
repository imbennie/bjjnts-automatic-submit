package im.bennie.model;

import cn.hutool.json.JSONUtil;
import lombok.Data;

/**
 * Created on 11/17 2021.
 * <p>
 * @author Bennie
 */
@Data
public class Video {
    private String id;
    private String open_id;
    private String course_id;
    private String unit_id;
    private String type;
    private String image;
    private String description;
    private String url;
    private String vod_id;
    private String time;
    private String size;
    private String total_url;
    private String status;
    private String sort;
    private String hide;
    private String create_time;
    private String update_time;
    private String remarks;

    @Override
    public String toString() {
        return JSONUtil.toJsonStr(this);
    }
}
