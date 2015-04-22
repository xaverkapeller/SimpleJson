package wrdlbrnft.github.com.simplejsondemo.model;

import com.github.wrdlbrnft.simplejson.annotations.JsonEntity;
import com.github.wrdlbrnft.simplejson.annotations.Key;

/**
 * Created by kapeller on 22/04/15.
 */
@JsonEntity
public interface ChildModelB {

    @Key("text")
    public String getText();

    @Key("asdf")
    public double getValue();
}
