package com.github.wrdlbrnft.codebuilder.impl;

import com.github.wrdlbrnft.codebuilder.code.CodeBlock;
import com.github.wrdlbrnft.codebuilder.elements.Executable;
import com.github.wrdlbrnft.codebuilder.elements.Type;
import com.github.wrdlbrnft.codebuilder.elements.Variable;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;

/**
 * Created with Android Studio
 * User: Xaver
 * Date: 21/12/14
 */
class ExecutableImpl implements Executable {

    private final List<Type> mExceptions;
    private final List<Variable> mParameters;
    private final Set<Modifier> mModifiers;
    private final CodeBlock mCode = CodeBlock.Factory.newInstance();

    public ExecutableImpl(Set<Modifier> modifiers, List<Variable> parameters, List<Type> exceptions) {
        mParameters = parameters;
        mModifiers = modifiers;
        mExceptions = exceptions;
    }

    @Override
    public List<Variable> parameters() {
        return mParameters;
    }

    @Override
    public List<Type> exceptions() {
        return mExceptions;
    }

    @Override
    public Set<Modifier> modifiers() {
        return mModifiers;
    }

    @Override
    public CodeBlock code() {
        return mCode;
    }
}
