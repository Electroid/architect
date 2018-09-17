package app.ashcon.architect.model.mongo;

import app.ashcon.architect.level.Level;
import app.ashcon.architect.level.LevelStore;
import app.ashcon.architect.util.Zip;
import com.mongodb.MongoGridFSException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.GridFSUploadStream;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Sorts;
import org.bson.BsonString;
import org.bukkit.Bukkit;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Singleton
public class MongoLevelStore extends MongoModelStore<Level> implements LevelStore {

    private final GridFSBucket bucket;

    @Inject MongoLevelStore(MongoDatabase db) {
        super(Level.class, db.getCollection("levels"));
        this.bucket = GridFSBuckets.create(db, "worlds");
        this.collection.createIndex(Indexes.text("name"));
        this.collection.createIndex(Indexes.ascending("default"));
    }

    @Override
    public void delete(String id) {
        super.delete(id);
        deleteFile(id);
    }

    private void deleteFile(String id) {
        try {
            bucket.delete(new BsonString(id));
        } catch(MongoGridFSException mgfse) {}
    }

    @Override
    public Level fallback() {
        final List<Level> fallbacks = requestDocuments(
            collection.find(Filters.eq("default", true))
        ).collect(Collectors.toList());
        if(fallbacks.size() == 1) {
            final Level fallback = fallbacks.get(0);
            final String defaultWorld = Bukkit.getWorlds().get(0).getName();
            if(fallback.getName().equals(defaultWorld)) {
                return fallback;
            }
        }
        fallbacks.forEach(level -> {
            level.setDefault(false);
            update(level);
        });
        return update(Level.createDefault());
    }

    @Override
    public List<Level> list(@Nullable String viewerId, int page, int perPage) {
        return requestDocuments(
            collection.find(
                Filters.or(
                    Filters.eq("visibility", "public"),
                    Filters.exists("players." + viewerId)
                )
            ).sort(Sorts.ascending("name"))
             .skip(page * perPage)
             .limit(perPage)
        ).collect(Collectors.toList());
    }

    @Override
    public boolean upload(String id, File source) {
        if(source.isDirectory() && source.listFiles().length > 0) {
            deleteFile(id);
            final GridFSUploadStream upload = bucket.openUploadStream(new BsonString(id), id);
            try {
                ZipOutputStream zip = new ZipOutputStream(upload);
                Zip.compress(source, zip);
                zip.close();
            } catch(IOException ioe) {
                ioe.printStackTrace();
                return false;
            }
            upload.close();
            return true;
        }
        return false;
    }

    @Override
    public boolean download(String id, File destination) {
        try {
            final GridFSDownloadStream download = bucket.openDownloadStream(id);
            destination.mkdirs();
            final ZipInputStream zip = new ZipInputStream(download);
            Zip.decompress(destination, zip);
            zip.close();
            download.close();
        } catch(MongoGridFSException mgfse) {
            return true; // File was not found: 404
        } catch(IOException ioe) {
            ioe.printStackTrace();
            return false;
        }
        return true;
    }

}
