package com.github.wrdlbrnft.simplejson.builder.implementation;

import com.github.wrdlbrnft.codebuilder.annotations.Annotations;
import com.github.wrdlbrnft.codebuilder.code.Block;
import com.github.wrdlbrnft.codebuilder.code.BlockWriter;
import com.github.wrdlbrnft.codebuilder.code.CodeElement;
import com.github.wrdlbrnft.codebuilder.executables.Constructor;
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
import com.github.wrdlbrnft.simplejson.models.ImplementationResult;
import com.github.wrdlbrnft.simplejson.models.MappedValue;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.PrimitiveType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

/**
 * Created by kapeller on 21/04/15.
 */
public class ImplementationBuilder {

    private final ProcessingEnvironment mProcessingEnvironment;
    private final InterfaceAnalyzer mAnalyzer;
    private final TypeMirror mTypeList;
    private final TypeMirror mTypeSet;

    private Implementation.Builder mBuilder;

    public ImplementationBuilder(ProcessingEnvironment processingEnvironment) {
        mProcessingEnvironment = processingEnvironment;
        mAnalyzer = new InterfaceAnalyzer(mProcessingEnvironment);

        mTypeList = mProcessingEnvironment.getElementUtils().getTypeElement("java.util.List").asType();
        mTypeSet = mProcessingEnvironment.getElementUtils().getTypeElement("java.util.Set").asType();
    }

    public ImplementationResult build(TypeElement model) {

        mBuilder = new Implementation.Builder();
        mBuilder.addImplementedType(Types.of(model));
        mBuilder.setModifiers(EnumSet.of(Modifier.PRIVATE, Modifier.STATIC));

        final List<MappedValue> mappedValues = new ArrayList<>();
        final List<MethodPairInfo> infos = mAnalyzer.analyze(model);
        for (MethodPairInfo info : infos) {
            mappedValues.add(createMappedValueWrapper(model, info));
        }

        final Constructor constructor = new Constructor.Builder()
                .setModifiers(EnumSet.of(Modifier.PUBLIC))
                .setCode(new ConstructorBuilder(mappedValues))
                .build();
        mBuilder.addConstructor(constructor);

        final LazyEvalType lazyImplType = new LazyEvalType();

        mBuilder.addMethod(new Method.Builder()
                .setModifiers(EnumSet.of(Modifier.PUBLIC))
                .setReturnType(Types.Primitives.BOOLEAN)
                .setName("equals")
                .addAnnotation(Annotations.forType(Override.class))
                .setCode(new EqualsExecutableBuilder(mappedValues, lazyImplType))
                .build());

        mBuilder.addMethod(new Method.Builder()
                .setModifiers(EnumSet.of(Modifier.PUBLIC))
                .setReturnType(Types.Primitives.INTEGER)
                .setName("hashCode")
                .addAnnotation(Annotations.forType(Override.class))
                .setCode(new HashCodeExecutableBuilder(mappedValues))
                .build());

        final Implementation implementation = mBuilder.build();
        lazyImplType.setType(implementation);

        return new ImplementationResult(implementation, model, mappedValues);
    }

