package app.ashcon.architect.level.type;

import net.md_5.bungee.api.ChatColor;

public enum Visibility {

    PUBLIC  (ChatColor.GREEN),  // All players can view the world, and it appears on listings.
    UNLISTED(ChatColor.YELLOW), // All players can view the world, but it does not appear on listing.
    PRIVATE (ChatColor.RED);    // Only players with explicit permission can view the world.

    final ChatColor color;

    Visibility(ChatColor color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return color + name();
    }

}
