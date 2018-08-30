package app.ashcon.architect.util.conversion;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.bson.Document;

public class ConversionImpl<T> implements Conversion<T> {

    final Gson gson;
    final Class<T> clazz;

    public ConversionImpl(Class<T> clazz) {
        this.gson = new GsonBuilder().create();
        this.clazz = clazz;
    }

    @Override
    public Document toDocument(T object) {
        return Document.parse(gson.toJson(object));
    }

    @Override
    public T toObject(Document document) {
        System.out.println(document.toJson());
        return gson.fromJson(document.toJson(), clazz);
    }

}
