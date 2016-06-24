package com.github.wrdlbrnft.simplejson.builder.factories.entity;

import com.github.wrdlbrnft.codebuilder.code.Block;
import com.github.wrdlbrnft.codebuilder.code.SourceFile;
import com.github.wrdlbrnft.codebuilder.executables.ExecutableBuilder;
import com.github.wrdlbrnft.codebuilder.executables.Method;
import com.github.wrdlbrnft.codebuilder.implementations.Implementation;
import com.github.wrdlbrnft.codebuilder.types.Type;
import com.github.wrdlbrnft.codebuilder.types.Types;
import com.github.wrdlbrnft.codebuilder.util.Utils;
import com.github.wrdlbrnft.codebuilder.variables.Field;
import com.github.wrdlbrnft.codebuilder.variables.Variable;
import com.github.wrdlbrnft.codebuilder.variables.Variables;
import com.github.wrdlbrnft.simplejson.SimpleJsonAnnotations;
import com.github.wrdlbrnft.simplejson.SimpleJsonTypes;
import com.github.wrdlbrnft.simplejson.builder.parser.InternalParserBuilder;
import com.github.wrdlbrnft.simplejson.models.ImplementationResult;
import com.github.wrdlbrnft.simplejson.models.MappedValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Created by kapeller on 24/04/15.
 */
public class JsonEntityFactoryBuilder {

    private final ProcessingEnvironment mProcessingEnvironment;

    public JsonEntityFactoryBuilder(ProcessingEnvironment processingEnvironment) {
        mProcessingEnvironment = processingEnvironment;
    }

    public void build(Type parserType, ImplementationResult result) throws IOException {
        final TypeElement element = result.getInterfaceType();
        final Type entityType = Types.of(element);

        final String factoryName = createFactoryName(element);

        final Implementation.Builder builder = new Implementation.Builder();
        builder.setName(factoryName);
        builder.setModifiers(EnumSet.of(Modifier.PUBLIC, Modifier.FINAL));

        final TypeElement interfaceType = result.getInterfaceType();
        final Type implType = result.getImplType();
        final List<MappedValue> mappedValues = result.getMappedValues();

        final Field parserField = new Field.Builder()
                .setType(Types.generic(SimpleJsonTypes.PARSER, entityType))
                .setModifiers(EnumSet.of(Modifier.PRIVATE, Modifier.STATIC, Modifier.FINAL))
                .setInitialValue(parserType.newInstance())
                .build();
        builder.addField(parserField);

        builder.addMethod(new Method.Builder()
                .setReturnType(Types.of(interfaceType))
                .setName("create")
                .setModifiers(EnumSet.of(Modifier.PUBLIC, Modifier.STATIC))
                .setCode(new FactoryMethodBuilder(implType, mappedValues))
                .build());

        builder.addMethod(new Method.Builder()
                .setReturnType(entityType)
                .setName("fromJson")
                .setModifiers(EnumSet.of(Modifier.PUBLIC, Modifier.STATIC))
                .addThrownException(SimpleJsonTypes.SIMPLE_JSON_EXCEPTION)
                .setCode(new ExecutableBuilder() {

                    private Variable mParamJson;

                    @Override
                    protected List<Variable> createParameters() {
                        final List<Variable> parameters = new ArrayList<>();
                        parameters.add(mParamJson = Variables.of(Types.STRING));
                        return parameters;
                    }

                    @Override
                    protected void write(Block block) {
                        block.append("return ").append(InternalParserBuilder.METHOD_STUB_FROM_JSON.callOnTarget(parserField, mParamJson)).append(";");
                    }
                })
                .build());

        builder.addMethod(new Method.Builder()
                .setReturnType(Types.generic(Types.LIST, entityType))
                .setName("fromJsonArray")
                .setModifiers(EnumSet.of(Modifier.PUBLIC, Modifier.STATIC))
                .addThrownException(SimpleJsonTypes.SIMPLE_JSON_EXCEPTION)
                .setCode(new ExecutableBuilder() {

                    private Variable mParamJson;

                    @Override
                    protected List<Variable> createParameters() {
                        final List<Variable> parameters = new ArrayList<>();
                        parameters.add(mParamJson = Variables.of(Types.STRING));
                        return parameters;
                    }

                    @Override
                    protected void write(Block block) {
                        block.append("return ").append(InternalParserBuilder.METHOD_STUB_FROM_JSON_ARRAY.callOnTarget(parserField, mParamJson)).append(";");
                    }
                })
                .build());

        builder.addMethod(new Method.Builder()
                .setReturnType(Types.STRING)
                .setName("toJson")
                .setModifiers(EnumSet.of(Modifier.PUBLIC, Modifier.STATIC))
                .addThrownException(SimpleJsonTypes.SIMPLE_JSON_EXCEPTION)
                .setCode(new ExecutableBuilder() {

                    private Variable mParamCollection;

                    @Override
                    protected List<Variable> createParameters() {
                        final List<Variable> parameters = new ArrayList<>();
                        parameters.add(mParamCollection = Variables.of(Types.generic(SimpleJsonTypes.COLLECTION, entityType)));
                        return parameters;
                    }

                    @Override
                    protected void write(Block block) {
                        block.append("return ").append(InternalParserBuilder.METHOD_STUB_LIST_TO_JSON.callOnTarget(parserField, mParamCollection)).append(";");
                    }
                })
                .build());

        builder.addMethod(new Method.Builder()
                .setReturnType(Types.STRING)
                .setName("toJson")
                .setModifiers(EnumSet.of(Modifier.PUBLIC, Modifier.STATIC))
                .addThrownException(SimpleJsonTypes.SIMPLE_JSON_EXCEPTION)
                .setCode(new ExecutableBuilder() {

                    private Variable mParamEntity;

                    @Override
                    protected List<Variable> createParameters() {
                        final List<Variable> parameters = new ArrayList<>();
                        parameters.add(mParamEntity = Variables.of(entityType));
                        return parameters;
                    }

                    @Override
                    protected void write(Block block) {
                        block.append("return ").append(InternalParserBuilder.METHOD_STUB_TO_JSON.callOnTarget(parserField, mParamEntity)).append(";");
                    }
                })
                .build());

        final Implementation factoryImplementation = builder.build();

        final SourceFile sourceFile = SourceFile.create(mProcessingEnvironment, Utils.getPackageName(element));
        sourceFile.write(factoryImplementation);
        sourceFile.flushAndClose();
    }

    private String createFactoryName(TypeElement element) {
        final AnnotationValue annotationFactoryName = Utils.getAnnotationValue(element, SimpleJsonAnnotations.JSON_ENTITY, "factoryName");
        if (annotationFactoryName != null) {
            final Object value = annotationFactoryName.getValue();
            if (value != null) {
                return value.toString();
            }
        }

        final String typeName = Utils.getClassName(element);
        if (Character.toLowerCase(typeName.charAt(typeName.length() - 1)) == 's') {
            return typeName + "Factory";
        }
        return typeName + "s";
    }
}
