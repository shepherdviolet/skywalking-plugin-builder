# SkyWalking Plugin Builder | SkyWalking 插件模板

# 注意

* 插件引用的ByteBuddy库在编译器调整了包路径, 见`sample-plugin/build.gradle`中的`shadowJar#relocate`
* 示例的逻辑不严谨, 仅包含最简洁必要的代码, 实际使用中请认真编写, 做好异常处理

# 模块清单

* sample-plugin: 示例插件
* sample-web: 示例插件的侵入对象(被监控的WEB项目)

# 编译构建

* 编译: `gradlew shadowJar`
* 提取: `sample-plugin/build/libs/sample-plugin-1.0-all.jar`, 放到SkyWalking Agent的`plugins`目录下

# 被监控应用启动参数

* 被监控应用添加启动参数: -javaagent:`Your-Path`/apache-skywalking-apm-bin/agent/skywalking-agent.jar -DSW_AGENT_NAME=`Your-App-Name`
