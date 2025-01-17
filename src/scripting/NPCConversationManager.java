package scripting;

import client.*;
import client.inventory.*;
import constants.GameConstants;
import constants.Occupations;
import constants.ServerConstants;
import database.DatabaseConnection;
import handling.channel.ChannelServer;
import handling.channel.MapleGuildRanking;
import handling.channel.handler.HiredMerchantHandler;
import handling.channel.handler.PlayersHandler;
import handling.login.LoginInformationProvider;
import handling.login.LoginServer;
import handling.world.MapleParty;
import handling.world.World;
import handling.world.exped.ExpeditionType;
import handling.world.guild.MapleGuild;
import handling.world.guild.MapleGuildAlliance;
import java.awt.Point;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.NumberFormat;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.regex.Pattern;
import javax.script.Invocable;
import server.*;
import server.Timer.WorldTimer;
import server.life.MapleLifeFactory;
import server.life.MapleMonster;
import server.life.MapleMonsterInformationProvider;
import server.life.MapleNPC;
import server.life.MonsterDropEntry;
import server.maps.Event_DojoAgent;
import server.maps.Event_PyramidSubway;
import server.maps.MapleMap;
import server.maps.MapleMapObject;
import server.maps.MapleMapObjectType;
import server.quest.MapleQuest;
import tools.FileoutputUtil;
import tools.StringUtil;
import tools.Triple;
import tools.packet.CField;
import tools.packet.CField.NPCPacket;
import tools.packet.CField.UIPacket;
import tools.packet.CWvsContext;
import tools.packet.CWvsContext.GuildPacket;
import tools.packet.CWvsContext.InfoPacket;

public class NPCConversationManager extends AbstractPlayerInteraction {

    private String getText;
    private byte type; // -1 = NPC, 0 = start quest, 1 = end quest
    private byte lastMsg = -1;
    public boolean pendingDisposal = false;
    private Invocable iv;
    private int npcMode = 0;
    private String Script;

    public int getNpcMode() {
        return npcMode;
    }

    public NPCConversationManager(MapleClient c, int npc, int questid, int itemid, byte type, int npcMode, String Script, Invocable iv) {
        super(c, npc, questid, itemid);
        this.type = type;
        this.npcMode = npcMode;
        this.iv = iv;
        this.Script = Script;
    }

    public NPCConversationManager(MapleClient c, int npc, int questid, int itemid, byte type, int npcMode, Invocable iv) {
        super(c, npc, questid, itemid);
        this.type = type;
        this.npcMode = npcMode;
        this.iv = iv;
    }

    public NPCConversationManager(MapleClient c, int npc, int questid, byte type, Invocable iv) {
        super(c, npc, questid);
        this.type = type;
        this.iv = iv;

    }

    public String getScript() {
        return Script;
    }

    public boolean getVip() {
        return getPlayer().isGM();
    }

    public String getJobNameById(int d) {
        return "";
    }

    public short getSpace(int type) {
        return getPlayer().getSpace(type);
    }

    public String getColor(int a) {
        return "灰卡";
    }

    public String getColor() {
        return "灰卡";
    }

    public String getMaplewinggp(String a, int b) {
        return "";
    }

    public String getMaplewinggp(int s) {
        return "";
    }

    public int getMaplewing(String te) {
        return getPlayer().getMaplewing(te);
    }

    public int getMaplewing(String te, int ids) {
        return getPlayer().getMaplewing(te, ids);
    }

    public boolean IsMaplewingGM() {
        return getPlayer().IsMaplewingGM();
    }

    public boolean IsMaplewing() {
        return getPlayer().IsMaplewing();
    }

    public String getVipname() {
        return getPlayer().getVipname();
    }

    public int getLevel() {
        return getPlayer().getLevel();
    }

    public final void maxSkillsByJob() {//棉被 貓咪:3 提供 以下為無效技能
        getPlayer().maxSkillsByJobN();
    }

    public final void maxAllSkills() {//棉被 貓咪:3 提供 以下為無效技能
        HashMap<Skill, SkillEntry> sa = new HashMap<>();
        SkillFactory.getAllSkills().forEach((skil) -> {
            if (((skil.getId() >= 74) && (skil.getId() <= 104)) || (skil.getId() == 180) || ((skil.getId() >= 1006) && (skil.getId() <= 1072) && (skil.getId() != 1026)) || ((skil.getId() >= 1081) && (skil.getId() <= 1189)) || ((skil.getId() >= 1203) && (skil.getId() <= 1214)) || ((skil.getId() >= 9000) && (skil.getId() <= 9002))
                    || ((skil.getId() >= 10001005) && (skil.getId() <= 10001179) && (skil.getId() != 10001026) && (skil.getId() != 10000018)) || ((skil.getId() >= 20011006) && (skil.getId() <= 20011179) && (skil.getId() != 20011026) && (skil.getId() != 20011024)) || ((skil.getId() >= 20021006) && (skil.getId() <= 20021179) && (skil.getId() != 20021026))
                    || ((skil.getId() >= 20001006) && (skil.getId() <= 20001179) && (skil.getId() != 20001026)) || ((skil.getId() >= 20031006) && (skil.getId() <= 20031179) && (skil.getId() != 20031026))
                    || ((skil.getId() >= 30001006) && (skil.getId() <= 30001179) && (skil.getId() != 30001024) && (skil.getId() != 30001026)) || ((skil.getId() >= 30011006) && (skil.getId() <= 30011179) && (skil.getId() != 30011026))
                    || ((skil.getId() >= 50001006) && (skil.getId() <= 50001179) && (skil.getId() != 50001026)) || ((skil.getId() >= 10009000) && (skil.getId() <= 10009002)) || ((skil.getId() >= 20009000) && (skil.getId() <= 20009002)) || ((skil.getId() >= 50009000) && (skil.getId() <= 50009002))
                    || ((skil.getId() >= 10000086) && (skil.getId() <= 10000180)) || ((skil.getId() >= 20000086) && (skil.getId() <= 20000180)) || ((skil.getId() >= 20010086) && (skil.getId() <= 20010180)) || ((skil.getId() >= 30000086) && (skil.getId() <= 30000180)) || ((skil.getId() >= 50000086) && (skil.getId() <= 50000180))
                    || ((skil.getId() >= 20000014) && (skil.getId() <= 20000018)) || ((skil.getId() >= 20019000) && (skil.getId() <= 20019002)) || (skil.getId() == 50001215) || ((skil.getId() >= 20020086) && (skil.getId() <= 20020180))
                    || ((skil.getId() >= 20030086) && (skil.getId() <= 20030180)) || ((skil.getId() >= 30010086) && (skil.getId() <= 30010180))
                    || ((skil.getId() >= 80000000) && (skil.getId() <= 80001002) && (skil.getId() != 80001000)) || ((skil.getId() >= 80001034) && (skil.getId() <= 80001036)) || ((skil.getId() >= 80001040) && (skil.getId() <= 80001044))
                    || ((skil.getId() >= 80001079) && (skil.getId() <= 80001087)) || ((skil.getId() >= 80001091) && (skil.getId() <= 80001108))
                    || ((skil.getId() >= 80001125) && (skil.getId() <= 80001126)) || ((skil.getId() >= 80001129) && (skil.getId() <= 80001130)) || ((skil.getId() >= 80001132) && (skil.getId() <= 80001151))
                    || (skil.getId() == 80001158) || (skil.getId() == 80001122) || (skil.getId() == 80001024)) {
            } else if (GameConstants.isApplicableSkill(skil.getId()) && skil.getId() < 90000000) {
                sa.put(skil, new SkillEntry((byte) skil.getMaxLevel(), (byte) skil.getMaxLevel(), SkillFactory.getDefaultSExpiry(skil)));
            }
        });
        getPlayer().changeSkillsLevel(sa);
    }

    public void setCData(int questid, int points) {
        final MapleQuestStatus record = getPlayer().getQuestNAdd(MapleQuest.getInstance(questid));

        if (record.getCustomData() != null) {
            record.setCustomData(String.valueOf(points + Integer.parseInt(record.getCustomData())));
        } else {
            record.setCustomData(String.valueOf(points)); // First time
        }
    }

