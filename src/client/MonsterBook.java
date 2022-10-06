package client;

import client.inventory.Equip;
import client.inventory.MapleInventoryType;
import constants.GameConstants;
import database.DatabaseConnection;
import handling.RecvPacketOpcode;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
import java.util.Map.Entry;
import server.MapleItemInformationProvider;
import server.quest.MapleQuest;
import tools.Pair;
import tools.Triple;
import tools.data.MaplePacketLittleEndianWriter;
import tools.packet.CField.EffectPacket;

public final class MonsterBook implements Serializable {

    private static final long serialVersionUID = 7179541993413738569L;
    private boolean changed = false;
    private int currentSet = -1, level = 0, setScore, finishedSets;
    private Map<Integer, Integer> cards;
    private List<Integer> cardItems = new ArrayList<>();
    private Map<Integer, Pair<Integer, Boolean>> sets = new HashMap<>();

    public MonsterBook(Map<Integer, Integer> cards, MapleCharacter chr) {
        this.cards = cards;
        calculateItem();
        calculateScore();

        MapleQuestStatus stat = chr.getQuestNoAdd(MapleQuest.getInstance(GameConstants.CURRENT_SET));
        if (stat != null && stat.getCustomData() != null) {
            currentSet = Integer.parseInt(stat.getCustomData());
            if (!sets.containsKey(currentSet) || !sets.get(currentSet).right) {
                currentSet = -1;
            }
        }
        applyBook(chr, true);
    }

    public void applyBook(MapleCharacter chr, boolean first_login) {
        if (GameConstants.GMS) {
            Equip item = (Equip) chr.getInventory(MapleInventoryType.EQUIPPED).getItem((short) -55);
            if (item == null) {
                item = (Equip) MapleItemInformationProvider.getInstance().getEquipById(1172000);
                item.setPosition((short) -55);
            }
            modifyBook(item);
            if (first_login) {
                chr.getInventory(MapleInventoryType.EQUIPPED).addFromDB(item);
            } else {
                chr.forceReAddItem_Book(item, MapleInventoryType.EQUIPPED);
                chr.equipChanged();
            }
        }
    }

    public byte calculateScore() {
        byte returnval = 0;
        sets.clear();
        int oldLevel = level, oldSetScore = setScore;
        setScore = 0;
        finishedSets = 0;
        final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        for (int i : cardItems) {
            //we need the card id but we store the mob id lol
            final Integer x = ii.getSetId(i);
            if (x != null && x > 0) {
                final Triple<Integer, List<Integer>, List<Integer>> set = ii.getMonsterBookInfo(x);
                if (set != null) {
                    if (!sets.containsKey(x)) {
                        sets.put(x, new Pair<>(1, Boolean.FALSE));
                    } else {
                        sets.get(x).left++;
                    }
                    if (sets.get(x).left == set.mid.size()) {
                        sets.get(x).right = Boolean.TRUE;
                        setScore += set.left;
                        if (currentSet == -1) {
                            currentSet = x;
                            returnval = 2;
                        }
                        finishedSets++;
                    }
                }
            }
        }
        level = 10;
        for (byte i = 0; i < 10; i++) {
            if (GameConstants.getSetExpNeededForLevel(i) > setScore) {
                level = (byte) i;
                break;
            }
        }
        if (level > oldLevel) {
            returnval = 2;
        } else if (setScore > oldSetScore) {
            returnval = 1;
        }
        return returnval;
    }

    public void writeCharInfoPacket(MaplePacketLittleEndianWriter mplew) {
        //cid, then the character's level
        List<Integer> cardSize = new ArrayList<>(10); //0 = total, 1-9 = card types..
        for (int i = 0; i < 10; i++) {
            cardSize.add(0);
        }
        cardItems.forEach((x) -> {
            cardSize.set(0, cardSize.get(0) + 1);
            cardSize.set(((x / 1000) % 10) + 1, cardSize.get(((x / 1000) % 10) + 1) + 1);
        });
        cardSize.forEach((i) -> {
            mplew.writeInt(i);
        });
        mplew.writeInt(setScore);
        mplew.writeInt(currentSet);
        mplew.writeInt(finishedSets);
    }

    public void writeFinished(MaplePacketLittleEndianWriter mplew) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        mplew.write(1);
        mplew.writeShort(cardItems.size());
        final List<Integer> mbList = new ArrayList<>(ii.getMonsterBookList());
        Collections.sort(mbList);
        final int fullCards = (mbList.size() / 8) + (mbList.size() % 8 > 0 ? 1 : 0);
        mplew.writeShort(fullCards); //which cards of all you have; more efficient than writing each card

        for (int i = 0; i < fullCards; i++) {
            int currentMask = 0x1, maskToWrite = 0;
            for (int y = (i * 8); y < ((i * 8) + 8); y++) {
                if (mbList.size() <= y) {
                    break;
                }
                if (cardItems.contains(mbList.get(y))) {
                    maskToWrite |= currentMask;
                }
                currentMask *= 2;
            }
            mplew.write(maskToWrite);
        }

