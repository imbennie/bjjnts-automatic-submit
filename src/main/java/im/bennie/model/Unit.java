package im.bennie.model;

import lombok.Data;

import java.util.List;

/**
 * Created on 11/17 2021.
 * <p>
 *
 * @author Bennie
 */
@Data
public class Unit {
    private String       id;
    private String       bid;
    private String       open_id;
    private String       chapter_id;
    private String       section_id;
    private String       type;
    private String       title;
    private String       description;
    private String       total_time;
    private String       total_question;
    private String       sort;
    private String       hide;
    private String       create_time;
    private String       update_time;
    private String       is_see;
    private TypeName     typeName;
    private List<String> files;
    private String       order_number;
    private String       train;
    private String       lastTrain;
    private String       unitMap;
    private int          lastUnitId;
    private int          progress_time;
    private int          progress;
    private String       videoId;
}
