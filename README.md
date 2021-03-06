# MajhongPointCal

已开放后端查询，URL(*Post*)：`http://47.100.50.175:8000/cal`

发送的RequestBody格式为json，具体如下：
```
{
	"inner": "12379m11p8m",  //未副露的手牌，和了牌放在最后一枚
	"outer": "406m 234p",    //副露的手牌，不同的面子用空格隔开，暗杠用0xx0表示，比如3m暗杠0330m，5的数牌暗杠会自动加入红宝
	"dora": "1p5s",          //dora，可以写多个
	"innerdora": "1p2s",     //里dora，可以写多个
	"reach": false,          //是否立直
	"tsumo": true,           //是否自摸
	
	"selfwind": 0,           //自风：0123-东南西北
	"placewind": 0,          //场风
	
	"yifa": false,           //一发
	"haidi": false,          //海底
	"hedi": false,           //河底
	"lingshang": false,      //岭上
	"qianggang": false,      //枪杠
	"wreach": false,         //w立
	"tianhe": false,         //天和
	"dihe": false            //地和
}
```

返回的Response如下：
```
{
    "status": 200,
    "data": {
        "yakus": [  // 役种，对照表见下方
            14,   // 一气通贯
            102,  // dora 2
            201   // 赤dora 1
        ],
        "fan": 4,   // 番
        "fu": 30,   // 符
        "perPoint": 1920,  // 单位点数
        "yakuman": false   // 是否役满
    }
}
```

役种对照表：

id | 役种名 | id | 役种名 | id | 役种名 | id | 役种名 |
--- | ---- | --- | ---- | --- | ---- | --- | ----
1 | 立直 | 9 | 河底捞鱼 | 21 | 混老头 | 33 | 清老头 | 
2 | 一发 | 10 | 枪杠 | 22 | 三杠子 | 34 | 九莲宝灯 | 
3 | 门前清自摸和 | 11 | 岭上开花 | 23 | 混一色 | 35 | 四杠子 | 
4 | 平和 | 12 | 两立直 | 24 | 纯全带幺九 | 36 | 天和 | 
5 | 断幺九 | 13 | 七对子 | 25 | 二杯口 | 37 | 地和 | 
6 | 一杯口 | 14 | 一气通贯 | 26 | 清一色 | 38 | 国士无双十三面 | 
71 | 役牌：场风牌 | 15 | 三色同顺 | 27 | 国士无双 | 39 | 大四喜 | 
72 | 役牌：自风牌 | 16 | 混全带幺九 | 28 | 大三元 | 40 | 四暗刻单骑 | 
73 | 役牌：白 | 17 | 三色同刻 | 29 | 四暗刻 | 41 | 纯正九莲宝灯 | 
74 | 役牌：发 | 18 | 三暗刻 | 30 | 小四喜 | 10x | 宝牌x | 
75 | 役牌：中 | 19 | 对对和 | 31 | 字一色 | 20x | 赤宝牌x | 
8 | 海底捞月 | 20 | 小三元 | 32 | 绿一色 | 30x | 里宝牌x | 
