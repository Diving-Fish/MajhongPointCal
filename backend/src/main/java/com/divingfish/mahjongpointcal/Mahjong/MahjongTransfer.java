package com.divingfish.mahjongpointcal.Mahjong;

import com.divingfish.mahjongpointcal.Exceptions.MahjongNumberException;
import com.divingfish.mahjongpointcal.Exceptions.TileSplitException;

import java.util.*;

public class MahjongTransfer {
    private List<Integer> innerTiles;
    private List<Integer> otherTiles; // dora indicate tile.
    private List<Mianzi> outerTiles;
    private List<Integer> consumedTiles;
    private int ronTile;
    private List<Integer> manns;
    private List<Integer> pinns;
    private List<Integer> sous;
    private List<Integer> characters;
    private int aka;
    private Set<Integer> akaset; // 1: 0m,  2: 0p,   3: 0s

    public MahjongTransfer(List<Integer> innerTiles, List<Mianzi> outerTiles, int ronTile, List<Integer> otherTiles) throws MahjongNumberException {
        if (innerTiles.size() + outerTiles.size() * 3 != 14 || innerTiles.indexOf(ronTile) == -1) {
            throw new MahjongNumberException();
        }
        this.akaset = new HashSet<>();
        this.innerTiles = innerTiles;
        this.outerTiles = outerTiles;
        this.otherTiles = otherTiles;
        this.ronTile = (ronTile % 10 == 0) ? ronTile + 5 : ronTile;
        checkTiles();
        manns = new ArrayList<>();
        pinns = new ArrayList<>();
        sous = new ArrayList<>();
        characters = new ArrayList<>();
        for (Integer t : this.innerTiles) {
            if (t >= 11 && t <= 19) {
                manns.add(t);
            } else if (t >= 21 && t <= 29) {
                pinns.add(t);
            } else if (t >= 31 && t <= 39) {
                sous.add(t);
            } else if (t >= 41 && t <= 47) {
                characters.add(t);
            }
        }
    }

    private void addAka(int akano) throws MahjongNumberException {
        if (akaset.contains(akano)) throw new MahjongNumberException();
        akaset.add(akano);
        this.aka += 1;
    }

    private void checkTiles() throws MahjongNumberException {
        for (int i = 0; i < innerTiles.size(); i++) {
            if (innerTiles.get(i) % 10 == 0) {
                int akano = innerTiles.get(i) / 10;
                addAka(akano);
                innerTiles.remove(innerTiles.get(i));
                innerTiles.add(akano * 10 + 5);
            }
        }
        List<Integer> list = new ArrayList<>(innerTiles);
        for (Mianzi m : outerTiles) {
            if (m.aka != 0) addAka(m.aka);
            list.addAll(m.toList());
        }
        list.addAll(otherTiles);
        consumedTiles = list;
        Map<Integer, Integer> map = arrayToMap(list);
        for (Integer key : map.keySet()) {
            if (map.get(key) > 4) throw new MahjongNumberException();
        }
    }

    public Set<List<Integer>> possibles(List<Integer> pons) {
        Set<List<Integer>> set = new HashSet<>();
        if (pons.size() == 0) {
            set.add(new ArrayList<Integer>());
            return set;
        }
        for (int i = 0; i < pons.size(); i++) {
            List<Integer> remain = pons.subList(i + 1, pons.size());
            for (List<Integer> list : possibles(remain)) {
                ArrayList<Integer> list1 = new ArrayList<>(list);
                set.add(list1);
                list.add(0, pons.get(i));
                set.add(list);
            }
        }
        return set;
    }

    private Map<Integer, Integer> arrayToMap(List<Integer> tiles) {
        Map<Integer, Integer> map = new HashMap<>();
        tiles.forEach(i ->
                map.merge(i, 1, (prev, one) -> prev + one)
        );
        return map;
    }

    private boolean subShunzi(List<Integer> tiles, int index) {
        int a = tiles.get(index);
        return !((tiles.indexOf(a+1) == -1) || (tiles.indexOf(a+2) == -1));
    }

    public ArrayList<Integer> arrangeShunzi(List<Integer> tiles) throws TileSplitException {
        ArrayList<Integer> tilesCopy = new ArrayList<>(tiles);
        Collections.sort(tilesCopy);
        ArrayList<Integer> result = new ArrayList<>();
        while (true) {
            if (tilesCopy.size() == 0) {
                return result;
            } else if (tilesCopy.size() >= 3) {
                int a;
                if (subShunzi(tilesCopy, 0)) {
                    a = tilesCopy.get(0);
                } else {
                    throw new TileSplitException();
                }
                result.add(a);
                result.add(a+1);
                result.add(a+2);
                tilesCopy.remove(new Integer(a));
                tilesCopy.remove(new Integer(a+1));
                tilesCopy.remove(new Integer(a+2));
            } else {
                throw new TileSplitException();
            }
        }
    }

    private Set<List<Integer>> arrangeRemain(List<Integer> tiles) {
        Set<List<Integer>> set = new HashSet<>();
        if (tiles.size() % 3 == 2) {
            Map<Integer, Integer> map = arrayToMap(tiles);
            for (Integer key : map.keySet()) {
                List<Integer> tilesCopy = new ArrayList<>(tiles);
                if (map.get(key) >= 2) {
                    tilesCopy.remove(key);
                    tilesCopy.remove(key);
                    List<Integer> re;
                    try {
                        re = arrangeShunzi(tilesCopy);
                    } catch (TileSplitException e) {
                        continue;
                    }
                    re.add(key);
                    re.add(key);
                    set.add(re);
                }
            }
        } else {
            try {
                set.add(arrangeShunzi(tiles));
            } catch (TileSplitException e) {

            }
        }
        return set;
    }

