# 京训钉自动挂机提交学习时长

## Useful tools

- JSON格式化：https://www.json.cn/json/jsononline.html

- JSON转Java、PHP等语言实体：https://www.json.cn/json/json2java.html

- CURL 转 Java：
  - https://fivesmallq.github.io/curl-to-java/

- 在线CURL请求：https://reqbin.com/curl

## 程序说明

#### 配置项

| 配置项    | 示例值                                      | 说明                                                         |
| --------- | ------------------------------------------- | ------------------------------------------------------------ |
| login     | true                                        | 是否登录到账号。                                             |
| username  | 13311112222                                 | 手机号。                                                     |
| password  | sssss                                       | 明文密码。                                                   |
| courseId  | 406                                         | 课程ID                                                       |
| keyPrefix | bennie                                      | 用于redis中key的前缀。为空时为当前时间戳。**（可选）**       |
| token     | a-iQ_qHLYe7Lp7p2GDeNT8_CnZg25CQ4-1637546627 | 访问Token。`long=false`时需提供该值。可从登录接口响应`token`字段中获取。 |
| userId    | 10740728                                    | 用户ID。`long=false`时需提供该值。可从登录接口响应`id`字段中获取。 |

支持通过`java -jar -Dkey=value`方式传值。实际上，程序中以`System.getProperty("key")`方式读取配置值。

#### 启动程序

**不登录账号：**

```shell
nohup java -Xmx64m -Xms64m -jar -Dlogin=false -Dtoken=a-iQ_qHLYe7Lp7p1GDeNT8_CnZg25CQ4-1637546627 -DuserId=10740728 -DcourseId=406 -DkeyPrefix=bennie ./bjjnts-1.0-SNAPSHOT.jar > /dev/null 2>&1 & 
```

**登录账号：**

```shell
nohup java -Xmx64m -Xms64m -jar -Dlogin=true -Dusername=13311112222 -Dpassword=sssss -DcourseId=406 -DkeyPrefix=bennie ./bjjnts-1.0-SNAPSHOT.jar > /dev/null 2>&1 & 
```

View log：

```shell
tail -f -n500 ./logs/bjjnts.log

[2021-11-24 14:49:51.962] INFO ---[main] im.bennie.util.RequestUtil#loginAccount:93: Logging into your account username = xxxxxxxx, password = xxxxxxxx
[2021-11-24 14:49:53.634] INFO ---[main] im.bennie.component.LoginComponent#cacheUserInfo:41: Caching user info...
[2021-11-24 14:49:55.247] INFO ---[main] im.bennie.component.LoginComponent#cacheUserInfo:47: Finished Caching.
[2021-11-24 14:49:55.355] INFO ---[main] im.bennie.Config#loadConfig:63: Config(courseId=446, keyPrefix=bennie, doLogin=true, userId=10740743, username=xxxxxxxx, password=xxxxxxxx, accessToken=Bearer zo5MTB8rgLZlbrPB7IeFZ-8Aa8-hFIm9-1637736593)
[2021-11-24 16:27:57.355] INFO ---[main] im.bennie.util.RequestUtil#listCourseInfo:110: Requesting for list courses(id = 446) info
[2021-11-24 16:27:57.355] DEBUG---[main] im.bennie.util.RequestUtil#logRequestUrl:186: Request URL: https://apif.bjjnts.cn/courses/test-preview?course_id=446&class_id=27779
[2021-11-24 16:27:58.324] INFO ---[main] im.bennie.Main#run:43: Total units is: 89
[2021-11-24 16:27:58.348] INFO ---[main] im.bennie.Main#logVideoUnitProgress:83: Video units, Finished: 62，Unfinished: 27.
[2021-11-24 16:27:58.351] INFO ---[main] im.bennie.component.CourseComponent#retrieveUnitVideoFromCache:60: Retrieving unit-video mapping from cache.
[2021-11-24 16:27:58.400] INFO ---[main] im.bennie.Main#playVideo:92: Working on video unit [id = 9760, videoId = 9724, title = 5.9内存计算实例-spark, totalTime = 338, progress = 0, progressTime = 0]. 

[2021-11-24 16:27:58.400] INFO ---[main] im.bennie.Main#playVideo:189: Submitting study time at 0s for 1 times.
[2021-11-24 16:27:58.401] INFO ---[main] im.bennie.util.RequestUtil#updateStudyTime:73: Requesting to update study time.
[2021-11-24 16:27:58.401] DEBUG---[main] im.bennie.util.RequestUtil#logRequestUrl:186: Request URL: https://apistudy.bjjnts.cn/studies/study?video_id=9724&u=10740743&time=0&unit_id=9760&class_id=27779&start=1
[2021-11-24 16:27:58.796] INFO ---[main] im.bennie.util.RequestUtil#updateStudyTime:82: Result: {"video_time":0,"count_time":"338"}
[2021-11-24 16:29:58.797] INFO ---[main] im.bennie.Main#playVideo:189: Submitting study time at 120s for 2 times.
[2021-11-24 16:29:58.798] INFO ---[main] im.bennie.util.RequestUtil#updateStudyTime:73: Requesting to update study time.
[2021-11-24 16:29:58.798] DEBUG---[main] im.bennie.util.RequestUtil#logRequestUrl:186: Request URL: https://apistudy.bjjnts.cn/studies/study?video_id=9724&u=10740743&time=120&unit_id=9760&class_id=27779
[2021-11-24 16:29:59.133] INFO ---[main] im.bennie.util.RequestUtil#updateStudyTime:82: Result: {"video_time":120,"count_time":"338"}
[2021-11-24 16:31:59.134] INFO ---[main] im.bennie.Main#playVideo:189: Submitting study time at 240s for 3 times.
```

