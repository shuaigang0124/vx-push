## vx-push（微信测试号推送）
* 项目优点：配置完成后，不用修改代码，直接使用数据库即可配置需要推送的用户以及推送的消息

###项目运行条件：
#### 第一步：注册微信公众号测试号
1.点击如下链接，在微信公众平台注册一个微信公众测试号:

[微信公众平台测试号地址](https://mp.weixin.qq.com/debug/cgi-bin/sandbox?t=sandbox/login)

2、打开并注册一个微信公众号测试号，扫码登录即可。

3、扫码登录成功后，就可以生成微信公众号测试号的appID和appsecret这两串信息，要记住位置，之后会用到！！！

<img src="https://shuaigang.top/gsg/static-resource/formal/resource/appid.webp" width="375"/>

#### 第二步：扫描测试号二维码

1、向下滑动，找到测试号二维码，使用微信扫描测试号二维码并关注。

2、用户列表会自动显示用户信息，生成一个特殊的微信号。

3、这里需要记住的是对应的微信号，后续会用到。

<img src="https://shuaigang.top/gsg/static-resource/formal/resource/user.webp" width="375"/>

#### 第三步：新增测试模板

1、向下滑动，找到消息模板接口。

2、点击 新增测试模板（模板内容注意换行）

模板标题一：恋爱小助手 （自定义也行）

模板内容一：

    日期：{{first.DATA}} 
    {{typeDes.DATA}} 
    城市：{{city.DATA}} 
    天气：{{temperature.DATA}} 
    农历：{{lunarCalendar.DATA}} 
    属相：{{chineseZodiac.DATA}} 
    节气：{{solarTerms.DATA}} 
    宜：{{suit.DATA}} 
    忌：{{avoid.DATA}} 
    今天是我们在一起的: {{togetherDate.DATA}}
    距离你的生日：{{birthDate.DATA}}
    {{message.DATA}}


模板标题二：每日喝水提醒小助手 （自定义也行）

模板内容二：

    {{first.DATA}} 
    小助手已运行: {{onlineDate.DATA}} 
    {{remind.DATA}}

模板标题三：每日提醒睡觉小助手 （自定义也行）

模板内容三：

    当前时间：{{time.DATA}}
    小贴士：{{sleep.DATA}}
    {{remind.DATA}}

模板标题四：自定义消息 （自定义也行）

模板内容四：

    {{message.DATA}}

说明：这里面的每一个{{xxx.DATA}}都对应相应的数据，需要就保留，不需要就删掉，不想理解这些东西就直接放在这里不用删除也可以的。

3、添加完，如图所示：

<img src="https://shuaigang.top/gsg/static-resource/formal/resource/moban.webp" width="375"/>

#### 第四步：下载项目后的配置

1.等待maven依赖下载完成

2.在等待maven依赖构建完成的同时去配置数据库

* 用户表（先执行创建语句，完成后将___步骤第二步第三点___中的用户数据录入）


    CREATE TABLE `user` (
    `id` varchar(255) NOT NULL COMMENT '微信id标识（自定义：例如vx001）',
    `name` varchar(100) DEFAULT NULL COMMENT '微信昵称',
    `wx_number` varchar(255) NOT NULL COMMENT '微信号',
    `city_num` varchar(100) NOT NULL DEFAULT '101270101' COMMENT '城市名称',
    `birthday` varchar(50) NOT NULL DEFAULT '01-24' COMMENT '生日',
    `together_date` date DEFAULT current_timestamp() COMMENT '在一起的时间',
    `message` longtext NOT NULL DEFAULT 'du' COMMENT '留言（为chp，du则每天推送接口获取的数据，反之为当前留言）',
    `is_enabled` int(1) NOT NULL DEFAULT 1 COMMENT '是否启用,0-禁用,1-启用,默认值1',
    `gmt_create` datetime NOT NULL DEFAULT current_timestamp() COMMENT '创建时间',
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信用户表'
  
    插入示例：
    注意修改插入语句中对应的___vx001、微信昵称、微信号.....
    insert into user(id, name, wx_number) values 
    ('vx0001', '微信昵称' , '微信号'),
    ('vx0002', '微信昵称2' , '微信号2')

* 模板表（先执行创建语句，完成后将___步骤第三步第三点___中的模板id数据录入）


    CREATE TABLE `template` (
    `id` varchar(25) NOT NULL COMMENT '模板id',
    `template` varchar(255) NOT NULL COMMENT '具体字段',
    `gmt_create` datetime DEFAULT current_timestamp() COMMENT '创建时间',
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信模板ID表' 

    插入示例：
    注意修改插入语句中对应的___temp001、模板一......
    insert into template (id, template) values 
    ('temp0001', '模板一'),
    ('temp0002', '模板二'),
    ('temp0003', '模板三'),
    ('temp0004', '模板四')

* 用户与模板关联表（需要为用户推送哪个模板就绑定那个模板id）


    CREATE TABLE `user_temp` (
    `user_id` varchar(25) NOT NULL COMMENT '用户id',
    `temp_id` varchar(25) NOT NULL COMMENT '模板id',
    UNIQUE KEY `user_id` (`user_id`,`temp_id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='用户模板关联表'
  
    插入示例：
    insert into user_temp (user_id , temp_id) values 
    ('vx0001', 'temp0001'),
    ('vx0001', 'temp0002'),
    ('vx0001', 'temp0003'),
    ('vx001', 'temp0004')




* 消息表（相应执行sql即可）


    CREATE TABLE `message` (
    `id` varchar(50) NOT NULL COMMENT '消息id',
    `content` longtext DEFAULT NULL COMMENT '消息内容',
    `gmt_create` datetime DEFAULT current_timestamp() COMMENT '创建时间',
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='微信消息表'

    insert into message (id , message)
    values ('msg0001',
    '我是提醒喝水小助手，看到这条消息就和我一起喝一杯水吧。上班快乐，一小时后我将继续提醒你喝水，和我一起成为一天八杯水的人吧！'),
    ('msg0002',
    '我是提醒喝水小助手，看到这条消息就和我一起喝一杯水吧。摸鱼快乐，一小时后我将继续提醒你喝水，和我一起成为一天八杯水的人吧！'),
    ('msg0003',
    '我是提醒喝水小助手，看到这条消息就和我一起喝一杯水吧。该吃饭了！午休过后我将继续提醒你喝水，和我一起成为一天八杯水的人吧！'),
    ('msg0004',
    '我是提醒喝水小助手，看到这条消息就和我一起喝一杯水吧。睡得咋样？一小时后我将继续提醒你喝水，和我一起成为一天八杯水的人吧！') ,
    ('msg0005',
    '我是提醒喝水小助手，看到这条消息就和我一起喝一杯水吧。天干物燥，一小时后我将继续提醒你喝水，和我一起成为一天八杯水的人吧！'),
    ('msg0006',
    '我是提醒喝水小助手，看到这条消息就和我一起喝一杯水吧。在认真工作吗？，一小时后我将继续提醒你喝水，和我一起成为一天八杯水的人吧！'),
    ('msg0007',
    '我是提醒喝水小助手，看到这条消息就和我一起喝一杯水吧。赶紧收拾准备下班！下班回家后我将继续提醒你喝水，和我一起成为一天八杯水的人吧！'),
    ('msg0008',
    '我是提醒喝水小助手，看到这条消息就和我一起喝最后一杯水吧。下班快乐，明天我将继续提醒你喝水，让我们恭喜今天的自己成为一天八杯水的人吧！'),
    ('msg0009',
    '白天挣钱，顺其自然，夜晚入睡，精力充沛，放松心情，圆满美梦，自由坦然，快乐无限，祝你晚安，轻松入眠，愿你开心每一天!')


* 彩虹皮文案表，毒鸡汤文案表， 睡觉提醒文案表（相应执行sql即可）


    CREATE TABLE `chp` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `text` varchar(255) NOT NULL COMMENT '内容',
    `gmt_create` datetime NOT NULL DEFAULT current_timestamp() COMMENT '创建时间',
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='彩虹屁文案表'

    CREATE TABLE `du` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `text` varchar(255) NOT NULL COMMENT '内容',
    `gmt_create` datetime NOT NULL DEFAULT current_timestamp() COMMENT '创建时间',
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='毒鸡汤文案表'

    CREATE TABLE `sleep` (
    `id` int(11) NOT NULL AUTO_INCREMENT COMMENT '主键',
    `text` varchar(255) NOT NULL COMMENT '内容',
    `gmt_create` datetime NOT NULL DEFAULT current_timestamp() COMMENT '创建时间',
    PRIMARY KEY (`id`)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='睡觉提醒文案表'

3.打开application.yml配置文件

    修改数据库链接地址以及账号密码
<img src="https://shuaigang.top/gsg/static-resource/formal/resource/yml-mysql.webp" width="375"/>

    修改为之前的appid和appSecret
<img src="https://shuaigang.top/gsg/static-resource/formal/resource/yml-appid.webp" width="375"/>

    点击如下链接获取并填入万年历接口对应的appid和appSecret
[去申请万年历接口](https://www.mxnzp.com/doc/detail?id=1)    

<img src="https://shuaigang.top/gsg/static-resource/formal/resource/yml-appid.webp" width="375"/>

    点击如下链接获取并填入天气接口对应的appid和appSecret
[去申请天气接口](https://tianqiapi.com/user/login)

<img src="https://shuaigang.top/gsg/static-resource/formal/resource/yml-appid.webp" width="375"/>

4.去WxController文件中修改cron的值变更推送时间（非必须）

    具体的cron表达式自己百度
    例如早上8：00点推送
    cron = "0 0 8 ? * *"
    例如早上9：30点推送
    cron = "0 30 9 ? * *"

#### 第五步：打开WxApplication文件

启动项目即可

部署：maven-install将打包后的jar包上传至服务器，输入一下命令直接运行（非必要）

    nohup java -jar wx-shuaigang.jar  >/dev/null 2>&1 &

#### 第六步： 测试

直接浏览器输入接口地址调用相关接口后，查看微信是否推送

    接口例如：localhost:5211/wx/getMorning


#### 第七步：推送成功示例：

<img src="https://shuaigang.top/gsg/static-resource/formal/resource/img.webp" width="375"/>

<img src="https://shuaigang.top/gsg/static-resource/formal/resource/img1.webp" width="375"/>

<img src="https://shuaigang.top/gsg/static-resource/formal/resource/img2.webp" width="375"/>
