﻿### webmagic爬虫项目说明


------------

- 基于 Spring Boot + Spring Data JPA （mongodb）+ JdbcTemplate（orcale）开发
- 数据库连接池采用阿里巴巴的 Druid 连接池
- 采用模板方法模式构建将业务逻辑层

<br>

------------

##### Dao层

mongodb Dao层通过继承Spring Data JPA提供的JpaRepository实现
orcale       Dao层通过继承Spring 提供的JdbcTemplate实现
 与燕云不同的是，项目十个表对应一个实体，表名通过传参实现业务需求
<br>


##### 业务逻辑层

webmagic爬虫项目的业务逻辑中的核心关键点如下：

-全量爬虫与增量爬虫的区别
- 校验：增量爬虫会校验前三页判定数据库是否有数据，有则停止爬虫，没有则启动爬虫。
            全量爬虫只是在添加请求之前校验数据是否存在，不计数。
- 异常处理：全量爬虫异常停止以后记录出错页码，下次获取异常页码作为启动初始页码。
                   增量爬虫每次启动都初始化页码，保证爬虫从首页开始获取数据。

<br>

新增附件字段，存储附件的原始链接、标题、以及内容的MD5


