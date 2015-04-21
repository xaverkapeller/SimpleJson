package com.github.wrdlbrnft.codebuilder.impl;

import com.github.wrdlbrnft.codebuilder.code.ExecutableBuilder;
import com.github.wrdlbrnft.codebuilder.elements.Constructor;
import com.github.wrdlbrnft.codebuilder.elements.Field;
import com.github.wrdlbrnft.codebuilder.elements.Method;
import com.github.wrdlbrnft.codebuilder.elements.Type;
import com.github.wrdlbrnft.codebuilder.elements.Variable;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.tools.JavaFileObject;

/**
 * Created with Android Studio
 * User: Xaver
 * Date: 09/12/14
 */
public class ClassBuilder {

    private final ProcessingEnvironment mEnvironment;
    private String mClassName;
    private String mPackageName = "";
    private Set<Modifier> mModifiers = new HashSet<Modifier>();
    private Set<Type> mImports = new HashSet<>();
    private Type mExtends = null;
    private Set<Type> mImplements = new HashSet<Type>();
    private final List<ClassBuilder> mSubClassBuilders = new ArrayList<>();

    private final VariableGenerator mFieldGenerator = new VariableGenerator();

    private final Set<FieldImpl> mFields = new HashSet<FieldImpl>();
    private final Set<MethodImpl> mMethods = new HashSet<MethodImpl>();
    private final Set<ConstructorImpl> mConstructors = new HashSet<ConstructorImpl>();

    public ClassBuilder(ProcessingEnvironment environment, String className) {
        mEnvironment = environment;
        mClassName = className;
        mFieldGenerator.setPrefix("_");
    }

    public VariableGenerator fieldGenerator() {
        return mFieldGenerator;
    }

    public ClassBuilder setPackageName(String packageName) {
        mPackageName = packageName;
        return this;
    }

    public ClassBuilder setClassName(String className) {
        mClassName = className;
        return this;
    }

    public ClassBuilder addImport(Type type) {
        mImports.add(type);
        return this;
    }

    public ClassBuilder setModifiers(Set<Modifier> modifiers) {
        mModifiers = modifiers;
        return this;
    }

    public ClassBuilder setExtends(Type type) {
        mExtends = type;
        mImports.add(type);
        return this;
    }

    public ClassBuilder setImplements(Set<Type> types) {
        mImplements = types;
        mImports.addAll(types);
        return this;
    }

    public Field addField(Type type, String name, Set<Modifier> modifiers) {
        final FieldImpl field = new FieldImpl(name, type, modifiers);
        mFields.add(field);
        mImports.add(type);
        return field;
    }

    public Field addField(Type type, Set<Modifier> modifiers) {
        final FieldImpl field = new FieldImpl(mFieldGenerator.generateName(), type, modifiers);
        mFields.add(field);
        mImports.add(type);
        return field;
    }

    public Method addMethod(Type returnType, String name, Set<Modifier> modifiers, List<Variable> parameters, List<Type> exceptions) {
        final MethodImpl method = new MethodImpl(returnType, name, parameters, modifiers, exceptions);
        mMethods.add(method);
        for (Variable parameter : parameters) {
            mImports.add(parameter.type());
        }
        for (Type type : exceptions) {
            mImports.add(type);
        }
        mImports.add(returnType);
        return method;
    }

    public Method addMethod(Type returnType, String name, Set<Modifier> modifiers, List<Variable> parameters) {
        return addMethod(returnType, name, modifiers, parameters, new ArrayList<Type>());
    }

    public Method addMethod(Type returnType, String name, Set<Modifier> modifiers, ExecutableBuilder builder) {
        return addMethod(returnType, name, modifiers, new ArrayList<Type>(), builder);
    }

    public Method addMethod(Type returnType, String name, Set<Modifier> modifiers, List<Type> exceptions, ExecutableBuilder builder) {
        final VariableGenerator generator = new VariableGenerator();
        final List<Variable> parameters = builder.createParameterList(generator);
        for (Variable parameter : parameters) {
            mImports.add(parameter.type());
        }
        for (Type type : exceptions) {
            mImports.add(type);
        }
        mImports.add(returnType);
        final MethodImpl method = new MethodImpl(returnType, name, parameters, modifiers, exceptions);
        mMethods.add(method);
        builder.writeBody(method.code(), generator);
        return method;
    }

    public Constructor addConstructor(Set<Modifier> modifiers, List<Variable> parameters, List<Type> exceptions) {
        final ConstructorImpl constructor = new ConstructorImpl(parameters, modifiers, exceptions);
        for (Variable parameter : parameters) {
            mImports.add(parameter.type());
        }
        for (Type type : exceptions) {
            mImports.add(type);
        }
        mConstructors.add(constructor);
        return constructor;
    }

    public Constructor addConstructor(Set<Modifier> modifiers, List<Variable> parameters) {
        return addConstructor(modifiers, parameters, new ArrayList<Type>());
    }

    public Method implementMethod(ExecutableElement method, ExecutableBuilder builder) {
        final String name = method.getSimpleName().toString();
        final Type returnType = Types.create(method.getReturnType());
        return addMethod(returnType, name, EnumSet.of(Modifier.PUBLIC), builder);
    }

    public Constructor addConstructor(Set<Modifier> modifiers, List<Type> exceptions, ExecutableBuilder builder) {
        final VariableGenerator generator = new VariableGenerator();
        final List<Variable> parameters = builder.createParameterList(generator);
        for (Variable parameter : parameters) {
            mImports.add(parameter.type());
        }
        for (Type type : exceptions) {
            mImports.add(type);
        }
        final ConstructorImpl constructor = new ConstructorImpl(parameters, modifiers, exceptions);
        mConstructors.add(constructor);
        builder.writeBody(constructor.code(), generator);
        return constructor;
    }

