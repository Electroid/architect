package app.ashcon.architect.model.mongo;

import app.ashcon.architect.user.User;
import app.ashcon.architect.user.UserStore;
import app.ashcon.architect.util.conversion.Conversion;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.model.Indexes;

import javax.inject.Inject;
import javax.inject.Singleton;
import java.util.UUID;

@Singleton
public class MongoUserStore extends MongoModelStore<User> implements UserStore {

    @Inject MongoUserStore(Conversion<User> conversion, MongoDatabase db) {
        this.conversion = conversion;
        this.collection = db.getCollection("users");
        this.collection.createIndex(Indexes.text("username"));
    }

    @Override
    public User login(String uuid, String username) {
        return find(uuid).orElseGet(() -> {
            User user = new User(UUID.fromString(uuid), username, null);
            collection.insertOne(conversion.toDocument(user));
            return user;
        });
    }

}
