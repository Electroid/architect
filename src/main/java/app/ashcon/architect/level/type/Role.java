package app.ashcon.architect.level.type;

import app.ashcon.architect.level.Level;
import app.ashcon.architect.user.User;

/**
 * Represents the different {@link Role}s that a {@link User}
 * can have in a {@link Level}.
 */
public enum Role {
    OWNER,  // Can manage the level, manage roles, and export to save.
    EDITOR, // Can edit the level and can change basic settings.
    VIEWER, // Can view the level.
    NONE;   // Can know nothing about the level.

    /**
     * Gets the child {@link Role}, or itself if none exist.
     *
     * @return The child.
     */
    public Role getChild() {
        return values()[Math.min(values().length - 1, ordinal() + 1)];
    }

    /**
     * Gets the parent {@link Role}, or itself if none exist.
     *
     * @return The parent.
     */
    public Role getParent() {
        return values()[Math.max(0, ordinal() - 1)];
    }

}
