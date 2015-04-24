package com.github.wrdlbrnft.simplejsoncompiler.builder.parser;

import com.github.wrdlbrnft.codebuilder.code.CodeBlock;
import com.github.wrdlbrnft.codebuilder.code.ExecutableBuilder;
import com.github.wrdlbrnft.codebuilder.elements.Field;
import com.github.wrdlbrnft.codebuilder.elements.Method;
import com.github.wrdlbrnft.codebuilder.elements.Type;
import com.github.wrdlbrnft.codebuilder.elements.Variable;
import com.github.wrdlbrnft.codebuilder.impl.ClassBuilder;
import com.github.wrdlbrnft.codebuilder.impl.Types;
import com.github.wrdlbrnft.codebuilder.impl.VariableGenerator;
import com.github.wrdlbrnft.codebuilder.utils.Utils;
import com.github.wrdlbrnft.simplejsoncompiler.SimpleJsonTypes;
import com.github.wrdlbrnft.simplejsoncompiler.models.ImplementationResult;
import com.github.wrdlbrnft.simplejsoncompiler.models.MappedValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;

/**
 * Created by kapeller on 21/04/15.
 */
public class ParserBuilder {

    private final ProcessingEnvironment mProcessingEnvironment;

    private final Map<String, Type> mEnumParserMap;

    public ParserBuilder(ProcessingEnvironment processingEnvironment, Map<String, Type> enumParserMap) {
        mProcessingEnvironment = processingEnvironment;
        mEnumParserMap = enumParserMap;
    }

