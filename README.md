![](https://socialify.git.ci/JhihJian/Bili-Data-Process/image?description=1&font=Source%20Code%20Pro&language=1&owner=1&pattern=Floating%20Cogs&stargazers=1&theme=Light)
### 安装数据库
安装
```
mkdir -p /opt/mysql/datadir
docker run --name bili-mysql --restart always -v /opt/mysql/datadir:/var/lib/mysql -p 3306:3306 -p 33060:33060 -e MYSQL_ROOT_PASSWORD=my-secret-pw -d mysql:8.0
```
此处some-mysql是您要分配给容器的名称，my-secret-pw是要为MySQL根用户设置的密码
其他指令
```
- 以下命令行将在mysql容器内为您提供一个bash shell
docker exec -it bili-mysql bash
 - Docker的容器日志获取
docker logs some-mysql
- 创建数据库转储
docker exec some-mysql sh -c 'exec mysqldump --all-databases -uroot -p"$MYSQL_ROOT_PASSWORD"' > /some/path/on/your/host/all-databases.sql
- 从转储文件还原数据
docker exec -i some-mysql sh -c 'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD"' < /some/path/on/your/host/all-databases.sql
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
# 字幕表
CREATE TABLE bili_text (av BIGINT,play_time BIGINT, text TEXT);
# 弹幕表
CREATE TABLE `bili_chat` (
    av BIGINT,
    play_time BIGINT, 
    text TEXT,
    send_time TIMESTAMP,
    user_name varchar(64)
);

CREATE TABLE `bili_comment` (
   `av` BIGINT,
  `uname` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `sign` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `sex` varchar(16) COLLATE utf8_bin DEFAULT NULL,
  `mid` int(64) ,
  `current_level` int(16) DEFAULT NULL,
  `ctime` varchar(32) COLLATE utf8_bin ,
  `message` varchar(12222) CHARACTER SET utf8mb4 COLLATE utf8mb4_bin DEFAULT NULL,
  `plat` int(8) DEFAULT NULL,
  `likehah` varchar(32) COLLATE utf8_bin DEFAULT NULL,
  `rcount` int(32) DEFAULT NULL,
   PRIMARY KEY (mid, ctime)
) ENGINE=InnoDB DEFAULT CHARSET=utf8 COLLATE=utf8_bin;


```"# Bili-Subtitle-Process" 
