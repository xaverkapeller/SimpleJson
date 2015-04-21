package com.github.wrdlbrnft.simplejsoncompiler.builder.enums;

import com.github.wrdlbrnft.codebuilder.code.CodeBlock;
import com.github.wrdlbrnft.codebuilder.code.ExecutableBuilder;
import com.github.wrdlbrnft.codebuilder.code.Switch;
import com.github.wrdlbrnft.codebuilder.elements.Type;
import com.github.wrdlbrnft.codebuilder.elements.Variable;
import com.github.wrdlbrnft.codebuilder.impl.ClassBuilder;
import com.github.wrdlbrnft.codebuilder.impl.Types;
import com.github.wrdlbrnft.codebuilder.impl.VariableGenerator;
import com.github.wrdlbrnft.codebuilder.utils.Utils;
import com.github.wrdlbrnft.simplejsoncompiler.Annotations;
import com.github.wrdlbrnft.simplejsoncompiler.SimpleJsonTypes;

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
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

/**
 * Created by kapeller on 21/04/15.
 */
public class EnumParserBuilder {

    private enum Mode {
        INTEGER_MAPPING,
        STRING_MAPPING
    }

    private final ProcessingEnvironment mProcessingEnvironment;
    private final TypeElement mEnumElement;

    private final Map<Element, String> mElementValueMap = new HashMap<>();
    private final Map<String, Element> mValueElementMap = new HashMap<>();
    private final Set<Object> mValues = new HashSet<>();


    private Mode mMode = null;

    public EnumParserBuilder(ProcessingEnvironment processingEnvironment, TypeElement enumElement) {
        mProcessingEnvironment = processingEnvironment;
        mEnumElement = enumElement;
    }

    public Type build() throws IOException {
        final Type enumType = Types.create(mEnumElement);

        final String className = mEnumElement.getSimpleName() + "$Parser";
        final ClassBuilder enumParserBuilder = new ClassBuilder(mProcessingEnvironment, className);
        enumParserBuilder.setModifiers(EnumSet.of(Modifier.PUBLIC, Modifier.FINAL));
        enumParserBuilder.setPackageName(Utils.getPackageName(mEnumElement));
        final Set<Type> implementedTypes = new HashSet<>();
        implementedTypes.add(SimpleJsonTypes.ELEMENT_PARSER.genericVersion(enumType));
        enumParserBuilder.setImplements(implementedTypes);

        Element defaultElement = null;
        for (Element element : mEnumElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.ENUM_CONSTANT) {
                boolean hasMapInt = Utils.hasAnnotation(element, Annotations.MAP_INT);
                boolean hasMapString = Utils.hasAnnotation(element, Annotations.MAP_STRING);
                boolean hasMapDefault = Utils.hasAnnotation(element, Annotations.MAP_DEFAULT);

                if (hasMapString && hasMapInt) {
                    mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, element.getSimpleName() + " has both @MapString and @MapInt annotations! Only one can be used on the same element!", element);
                }

                if (!hasMapDefault && !hasMapString && !hasMapInt) {
                    mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, element.getSimpleName() + " is missing an @Map annotation! Add @MapString, @MapInt or @MapDefault to specify how to map the values.", element);
                }

                if (hasMapInt) {
                    checkMapIntegrity(Mode.INTEGER_MAPPING, element);
                    final AnnotationValue annotationValue = Utils.getAnnotationValue(element, Annotations.MAP_INT, "value");
                    final String value = String.valueOf(annotationValue.getValue());

                    if (mValues.contains(value)) {
                        mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "You are using the same value to map multiple fields in the Enum! Each field needs to have a unique mapped value!!1", element);
                    } else {
                        mValues.add(value);
                    }

