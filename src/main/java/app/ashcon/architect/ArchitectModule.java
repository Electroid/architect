package app.ashcon.architect;

import app.ashcon.architect.level.Level;
import app.ashcon.architect.level.LevelListener;
import app.ashcon.architect.level.LevelLoader;
import app.ashcon.architect.level.LevelStore;
import app.ashcon.architect.level.command.LevelCommands;
import app.ashcon.architect.level.command.provider.NamedLevelProvider;
import app.ashcon.architect.level.command.provider.CurrentLevelProvider;
import app.ashcon.architect.model.mongo.MongoLevelStore;
import app.ashcon.architect.model.mongo.MongoUserStore;
import app.ashcon.architect.user.User;
import app.ashcon.architect.user.UserListener;
import app.ashcon.architect.user.UserStore;
import app.ashcon.architect.util.conversion.Conversion;
import app.ashcon.architect.util.conversion.ConversionImpl;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

import java.util.logging.Logger;

@Module
public abstract class ArchitectModule {

    @Binds
    abstract UserStore provideUserStore(MongoUserStore userStore);

    @Binds
    abstract LevelStore provideLevelStore(MongoLevelStore levelStore);

    abstract void provideLevelCommands(LevelCommands levelCommands);

    abstract void provideLevelListener(LevelListener levelListener);

    abstract void provideUserListener(UserListener userListener);

    abstract void provideLevelLoader(LevelLoader levelLoader);

    abstract void provideNamedLevelProvider(NamedLevelProvider namedLevelProvider);

    abstract void provideCurrentLevelProvider(CurrentLevelProvider currentLevelProvider);

    @Provides
    static ConnectionString provideMongoClientUri() {
        return new ConnectionString(getSetting("mongo.uri", "mongodb://localhost:27017/?replicaSet=rs0"));
    }

    @Provides
    static MongoClient provideMongoClient(ConnectionString uri) {
        return MongoClients.create(uri);
    }

    @Provides
    static MongoDatabase provideMongoDatabase(MongoClient client) {
        return client.getDatabase(getAppName());
    }

    @Provides
    static Conversion<Level> provideBsonLevel() {
        return new ConversionImpl<>(Level.class);
    }

    @Provides
    static Conversion<User> provideBsonUser() {
        return new ConversionImpl<>(User.class);
    }

    static {
        Logger.getLogger("org.mongodb").setLevel(java.util.logging.Level.WARNING);
    }

    static String getAppName() {
        return "architect";
    }

    static String getSetting(String key, String def) {
        String env = System.getenv(key.replaceAll("\\.", "_").toUpperCase());
        if(env == null) {
            env = System.getProperty(key.toLowerCase(), def);
        }
        return env;
    }

}
