package app.ashcon.architect.model.mongo;

import app.ashcon.architect.model.Model;
import app.ashcon.architect.model.ModelStore;
import app.ashcon.architect.util.conversion.Conversion;
import com.mongodb.client.MongoCollection;
import com.mongodb.client.MongoIterable;
import com.mongodb.client.model.Filters;
import com.mongodb.client.model.FindOneAndReplaceOptions;
import com.mongodb.client.model.Projections;
import com.mongodb.client.model.ReturnDocument;
import com.mongodb.client.model.Sorts;
import org.bson.Document;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class MongoModelStore<M extends Model> implements ModelStore<M> {

    protected Conversion<M> conversion;
    protected MongoCollection<Document> collection;

    @Override
    public Optional<M> find(String id) {
        if(id == null) {
            return Optional.empty();
        }
        return requestDocument(collection.find(Filters.eq("_id", id)));
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
