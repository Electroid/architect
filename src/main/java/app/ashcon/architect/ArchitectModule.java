package app.ashcon.architect;

import app.ashcon.architect.level.LevelListener;
import app.ashcon.architect.level.LevelLoader;
import app.ashcon.architect.level.command.LevelCommands;
import app.ashcon.architect.level.command.provider.LevelCurrentProvider;
import app.ashcon.architect.level.command.provider.LevelNamedProvider;
import app.ashcon.architect.user.UserListener;
import dagger.Module;

@Module
public interface ArchitectModule {

    void provideLevelCommands(LevelCommands levelCommands);

    void provideLevelListener(LevelListener levelListener);

    void provideLevelLoader(LevelLoader levelLoader);

    void provideLevelNamedProvider(LevelNamedProvider levelNamedProvider);

    void provideLevelCurrentProvider(LevelCurrentProvider levelCurrentProvider);

    void provideUserListener(UserListener userListener);

}
