package app.ashcon.architect;

import app.ashcon.architect.level.Level;
import app.ashcon.architect.level.LevelStore;
import app.ashcon.architect.level.command.LevelCommands;
import app.ashcon.architect.level.command.provider.DynamicLevelProvider;
import app.ashcon.architect.level.command.provider.StaticLevelProvider;
import app.ashcon.architect.level.listener.LevelInteractListener;
import app.ashcon.architect.user.UserStore;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ArchitectModule.class})
interface ArchitectComponent {

    UserStore users();
    LevelStore levels();

    Level lobby();

    LevelCommands commands();

    DynamicLevelProvider dynamicProvider();

    StaticLevelProvider staticProvider();

    LevelInteractListener interactListener();

}
