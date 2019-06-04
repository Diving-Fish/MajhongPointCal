package com.divingfish.mahjongpointcal.Mahjong;

import net.sf.json.JSONObject;

import java.util.*;

public class MahjongGroup {
    // source
    public int type;
    public List<Mianzi> mianzis;
    public int duizi;
    public int ronTile;
    public boolean reached;
    public boolean isTsumo;
    public List<Integer> dora;
    public int aka;
    public int northDora;
    public List<Integer> innerDora;

    // input
    public int selfWind; // 0123 东南西北
    public int placeWind;

    public void setWind(int placeWind, int selfWind) {
        placeWind = placeWind;
        selfWind = selfWind;
    }

    public boolean isYifa;
    public boolean isHaidi;
    public boolean isHedi;
    public boolean isLingshang;
    public boolean isQiangGang;
    public boolean isWReach;
    public boolean isTianhe;
    public boolean isDihe;

    public void setYakuByChance(boolean yifa, boolean haidi, boolean hedi, boolean lingshang, boolean qianggang, boolean wreach, boolean tianhe, boolean dihe) {
        isYifa = yifa;
        isHaidi = haidi;
        isHedi = hedi;
        isLingshang = lingshang;
        isQiangGang = qianggang;
        isWReach = wreach;
        isTianhe = tianhe;
        isDihe = dihe;
    }

    // result
    public int fu;
    public List<Integer> yakus;
    public List<Integer> yakumans;
    public int fan;
    public int yakuman;
    public int perPoint;

    // procedure
    public List<Integer> tiles;
    public int ronType; // 0: 两面,  1：双碰,  2: 单骑、坎张、边张
    public boolean isInner;

    public MahjongGroup() {
        mianzis = new ArrayList<>();
    }

    private Set<Integer> genRonType() {
        Set<Integer> set = new HashSet<>();
        for (Mianzi m : mianzis) {
            if (m.type == 0 && m.status == 0) {
                if (ronTile == m.startTile) {
                    if (ronTile % 10 == 7) set.add(2);
                    else set.add(0);
                } else if (ronTile == m.startTile + 1)
                    set.add(2);
                else if (ronTile == m.startTile + 2) {
                    if (ronTile % 10 == 3) set.add(2);
                    else set.add(0);
                }
            }
            if (m.type == 1) {
                if (ronTile == m.startTile) set.add(1);
            }
        }
        if (duizi == ronTile) set.add(2);
        return set;
    }

    private void setInner() {
        isInner = false;
        for (Mianzi m : mianzis) {
            if (m.status == 1) return;
        }
        isInner = true;
    }

