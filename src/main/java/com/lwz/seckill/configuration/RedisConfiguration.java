package com.lwz.seckill.configuration;
import com.lwz.seckill.common.FastJsonSerializer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * redis配置类
 */
@Configuration
public class RedisConfiguration {

    /**
     * redisTemplate配置方法
     * @param factory
     * @return template
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        // 创建redisTemplate
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 为template设置连接工厂
        template.setConnectionFactory(factory);

        /*
        * 将数据对象序列化的目的：让数据对象可以跨平台存储以及网络传输，而跨平台存储和网络传输需要IO，而IO需要使用字节数组
        * 设置key序列化
        */
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());

        /*
        * 设置value序列化
        * 创建FastJsonSerializer用于value的序列化
        */
        FastJsonSerializer fastJsonSerializer = new FastJsonSerializer();
        template.setValueSerializer(fastJsonSerializer);
        template.setHashValueSerializer(fastJsonSerializer);

        // 返回template之前需要对其初始化设置
        template.afterPropertiesSet();

        return template;
    }

}
