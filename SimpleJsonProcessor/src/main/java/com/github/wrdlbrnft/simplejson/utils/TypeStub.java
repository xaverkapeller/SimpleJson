package com.github.wrdlbrnft.simplejson.utils;

import com.github.wrdlbrnft.codebuilder.code.CodeBuilder;
import com.github.wrdlbrnft.codebuilder.code.CodeElement;
import com.github.wrdlbrnft.codebuilder.code.NameGenerator;
import com.github.wrdlbrnft.codebuilder.code.Resolver;
import com.github.wrdlbrnft.codebuilder.types.Type;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 04/02/2018
 */
public class TypeStub implements Type {

    private Type mType;

    public void setType(Type type) {
        mType = type;
    }

    @Override
    public CodeElement newInstance(CodeElement... codeElements) {
        return mType.newInstance(codeElements);
    }

    @Override
    public CodeElement classObject() {
        return mType.classObject();
    }

    @Override
    public void prepare() {
        mType.prepare();
    }

    @Override
    public void resolve(Resolver resolver, NameGenerator nameGenerator) {
        mType.resolve(resolver, nameGenerator);
    }

    @Override
    public void write(CodeBuilder codeBuilder) {
        mType.write(codeBuilder);
    }
}
