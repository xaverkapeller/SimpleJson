package com.github.wrdlbrnft.simplejsoncompiler.builder.implementation;

import com.github.wrdlbrnft.codebuilder.code.CodeBlock;
import com.github.wrdlbrnft.codebuilder.code.ExecutableBuilder;
import com.github.wrdlbrnft.codebuilder.elements.Field;
import com.github.wrdlbrnft.codebuilder.elements.Type;
import com.github.wrdlbrnft.codebuilder.elements.Variable;
import com.github.wrdlbrnft.codebuilder.impl.ClassBuilder;
import com.github.wrdlbrnft.codebuilder.impl.Types;
import com.github.wrdlbrnft.codebuilder.impl.VariableGenerator;
import com.github.wrdlbrnft.codebuilder.utils.Utils;
import com.github.wrdlbrnft.simplejsoncompiler.Annotations;
import com.github.wrdlbrnft.simplejsoncompiler.models.ImplementationResult;
import com.github.wrdlbrnft.simplejsoncompiler.models.MappedValue;

import java.io.IOException;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * Created by kapeller on 21/04/15.
 */
public class ImplementationBuilder {

    protected final ProcessingEnvironment mProcessingEnv;

    public ImplementationBuilder(ProcessingEnvironment processingEnv) {
        mProcessingEnv = processingEnv;
    }

    public ImplementationResult build(TypeElement interfaceElement) throws IOException {

        final Type interfaceType = Types.create(interfaceElement);
        final String implClassName = interfaceType.className() + "$Impl";

        final ClassBuilder implBuilder = new ClassBuilder(mProcessingEnv, implClassName);
        implBuilder.setModifiers(EnumSet.of(Modifier.PUBLIC, Modifier.FINAL));
        final Set<Type> implementedTypes = new HashSet<>();
        implementedTypes.add(interfaceType);
        implBuilder.setImplements(implementedTypes);
        implBuilder.setPackageName(Utils.getPackageName(interfaceElement));

        final List<MappedValue> mappedValues = new ArrayList<>();
        for (Element element : interfaceElement.getEnclosedElements()) {
            if (element.getKind() == ElementKind.METHOD) {
                final ExecutableElement method = (ExecutableElement) element;
                final MappedValue mappedValue = handleMethod(implBuilder, method);
                mappedValues.add(mappedValue);
            }
        }

        implBuilder.addConstructor(EnumSet.of(Modifier.PUBLIC), new ExecutableBuilder() {

            private final Map<Field, Variable> parameterMap = new HashMap<>();

            @Override
            public List<Variable> createParameterList(VariableGenerator generator) {
                final List<Variable> parameters = new ArrayList<Variable>();

                for (int i = 0, count = mappedValues.size(); i < count; i++) {
                    final MappedValue mappedValue = mappedValues.get(i);
                    final Field field = mappedValue.getField();
                    final Variable parameter = generator.generate(field.type());
                    parameterMap.put(field, parameter);
                    parameters.add(parameter);
                }

                return parameters;
            }

            @Override
            public void writeBody(CodeBlock code, VariableGenerator generator) {
                final Set<Field> keySet = parameterMap.keySet();
                for (Field field : keySet) {
                    final Variable parameter = parameterMap.get(field);
                    code.append(field.set(parameter)).append(";\n");
                }
            }
        });

        final Type implType = implBuilder.build();

        return new ImplementationResult(implType, interfaceType, mappedValues);
    }

    private MappedValue handleMethod(ClassBuilder builder, ExecutableElement method) {
        if (method.getParameters().size() > 0) {
            mProcessingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Your interfaces annotated with @JsonEntity can only contain methods which do not have parameters! " + method.getSimpleName() + " has parameters!", method);
        }

        if (!Utils.hasAnnotation(method, Annotations.KEY)) {
            mProcessingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "The method " + method.getSimpleName() + " is missing the @Key annotation!", method);
        }

        final TypeMirror returnTypeMirror = method.getReturnType();
        final Type baseReturnType = Types.create(method.getReturnType());
        final String name = method.getSimpleName().toString();
        final Set<Modifier> modifiers = new HashSet<>(method.getModifiers());
        modifiers.remove(Modifier.ABSTRACT);

        final Type itemType;
        final Type finalReturnType;
        final MappedValue.ValueType valueType;
        if (returnTypeMirror.getKind() == TypeKind.ARRAY) {
            mProcessingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Arrays as return types are not supported!", method);
            itemType = null;
            finalReturnType = null;
            valueType = null;
        } else if (baseReturnType.isSubTypeOf(Types.LIST)) {
            valueType = MappedValue.ValueType.LIST;
            final List<Type> typeParameters = Utils.getTypeParameters(returnTypeMirror);
            if (typeParameters.size() > 0) {
                itemType = typeParameters.get(0);
                finalReturnType = Types.LIST.genericVersion(itemType);
            } else {
                mProcessingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Type Parameter Wildcards on Lists are not supported! You need to explicitly state the type of the elements in the returned List!!1", method);
                itemType = null;
                finalReturnType = null;
            }
        } else if (baseReturnType.isSubTypeOf(Types.SET)) {
            valueType = MappedValue.ValueType.SET;
            final List<Type> typeParameters = Utils.getTypeParameters(returnTypeMirror);
            if (typeParameters.size() > 0) {
                itemType = typeParameters.get(0);
                finalReturnType = Types.SET.genericVersion(itemType);
            } else {
                mProcessingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Type Parameter Wildcards on Sets are not supported! You need to explicitly state the type of the elements in the returned List!!1", method);
                itemType = null;
                finalReturnType = null;
            }
        } else {
            valueType = MappedValue.ValueType.VALUE;
            itemType = baseReturnType;
            finalReturnType = baseReturnType;
        }


        final Field field = builder.addField(finalReturnType, EnumSet.of(Modifier.PUBLIC, Modifier.FINAL));
        final String key = Utils.getAnnotationValue(method, Annotations.KEY, "value").getValue().toString();
        final boolean optional = Utils.hasAnnotation(method, Annotations.OPTIONAL);
        if (optional && Types.isPrimitive(itemType)) {
            mProcessingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "You cannot use @Optional when the return type of the method is a primitive! Use Boxed return types instead. (e.g. int -> Integer)", method);
        }

        builder.addMethod(finalReturnType, name, modifiers, new ExecutableBuilder() {
            @Override
            public List<Variable> createParameterList(VariableGenerator generator) {
                return new ArrayList<>();
            }

            @Override
            public void writeBody(CodeBlock code, VariableGenerator generator) {
                code.append("return ").append(field).append(";\n");
            }
        });

        return new MappedValue(key, itemType, optional, valueType, method, field);
    }
}
