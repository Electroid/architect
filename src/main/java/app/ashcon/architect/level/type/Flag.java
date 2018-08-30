package app.ashcon.architect.level.type;

public enum Flag {
    ANIMALS,    // Any entity that is an Animal will not spawn.
    MONSTERS,   // Any entity that is a Monster will not spawn.
    EXPLOSIONS, // Explosions will not harm entities or blocks.
    FIRE,       // Fire does not spread.
    PHYSICS,    // Block physics, such as sand falling, are disabled.
    WEATHER,    // Weather, such as rain or lightning, is disabled.
    WORLD;      // Generic world events, such as water flow, are disabled.
}