    private MappedValue createMappedValueWrapper(TypeElement parent, MethodPairInfo info) {

        final ExecutableElement getter = info.getGetter();
        final ExecutableElement setter = info.getSetter();
        final TypeMirror baseType = getter.getReturnType();

        final Type resultingType;
        final TypeMirror itemType;
        final MappedValue.ValueType valueType;
        final boolean isParentType;
        if (baseType.getKind() == TypeKind.ARRAY) {
            mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "Arrays are not supported yet as return types!", getter);
            itemType = null;
            valueType = null;
            isParentType = false;
            resultingType = Types.Boxed.VOID;
        } else if (Utils.isSubTypeOf(mProcessingEnvironment, baseType, mTypeList)) {
            valueType = MappedValue.ValueType.LIST;
            final List<TypeMirror> typeParameters = Utils.getTypeParameters(baseType);
            if (!typeParameters.isEmpty()) {
                itemType = typeParameters.get(0);
                isParentType = Utils.isSameType(mProcessingEnvironment, itemType, parent.asType());
                resultingType = Types.generic(Types.LIST, Types.of(itemType));
            } else {
                mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "Type Parameter Wildcards on Lists are not supported! You need to explicitly state the type of the elements in the returned List!!1", getter);
                itemType = null;
                isParentType = false;
                resultingType = Types.LIST;
            }
        } else if (Utils.isSubTypeOf(mProcessingEnvironment, baseType, mTypeSet)) {
            valueType = MappedValue.ValueType.SET;
            final List<TypeMirror> typeParameters = Utils.getTypeParameters(baseType);
            if (!typeParameters.isEmpty()) {
                itemType = typeParameters.get(0);
                isParentType = Utils.isSameType(mProcessingEnvironment, itemType, parent.asType());
                resultingType = Types.generic(Types.SET, Types.of(itemType));
            } else {
                mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "Type Parameter Wildcards on Sets are not supported! You need to explicitly state the type of the elements in the returned List!!1", getter);
                itemType = null;
                isParentType = false;
                resultingType = Types.SET;
            }
        } else {
            valueType = MappedValue.ValueType.VALUE;
            itemType = baseType;
            isParentType = Utils.isSameType(mProcessingEnvironment, itemType, parent.asType());
            resultingType = Types.of(itemType);
        }

        final Field field = new Field.Builder()
                .setType(resultingType)
                .setModifiers(setter == null ? EnumSet.of(Modifier.PRIVATE, Modifier.FINAL) : EnumSet.of(Modifier.PRIVATE))
                .build();
        mBuilder.addField(field);

        final boolean optional = Utils.hasAnnotation(getter, SimpleJsonAnnotations.OPTIONAL);
        if (optional && itemType instanceof PrimitiveType) {
            mProcessingEnvironment.getMessager().printMessage(Diagnostic.Kind.ERROR, "You cannot use @Optional when the return type of the method is a primitive! Use Boxed return types instead. (e.g. int -> Integer)", getter);
        }

        if (setter != null) {
            final Method setterMethod = createSetterImplementation(info, resultingType, field);
            if (setterMethod != null) {
                mBuilder.addMethod(setterMethod);
            }
        }

        final Method getterMethod = createGetterImplementation(info, resultingType, field);
        if (getterMethod != null) {
            mBuilder.addMethod(getterMethod);
        }

        return new MappedValue(info.getFieldName(), baseType, itemType, optional, valueType, isParentType, info, field);
    }

    private Method createGetterImplementation(MethodPairInfo info, Type resultingType, final Field field) {
        final ExecutableElement getter = info.getGetter();
        return new Method.Builder()
                .setName(getter.getSimpleName().toString())
                .setReturnType(resultingType)
                .addAnnotation(Annotations.forType(Override.class))
                .setModifiers(EnumSet.of(Modifier.PUBLIC))
                .setCode(new ExecutableBuilder() {
                    @Override
                    protected List<Variable> createParameters() {
                        return new ArrayList<>();
                    }

                    @Override
                    protected void write(Block block) {
                        block.append("return ").append(field).append(";");
                    }
                })
                .build();
    }

    private Method createSetterImplementation(MethodPairInfo info, final Type resultingType, final Field field) {
        final ExecutableElement setter = info.getSetter();
        final List<? extends VariableElement> parameters = setter.getParameters();
        if (parameters.size() != 1) {
            return null;
        }

        return new Method.Builder()
                .setName(setter.getSimpleName().toString())
                .addAnnotation(Annotations.forType(Override.class))
                .setModifiers(EnumSet.of(Modifier.PUBLIC))
                .setCode(new ExecutableBuilder() {

                    private Variable parameter;

                    @Override
                    protected List<Variable> createParameters() {
                        final List<Variable> parameters = new ArrayList<>();

                        parameters.add(parameter = Variables.of(resultingType));

                        return parameters;
                    }

                    @Override
                    protected void write(Block block) {
                        block.set(field, parameter).append(";");
                    }
                })
                .build();
    }

    private static class LazyEvalType extends BlockWriter implements Type {

        private Type mInternalType;

        @Override
        public CodeElement newInstance(CodeElement... parameters) {
            return Types.createNewInstance(this, parameters);
        }

        @Override
        public CodeElement classObject() {
            return Types.classOf(this);
        }

        @Override
        protected void write(Block block) {
            block.append(mInternalType);
        }

        public void setType(Type type) {
            mInternalType = type;
        }
    }
}
