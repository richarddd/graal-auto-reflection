package se.davison.graal.autoreflection;

import io.github.classgraph.ClassGraph;

import java.util.List;

public interface ReflectionProvider {

    List<Class<?>> classes(ClassGraph classGraph);

    List<String> classNames(ClassGraph classGraph);

    List<String> packages(ClassGraph classGraph);
}