    public String paiMing() {
        String result = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("select * from characters order by reborns desc limit 0, 10");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result = result + rs.getString("name") + " 轉生次數： " + rs.getInt("reborns") + " 等級： " + rs.getInt("level") + "\r\n";
            }

        } catch (SQLException ex) {
            return "";
        }
        return result;
    }

    public String PvPpaiMing() {
        String result = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("select * from characters order by pvpkills desc limit 0, 10");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result = result + rs.getString("name") + " 殺人次數： " + rs.getInt("pvpkills") + " 等級： " + rs.getInt("level") + "\r\n";
            }

        } catch (SQLException ex) {
            return "";
        }
        return result;
    }

    public String MesopaiMing() {
        String result = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("select * from characters order by meso desc limit 0, 10");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result = result + rs.getString("name") + " 金錢數量： " + rs.getInt("meso") + " 等級： " + rs.getInt("level") + "\r\n";
            }

        } catch (SQLException ex) {
            return "";
        }
        return result;
    }

    public String FamepaiMing() {
        String result = "";
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("select * from characters order by fame desc limit 0, 10");
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                result = result + rs.getString("name") + " 名聲排行： " + rs.getInt("fame") + " 等級： " + rs.getInt("level") + "\r\n";
            }

        } catch (SQLException ex) {
            return "";
        }
        return result;
    }

    public int getCData(MapleCharacter sai, int questid) {
        final MapleQuestStatus record = sai.getQuestNAdd(MapleQuest.getInstance(questid));
        if (record.getCustomData() != null) {
            return Integer.parseInt(record.getCustomData());
        }
        return 0;

    }

    public boolean isExist(int id) {
        //Simpleton
                return MapleItemInformationProvider.getInstance().itemExists(id);
    }

    public void WarpCashShop() {
        getPlayer().WarpCashShop();
    }

    public void gainItemAll(int itemID) {
        getClient().getWorldServer().getChannels().forEach((cserv) -> {
            cserv.getPlayerStorage().getAllCharacters().forEach((chr) -> {
                chr.gainItem(itemID);
            });
        });
    }

    public int getGiftLog(String bossid) {
        return getPlayer().getGiftLog(bossid);
    }

    public int getBossLog(String id) {
        return getPlayer().getBossLog(id);
    }

    public void setBossLog(String id) {
        getPlayer().setBossLog(id);
    }

    public String EquipList(MapleClient c) {
        StringBuilder str = new StringBuilder();
        MapleInventory equip = c.getPlayer().getInventory(MapleInventoryType.EQUIP);
        List<String> stra = new LinkedList<String>();
        equip.list().forEach((IItem) -> {
            stra.add("#L" + IItem.getPosition() + "##v" + IItem.getItemId() + "##l");
        });
        stra.forEach((strb) -> {
            str.append(strb);
        });
        return str.toString();
    }

    public String UseList(MapleClient c) {
        StringBuilder str = new StringBuilder();
        MapleInventory use = c.getPlayer().getInventory(MapleInventoryType.USE);
        List<String> stra = new LinkedList<String>();
        use.list().forEach((IItem) -> {
            stra.add("#L" + IItem.getPosition() + "##v" + IItem.getItemId() + "##l");
        });
        stra.forEach((strb) -> {
            str.append(strb);
        });
        return str.toString();
    }

    public String CashList(MapleClient c) {
        StringBuilder str = new StringBuilder();
        MapleInventory cash = c.getPlayer().getInventory(MapleInventoryType.CASH);
        List<String> stra = new LinkedList<String>();
        cash.list().forEach((IItem) -> {
            stra.add("#L" + IItem.getPosition() + "##v" + IItem.getItemId() + "##l");
        });
        stra.forEach((strb) -> {
            str.append(strb);
        });
        return str.toString();
    }

    public String ETCList(MapleClient c) {
        StringBuilder str = new StringBuilder();
        MapleInventory etc = c.getPlayer().getInventory(MapleInventoryType.ETC);
        List<String> stra = new LinkedList<String>();
        etc.list().forEach((IItem) -> {
            stra.add("#L" + IItem.getPosition() + "##v" + IItem.getItemId() + "##l");
        });
        stra.forEach((strb) -> {
            str.append(strb);
        });
        return str.toString();
    }

    public String EquippedList(MapleClient c) {
        StringBuilder str = new StringBuilder();
        MapleInventory equipped = c.getPlayer().getInventory(MapleInventoryType.EQUIPPED);
        List<String> stra = new LinkedList<>();
        int num = 0;
        equipped.list().forEach((IItem) -> {
            stra.add("#L" + IItem.getPosition() + "##v" + IItem.getItemId() + "##l");
        });
        stra.forEach((strb) -> {
            str.append(strb);
        });
        return str.toString();
    }

    public String SetupList(MapleClient c) {
        StringBuilder str = new StringBuilder();
        MapleInventory setup = c.getPlayer().getInventory(MapleInventoryType.SETUP);
        List<String> stra = new LinkedList<>();
        int num = 0;
        setup.list().forEach((IItem) -> {
            stra.add("#L" + IItem.getPosition() + "##v" + IItem.getItemId() + "##l");
        });
        stra.forEach((strb) -> {
            str.append(strb);
        });
        return str.toString();
    }

    public void reloadChar() {
        getPlayer().getClient().getSession().write(CField.getCharInfo(getPlayer()));
        getPlayer().getMap().removePlayer(getPlayer());
        getPlayer().getMap().addPlayer(getPlayer());
    }

    public void modifyNx(int amount) {
        getPlayer().modifyCSPoints(1, amount);
        if (amount > 0) {
            getPlayer().dropMessage(6, "您得到 " + amount / 2 + " 商城點數.");
            getPlayer().dropMessage(-1, "您得到 " + amount / 2 + " 商城點數.");
        } else {
            getPlayer().dropMessage(6, "您失去 " + amount / 2 + " 商城點數.");
            getPlayer().dropMessage(-1, "您得到 " + amount / 2 + " 商城點數.");
        }
    }

    public void sendAndroidStyle(String text, int styles[]) {
        if (lastMsg > -1) {
            return;
        }
        c.getSession().write(NPCPacket.getAndroidTalkStyle(id, text, styles));
        lastMsg = 10;
    }

    public void setAndroidHair(int hair) {
        getPlayer().getAndroid().setHair(hair);
        getPlayer().getAndroid().saveToDb();
        c.getPlayer().setAndroid(c.getPlayer().getAndroid());
    }

    public void setAndroidFace(int face) {
        getPlayer().getAndroid().setFace(face);
        getPlayer().getAndroid().saveToDb();
        c.getPlayer().setAndroid(c.getPlayer().getAndroid());
    }

    public int getHour() {
        return Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
    }

    public int getDayOfWeek() {
        return Calendar.getInstance().get(Calendar.DAY_OF_WEEK);
    }

    public int gethour2() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        return hour;
    }

    public int getmin2() {
        Calendar cal = Calendar.getInstance();
        int min = cal.get(Calendar.MINUTE);
        return min;
    }

    public int getsec2() {
        Calendar cal = Calendar.getInstance();
        int sec = cal.get(Calendar.SECOND);
        return sec;
    }

    public int getMin() {
        return Calendar.getInstance().get(Calendar.MINUTE);
    }

    public int getSec() {
        return Calendar.getInstance().get(Calendar.SECOND);
    }

    public int gethour() {
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        return hour;
    }

    public int getmin() {
        Calendar cal = Calendar.getInstance();
        int min = cal.get(Calendar.MINUTE);
        return min;
    }

    public int getsec() {
        Calendar cal = Calendar.getInstance();
        int sec = cal.get(Calendar.SECOND);
        return sec;
    }

    public void EquipUpgrade(byte slot, MapleCharacter player) {
        MapleInventory equip = player.getInventory(MapleInventoryType.EQUIP);
        Equip eu = (Equip) equip.getItem(slot);
        int item = equip.getItem(slot).getItemId();
        short hand = eu.getHands();
        byte level = eu.getLevel();
        Random xs = new Random();
        int upstr = xs.nextInt(5);
        int updex = xs.nextInt(5);
        int upint = xs.nextInt(5);
        int upluk = xs.nextInt(5);
        int uphpmp = xs.nextInt(30);
        int upwatk = xs.nextInt(10);
        int upmatk = xs.nextInt(10);
        int upwdef = xs.nextInt(15);
        int upmdef = xs.nextInt(15);
        int upspd = xs.nextInt(2);
        int upjup = xs.nextInt(1);
        int ranP = (int) (Math.random() * 100);
        int frP = (int) (Math.random() * 100);
        Equip nItem = new Equip(item, (byte) equip.getNextFreeSlot(), (byte) 0);
        if (eu.getUpgradeSlots() > 0) {
            if ((ranP > 0 && ranP <= 5) || (ranP > 13 && ranP <= 35) || (ranP > 45 && ranP <= 50) || (ranP > 62 && ranP <= 68) || (ranP > 71 && ranP <= 79) || (ranP > 82 && ranP <= 96)) { // 強化成功概率60%
                nItem.setStr((short) (eu.getStr() + upstr)); // 力量
                nItem.setDex((short) (eu.getDex() + updex)); // 敏捷
                nItem.setInt((short) (eu.getInt() + upint)); // 智力
                nItem.setLuk((short) (eu.getLuk() + upluk)); // 運氣
                nItem.setHp((short) (eu.getHp() + uphpmp)); // MAXHP
                nItem.setMp((short) (eu.getMp() + uphpmp)); // MAXMP
                nItem.setWatk((short) (eu.getWatk() + upwatk)); // 攻擊力
                nItem.setMatk((short) (eu.getMatk() + upmatk)); // 魔法力
                nItem.setWdef((short) (eu.getWdef() + upwdef)); // 防禦力
                nItem.setMdef((short) (eu.getMdef() + upmdef)); // 魔法防禦力
                nItem.setAcc((short) (eu.getAcc() + (updex * 3 + upluk) / 2)); // 命中率
                nItem.setAvoid((short) (eu.getAvoid() + (updex + upluk * 2) / 2)); // 迴避率
                nItem.setSpeed​​((short) (eu.getSpeed​​() + upspd)); // 速度        
                nItem.setJump((short) (eu.getJump() + upjup)); // 跳躍         
                nItem.setUpgradeSlots((byte) (eu.getUpgradeSlots() - 1)); // 可升級次數 
                nItem.setLevel((byte) (eu.getLevel() + 1)); // 裝備已升級次數      
                nItem.setHands(hand);
                player.getInventory(MapleInventoryType.EQUIP).addFromDB(nItem);
                equip.removeItem(slot);
                player.dropMessage(6, "強化成功！");
                player.reloadChar();
            } else { //強化失敗後30%概率裝備消失  
                if ((frP > 6 && frP <= 15) || (frP > 21 && frP <= 41) || (frP > 50 && frP <= 65) || (frP > 70 && frP <= 84) || (frP > 87 && frP <= 99)) {
                    nItem.setStr(eu.getStr());
                    nItem.setDex(eu.getDex());
                    nItem.setInt(eu.getInt());
                    nItem.setLuk(eu.getLuk());
                    nItem.setHp(eu.getHp());
                    nItem.setMp(eu.getMp());
                    nItem.setWatk(eu.getWatk());
                    nItem.setMatk(eu.getMatk());
                    nItem.setWdef(eu.getWdef());
                    nItem.setMdef(eu.getMdef());
                    nItem.setAcc(eu.getAcc());
                    nItem.setAvoid(eu.getAvoid());
                    nItem.setSpeed​​((short) eu.getSpeed​​());
                    nItem.setJump(eu.getJump());
                    nItem.setUpgradeSlots((byte) (eu.getUpgradeSlots() - 1));
                    nItem.setHands(hand);
                    nItem.setLevel(level);
                    player.getInventory(MapleInventoryType.EQUIP).addFromDB(nItem);
                    equip.removeItem(slot);
                    player.dropMessage(6, "強化失敗，可升級次數減少1次！");
                    player.reloadChar();
                } else {
                    equip.removeItem(slot);
                    player.dropMessage(6, "你的裝備已經損壞！");
                    player.reloadChar();
                }
            }
        } else {
            player.setFame(player.getFame() - 10);
            player.dropMessage(6, "你失去了10點人氣！");
        }
    }

    public void YellowMessage(String text) {
        ChannelServer.getAllInstances().stream().map((cserv) -> {
            cserv.setServerMessage(text);
            return cserv;
        }).forEachOrdered((_item) -> {
            System.out.println("伺服器黃色公告被更改為 [" + text + "]");
        });
    }

    public void Heal(MapleCharacter c) {
        c.getStat().setHp(c.getStat().getMaxHp(), c);
        c.updateSingleStat(MapleStat.HP, c.getStat().getMaxHp());
        c.getStat().setMp(c.getStat().getMaxMp(), c);
        c.updateSingleStat(MapleStat.MP, c.getStat().getMaxMp());
        c.reloadChar();
    }

    public void gainNx(int amount) {
        c.getPlayer().modifyCSPoints(1, amount, true);
    }

    public void Shop(int id) {
        MapleShopFactory.getInstance().getShop(id).sendShop(getClient());
    }

    public void opennpc(int id) {
        MapleNPC npc = MapleLifeFactory.getNPC(id);
        if (npc != null && !npc.getName().equalsIgnoreCase("MISSINGNO")) {
            dispose();
            NPCScriptManager.getInstance().start(getClient(), id, null);
        }
    }
    private String customData;

    public final void setCData(final String customData) {
        this.customData = customData;
    }

    public final String getCData(final String customData) {
        return customData;
    }

    public void levelUp() {
        c.getPlayer().levelUp();
        c.getPlayer().setExp(0);
    }

    public boolean makeProItem(int id, int hardcore) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        Item item = ii.getEquipById(id);
        MapleInventoryType type = ii.getInventoryType(id);
        if (type.equals(MapleInventoryType.EQUIP)) {
            MapleInventoryManipulator.addFromDrop(c, ii.hardcoreItem((Equip) item, (short) hardcore), true);
            return true;
        } else {
            return false;
        }
    }

    public void ServerNotice(String text) {
        World.Broadcast.broadcastMessage(CWvsContext.serverNotice(6, text));
    }

    public void serverNotice(String text) {
        World.Broadcast.broadcastMessage(CWvsContext.serverNotice(6, text));
    }

    public void serverMessage(String text) {
        World.Broadcast.broadcastMessage(CWvsContext.serverNotice(6, text));
    }

    public void ServerMessage(String text) {
        World.Broadcast.broadcastMessage(CWvsContext.serverNotice(6, text));
    }

    public void worldMessage(String text) {
        World.Broadcast.broadcastMessage(CWvsContext.serverNotice(6, text));
    }

    public void WorldMessage(String text) {
        World.Broadcast.broadcastMessage(CWvsContext.serverNotice(6, text));
    }

    public void teachSkill(int id, int level, int masterlevel) {
        getPlayer().changeSkillLevel(SkillFactory.getSkill(id), level, masterlevel);
    }

    public void equipupdate(byte slot, MapleCharacter player, int stats, int HMP, int def) {
        MapleInventory equip = player.getInventory(MapleInventoryType.EQUIP);
        Equip eu = (Equip) equip.getItem(slot);
        int item = equip.getItem(slot).getItemId();
        Equip nItem = (Equip) player.getInventory(MapleInventoryType.EQUIP).getItem(slot).copy();

        nItem.setStr((short) (eu.getStr() + stats)); // STR
        nItem.setDex((short) (eu.getDex() + stats)); // DEX
        nItem.setInt((short) (eu.getInt() + stats)); // INT
        nItem.setLuk((short) (eu.getLuk() + stats)); //LUK
        nItem.setHp((short) (eu.getHp() + HMP)); // HP
        nItem.setMp((short) (eu.getMp() + HMP)); // MP
        nItem.setWdef((short) (eu.getWdef() + def)); //WDEF
        nItem.setMdef((short) (eu.getMdef() + def)); //MDEF
        if (!GameConstants.isMage(player.getJob())) {
            nItem.setWatk((short) (eu.getWatk() + stats)); // 
        } else {
            nItem.setMatk((short) (eu.getMatk() + stats)); // 
        }
        nItem.setPotential1(eu.getPotential1());
        nItem.setPotential2(eu.getPotential2());
        nItem.setPotential3(eu.getPotential3());
        nItem.setUpgradeSlots((byte) (eu.getUpgradeSlots() - 1)); // 可升級次數 
        nItem.setLevel((byte) (eu.getLevel() + 1)); // 裝備已升級次數      
        player.getInventory(MapleInventoryType.EQUIP).addItem(nItem);
        player.getInventory(MapleInventoryType.EQUIP).removeItem(slot);  // 原始物品刪除
        player.reloadChar();
        dispose();
    }

    public void equipMix(byte slot1, byte slot2, byte slot3) {
        String SEX = (getChar().getGender() == 0 ? "歐尼醬" : "歐乃醬");
        MapleInventory equip = getPlayer().getInventory(MapleInventoryType.EQUIP);
        Equip eu1 = (Equip) equip.getItem(slot1);//slot1
        Equip eu2 = (Equip) equip.getItem(slot2);//slot2
        Equip eu3 = (Equip) equip.getItem(slot3);//slot3

        if (eu1 == null || eu2 == null || eu3 == null) {
            sendOk("#b" + SEX + "#d的背包前三格有一格沒東西呀，檢查看看呀。");
            dispose();
            return;
        } else if ((eu1.getItemId() != eu2.getItemId()) || (eu2.getItemId() != eu3.getItemId()) || (eu1.getItemId() != eu3.getItemId())) {
            sendOk("#b" + SEX + "#d看清楚點：#v" + eu1.getItemId() + "# #v" + eu2.getItemId() + "# #v" + eu3.getItemId() + "# 是不一樣的!");
            dispose();
            return;
        } else if (eu1.getUpgradeSlots() < 1 || eu2.getUpgradeSlots() < 1 || eu3.getUpgradeSlots() < 1) {
            sendOk("#b" + SEX + "#d無法融合已融合過的裝備。");
            dispose();
            return;
        }
        short Str = (short) (eu1.getStr() + eu2.getStr() + eu3.getStr());
        short Dex = (short) (eu1.getDex() + eu2.getDex() + eu3.getDex());
        short Int = (short) (eu1.getInt() + eu2.getInt() + eu3.getInt());
        short Luk = (short) (eu1.getLuk() + eu2.getLuk() + eu3.getLuk());
        short Hp = (short) (eu1.getHp() + eu2.getHp() + eu3.getHp());
        short Mp = (short) (eu1.getMp() + eu2.getMp() + eu3.getMp());
        short Watk = (short) (eu1.getWatk() + eu2.getWatk() + eu3.getWatk());
        short Matk = (short) (eu1.getMatk() + eu2.getMatk() + eu3.getMatk());
        short Wdef = (short) (eu1.getWdef() + eu2.getWdef() + eu3.getWdef());
        short Mdef = (short) (eu1.getMdef() + eu2.getMdef() + eu3.getMdef());
        short Acc = (short) (eu1.getAcc() + eu2.getAcc() + eu3.getAcc());
        short Avoid = (short) (eu1.getAvoid() + eu2.getAvoid() + eu3.getAvoid());
        Byte Level = 0;
        Equip Neu = (Equip) eu1.copy();
        Neu.setStr(Str);
        Neu.setDex(Dex);
        Neu.setInt(Int);
        Neu.setLuk(Luk);
        Neu.setHp(Hp);
        Neu.setMp(Mp);
        Neu.setWatk(Watk);
        Neu.setMatk(Matk);
        Neu.setWdef(Wdef);
        Neu.setMdef(Mdef);
        Neu.setAcc(Acc);
        Neu.setAvoid(Avoid);
        Neu.setUpgradeSlots(Level);
        Neu.setPotential1(eu1.getPotential1());
        Neu.setPotential2(eu1.getPotential2());
        Neu.setPotential3(eu1.getPotential3());
        equip.removeItem(slot1);
        equip.removeItem(slot2);
        equip.removeItem(slot3);
        equip.addItem(Neu);
        getPlayer().reloadChar();
        dispose();

    }

    public Invocable getIv() {
        return iv;
    }

    public int getNpc() {
        return id;
    }

    public int getQuest() {
        return id2;
    }

    public String getDevNews() throws SQLException {
        StringBuilder ret = new StringBuilder();
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT title, msg, date FROM dev_news ORDER BY id desc LIMIT 5");
        ResultSet rs = ps.executeQuery();
        try {
            while (rs.next()) {
                ret.append("\r\n#e").append(rs.getString("title")).append(" - (").append(rs.getString("date")).append(")#n\r\n").append(rs.getString("msg")).append("\r\n");
            }
        } catch (SQLException ex) {
            Logger.getLogger(NPCConversationManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        ps.close();
        rs.close();
        return ret.toString();
    }

    public byte getType() {
        return type;
    }

    public void updateAndroid(boolean itemonly) {
        CField.updateAndroidLook(itemonly, c.getPlayer(), c.getPlayer().getAndroid());
    }

    public void safeDispose() {
        pendingDisposal = true;
    }

    public void dispose() {
        NPCScriptManager.getInstance().dispose(c);
    }

    public static void dispose(MapleClient c) {
        c.announce(CWvsContext.enableActions());
        NPCScriptManager.getInstance().getCM(c).dispose();
    }

    public void askMapSelection(final String sel) {
        if (lastMsg > -1) {
            return;
        }
        c.getSession().write(NPCPacket.getMapSelection(id, sel));
        lastMsg = (byte) (GameConstants.GMS ? 0x11 : 0x10);
    }

    public void askBuffSelection(final String sel) {
        if (lastMsg > -1) {
            return;
        }
        c.getSession().write(NPCPacket.getBuffSelection(id, sel));
        lastMsg = (byte) 17;
    }

    public void sendNext(String text) {
        sendNext(text, id);
    }

    public void sendNext(String text, int id) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { //sendNext will dc otherwise!
            sendSimple(text);
            return;
        }
        c.getSession().write(NPCPacket.getNPCTalk(id, (byte) 0, text, "00 01", (byte) 0));
        lastMsg = 0;
    }

    public void sendPlayerToNpc(String text) {
        sendNextS(text, (byte) 3, id);
    }

    public void sendNextNoESC(String text) {
        sendNextS(text, (byte) 1, id);
    }

    public void sendNextNoESC(String text, int id) {
        sendNextS(text, (byte) 1, id);
    }

    public void sendNextS(String text, byte type) {
        sendNextS(text, type, id);
    }

    public void sendNextS(String text, byte type, int idd) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimpleS(text, type);
            return;
        }
        c.getSession().write(NPCPacket.getNPCTalk(id, (byte) 0, text, "00 01", type, idd));
        lastMsg = 0;
    }

    public void sendPrev(String text) {
        sendPrev(text, id);
    }

    public void sendPrev(String text, int id) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimple(text);
            return;
        }
        c.getSession().write(NPCPacket.getNPCTalk(id, (byte) 0, text, "01 00", (byte) 0));
        lastMsg = 0;
    }

    public void sendPrevS(String text, byte type) {
        sendPrevS(text, type, id);
    }

    public void sendPrevS(String text, byte type, int idd) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimpleS(text, type);
            return;
        }
        c.getSession().write(NPCPacket.getNPCTalk(id, (byte) 0, text, "01 00", type, idd));
        lastMsg = 0;
    }

    public void sendNextPrev(String text) {
        sendNextPrev(text, id);
    }

    public void sendNextPrev(String text, int id) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimple(text);
            return;
        }
        c.getSession().write(NPCPacket.getNPCTalk(id, (byte) 0, text, "01 01", (byte) 0));
        lastMsg = 0;
    }

    public void PlayerToNpc(String text) {
        sendNextPrevS(text, (byte) 3);
    }

    public void sendNextPrevS(String text) {
        sendNextPrevS(text, (byte) 3);
    }

    public void sendNextPrevS(String text, byte type) {
        sendNextPrevS(text, type, id);
    }

    public void sendNextPrevS(String text, byte type, int idd) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimpleS(text, type);
            return;
        }
        c.getSession().write(NPCPacket.getNPCTalk(id, (byte) 0, text, "01 01", type, idd));
        lastMsg = 0;
    }

    public void sendOk(String text) {
        sendOk(text, id);
    }

    public void sendOk(String text, int id) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimple(text);
            return;
        }
        c.getSession().write(NPCPacket.getNPCTalk(id, (byte) 0, text, "00 00", (byte) 0));
        lastMsg = 0;
    }

    public void sendOkS(String text, byte type) {
        sendOkS(text, type, id);
    }

    public void sendOkS(String text, byte type, int idd) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimpleS(text, type);
            return;
        }
        c.getSession().write(NPCPacket.getNPCTalk(id, (byte) 0, text, "00 00", type, idd));
        lastMsg = 0;
    }

    public void sendYesNo(String text) {
        sendYesNo(text, id);
    }

    public void sendYesNo(String text, int id) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimple(text);
            return;
        }
        c.getSession().write(NPCPacket.getNPCTalk(id, (byte) 2, text, "", (byte) 0));
        lastMsg = 2;
    }

    public void sendYesNoS(String text, byte type) {
        sendYesNoS(text, type, id);
    }

    public void sendYesNoS(String text, byte type, int idd) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimpleS(text, type);
            return;
        }
        c.getSession().write(NPCPacket.getNPCTalk(id, (byte) 2, text, "", type, idd));
        lastMsg = 2;
    }

    public void sendAcceptDecline(String text) {
        askAcceptDecline(text);
    }

    public void sendAcceptDeclineNoESC(String text) {
        askAcceptDeclineNoESC(text);
    }

    public void askAcceptDecline(String text) {
        askAcceptDecline(text, id);
    }

    public void askAcceptDecline(String text, int id) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimple(text);
            return;
        }
        lastMsg = (byte) (GameConstants.GMS ? 0xF : 0xE);
        c.getSession().write(NPCPacket.getNPCTalk(id, (byte) lastMsg, text, "", (byte) 0));
    }

    public void askAcceptDeclineNoESC(String text) {
        askAcceptDeclineNoESC(text, id);
    }

    public void askAcceptDeclineNoESC(String text, int id) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimple(text);
            return;
        }
        lastMsg = (byte) (GameConstants.GMS ? 0xF : 0xE);
        c.getSession().write(NPCPacket.getNPCTalk(id, (byte) lastMsg, text, "", (byte) 1));
    }

    public void askAvatar(String text, int... args) {
        if (lastMsg > -1) {
            return;
        }
        c.getSession().write(NPCPacket.getNPCTalkStyle(id, text, args));
        lastMsg = 9;
    }

    public void sendSimple(String text) {
        sendSimple(text, id);
    }

    public void sendSimple(String text, int id) {
        if (lastMsg > -1) {
            return;
        }
        if (!text.contains("#L")) { //sendSimple will dc otherwise!
            sendNext(text);
            return;
        }
        c.getSession().write(NPCPacket.getNPCTalk(id, (byte) 5, text, "", (byte) 0));
        lastMsg = 5;
    }

    public void sendSimpleS(String text, byte type) {
        sendSimpleS(text, type, id);
    }

    public void sendSimpleS(String text, byte type, int idd) {
        if (lastMsg > -1) {
            return;
        }
        if (!text.contains("#L")) { //sendSimple will dc otherwise!
            sendNextS(text, type);
            return;
        }
        c.getSession().write(NPCPacket.getNPCTalk(id, (byte) 5, text, "", (byte) type, idd));
        lastMsg = 5;
    }

    public void sendStyle(String text, int styles[]) {
        if (lastMsg > -1) {
            return;
        }
        c.getSession().write(NPCPacket.getNPCTalkStyle(id, text, styles));
        lastMsg = 9;
    }

    public void askAndroid(String text, int... args) {
        if (lastMsg > -1) {
            return;
        }
        c.getSession().write(NPCPacket.getAndroidTalkStyle(id, text, args));
        lastMsg = 10;
    }

    public void sendGetNumber(String text, int def, int min, int max) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimple(text);
            return;
        }
        c.getSession().write(NPCPacket.getNPCTalkNum(id, text, def, min, max));
        lastMsg = 4;
    }

    public void sendGetText(String text) {
        sendGetText(text, id);
    }

    public void sendGetText(String text, int id) {
        if (lastMsg > -1) {
            return;
        }
        if (text.contains("#L")) { // will dc otherwise!
            sendSimple(text);
            return;
        }
        c.getSession().write(NPCPacket.getNPCTalkText(id, text));
        lastMsg = 3;
    }

    public void setGetText(String text) {
        this.getText = text;
    }

    public String getText() {
        return getText;
    }

    public void setHair(int hair) {
        getPlayer().setHair(hair);
        getPlayer().updateSingleStat(MapleStat.HAIR, hair);
        getPlayer().equipChanged();
    }

    @Override
    public final MapleCharacter getChar() {
        return getPlayer();
    }

    public void setFace(int face) {
        getPlayer().setFace(face);
        getPlayer().updateSingleStat(MapleStat.FACE, face);
        getPlayer().equipChanged();
    }

    public void setSkin(int color) {
        getPlayer().setSkinColor((byte) color);
        getPlayer().updateSingleStat(MapleStat.SKIN, color);
        getPlayer().equipChanged();
    }

    public String getPvPRanks() throws SQLException {
        StringBuilder ret = new StringBuilder();
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT name, pvpKills FROM characters WHERE gm < 3 ORDER BY pvpKills desc LIMIT 10");
        ps.executeQuery();
        ResultSet rs = ps.executeQuery();
        try {
            while (rs.next()) {
                ret.append("\r\n").append("#b").append("Name(IGN) : ").append(rs.getString("name")).append("#k#r").append("        |      Kills : ").append(rs.getInt("pvpKills")).append("#k");
            }
            rs.close();
            ps.close();
        } catch (Exception ex) {
            Logger.getLogger(NPCConversationManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret.toString();
    }

    public String getFameRanks() throws SQLException {
        StringBuilder ret = new StringBuilder();
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT name, fame FROM characters WHERE gm < 3 ORDER BY fame desc LIMIT 10");
        ps.executeQuery();
        ResultSet rs = ps.executeQuery();
        try {
            while (rs.next()) {
                ret.append("\r\n").append("#b").append(rs.getString("name")).append(" : ").append(rs.getInt("fame")).append("#k");
            }
            rs.close();
            ps.close();
        } catch (Exception ex) {
            Logger.getLogger(NPCConversationManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret.toString();
    }

    public String getJQRanks() throws SQLException {
        StringBuilder ret = new StringBuilder();
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT name, jqlevel, jqexp FROM characters WHERE gm < 3 ORDER BY jqlevel desc, jqexp DESC LIMIT 10");
        ps.executeQuery();
        ResultSet rs = ps.executeQuery();
        try {
            while (rs.next()) {
                ret.append("\r\n").append("#d").append(rs.getString("name")).append("#k - #bJQ Level:#k #rLv. ").append(rs.getInt("jqlevel")).append("#k #bJQ Exp:#k #r").append(rs.getInt("jqexp")).append("#k");
            }
            rs.close();
            ps.close();
        } catch (Exception ex) {
            Logger.getLogger(NPCConversationManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        return ret.toString();
    }

    public void ProDonatorItem(byte slot, int str, int dex, int int_, int luk) {
        MapleInventory equip = getPlayer().getInventory(MapleInventoryType.EQUIP);
        Equip eu = (Equip) equip.getItem(slot); // get slot determine eq
        short hand = eu.getHands(); // HANDS
        byte level = eu.getLevel(); // LEVEL
        eu.setStr((short) str); // STR
        eu.setDex((short) dex); // DEX
        eu.setInt((short) int_); // INT
        eu.setLuk((short) luk); //LUK
        eu.setUpgradeSlots((byte) 0); // Feel free to change
        eu.setHands(hand);
        eu.setLevel(level);
        getPlayer().getInventory(MapleInventoryType.EQUIP).addFromDB(eu);
    }

    public int setAndroid(int args) {
        if (args < 30000) {
            c.getPlayer().getAndroid().setFace(args);
            c.getPlayer().getAndroid().saveToDb();
        } else {
            c.getPlayer().getAndroid().setHair(args);
            c.getPlayer().getAndroid().saveToDb();
        }
        c.getPlayer().equipChanged();

        return 1;
    }

    public int getAndroidStat(final String type) {
        if (type.equals("HAIR")) {
            return c.getPlayer().getAndroid().getHair();
        } else if (type.equals("FACE")) {
            return c.getPlayer().getAndroid().getFace();
        } else if (type.equals("GENDER")) {
            int itemid = c.getPlayer().getAndroid().getItemId();
            if (itemid == 1662000 || itemid == 1662002) {
                return 0;
            } else {
                return 1;
            }
        }
        return -1;
    }

    public void gainJQExp(int gain) {
        getPlayer().gainJQExp(gain);
    }

    public void MakeGMItem(byte slot, MapleCharacter player) {
        MapleInventory equip = player.getInventory(MapleInventoryType.EQUIP);
        Equip eu = (Equip) equip.getItem(slot);
        int item = equip.getItem(slot).getItemId();
        short hand = eu.getHands();
        byte level = eu.getLevel();
        Equip nItem = new Equip(item, slot, (byte) 0);
        nItem.setStr((short) 32767); // STR
        nItem.setDex((short) 32767); // DEX
        nItem.setInt((short) 32767); // INT
        nItem.setLuk((short) 32767); //LUK
        nItem.setUpgradeSlots((byte) 0);
        nItem.setHands(hand);
        nItem.setLevel(level);
        player.getInventory(MapleInventoryType.EQUIP).removeItem(slot);
        player.getInventory(MapleInventoryType.EQUIP).addFromDB(nItem);
    }

    public void giveBuff(int buff) {
        SkillFactory.getSkill(buff).getEffect(SkillFactory.getSkill(buff).getMaxLevel()).applyTo(getPlayer());
    }

    public void MakeNoobPot(byte slot, MapleCharacter player) {
        int randst = (int) (100.0 * Math.random()) + 21;
        MapleInventory equip = player.getInventory(MapleInventoryType.EQUIP);
        Equip eu = (Equip) equip.getItem(slot);
        int item = equip.getItem(slot).getItemId();
        // MapleJob job = eu.();
        short hand = eu.getHands();
        byte level = eu.getLevel();
        Equip nItem = new Equip(item, equip.getNextFreeSlot(), (byte) 0);
        nItem.setStr((short) randst); // STR
        nItem.setDex((short) randst); // DEX
        nItem.setInt((short) randst); // INT
        nItem.setLuk((short) randst); //LUK
        nItem.setWatk((short) randst); // WA
        nItem.setUpgradeSlots((byte) 0); // SLOT
        //nItem.setJob(job);
        nItem.setHands(hand);
        nItem.setLevel(level);
        player.getInventory(MapleInventoryType.EQUIP).addFromDB(nItem);
    }

    public void MakeProPot(byte slot, MapleCharacter player) {
        int randst = (int) (1000.0 * Math.random()) + 21;
        int randwa = (int) (500.0 * Math.random()) + 21;
        MapleInventory equip = player.getInventory(MapleInventoryType.EQUIP);
        Equip eu = (Equip) equip.getItem(slot);
        int item = equip.getItem(slot).getItemId();
        // MapleJob job = eu.();
        short hand = eu.getHands();
        byte level = eu.getLevel();
        Equip nItem = new Equip(item, equip.getNextFreeSlot(), (byte) 0);
        nItem.setStr((short) randst); // STR
        nItem.setDex((short) randst); // DEX
        nItem.setInt((short) randst); // INT
        nItem.setLuk((short) randst); //LUK
        nItem.setWatk((short) randwa); // WA
        nItem.setUpgradeSlots((byte) 0); // SLOT
        //nItem.setJob(job);
        nItem.setHands(hand);
        nItem.setLevel(level);
        player.getInventory(MapleInventoryType.EQUIP).addFromDB(nItem);
    }

    public void MakeAdvPot(byte slot, MapleCharacter player) {
        int randst = (int) (5000.0 * Math.random()) + 21;
        int randwa = (int) (2000.0 * Math.random()) + 21;
        MapleInventory equip = player.getInventory(MapleInventoryType.EQUIP);
        Equip eu = (Equip) equip.getItem(slot);
        int item = equip.getItem(slot).getItemId();
        // MapleJob job = eu.();
        short hand = eu.getHands();
        byte level = eu.getLevel();
        Equip nItem = new Equip(item, equip.getNextFreeSlot(), (byte) 0);
        nItem.setStr((short) randst); // STR
        nItem.setDex((short) randst); // DEX
        nItem.setInt((short) randst); // INT
        nItem.setLuk((short) randst); //LUK
        nItem.setWatk((short) randwa); // WA
        nItem.setUpgradeSlots((byte) 0); // SLOT
        //nItem.setJob(job);
        nItem.setHands(hand);
        nItem.setLevel(level);
        player.getInventory(MapleInventoryType.EQUIP).addFromDB(nItem);
    }

    public boolean nameIsLegal(String text) {
        String[] illegalChars = {" ", "!", "@", "#", "$", "%", "^", "&", "*", "(", ")", "_", "-", "+", "=", ";", ":", "\"", "\\", "/", "//", ",", ".", "<", ">", "?", "{", "}", "[", "]", "|", " ", "Owner", "Admin", "GameMaster", "GM", "Fuck", "Bitch", "Pussy", "friend", "EricIs", "Shit", "Dick", " Vagina", "Penis", "Clit", "Faggot", "Gay", "Gayboi", "Asian", "DeathStar", "God"};
        for (String illegalChar : illegalChars) {
            if (text.contains(illegalChar)) {
                return false;
            }
        }
        return true;
    }

    public boolean nameIsLegalDonor(String dtext) {
        String[] illegalChars = {" ", "Owner", "Admin", "GameMaster", "GM", "Fuck", "Bitch", "Pussy", "friend", "EricIs", "Shit", "Dick", " Vagina", "Penis", "Clit", "Faggot", "Gay", "Gayboi", "Asian", "DeathStar", "God"};
        for (int i = 0; i < illegalChars.length; i++) {
            if (dtext.contains(illegalChars[i])) {
                return false;
            }
        }
        return true;
    }

    public String sendGreen(String text) {
        char[] hi = text.toLowerCase().toCharArray();
        int itemId;
        String newString = "";
        for (Character hello : hi) {
            itemId = Character.getNumericValue(hello) + 3991016;
            newString += itemId != 3991015 ? "#v" + itemId + "#" : " ";
        }
        return newString;
    }

    public int getRandom(int start, int end) {
        return (int) Math.floor(Math.random() * end + start);
    }

    public String getInsult() {
        try {
            HttpURLConnection con = (HttpURLConnection) new URL("http://www.randominsults.net/").openConnection();
            StringBuilder sb = new StringBuilder();
            con.connect();
            InputStream input = con.getInputStream();
            byte[] buf = new byte[2048];
            int read;
            while ((read = input.read(buf)) > 0) {
                sb.append(new String(buf, 0, read));
            }
            final String find = "<strong><i>";
            int firstPost = sb.indexOf(find);
            StringBuilder send = new StringBuilder();
            for (int i = firstPost + find.length(); i < sb.length(); i++) {
                char ch = sb.charAt(i);
                if (sb.charAt(i) == '<' && sb.charAt(i + 1) == '/' && sb.charAt(i + 2) == 'i') {
                    break;
                }
                send.append(ch);
            }
            String sendTxt = send.toString();
            sendTxt = sendTxt.replaceAll("\\<.*?>", "");
            sendTxt = fixHTML(sendTxt);
            return sendTxt;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return "Error Occured!";
    }

    public String fixHTML(String in) {
        in = in.replaceAll(Pattern.quote("&quot;"), "\"");
        in = in.replaceAll(Pattern.quote("&amp;"), "&");

        return in;
    }

    public void playMusic(String music) {
        getPlayer().getMap().broadcastMessage(CField.musicChange(music));
    }

    public String getChallenges() throws SQLException {
        StringBuilder ret = new StringBuilder();
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT title, completed, date FROM challenges ORDER BY id desc LIMIT 5");
        ResultSet rs = ps.executeQuery();
        try {
            while (rs.next()) {
                ret.append("\r\n#e").append(rs.getString("title")).append(" - (").append(rs.getString("date")).append(")#n\r\n").append(rs.getString("completed")).append("\r\n");
            }
        } catch (SQLException ex) {
            Logger.getLogger(NPCConversationManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        ps.close();
        rs.close();
        return ret.toString();
    }

    public void openNpc2(int id) {
        dispose();
        NPCScriptManager.getInstance().start(getClient(), id);
    }

    public void changeKeyBinding(int key, byte type, int action) {
        getPlayer().changeKeybinding(key, type, action);
        getPlayer().sendKeymap();
    }

    public String getNews() throws SQLException {
        StringBuilder ret = new StringBuilder();
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT title, message, date FROM trollnews ORDER BY newsid desc LIMIT 5");
        ResultSet rs = ps.executeQuery();
        try {
            while (rs.next()) {
                ret.append("\r\n#e").append(rs.getString("title")).append(" - (").append(rs.getString("date")).append(")#n\r\n").append(rs.getString("message")).append("\r\n");
            }
        } catch (SQLException ex) {
            Logger.getLogger(NPCConversationManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        ps.close();
        rs.close();
        return ret.toString();
    }

    public String getDigits() throws SQLException {
        StringBuilder ret = new StringBuilder();
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT mesos FROM mrush");
        ResultSet rs = ps.executeQuery();
        try {
            while (rs.next()) {
                ret.append("#e#b").append(rs.getLong("mesos")).append("#k#n");
            }
        } catch (SQLException ex) {
            Logger.getLogger(NPCConversationManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        ps.close();
        rs.close();
        return ret.toString();
    }

    public long MRushAmount = 700000000000L;

    public long getMMesos() throws SQLException {
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT mesos FROM mrush");
        ResultSet rs = ps.executeQuery();
        try {
            while (rs.next()) {
                MRushAmount = rs.getLong("mesos");
            }
        } catch (SQLException ex) {
            Logger.getLogger(NPCConversationManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        ps.close();
        rs.close();
        return MRushAmount;
    }

    public long Mesos = getPlayer().getMeso();

    public long getPMesos() throws SQLException {
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT meso FROM characters");
        ResultSet rs = ps.executeQuery();
        try {
            while (rs.next()) {
                Mesos = rs.getLong("meso");
            }
        } catch (SQLException ex) {
            Logger.getLogger(NPCConversationManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        ps.close();
        rs.close();
        return Mesos;
    }

    public void resetMonsterRush() throws SQLException {
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE mrush SET mesos = 700000000000");
        ps.executeUpdate();
        ps.close();
    }

    public void deduct50M() {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE mrush SET mesos = ?");
            //ResultSet rs = ps.executeQuery();
            if (getMMesos() <= 50000000) {
                ps.setLong(1, 0); // set total to 0
            } else {
                ps.setLong(1, getMMesos() - 50000000); //see if it will subtract or do anything at all. :)
            }
            ps.executeUpdate();
            ps.close();
            //rs.close();
        } catch (SQLException e) {
            Logger.getLogger(NPCConversationManager.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void deduct100M() {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE mrush SET mesos = ?");
            if (getMMesos() <= 50000000) {
                ps.setLong(1, 0); // set total to 0
            } else {
                ps.setLong(1, getMMesos() - 100000000);
            }
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            Logger.getLogger(NPCConversationManager.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void deduct200M() {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE mrush SET mesos = ?");
            if (getMMesos() <= 50000000) {
                ps.setLong(1, 0); // set total to 0
            } else {
                ps.setLong(1, getMMesos() - 200000000);
            }
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            Logger.getLogger(NPCConversationManager.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void deduct400M() {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE mrush SET mesos = ?");
            if (getMMesos() <= 50000000) {
                ps.setLong(1, 0); // set total to 0
            } else {
                ps.setLong(1, getMMesos() - 400000000);
            }
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            Logger.getLogger(NPCConversationManager.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void deduct800M() {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE mrush SET mesos = ?");
            if (getMMesos() <= 50000000) {
                ps.setLong(1, 0); // set total to 0
            } else {
                ps.setLong(1, getMMesos() - 800000000);
            }
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            Logger.getLogger(NPCConversationManager.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void deduct1_5B() {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE mrush SET mesos = ?");
            if (getMMesos() <= 50000000) {
                ps.setLong(1, 0); // set total to 0
            } else {
                ps.setLong(1, getMMesos() - 1500000000);
            }
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            Logger.getLogger(NPCConversationManager.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void deduct69() {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE mrush SET mesos = ?");
            if (getMMesos() <= 69) {
                ps.setLong(1, 0); // set total to 0
            } else {
                ps.setLong(1, getMMesos() - 69);
            }
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            Logger.getLogger(NPCConversationManager.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void deductAll() {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE mrush SET mesos = ?");
            ps.setLong(1, getMMesos() - getMeso());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            Logger.getLogger(NPCConversationManager.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void startMonsterRush() { // don't use my sloppy code until i re-build this entire system #EricsOldCodeIsLul
        for (int l = 1; l <= LoginServer.getWorlds().size(); l++) { //ChannelServer instance [l]
            //ChannelServer.getInstance(l).MonsterRush = true;
            //MapleMonsterStats newStats = new MapleMonsterStats();
            //newStats.setHp(2000000000);
            //newStats.setLevel((short)195);
            //newStats.setExp(200000000); //EXP = 250x, 8589934 * 250 = MAX value rounded up, 140less then Integer.MAX_VALUE. EXP equal from leveling 1 to 200 per mob LOL
            MapleMap map = c.getChannelServer().getMapFactory().getMap(100000000);
            MapleMap map1 = c.getChannelServer().getMapFactory().getMap(103000000);
            MapleMap map2 = c.getChannelServer().getMapFactory().getMap(680000000);
            MapleMap map3 = c.getChannelServer().getMapFactory().getMap(220000000);
            MapleMap map4 = c.getChannelServer().getMapFactory().getMap(200000000);
            MapleMap map5 = c.getChannelServer().getMapFactory().getMap(240000000);
            for (int i = 0; i < 25; i++) { //Monster Spawning Instance [i]
                MapleMonster npcmob = MapleLifeFactory.getMonster(9400203);
                //npcmob.setOverrideStats(newStats);
                npcmob.setHp(npcmob.getMobMaxHp());
                npcmob.setMp(npcmob.getMobMaxMp());
                map.spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9400203), new Point(72, 274));
                map1.spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9400203), new Point(72, 274));
                map2.spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9400203), new Point(72, 274));
                map3.spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9400203), new Point(72, 274));
                map4.spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9400203), new Point(72, 274));
                map5.spawnMonsterOnGroudBelow(MapleLifeFactory.getMonster(9400203), new Point(72, 274));
            }
        }
    }

    public int getJQMap() {
        return World.getEventMap();
    }

    public int getEventMap() {

        if (getPlayer().getMapId() == 690000067 && World.getEventMap() == 690000066) {
            return 690000067; // Forest of Patience
        } else if (getPlayer().getMapId() == 280020001 && World.getEventMap() == 280020000) {
            return 280020001; // Zakum
        } else if (getPlayer().getMapId() == 109040004 && World.getEventMap() == 109040000) {
            return 109040004; // Fitness
        } else if (getPlayer().getMapId() == 910130102 && World.getEventMap() == 910130100) {
            return 910130102; // Forest of Endurance
        } else if (getPlayer().getMapId() == 910530001 && World.getEventMap() == 910530000) {
            return 910530001; // Forest of Tenacity
        } else if (getPlayer().getMapId() == World.getEventMap()) {
            return World.getEventMap();
        } else {
            return 0; // invalid - will return unavailable | wrong map
        }
    }

    public boolean getEventMapWarp(MapleCharacter player) {

        if (player.getMapId() >= 690000066 && player.getMapId() <= 690000067) {
            return true; // Forest of Patience
        } else if (player.getMapId() >= 280020000 && player.getMapId() <= 280020001) {
            return true; // Zakum
        } else if (player.getMapId() >= 109040000 && player.getMapId() <= 109040004) {
            return true; // Fitness
        } else if (player.getMapId() >= 910130100 && player.getMapId() <= 910130102) {
            return true; // Forest of Endurance
        } else if (player.getMapId() >= 910530000 && player.getMapId() <= 910530001) {
            return true; // Forest of Tenacity
        } else return player.getMapId() == World.getEventMap(); // invalid - will return unavailable | wrong map
        
    }

    public int getEventMapByGM() {
        return c.getChannelServer().eventMap;
    }

    public int getMapleEvent() {
        // if (getChannelServer().getEvent() > 0) {
        //   if (getChannelServer().getEvent() == 109080000 || getChannelServer().getEvent() == 109080010) {
        //       warp(getChannelServer().getEvent(), 0);
        //   } else {
        //       warp(getChannelServer().getEvent(), "join00");
        //   }
        return -1;//getChannelServer().getEventMap();
        // }
    }

    public void changeOccupationById(int occ) {
        getPlayer().changeOccupation(Occupations.getById(occ));
    }

    public boolean hasOccupation() {
        return (getPlayer().retrieveOccupation().getId() % 100 == 0);
    }

    public void setEventMap(int id) {
        World.setEventMap(id);
        World.setJQChannel(-1); // could use 0 but whatever? o.O this will never be used unless a GM has hosted
    }

    public void gainCurrency(short amount) {
        MapleInventoryManipulator.addById(c, ServerConstants.Currency, (short) amount, "AutoJQ Reward");
    }

    public void makeCustomPet(int petid) {
        if (petid >= 5000000 && petid <= 5000500) {
            MapleInventoryManipulator.addById(c, petid, (short) 1, "", MaplePet.createPet(petid, MapleItemInformationProvider.getInstance().getName(petid), 1, 0, 100, MapleInventoryIdentifier.getInstance(), 0, (short) 0), 20000, "");
        } else {
            getPlayer().dropMessage(1, "The item you just received is not a pet, please report this.");
            System.out.println("ERROR: makeCustomPet AT NPCConversationManager :: Unable to create pet of id " + petid);
        }
    }

    public int setRandomAvatar(int ticket, int... args_all) {
        if (!haveItem(ticket)) {
            return -1;
        }
        gainItem(ticket, (short) -1);

        int args = args_all[Randomizer.nextInt(args_all.length)];
        if (args < 100) {
            c.getPlayer().setSkinColor((byte) args);
            c.getPlayer().updateSingleStat(MapleStat.SKIN, args);
        } else if (args < 30000) {
            c.getPlayer().setFace(args);
            c.getPlayer().updateSingleStat(MapleStat.FACE, args);
        } else {
            c.getPlayer().setHair(args);
            c.getPlayer().updateSingleStat(MapleStat.HAIR, args);
        }
        c.getPlayer().equipChanged();

        return 1;
    }

    public int setAvatar(int ticket, int args) {
        if (!haveItem(ticket)) {
            return -1;
        }
        gainItem(ticket, (short) -1);

        if (args < 100) {
            c.getPlayer().setSkinColor((byte) args);
            c.getPlayer().updateSingleStat(MapleStat.SKIN, args);
        } else if (args < 30000) {
            c.getPlayer().setFace(args);
            c.getPlayer().updateSingleStat(MapleStat.FACE, args);
        } else {
            c.getPlayer().setHair(args);
            c.getPlayer().updateSingleStat(MapleStat.HAIR, args);
        }
        c.getPlayer().equipChanged();

        return 1;
    }

    // For use of cm.haveMeso(100000); (Was this previously used in sources? I could've swarn..)
    public boolean haveMeso(int meso) {
        return getPlayer().getMeso() >= meso;
    }

    public void gainReborns(int reborns) {
        getPlayer().setReborns(reborns + getPlayer().getReborns());
    }

    public MapleCharacter getCharByName(String name) {
        return c.getChannelServer().getPlayerStorage().getCharacterByName(name);
    }

    public void sendStorage() {
        c.getPlayer().setConversation(4);
        c.getPlayer().getStorage().sendStorage(c, id);
    }

    public void openShop(int id) {
        MapleShopFactory.getInstance().getShop(id).sendShop(c);
    }

    public void openShopNPC(int id) {
        MapleShopFactory.getInstance().getShop(id).sendShop(c, this.id);
    }

    public int gainGachaponItem(int id, int quantity) {
        return gainGachaponItem(id, quantity, c.getPlayer().getMap().getStreetName());
    }

    public int gainGachaponItem(int id, int quantity, final String msg) {
        try {
            if (!MapleItemInformationProvider.getInstance().itemExists(id)) {
                return -1;
            }
            final Item item = MapleInventoryManipulator.addbyId_Gachapon(c, id, (short) quantity);

            if (item == null) {
                return -1;
            }
            final byte rareness = GameConstants.gachaponRareItem(item.getItemId());
            if (rareness > 0) {
                World.Broadcast.broadcastMessage(CWvsContext.getGachaponMega(c.getPlayer().getName(), " : 得到了 ", item, rareness, msg));
            }
            c.getSession().write(InfoPacket.getShowItemGain(item.getItemId(), (short) quantity, true));
            return item.getItemId();
        } catch (Exception e) {
        }
        return -1;
    }

    public int useNebuliteGachapon() {
        try {
            if (c.getPlayer().getInventory(MapleInventoryType.EQUIP).getNumFreeSlot() < 1
                    || c.getPlayer().getInventory(MapleInventoryType.USE).getNumFreeSlot() < 1
                    || c.getPlayer().getInventory(MapleInventoryType.SETUP).getNumFreeSlot() < 1
                    || c.getPlayer().getInventory(MapleInventoryType.ETC).getNumFreeSlot() < 1
                    || c.getPlayer().getInventory(MapleInventoryType.CASH).getNumFreeSlot() < 1) {
                return -1;
            }
            int grade; // Default D
            final int chance = Randomizer.nextInt(100); // cannot gacha S, only from alien cube.
            if (chance < 1) { // Grade A
                grade = 3;
            } else if (chance < 5) { // Grade B
                grade = 2;
            } else if (chance < 35) { // Grade C
                grade = 1;
            } else { // grade == 0
                grade = Randomizer.nextInt(100) < 25 ? 5 : 0; // 25% again to get premium ticket piece				
            }
            int newId = 0;
            if (grade == 5) {
                newId = 4420000;
            } else {
                final MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
                final List<StructItemOption> pots = new LinkedList<>(ii.getAllSocketInfo(grade).values());
                while (newId == 0) {
                    StructItemOption pot = pots.get(Randomizer.nextInt(pots.size()));
                    if (pot != null) {
                        newId = pot.opID;
                    }
                }
            }
            final Item item = MapleInventoryManipulator.addbyId_Gachapon(c, newId, (short) 1);
            if (item == null) {
                return -1;
            }
            if (grade >= 2 && grade != 5) {
                World.Broadcast.broadcastMessage(CWvsContext.getGachaponMega(c.getPlayer().getName(), " : 得到了 ", item, (byte) 0, "Maple World"));
            }
            c.getSession().write(InfoPacket.getShowItemGain(newId, (short) 1, true));
            gainItem(2430748, (short) 1);
            gainItemSilent(5220094, (short) -1);
            return item.getItemId();
        } catch (Exception e) {
            System.out.println("[Error] Failed to use Nebulite Gachapon. " + e);
        }
        return -1;
    }

    public void sendSimple(String text, String... selections) {
        if (selections.length > 0) // Adding this even if selections length is 0 will do anything, but whatever.
        {
            text += "#b\r\n";
        }
        for (int i = 0; i < selections.length; i++) {
            text += "#L" + i + "#" + selections[i] + "#l\r\n";
        }
        sendSimple(text, id);
    }

    public void changeJob(int job) {
        c.getPlayer().changeJob(job);
    }

    public void startQuest(int idd) {
        MapleQuest.getInstance(idd).start(getPlayer(), id);
    }

    public void completeQuest(int idd) {
        MapleQuest.getInstance(idd).complete(getPlayer(), id);
    }

    public void forfeitQuest(int idd) {
        MapleQuest.getInstance(idd).forfeit(getPlayer());
    }

    public void forceStartQuest() {
        MapleQuest.getInstance(id2).forceStart(getPlayer(), getNpc(), null);
    }

    @Override
    public void forceStartQuest(int idd) {
        MapleQuest.getInstance(idd).forceStart(getPlayer(), getNpc(), null);
    }

    public void forceStartQuest(String customData) {
        MapleQuest.getInstance(id2).forceStart(getPlayer(), getNpc(), customData);
    }

    public void forceCompleteQuest() {
        MapleQuest.getInstance(id2).forceComplete(getPlayer(), getNpc());
    }

    @Override
    public void forceCompleteQuest(final int idd) {
        MapleQuest.getInstance(idd).forceComplete(getPlayer(), getNpc());
    }

    public String getQuestCustomData() {
        return c.getPlayer().getQuestNAdd(MapleQuest.getInstance(id2)).getCustomData();
    }

    public void setQuestCustomData(String customData) {
        getPlayer().getQuestNAdd(MapleQuest.getInstance(id2)).setCustomData(customData);
    }

    public String getQuestCustomData(int qid) {
        return c.getPlayer().getQuestNAdd(MapleQuest.getInstance(qid)).getCustomData();
    }

    public void setQuestCustomData(int qid, String customData) {
        getPlayer().getQuestNAdd(MapleQuest.getInstance(qid)).setCustomData(customData);
    }

    public int getMeso() {
        return getPlayer().getMeso();
    }

    public void gainAp(final int amount) {
        c.getPlayer().gainAp((short) amount);
    }

    public void expandInventory(byte type, int amt) {
        c.getPlayer().expandInventory(type, amt);
    }

    public void unequipEverything() {
        MapleInventory equipped = getPlayer().getInventory(MapleInventoryType.EQUIPPED);
        MapleInventory equip = getPlayer().getInventory(MapleInventoryType.EQUIP);
        List<Short> ids = new LinkedList<>();
        equipped.newList().forEach((item) -> {
            ids.add(item.getPosition());
        });
        ids.forEach((idvs) -> {
            MapleInventoryManipulator.unequip(getC(), idvs, equip.getNextFreeSlot());
        });
    }

    public final void clearSkills() {
        final Map<Skill, SkillEntry> skills = new HashMap<>(getPlayer().getSkills());
        final Map<Skill, SkillEntry> newList = new HashMap<>();
        skills.entrySet().forEach((skill) -> {
            newList.put(skill.getKey(), new SkillEntry((byte) 0, (byte) 0, -1));
        });
        getPlayer().changeSkillsLevel(newList);
        newList.clear();
        skills.clear();
    }

    public boolean hasSkill(int skillid) {
        Skill theSkill = SkillFactory.getSkill(skillid);
        if (theSkill != null) {
            return c.getPlayer().getSkillLevel(theSkill) > 0;
        }
        return false;
    }

    public void showEffect(boolean broadcast, String effect) {
        if (broadcast) {
            c.getPlayer().getMap().broadcastMessage(CField.showEffect(effect));
        } else {
            c.getSession().write(CField.showEffect(effect));
        }
    }

    public void playSound(boolean broadcast, String sound) {
        if (broadcast) {
            c.getPlayer().getMap().broadcastMessage(CField.playSound(sound));
        } else {
            c.getSession().write(CField.playSound(sound));
        }
    }

    public void environmentChange(boolean broadcast, String env) {
        if (broadcast) {
            c.getPlayer().getMap().broadcastMessage(CField.environmentChange(env, 2));
        } else {
            c.getSession().write(CField.environmentChange(env, 2));
        }
    }

    public void updateBuddyCapacity(int capacity) {
        c.getPlayer().setBuddyCapacity((byte) capacity);
    }

    public int getBuddyCapacity() {
        return c.getPlayer().getBuddyCapacity();
    }

    public int partyMembersInMap() {
        int inMap = 0;
        if (getPlayer().getParty() == null) {
            return inMap;
        }
        inMap = getPlayer().getMap().getCharactersThreadsafe().stream().filter((char2) -> (char2.getParty() != null && char2.getParty().getId() == getPlayer().getParty().getId())).map((_item) -> 1).reduce(inMap, Integer::sum);
        return inMap;
    }

    public List<MapleCharacter> getPartyMembers() {
        if (getPlayer().getParty() == null) {
            return null;
        }
        List<MapleCharacter> chars = new LinkedList<>(); // creates an empty array full of shit..
        getPlayer().getParty().getMembers().forEach((chr) -> {
            LoginServer.getInstance().getWorld(c.getWorld()).getChannels().stream().map((channel) -> channel.getPlayerStorage().getCharacterById(chr.getId())).filter((ch) -> (ch != null)).forEachOrdered((ch) -> {
                // double check <3
                chars.add(ch);
            });
        });
        return chars;
    }

    public void warpPartyWithExp(int mapId, int exp) {
        if (getPlayer().getParty() == null) {
            warp(mapId, 0);
            gainExp(exp);
            return;
        }
        MapleMap target = getMap(mapId);
        getPlayer().getParty().getMembers().stream().map((chr) -> c.getChannelServer().getPlayerStorage().getCharacterByName(chr.getName())).filter((curChar) -> ((curChar.getEventInstance() == null && getPlayer().getEventInstance() == null) || curChar.getEventInstance() == getPlayer().getEventInstance())).map((curChar) -> {
            curChar.changeMap(target, target.getPortal(0));
            return curChar;
        }).forEachOrdered((curChar) -> {
            curChar.gainExp(exp, true, false, true);
        });
    }

    public void warpPartyWithExpMeso(int mapId, int exp, int meso) {
        if (getPlayer().getParty() == null) {
            warp(mapId, 0);
            gainExp(exp);
            gainMeso(meso);
            return;
        }
        MapleMap target = getMap(mapId);
        getPlayer().getParty().getMembers().stream().map((chr) -> c.getChannelServer().getPlayerStorage().getCharacterByName(chr.getName())).filter((curChar) -> ((curChar.getEventInstance() == null && getPlayer().getEventInstance() == null) || curChar.getEventInstance() == getPlayer().getEventInstance())).map((curChar) -> {
            curChar.changeMap(target, target.getPortal(0));
            return curChar;
        }).map((curChar) -> {
            curChar.gainExp(exp, true, false, true);
            return curChar;
        }).forEachOrdered((curChar) -> {
            curChar.gainMeso(meso, true);
        });
    }

    public void openGate() { // for MV's Lair
        if (getPlayer().getParty() == null) {
            return;
        }
        getPlayer().getParty().getMembers().stream().map((chr) -> c.getChannelServer().getPlayerStorage().getCharacterByName(chr.getName())).filter((curChar) -> ((curChar.getEventInstance() == null && getPlayer().getEventInstance() == null) || curChar.getEventInstance() == getPlayer().getEventInstance())).forEachOrdered((curChar) -> {
            curChar.gate[4] = true;
        });
    }

    public void closeGate() { // for MV's Lair
        if (getPlayer().getParty() == null) {
            return;
        }
        getPlayer().getParty().getMembers().stream().map((chr) -> c.getChannelServer().getPlayerStorage().getCharacterByName(chr.getName())).filter((curChar) -> ((curChar.getEventInstance() == null && getPlayer().getEventInstance() == null) || curChar.getEventInstance() == getPlayer().getEventInstance())).forEachOrdered((curChar) -> {
            curChar.gate[4] = false;
        });
    }

    public MapleSquad getSquad(String type) {
        return c.getChannelServer().getMapleSquad(type);
    }

    public int getSquadAvailability(String type) {
        final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad == null) {
            return -1;
        }
        return squad.getStatus();
    }

    public boolean registerSquad(String type, int minutes, String startText) {
        if (c.getChannelServer().getMapleSquad(type) == null) {
            final MapleSquad squad = new MapleSquad(c.getWorld(), c.getChannel(), type, c.getPlayer(), minutes * 60 * 1000, startText);
            final boolean ret = c.getChannelServer().addMapleSquad(squad, type);
            if (ret) {
                final MapleMap map = c.getPlayer().getMap();

                map.broadcastMessage(CField.getClock(minutes * 60));
                map.broadcastMessage(CWvsContext.serverNotice(6, c.getPlayer().getName() + startText));
            } else {
                squad.clear();
            }
            return ret;
        }
        return false;
    }

    public boolean getSquadList(String type, byte type_) {
        try {
            final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
            if (squad == null) {
                return false;
            }
            switch (type_) {
                case 0:
                case 3:
                    // Normal viewing
                    sendNext(squad.getSquadMemberString(type_));
                    break;
                case 1:
                    // Squad Leader banning, Check out banned participant
                    sendSimple(squad.getSquadMemberString(type_));
                    break;
                case 2:
                    if (squad.getBannedMemberSize() > 0) {
                        sendSimple(squad.getSquadMemberString(type_));
                    } else {
                        sendNext(squad.getSquadMemberString(type_));
                    }   break;
                default:
                    break;
            }
            return true;
        } catch (NullPointerException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.ScriptEx_Log, ex);
            return false;
        }
    }

    public byte isSquadLeader(String type) {
        final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad == null) {
            return -1;
        } else {
            if (squad.getLeader() != null && squad.getLeader().getId() == c.getPlayer().getId()) {
                return 1;
            } else {
                return 0;
            }
        }
    }

    public boolean reAdd(String eim, String squad) {
        EventInstanceManager eimz = getDisconnected(eim);
        MapleSquad squadz = getSquad(squad);
        if (eimz != null && squadz != null) {
            squadz.reAddMember(getPlayer());
            eimz.registerPlayer(getPlayer());
            return true;
        }
        return false;
    }

    public void banMember(String type, int pos) {
        final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            squad.banMember(pos);
        }
    }

    public void acceptMember(String type, int pos) {
        final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad != null) {
            squad.acceptMember(pos);
        }
    }

    public int addMember(String type, boolean join) {
        try {
            final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
            if (squad != null) {
                return squad.addMember(c.getPlayer(), join);
            }
            return -1;
        } catch (NullPointerException ex) {
            FileoutputUtil.outputFileError(FileoutputUtil.ScriptEx_Log, ex);
            return -1;
        }
    }

    public byte isSquadMember(String type) {
        final MapleSquad squad = c.getChannelServer().getMapleSquad(type);
        if (squad == null) {
            return -1;
        } else {
            if (squad.getMembers().contains(c.getPlayer().getName())) {
                return 1;
            } else if (squad.isBanned(c.getPlayer())) {
                return 2;
            } else {
                return 0;
            }
        }
    }

    public void resetReactors() {
        getPlayer().getMap().resetReactors();
    }

    public void genericGuildMessage(int code) {
        c.getSession().write(GuildPacket.genericGuildMessage((byte) code));
    }

    public void disbandGuild() {
        final int gid = c.getPlayer().getGuildId();
        if (gid <= 0 || c.getPlayer().getGuildRank() != 1) {
            return;
        }
        World.Guild.disbandGuild(gid);
    }

    public void increaseGuildCapacity(boolean trueMax) {
        if (c.getPlayer().getMeso() < 500000 && !trueMax) {
            c.getSession().write(CWvsContext.serverNotice(1, " 您的金錢不足"));
            return;
        }
        final int gid = c.getPlayer().getGuildId();
        if (gid <= 0) {
            return;
        }
        if (World.Guild.increaseGuildCapacity(gid, trueMax)) {
            if (!trueMax) {
                c.getPlayer().gainMeso(-500000, true, true);
            } else {
                gainGP(-25000);
            }
            sendNext("Your guild capacity has been raised...");
        } else if (!trueMax) {
            sendNext("Please check if your guild capacity is full. (Limit: 100)");
        } else {
            sendNext("Please check if your guild capacity is full, if you have the GP needed or if subtracting GP would decrease a guild level. (Limit: 200)");
        }
    }

    public void displayGuildRanks() {
        c.getSession().write(GuildPacket.showGuildRanks(id, MapleGuildRanking.getInstance().getRank()));
    }

    public boolean removePlayerFromInstance() {
        if (c.getPlayer().getEventInstance() != null) {
            c.getPlayer().getEventInstance().removePlayer(c.getPlayer());
            return true;
        }
        return false;
    }

    public boolean isPlayerInstance() {
        return c.getPlayer().getEventInstance() != null;
    }

    public void changeStat(byte slot, int type, int amount) {
        Equip sel = (Equip) c.getPlayer().getInventory(MapleInventoryType.EQUIPPED).getItem(slot);
        switch (type) {
            case 0:
                sel.setStr((short) amount);
                break;
            case 1:
                sel.setDex((short) amount);
                break;
            case 2:
                sel.setInt((short) amount);
                break;
            case 3:
                sel.setLuk((short) amount);
                break;
            case 4:
                sel.setHp((short) amount);
                break;
            case 5:
                sel.setMp((short) amount);
                break;
            case 6:
                sel.setWatk((short) amount);
                break;
            case 7:
                sel.setMatk((short) amount);
                break;
            case 8:
                sel.setWdef((short) amount);
                break;
            case 9:
                sel.setMdef((short) amount);
                break;
            case 10:
                sel.setAcc((short) amount);
                break;
            case 11:
                sel.setAvoid((short) amount);
                break;
            case 12:
                sel.setHands((short) amount);
                break;
            case 13:
                sel.setSpeed((short) amount);
                break;
            case 14:
                sel.setJump((short) amount);
                break;
            case 15:
                sel.setUpgradeSlots((byte) amount);
                break;
            case 16:
                sel.setViciousHammer((byte) amount);
                break;
            case 17:
                sel.setLevel((byte) amount);
                break;
            case 18:
                sel.setEnhance((byte) amount);
                break;
            case 19:
                sel.setPotential1(amount);
                break;
            case 20:
                sel.setPotential2(amount);
                break;
            case 21:
                sel.setPotential3(amount);
                break;
            case 22:
                sel.setPotential4(amount);
                break;
            case 23:
                sel.setPotential5(amount);
                break;
            case 24:
                sel.setOwner(getText());
                break;
            default:
                break;
        }
        c.getPlayer().equipChanged();
        c.getPlayer().fakeRelog();
    }

    public void openDuey() {
        c.getPlayer().setConversation(2);
        c.getSession().write(CField.sendDuey((byte) 9, null));
    }

    public void openMerchantItemStore() {
        c.getPlayer().setConversation(3);
        HiredMerchantHandler.displayMerch(c);
        //c.getSession().write(PlayerShopPacket.merchItemStore((byte) 0x22));
        //c.getPlayer().dropMessage(5, "Please enter ANY 13 characters.");
    }

    public void sendPVPWindow() {
        c.getSession().write(UIPacket.openUI(50));
        c.getSession().write(CField.sendPVPMaps());
    }

    public void sendDojoRanks() {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT `name`, `time` FROM dojo_ranks ORDER BY `time` ASC LIMIT 50");
            ResultSet rs = ps.executeQuery();
            c.getSession().write(CWvsContext.getMulungRanks(rs));
            ps.close();
            rs.close();
        } catch (SQLException e) {
            System.out.println("Failed to load Mu Lung Ranking. " + e);
        }
    }

    public void setDojoMode(int mode) {
        getPlayer().setDojoMode(getPlayer().getDojoMode(mode));
    }

    public void findParty() {
        c.getSession().write(UIPacket.openUI(21));
    }

    public void sendAzwanWindow() {
        c.getSession().write(UIPacket.openUI(0x46));
    }

    public void sendRepairWindow() {
        c.getSession().write(UIPacket.sendRepairWindow(id));
    }

    public void sendProfessionWindow() {
        c.getSession().write(UIPacket.openUI(42));
    }

    public final int getDojoPoints() {
        return dojo_getPts();
    }

    public final int getDojoRecord() {
        return c.getPlayer().getIntNoRecord(GameConstants.DOJO_RECORD);
    }

    public void setDojoRecord(final boolean reset) {
        if (reset) {
            c.getPlayer().getQuestNAdd(MapleQuest.getInstance(GameConstants.DOJO_RECORD)).setCustomData("0");
            c.getPlayer().getQuestNAdd(MapleQuest.getInstance(GameConstants.DOJO)).setCustomData("0");
        } else {
            c.getPlayer().getQuestNAdd(MapleQuest.getInstance(GameConstants.DOJO_RECORD)).setCustomData(String.valueOf(c.getPlayer().getIntRecord(GameConstants.DOJO_RECORD) + 1));
        }
    }

    public void takeDojoPoints(final int data) {
        if (data < 1) {
            c.getPlayer().getQuestNAdd(MapleQuest.getInstance(GameConstants.DOJO_RECORD)).setCustomData("0");
            c.getPlayer().getQuestNAdd(MapleQuest.getInstance(GameConstants.DOJO)).setCustomData("0");
        } else {
            final int dojo = c.getPlayer().getIntRecord(GameConstants.DOJO) - data;
            c.getPlayer().getQuestNAdd(MapleQuest.getInstance(GameConstants.DOJO)).setCustomData(String.valueOf(dojo));
        }
    }

    public void addDojoPoints(final int data) {
        final int dojo = c.getPlayer().getIntRecord(GameConstants.DOJO) + data;
        c.getPlayer().getQuestNAdd(MapleQuest.getInstance(GameConstants.DOJO)).setCustomData(String.valueOf(dojo));
        // c.getPlayer().getClient().getSession().write(CWvsContext.Mulung_Pts(data, dojo));
    }

    public boolean start_DojoAgent(final boolean dojo, final boolean party) {
        if (dojo) {
            return Event_DojoAgent.warpStartDojo(c.getPlayer(), party);
        }
        return Event_DojoAgent.warpStartAgent(c.getPlayer(), party);
    }

    public boolean start_PyramidSubway(final int pyramid) {
        if (pyramid >= 0) {
            return Event_PyramidSubway.warpStartPyramid(c.getPlayer(), pyramid);
        }
        return Event_PyramidSubway.warpStartSubway(c.getPlayer());
    }

    public boolean bonus_PyramidSubway(final int pyramid) {
        if (pyramid >= 0) {
            return Event_PyramidSubway.warpBonusPyramid(c.getPlayer(), pyramid);
        }
        return Event_PyramidSubway.warpBonusSubway(c.getPlayer());
    }

    public final short getKegs() {
        return c.getChannelServer().getFireWorks().getKegsPercentage();
    }

    public void giveKegs(final int kegs) {
        c.getChannelServer().getFireWorks().giveKegs(c.getPlayer(), kegs);
    }

    public final short getSunshines() {
        return c.getChannelServer().getFireWorks().getSunsPercentage();
    }

    public void addSunshines(final int kegs) {
        c.getChannelServer().getFireWorks().giveSuns(c.getPlayer(), kegs);
    }

    public final short getDecorations() {
        return c.getChannelServer().getFireWorks().getDecsPercentage();
    }

    public void addDecorations(final int kegs) {
        try {
            c.getChannelServer().getFireWorks().giveDecs(c.getPlayer(), kegs);
        } catch (Exception e) {
        }
    }

    public final MapleCarnivalParty getCarnivalParty() {
        return c.getPlayer().getCarnivalParty();
    }

    public final MapleCarnivalChallenge getNextCarnivalRequest() {
        return c.getPlayer().getNextCarnivalRequest();
    }

    public final MapleCarnivalChallenge getCarnivalChallenge(MapleCharacter chr) {
        return new MapleCarnivalChallenge(chr);
    }

    public void maxStats() {
        Map<MapleStat, Integer> statup = new EnumMap<>(MapleStat.class);
        c.getPlayer().getStat().str = (short) 32767;
        c.getPlayer().getStat().dex = (short) 32767;
        c.getPlayer().getStat().int_ = (short) 32767;
        c.getPlayer().getStat().luk = (short) 32767;

        int overrDemon = GameConstants.isDemon(c.getPlayer().getJob()) ? GameConstants.getMPByJob(c.getPlayer().getJob()) : 99999;
        c.getPlayer().getStat().maxhp = 99999;
        c.getPlayer().getStat().maxmp = overrDemon;
        c.getPlayer().getStat().setHp(99999, c.getPlayer());
        c.getPlayer().getStat().setMp(overrDemon, c.getPlayer());

        statup.put(MapleStat.STR, 32767);
        statup.put(MapleStat.DEX, 32767);
        statup.put(MapleStat.LUK, 32767);
        statup.put(MapleStat.INT, 32767);
        statup.put(MapleStat.HP, 99999);
        statup.put(MapleStat.MAXHP, 99999);
        statup.put(MapleStat.MP, overrDemon);
        statup.put(MapleStat.MAXMP, overrDemon);
        c.getPlayer().getStat().recalcLocalStats(c.getPlayer());
        c.getSession().write(CWvsContext.updatePlayerStats(statup, c.getPlayer()));
    }

    public Triple<String, Map<Integer, String>, Long> getSpeedRun(String typ) {
        final ExpeditionType fefea = ExpeditionType.valueOf(typ);
        if (SpeedRunner.getSpeedRunData(fefea) != null) {
            return SpeedRunner.getSpeedRunData(fefea);
        }
        return new Triple<String, Map<Integer, String>, Long>("", new HashMap<Integer, String>(), 0L);
    }

    public boolean getSR(Triple<String, Map<Integer, String>, Long> ma, int sel) {
        if (ma.mid.get(sel) == null || ma.mid.get(sel).length() <= 0) {
            dispose();
            return false;
        }
        sendOk(ma.mid.get(sel));
        return true;
    }

    public Equip getEquip(int itemid) {
        return (Equip) MapleItemInformationProvider.getInstance().getEquipById(itemid);
    }

    public void setExpiration(Object statsSel, long expire) {
        if (statsSel instanceof Equip) {
            ((Equip) statsSel).setExpiration(System.currentTimeMillis() + (expire * 24 * 60 * 60 * 1000));
        }
    }

    public void setLock(Object statsSel) {
        if (statsSel instanceof Equip) {
            Equip eq = (Equip) statsSel;
            if (eq.getExpiration() == -1) {
                eq.setFlag((byte) (eq.getFlag() | ItemFlag.LOCK.getValue()));
            } else {
                eq.setFlag((byte) (eq.getFlag() | ItemFlag.UNTRADEABLE.getValue()));
            }
        }
    }

    public boolean addFromDrop(Object statsSel) {
        if (statsSel instanceof Item) {
            final Item it = (Item) statsSel;
            return MapleInventoryManipulator.checkSpace(getClient(), it.getItemId(), it.getQuantity(), it.getOwner()) && MapleInventoryManipulator.addFromDrop(getClient(), it, false);
        }
        return false;
    }

    public boolean replaceItem(int slot, int invType, Object statsSel, int offset, String type) {
        return replaceItem(slot, invType, statsSel, offset, type, false);
    }

    public boolean replaceItem(int slot, int invType, Object statsSel, int offset, String type, boolean takeSlot) {
        MapleInventoryType inv = MapleInventoryType.getByType((byte) invType);
        if (inv == null) {
            return false;
        }
        Item item = getPlayer().getInventory(inv).getItem((byte) slot);
        if (item == null || statsSel instanceof Item) {
            item = (Item) statsSel;
        }
        if (offset > 0) {
            if (inv != MapleInventoryType.EQUIP) {
                return false;
            }
            Equip eq = (Equip) item;
            if (takeSlot) {
                if (eq.getUpgradeSlots() < 1) {
                    return false;
                } else {
                    eq.setUpgradeSlots((byte) (eq.getUpgradeSlots() - 1));
                }
                if (eq.getExpiration() == -1) {
                    eq.setFlag((byte) (eq.getFlag() | ItemFlag.LOCK.getValue()));
                } else {
                    eq.setFlag((byte) (eq.getFlag() | ItemFlag.UNTRADEABLE.getValue()));
                }
            }
            if (type.equalsIgnoreCase("Slots")) {
                eq.setUpgradeSlots((byte) (eq.getUpgradeSlots() + offset));
                eq.setViciousHammer((byte) (eq.getViciousHammer() + offset));
            } else if (type.equalsIgnoreCase("Level")) {
                eq.setLevel((byte) (eq.getLevel() + offset));
            } else if (type.equalsIgnoreCase("Hammer")) {
                eq.setViciousHammer((byte) (eq.getViciousHammer() + offset));
            } else if (type.equalsIgnoreCase("STR")) {
                eq.setStr((short) (eq.getStr() + offset));
            } else if (type.equalsIgnoreCase("DEX")) {
                eq.setDex((short) (eq.getDex() + offset));
            } else if (type.equalsIgnoreCase("INT")) {
                eq.setInt((short) (eq.getInt() + offset));
            } else if (type.equalsIgnoreCase("LUK")) {
                eq.setLuk((short) (eq.getLuk() + offset));
            } else if (type.equalsIgnoreCase("HP")) {
                eq.setHp((short) (eq.getHp() + offset));
            } else if (type.equalsIgnoreCase("MP")) {
                eq.setMp((short) (eq.getMp() + offset));
            } else if (type.equalsIgnoreCase("WATK")) {
                eq.setWatk((short) (eq.getWatk() + offset));
            } else if (type.equalsIgnoreCase("MATK")) {
                eq.setMatk((short) (eq.getMatk() + offset));
            } else if (type.equalsIgnoreCase("WDEF")) {
                eq.setWdef((short) (eq.getWdef() + offset));
            } else if (type.equalsIgnoreCase("MDEF")) {
                eq.setMdef((short) (eq.getMdef() + offset));
            } else if (type.equalsIgnoreCase("ACC")) {
                eq.setAcc((short) (eq.getAcc() + offset));
            } else if (type.equalsIgnoreCase("Avoid")) {
                eq.setAvoid((short) (eq.getAvoid() + offset));
            } else if (type.equalsIgnoreCase("Hands")) {
                eq.setHands((short) (eq.getHands() + offset));
            } else if (type.equalsIgnoreCase("Speed")) {
                eq.setSpeed((short) (eq.getSpeed() + offset));
            } else if (type.equalsIgnoreCase("Jump")) {
                eq.setJump((short) (eq.getJump() + offset));
            } else if (type.equalsIgnoreCase("ItemEXP")) {
                eq.setItemEXP(eq.getItemEXP() + offset);
            } else if (type.equalsIgnoreCase("Expiration")) {
                eq.setExpiration((long) (eq.getExpiration() + offset));
            } else if (type.equalsIgnoreCase("Flag")) {
                eq.setFlag((byte) (eq.getFlag() + offset));
            }
            item = eq.copy();
        }
        MapleInventoryManipulator.removeFromSlot(getClient(), inv, (short) slot, item.getQuantity(), false);
        return MapleInventoryManipulator.addFromDrop(getClient(), item, false);
    }

    public boolean replaceItem(int slot, int invType, Object statsSel, int upgradeSlots) {
        return replaceItem(slot, invType, statsSel, upgradeSlots, "Slots");
    }

    public boolean isCash(final int itemId) {
        return MapleItemInformationProvider.getInstance().isCash(itemId);
    }

    public void clearDrops() {
        MapleMap map = c.getPlayer().getMap();
        double range = Double.POSITIVE_INFINITY;
        java.util.List<MapleMapObject> items = map.getMapObjectsInRange(c.getPlayer().getPosition(), range, Arrays.asList(MapleMapObjectType.ITEM));
        items.stream().map((itemmo) -> {
            map.removeMapObject(itemmo);
            return itemmo;
        }).forEachOrdered((itemmo) -> {
            map.broadcastMessage(CField.removeItemFromMap(itemmo.getObjectId(), 0, c.getPlayer().getId()));
        });
    }

    public int getTotalStat(final int itemId) {
        return MapleItemInformationProvider.getInstance().getTotalStat((Equip) MapleItemInformationProvider.getEquipById(itemId));
    }

    public int getReqLevel(final int itemId) {
        return MapleItemInformationProvider.getInstance().getReqLevel(itemId);
    }

    public MapleStatEffect getEffect(int buff) {
        return MapleItemInformationProvider.getInstance().getItemEffect(buff);
    }

    public void buffGuild(final int buff, final int duration, final String msg) {
        MapleItemInformationProvider ii = MapleItemInformationProvider.getInstance();
        if (ii.getItemEffect(buff) != null && getPlayer().getGuildId() > 0) {
            final MapleStatEffect mse = ii.getItemEffect(buff);
            LoginServer.getInstance().getWorld(c.getWorld()).getChannels().forEach((cserv) -> {
                cserv.getPlayerStorage().getAllCharacters().stream().filter((chr) -> (chr.getGuildId() == getPlayer().getGuildId())).map((chr) -> {
                    mse.applyTo(chr, chr, true, null, duration);
                    return chr;
                }).forEachOrdered((chr) -> {
                    chr.dropMessage(5, "Your guild has gotten a " + msg + " buff.");
                });
            });
        }
    }

    public boolean createAlliance(String alliancename) {
        MapleParty pt = c.getPlayer().getParty();
        MapleCharacter otherChar = c.getChannelServer().getPlayerStorage().getCharacterById(pt.getMemberByIndex(1).getId());
        if (otherChar == null || otherChar.getId() == c.getPlayer().getId()) {
            return false;
        }
        try {
            return World.Alliance.createAlliance(alliancename, c.getPlayer().getId(), otherChar.getId(), c.getPlayer().getGuildId(), otherChar.getGuildId());
        } catch (Exception re) {
            return false;
        }
    }

    public boolean addCapacityToAlliance() {
        try {
            final MapleGuild gs = World.Guild.getGuild(c.getPlayer().getGuildId());
            if (gs != null && c.getPlayer().getGuildRank() == 1 && c.getPlayer().getAllianceRank() == 1) {
                if (World.Alliance.getAllianceLeader(gs.getAllianceId()) == c.getPlayer().getId() && World.Alliance.changeAllianceCapacity(gs.getAllianceId())) {
                    gainMeso(-MapleGuildAlliance.CHANGE_CAPACITY_COST);
                    return true;
                }
            }
        } catch (Exception re) {
        }
        return false;
    }

    public boolean disbandAlliance() {
        try {
            final MapleGuild gs = World.Guild.getGuild(c.getPlayer().getGuildId());
            if (gs != null && c.getPlayer().getGuildRank() == 1 && c.getPlayer().getAllianceRank() == 1) {
                if (World.Alliance.getAllianceLeader(gs.getAllianceId()) == c.getPlayer().getId() && World.Alliance.disbandAlliance(gs.getAllianceId())) {
                    return true;
                }
            }
        } catch (Exception re) {
        }
        return false;
    }

    public byte getLastMsg() {
        return lastMsg;
    }

    public final void setLastMsg(final byte last) {
        this.lastMsg = last;
    }

    public final void resetStats(int str, int dex, int z, int luk) {
        c.getPlayer().resetStats(str, dex, z, luk);
    }

    public final boolean dropItem(int slot, int invType, int quantity) {
        MapleInventoryType inv = MapleInventoryType.getByType((byte) invType);
        if (inv == null) {
            return false;
        }
        return MapleInventoryManipulator.drop(c, inv, (short) slot, (short) quantity, true);
    }

    public final List<Integer> getAllPotentialInfo() {
        List<Integer> list = new ArrayList<>(MapleItemInformationProvider.getInstance().getAllPotentialInfo().keySet());
        Collections.sort(list);
        return list;
    }

    public final List<Integer> getAllPotentialInfoSearch(String content) {
        List<Integer> list = new ArrayList<>();
        MapleItemInformationProvider.getInstance().getAllPotentialInfo().entrySet().forEach((i) -> {
            i.getValue().stream().filter((ii) -> (ii.toString().contains(content))).forEachOrdered((_item) -> {
                list.add(i.getKey());
            });
        });
        Collections.sort(list);
        return list;
    }

    public final String getPotentialInfo(final int id) {
        final List<StructItemOption> potInfo = MapleItemInformationProvider.getInstance().getPotentialInfo(id);
        final StringBuilder builder = new StringBuilder("#b#ePOTENTIAL INFO FOR ID: ");
        builder.append(id);
        builder.append("#n#k\r\n\r\n");
        int minLevel = 1, maxLevel = 10;
        for (StructItemOption item : potInfo) {
            builder.append("#等級 ");
            builder.append(minLevel);
            builder.append("~");
            builder.append(maxLevel);
            builder.append(": #n");
            builder.append(item.toString());
            minLevel += 10;
            maxLevel += 10;
            builder.append("\r\n");
        }
        return builder.toString();
    }

    public final void sendRPS() {
        c.getSession().write(CField.getRPSMode((byte) 8, -1, -1, -1));
    }

    public final void setQuestRecord(Object ch, final int questid, final String data) {
        ((MapleCharacter) ch).getQuestNAdd(MapleQuest.getInstance(questid)).setCustomData(data);
    }

    public void warpMap(int id) {
        getMap().getCharacters().forEach((chr) -> {
            chr.changeMap(id);
        });
    }

    public void warpMapAutoJQers(int id) {
        World.getAllCharacters().stream().filter((jqers) -> (getEventMapWarp(jqers))).forEachOrdered((jqers) -> {
            // this is so we catch all the characters within the jq maps (even staged)*
            jqers.changeMap(id);
        });
    }

    public final void doWeddingEffect(final Object ch) {
        final MapleCharacter chr = (MapleCharacter) ch;
        final MapleCharacter player = getPlayer();
        getMap().broadcastMessage(CWvsContext.yellowChat(player.getName() + ", do you take " + chr.getName() + " as your wife and promise to stay beside her through all downtimes, crashes, and lags?"));
        WorldTimer.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                if (chr == null || player == null) {
                    warpMap(680000500, 0);
                } else {
                    chr.getMap().broadcastMessage(CWvsContext.yellowChat(chr.getName() + ", do you take " + player.getName() + " as your husband and promise to stay beside him through all downtimes, crashes, and lags?"));
                }
            }
        }, 10000);
        WorldTimer.getInstance().schedule(new Runnable() {

            @Override
            public void run() {
                if (chr == null || player == null) {
                    if (player != null) {
                        setQuestRecord(player, 160001, "3");
                        setQuestRecord(player, 160002, "0");
                    } else if (chr != null) {
                        setQuestRecord(chr, 160001, "3");
                        setQuestRecord(chr, 160002, "0");
                    }
                    warpMap(680000500, 0);
                } else {
                    setQuestRecord(player, 160001, "2");
                    setQuestRecord(chr, 160001, "2");
                    sendNPCText(player.getName() + " and " + chr.getName() + ", I wish you two all the best on your " + chr.getClient().getWorldServer().getWorldName() + " journey together!", 9201002);
                    chr.getMap().startExtendedMapEffect("You may now kiss the bride, " + player.getName() + "!", 5120006);
                    if (chr.getGuildId() > 0) {
                        World.Guild.guildPacket(chr.getGuildId(), CWvsContext.sendMarriage(false, chr.getName()));
                    }
                    if (chr.getFamilyId() > 0) {
                        World.Family.familyPacket(chr.getFamilyId(), CWvsContext.sendMarriage(true, chr.getName()), chr.getId());
                    }
                    if (player.getGuildId() > 0) {
                        World.Guild.guildPacket(player.getGuildId(), CWvsContext.sendMarriage(false, player.getName()));
                    }
                    if (player.getFamilyId() > 0) {
                        World.Family.familyPacket(player.getFamilyId(), CWvsContext.sendMarriage(true, chr.getName()), player.getId());
                    }
                }
            }
        }, 20000); //10 sec 10 sec

    }

    public void putKey(int key, int type, int action) {
        getPlayer().changeKeybinding(key, (byte) type, action);
        getClient().getSession().write(CField.getKeymap(getPlayer().getKeyLayout()));
    }

    public void logDonator(String log, int previous_points) {
        final StringBuilder logg = new StringBuilder();
        logg.append(MapleCharacterUtil.makeMapleReadable(getPlayer().getName()));
        logg.append(" [CID: ").append(getPlayer().getId()).append("] ");
        logg.append(" [Account: ").append(MapleCharacterUtil.makeMapleReadable(getClient().getAccountName())).append("] ");
        logg.append(log);
        logg.append(" [Previous: ").append(previous_points).append("] [Now: ").append(getPlayer().getPoints()).append("]");

        try {
            Connection con = DatabaseConnection.getConnection();
            try (PreparedStatement ps = con.prepareStatement("INSERT INTO donorlog VALUES(DEFAULT, ?, ?, ?, ?, ?, ?, ?, ?)")) {
                ps.setString(1, MapleCharacterUtil.makeMapleReadable(getClient().getAccountName()));
                ps.setInt(2, getClient().getAccID());
                ps.setString(3, MapleCharacterUtil.makeMapleReadable(getPlayer().getName()));
                ps.setInt(4, getPlayer().getId());
                ps.setString(5, log);
                ps.setString(6, FileoutputUtil.CurrentReadable_Time());
                ps.setInt(7, previous_points);
                ps.setInt(8, getPlayer().getPoints());
                ps.executeUpdate();
            }
        } catch (SQLException e) {
        }
        FileoutputUtil.log(FileoutputUtil.Donator_Log, logg.toString());
    }

    public void doRing(final String name, final int itemid) {
        PlayersHandler.DoRing(getClient(), name, itemid);
    }

    public int getNaturalStats(final int itemid, final String it) {
        Map<String, Integer> eqStats = MapleItemInformationProvider.getInstance().getEquipStats(itemid);
        if (eqStats != null && eqStats.containsKey(it)) {
            return eqStats.get(it);
        }
        return 0;
    }

    public boolean isEligibleName(String t) {
        return MapleCharacterUtil.canCreateChar(t, getPlayer().isGM()) && (!LoginInformationProvider.getInstance().isForbiddenName(t) || getPlayer().isGM());
    }

    public String checkDrop(int mobId) {
        final List<MonsterDropEntry> ranks = MapleMonsterInformationProvider.getInstance().retrieveDrop(mobId);
        if (ranks != null && ranks.size() > 0) {
            int num = 0, itemId, ch;
            MonsterDropEntry de;
            StringBuilder name = new StringBuilder();
            for (int i = 0; i < ranks.size(); i++) {
                de = ranks.get(i);
                if (de.chance > 0 && (de.questid <= 0 || (de.questid > 0 && MapleQuest.getInstance(de.questid).getName().length() > 0))) {
                    itemId = de.itemId;
                    if (num == 0) {
                        name.append("Drops for #o").append(mobId).append("#\r\n");
                        name.append("--------------------------------------\r\n");
                    }
                    String namez = "#z" + itemId + "#";
                    if (itemId == 0) { //meso
                        itemId = 4031041; //display sack of cash
                        namez = (de.Minimum * getClient().getWorldServer().getMesoRate()) + " to " + (de.Maximum * getClient().getWorldServer().getMesoRate()) + " meso";
                    }
                    ch = de.chance * getClient().getWorldServer().getDropRate();
                    name.append(num + 1).append(") #v").append(itemId).append("#").append(namez).append(" - ").append(Integer.valueOf(ch >= 999999 ? 1000000 : ch).doubleValue() / 10000.0).append("% chance. ").append(de.questid > 0 && MapleQuest.getInstance(de.questid).getName().length() > 0 ? ("Requires quest " + MapleQuest.getInstance(de.questid).getName() + " to be started.") : "").append("\r\n");
                    num++;
                }
            }
            if (name.length() > 0) {
                return name.toString();
            }

        }
        return "No drops was returned.";
    }

    public void handleDivorce() {
        if (getPlayer().getMarriageId() <= 0) {
            sendNext("Please make sure you have a marriage.");
            return;
        }
        final int chz = World.Find.findChannel(getPlayer().getMarriageId());
        final int wlz = World.Find.findWorld(getPlayer().getMarriageId());
        if (chz == -1) {
            //sql queries
            try {
                Connection con = DatabaseConnection.getConnection();
                PreparedStatement ps = con.prepareStatement("UPDATE queststatus SET customData = ? WHERE characterid = ? AND (quest = ? OR quest = ?)");
                ps.setString(1, "0");
                ps.setInt(2, getPlayer().getMarriageId());
                ps.setInt(3, 160001);
                ps.setInt(4, 160002);
                ps.executeUpdate();
                ps.close();

                ps = con.prepareStatement("UPDATE characters SET marriageid = ? WHERE id = ?");
                ps.setInt(1, 0);
                ps.setInt(2, getPlayer().getMarriageId());
                ps.executeUpdate();
                ps.close();
            } catch (SQLException e) {
                outputFileError(e);
                return;
            }
            setQuestRecord(getPlayer(), 160001, "0");
            setQuestRecord(getPlayer(), 160002, "0");
            getPlayer().setMarriageId(0);
            sendNext("You have been successfully divorced...");
            return;
        } else if (chz < -1) {
            sendNext("Please make sure your partner is logged on.");
            return;
        }
        MapleCharacter cPlayer = ChannelServer.getInstance(wlz, chz).getPlayerStorage().getCharacterById(getPlayer().getMarriageId());
        if (cPlayer != null) {
            cPlayer.dropMessage(1, "Your partner has divorced you.");
            cPlayer.setMarriageId(0);
            setQuestRecord(cPlayer, 160001, "0");
            setQuestRecord(getPlayer(), 160001, "0");
            setQuestRecord(cPlayer, 160002, "0");
            setQuestRecord(getPlayer(), 160002, "0");
            getPlayer().setMarriageId(0);
            sendNext("You have been successfully divorced...");
        } else {
            sendNext("An error occurred...");
        }
    }

    public String getReadableMillis(long startMillis, long endMillis) {
        return StringUtil.getReadableMillis(startMillis, endMillis);
    }

    public void sendUltimateExplorer() {
        getClient().getSession().write(CWvsContext.ultimateExplorer());
    }

    public void sendPendant(boolean b) {
        c.getSession().write(CWvsContext.pendantSlot(b));
    }

    public void addPendantSlot(int days) {
        c.getPlayer().getQuestNAdd(MapleQuest.getInstance(GameConstants.PENDANT_SLOT)).setCustomData(String.valueOf(System.currentTimeMillis() + ((long) days * 24 * 60 * 60 * 1000)));
    }

    public Triple<Integer, Integer, Integer> getCompensation() {
        Triple<Integer, Integer, Integer> ret = null;
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT * FROM compensationlog_confirmed WHERE chrname LIKE ?")) {
                ps.setString(1, getPlayer().getName());
                try (ResultSet rs = ps.executeQuery()) {
                    if (rs.next()) {
                        ret = new Triple<>(rs.getInt("value"), rs.getInt("taken"), rs.getInt("donor"));
                    }
                }
            }
            return ret;
        } catch (SQLException e) {
            FileoutputUtil.outputFileError(FileoutputUtil.ScriptEx_Log, e);
            return ret;
        }
    }

    public boolean deleteCompensation(int taken) {
        try {
            try (PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE compensationlog_confirmed SET taken = ? WHERE chrname LIKE ?")) {
                ps.setInt(1, taken);
                ps.setString(2, getPlayer().getName());
                ps.executeUpdate();
            }
            return true;
        } catch (SQLException e) {
            FileoutputUtil.outputFileError(FileoutputUtil.ScriptEx_Log, e);
            return false;
        }
    }

    // public MapleClan getClan() {
    //    return clan;
    // }
    public String getClanRanks() throws SQLException {
        StringBuilder ret = new StringBuilder();
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT name, level, members, wins FROM clans ORDER BY wins desc LIMIT 5");
        ResultSet rs = ps.executeQuery();
        try {
            int calc = 1;
            while (rs.next()) {
                int wins_Integer = rs.getInt("wins");
                String wins = NumberFormat.getNumberInstance(Locale.US).format(wins_Integer);
                ret.append("\r\n#e").append(calc).append("#n. #r").append(rs.getString("name")).append("#k\r\nClan Level : ").append(rs.getInt("level")).append(" | Clan Members : ").append(rs.getInt("members")).append(" | Wins : ").append(wins);
                calc++;
            }
        } catch (SQLException ex) {
            Logger.getLogger(NPCConversationManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        ps.close();
        rs.close();
        return ret.toString();
    }

    public String getClanRoster() throws SQLException {
        StringBuilder ret = new StringBuilder();
        PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("SELECT name FROM characters WHERE clanid = ? ORDER BY name desc LIMIT 5");
        ps.setInt(1, getClanId());
        ResultSet rs = ps.executeQuery();
        try {
            int calc = 1;
            while (rs.next()) {
                ret.append("\r\n#e").append(calc).append("#n. #b").append(rs.getString("name"));
                calc++;
            }
        } catch (SQLException ex) {
            Logger.getLogger(NPCConversationManager.class.getName()).log(Level.SEVERE, null, ex);
        }
        ps.close();
        rs.close();
        return ret.toString();
    }

    public String getClanMessage() {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps;
        try {
            ps = con.prepareStatement("SELECT message FROM clans WHERE id = ?");
            ps.setInt(1, getClanId());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return "";
            }
            String name = rs.getString("message");
            rs.close();
            ps.close();
            return name;
        } catch (SQLException e) {
            System.out.print("ERROR" + e);
        }
        return "";
    }

    public String getClanName() {
        return getClanName(getClanId());
    }

    public String getClanNameNonStatic(int id) {
        return getClanName(id);
    }

    public static String getClanName(int id) {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps;
        try {
            ps = con.prepareStatement("SELECT name FROM clans WHERE id = ?");
            ps.setInt(1, id);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return "Clan";
            }
            String name = rs.getString("name");
            rs.close();
            ps.close();
            return name;
        } catch (SQLException e) {
            System.out.print("ERROR" + e);
        }
        return "Clan";
    }

    public static int getClanIdByName(String name) {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps;
        try {
            ps = con.prepareStatement("SELECT id FROM clans WHERE name = ?");
            ps.setString(1, name);
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            int id = rs.getInt("id");
            rs.close();
            ps.close();
            return id;
        } catch (SQLException e) {
            System.out.print("ERROR" + e);
        }
        return -1;
    }

    public String getClantag() {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps;
        try {
            ps = con.prepareStatement("SELECT clantag FROM clans WHERE id = ?");
            ps.setInt(1, getClanId());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return "none";
            }
            String name = rs.getString("clantag");
            rs.close();
            ps.close();
            return name;
        } catch (SQLException e) {
            System.out.print("ERROR" + e);
        }
        return "none";
    }

    public int getClanId() {
        return getClanId(null);
    }

    public int getClanId(MapleCharacter chr) {
        return chr == null ? getPlayer().getClanId() : chr.getClanId();
    }

    public void setClanMessage(String message) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE clans SET message = ? WHERE id = ?");
            ps.setString(1, message);
            ps.setInt(2, getClanId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            Logger.getLogger(NPCConversationManager.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void setClanTag(String tag) {
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE clans SET clantag = ? WHERE id = ?");
            ps.setString(1, tag);
            ps.setInt(2, getClanId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            Logger.getLogger(NPCConversationManager.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void levelUpClan() { // for npc use only
        try {
            PreparedStatement ps = DatabaseConnection.getConnection().prepareStatement("UPDATE clans SET level = level+1 WHERE id = ?");
            ps.setInt(1, getClanId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            Logger.getLogger(NPCConversationManager.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public void createClan(String name) {
        try {
            java.sql.Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("INSERT INTO clans ( name, level, members, wins, message, clantag, leaderid ) VALUES ( ?, ?, ?, ?, ?, ?, ? )");
            ps.setString(1, name);
            ps.setInt(2, 1);
            ps.setInt(3, 1);
            ps.setInt(4, 0);
            ps.setString(5, "");
            ps.setString(6, "");
            ps.setInt(7, getPlayer().getId());
            ps.executeUpdate();
            ps.close();
        } catch (SQLException e) {
            Logger.getLogger(NPCConversationManager.class.getName()).log(Level.SEVERE, null, e);
        }
    }

    public int getClanLeader() {
        Connection con = DatabaseConnection.getConnection();
        PreparedStatement ps;
        try {
            ps = con.prepareStatement("SELECT leaderid FROM clans WHERE id = ?");
            ps.setInt(1, getClanId());
            ResultSet rs = ps.executeQuery();
            if (!rs.next()) {
                rs.close();
                ps.close();
                return -1;
            }
            int leaderid = rs.getInt("leaderid");
            rs.close();
            ps.close();
            return leaderid;
        } catch (SQLException e) {
            System.out.print("ERROR" + e);
        }
        return -1;
    }

    public String getClanRequest(MapleCharacter chr) {
        String information = "#e";
        information += chr.getName() + "#n, the leader of #e" + getClanName(chr.getClanId()) + "#n, has sent you a #bClan Invitation#k.\r\n";
        information += "If you choose to accept, you will become a(n) #e" + getClanName(chr.getClanId()) + "#n member from here on out.";
        return information;
    }

    public String getClanKickMenu() { // fuk im bad at naming xD
        String sendText = "#e";
        sendText += getClanName() + "#n's Memberlist :\r\nWho do you wish to kick from #e" + getClanName() + "#n?";
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("SELECT `name`, `id`, `clanid` FROM `characters` WHERE `clanid` = ?");
            ps.setInt(1, getPlayer().getClanId());
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                int charid = rs.getInt("id");
                String name = rs.getString("name");
                if (!name.equalsIgnoreCase(getPlayer().getName()) || charid != getPlayer().getId()) {
                    sendText += "#b\r\n#L" + charid + "#" + name + "#k (" + (World.isConnected(name) || World.isCSConnected(charid) ? "#gOnline#k" : "#rOffline#k") + ")";
                }
            }
        } catch (SQLException e) {
            System.out.println("Unable to load kick menu due to " + e.getMessage());
        }
        return sendText;
    }

    public void kickPlayerFromClan(int id) {
        try {
            Connection con = DatabaseConnection.getConnection();
            PreparedStatement ps = con.prepareStatement("UPDATE `characters` SET `clanid` = 0 WHERE id = ?");
            ps.setInt(1, id);
            ps.executeUpdate();
            ps = con.prepareStatement("UPDATE `clans` SET `members` = `members` - 1 WHERE `name` = ?");
            ps.setString(1, getClanName());
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println("Unable to kick player from the clan. " + e.getMessage());
        }
        if (World.Find.findChannel(id) > 0) {
            int channel = World.Find.findChannel(id);
            int world = World.Find.findWorld(id);
            MapleCharacter chr = ChannelServer.getInstance(world, channel).getPlayerStorage().getCharacterById(id);
            chr.setClanId(0);
            chr.savePlayer();
            World.getAllCharacters().stream().filter((members) -> ((members.getClanId() == getPlayer().getClanId()) && members != getPlayer())).forEachOrdered((members) -> {
                members.dropMessage(5, "[" + getClanName() + "] " + chr.getName() + " has been kicked from the clan.");
            });
            getPlayer().dropMessage(5, "[" + getClanName() + "] " + chr.getName() + " has been kicked from the clan.");
        }
    }

    public void gainAPS(int gain) {
        getPlayer().gainAPS(gain);
    }

    public void hideNpc(int npcid) {
        c.getPlayer().getMap().getAllNPCsThreadsafe().stream().map((npcs) -> (MapleNPC) npcs).filter((npc) -> (npc.getId() == npcid)).map((npc) -> {
            c.getSession().write(NPCPacket.removeNPCController(npc.getObjectId()));
            return npc;
        }).forEachOrdered((npc) -> {
            c.getSession().write(NPCPacket.removeNPC(npc.getObjectId()));
        });
    }

    public void showHilla() {
        try {
            c.getSession().write(CField.MapEff("phantom/hillah"));
            MapleNPC hilla = new MapleNPC(1402400, "Hilla");
            hilla.setPosition(new Point(-131, -2));
            hilla.setCy(-7);
            hilla.setF(1);
            hilla.setFh(12);
            hilla.setRx0(-181);
            hilla.setRx1(-81);
            MapleNPC guard1 = new MapleNPC(1402401, "Hilla's Guard");
            guard1.setPosition(new Point(-209, -2));
            guard1.setCy(-7);
            guard1.setF(1);
            guard1.setFh(12);
            guard1.setRx0(-259);
            guard1.setRx1(-159);
            MapleNPC guard2 = new MapleNPC(1402401, "Hilla's Guard");
            guard2.setPosition(new Point(-282, -2));
            guard2.setCy(-7);
            guard2.setF(1);
            guard2.setFh(12);
            guard2.setRx0(-332);
            guard2.setRx1(-232);
            MapleNPC guard3 = new MapleNPC(1402401, "Hilla's Guard");
            guard3.setPosition(new Point(-59, -2));
            guard3.setCy(-7);
            guard3.setF(1);
            guard3.setFh(12);
            guard3.setRx0(-109);
            guard3.setRx1(-9);
            c.getSession().write(NPCPacket.spawnNPC(hilla, true));
            c.getSession().write(NPCPacket.spawnNPC(guard1, true));
            c.getSession().write(NPCPacket.spawnNPC(guard2, true));
            c.getSession().write(NPCPacket.spawnNPC(guard3, true));
            Thread.sleep(6000);
        } catch (InterruptedException e) {
        }
        NPCScriptManager.getInstance().start(c.getPlayer().getClient(), 1104201, "PTtutor500_2");
    }

    public void showSkaia() {
        try {
            c.getSession().write(CField.MapEff("phantom/skaia"));
            Thread.sleep(8000);
        } catch (InterruptedException e) {
        }
        NPCScriptManager.getInstance().start(c.getPlayer().getClient(), 1104201, "PTtutor500_3");
    }

    public void showPhantomWait() {
        try {
            c.getSession().write(CField.MapEff("phantom/phantom"));
            Thread.sleep(2000);
        } catch (InterruptedException e) {
        }
        NPCScriptManager.getInstance().start(c.getPlayer().getClient(), 1104201, "PTtutor500_4");
    }

    public void movePhantom() {
        try {
            c.getSession().write(CField.UIPacket.getDirectionInfoTest((byte) 3, 2));
            c.getSession().write(CField.UIPacket.getDirectionInfoTest((byte) 1, 2000));
            Thread.sleep(2000);
            c.getSession().write(CField.UIPacket.getDirectionInfoTest((byte) 3, 0));
        } catch (InterruptedException e) {
        }
        NPCScriptManager.getInstance().start(c.getPlayer().getClient(), 1104201, "PTtutor500_1");
    }

    public void showPhantomMovie() {
        warp(150000000);
        try {
            c.getSession().write(UIPacket.playMovie("phantom.avi", true));
            Thread.sleep(4 * 60 * 1000); //4 minutes
        } catch (InterruptedException e) {
        }
        MapleQuest.getInstance(25000).forceComplete(c.getPlayer(), 1402000);
        c.getSession().write(CField.UIPacket.getDirectionStatus(false));
        c.getSession().write(CField.UIPacket.IntroEnableUI(0));
    }

    public void getAdviceTalk(String wzinfo) {
        c.getSession().write(NPCPacket.getAdviceTalk(wzinfo));
    }
}
