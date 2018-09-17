package app.ashcon.architect;

import app.ashcon.architect.level.LevelListener;
import app.ashcon.architect.level.command.LevelCommands;
import app.ashcon.architect.level.command.provider.LevelCurrentProvider;
import app.ashcon.architect.level.command.provider.LevelNamedProvider;
import app.ashcon.architect.model.mongo.MongoModule;
import app.ashcon.architect.user.UserListener;
import dagger.Component;

import javax.inject.Singleton;

@Singleton
@Component(modules = {ArchitectModule.class, MongoModule.class})
interface ArchitectComponent {

    LevelListener levelListener();

    LevelCommands levelCommands();

    LevelNamedProvider levelNamedProvider();

    LevelCurrentProvider levelCurrentProvider();

    UserListener userListener();

}