                    mElementValueMap.put(element, value);
                    mValueElementMap.put(value, element);
                } else if (hasMapString) {
                    checkMapIntegrity(Mode.STRING_MAPPING, element);

                    final AnnotationValue annotationValue = Utils.getAnnotationValue(element, Annotations.MAP_STRING, "value");
                    final String value = "\"" + annotationValue.getValue() + "\"";

                    if (mValues.contains(value)) {
                        mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "You are using the same value to map multiple fields in the Enum! Each field needs to have a unique mapped value!!1", element);
                    } else {
                        mValues.add(value);
                    }

                    mElementValueMap.put(element, value);
                    mValueElementMap.put(value, element);
                }

                if (hasMapDefault) {
                    if (defaultElement == null) {
                        defaultElement = element;
                    } else {
                        mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "You can only annotate one value in the enum with @MapDefault!!1", defaultElement);
                        mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "You can only annotate one value in the enum with @MapDefault!!1", element);
                    }
                }
            }
        }

        final Element finalDefaultElement = defaultElement;
        enumParserBuilder.addMethod(enumType, "parse", EnumSet.of(Modifier.PRIVATE), Arrays.asList(SimpleJsonTypes.JSON_EXCEPTION), new ExecutableBuilder() {

            private Variable paramInput;

            @Override
            public List<Variable> createParameterList(VariableGenerator generator) {
                final List<Variable> parameters = new ArrayList<Variable>();

                switch (mMode) {

                    case INTEGER_MAPPING:
                        parameters.add(paramInput = generator.generate(Types.Primitives.INTEGER));
                        break;

                    case STRING_MAPPING:
                        parameters.add(paramInput = generator.generate(Types.STRING));
                        break;

                    default:
                        throw new IllegalStateException("Illegal Mode value.");
                }

                return parameters;
            }

            @Override
            public void writeBody(CodeBlock code, VariableGenerator generator) {

                final Switch<String> stub = code.newSwitch(paramInput);

                for (String value : mValueElementMap.keySet()) {
                    final Element element = mValueElementMap.get(value);
                    stub.forCase(value)
                            .append("return ").append(enumType.className()).append(".").append(element.getSimpleName()).append(";\n\n");
                }

                if (finalDefaultElement != null) {
                    stub.forDefault().append("return ").append(enumType.className()).append(".").append(finalDefaultElement.getSimpleName()).append(";\n\n");
                } else {
                    stub.forDefault().append("throw ").append(SimpleJsonTypes.JSON_EXCEPTION.newInstance("\"Could not map value\" + " + paramInput + " + \"!!1\"")).append(";\n");
                }
            }
        });

        enumParserBuilder.addMethod(mMode == Mode.STRING_MAPPING ? Types.STRING : Types.Primitives.INTEGER, "format",
                EnumSet.of(Modifier.PRIVATE), Arrays.asList(SimpleJsonTypes.JSON_EXCEPTION), new ExecutableBuilder() {

                    private Variable paramInput;

                    @Override
                    public List<Variable> createParameterList(VariableGenerator generator) {
                        final List<Variable> parameters = new ArrayList<Variable>();

                        parameters.add(paramInput = generator.generate(enumType));

                        return parameters;
                    }

                    @Override
                    public void writeBody(CodeBlock code, VariableGenerator generator) {

                        final Switch<Element> stub = code.newSwitch(paramInput);

                        for (Element element : mElementValueMap.keySet()) {
                            final String value = mElementValueMap.get(element);
                            stub.forCase(element).append("return ").append(value).append(";\n\n");
                        }

                        if (finalDefaultElement != null) {
                            final String value = mElementValueMap.get(finalDefaultElement);
                            stub.forDefault().append("return ").append(value).append(";\n\n");
                        } else {
                            stub.forDefault().append("throw ").append(SimpleJsonTypes.JSON_EXCEPTION.newInstance("\"Could not map value\" + " + paramInput + " + \"!!1\"")).append(";\n");
                        }
                    }
                });

        enumParserBuilder.addMethod(enumType, "fromJsonObject", EnumSet.of(Modifier.PUBLIC), Arrays.asList(SimpleJsonTypes.JSON_EXCEPTION), new ExecutableBuilder() {

            private Variable paramJsonObject;
            private Variable paramKey;

            @Override
            public List<Variable> createParameterList(VariableGenerator generator) {
                final List<Variable> parameters = new ArrayList<Variable>();

                parameters.add(paramJsonObject = generator.generate(SimpleJsonTypes.JSON_OBJECT));
                parameters.add(paramKey = generator.generate(Types.STRING));

                return parameters;
            }

            @Override
            public void writeBody(CodeBlock code, VariableGenerator generator) {
                switch (mMode) {

                    case INTEGER_MAPPING:
                        code.append("return parse(").append(paramJsonObject).append(".getInt(").append(paramKey).append("));\n");
                        break;

                    case STRING_MAPPING:
                        code.append("return parse(").append(paramJsonObject).append(".getString(").append(paramKey).append("));\n");
                        break;

                    default:
                        throw new IllegalStateException("Illegal Mode value.");
                }
            }
        });

        enumParserBuilder.addMethod(Types.Primitives.VOID, "toJsonObject", EnumSet.of(Modifier.PUBLIC), Arrays.asList(SimpleJsonTypes.JSON_EXCEPTION), new ExecutableBuilder() {

            private Variable paramJsonObject;
            private Variable paramKey;
            private Variable paramValue;

            @Override
            public List<Variable> createParameterList(VariableGenerator generator) {
                final List<Variable> parameters = new ArrayList<Variable>();

                parameters.add(paramJsonObject = generator.generate(SimpleJsonTypes.JSON_OBJECT));
                parameters.add(paramKey = generator.generate(Types.STRING));
                parameters.add(paramValue = generator.generate(enumType));

                return parameters;
            }

            @Override
            public void writeBody(CodeBlock code, VariableGenerator generator) {
                code.append(paramJsonObject).append(".put(").append(paramKey).append(", format(").append(paramValue).append("));\n");
            }
        });

        enumParserBuilder.addMethod(enumType, "fromJsonArray", EnumSet.of(Modifier.PUBLIC), Arrays.asList(SimpleJsonTypes.JSON_EXCEPTION), new ExecutableBuilder() {

            private Variable paramJsonArray;
            private Variable paramIndex;

            @Override
            public List<Variable> createParameterList(VariableGenerator generator) {
                final List<Variable> parameters = new ArrayList<Variable>();

                parameters.add(paramJsonArray = generator.generate(SimpleJsonTypes.JSON_ARRAY));
                parameters.add(paramIndex = generator.generate(Types.Primitives.INTEGER));

                return parameters;
            }

            @Override
            public void writeBody(CodeBlock code, VariableGenerator generator) {
                switch (mMode) {

                    case INTEGER_MAPPING:
                        code.append("return parse(").append(paramJsonArray).append(".getInt(").append(paramIndex).append("));\n");
                        break;

                    case STRING_MAPPING:
                        code.append("return parse(").append(paramJsonArray).append(".getString(").append(paramIndex).append("));\n");
                        break;

                    default:
                        throw new IllegalStateException("Illegal Mode value.");
                }
            }
        });

        enumParserBuilder.addMethod(Types.Primitives.VOID, "toJsonArray", EnumSet.of(Modifier.PUBLIC), Arrays.asList(SimpleJsonTypes.JSON_EXCEPTION), new ExecutableBuilder() {

            private Variable paramJsonArray;
            private Variable paramValue;

            @Override
            public List<Variable> createParameterList(VariableGenerator generator) {
                final List<Variable> parameters = new ArrayList<Variable>();

                parameters.add(paramJsonArray = generator.generate(SimpleJsonTypes.JSON_ARRAY));
                parameters.add(paramValue = generator.generate(enumType));

                return parameters;
            }

            @Override
            public void writeBody(CodeBlock code, VariableGenerator generator) {
                code.append(paramJsonArray).append(".put(").append("format(").append(paramValue).append("));\n");
            }
        });

        return enumParserBuilder.build();
    }

    public void checkMapIntegrity(Mode mode, Element currentElement) {
        if (mMode == null) {
            mMode = mode;
        } else if (mMode != mode) {
            mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "You cannot mix @MapString and @MapInt annotations with each other! You have to exclusively use one for the whole enum!", currentElement);
        }
    }
}
