package com.github.wrdlbrnft.simplejson;

import com.github.wrdlbrnft.codebuilder.types.Type;
import com.github.wrdlbrnft.codebuilder.types.Types;
import com.github.wrdlbrnft.codebuilder.util.Utils;
import com.github.wrdlbrnft.simplejson.builder.ParserBuilder;

import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.tools.Diagnostic;

public class SimpleJsonProcessor extends AbstractProcessor {
    private static final String ELEMENT_PARSER_TYPE_NAME = "com.github.wrdlbrnft.simplejson.parsers.ElementParser";

    private ParserBuilder mParserBuilder;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnvironment) {
        super.init(processingEnvironment);
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        try {
            tryProcess(annotations, roundEnv);
        } catch (Exception e) {
            final String errorMessage = "Could not generate Factory implementation for JsonEntities!";
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, errorMessage);
            Logger.getAnonymousLogger().log(Level.SEVERE, errorMessage, e);
        }

        return false;
    }

    private void tryProcess(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws IOException {
        mParserBuilder = new ParserBuilder(processingEnv);
        final ParserBuilder.ParserCollection parserCollection = mParserBuilder.getParserCollection();

        final Map<String, Type> customParserMap = createCustomParserMap(annotations, roundEnv);
        parserCollection.registerCustomParsers(customParserMap);

        tryHandleAnnotations(annotations, roundEnv);
    }

    private TypeElement getTypeElement(String charSequence) {
        return processingEnv.getElementUtils().getTypeElement(charSequence);
    }

    private Map<String, Type> createCustomParserMap(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        final Map<String, Type> customParserMap = new HashMap<>();

        final TypeElement annotation = Utils.getTypeElementFromList(annotations, SimpleJsonAnnotations.CUSTOM_JSON_PARSER);
        if (annotation != null) {
            final TypeMirror elementParserTypeMirror = getTypeElement(ELEMENT_PARSER_TYPE_NAME).asType();

            final Set<? extends Element> customParsers = roundEnv.getElementsAnnotatedWith(annotation);
            for (Element parser : customParsers) {
                final TypeMirror parserTypeMirror = parser.asType();
                if (!Utils.isSubTypeOf(processingEnv, parserTypeMirror, elementParserTypeMirror)) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, parser.getSimpleName() + " is not a subtype of ElementParser.", parser);
                    continue;
                }

                final List<TypeMirror> typeParameters = Utils.getTypeParametersOfInterface(processingEnv, parserTypeMirror, elementParserTypeMirror);
                if (typeParameters.isEmpty()) {
                    processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, parser.getSimpleName() + " does not specify its type parameter.", parser);
                    continue;
                }

                final TypeMirror typeParameterMirror = typeParameters.get(0);
                final String qualifiedName = typeParameterMirror.toString();
                customParserMap.put(qualifiedName, Types.of(parserTypeMirror));
            }
        }

        return customParserMap;
    }

    private void tryHandleAnnotations(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) throws IOException {
        final TypeElement jsonEntityAnnotation = Utils.getTypeElementFromList(annotations, SimpleJsonAnnotations.JSON_ENTITY);
        if (jsonEntityAnnotation != null) {
            final Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(jsonEntityAnnotation);
            for (Element element : annotatedElements) {
                handleJsonEntity(element);
            }
        }
    }

    private void handleJsonEntity(Element element) {
        try {
            tryHandleJsonEntity(element);
        } catch (Exception e) {
            final String errorMessage = "Could not generate parser or implementation for " + element.getSimpleName() + "!";
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, errorMessage, element);
            Logger.getAnonymousLogger().log(Level.SEVERE, errorMessage, e);
        }
    }

    private void tryHandleJsonEntity(Element element) throws IOException {
        if (element.getKind() == ElementKind.INTERFACE) {
            final TypeElement model = (TypeElement) element;
            mParserBuilder.build(model);
        } else {
            processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, "You can only annotate interfaces with @JsonEntity! ", element);
        }
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        final Set<String> annotationTypes = new HashSet<>();

        annotationTypes.add(SimpleJsonAnnotations.JSON_ENTITY);
        annotationTypes.add(SimpleJsonAnnotations.JSON_ENUM);
        annotationTypes.add(SimpleJsonAnnotations.CUSTOM_JSON_PARSER);

        return annotationTypes;
    }
}
