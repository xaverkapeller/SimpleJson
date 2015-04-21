package com.github.wrdlbrnft.simplejsoncompiler;

import com.github.wrdlbrnft.codebuilder.elements.Type;
import com.github.wrdlbrnft.codebuilder.impl.Types;
import com.github.wrdlbrnft.simplejsoncompiler.builder.enums.EnumParserBuilder;
import com.github.wrdlbrnft.simplejsoncompiler.builder.implementation.ImplementationBuilder;
import com.github.wrdlbrnft.simplejsoncompiler.builder.parser.ParserBuilder;
import com.github.wrdlbrnft.simplejsoncompiler.models.ImplementationResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.tools.Diagnostic;

public class SimpleJsonCompiler extends AbstractProcessor {

    private final Map<String, Type> mEnumParserMap = new HashMap<>();

    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {

        TypeElement jsonEntityAnnotation = null;
        TypeElement jsonEnumAnnotation = null;
        for (TypeElement annotation : annotations) {
            final String annotationClassName = annotation.asType().toString();
            if (annotationClassName.equals(Annotations.JSON_ENTITY)) {
                jsonEntityAnnotation = annotation;
            }
            if (annotationClassName.equals(Annotations.JSON_ENUM)) {
                jsonEnumAnnotation = annotation;
            }
        }

        if (jsonEnumAnnotation != null) {
            final Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(jsonEnumAnnotation);
            for (Element element : annotatedElements) {
                try {
                    if (element.getKind() == ElementKind.ENUM) {
                        final TypeElement enumElement = (TypeElement) element;
                        final Type enumType = Types.create(enumElement);

                        final EnumParserBuilder builder = new EnumParserBuilder(processingEnv, enumElement);
                        final Type parserType = builder.build();

                        mEnumParserMap.put(enumType.fullClassName(), parserType);
                    } else {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "You can only annotate enums with @JsonEnum! ", element);
                    }
                } catch (Exception e) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Could not generate parser or implementation for " + element.getSimpleName() + "! " + e.toString(), element);
                    e.printStackTrace();
                }
            }
        }

        if (jsonEntityAnnotation != null) {
            final Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(jsonEntityAnnotation);
            for (Element element : annotatedElements) {
                try {
                    if (element.getKind() == ElementKind.INTERFACE) {
                        final TypeElement model = (TypeElement) element;
                        buildClasses(model);
                    } else {
                        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "You can only annotate interfaces with @JsonEntity! ", element);
                    }
                } catch (Exception e) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "Could not generate parser or implementation for " + element.getSimpleName() + "! " + e.toString(), element);
                    e.printStackTrace();
                }
            }
        }

        return false;
    }

    private void buildClasses(TypeElement model) throws IOException {
        final ImplementationBuilder implementationBuilder = new ImplementationBuilder(processingEnv, model);
        final ImplementationResult result = implementationBuilder.build();

        final ParserBuilder parserBuilder = new ParserBuilder(processingEnv, model, result, mEnumParserMap);
        parserBuilder.build();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        final Set<String> annotationSet = new HashSet<>();
        annotationSet.add(Annotations.JSON_ENTITY);
        annotationSet.add(Annotations.JSON_ENUM);
        return annotationSet;
    }
}