## 接口分析

#### 登录接口

Request：

```shell
## username: 手机号
## password：密码，明文

curl 'https://apif.bjjnts.cn/account/login' -X POST -H 'User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:94.0) Gecko/20100101 Firefox/94.0' -H 'Accept: application/json, text/plain, */*' -H 'Accept-Language: zh-CN,en-US;q=0.7,en;q=0.3' --compressed -H 'Referer: https://www.bjjnts.cn/user/login' -H 'Content-Type: application/json' -H 'X-Client-Type: pc' -H 'Origin: https://www.bjjnts.cn' -H 'DNT: 1' -H 'Connection: keep-alive' -H 'Sec-Fetch-Dest: empty' -H 'Sec-Fetch-Mode: cors' -H 'Sec-Fetch-Site: same-site' --data-raw '{"username":"xxxxxxxx","password":"xxxxxxxx","type":1}'
```

Response：

```json
{
  // User id
  "id": 1111111,
  "sid": "1011",
  "type": 1,
  // Your phone number
  "mobile": "11111111111",
  "email": "",
  "username": "s10847107",
  "name": "",
  "avatar": "https://thirdwx.qlogo.cn/mmopen/vi_32/bf03H2wR4RNc42PqBEFg5kSSUAIa6h0DW0egY2DRfP3bXL0IJDwN3yZandfEvA4LJ0Lb7mjo6sbwTjSEHGwJ6g/132",
  // Access token
  "access_token": "Bo0CMcktxWHBI_UO6oHHN2l3driYjX10-1637294006",
  "app_token": "",
  "status": 0,
  "create_time": 1629167325,
  "update_time": 1637294006,
  "info": {
    "id": "706233",
    "sid": "1011",
    // User id 
    "uid": "00000000",
    // Your id-card
    "idcard": "1111111111111111111111111",
    "real_auth_status": "1",
    "is_phone": "1",
    "is_actived": "0",
    "birthday": "1111111111111111111",
    "sex": "1",
    "education": "7",
    "student_category": "1",
    "census_type": "22",
    "census_province": "340000",
    "census_city": "340500",
    "census_area": "340523"
  },
  "jx_uid": 0,
  "jx_password": "",
  "jx_salt": "",
  "rid": 0,
  "idcard_front": "",
  "idcard_back": "",
  "seller_platform_token": "",
  "is_password": 1
}
```

