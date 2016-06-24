package com.github.wrdlbrnft.simplejson.builder.retrofit;

import com.github.wrdlbrnft.codebuilder.executables.Method;
import com.github.wrdlbrnft.codebuilder.executables.Methods;

/**
 * Created by kapeller on 21/06/16.
 */

abstract class BaseRequestConverterBuilder implements ConverterBuilder {

    static final Method METHOD_MEDIA_TYPE_PARSE = Methods.stub("parse");
    static final Method METHOD_REQUEST_BODY_CREATE = Methods.stub("create");
    static final String MEDIA_TYPE_JSON = "application/json; charset=UTF-8";
}
