package app.ashcon.architect.model.mongo;

import app.ashcon.architect.model.Model;
import app.ashcon.architect.model.ModelStore;
import app.ashcon.architect.util.conversion.Conversion;
import app.ashcon.architect.util.conversion.ConversionImpl;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Sorts;
import com.mongodb.client.model.changestream.ChangeStreamDocument;
import com.mongodb.client.model.changestream.FullDocument;
import org.bson.Document;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MongoModelStore<M extends Model> implements ModelStore<M> {

    protected final MongoCollection<Document> collection;
    protected final Conversion<M> conversion;
    protected final Cache<String, M> cache;

    protected MongoModelStore(Class<M> clazz, MongoCollection<Document> collection) {
        this.collection = collection;
        this.conversion = new ConversionImpl<>(clazz);
        this.cache = CacheBuilder.newBuilder()
                                 .expireAfterWrite(15, TimeUnit.MINUTES)
                                 .initialCapacity(16)
                                 .softValues()
                                 .build();
        new Thread(() -> this.collection.watch().fullDocument(FullDocument.UPDATE_LOOKUP).forEach((Consumer<ChangeStreamDocument<Document>>) event -> {
            final Document document = event.getFullDocument();
            if(document != null) {
                final Object id = document.get("_id");
                if(id != null && cache.getIfPresent(id) != null) {
                    cache.put(id.toString(), conversion.toObject(document));
                }
            }
        })).start();
    }

    @Override
    public Optional<M> find(String id) {
        if(id == null) return Optional.empty();
        return requestDocument(collection.find(Filters.eq("_id", id)));
    }

    @Override
    public Optional<M> findCached(String id) {
        if(id == null) return Optional.empty();
        M cached = cache.getIfPresent(id);
        if(cached == null) {
            Optional<M> result = find(id);
            result.ifPresent(model -> cache.put(id, model));
            return result;
        }
        return Optional.of(cached);
    }

    @Override
    public M update(M model) {
        return toObject(
            collection.findOneAndReplace(
                Filters.eq("_id", model.getId()),
                conversion.toDocument(model),
                new FindOneAndReplaceOptions().upsert(true).returnDocument(ReturnDocument.AFTER)
            )
        ).findFirst()
         .orElseThrow(() -> new IllegalStateException("Unable to update model: " + model));
    }

    @Override
    public void delete(String id) {
        collection.deleteOne(Filters.eq("_id", id));
    }

    @Override
    public List<M> search(String name) {
        return request(
            collection.find(Filters.text(name))
                      .limit(5)
                      .projection(Projections.metaTextScore("score"))
                      .sort(Sorts.metaTextScore("score"))
        ).flatMap(this::toObject)
         .collect(Collectors.toList());
    }

    protected <R> Stream<R> request(MongoIterable<R> cursor) {
        Stream.Builder<R> builder = Stream.builder();
        try {
            for(R result : cursor) {
                builder.accept(result);
            }
        } catch(NoSuchElementException empty) {
            // Keep stream empty if nothing was found.
        } catch(Throwable error) {
            error.printStackTrace();
        }
        return builder.build();
    }

    protected Stream<M> requestDocuments(MongoIterable<Document> cursor) {
        return request(cursor).flatMap(this::toObject);
    }

    protected Optional<M> requestDocument(MongoIterable<Document> cursor) {
        return requestDocuments(cursor).findFirst();
    }

    protected Stream<M> toObject(Document document) {
        try {
            return Stream.of(conversion.toObject(document));
        } catch(Throwable err) {
            System.out.println("CAUGHT");
            err.printStackTrace();
            return Stream.empty();
        }
    }

}
