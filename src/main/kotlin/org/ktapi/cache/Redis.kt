package org.ktapi.cache

import org.apache.commons.pool2.impl.GenericObjectPoolConfig
import org.ktapi.config
import org.ktapi.configOrNull
import org.ktapi.toJson
import redis.clients.jedis.Jedis
import redis.clients.jedis.JedisPool
import redis.clients.jedis.Protocol
import redis.clients.jedis.params.SetParams

/**
 * Redis Cache implementation.
 *
 * Config parameters: cache.redis.host, cache.redis.port, cache.redis.password
 */
object Redis : Cache {
    private val jedisPool = JedisPool(
        GenericObjectPoolConfig(),
        config("cache.redis.host"),
        config("cache.redis.port"),
        Protocol.DEFAULT_TIMEOUT,
        configOrNull("cache.redis.password")
    )

    override val connected
        get() = try {
            get("J")
            true
        } catch (e: Throwable) {
            e.printStackTrace()
            false
        }

    override fun add(key: String, value: Any, ttlSecond: Long?) = call { redis ->
        when (ttlSecond) {
            null -> redis.set(key, toString(value), SetParams().nx())
            else -> redis.set(key, toString(value), SetParams().nx().ex(ttlSecond))
        }
        Unit
    }

    private fun toString(value: Any) = when (value) {
        is String -> value
        else -> value.toJson()
    }

    override fun delete(key: String) = call {
        it.del(key)
        Unit
    }

    override fun get(key: String): String? = call { it.get(key) }

    override fun set(key: String, value: Any, ttlSecond: Long?) = call { redis ->
        when (ttlSecond) {
            null -> redis.set(key, toString(value))
            else -> redis.psetex(key, ttlSecond * 1000, toString(value))
        }
        Unit
    }

    override fun update(key: String, value: Any, ttlSecond: Long?) = call { redis ->
        when (ttlSecond) {
            null -> redis.set(key, toString(value), SetParams().xx())
            else -> redis.set(key, toString(value), SetParams().xx().ex(ttlSecond))
        }
        Unit
    }

    private fun <T> call(block: (jedis: Jedis) -> T) = jedisPool.resource.use(block)
}
