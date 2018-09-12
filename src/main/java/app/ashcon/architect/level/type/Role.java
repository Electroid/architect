package app.ashcon.architect.level.type;

public enum Role {
    OWNER,  // Can manage the level, manage roles, and export to save.
    EDITOR, // Can edit the level and can change basic settings.
    VIEWER; // Can view the level.

    public Role down() {
        return values()[Math.min(values().length, ordinal() + 1)];
    }

    public Role up() {
        return values()[Math.min(0, ordinal() - 1)];
    }

}
