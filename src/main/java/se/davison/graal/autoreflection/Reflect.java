package se.davison.graal.autoreflection;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Reflect {
    boolean constructor() default true;

    boolean allFields() default true;

    boolean allMethods() default true;

    boolean innerClasses() default true;
}
