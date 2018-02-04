package com.github.wrdlbrnft.simplejson.utils;

import com.github.wrdlbrnft.codebuilder.code.Block;
import com.github.wrdlbrnft.codebuilder.code.BlockWriter;
import com.github.wrdlbrnft.codebuilder.code.CodeBuilder;
import com.github.wrdlbrnft.codebuilder.code.CodeElement;
import com.github.wrdlbrnft.codebuilder.code.NameGenerator;
import com.github.wrdlbrnft.codebuilder.code.Resolver;
import com.github.wrdlbrnft.codebuilder.elements.names.NameElement;
import com.github.wrdlbrnft.codebuilder.implementations.Implementation;

import java.awt.datatransfer.MimeTypeParseException;

/**
 * Created with Android Studio<br>
 * User: Xaver<br>
 * Date: 04/02/2018
 */

public class ImplementationStub implements Implementation {

    private Implementation mImplementation;

    public void setImplementation(Implementation implementation) {
        mImplementation = implementation;
    }

    @Override
    public NameElement getName() {
        return new NameElement() {
            @Override
            public String getResolvedName() {
                return mImplementation.getName().getResolvedName();
            }

            @Override
            public void prepare() {
                mImplementation.getName().prepare();
            }

            @Override
            public void resolve(Resolver resolver, NameGenerator nameGenerator) {
                mImplementation.getName().resolve(resolver, nameGenerator);
            }

            @Override
            public void write(CodeBuilder codeBuilder) {
                mImplementation.getName().write(codeBuilder);
            }
        };
    }

    @Override
    public CodeElement getDeclaration() {
        return new BlockWriter() {
            @Override
            protected void write(Block block) {
                block.append(mImplementation.getDeclaration());
            }
        };
    }

    @Override
    public CodeElement newInstance(CodeElement... codeElements) {
        return new BlockWriter() {
            @Override
            protected void write(Block block) {
                block.append(mImplementation.newInstance(codeElements));
            }
        };
    }

    @Override
    public CodeElement classObject() {
        return new BlockWriter() {
            @Override
            protected void write(Block block) {
                block.append(mImplementation.classObject());
            }
        };
    }

    @Override
    public void prepare() {
        mImplementation.prepare();
    }

    @Override
    public void resolve(Resolver resolver, NameGenerator nameGenerator) {
        mImplementation.resolve(resolver, nameGenerator);
    }

    @Override
    public void write(CodeBuilder codeBuilder) {
        mImplementation.write(codeBuilder);
    }
}