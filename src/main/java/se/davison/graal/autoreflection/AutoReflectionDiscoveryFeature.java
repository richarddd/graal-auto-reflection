package se.davison.graal.autoreflection;

import com.oracle.svm.core.annotate.AutomaticFeature;
import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfo;
import org.graalvm.nativeimage.hosted.Feature;
import org.graalvm.nativeimage.hosted.RuntimeReflection;

import java.lang.reflect.TypeVariable;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@AutomaticFeature
@SuppressWarnings("unused")
public class AutoReflectionDiscoveryFeature implements Feature {

    private static final String REFLECT_ANNOTATION_NAME = Reflect.class.getName();
    private static final String REFLECTION_PROVIDER_NAME = ReflectionProvider.class.getName();

    private static final ClassGraph CLASS_GRAPH = new ClassGraph();

    @Override
    public void beforeAnalysis(BeforeAnalysisAccess access) {
        try {
            System.out.println("Scanning classes for reflection");

            for (ClassInfo info : CLASS_GRAPH.enableAnnotationInfo().scan().getClassesWithAnnotation(REFLECT_ANNOTATION_NAME)) {
                final Class<?> target = info.loadClass();
                System.out.println(String.format("Discovered class: %s", target.getName()));
                final Reflect reflect = (Reflect) info.getAnnotationInfo(REFLECT_ANNOTATION_NAME).loadClassAndInstantiate();
                if (reflect.allFields()) {
                    allowFields(target);
                }
                if (reflect.allMethods()) {
                    allowMethods(target);
                }
                if (reflect.constructor()) {
                    allowInstantiate(target);
                }
                if (reflect.innerClasses()) {
                    registerInnerClasses(info, target);
                }

            }
            ClassGraph classInfo = CLASS_GRAPH.enableClassInfo();
            for (ClassInfo info : classInfo.scan().getClassesImplementing(REFLECTION_PROVIDER_NAME)) {
                final Class<?> providerClass = info.loadClass();
                System.out.println(String.format("Using provider class: %s", providerClass.getName()));
                final ReflectionProvider provider = (ReflectionProvider) providerClass.newInstance();
                boolean shouldScan = false;
                if (provider.packages(CLASS_GRAPH).size() > 0) {
                    shouldScan = true;
                    classInfo = classInfo.whitelistPackages(provider.packages(CLASS_GRAPH).toArray(new String[0]));
                }
                if (provider.classes(CLASS_GRAPH).size() > 0 || provider.classNames(CLASS_GRAPH).size() > 0) {
                    shouldScan = true;
                    classInfo = classInfo.whitelistClasses(
                            Stream.concat(
                                    provider.classes(CLASS_GRAPH).stream().map(Class::getName),
                                    provider.classNames(CLASS_GRAPH).stream()).toArray(String[]::new));
                }
                if (shouldScan) {
                    for (ClassInfo providerInfo : classInfo.scan().getAllClasses()) {
                        try {
                            Class<?> parentClass = providerInfo.loadClass();
                            registerInnerClasses(providerInfo, parentClass);
                        } catch (IllegalArgumentException exception) {
                            ColorConsole.RED_BACKGROUND_BRIGHT.println(String.format("WARNING: Cant load class (%s)", providerInfo.getName()));
                        }
                    }
                }
            }


        } catch (Throwable ex) {
            throw new RuntimeException("Unable to analyse classes", ex);
        }

    }

    private void registerInnerClasses(ClassInfo providerInfo, Class<?> parentClass) {
        Stream.concat(
                Stream.of(parentClass),
                providerInfo.getInnerClasses().loadClasses().stream()
        ).forEach(target -> {
            allowFields(target);
            allowMethods(target);
            allowInstantiate(target);
        });
    }

    private void allowMethods(Class<?> cl) {
        System.out.println(String.format("\tAdding methods for %s", cl.getName()));
        try {
            Stream.of(cl.getDeclaredMethods()).forEach(RuntimeReflection::register);
        } catch (NoClassDefFoundError exception) {
            ColorConsole.RED_BACKGROUND_BRIGHT.println(String.format("WARNING: Cant register methods for (%s)", cl.getName()));
        }
    }

    private void allowFields(Class<?> cl) {
        System.out.println(String.format("\tAdding fields for %s", cl.getName()));

        try {
            Stream.of(cl.getDeclaredFields()).forEach(RuntimeReflection::register);
        } catch (NoClassDefFoundError exception) {
            ColorConsole.RED_BACKGROUND_BRIGHT.println(String.format("WARNING: Cant register fields for (%s)", cl.getName()));
        }
    }

    private void allowInstantiate(Class<?> cl) {
        System.out.println(String.format("\tAdding constructors for %s", cl.getName()));
        RuntimeReflection.register(cl);
        Stream.of(cl.getDeclaredConstructors()).forEach(c -> {
            System.out.println(String.format("\t\tRegistering constructor %s(%s)", cl.getSimpleName(), Arrays.stream(c.getTypeParameters()).map(TypeVariable::getName).collect(Collectors.joining())));
            RuntimeReflection.register(c);
        });
    }

    private static <T> Stream<T> mergeArrayStream(T[] a, T[] b) {
        return Stream.concat(Arrays.stream(a), Arrays.stream(b));
    }
}
