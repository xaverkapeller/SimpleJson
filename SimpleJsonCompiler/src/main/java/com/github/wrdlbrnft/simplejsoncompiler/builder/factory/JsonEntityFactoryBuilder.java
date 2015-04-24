package com.github.wrdlbrnft.simplejsoncompiler.builder.factory;

import com.github.wrdlbrnft.codebuilder.code.CodeBlock;
import com.github.wrdlbrnft.codebuilder.code.ExecutableBuilder;
import com.github.wrdlbrnft.codebuilder.elements.Field;
import com.github.wrdlbrnft.codebuilder.elements.Type;
import com.github.wrdlbrnft.codebuilder.elements.Variable;
import com.github.wrdlbrnft.codebuilder.impl.ClassBuilder;
import com.github.wrdlbrnft.codebuilder.impl.Types;
import com.github.wrdlbrnft.codebuilder.impl.VariableGenerator;
import com.github.wrdlbrnft.simplejsoncompiler.models.ImplementationResult;
import com.github.wrdlbrnft.simplejsoncompiler.models.MappedValue;

import java.io.IOException;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;

/**
 * Created by kapeller on 24/04/15.
 */
public class JsonEntityFactoryBuilder {

    private static final String FACTORY_CLASS_NAME = "JsonEntities";
    private static final String FACTORY_PACKAGE_NAME = "com.github.wrdlbrnft.simplejson";

    private final ProcessingEnvironment mProcessingEnvironment;
    private final List<ImplementationResult> mImplementationResultList;

    public JsonEntityFactoryBuilder(ProcessingEnvironment processingEnvironment, List<ImplementationResult> implementationResultList) {
        mProcessingEnvironment = processingEnvironment;
        mImplementationResultList = implementationResultList;
    }

    public void build() throws IOException {
        final ClassBuilder builder = new ClassBuilder(mProcessingEnvironment, FACTORY_CLASS_NAME);
        builder.setPackageName(FACTORY_PACKAGE_NAME);
        builder.setModifiers(EnumSet.of(Modifier.PUBLIC, Modifier.FINAL));

        for (ImplementationResult result : mImplementationResultList) {
            final Type interfaceType = result.getInterfaceType();
            final Type implType = result.getImplType();
            final List<MappedValue> mappedValueList = result.getMappedValues();
            builder.addImport(implType);

            final Set<Type> requiredImports = resolveImportsFromMappedValues(mappedValueList);
            for (Type type : requiredImports) {
                builder.addImport(type);
            }

            final Variable[] parameters = resolveConstructorParameters(mappedValueList);

            final String factoryMethodName = "new" + interfaceType.className();
            builder.addMethod(interfaceType, factoryMethodName, EnumSet.of(Modifier.PUBLIC, Modifier.STATIC), new ExecutableBuilder() {
                @Override
                public List<Variable> createParameterList(VariableGenerator generator) {
                    return Arrays.asList(parameters);
                }

                @Override
                public void writeBody(CodeBlock code, VariableGenerator generator) {
                    code.append("return ").append(implType.newInstance(parameters)).append(";\n");
                }
            });
        }

        builder.build();
    }

    private Set<Type> resolveImportsFromMappedValues(List<MappedValue> mappedValueList) {
        final Set<Type> importSet = new HashSet<>();

        for (MappedValue value : mappedValueList) {
            final ExecutableElement element = value.getMethod();
            final TypeMirror returnTypeMirror = element.getReturnType();
            final Set<Type> imports = resolveImportsFromTypeMirror(returnTypeMirror);
            importSet.addAll(imports);
        }

        return importSet;
    }

    private Set<Type> resolveImportsFromTypeMirror(TypeMirror typeMirror) {
        final Set<Type> importSet = new HashSet<>();
        importSet.add(Types.create(typeMirror));

        if (typeMirror instanceof DeclaredType) {
            final DeclaredType declaredType = (DeclaredType) typeMirror;
            for (TypeMirror typeArgument : declaredType.getTypeArguments()) {
                final Set<Type> resolvedTypeArgumentSet = resolveImportsFromTypeMirror(typeArgument);
                importSet.addAll(resolvedTypeArgumentSet);
            }
        }

        return importSet;
    }

    private Variable[] resolveConstructorParameters(List<MappedValue> mappedValueList) {
        final Variable[] parameters = new Variable[mappedValueList.size()];

        for (int i = 0, count = mappedValueList.size(); i < count; i++) {
            final MappedValue mappedValue = mappedValueList.get(i);
            final Field field = mappedValue.getField();
            final ExecutableElement method = mappedValue.getMethod();
            final String methodName = method.getSimpleName().toString();
            final String parameterName = createParameterNameFromMethod(methodName);
            final Type type = field.type();
            parameters[i] = new StubVariableImpl(parameterName, type, EnumSet.noneOf(Modifier.class));
        }

        return parameters;
    }

    private String createParameterNameFromMethod(String methodName) {
        if (methodName.startsWith("get")) {
            return methodName.substring(3, 4).toLowerCase()
                    + methodName.substring(4, methodName.length());
        }

        return methodName;
    }

    private static class StubVariableImpl implements Variable {

        private final String mName;
        private final Type mType;
        private final Set<Modifier> mModifiers;

        private StubVariableImpl(String name, Type type, Set<Modifier> modifiers) {
            mName = name;
            mType = type;
            mModifiers = modifiers;
        }

        @Override
        public String name() {
            return mName;
        }

        @Override
        public String set(String value) {
            return null;
        }

        @Override
        public String set(Variable variable) {
            return null;
        }

        @Override
        public Type type() {
            return mType;
        }

        @Override
        public String initialize() {
            return null;
        }

        @Override
        public String initialize(Variable variable) {
            return null;
        }

        @Override
        public String initialize(String value) {
            return null;
        }

        @Override
        public String cast(Type type) {
            return null;
        }

        @Override
        public Set<Modifier> modifiers() {
            return mModifiers;
        }

        @Override
        public String equalsTo(String value) {
            return null;
        }

        @Override
        public String equalsTo(Variable variable) {
            return null;
        }

        @Override
        public String notEqualsTo(String value) {
            return null;
        }

        @Override
        public String notEqualsTo(Variable variable) {
            return null;
        }
    }
}
