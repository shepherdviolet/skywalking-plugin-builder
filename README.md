# Skywalking Plugin Builder

* 插件引用的ByteBuddy的包路径有调整, 见`sample-plugin/build.gradle`中的`shadowJar#relocate`
* 必须用`gradlew shadowJar`打包, 提取`sample-plugin/build/libs/sample-plugin-1.0-all.jar`放到Agent的`plugins`目录下
