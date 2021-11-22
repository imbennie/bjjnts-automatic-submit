# 程序说明

## 配置项

## 启动程序

```shell
nohup java -Xmx64m -Xms64m -jar -Dlogin=false -Dtoken=a-iQ_qHLYe7Lp7p1GDeNT8_CnZg25CQ4-1637546627 -DuserId=10740728 -DcourseId=406 -DkeyPrefix=hb ./bjjnts-1.0-SNAPSHOT.jar > /dev/null 2>&1 & 
```

View log：

```shell
tail -f -n500 ./logs/bjjnts.log
```

# 京训钉 刷学习时长接口分析

一些有用的工具：

JSON格式：https://www.json.cn/json/jsononline.html

JSON转Java、PHP等语言实体：https://www.json.cn/json/json2java.html

CURL 转 Java：

- https://fivesmallq.github.io/curl-to-java/
- https://reqbin.com/curl

## 登录接口

Request：

```shell
# username: 手机号
# password：密码，明文

curl 'https://apif.bjjnts.cn/account/login' -X POST -H 'User-Agent: Mozilla/5.0 (Windows NT 10.0; Win64; x64; rv:94.0) Gecko/20100101 Firefox/94.0' -H 'Accept: application/json, text/plain, */*' -H 'Accept-Language: zh-CN,en-US;q=0.7,en;q=0.3' --compressed -H 'Referer: https://www.bjjnts.cn/user/login' -H 'Content-Type: application/json' -H 'X-Client-Type: pc' -H 'Origin: https://www.bjjnts.cn' -H 'DNT: 1' -H 'Connection: keep-alive' -H 'Sec-Fetch-Dest: empty' -H 'Sec-Fetch-Mode: cors' -H 'Sec-Fetch-Site: same-site' --data-raw '{"username":"15058221727","password":"6868668","type":1}'
```

Response：

```json
{
  // User id
  "id": 00000000,
  "sid": "1011",
  "type": 1,
  // Your phone number
  "mobile": "11111111111",
  "email": "",
  "username": "s10847107",
  "name": "",
  "avatar": "https://thirdwx.qlogo.cn/mmopen/vi_32/bf03H2wR4RNc42PqBEFg5kSSUAIa6h0DW0egY2DRfP3bXL0IJDwN3yZandfEvA4LJ0Lb7mjo6sbwTjSEHGwJ6g/132",
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
    // your id-card
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

## 获取课程所有单元视频详情信息

返回数据中包含某章节内视频单元的视频具体信息：时长、名称、当前播放时间、总时间、是否已播放等。

Request：

参数：

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

see：https://pastebin.com/twvtC7n0

## 获取单个课程单元的视频信息

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

## 提交单元视频的学习时间点

分两种情况：

1. 播放新的视频。
2. 继续已播放的视频。

提交视频播放时间点，最好是120秒提交一次，否则会检测到频繁提交，在程序中可以设置线程延迟。

如果是新播放的视频，那么初次提交时间从0开始，继续播放的视频，提交时间可以从当前播放时间120秒后开始。当前视频的播放时间的`progress_time`属性可以**获取单元的video信息**的接口响应中取得。

### 播放新视频

首次播放时注意，参数`time=0&start=1`，time从0开始，并且多出`start=1`参数。

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

正常提交:

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

最后一次提交：

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

### 继续播放视频

例如一个视频321秒，之前未播放完，接下来继续播放，那么提交参数时，可以从当前播放视频的时间点追加120秒继续播放，然后继续提交至视频总时长即可。

继续提交时间时不需要提供`start=1`参数。
