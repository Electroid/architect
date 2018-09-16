package app.ashcon.architect;

import app.ashcon.architect.level.LevelStore;
import app.ashcon.architect.level.command.LevelCommands;
import app.ashcon.architect.level.command.provider.CurrentLevelProvider;
import app.ashcon.architect.level.command.provider.NamedLevelProvider;
import app.ashcon.architect.level.LevelListener;
import app.ashcon.architect.user.UserListener;
import app.ashcon.architect.user.UserStore;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ArchitectModule.class})
interface ArchitectComponent {

    UserStore users();

    LevelStore levels();

    LevelCommands commands();

    NamedLevelProvider namedLevelProvider();

    CurrentLevelProvider currentLevelProvider();

    LevelListener levelListener();

    UserListener userListener();

}
