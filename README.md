JRESPLUS
===

jresplus是恒生电子研发中心自2013年起发布的一个基于java、spring技术的分层开发框架。它包含UI、MVC、CEP、DAO等模块组件和配套的开发规范，使用这些模块组件可以快速构建web、分布式、关系数据库等类型的企业级应用，目前已广泛应用在金融领域的产品中。<br>

### jresplus 结构
jresplus采用maven进行工程管理。<br/>
jresplus目前包含但不局限与以下模块组件：<br/>
<ul>
<li>jresplus-common</li>
<li>jresplus-mvc</li>
<li>jresplus-ui</li>
<li>jresplus-cep</li>
<li>jresplus-dao</li>
</ul>
jresplus  模块说明
---

### jresplus-common
jresplus的基础公共模块，内部包含工厂、异常、参数、数组处理、字符串处理、对象处理、html处理、io处理等工具类和基础类，供其他模块使用。<br/>
该模块可以做为工具包单独使用;

### jresplus-mvc
mvc模块，基于springmvc之上，提供了分布式集群环境下所需的回话同步、资源代理、页面静态化、Bigpipe、沙箱特性等web端技术解决方案，同时提供了COC的职责分离式视图开发方案可以有效提高前端开发的效率和变更应对效率。
该模块可以做为mvc单独使用（但需要引入jresplus-common）;

### jresplus-cep
消息中间件模块，以通用事件处理平台（CEP）为核心，为构建分布式集群应用提供分层部署、路由分发、安全连接和面向插件式的可扩展通道、可扩展业务处理模块以及面向接口面向对象的远程服务开发（支持本地穿透）等特性，同时jresplus-cep作为恒生企业服务总线（ESB）的重要组成部分，可以和恒生的C/C++系统无缝对接，基于jresplus-cep可以快速构建分布式集群部署环境.
该模块可以做为消息中间件单独使用（但需要引入jresplus-common）;

### jresplus-ui
UI组件模块，依赖jresplus-mvc模块，在此之上提供UI开发所需的字典、参数、缓存、校验等特性支持，并提供一套采用服务端输出技术的基础UI控件（表单、列表、窗口、多标签页、提示、布局等），基于jresplus-ui可以快速构建前端展示界面及扩展出自己的UI组件。

### jresplus-dao
数据访问层模块，目前主要面向关系型数据库，提供了原子操作（CRUD）、自定义主键策略、乐观锁、异常等基础层，并在此基础上分别基于市场上流行成熟的持久层方案进行集成实现（mybatis\hibernate），对上层提供统一的持久层接口，持久层的实现可以结合适用场景选择jresplus-dao的具体实现组件，基于jresplus-dao模块组件结合jresplus-dao的开发规范可以快速构建关系型数据库持久层方案。
jresplus使用Java的开发技术

---
使用到的语言和技术：java,spring,springmvc,velocity,HTML,CSS,Javascript,JQueryapache-common,slf4j,mybatis,Hibernate;<br/>
开发工具：Eclipse(或IntelliJ IDEA),Maven和Git(或SVN);<br/>

参与我们
---
如果你的电脑里还可以放下一部短片、或者几首mp3，那么不妨[Fork一下jresplus](https://github.com/hundsun/jresplus/fork)吧，也许它能帮助你换台更好的电脑...
快速开始
---
* 访问[wiki](https://github.com/hundsun/jresplus/wiki)了解更多;
* 下载[jresplus-mvc quick-start project](https://github.com/hundsun/jresplus/tree/master/quick-start/quick_start_mvc.zip)体验jresplus-mvc特性；