    public Constructor addConstructor(Set<Modifier> modifiers, ExecutableBuilder builder) {
        return addConstructor(modifiers, new ArrayList<Type>(), builder);
    }

    private String[] getImplementedInterfacesArray() {
        final int count = mImplements.size();
        final String[] array = new String[count];

        int i = 0;
        for (Type type : mImplements) {
            array[i++] = type.className();
        }

        return array;
    }

    public ClassBuilder addSubClass(String className) {
        final ClassBuilder builder = new ClassBuilder(mEnvironment, className);
        mSubClassBuilders.add(builder);
        return builder;
    }

    public Type build() throws IOException {
        final JavaFileObject sourceFile = mEnvironment.getFiler().createSourceFile(mClassName);

        final StringBuilder builder = new StringBuilder()
                .append("package ").append(mPackageName).append(";\n\n");

        final Type type = writeType(builder);

        final Writer writer = sourceFile.openWriter();
        writer.write(builder.toString());
        writer.close();

        return type;
    }

    protected Set<String> getImports() {
        final Set<String> imports = new HashSet<>();

        for (Type type : mImports) {
            if (type != null) {
                if (Types.isPrimitive(type)) {
                    continue;
                }

                imports.add(type.nonGenericVersion().fullClassName());
            }
        }

        return imports;
    }

    protected Type writeType(StringBuilder builder) throws IOException {
        final String[] implementedInterfaces = getImplementedInterfacesArray();

        final Set<String> imports = getImports();
        for (ClassBuilder subClassBuilder : mSubClassBuilders) {
            imports.addAll(subClassBuilder.getImports());
        }

        for (String className : imports) {
            builder.append("import ").append(className).append(";\n");
        }

        builder.append("\n");

        writeModifiers(builder, mModifiers);

        builder.append(" class ").append(mClassName);

        if (mExtends != null) {
            builder.append(" extends ").append(mExtends.className());
        }

        if (implementedInterfaces.length > 0) {
            builder.append(" implements ");

            for (int i = 0, count = implementedInterfaces.length; i < count; i++) {

                if (i > 0) {
                    builder.append(" ");
                }
                builder.append(implementedInterfaces[i]);
            }
        }

        builder.append(" {\n\n");

        for (FieldImpl field : mFields) {
            final String type = field.type().className();
            final String name = field.name();
            final Set<Modifier> modifiers = field.modifiers();
            final String initialValue = field.initialValue();

            writeModifiers(builder, modifiers);

            builder.append(" ").append(type).append(" ").append(name);

            if (initialValue != null) {
                builder.append(" = ").append(initialValue);
            }

            builder.append(";\n");
        }

        builder.append("\n");

        for (ConstructorImpl constructor : mConstructors) {
            final Set<Modifier> modifiers = constructor.modifiers();
            final List<Variable> parameters = constructor.parameters();
            final List<Type> exceptions = constructor.exceptions();

            writeModifiers(builder, modifiers);

            builder.append(" ").append(mClassName).append("(");

            for (int i = 0, count = parameters.size(); i < count; i++) {
                final Variable variable = parameters.get(i);

                if (i > 0) {
                    builder.append(", ");
                }

                writeModifiers(builder, variable.modifiers());
                builder.append(variable.type().className())
                        .append(" ").append(variable.name());
            }

            builder.append(")");

            for (int i = 0, count = exceptions.size(); i < count; i++) {
                final Type exception = exceptions.get(i);

                if (i == 0) {
                    builder.append(" throws ");
                } else {
                    builder.append(", ");
                }

                builder.append(exception.className());
            }

            builder.append(" {\n");
            constructor.code().write(builder);
            builder.append("}\n\n");
        }

        for (MethodImpl method : mMethods) {
            final Set<Modifier> modifiers = method.modifiers();
            final List<Variable> parameters = method.parameters();
            final List<Type> exceptions = method.exceptions();
            final String returnType = method.returnType() != null ? method.returnType().className() : "void";

            writeModifiers(builder, modifiers);

            builder.append(" ").append(returnType).append(" ").append(method.name()).append("(");

            for (int i = 0, count = parameters.size(); i < count; i++) {
                final Variable variable = parameters.get(i);

                if (i > 0) {
                    builder.append(", ");
                }

                writeModifiers(builder, variable.modifiers());
                if (variable.modifiers().size() > 0) {
                    builder.append(" ");
                }

                builder.append(variable.type().className())
                        .append(" ").append(variable.name());
            }

            builder.append(")");

            for (int i = 0, count = exceptions.size(); i < count; i++) {
                final Type exception = exceptions.get(i);

                if (i == 0) {
                    builder.append(" throws ");
                } else {
                    builder.append(", ");
                }

                builder.append(exception.className());
            }

            builder.append(" {\n");
            method.code().write(builder);
            builder.append("}\n\n");
        }

        for (ClassBuilder subClassBuilder : mSubClassBuilders) {
            subClassBuilder.writeType(builder);
        }

        builder.append("}");

        return new TypeImpl(mPackageName, mClassName, mModifiers, mExtends != null ? mExtends : Types.OBJECT);
    }

    private void writeModifiers(StringBuilder builder, Set<Modifier> modifiers) {
        boolean insertSpace = false;
        for (Modifier modifier : modifiers) {

            if (insertSpace) {
                builder.append(" ");
            } else {
                insertSpace = true;
            }

            builder.append(modifier.name().toLowerCase());
        }
    }
}