#### 获取课程所有单元视频详情信息

返回数据中包含某章节内视频单元的视频具体信息：时长、名称、当前播放时间、总时间、是否已播放等。

Request：

- course_id=446 课程id
- class_id=27779 班级id

```shell
curl 'https://apif.bjjnts.cn/courses/test-preview?course_id=446&class_id=27779' \
  -H 'Connection: keep-alive' \
  -H 'sec-ch-ua: "Google Chrome";v="95", "Chromium";v="95", ";Not A Brand";v="99"' \
  -H 'DNT: 1' \
  -H 'sec-ch-ua-mobile: ?0' \
  -H 'Authorization: Bearer Sa7rmJrYpNQ6KQdoiRdbw4oMUFt3D_54-1637040580' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/plain, */*' \
  -H 'User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36' \
  -H 'X-Client-Type: pc' \
  -H 'sec-ch-ua-platform: "Windows"' \
  -H 'Origin: https://www.bjjnts.cn' \
  -H 'Sec-Fetch-Site: same-site' \
  -H 'Sec-Fetch-Mode: cors' \
  -H 'Sec-Fetch-Dest: empty' \
  -H 'Referer: https://www.bjjnts.cn/study?course_id=446&class_id=27779' \
  -H 'Accept-Language: zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7,en-GB;q=0.6' \
  --compressed
```

Response：

Too long, see：https://pastebin.com/twvtC7n0

#### 获取单个课程单元的视频信息

Request：

```shell
curl 'https://apif.bjjnts.cn/course-units/9718?class_id=27779' \
  -H 'Connection: keep-alive' \
  -H 'sec-ch-ua: "Google Chrome";v="95", "Chromium";v="95", ";Not A Brand";v="99"' \
  -H 'DNT: 1' \
  -H 'sec-ch-ua-mobile: ?0' \
  -H 'Authorization: Bearer Sa7rmJrYpNQ6KQdoiRdbw4oMUFt3D_54-1637040580' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/plain, */*' \
  -H 'User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36' \
  -H 'X-Client-Type: pc' \
  -H 'sec-ch-ua-platform: "Windows"' \
  -H 'Origin: https://www.bjjnts.cn' \
  -H 'Sec-Fetch-Site: same-site' \
  -H 'Sec-Fetch-Mode: cors' \
  -H 'Sec-Fetch-Dest: empty' \
  -H 'Referer: https://www.bjjnts.cn/study/video?course_id=446&unit_id=9718&class_id=27779' \
  -H 'Accept-Language: zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7,en-GB;q=0.6' \
  --compressed
```

Response：

