package wrdlbrnft.github.com.simplejsondemo.model;

import com.github.wrdlbrnft.simplejson.annotations.JsonEntity;
import com.github.wrdlbrnft.simplejson.annotations.Key;
import com.github.wrdlbrnft.simplejson.annotations.Optional;

import java.util.List;
import java.util.Set;

/**
 * Created by kapeller on 21/04/15.
 */
@JsonEntity
public interface TestModel {

    @Key("int_enums")
    public List<TestIntEnum> getIntEnums();

    @Optional
    @Key("string_enums")
    public Set<TestStringEnum> getStringEnums();

    @Optional
    @Key("text")
    public String getText();

    @Key("root")
    public RootModel getRootChild();

    @Optional
    @Key("children")
    public List<ChildModelB> getChildren();
}
