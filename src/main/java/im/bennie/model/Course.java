package im.bennie.model;

import lombok.Data;

import java.util.List;

/**
 * Created on 11/17 2021.
 * <p>
 * @author Bennie
 */
@Data
public class Course {
    private String         id;
    private String         course_id;
    private String         title;
    private String         description;
    private String         sort;
    private String         hide;
    private String         create_time;
    private String         update_time;
    private List<Sections> sections;
    private String         units;
    private String         order_number;
}