    // Format: [11, 12, 13, 12, 13, 14, 12, 12]
    public Set<List<Integer>> splitTiles(List<Integer> tiles) {
        // check ponns count
        Set<List<Integer>> result = new HashSet<>();
        if (tiles.size() == 0) {
            result.add(new ArrayList<>());
            return result;
        }
        Map<Integer, Integer> map = arrayToMap(tiles);
        ArrayList<Integer> ponns = new ArrayList<>();
        map.forEach((k, v) -> {
            if (v >= 3) {
                ponns.add(k);
            }
        });
        Set<List<Integer>> possSet = possibles(ponns);
        for (List<Integer> poss: possSet) {
            ArrayList<Integer> splitTiles = new ArrayList<>();
            ArrayList<Integer> tilesCopy = new ArrayList<>(tiles);
            for (Integer i: poss) {
                for (int k = 0; k < 3; k++) {
                    tilesCopy.remove(i);
                    splitTiles.add(i);
                }
            }
            Set<List<Integer>> set = arrangeRemain(tilesCopy);
            List<Integer> splitTilesCopy;
            for (List<Integer> list : set) {
                splitTilesCopy = new ArrayList<>(splitTiles);
                splitTilesCopy.addAll(list);
                result.add(splitTilesCopy);
            }
        }
        return result;
    }

    private List<Integer> concat(List<Integer> a, List<Integer> b) {
        if (a.size() % 3 == 2) {
            b.addAll(a.subList(a.size() -2, a.size()));
            a.remove(a.size() - 1);
            a.remove(a.size() - 1);
        }
        a.addAll(b);
        return a;
    }

    private Set<List<Integer>> genTiles(List<Integer> addTiles, Set<List<Integer>> prev) {
        Set<List<Integer>> result = new HashSet<>();
        Set<List<Integer>> temp = splitTiles(addTiles);
        for (List<Integer> a: temp) {
            for (List<Integer> b: prev) {
                result.add(concat(new ArrayList<>(b), new ArrayList<>(a)));
            }
        }
        return result;
    }

    private Set<List<Integer>> finalTiles() {
        Set<List<Integer>> result;
        result = splitTiles(manns);
        result = genTiles(pinns, result);
        result = genTiles(sous, result);
        result = genTiles(characters, result);
        return result;
    }

    private MahjongGroup sevenPairs() {
        if (innerTiles.size() != 14) return null;
        Map<Integer, Integer> map = arrayToMap(innerTiles);
        for (int a : map.keySet()) {
            if (map.get(a) != 2) return null;
        }
        MahjongGroup mahjongGroup = new MahjongGroup();
        mahjongGroup.type = 1;
        mahjongGroup.tiles = new ArrayList<>(innerTiles);
        Collections.sort(mahjongGroup.tiles);
        return mahjongGroup;
    }

    public MahjongGroup thirteenOrphans() {
        if (innerTiles.size() != 14) return null;
        List<Integer> tiles = Arrays.asList(new Integer[]{11, 19, 21, 29, 31, 39, 41, 42, 43, 44, 45, 46, 47});
        List<Integer> innerCopy = new ArrayList<>(innerTiles);
        for (Integer tile : tiles) {
            innerCopy.remove(tile);
        }
        if (innerCopy.size() != 1) return null;
        if (tiles.indexOf(innerCopy.get(0)) == -1) return null;
        MahjongGroup mahjongGroup = new MahjongGroup();
        mahjongGroup.type = 2;
        mahjongGroup.tiles = new ArrayList<>(innerTiles);
        Collections.sort(mahjongGroup.tiles);
        return mahjongGroup;
    }

    public Set<MahjongGroup> toMahjongGroup() {
        Set<MahjongGroup> mahjongGroups = new HashSet<>();
        MahjongGroup sevenPairs = sevenPairs();
        if (sevenPairs != null) {
            sevenPairs.ronTile = ronTile;
            sevenPairs.aka = aka;
            sevenPairs.consumedTiles = consumedTiles;
            mahjongGroups.add(sevenPairs);
        }
        MahjongGroup thirteenOrphans = thirteenOrphans();
        if (thirteenOrphans != null) {
            thirteenOrphans.ronTile = ronTile;
            thirteenOrphans.aka = aka;
            thirteenOrphans.consumedTiles = consumedTiles;
            mahjongGroups.add(thirteenOrphans);
        }
        Set<List<Integer>> innerLists = finalTiles();
        for (List<Integer> list : innerLists) {
            MahjongGroup mahjongGroup = new MahjongGroup();
            int length = list.size() / 3;
            mahjongGroup.duizi = list.get(list.size() - 1);
            for (int j = 0; j < length; j++) {
                int index = j * 3;
                if (list.get(index).equals(list.get(index + 1))) {
                    mahjongGroup.mianzis.add(new Mianzi(list.get(index), 0, 1));
                } else {
                    mahjongGroup.mianzis.add(new Mianzi(list.get(index), 0, 0));
                }
            }
            mahjongGroup.mianzis.addAll(outerTiles);
            mahjongGroup.ronTile = ronTile;
            mahjongGroup.aka = this.aka;
            mahjongGroup.consumedTiles = consumedTiles;
            mahjongGroups.add(mahjongGroup);
        }
        return mahjongGroups;
    }
}