    public void build(TypeElement interfaceElement, ImplementationResult implementationResult) throws IOException {
        final List<MappedValue> mappedValues = implementationResult.getMappedValues();
        final Type implType = implementationResult.getImplType();

        final Type entityType = Types.create(interfaceElement);
        final Type parserType = SimpleJsonTypes.PARSER.genericVersion(entityType);
        final String parserClassName = entityType.className() + "$Parser";

        ClassBuilder builder = new ClassBuilder(mProcessingEnvironment, parserClassName);
        builder.setPackageName(Utils.getPackageName(interfaceElement));
        builder.setModifiers(EnumSet.of(Modifier.PUBLIC, Modifier.FINAL));
        builder.addImport(implType);
        builder.addImport(SimpleJsonTypes.JSON_OBJECT);
        builder.addImport(SimpleJsonTypes.JSON_ARRAY);
        builder.addImport(Types.ARRAY_LIST);
        builder.addImport(Types.LIST);
        builder.addImport(Types.SET);
        builder.addImport(Types.HASH_SET);

        final Set<Type> implementedTypes = new HashSet<>();
        implementedTypes.add(parserType);
        builder.setImplements(implementedTypes);

        final ElementParserResolver parserResolver = new ElementParserResolver(mProcessingEnvironment, interfaceElement, builder, mEnumParserMap);

        final Method fromJsonObject = builder.addMethod(entityType, "fromJsonObject", EnumSet.of(Modifier.PUBLIC), Arrays.asList(SimpleJsonTypes.JSON_EXCEPTION), new ExecutableBuilder() {

            private Variable paramJsonObject;

            @Override
            public List<Variable> createParameterList(VariableGenerator generator) {
                final List<Variable> parameters = new ArrayList<>();

                parameters.add(paramJsonObject = generator.generate(SimpleJsonTypes.JSON_OBJECT));

                return parameters;
            }

            @Override
            public void writeBody(CodeBlock code, VariableGenerator generator) {
                final Variable[] params = new Variable[mappedValues.size()];
                for (int i = 0, count = mappedValues.size(); i < count; i++) {
                    final MappedValue mappedValue = mappedValues.get(i);

                    switch (mappedValue.getValueType()) {

                        case VALUE:
                            params[i] = parseValue(code, parserResolver, mappedValue, paramJsonObject, generator);
                            break;

                        case LIST:
                            params[i] = parseList(code, parserResolver, mappedValue, paramJsonObject, generator);
                            break;

                        case SET:
                            params[i] = parseSet(code, parserResolver, mappedValue, paramJsonObject, generator);
                            break;

                        default:
                            throw new IllegalStateException("Encountered unknown ValueType! Compiler is broken...");
                    }
                }

                code.append("return ").append(implType.newInstance(params)).append(";\n");
            }
        });

        final Method toJsonObject = builder.addMethod(SimpleJsonTypes.JSON_OBJECT, "toJsonObject", EnumSet.of(Modifier.PUBLIC), Arrays.asList(SimpleJsonTypes.JSON_EXCEPTION), new ExecutableBuilder() {

            private Variable paramEntity;

            @Override
            public List<Variable> createParameterList(VariableGenerator generator) {
                final List<Variable> parameters = new ArrayList<Variable>();

                parameters.add(paramEntity = generator.generate(entityType));

                return parameters;
            }

            @Override
            public void writeBody(CodeBlock code, VariableGenerator generator) {
                final Variable varJsonObject = generator.generate(SimpleJsonTypes.JSON_OBJECT, Modifier.FINAL);
                code.append(varJsonObject.initialize(SimpleJsonTypes.JSON_OBJECT.newInstance(new String[0]))).append(";\n");

                for (int i = 0, count = mappedValues.size(); i < count; i++) {
                    final MappedValue mappedValue = mappedValues.get(i);

                    switch (mappedValue.getValueType()) {

                        case VALUE:
                            formatValue(code, parserResolver, mappedValue, varJsonObject, paramEntity, generator);
                            break;

                        case LIST:
                            formatList(code, parserResolver, mappedValue, varJsonObject, paramEntity, generator);
                            break;

                        case SET:
                            formatSet(code, parserResolver, mappedValue, varJsonObject, paramEntity, generator);
                            break;

                        default:
                            throw new IllegalStateException("Encountered unknown ValueType! Compiler is broken...");
                    }
                }

                code.append("return ").append(varJsonObject).append(";\n");
            }
        });

        builder.addMethod(entityType, "fromJson", EnumSet.of(Modifier.PUBLIC), Arrays.asList(SimpleJsonTypes.JSON_EXCEPTION), new ExecutableBuilder() {

            private Variable paramJson;

            @Override
            public List<Variable> createParameterList(VariableGenerator generator) {
                final List<Variable> parameters = new ArrayList<Variable>();

                parameters.add(paramJson = generator.generate(Types.STRING));

                return parameters;
            }

            @Override
            public void writeBody(CodeBlock code, VariableGenerator generator) {
                final Variable varJsonObject = generator.generate(SimpleJsonTypes.JSON_OBJECT, Modifier.FINAL);
                code.append(varJsonObject.initialize(SimpleJsonTypes.JSON_OBJECT.newInstance(paramJson))).append(";\n");
                code.append("return ").append(fromJsonObject.execute(null, varJsonObject)).append(";\n");
            }
        });

        builder.addMethod(Types.LIST.genericVersion(entityType), "fromJsonArray", EnumSet.of(Modifier.PUBLIC), Arrays.asList(SimpleJsonTypes.JSON_EXCEPTION), new ExecutableBuilder() {

            private Variable paramJson;

            @Override
            public List<Variable> createParameterList(VariableGenerator generator) {
                final List<Variable> parameters = new ArrayList<Variable>();

                parameters.add(paramJson = generator.generate(Types.STRING));

                return parameters;
            }

            @Override
            public void writeBody(CodeBlock code, VariableGenerator generator) {
                final Variable varList = generator.generate(Types.LIST.genericVersion(entityType), Modifier.FINAL);
                code.append(varList.initialize(Types.ARRAY_LIST.genericVersion(entityType).newInstance(new String[0]))).append(";\n");
                final Variable varJsonArray = generator.generate(SimpleJsonTypes.JSON_ARRAY, Modifier.FINAL);
                final Variable varJsonObject = generator.generate(SimpleJsonTypes.JSON_OBJECT, Modifier.FINAL);
                final Variable varIndex = generator.generate(Types.Primitives.INTEGER);
                final Variable varCounter = generator.generate(Types.Primitives.INTEGER);
                code.append(varJsonArray.initialize(SimpleJsonTypes.JSON_ARRAY.newInstance(paramJson))).append(";\n");
                code.append("for(").append(varIndex.initialize("0")).append(", ").append(varCounter).append(" = ").append(varJsonArray).append(".length()")
                        .append("; ").append(varIndex).append(" < ").append(varCounter).append("; ")
                        .append(varIndex).append("++) {\n");
                code.append(varJsonObject.initialize(varJsonArray + ".getJSONObject(" + varIndex + ");\n"));
                code.append(varList).append(".add(").append(fromJsonObject.execute(null, varJsonObject)).append(");\n");
                code.append("}\n");
                code.append("return ").append(varList).append(";\n");
            }
        });

        builder.addMethod(Types.STRING, "toJson", EnumSet.of(Modifier.PUBLIC), Arrays.asList(SimpleJsonTypes.JSON_EXCEPTION), new ExecutableBuilder() {

            private Variable paramEntity;

            @Override
            public List<Variable> createParameterList(VariableGenerator generator) {
                final List<Variable> parameters = new ArrayList<Variable>();

                parameters.add(paramEntity = generator.generate(entityType));

                return parameters;
            }

            @Override
            public void writeBody(CodeBlock code, VariableGenerator generator) {
                final Variable varJsonObject = generator.generate(SimpleJsonTypes.JSON_OBJECT, Modifier.FINAL);
                code.append(varJsonObject.initialize(toJsonObject.execute(null, paramEntity))).append(";\n");
                code.append("return ").append(varJsonObject).append(".toString();\n");
            }
        });

        builder.addMethod(Types.STRING, "toJson", EnumSet.of(Modifier.PUBLIC), Arrays.asList(SimpleJsonTypes.JSON_EXCEPTION), new ExecutableBuilder() {

            private Variable paramList;

            @Override
            public List<Variable> createParameterList(VariableGenerator generator) {
                final List<Variable> parameters = new ArrayList<>();

                parameters.add(paramList = generator.generate(Types.LIST.genericVersion(entityType)));

                return parameters;
            }

            @Override
            public void writeBody(CodeBlock code, VariableGenerator generator) {
                final Variable varJsonArray = generator.generate(SimpleJsonTypes.JSON_ARRAY, Modifier.FINAL);
                code.append(varJsonArray.initialize(SimpleJsonTypes.JSON_ARRAY.newInstance(new String[0]))).append(";\n");

                final Variable varEntity = generator.generate(entityType);
                code.append("for(").append(varEntity.initialize()).append(" : ").append(paramList).append(") {\n");

                final Variable varJsonObject = generator.generate(SimpleJsonTypes.JSON_OBJECT, Modifier.FINAL);
                code.append(varJsonObject.initialize(toJsonObject.execute(null, varEntity))).append(";\n");
                code.append(varJsonArray).append(".put(").append(varJsonObject).append(");\n");

                code.append("}\n");

                code.append("return ").append(varJsonArray).append(".toString();\n");
            }
        });

        builder.build();
    }

