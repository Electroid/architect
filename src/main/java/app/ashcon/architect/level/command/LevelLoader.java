package app.ashcon.architect.level.command;

import app.ashcon.architect.level.Level;
import app.ashcon.architect.level.LevelStore;
import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.google.common.cache.RemovalCause;
import com.google.common.collect.Iterables;
import org.bukkit.command.CommandSender;

import javax.inject.Inject;
import java.lang.ref.WeakReference;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class LevelLoader {

    final LevelStore levelStore;
    final Cache<Entry, Void> loadingCache;

    @Inject LevelLoader(LevelStore levelStore) {
        this.levelStore = levelStore;
        this.loadingCache = CacheBuilder.newBuilder().expireAfterWrite(30, TimeUnit.SECONDS).removalListener(notify -> {

        }).build();
    }

    static class Entry implements Iterable<CommandSender> {

        final String levelId;
        final String levelName;
        final List<WeakReference<CommandSender>> senderList;
        final Thread loadThread;

        Entry(Level level) {
            this.levelId = level.getId();
            this.levelName = level.getName();
            this.senderList = new LinkedList<>();
            this.loadThread = new Thread(level::loadWorld);
            this.loadThread.start();
        }

        public void watch(CommandSender sender) {
            senderList.add(new WeakReference<>(sender));
        }

        public void reply() {

        }

        @Override
        public Iterator<CommandSender> iterator() {
            return senderList.stream()
                             .flatMap(ref -> ref.isEnqueued() ? Stream.empty() : Stream.of(ref.get()))
                             .collect(Collectors.toList())
                             .iterator();
        }

    }

}
