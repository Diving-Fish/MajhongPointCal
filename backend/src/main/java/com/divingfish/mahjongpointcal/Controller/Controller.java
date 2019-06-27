package com.divingfish.mahjongpointcal.Controller;

import com.divingfish.mahjongpointcal.Exceptions.MahjongStringException;
import com.divingfish.mahjongpointcal.Exceptions.YakuException;
import com.divingfish.mahjongpointcal.Mahjong.MahjongGroup;
import com.divingfish.mahjongpointcal.Mahjong.MahjongParser;
import net.sf.json.JSONException;
import net.sf.json.JSONObject;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RestController
@EnableAutoConfiguration
public class Controller {
    @RequestMapping(value = "/cal", method = RequestMethod.POST)
    @ResponseBody
    public JSONObject caldetail(@RequestBody JSONObject requestBody) {
        JSONObject jsonObject = new JSONObject();

        String inner, outer, dora, innerdora;
        boolean reach, tsumo;
        try {
            inner = requestBody.getString("inner");
            outer = requestBody.getString("outer");
            dora = requestBody.getString("dora");
            innerdora = requestBody.getString("innerdora");
            reach = requestBody.getBoolean("reach");
            tsumo = requestBody.getBoolean("tsumo");
        } catch (JSONException e) {
            jsonObject.put("status", "error");
            jsonObject.put("message", e.getMessage());
            return jsonObject;
        }
        if (inner.equals("")) {
            jsonObject.put("status", "error");
            jsonObject.put("message", "请输入必要的数据项");
            return jsonObject;
        }

        int placewind = 0, selfwind = 0;
        boolean yifa = false, haidi = false, hedi = false, lingshang = false,
                qianggang = false, wreach = false, tianhe = false, dihe = false;

        placewind = requestBody.getInt("placewind");
        selfwind = requestBody.getInt("selfwind");

        yifa = requestBody.getBoolean("yifa");
        haidi = requestBody.getBoolean("haidi");
        hedi = requestBody.getBoolean("hedi");
        lingshang = requestBody.getBoolean("lingshang");
        qianggang = requestBody.getBoolean("qianggang");
        wreach = requestBody.getBoolean("wreach");
        tianhe = requestBody.getBoolean("tianhe");
        dihe = requestBody.getBoolean("dihe");

        String ronTile = inner.substring(inner.length() - 2);
        List<String> strings = Arrays.asList(outer.split(" "));
        MahjongParser parser;
        try {
            parser = new MahjongParser(inner, strings, ronTile, dora, innerdora, 0, reach, tsumo);
        } catch (MahjongStringException e) {
            jsonObject.put("status", "error");
            jsonObject.put("message", "查询有误。牌的数量是否不正确，或牌输入格式有误？");
            return jsonObject;
        }
        List<JSONObject> jsonObjects = new ArrayList<>();
        Set<MahjongGroup> mahjongGroups = parser.getMahjongGroups();
        for (MahjongGroup mahjongGroup : mahjongGroups) {
            mahjongGroup.setYakuByChance(yifa, haidi, hedi, lingshang, qianggang, wreach, tianhe, dihe);
            mahjongGroup.setWind(placewind, selfwind);
            try {
                mahjongGroup.init();
            } catch (YakuException e) {
                jsonObject.put("status", "error");
                jsonObject.put("message", e.getMessage());
                return jsonObject;
            }
            jsonObjects.add(mahjongGroup.getjson());
        }
        int perPoint = 0, fan = 0, index = 0, fu = 0;
        for (int i = 0; i < jsonObjects.size(); i++) {
            JSONObject j = jsonObjects.get(i);
            if (j.getInt("perPoint") == perPoint) {
                if (j.getInt("fan") > fan) {
                    index = i;
                    fan = j.getInt("fan");
                    fu = j.getInt("fu");
                } else if (j.getInt("fan") == fan) {
                    if (j.getInt("fu") > fu) {
                        index = i;
                        fu = j.getInt("fu");
                    }
                }
            } else if (j.getInt("perPoint") > perPoint) {
                index = i;
                fan = j.getInt("fan");
                fu = j.getInt("fu");
                perPoint = j.getInt("perPoint");
            }
        }
        if (jsonObjects.size() == 0) {
            jsonObject.put("status", "error");
            jsonObject.put("message", "此牌无法和牌");
        } else {
            jsonObject.put("status", 200);
            jsonObject.put("data", jsonObjects.get(index));
        }
        return jsonObject;
    }
}