    private void formatValue(CodeBlock code, ElementParserResolver parserResolver, MappedValue mappedValue, Variable varJsonObject, Variable varEntity, VariableGenerator generator) {
        final String key = mappedValue.getKey();
        final Type type = mappedValue.getType();
        final Field parser = parserResolver.getElementParserField(type);
        code.append(parser).append(".toJsonObject(")
                .append(varJsonObject)
                .append(", \"").append(key).append("\", ")
                .append(varEntity).append(".").append(mappedValue.getMethod().getSimpleName()).append("()")
                .append(");\n");
    }

    private void formatList(CodeBlock code, ElementParserResolver parserResolver, MappedValue mappedValue, Variable varJsonObject, Variable varEntity, VariableGenerator generator) {
        formatCollection(code, parserResolver, mappedValue, varJsonObject, varEntity, generator);
    }

    private void formatSet(CodeBlock code, ElementParserResolver parserResolver, MappedValue mappedValue, Variable varJsonObject, Variable varEntity, VariableGenerator generator) {
        formatCollection(code, parserResolver, mappedValue, varJsonObject, varEntity, generator);
    }

    private void formatCollection(CodeBlock code, ElementParserResolver parserResolver, MappedValue mappedValue, Variable varJsonObject, Variable varEntity, VariableGenerator generator) {
        final String key = mappedValue.getKey();
        final Type type = mappedValue.getType();
        final Field parser = parserResolver.getElementParserField(type);
        final Variable varItem = generator.generate(type);
        final Variable varJsonArray = generator.generate(SimpleJsonTypes.JSON_ARRAY);
        code.append(varJsonArray.initialize(SimpleJsonTypes.JSON_ARRAY.newInstance(new String[0]))).append(";\n");
        code.append("for(").append(varItem.initialize()).append(" : ").append(varEntity).append(".").append(mappedValue.getMethod().getSimpleName()).append("()) {\n");
        code.append(parser).append(".toJsonArray(").append(varJsonArray).append(", ").append(varItem).append(");\n");
        code.append("}\n");
        code.append(varJsonObject).append(".put(\"").append(key).append("\", ").append(varJsonArray).append(");\n");
    }

