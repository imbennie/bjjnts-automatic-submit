package im.bennie.model;

import lombok.Data;

import java.util.List;

/**
 * Created on 11/17 2021.
 * <p>
 * @author Bennie
 */
@Data
public class Sections {
    private String     id;
    private String     bid;
    private String     open_id;
    private String     chapter_id;
    private String     title;
    private String     description;
    private String     sort;
    private String     hide;
    private String     create_time;
    private String     update_time;
    private List<Unit> units;
    private String     order_number;
}
