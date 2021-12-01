# mini-seckill

#### 介绍
mini-seckill项目是一个商品秒杀的单体应用，项目使用了SpringBoot、MyBatis、Redis、RocketMQ、Nginx、guava、fastjson、tokenbucket等框架、中间件、组件，通过使用缓存中间件、消息中间件、令牌桶技术等方法最大限度提升单体应用的QPS，增强用户体验。

#### 软件架构

1.第一版

![输入图片说明](image.png)

2.第二版：加入Redis、guava缓存

![输入图片说明](src/image.png)

3.第三版：加入RocketMQ对扣减库存和下单进行异步解耦

The picture on the road...

4.第四版：加入TokenBucket对用户请求进行削峰限流

The picture on the road...