    private Variable parseList(CodeBlock code, ElementParserResolver parserResolver, MappedValue mappedValue, Variable varJsonObject, VariableGenerator generator) {
        final Type itemType = mappedValue.getType();
        final Field parser = parserResolver.getElementParserField(itemType);
        final Type setType = Types.LIST.genericVersion(itemType);
        final Variable varSet = generator.generate(setType, Modifier.FINAL);
        code.append(varSet.initialize(Types.ARRAY_LIST.genericVersion(itemType).newInstance(new String[0]))).append(";\n");

        if (mappedValue.isOptional()) {
            code = code.newIf(varJsonObject + ".has(\"" + mappedValue.getKey() + "\")").whenTrue();
        }

        parseCollection(code, varSet, varJsonObject, mappedValue.getKey(), parser, generator);

        return varSet;
    }


    private Variable parseSet(CodeBlock code, ElementParserResolver parserResolver, MappedValue mappedValue, Variable varJsonObject, VariableGenerator generator) {
        final Type itemType = mappedValue.getType();
        final Field parser = parserResolver.getElementParserField(itemType);
        final Type setType = Types.SET.genericVersion(itemType);
        final Variable varSet = generator.generate(setType, Modifier.FINAL);
        code.append(varSet.initialize(Types.HASH_SET.genericVersion(itemType).newInstance(new String[0]))).append(";\n");

        if (mappedValue.isOptional()) {
            code = code.newIf(varJsonObject + ".has(\"" + mappedValue.getKey() + "\")").whenTrue();
        }

        parseCollection(code, varSet, varJsonObject, mappedValue.getKey(), parser, generator);

        return varSet;
    }

    private void parseCollection(CodeBlock code, Variable varCollection, Variable varJsonObject, String key, Field parser, VariableGenerator generator) {
        final Variable varJsonArray = generator.generate(SimpleJsonTypes.JSON_ARRAY, Modifier.FINAL);
        final Variable varIndex = generator.generate(Types.Primitives.INTEGER);
        final Variable varCounter = generator.generate(Types.Primitives.INTEGER);

        code.append(varJsonArray.initialize(varJsonObject + ".getJSONArray(\"" + key + "\")")).append(";\n");
        code.append("for(").append(varIndex.initialize("0")).append(", ").append(varCounter).append(" = ").append(varJsonArray).append(".length()")
                .append("; ").append(varIndex).append(" < ").append(varCounter).append("; ")
                .append(varIndex).append("++) {\n");
        code.append(varCollection).append(".add(").append(parser).append(".fromJsonArray(").append(varJsonArray).append(", ").append(varIndex).append(")").append(");\n");
        code.append("}\n");
    }

    private Variable parseValue(CodeBlock code, ElementParserResolver parserResolver, MappedValue mappedValue, Variable varJsonObject, VariableGenerator generator) {
        final String key = "\"" + mappedValue.getKey() + "\"";
        final Type type = mappedValue.getType();
        final Field parser = parserResolver.getElementParserField(type);
        final Variable variable = generator.generate(type, Modifier.FINAL);

        if (mappedValue.isOptional()) {
            code.append(variable.initialize(varJsonObject + ".has(" + key + ") ? " + parser + ".fromJsonObject(" + varJsonObject + ", " + key + ") : null")).append(";\n");
        } else {
            code.append(variable.initialize(parser + ".fromJsonObject(" + varJsonObject + ", " + key + ")")).append(";\n");
        }

        return variable;
    }
}
