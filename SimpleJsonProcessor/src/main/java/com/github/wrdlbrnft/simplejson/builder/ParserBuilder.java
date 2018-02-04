package com.github.wrdlbrnft.simplejson.builder;

import com.github.wrdlbrnft.codebuilder.code.SourceFile;
import com.github.wrdlbrnft.codebuilder.implementations.Implementation;
import com.github.wrdlbrnft.codebuilder.types.Type;
import com.github.wrdlbrnft.codebuilder.util.Utils;
import com.github.wrdlbrnft.simplejson.builder.builder.BuilderBuilder;
import com.github.wrdlbrnft.simplejson.builder.enums.EnumParserBuilder;
import com.github.wrdlbrnft.simplejson.builder.factories.entity.JsonEntityFactoryBuilder;
import com.github.wrdlbrnft.simplejson.builder.factories.enums.EnumFactoryBuilder;
import com.github.wrdlbrnft.simplejson.builder.implementation.ImplementationBuilder;
import com.github.wrdlbrnft.simplejson.builder.parser.InternalParserBuilder;
import com.github.wrdlbrnft.simplejson.models.ImplementationResult;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.TypeElement;

/**
 * Created by kapeller on 13/01/16.
 */
public class ParserBuilder {

    private final ProcessingEnvironment mProcessingEnvironment;

    public interface BuildCache {
        Type getEnumParser(TypeElement element);
        Type getEntityParser(TypeElement element);
        Type getCustomParser(TypeElement element);
        void registerParent(Implementation.Builder builder);
        boolean hasEntityParser(TypeElement element);
        List<Implementation> getImplementations();
        void clear();
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
        mProcessingEnvironment = processingEnvironment;
        final BuildCacheParserCollection collection = new BuildCacheParserCollection();
        mBuildCache = collection;
        mParserCollection = collection;

        mImplementationBuilder = new ImplementationBuilder(processingEnvironment);
        mEnumParserBuilder = new EnumParserBuilder(processingEnvironment);
        mInternalParserBuilder = new InternalParserBuilder(processingEnvironment, collection);
        mFactoryBuilder = new JsonEntityFactoryBuilder(mBuildCache, mImplementationBuilder, mInternalParserBuilder);
        mEnumFactoryBuilder = new EnumFactoryBuilder(processingEnvironment);
    }

    public void build(TypeElement element) throws IOException {
        try {
            final SourceFile sourceFile = SourceFile.create(mProcessingEnvironment, Utils.getPackageName(element));
            sourceFile.write(mFactoryBuilder.build(element));
            sourceFile.flushAndClose();
        } finally {
            mBuildCache.clear();
        }
    }

    public ParserCollection getParserCollection() {
        return mParserCollection;
    }

    private class BuildCacheParserCollection implements BuildCache, ParserCollection {

        private final Map<String, Implementation> mClassNameToEnumParserMap = new HashMap<>();
        private final Map<String, Implementation> mClassNameToEntityParserMap = new HashMap<>();
        private final Map<String, Type> mClassNameToCustomParserMap = new HashMap<>();

        private Implementation.Builder mParent;

        @Override
        public Type getEnumParser(TypeElement element) {
            final String qualifiedName = element.getQualifiedName().toString();
            final Implementation cachedType = mClassNameToEnumParserMap.get(qualifiedName);
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
        public void registerParent(Implementation.Builder builder) {
            mParent = builder;
        }

        @Override
        public boolean hasEntityParser(TypeElement element) {
            final String qualifiedName = element.getQualifiedName().toString();
            return mClassNameToEntityParserMap.containsKey(qualifiedName);
        }

        @Override
        public List<Implementation> getImplementations() {
            final List<Implementation> implementations = new ArrayList<>();

            for (String key : mClassNameToEnumParserMap.keySet()) {
                final Implementation implementation = mClassNameToEnumParserMap.get(key);
                implementations.add(implementation);
            }

            for (String key : mClassNameToEntityParserMap.keySet()) {
                final Implementation implementation = mClassNameToEntityParserMap.get(key);
                implementations.add(implementation);
            }

            return implementations;
        }

        @Override
        public void clear() {
            mClassNameToCustomParserMap.clear();
            mClassNameToEntityParserMap.clear();
            mClassNameToEnumParserMap.clear();
        }

        private void createEnumParser(TypeElement element, String qualifiedName) {
            final Implementation parserType = mEnumParserBuilder.build(element);
            final Implementation factoryImpl = mEnumFactoryBuilder.build(parserType, element);
            mClassNameToEnumParserMap.put(qualifiedName, parserType);
            mParent.addNestedImplementation(parserType);
            mParent.addNestedImplementation(factoryImpl);
        }

        private void createImplementationAndParser(TypeElement element, String qualifiedName) {
            final ImplementationResult result = mImplementationBuilder.build(element);
            final Implementation parserType = mInternalParserBuilder.build(element, result);
            mClassNameToEntityParserMap.put(qualifiedName, parserType);
            mParent.addNestedImplementation(result.getImplType());
            mParent.addNestedImplementation(parserType);
        }

        @Override
        public Map<TypeElement, Type> getEntityToParserMap() {
            return null;
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
