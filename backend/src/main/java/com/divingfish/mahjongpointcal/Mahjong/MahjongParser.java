package com.divingfish.mahjongpointcal.Mahjong;

import com.divingfish.mahjongpointcal.Exceptions.MahjongNumberException;
import com.divingfish.mahjongpointcal.Exceptions.MahjongStringException;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

public class MahjongParser {
    private int aka;
    private Set<MahjongGroup> mahjongGroups;

    private void merge(List<Integer> src, List<Integer> desc, int type) {
        for (int i : src) {
            desc.add(type * 10 + i);
        }
    }

    public List<Integer> seqToList(String sequence) throws MahjongStringException {
        List<Integer> manns = new ArrayList<>();
        List<Integer> pinns = new ArrayList<>();
        List<Integer> sous = new ArrayList<>();
        List<Integer> characters = new ArrayList<>();
        List<Integer> temp = new ArrayList<>();
        for (int i = 0; i < sequence.length(); i++) {
            char c = sequence.charAt(i);
            switch (c) {
                case 'm': {
                    merge(temp, manns, 1);
                    temp = new ArrayList<>();
                    break;
                }
                case 'p': {
                    merge(temp, pinns, 2);
                    temp = new ArrayList<>();
                    break;
                }
                case 's': {
                    merge(temp, sous, 3);
                    temp = new ArrayList<>();
                    break;
                }
                case 'z': {
                    merge(temp, characters, 4);
                    temp = new ArrayList<>();
                    break;
                }
                case '0':
                case '1':
                case '2':
                case '3':
                case '4':
                case '5':
                case '6':
                case '7':
                case '8':
                case '9': {
                    temp.add(c - 48);
                    break;
                }
                default:
                    throw new MahjongStringException();
            }
        }
        manns.addAll(pinns);
        manns.addAll(sous);
        manns.addAll(characters);
        return manns;
    }

    private List<Mianzi> outerToMianzi(List<String> outer) throws MahjongStringException {
        List<Mianzi> mianzis = new ArrayList<>();
        for (String s : outer) {
            List<Integer> list = seqToList(s);
            Collections.sort(list);
            if (list.size() == 3) {
                int aka = 0;
                if (list.get(0) % 10 == 0) {
                    aka = list.get(0) / 10;
                    list.add(list.get(0) + 5);
                    list.remove(0);
                }
                Collections.sort(list);
                if (list.get(0) == list.get(1) - 1 && list.get(0) == list.get(2) - 2 && list.get(0) < 40) {
                    mianzis.add(new Mianzi(list.get(0), 1, 0, aka));
                } else if (list.get(0).equals(list.get(1)) && list.get(0).equals(list.get(2))) {
                    mianzis.add(new Mianzi(list.get(0), 1, 1, aka));
                } else {
                    throw new MahjongStringException();
                }
            } else if (list.size() == 4) {
                if (list.get(0) % 10 == 0 && list.get(1) % 10 != 0) {
                    aka = list.get(0) / 10;
                    list.add(list.get(0) + 5);
                    list.remove(0);
                }
                if (list.get(0).equals(list.get(1)) && list.get(0).equals(list.get(2)) && list.get(0).equals(list.get(3))) {
                    mianzis.add(new Mianzi(list.get(0), 1, 2, (list.get(0) <= 35 && list.get(2) % 10 == 5) ? list.get(0) / 10 : 0));
                } else if (list.get(0) % 10 == 0 && list.get(0).equals(list.get(1)) && list.get(2).equals(list.get(3))) {
                    mianzis.add(new Mianzi(list.get(2), 0, 2, (list.get(2) <= 35 && list.get(2) % 10 == 5) ? list.get(2) / 10 : 0));
                } else {
                    throw new MahjongStringException();
                }
            }
        }
        return mianzis;
    }

    public int getDoraIndicate(int dora) {
        if (dora <= 39 && dora >= 11) {
            return (dora % 10 == 1) ? dora + 8 : dora - 1;
        }
        if (dora <= 44 && dora >= 41) {
            return (dora == 41) ? dora + 3 : dora - 1;
        }
        if (dora <= 47 && dora >= 45) {
            return (dora == 45) ? dora + 2 : dora - 1;
        }
        return 0;
    }

    public MahjongParser(String innerSequence, List<String> outerSequence, String ronTile, String dora, String innerDora, int north, boolean reached, boolean isTsumo) throws MahjongStringException {
        List<Integer> temp = seqToList(ronTile);
        if (temp.size() != 1) throw new MahjongStringException();
        int _ronTile = temp.get(0);

        List<Integer> doras = seqToList(dora);
        if (doras.size() > 5) throw new MahjongStringException();

        List<Integer> innerDoras = seqToList(innerDora);
        if (innerDoras.size() > 5) throw new MahjongStringException();

        if (north > 4 || north < 0) throw new MahjongStringException();

        List<Integer> otherTails = new ArrayList<>();

        for (Integer i: doras) {
            otherTails.add(getDoraIndicate(i));
        }

        for (Integer i: innerDoras) {
            otherTails.add(getDoraIndicate(i));
        }

        for (int i = 0; i < north; i++) {
            otherTails.add(44);
        }

        MahjongTransfer mahjongTransfer;
        try {
            mahjongTransfer = new MahjongTransfer(seqToList(innerSequence), outerToMianzi(outerSequence), _ronTile, otherTails);
        } catch (MahjongNumberException e) {
            throw new MahjongStringException();
        }
        Set<MahjongGroup> mahjongGroups = mahjongTransfer.toMahjongGroup();
        mahjongGroups.forEach(k -> {
            k.dora = doras;
            k.innerDora = innerDoras;
            k.northDora = north;
            k.reached = reached;
            k.isTsumo = isTsumo;
        });
        this.mahjongGroups = mahjongGroups;
    }

    public Set<MahjongGroup> getMahjongGroups() {
        return mahjongGroups;
    }
}
