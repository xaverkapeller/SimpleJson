package com.github.wrdlbrnft.simplejson.builder.retrofit;

import com.github.wrdlbrnft.codebuilder.executables.Method;
import com.github.wrdlbrnft.codebuilder.executables.Methods;

/**
 * Created by kapeller on 21/06/16.
 */

abstract class BaseResponseConverterBuilder implements ConverterBuilder {

    static final Method METHOD_RESPONSE_BODY_STRING = Methods.stub("string");
}