        final int fullSize = (cardItems.size() / 2) + (cardItems.size() % 2 > 0 ? 1 : 0);
        mplew.writeShort(fullSize); //i honestly don't know the point of this, is it to signify yes/no if you have the card or not?... which is already done...?
        for (int i = 0; i < fullSize; i++) {
            mplew.write(i == (cardItems.size() / 2) ? 1 : 0x11);
        }
    }

    public void writeUnfinished(MaplePacketLittleEndianWriter mplew) {
        mplew.write(0);
        mplew.writeShort(cardItems.size());
        cardItems.stream().map((i) -> {
            mplew.writeShort(i % 10000);
            return i;
        }).forEachOrdered((_item) -> {
            mplew.write(1); //whether you have the card or not? idk
        });
    }

    public void calculateItem() {
        cardItems.clear();
        cards.entrySet().forEach((s) -> {
            addCardItem(s.getKey(), s.getValue());
        });
    }

    public void addCardItem(int key, int value) {
        if (value >= 2) {
            Integer x = MapleItemInformationProvider.getInstance().getItemIdByMob(key);
            if (x != null && x > 0) {
                cardItems.add(x);
            }
        }
    }

    public void modifyBook(Equip eq) {
        eq.setStr((short) level);
        eq.setDex((short) level);
        eq.setInt((short) level);
        eq.setLuk((short) level);
        eq.setPotential1(0);
        eq.setPotential2(0);
        eq.setPotential3(0);
        eq.setPotential4(0);
        eq.setPotential5(0);
        if (currentSet > -1) {
            final Triple<Integer, List<Integer>, List<Integer>> set = MapleItemInformationProvider.getInstance().getMonsterBookInfo(currentSet);
            if (set != null) {
                OUTER:
                for (int i = 0; i < set.right.size(); i++) {
                    switch (i) {
                        case 0:
                            eq.setPotential1(set.right.get(i));
                            break;
                        case 1:
                            eq.setPotential2(set.right.get(i));
                            break;
                        case 2:
                            eq.setPotential3(set.right.get(i));
                            break;
                        case 3:
                            eq.setPotential4(set.right.get(i));
                            break;
                        case 4:
                            eq.setPotential5(set.right.get(i));
                            break OUTER;
                        default:
                            break;
                    }
                }
            } else {
                currentSet = -1;
            }
        }
    }

    public int getSetScore() {
        return setScore;
    }

    public int getLevel() {
        return level;
    }

    public int getSet() {
        return currentSet;
    }

    public boolean changeSet(int c) {
        if (sets.containsKey(c) && sets.get(c).right) {
            this.currentSet = c;
            return true;
        }
        return false;
    }

    public void changed() {
        changed = true;
    }

    public Map<Integer, Integer> getCards() {
        return cards;
    }

    public final int getSeen() {
        return cards.size();
    }

    public final int getCaught() {
        int ret = 0;
        ret = cards.values().stream().filter((i) -> (i >= 2)).map((_item) -> 1).reduce(ret, Integer::sum);
        return ret;
    }

    public final int getLevelByCard(final int cardid) {
        return cards.get(cardid) == null ? 0 : cards.get(cardid);
    }

    public static MonsterBook loadCards(final int charid, final MapleCharacter chr) throws SQLException {
        Map<Integer, Integer> cards;
        try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM monsterbook WHERE charid = ? ORDER BY cardid ASC")) {
            ps.setInt(1, charid);
            try (ResultSet rs = ps.executeQuery()) {
                cards = new LinkedHashMap<>();
                while (rs.next()) {
                    cards.put(rs.getInt("cardid"), rs.getInt("level"));
                }
            }
        }
        return new MonsterBook(cards, chr);
    }

    public final void saveCards(final int charid) throws SQLException {
        if (!changed) {
            return;
        }
        final Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps = con.prepareStatement("DELETE FROM monsterbook WHERE charid = ?");
        ps.setInt(1, charid);
        ps.execute();
        ps.close();
        changed = false;
        if (cards.isEmpty()) {
            return;
        }

        boolean first = true;
        final StringBuilder query = new StringBuilder();

        for (final Entry<Integer, Integer> all : cards.entrySet()) {
            if (first) {
                first = false;
                query.append("INSERT INTO monsterbook VALUES (DEFAULT,");
            } else {
                query.append(",(DEFAULT,");
            }
            query.append(charid);
            query.append(",");
            query.append(all.getKey()); // Card ID
            query.append(",");
            query.append(all.getValue()); // Card level
            query.append(")");
        }
        ps = con.prepareStatement(query.toString());
        ps.execute();
        ps.close();
    }

    public final boolean monsterCaught(final MapleClient c, final int cardid, final String cardname) {
        if (!cards.containsKey(cardid) || cards.get(cardid) < 2) {
            changed = true;
            c.getPlayer().dropMessage(-6, "怪物卡片收集冊更新 - " + cardname);
            c.getSession().write(EffectPacket.showForeignEffect(16));
            cards.put(cardid, 2);
            if (GameConstants.GMS) {
                if (c.getPlayer().getQuestStatus(50195) != 1) {
                    MapleQuest.getInstance(50195).forceStart(c.getPlayer(), 9010000, "1"); //this quest signifies that a card is done
                }
                if (c.getPlayer().getQuestStatus(50196) != 1) {
                    MapleQuest.getInstance(50196).forceStart(c.getPlayer(), 9010000, "1"); //this quest signifies something
                }
                addCardItem(cardid, 2);
                byte rr = calculateScore();
                if (rr > 0) {
                    if (c.getPlayer().getQuestStatus(50197) != 1) {
                        MapleQuest.getInstance(50197).forceStart(c.getPlayer(), 9010000, "1"); //this quest signifies that a set is done
                    }
                    c.getSession().write(EffectPacket.showForeignEffect(RecvPacketOpcode.MCAUGHTEFF.getValue()));
                    if (rr > 1) {
                        applyBook(c.getPlayer(), false);
                    }
                }
            }
            return true;
        }
        return false;
    }

    public boolean hasCard(int cardid) {
        return cardItems == null ? false : cardItems.contains(cardid);
    }

    public final void monsterSeen(final MapleClient c, final int cardid, final String cardname) {
        if (cards.containsKey(cardid)) {
            return;
        }
        changed = true;
        // New card
        c.getPlayer().dropMessage(-6, "New book entry - " + cardname);
        cards.put(cardid, 1);
        c.getSession().write(EffectPacket.showForeignEffect(16));
    }
}
