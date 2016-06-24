package com.github.wrdlbrnft.simplejson.builder.parser;

import com.github.wrdlbrnft.codebuilder.annotations.Annotations;
import com.github.wrdlbrnft.codebuilder.code.SourceFile;
import com.github.wrdlbrnft.codebuilder.executables.Method;
import com.github.wrdlbrnft.codebuilder.executables.Methods;
import com.github.wrdlbrnft.codebuilder.implementations.Implementation;
import com.github.wrdlbrnft.codebuilder.types.Type;
import com.github.wrdlbrnft.codebuilder.types.Types;
import com.github.wrdlbrnft.codebuilder.util.Utils;
import com.github.wrdlbrnft.codebuilder.variables.Field;
import com.github.wrdlbrnft.simplejson.SimpleJsonTypes;
import com.github.wrdlbrnft.simplejson.builder.ParserBuilder;
import com.github.wrdlbrnft.simplejson.models.ImplementationResult;
import com.github.wrdlbrnft.simplejson.models.MappedValue;

import java.io.IOException;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;

/**
 * Created by kapeller on 21/04/15.
 */
public class InternalParserBuilder {

    private static final String METHOD_NAME_CONVERT_FROM_JSON = "convertFromJson";
    private static final String METHOD_NAME_CONVERT_TO_JSON = "convertToJson";
    private static final String METHOD_NAME_FROM_JSON = "fromJson";
    private static final String METHOD_NAME_FROM_JSON_ARRAY = "fromJsonArray";
    private static final String METHOD_NAME_TO_JSON = "toJson";
    private static final String METHOD_NAME_LIST_TO_JSON = "toJson";

    public static final Method METHOD_STUB_FROM_JSON = Methods.stub(METHOD_NAME_FROM_JSON);
    public static final Method METHOD_STUB_FROM_JSON_ARRAY = Methods.stub(METHOD_NAME_FROM_JSON_ARRAY);
    public static final Method METHOD_STUB_TO_JSON = Methods.stub(METHOD_NAME_TO_JSON);
    public static final Method METHOD_STUB_LIST_TO_JSON = Methods.stub(METHOD_NAME_LIST_TO_JSON);


    private final ProcessingEnvironment mProcessingEnvironment;
    private final ParserBuilder.BuildCache mBuildCache;

    public InternalParserBuilder(ProcessingEnvironment processingEnvironment, ParserBuilder.BuildCache buildCache) {
        mProcessingEnvironment = processingEnvironment;
        mBuildCache = buildCache;
    }

    public Type build(TypeElement interfaceElement, ImplementationResult implementationResult) throws IOException {
        final List<MappedValue> mappedValues = implementationResult.getMappedValues();
        final Type implType = implementationResult.getImplType();

        final Type interfaceType = Types.of(interfaceElement);
        final Type parserType = Types.generic(SimpleJsonTypes.BASE_PARSER, interfaceType);
        final String parserClassName = Utils.createGeneratedClassName(interfaceElement, "", "Parser");

        final Implementation.Builder builder = new Implementation.Builder();
        builder.setName(parserClassName);
        builder.setModifiers(EnumSet.of(Modifier.PUBLIC, Modifier.FINAL));
        builder.setExtendedType(parserType);

        final ElementParserResolver parserResolver = new ElementParserResolver(mProcessingEnvironment, interfaceElement, mBuildCache);
        final EntityParser entityParser = new EntityParser(parserResolver);
        final EntityFormater entityFormater = new EntityFormater(parserResolver);
        prepareFieldsForLazyEvaluation(mappedValues, parserResolver);

        final Method fromJsonObject = new Method.Builder()
                .setReturnType(interfaceType)
                .setName(METHOD_NAME_CONVERT_FROM_JSON)
                .setModifiers(EnumSet.of(Modifier.PUBLIC))
                .addAnnotation(Annotations.forType(Override.class))
                .addThrownException(SimpleJsonTypes.JSON_EXCEPTION)
                .setCode(new FromJsonObjectBuilder(implType, mappedValues, entityParser))
                .build();
        builder.addMethod(fromJsonObject);

        final Method toJsonObject = new Method.Builder()
                .setReturnType(SimpleJsonTypes.JSON_OBJECT)
                .setName(METHOD_NAME_CONVERT_TO_JSON)
                .setModifiers(EnumSet.of(Modifier.PUBLIC))
                .addAnnotation(Annotations.forType(Override.class))
                .addThrownException(SimpleJsonTypes.JSON_EXCEPTION)
                .setCode(new ToJsonObjectBuilder(interfaceElement, mappedValues, entityFormater))
                .build();
        builder.addMethod(toJsonObject);

        final List<Field> fields = parserResolver.getFields();
        for (Field field : fields) {
            builder.addField(field);
        }

        final Implementation parserImplementation = builder.build();
        final SourceFile sourceFile = SourceFile.create(mProcessingEnvironment, Utils.getPackageName(interfaceElement));
        final Type type = sourceFile.write(parserImplementation);
        sourceFile.flushAndClose();


        return type;
    }

    private void prepareFieldsForLazyEvaluation(List<MappedValue> mappedValues, ElementParserResolver parserResolver) throws IOException {
        for (int i = 0, count = mappedValues.size(); i < count; i++) {
            final MappedValue mappedValue = mappedValues.get(i);
            final TypeMirror itemType = mappedValue.getItemType();
            parserResolver.getElementParserField(itemType);
        }
    }
}
