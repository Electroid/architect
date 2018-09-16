package app.ashcon.architect.level.command.annotation;

import app.ashcon.architect.level.Level;
import app.ashcon.architect.level.type.Role;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

/**
 * Parameter annotation that requires a {@link Role} from a {@link Level}.
 */
@Retention(RetentionPolicy.RUNTIME)
public @interface Require {
    Role value();
}