    public JSONObject getjson() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("yakus", yakus);
        jsonObject.put("fan", fan);
        jsonObject.put("fu", fu);
        jsonObject.put("perPoint", perPoint);
        return jsonObject;
    }

    public void init() {
        tiles = new ArrayList<>();
        for (Mianzi m : mianzis) {
            tiles.addAll(m.toList());
        }
        tiles.add(duizi);
        tiles.add(duizi);
        setInner();
        int fan_temp = 0;
        int fu_temp = 0;
        int score_temp = 0;
        List<Integer> yakus_temp = new ArrayList<>();
        Set<Integer> ronTypeSet = genRonType();
        for (int ronType: ronTypeSet) {
            this.yakus = new ArrayList<>();
            fan = 0;
            this.ronType = ronType;
            checkAllYakus();
            calFu();
            int score = PerPoint();
            if (score == score_temp) {
                if (fan > fan_temp) {
                    yakus_temp = this.yakus;
                    fan_temp = this.fan;
                    fu_temp = this.fu;
                    score_temp = score;
                }
            } else if (score > score_temp) {
                yakus_temp = this.yakus;
                fan_temp = this.fan;
                fu_temp = this.fu;
                score_temp = score;
            }
        }
        this.yakus = yakus_temp;
        this.fan = fan_temp;
        this.fu = fu_temp;
        this.perPoint = score_temp;
    }

    public int up(int i) {
        return ((i - 1) / 100 + 1) * 100;
    }

    private void output() {
        Integer[] ids = {1, 2, 3, 4, 5, 6, 71, 72, 73, 74, 75, 8, 9, 10, 11, 12, 13, 14, 15, 16, 17, 18, 19, 20, 21, 22, 23, 24, 25, 26};
        String[] names = {"立直", "一发", "门前清自摸和", "平和", "断幺九", "一杯口", "役牌：场风牌", "役牌：自风牌", "役牌：白", "役牌：发", "役牌：中", "海底捞月", "河底捞鱼", "枪杠", "岭上开花", "两立直", "七对子", "一气通贯", "三色同顺", "混全带幺九", "三色同刻", "三暗刻", "对对和", "小三元", "混老头", "三杠子", "混一色", "纯全带幺九", "二杯口", "清一色"};
        int[] fan_mq = {1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 2, 3, 3, 3, 6};
        int[] fan_fl = {0, 0, 0, 0, 1, 0, 1, 1, 1, 1, 1, 1, 1, 1, 1, 0, 0, 1, 1, 1, 2, 2, 2, 2, 2, 2, 2, 2, 0, 5};
        List<Integer> idList = Arrays.asList(ids);
        for (Integer yaku : yakus) {
            int index = idList.indexOf(yaku);
            if (isInner) {
                System.out.println(names[index] + "    " + fan_mq[index] + "番");
            } else {
                System.out.println(names[index] + "    " + fan_fl[index] + "番");
            }
        }
        System.out.println("------------------------");
        System.out.println("合计："+ fu + "符" + fan + "番");
        if (!isTsumo) {
            System.out.println("亲家/子家：" + up(perPoint * 6) + " / " + up(perPoint * 4));
        } else {
            System.out.println("亲家/子家：" + up(perPoint * 2) + "All / " + up(perPoint) + "-" + up(perPoint * 2));
        }
    }

    private void calFu() {
        int difu = 20;
        if (yakus.contains(4) && yakus.contains(3)) {
            fu = 20;
            return;
        } else if (yakus.contains(4)) {
            fu = 30;
            return;
        }
        if (duizi <= 47 && duizi >= 45) difu += 2;
        if (duizi == selfWind + 41) difu += 2;
        if (duizi == placeWind + 41) difu += 2;
        for (Mianzi m : mianzis) {
            int mul = 0;
            if (m.type == 2) mul = 4;
            if (m.type == 1) mul = 1;
            int stat = 1;
            if (m.status == 0 && !(ronTile == m.startTile && ronType == 1 && !isTsumo)) {
                stat = 2;
            }
            difu += 2 * stat * mul * ((m.startTile % 10 == 1 || m.startTile % 10 == 9 || m.startTile >= 41) ? 2 : 1);
        }
        difu += (ronType < 2) ? 0 : 2;
        if (isTsumo) difu += 2;
        else if (isInner) difu += 10;
        if (difu == 20 && !isInner) {
            fu = 30;
        } else {
            fu = ((difu - 1) / 10 + 1) * 10;
        }
    }

    private int PerPoint() {
        if (yakuman != 0) return yakuman * 8000;
        if (fan == 0) {
            return 0;
        } else if (fan <= 4) {
            return Math.min(fu * (int) Math.pow(2, 2 + fan), 2000);
        } else if (fan <= 5) {
            return 2000;
        } else if (fan <= 7) {
            return 3000;
        } else if (fan <= 10) {
            return 4000;
        } else if (fan <= 12) {
            return 6000;
        } else {
            return 8000;
        }
    }

    private void checkAllYakus() {
        checkYaku1();
        checkYaku2();
        checkYaku3();
        checkYaku4();
        checkYaku5();
        checkYaku6();
        checkYaku7();
        checkYaku8();
        checkYaku9();
        checkYaku10();
        checkYaku11();
        checkYaku12();
        checkYaku13();
        checkYaku14();
        checkYaku15();
        checkYaku16();
        checkYaku17();
        checkYaku18();
        checkYaku19();
        checkYaku20();
        checkYaku21();
        checkYaku22();
        checkYaku23();
        checkYaku24();
        checkYaku25();
        checkYaku26();
    }

    //    1 - 立直
    private void checkYaku1() {
        if (isInner && reached) {
            this.yakus.add(1);
            this.fan += 1;
        }
    }

    //    2 - 一发
    private void checkYaku2() {
        if (isInner && reached && isYifa) {
            this.yakus.add(2);
            this.fan += 1;
        }
    }

    //    3 - 自摸
    private void checkYaku3() {
        if (isInner && isTsumo) {
            this.yakus.add(3);
            this.fan += 1;
        }
    }

    //    4 - 平和
    private void checkYaku4() {
        for (Mianzi mz : mianzis) {
            if (mz.type != 0) return;
        }
        if (duizi <= 47 && duizi >= 45) return;
        if (duizi == selfWind + 41) return;
        if (duizi == placeWind + 41) return;
        if (isInner & ronType == 0) {
            this.yakus.add(4);
            this.fan += 1;
        }
    }

    //    5 - 断幺
    private void checkYaku5() {
        for (int tile : tiles) {
            if (tile % 10 == 1 || tile % 10 == 9 || tile >= 41) return;
        }
        this.yakus.add(5);
        this.fan += 1;
    }

    //    6 - 一杯口
    private void checkYaku6() {
        if (!isInner) return;
        for (int i = 0; i < mianzis.size() - 1; i++) {
            Mianzi a = mianzis.get(i);
            Mianzi b = mianzis.get(i+1);
            if (a.type == b.type && a.type == 0 && a.startTile == b.startTile) {
                this.yakus.add(6);
                this.fan += 1;
                return;
            }
        }
    }

    //    7 - 役牌
    private void checkYaku7() {
        checkYaku71();
        checkYaku72();
        checkYaku73();
        checkYaku74();
        checkYaku75();
    }

    private void checkYaku71() {
        for (Mianzi m : mianzis) {
            if (m.type == 1 && m.startTile == placeWind + 41) {
                this.yakus.add(71);
                this.fan += 1;
            }
        }
    }

    private void checkYaku72() {
        for (Mianzi m : mianzis) {
            if (m.type == 1 && m.startTile == selfWind + 41) {
                this.yakus.add(72);
                this.fan += 1;
            }
        }
    }


    private void checkYaku73() {
        for (Mianzi m : mianzis) {
            if (m.type == 1 && m.startTile == 45) {
                this.yakus.add(73);
                this.fan += 1;
            }
        }
    }


    private void checkYaku74() {
        for (Mianzi m : mianzis) {
            if (m.type == 1 && m.startTile == 46) {
                this.yakus.add(74);
                this.fan += 1;
            }
        }
    }


    private void checkYaku75() {
        for (Mianzi m : mianzis) {
            if (m.type == 1 && m.startTile == 47) {
                this.yakus.add(75);
                this.fan += 1;
            }
        }
    }


    //    8 - 海底
    private void checkYaku8() {
        if (isHaidi & isTsumo) {
            this.yakus.add(8);
            this.fan += 1;
        }
    }

    //    9 - 河底
    private void checkYaku9() {
        if (isHedi & !isTsumo) {
            this.yakus.add(9);
            this.fan += 1;
        }
    }

    //   10 - 抢杠
    private void checkYaku10() {
        if (isQiangGang & !isTsumo) {
            this.yakus.add(10);
            this.fan += 1;
        }
    }

    //   11 - 岭上
    private void checkYaku11() {
        if (isLingshang & isTsumo) {
            this.yakus.add(11);
            this.fan += 1;
        }
    }

    //   12 - 两立直
    private void checkYaku12() {
        if (isWReach & reached) {
            this.yakus.add(12);
            this.fan += 1;
        }
    }

    //   13 - 七对子
    private void checkYaku13() {
        // ...
    }

    //   14 - 一气
    private void checkYaku14() {
        for (int i = 0; i < mianzis.size(); i++) {
            for (int j = i + 1; j < mianzis.size(); j++) {
                for (int k = j + 1; k < mianzis.size(); k++) {
                    Mianzi a = mianzis.get(i), b = mianzis.get(j), c = mianzis.get(k);
                    if (a.startTile / 10 == b.startTile / 10 && a.startTile / 10 == c.startTile / 10
                    && a.type == 0 && b.type == 0 && c.type == 0) {
                        int[] array = {a.startTile % 10, b.startTile % 10, c.startTile % 10};
                        Arrays.sort(array);
                        if (Arrays.equals(array, new int[] {1, 4, 7})) {
                            yakus.add(14);
                            if (isInner) fan += 2;
                            else fan += 1;
                            return;
                        }
                    }
                }
            }
        }
    }

    //   15 - 三色
    private void checkYaku15() {
        for (int i = 0; i < mianzis.size(); i++) {
            for (int j = i + 1; j < mianzis.size(); j++) {
                for (int k = j + 1; k < mianzis.size(); k++) {
                    Mianzi a = mianzis.get(i), b = mianzis.get(j), c = mianzis.get(k);
                    if (a.startTile % 10 == b.startTile % 10 && a.startTile % 10 == c.startTile % 10
                            && a.type == 0 && b.type == 0 && c.type == 0) {
                        int[] array = {a.startTile / 10, b.startTile / 10, c.startTile / 10};
                        Arrays.sort(array);
                        if (Arrays.equals(array, new int[] {1, 2, 3})) {
                            yakus.add(15);
                            if (isInner) fan += 2;
                            else fan += 1;
                            return;
                        }
                    }
                }
            }
        }
    }

    //   16 - 全带
    private void checkYaku16() {
        if (duizi % 10 != 1 && duizi % 10 != 9 && duizi < 40) return;
        for (Mianzi m: mianzis) {
            if (m.type == 0) {
                if (m.startTile % 10 != 1 && m.startTile % 10 != 7) return;
            } else {
                if (m.startTile % 10 != 1 && m.startTile % 10 != 9 && m.startTile < 40) return;
            }
        }
        yakus.add(16);
        if (isInner) fan += 2;
        else fan += 1;
    }

    //   17 - 三色同刻
    private void checkYaku17() {
        for (int i = 0; i < mianzis.size(); i++) {
            for (int j = i + 1; j < mianzis.size(); j++) {
                for (int k = j + 1; k < mianzis.size(); k++) {
                    Mianzi a = mianzis.get(i), b = mianzis.get(j), c = mianzis.get(k);
                    if (a.startTile % 10 == b.startTile % 10 && a.startTile % 10 == c.startTile % 10
                            && a.type >= 1 && b.type >= 1 && c.type >= 1) {
                        int[] array = {a.startTile / 10, b.startTile / 10, c.startTile / 10};
                        Arrays.sort(array);
                        if (Arrays.equals(array, new int[] {1, 2, 3})) {
                            yakus.add(16);
                            fan += 2;
                        }
                    }
                }
            }
        }
    }

    //   18 - 三暗
    private void checkYaku18() {
        for (int i = 0; i < mianzis.size(); i++) {
            for (int j = i + 1; j < mianzis.size(); j++) {
                for (int k = j + 1; k < mianzis.size(); k++) {
                    Mianzi a = mianzis.get(i), b = mianzis.get(j), c = mianzis.get(k);
                    if (a.type >= 1 && b.type >= 1 && c.type >= 1 && a.status == 0 && b.status == 0 && c.status == 0) {
                        if (ronType == 1 && !isTsumo) {
                            if (ronTile == a.startTile || ronTile == b.startTile || ronTile == c.startTile) {
                                continue;
                            }
                        }
                        yakus.add(18);
                        fan += 2;
                        return;
                    }
                }
            }
        }
    }

    //   19 - 对对
    private void checkYaku19() {
        for (Mianzi m : mianzis) {
            if (m.type == 0) return;
        }
        yakus.add(19);
        fan += 2;
    }

    //   20 - 小三
    private void checkYaku20() {
        for (int i = 0; i < mianzis.size(); i++) {
            for (int j = i + 1; j < mianzis.size(); j++) {
                Mianzi a = mianzis.get(i), b = mianzis.get(j);
                int[] array = {a.startTile, b.startTile, duizi};
                Arrays.sort(array);
                if (Arrays.equals(array, new int[]{45, 46, 47})) {
                    yakus.add(20);
                    fan += 2;
                    return;
                }
            }
        }
    }

    //   21 - 混老头
    private void checkYaku21() {
        for (int tile : tiles) {
            if (tile % 10 != 1 && tile % 10 != 9 && tile < 40) return;
        }
        yakus.remove(new Integer(16));
        yakus.add(21);
        if (!isInner) fan += 1;
    }

    //   22 - 三杠子
    private void checkYaku22() {
        for (int i = 0; i < mianzis.size(); i++) {
            for (int j = i + 1; j < mianzis.size(); j++) {
                for (int k = j + 1; k < mianzis.size(); k++) {
                    Mianzi a = mianzis.get(i), b = mianzis.get(j), c = mianzis.get(k);
                    if (a.type == 2 && b.type == 2 && c.type == 2) {
                        yakus.add(22);
                        fan += 2;
                    }
                }
            }
        }
    }

    //   23 - 混一色
    private void checkYaku23() {
        int color = -1;
        for (int tile : tiles) {
            if (tile < 40) {
                if (color == -1) color = tile / 10;
                else if (color != tile / 10) return;
            }
        }
        yakus.add(23);
        if (isInner) fan += 3;
        else fan += 2;
    }

    //   24 - 纯全
    private void checkYaku24() {
        if (duizi % 10 != 1 && duizi % 10 != 9 || duizi > 40) return;
        for (Mianzi m: mianzis) {
            if (m.type == 0) {
                if (m.startTile % 10 != 1 && m.startTile % 10 != 7) return;
            } else {
                if (m.startTile % 10 != 1 && m.startTile % 10 != 9 || m.startTile > 40) return;
            }
        }
        yakus.remove(new Integer(16));
        yakus.add(24);
        fan += 1;
    }

    //   25 - 二杯
    private void checkYaku25() {
        if (!isInner) return;
        if ((mianzis.get(0).type == mianzis.get(1).type && mianzis.get(0).type == 0 && mianzis.get(1).startTile == mianzis.get(0).startTile)
            && (mianzis.get(3).type == mianzis.get(2).type && mianzis.get(3).type == 0 && mianzis.get(3).startTile == mianzis.get(2).startTile)) {
            yakus.remove(new Integer(6));
            yakus.add(25);
            fan += 2;
        }
    }

    //   26 - 清一色
    private void checkYaku26() {
        int color = -1;
        for (int tile : tiles) {
            if (color == -1) color = tile / 10;
            else if (color != tile / 10) return;
        }
        yakus.remove(new Integer(23));
        yakus.add(26);
        fan += 3;
    }

    //   27 - 国士无双
    private void checkYaku27() {
        // ...
    }

    //   28 - 大三元
    private void checkYaku28() {
        for (int i = 0; i < mianzis.size(); i++) {
            for (int j = i + 1; j < mianzis.size(); j++) {
                for (int k = j + 1; k < mianzis.size(); k++) {
                    Mianzi a = mianzis.get(i), b = mianzis.get(j), c = mianzis.get(k);
                    if (a.type >= 1 && b.type >= 1 && c.type >= 1) {
                        int[] array = {a.startTile, b.startTile, c.startTile};
                        Arrays.sort(array);
                        if (Arrays.equals(array, new int[] {45, 46, 47})) {
                            yakuman += 1;
                            yakumans.add(28);
                        }
                    }
                }
            }
        }
    }

    //   29 - 四暗刻
    //   30 - 小四喜
    //   31 - 字一色
    //   32 - 绿一色
    //   33 - 清老头
    //   34 - 九莲宝灯
    //   35 - 四杠子
    //   36 - 天和
    //   37 - 地和
    //   38 - 国士无双十三面
    //   39 - 大四喜
    //   40 - 四暗刻单骑
    //   41 - 纯正九莲宝灯
    //   42 - 宝牌计算
}
