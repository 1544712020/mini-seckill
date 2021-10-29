package com.lwz.seckill.common;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.parser.Feature;
import com.alibaba.fastjson.serializer.SerializerFeature;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.SerializationException;

import java.nio.charset.Charset;

public class FastJsonSerializer implements RedisSerializer<Object> {

    public static final Charset UTF_8 = Charset.forName("UTF-8");

    /**
     * 序列化
     * @param obj
     * @return
     * @throws SerializationException
     */
    @Override
    public byte[] serialize(Object obj) throws SerializationException {
        if (obj == null) {
            return null;
        }
        // 将obj转为JSON字符串格式
        String json = JSON.toJSONString(obj, SerializerFeature.WriteClassName);
        // 使用给定的字符集将JSON字符串编码为字节序列，并将字节序列存储到新的字节数组进行返回
        return json.getBytes(UTF_8);
    }

    /**
     * 反序列化
     * @param bytes
     * @return
     * @throws SerializationException
     */
    @Override
    public Object deserialize(byte[] bytes) throws SerializationException {
        if (bytes == null || bytes.length <= 0) {
            return null;
        }
        // 使用序列化规则将对象进行反序列化
        String json = new String(bytes, UTF_8);
        return JSON.parseObject(json, Object.class, Feature.SupportAutoType);
    }
}
