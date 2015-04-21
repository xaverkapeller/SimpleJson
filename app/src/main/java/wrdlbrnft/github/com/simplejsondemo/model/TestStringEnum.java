package wrdlbrnft.github.com.simplejsondemo.model;

import com.github.wrdlbrnft.simplejson.annotations.JsonEnum;
import com.github.wrdlbrnft.simplejson.annotations.MapString;

/**
 * Created by kapeller on 21/04/15.
 */
@JsonEnum
public enum TestStringEnum {

    @MapString("testA")
    VALUE_A,

    @MapString("testB")
    VALUE_B,

    @MapString("testC")
    VALUE_C
}
