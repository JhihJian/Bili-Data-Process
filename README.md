
### 安装数据库
安装
```
mkdir -p /opt/org.jhihjian.bili.mysql/datadir
docker run --name bili-org.jhihjian.bili.mysql --restart on-failure -v /opt/org.jhihjian.bili.mysql/datadir:/var/lib/org.jhihjian.bili.mysql -p 3306:3306 -p 33060:33060 -e MYSQL_ROOT_PASSWORD=my-secret-pw -d org.jhihjian.bili.mysql:8.0
```
此处some-mysql是您要分配给容器的名称，my-secret-pw是要为MySQL根用户设置的密码
其他指令
```
- 以下命令行将在mysql容器内为您提供一个bash shell
docker exec -it bili-org.jhihjian.bili.mysql bash
 - Docker的容器日志获取
docker logs some-org.jhihjian.bili.mysql
- 创建数据库转储
docker exec some-org.jhihjian.bili.mysql sh -c 'exec mysqldump --all-databases -uroot -p"$MYSQL_ROOT_PASSWORD"' > /some/path/on/your/host/all-databases.sql
- 从转储文件还原数据
docker exec -i some-org.jhihjian.bili.mysql sh -c 'exec org.jhihjian.bili.mysql -uroot -p"$MYSQL_ROOT_PASSWORD"' < /some/path/on/your/host/all-databases.sql
- 不配置密码，获得随机生成的密码
docker logs mysql1 2>&1 | grep GENERATED
- 修改密码
ALTER USER 'root'@'localhost' IDENTIFIED BY 'password';
```

create sql

```
/* create database */

CREATE DATABASE `bili` DEFAULT CHARACTER SET utf8 COLLATE utf8_general_ci;

CREATE USER biliu IDENTIFIED BY '123456';

grant all privileges on bili.* to 'biliu'@'%';

FLUSH PRIVILEGES;

use bili

/* create table */

CREATE TABLE bili_total_text (av BIGINT, total_text TEXT);

```"# Bili-Subtitle-Process" 