<details>   <summary>展开查看</summary>   
<pre><code>
{
    "id":"9718",
    "bid":"1024",
    "open_id":"149605",
    "chapter_id":"2131",
    "section_id":"6009",
    "type":"1",
    "title":"2.14存储虚拟化：分布式存储实现方式",
    "description":"",
    "total_time":"312",
    "total_question":"0",
    "sort":"0",
    "hide":"0",
    "create_time":"1620725509",
    "update_time":"1620725509",
    "is_see":"0",
    "typeName":{
        "key":"1",
        "name":"视频"
    },
    "chapter":{
        "id":"2131",
        "title":"2.云计算",
        "course_id":"446"
    },
    "section":{
        "id":"6009",
        "title":"授课视频"
    },
    "files":[
    ],
    "unitMap":null,
    "train":null,
    "lastTrain":null,
    "video":{
        "id":"9682",
        "open_id":"148854",
        "course_id":"446",
        "unit_id":"9718",
        "type":"4",
        "image":"",
        "description":"",
        "url":"",
        "vod_id":"",
        "time":"312",
        "size":"0",
        "total_url":"0",
        "status":"2",
        "sort":"0",
        "hide":"0",
        "create_time":"1620725509",
        "update_time":"1620725509",
        "remarks":"",
        "urls":[
            {
                "id":"18365",
                "video_id":"9682",
                "clarity":"2",
                "type":"0",
                "url":"http://bjjnts-bd.xuetangx.com/a3c7941c2c9152da-10.mp4?auth_key=1637133934-0-0-1525D58AB3234CBEDB83FE5B515B32BD&wf=bj.pc",
                "resolution_width":"0",
                "resolution_height":"0",
                "bit":"0.0000",
                "size":"0",
                "source_id":"99E776B896BEE8989C33DC5901307461",
                "clarityName":{
                    "key":"2",
                    "name":"标清"
                }
            },
            {
                "id":"18366",
                "video_id":"9682",
                "clarity":"3",
                "type":"0",
                "url":"http://bjjnts-bd.xuetangx.com/a3c7941c2c9152da-20.mp4?auth_key=1637133934-0-0-D57CFA033AE751F6A9173702D0051CB5&wf=bj.pc",
                "resolution_width":"0",
                "resolution_height":"0",
                "bit":"0.0000",
                "size":"0",
                "source_id":"99E776B896BEE8989C33DC5901307461",
                "clarityName":{
                    "key":"3",
                    "name":"高清"
                }
            }
        ]
    },
    "progress_time":"0",
    "progress":0,
    "is_task":1,
    "course_id":"446",
    "class_id":27779,
    "unit_id":"9718"
}
</code></pre> </details>


属性说明：

```json
"progress_time":"0", // 为0表示当前视频未播放、等于totalTime时表示视频播放完毕，否则该视频未播放完毕，当前处于的播放时间点。
"progress": 0, // 视频已播放，1：已播放，0：未播放
"total_time": "312", // 视频总时长

"video": {
    "id": "9682", // 单元对应视频的视频id，在提交学习时长接口中作请求参数。
    ....
}
```

#### 提交单元视频的学习时间点

分两种情况：

1. 播放新视频。
2. 继续已播放视频。

提交视频播放时间点，最好是120秒提交一次，否则会检测到频繁提交，在程序中可以设置延迟。

如果是新播放的视频，那么初次提交时间从0开始，继续播放的视频，提交时间可以从当前播放时间120秒后开始。当前视频的播放时间的`progress_time`属性可以**获取单元的video信息**的接口响应中取得。

##### 播放新视频

**首次播放：**

注意，参数`time=0&start=1`，time从0开始，并且多出`start=1`参数。

```shell

  curl 'https://apistudy.bjjnts.cn/studies/study?video_id=9679&u=10740728&time=0&start=1&unit_id=9715&class_id=27779' \
  -X 'POST' \
  -H 'Connection: keep-alive' \
  -H 'Content-Length: 0' \
  -H 'sec-ch-ua: "Google Chrome";v="95", "Chromium";v="95", ";Not A Brand";v="99"' \
  -H 'DNT: 1' \
  -H 'sec-ch-ua-mobile: ?0' \
  -H 'Authorization: Bearer Sa7rmJrYpNQ6KQdoiRdbw4oMUFt3D_54-1637040580' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/plain, */*' \
  -H 'User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36' \
  -H 'X-Client-Type: pc' \
  -H 'sec-ch-ua-platform: "Windows"' \
  -H 'Origin: https://www.bjjnts.cn' \
  -H 'Sec-Fetch-Site: same-site' \
  -H 'Sec-Fetch-Mode: cors' \
  -H 'Sec-Fetch-Dest: empty' \
  -H 'Referer: https://www.bjjnts.cn/study/video?course_id=446&unit_id=9715&class_id=27779' \
  -H 'Accept-Language: zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7,en-GB;q=0.6' \
  --compressed
```

**正常提交：**

