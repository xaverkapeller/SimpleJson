package com.github.wrdlbrnft.simplejson.builder.retrofit;

import com.github.wrdlbrnft.codebuilder.types.Type;

import java.io.IOException;

/**
 * Created by kapeller on 21/06/16.
 */

interface ConverterBuilder {
    Type build(String packageName) throws IOException;
}
