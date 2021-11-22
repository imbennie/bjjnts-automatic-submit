package im.bennie.util;

import cn.hutool.core.lang.Assert;
import im.bennie.Config;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.codec.StringCodec;
import io.lettuce.core.support.AsyncConnectionPoolSupport;
import io.lettuce.core.support.AsyncPool;
import io.lettuce.core.support.BoundedPoolConfig;
import lombok.extern.slf4j.Slf4j;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.StringJoiner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionStage;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;

import static im.bennie.consts.RedisConst.KEY_DELIMITER;
import static im.bennie.consts.RedisConst.REDIS_URL;

@Slf4j
public class RedisUtil {

    private static final AsyncPool<StatefulRedisConnection<String, String>> pool;
    private static final RedisClient                                        client = RedisClient.create();

    private static final Config config = Config.getInstance();

    static {
        pool = AsyncConnectionPoolSupport.createBoundedObjectPool(
                () -> client.connectAsync(StringCodec.UTF8, RedisURI.create(REDIS_URL)),
                BoundedPoolConfig.create()
        );
    }


    public static String getKey(Object... param) {
        Assert.notEmpty(param);
        StringJoiner sj = new StringJoiner(KEY_DELIMITER).add(config.getKeyPrefix());
        for (Object p : param) {
            sj.add(String.valueOf(p));
        }
        return sj.toString();
    }

    public static CompletableFuture<StatefulRedisConnection<String, String>> acquire() {
        return pool.acquire();
    }

    public static Object sendCommand(Function<? super StatefulRedisConnection, ? extends CompletionStage<String>> fn) throws ExecutionException, InterruptedException {
        return acquire()
                .thenCompose(fn)
                .toCompletableFuture()
                .get();
    }

    public static Object get(Object key) throws Exception {
        return sendCommand(
                connection -> connection.async().get(key)
                        .whenComplete((s, throwable) -> pool.release(connection)));
    }

    public static Object hGet(Object key, Object hashKey) throws Exception {
        return sendCommand(
                connection -> connection.async().hget(key, hashKey)
                        .whenComplete((s, throwable) -> pool.release(connection)));
    }

    public static void set(Object key, Object value) throws Exception {
        sendCommand(
                connection -> connection.async().set(key, value)
                        .whenComplete((s, throwable) -> pool.release(connection)));
    }

    public static void hSet(Object key, Object field, Object value) throws Exception {
        sendCommand(
                connection -> connection.async().hset(key, field, value)
                        .whenComplete((s, throwable) -> pool.release(connection)));
    }

    public static void hSet(Object key, Map<String, Object> map) throws Exception {
        sendCommand(
                connection -> connection.async().hset(key, map)
                        .whenComplete((s, throwable) -> pool.release(connection)));
    }

    public static LinkedHashMap<String, Object> entries(String key) throws Exception {
        return (LinkedHashMap<String, Object>) sendCommand(c -> c.async().hgetall(key)
                .whenComplete((s, t) -> pool.release(c))
        );
    }

    public static boolean hasKeys(String... keys) throws Exception {
        Long nums = (Long) sendCommand(c -> c.async().exists(keys)
                .whenComplete((s, t) -> pool.release(c))
        );
        return nums == keys.length;
    }

    public static void del(String... keys) throws Exception {
        sendCommand(c -> c.async().del(keys)
                .whenComplete((s, t) -> pool.release(c)));
    }


}
