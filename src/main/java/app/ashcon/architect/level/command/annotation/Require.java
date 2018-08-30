package app.ashcon.architect.level.command.annotation;

import app.ashcon.architect.level.type.Role;
import app.ashcon.intake.parametric.annotation.Classifier;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Require {
    Role value();
}
