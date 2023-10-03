# ChatRoom-NIO
ChatRoom是以三层缓冲为基础，使用NIO搭建的即时通讯工具。

功能：
1、解决了消息粘包、接收消息不完全问题。
2、参照http2.0框架概念，实现文件的分片传输。
3、实现了传输文件以及普通消息的优先级问题。
4、实现传输文件时对文件传输的取消操作。

以下为该框架在10000人同时进行数据发送时服务器的性能：
cpu性能
![image](https://github.com/mxh13564/ChatRoom-NIO/assets/116016729/0c797b28-9294-45ea-8ea5-2d8f53aa6768)
内存消耗
![image](https://github.com/mxh13564/ChatRoom-NIO/assets/116016729/18f2945a-497f-4bb6-97fa-1f38a134c793)



