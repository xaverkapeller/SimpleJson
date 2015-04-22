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
import com.github.wrdlbrnft.simplejsoncompiler.Annotations;
import com.github.wrdlbrnft.simplejsoncompiler.SimpleJsonTypes;
import com.github.wrdlbrnft.simplejsoncompiler.models.ImplementationResult;
import com.github.wrdlbrnft.simplejsoncompiler.models.MappedValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * Created by kapeller on 21/04/15.
 */
public class ParserBuilder {

    private final ProcessingEnvironment mProcessingEnvironment;
    private final TypeElement mElement;
    private final ImplementationResult mImplementationResult;

    private final Map<String, Type> mEnumParserMap;
    private final Map<String, Field> mParserMap = new HashMap<>();

    private ClassBuilder mBuilder;

    public ParserBuilder(ProcessingEnvironment processingEnvironment, TypeElement element, ImplementationResult implementationResult, Map<String, Type> enumParserMap) {
        mProcessingEnvironment = processingEnvironment;
        mElement = element;
        mImplementationResult = implementationResult;
        mEnumParserMap = enumParserMap;
    }

    public void build() throws IOException {
        final List<MappedValue> mappedValues = mImplementationResult.getMappedValues();
        final Type implType = mImplementationResult.getImplType();

        final Type entityType = Types.create(mElement);
        final Type parserType = SimpleJsonTypes.PARSER.genericVersion(entityType);
        final String parserClassName = entityType.className() + "$Parser";

        mBuilder = new ClassBuilder(mProcessingEnvironment, parserClassName);
        mBuilder.setPackageName(Utils.getPackageName(mElement));
        mBuilder.setModifiers(EnumSet.of(Modifier.PUBLIC, Modifier.FINAL));
        final Set<Type> implementedTypes = new HashSet<>();
        implementedTypes.add(parserType);
        mBuilder.setImplements(implementedTypes);
        mBuilder.addImport(implType);
        mBuilder.addImport(SimpleJsonTypes.JSON_OBJECT);
        mBuilder.addImport(SimpleJsonTypes.JSON_ARRAY);
        mBuilder.addImport(Types.ARRAY_LIST);

        final Method fromJsonObject = mBuilder.addMethod(entityType, "fromJsonObject", EnumSet.of(Modifier.PRIVATE), Arrays.asList(SimpleJsonTypes.JSON_EXCEPTION), new ExecutableBuilder() {

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
                            params[i] = parseValue(code, mappedValue, paramJsonObject, generator);
                            break;

                        case LIST:
                            params[i] = parseList(code, mappedValue, paramJsonObject, generator);
                            break;

                        case SET:
                            params[i] = parseSet(code, mappedValue, paramJsonObject, generator);
                            break;

                        default:
                            throw new IllegalStateException("Encountered unknown ValueType! Compiler is broken...");
                    }
                }

                code.append("return ").append(implType.newInstance(params)).append(";\n");
            }
        });

        final Method toJsonObject = mBuilder.addMethod(SimpleJsonTypes.JSON_OBJECT, "toJsonObject", EnumSet.of(Modifier.PRIVATE), Arrays.asList(SimpleJsonTypes.JSON_EXCEPTION), new ExecutableBuilder() {

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
                            formatValue(code, mappedValue, varJsonObject, paramEntity, generator);
                            break;

                        case LIST:
                            formatList(code, mappedValue, varJsonObject, paramEntity, generator);
                            break;

                        case SET:
                            formatSet(code, mappedValue, varJsonObject, paramEntity, generator);
                            break;

                        default:
                            throw new IllegalStateException("Encountered unknown ValueType! Compiler is broken...");
                    }
                }

                code.append("return ").append(varJsonObject).append(";\n");
            }
        });

        mBuilder.addMethod(entityType, "fromJson", EnumSet.of(Modifier.PUBLIC), Arrays.asList(SimpleJsonTypes.JSON_EXCEPTION), new ExecutableBuilder() {

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

        mBuilder.addMethod(Types.LIST.genericVersion(entityType), "fromJsonArray", EnumSet.of(Modifier.PUBLIC), Arrays.asList(SimpleJsonTypes.JSON_EXCEPTION), new ExecutableBuilder() {

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

        mBuilder.addMethod(Types.STRING, "toJson", EnumSet.of(Modifier.PUBLIC), Arrays.asList(SimpleJsonTypes.JSON_EXCEPTION), new ExecutableBuilder() {

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

        mBuilder.addMethod(Types.STRING, "toJson", EnumSet.of(Modifier.PUBLIC), Arrays.asList(SimpleJsonTypes.JSON_EXCEPTION), new ExecutableBuilder() {

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

        mBuilder.build();
    }

    private void formatValue(CodeBlock code, MappedValue mappedValue, Variable varJsonObject, Variable varEntity, VariableGenerator generator) {
        final String key = mappedValue.getKey();
        final Type type = mappedValue.getType();
        final Field parser = getElementParser(type);
        code.append(parser).append(".toJsonObject(")
                .append(varJsonObject)
                .append(", \"").append(key).append("\", ")
                .append(varEntity).append(".").append(mappedValue.getMethod().getSimpleName()).append("()")
                .append(");\n");
    }

    private void formatList(CodeBlock code, MappedValue mappedValue, Variable varJsonObject, Variable varEntity, VariableGenerator generator) {
        formatCollection(code, mappedValue, varJsonObject, varEntity, generator);
    }

    private void formatSet(CodeBlock code, MappedValue mappedValue, Variable varJsonObject, Variable varEntity, VariableGenerator generator) {
        formatCollection(code, mappedValue, varJsonObject, varEntity, generator);
    }

    private void formatCollection(CodeBlock code, MappedValue mappedValue, Variable varJsonObject, Variable varEntity, VariableGenerator generator) {
        final String key = mappedValue.getKey();
        final Type type = mappedValue.getType();
        final Field parser = getElementParser(type);
        final Variable varItem = generator.generate(type);
        final Variable varJsonArray = generator.generate(SimpleJsonTypes.JSON_ARRAY);
        code.append(varJsonArray.initialize(SimpleJsonTypes.JSON_ARRAY.newInstance(new String[0]))).append(";\n");
        code.append("for(").append(varItem.initialize()).append(" : ").append(varEntity).append(".").append(mappedValue.getMethod().getSimpleName()).append("()) {\n");
        code.append(parser).append(".toJsonArray(").append(varJsonArray).append(", ").append(varItem).append(");\n");
        code.append("}\n");
        code.append(varJsonObject).append(".put(\"").append(key).append("\", ").append(varJsonArray).append(");\n");
    }

    private Variable parseList(CodeBlock code, MappedValue mappedValue, Variable varJsonObject, VariableGenerator generator) {
        final Type itemType = mappedValue.getType();
        final Field parser = getElementParser(itemType);
        final Type setType = Types.LIST.genericVersion(itemType);
        final Variable varSet = generator.generate(setType, Modifier.FINAL);
        mBuilder.addImport(Types.LIST);
        mBuilder.addImport(Types.ARRAY_LIST);
        mBuilder.addImport(SimpleJsonTypes.JSON_ARRAY);
        code.append(varSet.initialize(Types.ARRAY_LIST.genericVersion(itemType).newInstance(new String[0]))).append(";\n");

        if (mappedValue.isOptional()) {
            code = code.newIf(varJsonObject + ".has(\"" + mappedValue.getKey() + "\")").whenTrue();
        }

        parseCollection(code, varSet, varJsonObject, mappedValue.getKey(), parser, generator);

        return varSet;
    }


    private Variable parseSet(CodeBlock code, MappedValue mappedValue, Variable varJsonObject, VariableGenerator generator) {
        final Type itemType = mappedValue.getType();
        final Field parser = getElementParser(itemType);
        final Type setType = Types.SET.genericVersion(itemType);
        final Variable varSet = generator.generate(setType, Modifier.FINAL);
        mBuilder.addImport(Types.SET);
        mBuilder.addImport(Types.HASH_SET);
        mBuilder.addImport(SimpleJsonTypes.JSON_ARRAY);
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

    private Variable parseValue(CodeBlock code, MappedValue mappedValue, Variable varJsonObject, VariableGenerator generator) {
        final String key = "\"" + mappedValue.getKey() + "\"";
        final Type type = mappedValue.getType();
        final Field parser = getElementParser(type);
        final Variable variable = generator.generate(type, Modifier.FINAL);

        if (mappedValue.isOptional()) {
            code.append(variable.initialize(varJsonObject + ".has(" + key + ") ? " + parser + ".fromJsonObject(" + varJsonObject + ", " + key + ") : null")).append(";\n");
        } else {
            code.append(variable.initialize(parser + ".fromJsonObject(" + varJsonObject + ", " + key + ")")).append(";\n");
        }

        return variable;
    }

    private Field getElementParser(Type type) {
        final String key = type.fullClassName();
        if (mParserMap.containsKey(key)) {
            return mParserMap.get(key);
        }

        mBuilder.addImport(type);

        final Field field;
        if (type.isSubTypeOf(SimpleJsonTypes.ENUM)) {
            if (mEnumParserMap.containsKey(key)) {
                final Type parserType = mEnumParserMap.get(key);
                field = createElementParserField(parserType);
            } else {
                mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "There is no parser implementation for " + type + "! Have you forgot to annotate it with @JsonEnum?", type.getTypeElement());
                throw new IllegalStateException("There is no parser implementation for " + type + "! Have you forgot to annotate it with @JsonEnum?");
            }
        } else if (type.equals(Types.STRING)) {
            field = createElementParserField(SimpleJsonTypes.STRING_PARSER);
        } else if (type.equals(Types.Primitives.INTEGER)) {
            field = createElementParserField(SimpleJsonTypes.INTEGER_PARSER);
        } else if (type.equals(Types.Primitives.DOUBLE)) {
            field = createElementParserField(SimpleJsonTypes.DOUBLE_PARSER);
        } else if (type.equals(Types.Primitives.LONG)) {
            field = createElementParserField(SimpleJsonTypes.LONG_PARSER);
        } else if (type.equals(Types.Primitives.BOOLEAN)) {
            field = createElementParserField(SimpleJsonTypes.BOOLEAN_PARSER);
        } else if (type.equals(Types.Boxed.INTEGER)) {
            field = createElementParserField(SimpleJsonTypes.INTEGER_PARSER);
        } else if (type.equals(Types.Boxed.DOUBLE)) {
            field = createElementParserField(SimpleJsonTypes.DOUBLE_PARSER);
        } else if (type.equals(Types.Boxed.LONG)) {
            field = createElementParserField(SimpleJsonTypes.LONG_PARSER);
        } else if (type.equals(Types.Boxed.BOOLEAN)) {
            field = createElementParserField(SimpleJsonTypes.BOOLEAN_PARSER);
        } else {
            final TypeElement element = type.getTypeElement();
            if (element == null) {
                mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "Could not find a parser for " + type.className() + "!!1", mElement);
                throw new IllegalStateException("Could not find a parser for " + type.className() + "!!1");
            } else {
                if (Utils.hasAnnotation(element, Annotations.JSON_ENTITY)) {
                    final Type parserType = SimpleJsonTypes.ENTITY_PARSER.genericVersion(type);
                    field = mBuilder.addField(parserType, EnumSet.of(Modifier.PRIVATE, Modifier.FINAL));
                    field.setInitialValue(parserType.newInstance(type.className() + ".class"));
                } else {
                    mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "Could not find a parser for " + type.className() + "!!1 Have you forgot to annotate it?", mElement);
                    throw new IllegalStateException("Could not find a parser for " + type.className() + "!!1 Have you forgot to annotate it?");
                }
            }
        }

        mParserMap.put(key, field);

        return field;
    }

    private Field createElementParserField(Type type) {
        final Field field = mBuilder.addField(type, EnumSet.of(Modifier.PRIVATE, Modifier.FINAL));
        field.setInitialValue(type.newInstance(new String[0]));
        return field;
    }
}
