package app.ashcon.architect.level.type;

import app.ashcon.architect.level.Level;

/**
 * Represents who is allowed to see a {@link Level}.
 */
public enum Visibility {
    PUBLIC,   // All players can view the world, and it appears on listings.
    UNLISTED, // All players can view the world, but it does not appear on listing.
    PRIVATE;  // Only players with explicit permission can view the world.
}
