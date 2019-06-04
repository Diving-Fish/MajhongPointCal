package com.divingfish.mahjongpointcal.Mahjong;

import java.util.ArrayList;
import java.util.List;

public class Mianzi {
    public int startTile;
    public int status; // 0: inner, 1: outer
    public int type; // 0: chi 1: pon 2: kan
    public int aka; // 0: no aka  1: 0m   2:  0p   3:  0s
    public List<Integer> list;

    public Mianzi(int startTile, int status, int type) {
        this.status = status;
        this.type = type;
        this.startTile = startTile;
        this.aka = 0;
        this.list = toList();
    }


    public Mianzi(int startTile, int status, int type, int aka) {
        this.status = status;
        this.type = type;
        this.startTile = startTile;
        this.aka = aka;
        this.list = toList();
    }

    public List<Integer> toList() {
        List<Integer> a = new ArrayList<>();
        if (type == 0) {
            a.add(startTile);
            a.add(startTile+1);
            a.add(startTile+2);
        } else if (type == 1) {
            for (int i = 0; i < 3; i++) a.add(startTile);
        } else if (type == 2) {
            for (int i = 0; i < 4; i++) a.add(startTile);
        }
        return a;
    }
}
