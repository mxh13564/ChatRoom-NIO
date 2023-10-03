# ChatRoom-NIO
ChatRoom是以三层缓冲为基础，使用NIO搭建的即时通讯工具。

# 功能：
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

## 1、局域网
### 1、使用maven工具将项目打成jar包。
### 2、在本地打开三个终端，或者在三台位于同一局域网的PC上分别打开一个终端（如果有条件）
### 3、将server.jar通过java -jar server.jar的方式在终端部署。
### 4、将其余两个client.jar通过相同方式部署在其余两个终端上。

## 2、效果如下：

### 服务端部署
![image](https://github.com/mxh13564/ChatRoom-NIO/assets/116016729/7cef9137-2379-407a-a786-00f4974cf9ea)

### 客户端部署
![image](https://github.com/mxh13564/ChatRoom-NIO/assets/116016729/8ed75c3b-badc-49a2-a168-9b7efcbe6ac5)



