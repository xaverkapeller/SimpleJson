package com.github.wrdlbrnft.simplejson.builder;

import com.github.wrdlbrnft.codebuilder.types.Type;
import com.github.wrdlbrnft.simplejson.builder.enums.EnumParserBuilder;
import com.github.wrdlbrnft.simplejson.builder.factories.entity.JsonEntityFactoryBuilder;
import com.github.wrdlbrnft.simplejson.builder.factories.enums.EnumFactoryBuilder;
import com.github.wrdlbrnft.simplejson.builder.implementation.ImplementationBuilder;
import com.github.wrdlbrnft.simplejson.builder.parser.InternalParserBuilder;
import com.github.wrdlbrnft.simplejson.models.ImplementationResult;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

/**
 * Created by kapeller on 13/01/16.
 */
public class ParserBuilder {

    public interface BuildCache {
        Type getEnumParser(TypeElement element);
        Type getEntityParser(TypeElement element);
        Type getCustomParser(TypeElement element);
        boolean hasEntityParser(TypeElement element);
    }

    public interface ParserCollection {
        Map<TypeElement, Type> getEntityToParserMap();
        void registerCustomParsers(Map<String, Type> parserMap);
    }

    private final ImplementationBuilder mImplementationBuilder;
    private final EnumParserBuilder mEnumParserBuilder;
    private final InternalParserBuilder mInternalParserBuilder;
    private final JsonEntityFactoryBuilder mFactoryBuilder;
    private final EnumFactoryBuilder mEnumFactoryBuilder;
    private final BuildCache mBuildCache;
    private final ParserCollection mParserCollection;

    public ParserBuilder(ProcessingEnvironment processingEnvironment) {
        final BuildCacheParserCollection collection = new BuildCacheParserCollection();
        mBuildCache = collection;
        mParserCollection = collection;

        mImplementationBuilder = new ImplementationBuilder(processingEnvironment);
        mEnumParserBuilder = new EnumParserBuilder(processingEnvironment);
        mInternalParserBuilder = new InternalParserBuilder(processingEnvironment, collection);
        mFactoryBuilder = new JsonEntityFactoryBuilder(processingEnvironment);
        mEnumFactoryBuilder = new EnumFactoryBuilder(processingEnvironment);
    }

    public void build(TypeElement interfaceElement) throws IOException {
        if (mBuildCache.hasEntityParser(interfaceElement)) {
            return;
        }

        mBuildCache.getEntityParser(interfaceElement);
    }

    public ParserCollection getParserCollection() {
        return mParserCollection;
    }

    private class BuildCacheParserCollection implements BuildCache, ParserCollection {

        private final Map<TypeElement, Type> mEntityToParserMap = new HashMap<>();

        private final Map<String, Type> mClassNameToEnumParserMap = new HashMap<>();
        private final Map<String, Type> mClassNameToEntityParserMap = new HashMap<>();
        private final Map<String, Type> mClassNameToCustomParserMap = new HashMap<>();

        @Override
        public Type getEnumParser(TypeElement element) {
            final String qualifiedName = element.getQualifiedName().toString();
            final Type cachedType = mClassNameToEnumParserMap.get(qualifiedName);
            if (cachedType != null) {
                return cachedType;
            }

            createEnumParser(element, qualifiedName);
            return mClassNameToEnumParserMap.get(qualifiedName);
        }

        @Override
        public Type getEntityParser(TypeElement element) {
            final String qualifiedName = element.getQualifiedName().toString();
            final Type cachedType = mClassNameToEntityParserMap.get(qualifiedName);
            if (cachedType != null) {
                return cachedType;
            }

            createImplementationAndParser(element, qualifiedName);
            return mClassNameToEntityParserMap.get(qualifiedName);
        }

        @Override
        public Type getCustomParser(TypeElement element) {
            final String qualifiedName = element.getQualifiedName().toString();
            return mClassNameToCustomParserMap.get(qualifiedName);
        }

        @Override
        public boolean hasEntityParser(TypeElement element) {
            final String qualifiedName = element.getQualifiedName().toString();
            return mClassNameToEntityParserMap.containsKey(qualifiedName);
        }

        private void createEnumParser(TypeElement element, String qualifiedName) {
            try {
                final Type parserType = mEnumParserBuilder.build(element);
                mEnumFactoryBuilder.build(parserType, element);
                mClassNameToEnumParserMap.put(qualifiedName, parserType);
            } catch (IOException e) {
                throw new IllegalStateException("SimpleJson Processor is broken! Do you have enough disk space?", e);
            }
        }

        private void createImplementationAndParser(TypeElement element, String qualifiedName) {
            try {
                final ImplementationResult result = mImplementationBuilder.build(element);
                final Type parserType = mInternalParserBuilder.build(element, result);
                mFactoryBuilder.build(parserType, result);
                mClassNameToEntityParserMap.put(qualifiedName, parserType);
                mEntityToParserMap.put(element, parserType);
            } catch (IOException e) {
                throw new IllegalStateException("SimpleJson Processor is broken! Do you have enough disk space?", e);
            }
        }

        @Override
        public Map<TypeElement, Type> getEntityToParserMap() {
            return mEntityToParserMap;
        }

        @Override
        public void registerCustomParsers(Map<String, Type> parserMap) {
            for (String key : parserMap.keySet()) {
                final Type parser = parserMap.get(key);
                mClassNameToCustomParserMap.put(key, parser);
            }
        }
    }
}
