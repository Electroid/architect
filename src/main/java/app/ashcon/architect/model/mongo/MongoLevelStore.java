package app.ashcon.architect.model.mongo;

import app.ashcon.architect.level.Level;
import app.ashcon.architect.level.LevelStore;
import app.ashcon.architect.util.Zip;
import app.ashcon.architect.util.conversion.Conversion;
import com.mongodb.MongoGridFSException;
import com.mongodb.client.MongoDatabase;
import com.mongodb.client.gridfs.GridFSBucket;
import com.mongodb.client.gridfs.GridFSBuckets;
import com.mongodb.client.gridfs.GridFSDownloadStream;
import com.mongodb.client.gridfs.GridFSUploadStream;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.Indexes;
import com.mongodb.client.model.Sorts;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.inject.Singleton;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.zip.ZipInputStream;
import java.util.zip.ZipOutputStream;

@Singleton
public class MongoLevelStore extends MongoModelStore<Level> implements LevelStore {

    private final GridFSBucket bucket;
    private final Level lobby;

    @Inject MongoLevelStore(Conversion<Level> conversion, MongoDatabase db, Level lobby) {
        super();
        this.conversion = conversion;
        this.bucket = GridFSBuckets.create(db, "worlds");
        this.collection = db.getCollection("levels");
        this.collection.createIndex(Indexes.text("name"));
        this.lobby = update(lobby);
    }

    @Override
    public Optional<Level> find(String id) {
        if("world".equalsIgnoreCase(id)) {
            return Optional.of(lobby);
        }
        return super.find(id);
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
            GridFSUploadStream upload = bucket.openUploadStream(id);
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
            GridFSDownloadStream download = bucket.openDownloadStream(id);
            destination.mkdirs();
            ZipInputStream zip = new ZipInputStream(download);
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