```shell
curl 'https://apistudy.bjjnts.cn/studies/study?video_id=9679&u=10740728&time=300&unit_id=9715&class_id=27779' \
  -X 'POST' \
  -H 'Connection: keep-alive' \
  -H 'Content-Length: 0' \
  -H 'sec-ch-ua: "Google Chrome";v="95", "Chromium";v="95", ";Not A Brand";v="99"' \
  -H 'DNT: 1' \
  -H 'sec-ch-ua-mobile: ?0' \
  -H 'Authorization: Bearer Sa7rmJrYpNQ6KQdoiRdbw4oMUFt3D_54-1637040580' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/plain, */*' \
  -H 'User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36' \
  -H 'X-Client-Type: pc' \
  -H 'sec-ch-ua-platform: "Windows"' \
  -H 'Origin: https://www.bjjnts.cn' \
  -H 'Sec-Fetch-Site: same-site' \
  -H 'Sec-Fetch-Mode: cors' \
  -H 'Sec-Fetch-Dest: empty' \
  -H 'Referer: https://www.bjjnts.cn/study/video?course_id=446&unit_id=9715&class_id=27779' \
  -H 'Accept-Language: zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7,en-GB;q=0.6' \
  --compressed
```

**末次提交：**

参数`time=300&end=1`，time等于视频总时间，且添加`end=1`。

```shell
curl 'https://apistudy.bjjnts.cn/studies/study?video_id=9679&u=10740728&time=300&unit_id=9715&class_id=27779' \
  -X 'POST' \
  -H 'Connection: keep-alive' \
  -H 'Content-Length: 0' \
  -H 'sec-ch-ua: "Google Chrome";v="95", "Chromium";v="95", ";Not A Brand";v="99"' \
  -H 'DNT: 1' \
  -H 'sec-ch-ua-mobile: ?0' \
  -H 'Authorization: Bearer Sa7rmJrYpNQ6KQdoiRdbw4oMUFt3D_54-1637040580' \
  -H 'Content-Type: application/json' \
  -H 'Accept: application/json, text/plain, */*' \
  -H 'User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/95.0.4638.69 Safari/537.36' \
  -H 'X-Client-Type: pc' \
  -H 'sec-ch-ua-platform: "Windows"' \
  -H 'Origin: https://www.bjjnts.cn' \
  -H 'Sec-Fetch-Site: same-site' \
  -H 'Sec-Fetch-Mode: cors' \
  -H 'Sec-Fetch-Dest: empty' \
  -H 'Referer: https://www.bjjnts.cn/study/video?course_id=446&unit_id=9715&class_id=27779' \
  -H 'Accept-Language: zh-CN,zh;q=0.9,en-US;q=0.8,en;q=0.7,en-GB;q=0.6' \
  --compressed
```

##### 继续已播放视频

例如一个视频321秒，之前未播放完，接下来继续播放，那么提交参数时，可以从当前播放视频的时间点追加120秒继续播放，然后继续提交至视频总时长即可。

继续提交时间时不需要提供`start=1`参数。

#### 标记图文单元已完成

图文单元直接提交请求标记完成。

```shell
curl 'https://apif.bjjnts.cn/course-unit-maps' 
    -X 'POST' \
    -H 'User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:94.0) Gecko/20100101 Firefox/94.0' \
    -H 'Accept: application/json, text/plain, */*' \
    -H 'Accept-Language: zh-CN,en-US;q=0.7,en;q=0.3' \
    -H 'Referer: https://www.bjjnts.cn/study/courseware?course_id=406&unit_id=8352&class_id=27779' \
    -H 'Content-Type: application/json' \
    -H 'X-Client-Type: pc' \
    -H 'Authorization: Bearer OrpqSzeYzq9NRPccxS3poDG0_bCxeBU3-1637646278' \
    -H 'Origin: https://www.bjjnts.cn' \
    -H 'DNT: 1' \
    -H 'Connection: keep-alive' \
    -H 'Sec-Fetch-Dest: empty' \
    -H 'Sec-Fetch-Mode: cors' \
    -H 'Sec-Fetch-Site: same-site' \
    --data '{"course_id":"406","unit_id":"8354","class_id":"27779"}' \
    --compressed
```

**Request Body：**

```
--data '{"course_id":"406","unit_id":"8354","class_id":"27779"}'
```
