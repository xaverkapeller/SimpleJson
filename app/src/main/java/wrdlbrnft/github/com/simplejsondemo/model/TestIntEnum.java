package wrdlbrnft.github.com.simplejsondemo.model;

import com.github.wrdlbrnft.simplejson.annotations.JsonEnum;
import com.github.wrdlbrnft.simplejson.annotations.MapInt;

/**
 * Created by kapeller on 21/04/15.
 */
@JsonEnum
public enum TestIntEnum {

    @MapInt(0)
    VALUE_A,

    @MapInt(1)
    VALUE_B,

    @MapInt(2)
    VALUE_C
}
