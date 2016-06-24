package com.github.wrdlbrnft.simplejson.builder.enums;

import com.github.wrdlbrnft.codebuilder.code.SourceFile;
import com.github.wrdlbrnft.codebuilder.executables.Method;
import com.github.wrdlbrnft.codebuilder.executables.Methods;
import com.github.wrdlbrnft.codebuilder.implementations.Implementation;
import com.github.wrdlbrnft.codebuilder.types.Type;
import com.github.wrdlbrnft.codebuilder.types.Types;
import com.github.wrdlbrnft.codebuilder.util.Utils;
import com.github.wrdlbrnft.simplejson.SimpleJsonTypes;

import java.io.IOException;
import java.util.EnumSet;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Created by kapeller on 21/04/15.
 */
public class EnumParserBuilder {

    public static final String METHOD_NAME_PARSE = "parse";
    public static final String METHOD_NAME_FORMAT = "format";

    private final EnumAnalyzer mEnumAnalyzer;
    private final ProcessingEnvironment mProcessingEnvironment;

    public EnumParserBuilder(ProcessingEnvironment environment) {
        mProcessingEnvironment = environment;
        mEnumAnalyzer = new EnumAnalyzer(environment);
    }

    public Type build(TypeElement enumElement) throws IOException {
        final EnumAnalyzerResult result = mEnumAnalyzer.analyze(enumElement);

        final Type enumType = Types.of(enumElement);
        final String packageName = Utils.getPackageName(enumElement);
        final String className = Utils.createGeneratedClassName(enumElement, "", "Parser");

        final Implementation.Builder builder = new Implementation.Builder();
        builder.setName(className);
        builder.setModifiers(EnumSet.of(Modifier.PUBLIC, Modifier.FINAL));
        builder.setExtendedType(Types.generic(SimpleJsonTypes.BASE_ENUM_PARSER, enumType));

        final Method parse = new Method.Builder()
                .setReturnType(enumType)
                .setName(METHOD_NAME_PARSE)
                .setModifiers(EnumSet.of(Modifier.PUBLIC, Modifier.FINAL))
                .addThrownException(SimpleJsonTypes.SIMPLE_JSON_EXCEPTION)
                .setCode(new ParseBuilder(enumType, result))
                .build();
        builder.addMethod(parse);

        final Method format = new Method.Builder()
                .setReturnType(Types.STRING)
                .setName(METHOD_NAME_FORMAT)
                .setModifiers(EnumSet.of(Modifier.PUBLIC, Modifier.FINAL))
                .addThrownException(SimpleJsonTypes.SIMPLE_JSON_EXCEPTION)
                .setCode(new FormatBuilder(enumType, result))
                .build();
        builder.addMethod(format);

        final Implementation parserImplementation = builder.build();

        final SourceFile sourceFile = SourceFile.create(mProcessingEnvironment, packageName);
        final Type type = sourceFile.write(parserImplementation);
        sourceFile.flushAndClose();
        return type;
    }
}
