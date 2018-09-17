package app.ashcon.architect.model.mongo;

import app.ashcon.architect.level.LevelStore;
import app.ashcon.architect.user.UserStore;
import com.mongodb.ConnectionString;
import com.mongodb.client.MongoClient;
import com.mongodb.client.MongoClients;
import com.mongodb.client.MongoDatabase;
import dagger.Binds;
import dagger.Module;
import dagger.Provides;

import java.util.logging.Logger;

@Module
public interface MongoModule {

    @Binds
    LevelStore bindLevelStore(MongoLevelStore levelStore);

    @Binds
    UserStore bindUserStore(MongoUserStore userStore);

    @Provides
    static ConnectionString provideConnectionString() {
        Logger.getLogger("org.mongodb").setLevel(java.util.logging.Level.WARNING);
        return new ConnectionString(fetchSetting("mongo.uri", "mongodb://localhost:27017/?replicaSet=rs0"));
    }

    @Provides
    static MongoClient provideMongoClient(ConnectionString uri) {
        return MongoClients.create(uri);
    }

    @Provides
    static MongoDatabase provideMongoDatabase(MongoClient client) {
        return client.getDatabase("architect");
    }

    static String fetchSetting(String key, String def) {
        String env = System.getenv(key.replaceAll("\\.", "_").toUpperCase());
        if(env == null) {
            env = System.getProperty(key.toLowerCase(), def);
        }
        return env;
    }

}
