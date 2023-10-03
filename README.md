# ChatRoom-NIO
## ChatRoom是以三层缓冲为基础，使用NIO搭建的轻量级即时通讯框架。

# 功能
## 1、解决了消息粘包、接收消息不完全问题。
## 2、参照http2.0框架概念，实现文件的分片传输。
## 3、实现了传输文件以及普通消息的优先级问题。
## 4、实现传输文件时对文件传输的取消操作。

# 该框架在10000人同时进行数据发送时服务器的性能：
## cpu性能
![image](https://github.com/mxh13564/ChatRoom-NIO/assets/116016729/0c797b28-9294-45ea-8ea5-2d8f53aa6768)
## 内存消耗
![image](https://github.com/mxh13564/ChatRoom-NIO/assets/116016729/dc583348-19d9-4a36-b9e9-234ef11f98be)

# 使用方式
## 环境要求：在终端中输入java -version，与以下描述相同即可。
### ![image](https://github.com/mxh13564/ChatRoom-NIO/assets/116016729/1461ea66-fe92-41c6-a75f-d2f4476f6cdc)

## 1、局域网
### a、使用maven工具将项目打成jar包。
### b、在本地打开三个终端，或者在三台位于同一局域网的PC上分别打开一个终端（如果有条件）
### c、将server.jar通过java -jar server.jar的方式在终端部署。
### d、将其余两个client.jar通过相同方式部署在其余两个终端上。

### 效果如下：

#### 服务端部署
![image](https://github.com/mxh13564/ChatRoom-NIO/assets/116016729/7cef9137-2379-407a-a786-00f4974cf9ea)

#### 客户端部署
![image](https://github.com/mxh13564/ChatRoom-NIO/assets/116016729/8ed75c3b-badc-49a2-a168-9b7efcbe6ac5)

## 2、公网
### a、这里需要进行代码的改动，在局域网下可以使用UDP广播的方式进行数据传输（ip与端口），但在公网下需要指定ip地址与端口连接。
#### 在info信息中填入正确的ip地址，端口已经确定（如需修改在net.qiujuer.lesson.sample.foo.constants中修改），sn可以随便填写。
![image](https://github.com/mxh13564/ChatRoom-NIO/assets/116016729/a9ebecc3-6921-4e9c-bacc-f89c49e2a17c)
### b、与在局域网下部署服务端jar包相同。
![image](https://github.com/mxh13564/ChatRoom-NIO/assets/116016729/548e848f-ad9a-4c96-8f48-526f4ad55ef7)
### c、在任意网段的PC上通过终端部署client.jar包即可实现两台不同网段的PC之间的即时通讯。



