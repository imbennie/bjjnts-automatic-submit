package im.bennie.model;

import cn.hutool.json.JSONObject;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Created on 11/19 2021.
 * <p>
 * @author Bennie
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ResponseObject {
    private int        code;
    private JSONObject body;

    public static ResponseObject onException() {
        return new ResponseObject(-1, null);
    }
}
