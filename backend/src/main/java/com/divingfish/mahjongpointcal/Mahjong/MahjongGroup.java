package com.divingfish.mahjongpointcal.Mahjong;

import com.divingfish.mahjongpointcal.Exceptions.YakuException;
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
        this.placeWind = placeWind;
        this.selfWind = selfWind;
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
    public List<Integer> consumedTiles;
    public List<Integer> tiles;
    public int ronType; // 0: 两面,  1：双碰,  2: 单骑、坎张、边张
    public boolean isInner;

    public MahjongGroup() {
        mianzis = new ArrayList<>();
    }

    private Set<Integer> genRonType() {
        Set<Integer> set = new HashSet<>();
        if (type == 2) {
            set.add(0);
            return set;
        } else if (type == 1) {
            set.add(2);
            return set;
        }
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
        if (yakuman == 0) {
            jsonObject.put("yakus", yakus);
            jsonObject.put("fan", fan);
            jsonObject.put("fu", fu);
            jsonObject.put("perPoint", perPoint);
            jsonObject.put("inner", isInner);
            jsonObject.put("tsumo", isTsumo);
            jsonObject.put("isQin", selfWind == 0);
            jsonObject.put("yakuman", false);
        } else {
            jsonObject.put("yakus", yakumans);
            jsonObject.put("fan", yakuman * 13);
            jsonObject.put("fu", fu);
            jsonObject.put("perPoint", perPoint);
            jsonObject.put("inner", isInner);
            jsonObject.put("tsumo", isTsumo);
            jsonObject.put("isQin", selfWind == 0);
            jsonObject.put("yakuman", true);
        }
        return jsonObject;
    }

    public void init() throws YakuException {
        if (type == 0) {
            tiles = new ArrayList<>();
            for (Mianzi m : mianzis) {
                tiles.addAll(m.toList());
            }
            tiles.add(duizi);
            tiles.add(duizi);
        }
        consumedTiles.remove(new Integer(ronTile));
        setInner();
        int fan_temp = 0;
        int fu_temp = 0;
        int score_temp = 0;
        List<Integer> yakus_temp = new ArrayList<>();
        Set<Integer> ronTypeSet = genRonType();
        for (int ronType: ronTypeSet) {
            this.yakus = new ArrayList<>();
            this.yakumans = new ArrayList<>();
            yakuman = 0;
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

    private void calFu() {
        int difu = 20;
        if (type == 2) {
            fu = 30;
            return;
        }
        if (type == 1) {
            fu = 25;
            return;
        }
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

    private void checkAllYakus() throws YakuException {
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
        checkYaku27();
        checkYaku28();
        checkYaku29();
        checkYaku30();
        checkYaku31();
        checkYaku32();
        checkYaku33();
        checkYaku34();
        checkYaku35();
        checkYaku36();
        checkYaku37();
        checkYaku38();
        checkYaku39();
        checkYaku40();
        checkYaku41();
        checkDora();
    }

    //    1 - 立直
    private void checkYaku1() throws YakuException {
        if (reached) {
            if (!isInner) {
                throw new YakuException("非门前清状态下无法立直！");
            } else {
                this.yakus.add(1);
                this.fan += 1;
            }
        }
    }

    //    2 - 一发
    private void checkYaku2() throws YakuException {
        if (isYifa) {
            if (!reached) {
                throw new YakuException("非立直状态下无法一发！");
            } else {
                this.yakus.add(2);
                this.fan += 1;
            }
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
        if (type != 0) return;
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
        if (type != 0) return;
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
        if (type != 0) return;
        checkYaku71();
        checkYaku72();
        checkYaku73();
        checkYaku74();
        checkYaku75();
    }

    private void checkYaku71() {
        for (Mianzi m : mianzis) {
            if (m.type >= 1 && m.startTile == placeWind + 41) {
                this.yakus.add(71);
                this.fan += 1;
            }
        }
    }

    private void checkYaku72() {
        for (Mianzi m : mianzis) {
            if (m.type >= 1 && m.startTile == selfWind + 41) {
                this.yakus.add(72);
                this.fan += 1;
            }
        }
    }


    private void checkYaku73() {
        for (Mianzi m : mianzis) {
            if (m.type >= 1 && m.startTile == 45) {
                this.yakus.add(73);
                this.fan += 1;
            }
        }
    }


    private void checkYaku74() {
        for (Mianzi m : mianzis) {
            if (m.type >= 1 && m.startTile == 46) {
                this.yakus.add(74);
                this.fan += 1;
            }
        }
    }


    private void checkYaku75() {
        for (Mianzi m : mianzis) {
            if (m.type >= 1 && m.startTile == 47) {
                this.yakus.add(75);
                this.fan += 1;
            }
        }
    }


    //    8 - 海底
    private void checkYaku8() throws YakuException {
        if (isHaidi) {
            if (!isTsumo) {
                throw new YakuException("非自摸情况下无法海底！");
            } else {
                this.yakus.add(8);
                this.fan += 1;
            }
        }
    }

    //    9 - 河底
    private void checkYaku9() throws YakuException {
        if (isHedi) {
            if (isTsumo) {
                throw new YakuException("自摸情况下无法河底！");
            } else if (isYifa) {
                throw new YakuException("一发无法和河底复合！");
            }
            this.yakus.add(9);
            this.fan += 1;
        }
    }

    //   10 - 抢杠
    private void checkYaku10() throws YakuException {
        if (isQiangGang) {
            if (isTsumo) {
                throw new YakuException("枪杠情况下无法自摸！");
            } else if (consumedTiles.contains(ronTile)) {
                throw new YakuException("枪杠牌无法被杠出！");
            } else if (isHedi) {
                throw new YakuException("河底无法开杠！");
            }
            this.yakus.add(10);
            this.fan += 1;
        }
    }

    //   11 - 岭上
    private void checkYaku11() throws YakuException {
        if (isLingshang) {
            if (!isTsumo) {
                throw new YakuException("岭上开花必须自摸！");
            } else if (isHaidi) {
                throw new YakuException("岭上无法海底！");
            } else if (isYifa) {
                throw new YakuException("岭上无法一发！");
            } else {
                boolean flag = false;
                for (Mianzi m : mianzis) {
                    if (m.type == 2) flag = true;
                }
                if (!flag) throw new YakuException("岭上开花必须有杠材！");
            }
            this.yakus.add(11);
            this.fan += 1;
        }
    }

    //   12 - 两立直
    private void checkYaku12() throws YakuException {
        if (isWReach) {
            if (!reached) {
                throw new YakuException("双立直应处于立直状态！");
            } else if (isQiangGang) {
                throw new YakuException("双立直无法复合枪杠！（为什么呢？）");
            } else if (isYifa && isHaidi) {
                throw new YakuException("双立直无法一发自摸海底！");
            }
            this.yakus.add(12);
            this.yakus.remove(new Integer(1));
            this.fan += 1;
        }
    }

    //   13 - 七对子
    private void checkYaku13() {
        if (type == 1) {
            this.yakus.add(13);
            fan += 2;
        }
    }

    //   14 - 一气
    private void checkYaku14() {
        if (type != 0) return;
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
        if (type != 0) return;
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
        if (type != 0) return;
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
        if (type != 0) return;
        for (int i = 0; i < mianzis.size(); i++) {
            for (int j = i + 1; j < mianzis.size(); j++) {
                for (int k = j + 1; k < mianzis.size(); k++) {
                    Mianzi a = mianzis.get(i), b = mianzis.get(j), c = mianzis.get(k);
                    if (a.startTile % 10 == b.startTile % 10 && a.startTile % 10 == c.startTile % 10
                            && a.type >= 1 && b.type >= 1 && c.type >= 1) {
                        int[] array = {a.startTile / 10, b.startTile / 10, c.startTile / 10};
                        Arrays.sort(array);
                        if (Arrays.equals(array, new int[] {1, 2, 3})) {
                            yakus.add(17);
                            fan += 2;
                        }
                    }
                }
            }
        }
    }

    //   18 - 三暗
    private void checkYaku18() {
        if (type != 0) return;
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
        if (type != 0) return;
        for (Mianzi m : mianzis) {
            if (m.type == 0) return;
        }
        yakus.add(19);
        fan += 2;
    }

    //   20 - 小三
    private void checkYaku20() {
            if (type != 0) return;
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
        if (type == 1) fan += 2;
    }

    //   22 - 三杠子
    private void checkYaku22() {
        if (type != 0) return;
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
        if (type != 0) return;
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
        if (type != 0) return;
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
        if (type == 2) {
            yakuman += 1;
            yakumans.add(27);
        }
    }

    //   28 - 大三元
    private void checkYaku28() {
        if (type != 0) return;
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
                            return;
                        }
                    }
                }
            }
        }
    }

    //   29 - 四暗刻
    private void checkYaku29() {
        if (type != 0) return;
        Mianzi a = mianzis.get(0);
        Mianzi b = mianzis.get(1);
        Mianzi c = mianzis.get(2);
        Mianzi d = mianzis.get(3);
        if (a.type >= 1 && b.type >= 1 && c.type >= 1 && d.type >= 1 &&
                a.status == 0 && b.status == 0 && c.status == 0 && d.status == 0) {
            if (ronType == 1 && isTsumo) {
                yakuman += 1;
                yakumans.add(29);
            }
        }
    }

    //   30 - 小四喜
    private void checkYaku30() {
        if (type != 0) return;
        for (int i = 0; i < mianzis.size(); i++) {
            for (int j = i + 1; j < mianzis.size(); j++) {
                for (int k = j + 1; k < mianzis.size(); k++) {
                    Mianzi a = mianzis.get(i), b = mianzis.get(j), c = mianzis.get(k);
                    if (a.type >= 1 && b.type >= 1 && c.type >= 1) {
                        int[] array = {a.startTile, b.startTile, c.startTile, duizi};
                        Arrays.sort(array);
                        if (Arrays.equals(array, new int[] {41, 42, 43, 44})) {
                            yakuman += 1;
                            yakumans.add(30);
                            return;
                        }
                    }
                }
            }
        }
    }

    //   31 - 字一色
    private void checkYaku31() {
        for (int tile : tiles) {
            if (tile < 40) return;
        }
        yakuman += 1;
        yakumans.add(31);
    }

    //   32 - 绿一色
    private void checkYaku32() {
        if (type != 0) return;
        for (int tile : tiles) {
            if (tile != 32 && tile != 33 && tile != 34 && tile != 36 && tile != 38 && tile != 46) return;
        }
        yakuman += 1;
        yakumans.add(32);
    }

    //   33 - 清老头
    private void checkYaku33() {
        if (type != 0) return;
        for (int tile : tiles) {
            if (tile % 10 != 1 && tile % 10 != 9 || tile > 40) return;
        }
        yakuman += 1;
        yakumans.add(33);
    }

    //   34 - 九莲宝灯
    private void checkYaku34() {

    }

    //   35 - 四杠子
    private void checkYaku35() {
        if (type != 0) return;
        for (Mianzi m : mianzis) {
            if (m.type != 2) return;
        }
        yakuman += 1;
        yakumans.add(35);
    }

    //   36 - 天和
    private void checkYaku36() throws YakuException {
        if (isTianhe) {
            if (!isInner) {
                throw new YakuException("天和必须门前清！");
            } else if (selfWind != 0) {
                throw new YakuException("只有亲家才能天和！");
            } else if (!isTsumo) {
                throw new YakuException("天和必须自摸！");
            } else if (reached) {
                throw new YakuException("天和情况下不应立直！");
            } else {
                for (Mianzi m : mianzis) {
                    if (m.type == 2) throw new YakuException("天和情况下无法暗杠！");
                }
            }
            yakuman += 1;
            yakumans.add(36);
        }
    }

    //   37 - 地和
    private void checkYaku37() throws YakuException {
        if (isDihe) {
            if (!isInner) {
                throw new YakuException("地和必须门前清！");
            } else if (selfWind == 0) {
                throw new YakuException("只有子家才能地和！");
            } else if (!isTsumo) {
                throw new YakuException("地和必须自摸！");
            } else if (reached) {
                throw new YakuException("地和情况下不应立直！");
            } else if (isTianhe) {
                throw new YakuException("天地和无法复合！");
            } else {
                for (Mianzi m : mianzis) {
                    if (m.type == 2) throw new YakuException("地和情况下无法暗杠！");
                }
            }
            yakuman += 1;
            yakumans.add(37);
        }
    }

    //   38 - 国士无双十三面
    private void checkYaku38() {
        if (type != 2) return;
        int a = 0;
        for (int tile : tiles) {
            if (ronTile == tile) a += 1;
        }
        if (a == 2) {
            yakumans.remove(new Integer(27));
            yakumans.add(38);
            yakuman += 1;
        }
    }

    //   39 - 大四喜
    private void checkYaku39() {
        if (type != 0) return;
        Mianzi a = mianzis.get(0);
        Mianzi b = mianzis.get(1);
        Mianzi c = mianzis.get(2);
        Mianzi d = mianzis.get(3);
        int[] e = {a.startTile, b.startTile, c.startTile, d.startTile};
        Arrays.sort(e);
        if (Arrays.equals(e, new int[] {41, 42, 43, 44})) {
            yakumans.add(39);
            yakuman += 2;
        }
    }

    //   40 - 四暗刻单骑
    private void checkYaku40() {
        if (type != 0) return;
        Mianzi a = mianzis.get(0);
        Mianzi b = mianzis.get(1);
        Mianzi c = mianzis.get(2);
        Mianzi d = mianzis.get(3);
        if (a.type >= 1 && b.type >= 1 && c.type >= 1 && d.type >= 1 &&
                a.status == 0 && b.status == 0 && c.status == 0 && d.status == 0) {
            if (ronType == 2) {
                yakumans.add(40);
                yakuman += 2;
            }
        }
    }

    //   41 - 纯正九莲宝灯
    private void checkYaku41() {
        // ...
    }

    //   42 - 宝牌计算
    private void checkDora() {
        if (fan == 0) return;
        int _d = 0, _ad = aka, _id = 0;
        for (int tile : tiles) {
            for (int d: dora) {
                if (tile == d) _d += 1;
            }
            if (reached) {
                for (int id: innerDora) {
                    if (tile == id) _id += 1;
                }
            }
        }
        fan += _d + _ad + _id;
        if (_d != 0)  yakus.add(100 + _d);
        if (_ad != 0) yakus.add(200 + _ad);
        if (_id != 0) yakus.add(300 + _id);
    }
}
