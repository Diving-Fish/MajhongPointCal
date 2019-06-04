package com.divingfish.mahjongpointcal.Controller;

import com.divingfish.mahjongpointcal.Exceptions.MahjongStringException;
import com.divingfish.mahjongpointcal.Mahjong.MahjongGroup;
import com.divingfish.mahjongpointcal.Mahjong.MahjongParser;
import net.sf.json.JSONObject;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

@RestController
@EnableAutoConfiguration
public class Controller {
    @GetMapping(value = "/cal")
    @ResponseBody
    public JSONObject cal(@RequestParam("inner") String inner, @RequestParam("outer") String outer, @RequestParam("dora") String dora,
                          @RequestParam("innerDora") String innerDora, @RequestParam("reach") boolean reach, @RequestParam("tsumo") boolean tsumo)
    {
        JSONObject jsonObject = new JSONObject();
        String ronTile = inner.substring(inner.length() - 2);
        List<String> strings = Arrays.asList(outer.split(" "));
        MahjongParser parser;
        try {
            parser = new MahjongParser(inner, strings, ronTile, dora, innerDora, 0, reach, tsumo);
        } catch (MahjongStringException e) {
            jsonObject.put("status", 200);
            jsonObject.put("message", "invalid query");
            return jsonObject;
        }
        List<JSONObject> jsonObjects = new ArrayList<>();
        Set<MahjongGroup> mahjongGroups = parser.getMahjongGroups();
        for (MahjongGroup mahjongGroup : mahjongGroups) {
            mahjongGroup.init();
            jsonObjects.add(mahjongGroup.getjson());
        }
        int perPoint = 0, fan = 0, index = 0;
        for (int i = 0; i < jsonObjects.size(); i++) {
            JSONObject j = jsonObjects.get(i);
            if (j.getInt("perPoint") == perPoint) {
                if (j.getInt("fan") > fan) {
                    index = i;
                }
            } else if (j.getInt("perPoint") > perPoint) {
                index = i;
            }
        }
        if (jsonObjects.size() == 0) {
            jsonObject.put("status", 200);
            jsonObject.put("message", "此牌无法和牌");
        } else {
            jsonObject.put("status", 200);
            jsonObject.put("data", jsonObjects.get(index));
        }
        return jsonObject;
    }
}
