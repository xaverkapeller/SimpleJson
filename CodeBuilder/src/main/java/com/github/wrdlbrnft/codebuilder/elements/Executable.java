package com.github.wrdlbrnft.codebuilder.elements;

import com.github.wrdlbrnft.codebuilder.code.CodeBlock;

import java.util.List;
import java.util.Set;

import javax.lang.model.element.Modifier;

/**
 * Created with Android Studio
 * User: Xaver
 * Date: 21/12/14
 */
public interface Executable {
    public List<Variable> parameters();
    public List<Type> exceptions();
    public Set<Modifier> modifiers();
    public CodeBlock code();
}