package app.ashcon.architect.level.type;

import app.ashcon.architect.level.Level;

/**
 * Represents the different flags for a {@link Level} that
 * either enable or disable world features.
 */
public enum Flag {
    ANIMALS,    // Any entity that is an Animal can spawn.
    MONSTERS,   // Any entity that is a Monster can spawn.
    EXPLOSIONS, // Explosions can harm entities or blocks.
    FIRE,       // Fire can spread.
    PHYSICS,    // Block physics, such as sand falling, can occur.
    WEATHER,    // Weather, such as rain or lightning, can occur.
    WORLD;      // Generic world events, such as water flow, can occur.
}
