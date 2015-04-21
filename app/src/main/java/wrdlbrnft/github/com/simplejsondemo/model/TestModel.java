package wrdlbrnft.github.com.simplejsondemo.model;

import com.github.wrdlbrnft.simplejson.annotations.JsonEntity;
import com.github.wrdlbrnft.simplejson.annotations.Key;

import java.util.List;
import java.util.Set;

/**
 * Created by kapeller on 21/04/15.
 */
@JsonEntity
public interface TestModel {

    @Key("int_enums")
    public List<TestIntEnum> getIntEnums();

    @Key("string_enums")
    public Set<TestStringEnum> getStringEnums();

    @Key("text")
    public String getText();
}
