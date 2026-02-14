package org.hat.genaishield.api.privacy;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.FIELD)
public @interface Sensitive {
    SensitiveType type() default SensitiveType.FREE_TEXT;
    SensitiveAction action() default SensitiveAction.MASK;
}
