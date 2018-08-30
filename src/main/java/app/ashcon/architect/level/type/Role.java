package app.ashcon.architect.level.type;

import net.md_5.bungee.api.ChatColor;

public enum Role {

    OWNER (ChatColor.GREEN),  // Can manage the level, manage roles, and export to save.
    EDITOR(ChatColor.YELLOW), // Can edit the level and can change basic settings.
    VIEWER(ChatColor.GRAY);   // Can view the level.

    final ChatColor color;

    Role(ChatColor color) {
        this.color = color;
    }

    @Override
    public String toString() {
        return color + name();
    }

}
