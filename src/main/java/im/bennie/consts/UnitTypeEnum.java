package im.bennie.consts;

import im.bennie.model.Unit;

import java.util.Arrays;

/**
 * Created on 11/23 2021.
 * <p>
 * @author Bennie
 */
public enum UnitTypeEnum {
    Video(1),
    Article(3);


    private int type;

    UnitTypeEnum(int type) {
        this.type = type;
    }

    public int getType() {
        return type;
    }

    public static UnitTypeEnum type(Unit u) {
        return Arrays.stream(values()).filter(i -> i.getType() == Integer.parseInt(u.getType()))
                .findFirst()
                .orElseThrow(
                        () -> new RuntimeException("无法确定单元类型。")
                );
    }

    public static boolean isArticle(Unit u) {
        return Integer.parseInt(u.getType()) == Article.getType();
    }

    public static boolean isVideo(Unit u) {
        return Integer.parseInt(u.getType()) == Video.getType();
    }
}
