package com.github.tylerspaeth.common;

import org.reflections.Reflections;

import java.lang.annotation.Annotation;
import java.util.List;

/**
 * Utility class for finding things on the classpath.
 */
public class ClasspathScanner {

    private static final String PREFIX = "com.github.tylerspaeth";

    /**
     * Finds all Classes that are Annotated with a given Annotation.
     * @param annotationType The class of the annotation we are looking for.
     * @return List of all classes that are annotated with the given annotation.
     * @param <T> The annotation we are looking for.
     */
    public static <T extends Annotation> List<Class<?>> getClassesWithAnnotation(Class<T> annotationType) {
        Reflections reflections = new Reflections(PREFIX);
        return reflections.getTypesAnnotatedWith(annotationType).stream().toList();
    }

}
