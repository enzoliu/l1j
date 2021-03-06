/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */

package l1j.server.server.model.Instance;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.concurrent.ScheduledFuture;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.sql.Timestamp;

import l1j.server.Config;
import l1j.server.server.ActionCodes;
import l1j.server.server.ClientThread;
import l1j.server.server.GeneralThreadPool;
import l1j.server.server.PacketOutput;
import l1j.server.server.WarTimeController;
import l1j.server.server.command.executor.L1HpBar;
import l1j.server.server.datatables.CharacterTable;
import l1j.server.server.datatables.ExpTable;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.model.HpRegeneration;
import l1j.server.server.model.AcceleratorChecker;
import l1j.server.server.model.L1Attack;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1ChatParty;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1DwarfForElfInventory;
import l1j.server.server.model.L1DwarfInventory;
import l1j.server.server.model.L1EquipmentSlot;
import l1j.server.server.model.L1ExcludingList;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.L1Karma;
import l1j.server.server.model.L1Magic;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1Party;
import l1j.server.server.model.L1PcInventory;
import l1j.server.server.model.L1PinkName;
import l1j.server.server.model.L1Quest;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1TownLocation;
import l1j.server.server.model.L1War;
import l1j.server.server.model.L1World;
import l1j.server.server.model.MpReductionByAwake;
import l1j.server.server.model.MpRegeneration;
import l1j.server.server.model.MpRegenerationByDoll;
import l1j.server.server.model.classes.L1ClassFeature;
import l1j.server.server.model.gametime.L1GameTimeCarrier;
import l1j.server.server.model.monitor.L1PcAutoUpdate;
import l1j.server.server.model.monitor.L1PcExpMonitor;
import l1j.server.server.model.monitor.L1PcGhostMonitor;
import l1j.server.server.model.monitor.L1PcHellMonitor;
import l1j.server.server.model.monitor.L1PcInvisDelay;
import l1j.server.server.model.skill.L1SkillUse;
import l1j.server.server.serverpackets.S_BlueMessage;
import l1j.server.server.serverpackets.S_Exp;
import l1j.server.server.serverpackets.S_bonusstats;
import l1j.server.server.serverpackets.S_CastleMaster;
import l1j.server.server.serverpackets.S_ChangeShape;
import l1j.server.server.serverpackets.S_Disconnect;
import l1j.server.server.serverpackets.S_RemoveObject;
import l1j.server.server.serverpackets.S_DoActionGFX;
import l1j.server.server.serverpackets.S_DoActionShop;
import l1j.server.server.serverpackets.S_HPMeter;
import l1j.server.server.serverpackets.S_HPUpdate;
import l1j.server.server.serverpackets.S_Invis;
import l1j.server.server.serverpackets.S_Lawful;
import l1j.server.server.serverpackets.S_Liquor;
import l1j.server.server.serverpackets.S_MPUpdate;
import l1j.server.server.serverpackets.S_OtherCharPacks;
import l1j.server.server.serverpackets.S_OwnCharStatus;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_Poison;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillIconGFX;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.serverpackets.ServerBasePacket;
import l1j.server.server.templates.L1BookMark;
import l1j.server.server.templates.L1Item;
import l1j.server.server.templates.L1PrivateShopBuyList;
import l1j.server.server.templates.L1PrivateShopSellList;
import l1j.server.server.utils.CalcStat;
import static l1j.server.server.model.skill.L1SkillId.*;

import l1j.server.server.model.HpRegenerationByDoll;//????????????????????????

// Referenced classes of package l1j.server.server.model:
// L1Character, L1DropTable, L1Object, L1ItemInstance,
// L1World
//

public class L1PcInstance extends L1Character {
	private static final long serialVersionUID = 1L;

	public static final int CLASSID_KNIGHT_MALE = 61;
	public static final int CLASSID_KNIGHT_FEMALE = 48;
	public static final int CLASSID_ELF_MALE = 138;
	public static final int CLASSID_ELF_FEMALE = 37;
	public static final int CLASSID_WIZARD_MALE = 734;
	public static final int CLASSID_WIZARD_FEMALE = 1186;
	public static final int CLASSID_DARK_ELF_MALE = 2786;
	public static final int CLASSID_DARK_ELF_FEMALE = 2796;
	public static final int CLASSID_PRINCE = 0;
	public static final int CLASSID_PRINCESS = 1;
	public static final int CLASSID_DRAGON_KNIGHT_MALE = 6658;
	public static final int CLASSID_DRAGON_KNIGHT_FEMALE = 6661;
	public static final int CLASSID_ILLUSIONIST_MALE = 6671;
	public static final int CLASSID_ILLUSIONIST_FEMALE = 6650;

	private static Random _random = new Random();

	//add by sdcom 2010.3.9
	private boolean lock = false;
	public void lockWarehouse(){
		lock = true;
	}
	public void unlockWarehouse(){
		lock = false;
	}
	public boolean isWarehouseLock(){
		return lock;
	}
	//add by sdcom 2010.3.5
	private String actionName = "";
	public void setMakeItemAction(String htmlId){
		actionName = htmlId;
	}
	public String getMakeItemAction(){
		return actionName;
	}
	public void clearMakeItemAction(){
		actionName = "";
	}
	//~sdcom
	
	
	private short _hpr = 0;
	private short _trueHpr = 0;

	public short getHpr() {
		return _hpr;
	}

	public void addHpr(int i) {
		_trueHpr += i;
		_hpr = (short) Math.max(0, _trueHpr);
	}

	private short _mpr = 0;
	private short _trueMpr = 0;

	public short getMpr() {
		return _mpr;
	}

	public void addMpr(int i) {
		_trueMpr += i;
		_mpr = (short) Math.max(0, _trueMpr);
	}

	public short _originalHpr = 0; // ??? ???????????????CON HPR

	public short getOriginalHpr() {

		return _originalHpr;
	}

	public short _originalMpr = 0; // ??? ???????????????WIS MPR

	public short getOriginalMpr() {

		return _originalMpr;
	}

	public void startHpRegeneration() {
		final int INTERVAL = 1000;

		if (!_hpRegenActive) {
			_hpRegen = new HpRegeneration(this);
			_regenTimer.scheduleAtFixedRate(_hpRegen, INTERVAL, INTERVAL);
			_hpRegenActive = true;
		}
	}

	public void stopHpRegeneration() {
		if (_hpRegenActive) {
			_hpRegen.cancel();
			_hpRegen = null;
			_hpRegenActive = false;
		}
	}

	public void startMpRegeneration() {
		final int INTERVAL = 1000;

		if (!_mpRegenActive) {
			_mpRegen = new MpRegeneration(this);
			_regenTimer.scheduleAtFixedRate(_mpRegen, INTERVAL, INTERVAL);
			_mpRegenActive = true;
		}
	}

	//add ????????????????????????
	public void startHpRegenerationByDoll() {
	    final int INTERVAL_BY_DOLL = 60000;
	    boolean isExistHprDoll = false;
	    Object[] dollList = getDollList().values().toArray();
	    for (Object dollObject : dollList) {
	        L1DollInstance doll = (L1DollInstance) dollObject;
	        if (doll.isHpRegeneration()) {
	            isExistHprDoll = true;
	        }
	    }
	    if (!_hpRegenActiveByDoll && isExistHprDoll) {
	        _hpRegenByDoll = new HpRegenerationByDoll(this);
	        _regenTimer.scheduleAtFixedRate(_hpRegenByDoll, INTERVAL_BY_DOLL, INTERVAL_BY_DOLL);
	        _hpRegenActiveByDoll = true;
	    }
	}
	//end
	
	public void startMpRegenerationByDoll() {
		final int INTERVAL_BY_DOLL = 60000;
		boolean isExistMprDoll = false;
		Object[] dollList = getDollList().values().toArray();
		for (Object dollObject : dollList) {
			L1DollInstance doll = (L1DollInstance) dollObject;
			if (doll.isMpRegeneration()) {
				isExistMprDoll = true;
			}
		}
		if (!_mpRegenActiveByDoll && isExistMprDoll) {
			_mpRegenByDoll = new MpRegenerationByDoll(this);
			_regenTimer.scheduleAtFixedRate(_mpRegenByDoll, INTERVAL_BY_DOLL,
					INTERVAL_BY_DOLL);
			_mpRegenActiveByDoll = true;
		}
	}

	public void startMpReductionByAwake() {
		final int INTERVAL_BY_AWAKE = 4000;
		if (!_mpReductionActiveByAwake) {
			_mpReductionByAwake = new MpReductionByAwake(this);
			_regenTimer.scheduleAtFixedRate(_mpReductionByAwake,
					INTERVAL_BY_AWAKE, INTERVAL_BY_AWAKE);
			_mpReductionActiveByAwake = true;
		}
	}

	public void stopMpRegeneration() {
		if (_mpRegenActive) {
			_mpRegen.cancel();
			_mpRegen = null;
			_mpRegenActive = false;
		}
	}

	//add ????????????????????????
	public void stopHpRegenerationByDoll() {
	    if (_hpRegenActiveByDoll) {
	        _hpRegenByDoll.cancel();
	        _hpRegenByDoll = null;
	        _hpRegenActiveByDoll = false;
	    }
	}
	//end
	
	public void stopMpRegenerationByDoll() {
		if (_mpRegenActiveByDoll) {
			_mpRegenByDoll.cancel();
			_mpRegenByDoll = null;
			_mpRegenActiveByDoll = false;
		}
	}

	public void stopMpReductionByAwake() {
		if (_mpReductionActiveByAwake) {
			_mpReductionByAwake.cancel();
			_mpReductionByAwake = null;
			_mpReductionActiveByAwake = false;
		}
	}

	public void startObjectAutoUpdate() {
		removeAllKnownObjects();
		_autoUpdateFuture = GeneralThreadPool.getInstance()
				.pcScheduleAtFixedRate(new L1PcAutoUpdate(getId()), 0L,
						INTERVAL_AUTO_UPDATE);
	}

	/**
	 * ?????????????????????????????????????????????
	 */
	public void stopEtcMonitor() {
		if (_autoUpdateFuture != null) {
			_autoUpdateFuture.cancel(true);
			_autoUpdateFuture = null;
		}
		if (_expMonitorFuture != null) {
			_expMonitorFuture.cancel(true);
			_expMonitorFuture = null;
		}
		if (_ghostFuture != null) {
			_ghostFuture.cancel(true);
			_ghostFuture = null;
		}

		if (_hellFuture != null) {
			_hellFuture.cancel(true);
			_hellFuture = null;
		}

	}

	private static final long INTERVAL_AUTO_UPDATE = 300;
	private ScheduledFuture<?> _autoUpdateFuture;

	private static final long INTERVAL_EXP_MONITOR = 500;
	private ScheduledFuture<?> _expMonitorFuture;

	public void onChangeExp() {
		int level = ExpTable.getLevelByExp(getExp());
		int char_level = getLevel();
		int gap = level - char_level;
		if (gap == 0) {
			// sendPackets(new S_OwnCharStatus(this));
			sendPackets(new S_Exp(this));
			return;
		}

		// ??????????????????????????????
		if (gap > 0) {
			levelUp(gap);
		} else if (gap < 0) {
			levelDown(gap);
		}
	}

	@Override
	public void onPerceive(L1PcInstance perceivedFrom) {
		if (isGmInvis() || isGhost() || isInvisble()) {
			return;
		}

		perceivedFrom.addKnownObject(this);
		perceivedFrom.sendPackets(new S_OtherCharPacks(this)); // ????????????????????????
		if (isInParty() && getParty().isMember(perceivedFrom)) { // PT??????????????????HP?????????????????????
			perceivedFrom.sendPackets(new S_HPMeter(this));
		}

		if (isPrivateShop()) {
			perceivedFrom.sendPackets(new S_DoActionShop(getId(),
					ActionCodes.ACTION_Shop, getShopChat()));
		}

		if (isCrown()) { // ??????
			L1Clan clan = L1World.getInstance().getClan(getClanname());
			if (clan != null) {
				if (getId() == clan.getLeaderId() // ???????????????????????????
						&& clan.getCastleId() != 0) {
					perceivedFrom.sendPackets(new S_CastleMaster(clan
							.getCastleId(), getId()));
				}
			}
		}
	}

	// ????????????????????????????????????????????????????????????
	private void removeOutOfRangeObjects() {
		for (L1Object known : getKnownObjects()) {
			if (known == null) {
				continue;
			}

			if (Config.PC_RECOGNIZE_RANGE == -1) {
				if (!getLocation().isInScreen(known.getLocation())) { // ?????????
					removeKnownObject(known);
					sendPackets(new S_RemoveObject(known));
				}
			} else {
				if (getLocation().getTileLineDistance(known.getLocation()) > Config.PC_RECOGNIZE_RANGE) {
					removeKnownObject(known);
					sendPackets(new S_RemoveObject(known));
				}
			}
		}
	}

	// ??????????????????????????????
	public void updateObject() {
		removeOutOfRangeObjects();

		// ??????????????????????????????????????????????????????
		for (L1Object visible : L1World.getInstance().getVisibleObjects(this,
				Config.PC_RECOGNIZE_RANGE)) {
			if (!knownsObject(visible)) {
				visible.onPerceive(this);
			} else {
				if (visible instanceof L1NpcInstance) {
					L1NpcInstance npc = (L1NpcInstance) visible;
					if (getLocation().isInScreen(npc.getLocation())
							&& npc.getHiddenStatus() != 0) {
						npc.approachPlayer(this);
					}
				}
			}
			//?????????????????????????????????
			if (hasSkillEffect(GMSTATUS_HPBAR)
					&& L1HpBar.isHpBarTarget(visible)) {
				sendPackets(new S_HPMeter((L1Character) visible));
			} else if(Config.SHOW_HP_BAR && L1HpBar.isHpBarTarget(visible)
					&& visible instanceof L1MonsterInstance){
				sendPackets(new S_HPMeter((L1Character) visible));
			}
			//~?????????????????????????????????
		}
	}

	private void sendVisualEffect() {
		int poisonId = 0;
		if (getPoison() != null) { // ?????????
			poisonId = getPoison().getEffectId();
		}
		if (getParalysis() != null) { // ????????????
			// ??????????????????????????????????????????????????????poisonId???????????????
			poisonId = getParalysis().getEffectId();
		}
		if (poisonId != 0) { // ??????if?????????????????????????????????
			sendPackets(new S_Poison(getId(), poisonId));
			broadcastPacket(new S_Poison(getId(), poisonId));
		}
	}

	public void sendVisualEffectAtLogin() {
		//for (L1Clan clan : L1World.getInstance().getAllClans()) {
		//	sendPackets(new S_Emblem(clan.getClanId()));
		//}

		if (getClanid() != 0) { // ???????????????
			L1Clan clan = L1World.getInstance().getClan(getClanname());
			if (clan != null) {
				if (isCrown() && getId() == clan.getLeaderId() && // ?????????????????????????????????????????????????????????????????????????????????
						clan.getCastleId() != 0) {
					sendPackets(new S_CastleMaster(clan.getCastleId(), getId()));
				}
			}
		}

		sendVisualEffect();
	}

	public void sendVisualEffectAtTeleport() {
		if (isDrink()) { // liquor??????????????????
			sendPackets(new S_Liquor(getId()));
		}

		sendVisualEffect();
	}

	public L1PcInstance() {
		_accessLevel = 0;
		_currentWeapon = 0;
		_inventory = new L1PcInventory(this);
		_dwarf = new L1DwarfInventory(this);
		_dwarfForElf = new L1DwarfForElfInventory(this);
		_tradewindow = new L1Inventory();
		_bookmarks = new ArrayList<L1BookMark>();
		_quest = new L1Quest(this);
		_equipSlot = new L1EquipmentSlot(this); // ????????????????????????this??????????????????????????????????????????????????????
	}

	@Override
	public void setCurrentHp(int i) {
		if (getCurrentHp() == i) {
			return;
		}
		int currentHp = i;
		if (currentHp >= getMaxHp()) {
			currentHp = getMaxHp();
		}
		setCurrentHpDirect(currentHp);
		sendPackets(new S_HPUpdate(currentHp, getMaxHp()));
		if (isInParty()) { // ??????????????????
			getParty().updateMiniHP(this);
		}
	}
	
	// add by sdcom. try to enhance performance.
	public void addHp(int i) {
		if (i <= 0) {
			return;
		}
		
		int hp = getCurrentHp() + i;
		int maxHp = getMaxHp();
		
		if (hp >= maxHp) {
			hp = maxHp;
		}
		setCurrentHpDirect(hp);
		sendPackets(new S_HPUpdate(hp, getMaxHp()));
		if (isInParty()) { // ??????????????????
			getParty().updateMiniHP(this);
		}
	}

	@Override
	public void setCurrentMp(int i) {
		if (getCurrentMp() == i) {
			return;
		}
		int currentMp = i;
		if (currentMp >= getMaxMp() || isGm()) {
			currentMp = getMaxMp();
		}
		setCurrentMpDirect(currentMp);
		sendPackets(new S_MPUpdate(currentMp, getMaxMp()));
	}

	@Override
	public L1PcInventory getInventory() {
		return _inventory;
	}

	public L1DwarfInventory getDwarfInventory() {
		return _dwarf;
	}

	public L1DwarfForElfInventory getDwarfForElfInventory() {
		return _dwarfForElf;
	}

	public L1Inventory getTradeWindowInventory() {
		return _tradewindow;
	}

	public boolean isGmInvis() {
		return _gmInvis;
	}

	public void setGmInvis(boolean flag) {
		_gmInvis = flag;
	}

	public int getCurrentWeapon() {
		return _currentWeapon;
	}

	public void setCurrentWeapon(int i) {
		_currentWeapon = i;
	}

	public int getType() {
		return _type;
	}

	public void setType(int i) {
		_type = i;
	}

	public short getAccessLevel() {
		return _accessLevel;
	}

	public void setAccessLevel(short i) {
		_accessLevel = i;
	}

	public int getClassId() {
		return _classId;
	}

	public void setClassId(int i) {
		_classId = i;
		_classFeature = L1ClassFeature.newClassFeature(i);
	}

	private L1ClassFeature _classFeature = null;

	public L1ClassFeature getClassFeature() {
		return _classFeature;
	}

	@Override
	public synchronized int getExp() {
		return _exp;
	}

	@Override
	public synchronized void setExp(int i) {
		_exp = i;
	}

	private int _PKcount; // ??? PK????????????

	public int get_PKcount() {
		return _PKcount;
	}

	public void set_PKcount(int i) {
		_PKcount = i;
	}

	private int _clanid; // ??? ???????????????

	public int getClanid() {
		return _clanid;
	}

	public void setClanid(int i) {
		_clanid = i;
	}

	private String clanname; // ??? ????????????

	public String getClanname() {
		return clanname;
	}

	public void setClanname(String s) {
		clanname = s;
	}

	// ???????????????????????????????????????????????????????????????
	public L1Clan getClan() {
		return L1World.getInstance().getClan(getClanname());
	}

	private int _clanRank; // ??? ????????????????????????(??????????????????????????????????????????????????????)

	public int getClanRank() {
		return _clanRank;
	}

	public void setClanRank(int i) {
		_clanRank = i;
	}

	private byte _sex; // ??? ??????

	public byte get_sex() {
		return _sex;
	}

	public void set_sex(int i) {
		_sex = (byte) i;
	}

	public boolean isGm() {
		return _gm;
	}

	public void setGm(boolean flag) {
		_gm = flag;
	}

	public boolean isMonitor() {
		return _monitor;
	}

	public void setMonitor(boolean flag) {
		_monitor = flag;
	}

	private L1PcInstance getStat() {
		return null;
	}

	public void reduceCurrentHp(double d, L1Character l1character) {
		getStat().reduceCurrentHp(d, l1character);
	}

	/**
	 * ??????????????????????????????????????????????????????????????????????????????
	 * 
	 * @param playersList
	 *            ????????????????????????????????????
	 */
	private void notifyPlayersLogout(List<L1PcInstance> playersArray) {
		for (L1PcInstance player : playersArray) {
			if (player.knownsObject(this)) {
				player.removeKnownObject(this);
				player.sendPackets(new S_RemoveObject(this));
			}
		}
	}

	public void logout() {
		L1World world = L1World.getInstance();
		if (getClanid() != 0) // ???????????????
		{
			L1Clan clan = world.getClan(getClanname());
			if (clan != null) {
				if (clan.getWarehouseUsingChar() == getId()) // ???????????????????????????????????????
				{
					clan.setWarehouseUsingChar(0); // ????????????????????????????????????
				}
			}
		}
		notifyPlayersLogout(getKnownPlayers());
		world.removeVisibleObject(this);
		world.removeObject(this);
		notifyPlayersLogout(world.getRecognizePlayer(this));
		_inventory.clearItems();
		_dwarf.clearItems();
		removeAllKnownObjects();
		stopHpRegeneration();
		stopMpRegeneration();
		setDead(true); // ??????????????????????????????????????????????????????????????????????????????????????????????????????
		setNetConnection(null);
		setPacketOutput(null);
		
	}

	public ClientThread getNetConnection() {
		return _netConnection;
	}

	public void setNetConnection(ClientThread clientthread) {
		_netConnection = clientthread;
	}

	public boolean isInParty() {
		return getParty() != null;
	}

	public L1Party getParty() {
		return _party;
	}

	public void setParty(L1Party p) {
		_party = p;
	}

	public boolean isInChatParty() {
		return getChatParty() != null;
	}

	public L1ChatParty getChatParty() {
		return _chatParty;
	}

	public void setChatParty(L1ChatParty cp) {
		_chatParty = cp;
	}

	public int getPartyID() {
		return _partyID;
	}

	public void setPartyID(int partyID) {
		_partyID = partyID;
	}

	public int getTradeID() {
		return _tradeID;
	}

	public void setTradeID(int tradeID) {
		_tradeID = tradeID;
	}

	public void setTradeOk(boolean tradeOk) {
		_tradeOk = tradeOk;
	}

	public boolean getTradeOk() {
		return _tradeOk;
	}

	public int getTempID() {
		return _tempID;
	}

	public void setTempID(int tempID) {
		_tempID = tempID;
	}

	public boolean isTeleport() {
		return _isTeleport;
	}

	public void setTeleport(boolean flag) {
		_isTeleport = flag;
	}

	public boolean isDrink() {
		return _isDrink;
	}

	public void setDrink(boolean flag) {
		_isDrink = flag;
	}

	public boolean isGres() {
		return _isGres;
	}

	public void setGres(boolean flag) {
		_isGres = flag;
	}

	public boolean isPinkName() {
		return _isPinkName;
	}

	public void setPinkName(boolean flag) {
		_isPinkName = flag;
	}

	private ArrayList<L1PrivateShopSellList> _sellList = new ArrayList<L1PrivateShopSellList>();

	public ArrayList<L1PrivateShopSellList> getSellList() {
		return _sellList;
	}

	private ArrayList<L1PrivateShopBuyList> _buyList = new ArrayList<L1PrivateShopBuyList>();

	public ArrayList<L1PrivateShopBuyList> getBuyList() {
		return _buyList;
	}

	private byte[] _shopChat;

	public void setShopChat(byte[] chat) {
		_shopChat = chat;
	}

	public byte[] getShopChat() {
		return _shopChat;
	}

	private boolean _isPrivateShop = false;

	public boolean isPrivateShop() {
		return _isPrivateShop;
	}

	public void setPrivateShop(boolean flag) {
		_isPrivateShop = flag;
	}

	private boolean _isTradingInPrivateShop = false;

	public boolean isTradingInPrivateShop() {
		return _isTradingInPrivateShop;
	}

	public void setTradingInPrivateShop(boolean flag) {
		_isTradingInPrivateShop = flag;
	}

	private int _partnersPrivateShopItemCount = 0; // ??????????????????????????????????????????

	public int getPartnersPrivateShopItemCount() {
		return _partnersPrivateShopItemCount;
	}

	public void setPartnersPrivateShopItemCount(int i) {
		_partnersPrivateShopItemCount = i;
	}

	private PacketOutput _out;

	public void setPacketOutput(PacketOutput out) {
		_out = out;
	}

	public void sendPackets(ServerBasePacket serverbasepacket) {
		if (_out == null) {
			return;
		}

		try {
			_out.sendPacket(serverbasepacket);
		} catch (Exception e) {
		}
	}

	@Override
	public void onAction(L1PcInstance attacker) {
		// XXX:NullPointerException?????????onAction??????????????????L1Character?????????????????????
		if (attacker == null) {
			return;
		}
		// ????????????????????????
		if (isTeleport()) {
			return;
		}
		// ????????????????????????????????????????????????????????????????????????
		if (getZoneType() == 1 || attacker.getZoneType() == 1) {
			// ???????????????????????????
			L1Attack attack_mortion = new L1Attack(attacker, this);
			attack_mortion.action();
			return;
		}

		if (checkNonPvP(this, attacker) == true) {
			return;
		}

		if (getCurrentHp() > 0 && !isDead()) {
			attacker.delInvis();

			boolean isCounterBarrier = false;
			L1Attack attack = new L1Attack(attacker, this);
			if (attack.calcHit()) {
				if (hasSkillEffect(COUNTER_BARRIER)) {
					L1Magic magic = new L1Magic(this, attacker);
					boolean isProbability = magic
							.calcProbabilityMagic(COUNTER_BARRIER);
					boolean isShortDistance = attack.isShortDistance();
					if (isProbability && isShortDistance) {
						isCounterBarrier = true;
					}
				}
				if (!isCounterBarrier) {
					attacker.setPetTarget(this);

					attack.calcDamage();
					//attack.calcStaffOfMana();
					//noted by sdcom 2009.12.12
					attack.addPcPoisonAttack(attacker, this);
				}
			}
			if (isCounterBarrier) {
				attack.actionCounterBarrier();
				attack.commitCounterBarrier();
			} else {
				attack.action();
				attack.commit();
			}
		}
	}

	public boolean checkNonPvP(L1PcInstance pc, L1Character target) {
		L1PcInstance targetpc = null;
		if (target instanceof L1PcInstance) {
			targetpc = (L1PcInstance) target;
		} else if (target instanceof L1PetInstance) {
			targetpc = (L1PcInstance) ((L1PetInstance) target).getMaster();
		} else if (target instanceof L1SummonInstance) {
			targetpc = (L1PcInstance) ((L1SummonInstance) target).getMaster();
		}
		if (targetpc == null) {
			return false; // ?????????PC??????????????????????????????
		}
		if (!Config.ALT_NONPVP) { // Non-PvP??????
			if (getMap().isCombatZone(getLocation())) {
				return false;
			}

			// ???????????????????????????
			for (L1War war : L1World.getInstance().getWarList()) {
				if (pc.getClanid() != 0 && targetpc.getClanid() != 0) { // ????????????????????????
					boolean same_war = war.CheckClanInSameWar(pc.getClanname(),
							targetpc.getClanname());
					if (same_war == true) { // ????????????????????????
						return false;
					}
				}
			}
			// Non-PvP???????????????????????????????????????????????????
			if (target instanceof L1PcInstance) {
				L1PcInstance targetPc = (L1PcInstance) target;
				if (isInWarAreaAndWarTime(pc, targetPc)) {
					return false;
				}
			}
			return true;
		} else {
			return false;
		}
	}

	private boolean isInWarAreaAndWarTime(L1PcInstance pc, L1PcInstance target) {
		// pc???target??????????????????????????????????????????
		int castleId = L1CastleLocation.getCastleIdByArea(pc);
		int targetCastleId = L1CastleLocation.getCastleIdByArea(target);
		if (castleId != 0 && targetCastleId != 0 && castleId == targetCastleId) {
			if (WarTimeController.getInstance().isNowWar(castleId)) {
				return true;
			}
		}
		return false;
	}

	public void setPetTarget(L1Character target) {
		Object[] petList = getPetList().values().toArray();
		for (Object pet : petList) {
			if (pet instanceof L1PetInstance) {
				L1PetInstance pets = (L1PetInstance) pet;
				pets.setMasterTarget(target);
			} else if (pet instanceof L1SummonInstance) {
				L1SummonInstance summon = (L1SummonInstance) pet;
				summon.setMasterTarget(target);
			}
		}
	}

	public void delInvis() {
		// ??????????????????????????????????????????
		if (hasSkillEffect(INVISIBILITY)) { // ????????????????????????
			killSkillEffectTimer(INVISIBILITY);
			sendPackets(new S_Invis(getId(), 0));
			broadcastPacket(new S_OtherCharPacks(this));
		}
		if (hasSkillEffect(BLIND_HIDING)) { // ??????????????? ??????????????????
			killSkillEffectTimer(BLIND_HIDING);
			sendPackets(new S_Invis(getId(), 0));
			broadcastPacket(new S_OtherCharPacks(this));
		}
	}

	public void delBlindHiding() {
		// ????????????????????????????????????
		killSkillEffectTimer(BLIND_HIDING);
		sendPackets(new S_Invis(getId(), 0));
		broadcastPacket(new S_OtherCharPacks(this));
	}

	// ???????????????????????????????????????????????? (???????????????????????????????????????) attr:0.???????????????,1.?????????,2.?????????,3.?????????,4.?????????
	public void receiveDamage(L1Character attacker, int damage, int attr) {
		int player_mr = getMr();
		int rnd = _random.nextInt(100) + 1;
		if (player_mr >= rnd) {
			damage /= 2;
		}
		receiveDamage(attacker, damage);
	}

	public void receiveManaDamage(L1Character attacker, int mpDamage) { // ???????????????????????????????????????????????????
		if (mpDamage > 0 && !isDead()) {
			delInvis();
			if (attacker instanceof L1PcInstance) {
				L1PinkName.onAction(this, attacker);
			}
			if (attacker instanceof L1PcInstance
					&& ((L1PcInstance) attacker).isPinkName()) {
				// ??????????????????????????????????????????????????????????????????????????????????????????
				for (L1Object object : L1World.getInstance().getVisibleObjects(
						attacker)) {
					if (object instanceof L1GuardInstance) {
						L1GuardInstance guard = (L1GuardInstance) object;
						guard.setTarget(((L1PcInstance) attacker));
					}
				}
			}

			int newMp = getCurrentMp() - mpDamage;
			if (newMp > getMaxMp()) {
				newMp = getMaxMp();
			}

			if (newMp <= 0) {
				newMp = 0;
			}
			setCurrentMp(newMp);
		}
	}

	public void receiveDamage(L1Character attacker, int damage) { // ???????????????????????????????????????????????????
		if (getCurrentHp() > 0 && !isDead()) {
			if (attacker != this && !knownsObject(attacker)
					&& attacker.getMapId() == this.getMapId()) {
				attacker.onPerceive(this);
			}

			if (damage > 0) {
				delInvis();
				if (attacker instanceof L1PcInstance) {
					L1PinkName.onAction(this, attacker);
				}
				if (attacker instanceof L1PcInstance
						&& ((L1PcInstance) attacker).isPinkName()) {
					// ??????????????????????????????????????????????????????????????????????????????????????????
					for (L1Object object : L1World.getInstance()
							.getVisibleObjects(attacker)) {
						if (object instanceof L1GuardInstance) {
							L1GuardInstance guard = (L1GuardInstance) object;
							guard.setTarget(((L1PcInstance) attacker));
						}
					}
				}
				removeSkillEffect(FOG_OF_SLEEPING);
			}
			
			if (hasSkillEffect(MORTAL_BODY)
					&& getId() != attacker.getId()) {
				int rnd = _random.nextInt(100) + 1;
				if (damage > 0 && rnd <= 10) {
					if (attacker instanceof L1PcInstance) {
						L1PcInstance attackPc = (L1PcInstance) attacker;
						attackPc.sendPackets(new S_DoActionGFX(attackPc
								.getId(), ActionCodes.ACTION_Damage));
						attackPc.broadcastPacket(new S_DoActionGFX(attackPc
								.getId(), ActionCodes.ACTION_Damage));
						attackPc.receiveDamage(this, 30);
					} else if (attacker instanceof L1NpcInstance) {
						L1NpcInstance attackNpc = (L1NpcInstance) attacker;
						attackNpc.broadcastPacket(new S_DoActionGFX(attackNpc
								.getId(), ActionCodes.ACTION_Damage));
						attackNpc.receiveDamage(this, 30);
					}
				}
			}
			
			if (hasSkillEffect(JOY_OF_PAIN)
					&& getId() != attacker.getId()) {
				int nowDamage = getMaxHp() - getCurrentHp();
				if (nowDamage > 0) {
					if (attacker instanceof L1PcInstance) {
						L1PcInstance attackPc = (L1PcInstance) attacker;
						attackPc.sendPackets(new S_DoActionGFX(attackPc
								.getId(), ActionCodes.ACTION_Damage));
						attackPc.broadcastPacket(new S_DoActionGFX(attackPc
								.getId(), ActionCodes.ACTION_Damage));
						attackPc.receiveDamage(this, (int) (nowDamage / 5));
					} else if (attacker instanceof L1NpcInstance) {
						L1NpcInstance attackNpc = (L1NpcInstance) attacker;
						attackNpc.broadcastPacket(new S_DoActionGFX(attackNpc
								.getId(), ActionCodes.ACTION_Damage));
						attackNpc.receiveDamage(this, (int) (nowDamage / 5));
					}
				}
			}
			if (getInventory().checkEquipped(145) // ??????????????????????????????
					|| getInventory().checkEquipped(149)) { // ??????????????????????????????
				damage *= 1.5; // ?????????1.5???
			}
			if (hasSkillEffect(ILLUSION_AVATAR)) {
				damage *= 1.5; // ?????????1.5???
			}
			int newHp = getCurrentHp() - damage;
			if (newHp > getMaxHp()) {
				newHp = getMaxHp();
			}
			if (newHp <= 0) {
				if (isGm()) {
					setCurrentHp(getMaxHp());
				} else {
					death(attacker);
				}
			}
			if (newHp > 0) {
				setCurrentHp(newHp);
			}
		} else if (!isDead()) { // ????????????
			System.out
					.println("?????????PC???hp???????????????????????????????????????hp=0?????????");
			death(attacker);
		}
	}

	public void death(L1Character lastAttacker) {
		synchronized (this) {
			if (isDead()) {
				return;
			}
			setDead(true);
			setStatus(ActionCodes.ACTION_Die);
		}
		GeneralThreadPool.getInstance().execute(new Death(lastAttacker));

	}

	private class Death implements Runnable {
		L1Character _lastAttacker;

		Death(L1Character cha) {
			_lastAttacker = cha;
		}

		public void run() {
			L1Character lastAttacker = _lastAttacker;
			_lastAttacker = null;
			setCurrentHp(0);
			setGresValid(false); // EXP?????????????????????G-RES??????

			while (isTeleport()) { // ?????????????????????????????????????????????
				try {
					Thread.sleep(300);
				} catch (Exception e) {
				}
			}

			stopHpRegeneration();
			stopMpRegeneration();

			int targetobjid = getId();
			getMap().setPassable(getLocation(), true);

			// ?????????????????????????????????
			// ?????????????????????????????????????????????????????????????????????????????????????????????????????????
			int tempchargfx = 0;
			if (hasSkillEffect(SHAPE_CHANGE)) {
				tempchargfx = getTempCharGfx();
				setTempCharGfxAtDead(tempchargfx);
			} else {
				setTempCharGfxAtDead(getClassId());
			}

			// ???????????????????????????????????????????????????????????????
			L1SkillUse l1skilluse = new L1SkillUse();
			l1skilluse.handleCommands(L1PcInstance.this,
					CANCELLATION, getId(), getX(), getY(), null, 0,
					L1SkillUse.TYPE_LOGIN);

			// ??????????????????????????????????????????????????????????????????????????????????????????
			if (tempchargfx == 5727 || tempchargfx == 5730
					|| tempchargfx == 5733 || tempchargfx == 5736) {
				tempchargfx = 0;
			}
			if (tempchargfx != 0) {
				sendPackets(new S_ChangeShape(getId(), tempchargfx));
				broadcastPacket(new S_ChangeShape(getId(), tempchargfx));
			} else {
				// ????????????????????????????????????????????????????????????????????????????????????????????????????????????????????????
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
				}
			}

			sendPackets(new S_DoActionGFX(targetobjid, ActionCodes.ACTION_Die));
			broadcastPacket(new S_DoActionGFX(targetobjid,
					ActionCodes.ACTION_Die));

			if (lastAttacker != L1PcInstance.this) {
				// ???????????????????????????????????????????????????????????????????????????????????????
				// ???????????????or?????????????????????????????????????????????
				if (getZoneType() != 0) {
					L1PcInstance player = null;
					if (lastAttacker instanceof L1PcInstance) {
						player = (L1PcInstance) lastAttacker;
					} else if (lastAttacker instanceof L1PetInstance) {
						player = (L1PcInstance) ((L1PetInstance) lastAttacker)
								.getMaster();
					} else if (lastAttacker instanceof L1SummonInstance) {
						player = (L1PcInstance) ((L1SummonInstance) lastAttacker)
								.getMaster();
					}
					if (player != null) {
						// ???????????????????????????????????????????????????
						if (!isInWarAreaAndWarTime(L1PcInstance.this, player)) {
							return;
						}
					}
				}

				boolean sim_ret = simWarResult(lastAttacker); // ?????????
				if (sim_ret == true) { // ???????????????????????????????????????
					return;
				}
			}

			if (!getMap().isEnabledDeathPenalty()) {
				return;
			}

			// ????????????????????????????????????
			L1PcInstance fightPc = null;
			if (lastAttacker instanceof L1PcInstance) {
				fightPc = (L1PcInstance) lastAttacker;
			}
			if (fightPc != null) {
				if (getFightId() == fightPc.getId()
						&& fightPc.getFightId() == getId()) { // ?????????
					setFightId(0);
					sendPackets(new S_PacketBox(S_PacketBox.MSG_DUEL, 0, 0));
					fightPc.setFightId(0);
					fightPc.sendPackets(new S_PacketBox(S_PacketBox.MSG_DUEL,
							0, 0));
					return;
				}
			}

			deathPenalty(); // EXP?????????

			setGresValid(true); // EXP??????????????????G-RES??????

			if (getExpRes() == 0) {
				setExpRes(1);
			}

			// ???????????????????????????????????????PK????????????????????????????????????????????????????????????
			if (lastAttacker instanceof L1GuardInstance) {
				if (get_PKcount() > 0) {
					set_PKcount(get_PKcount() - 1);
				}
				setLastPk(null);
			}

			// ?????????????????????????????????DROP
			// ??????????????????32000?????????0%?????????-1000??????0.4%
			// ?????????????????????0??????????????????-1000??????0.8%
			// ??????????????????-32000???????????????51.2%???DROP???
			int lostRate = (int) (((getLawful() + 32768D) / 1000D - 65D) * 4D);
			if (lostRate < 0) {
				lostRate *= -1;
				if (getLawful() < 0) {
					lostRate *= 2;
				}
				int rnd = _random.nextInt(1000) + 1;
				if (rnd <= lostRate) {
					int count = 1;
					if (getLawful() <= -30000) {
						count = _random.nextInt(4) + 1;
					} else if (getLawful() <= -20000) {
						count = _random.nextInt(3) + 1;
					} else if (getLawful() <= -10000) {
						count = _random.nextInt(2) + 1;
					} else if (getLawful() < 0) {
						count = _random.nextInt(1) + 1;
					}
					caoPenaltyResult(count);
				}
			}

			boolean castle_ret = castleWarResult(); // ?????????
			if (castle_ret == true) { // ????????????????????????????????????????????????????????????
				return;
			}

			// ?????????????????????????????????????????????????????????????????????????????????
			L1PcInstance player = null;
			if (lastAttacker instanceof L1PcInstance) {
				player = (L1PcInstance) lastAttacker;
			}
			if (player != null) {
				if (getLawful() >= 0 && isPinkName() == false) {
					boolean isChangePkCount = false;
					// ?????????????????????30000??????????????????PK??????????????????
					if (player.getLawful() < 30000) {
						player.set_PKcount(player.get_PKcount() + 1);
						isChangePkCount = true;
					}
					player.setLastPk();

					// ????????????????????????
					// ???????????????????????????LV??????PK??????????????????????????????????????????
					// ???PK??????LV??????????????????LV???????????????????????????
					// 48????????????-8k?????? DK????????????10k???
					// 60??????20k??? 65???30k???
					int lawful;

					if (player.getLevel() < 50) {
						lawful = -1
								* (int) ((Math.pow(player.getLevel(), 2) * 4));
					} else {
						lawful = -1
								* (int) ((Math.pow(player.getLevel(), 3) * 0.08));
					}
					// ??????(???????????????????????????-1000)??????????????????????????????
					// ???????????????????????????-1000?????????????????????????????????
					// ????????????PK?????????????????????????????????????????????????????????????????????
					// ?????????????????????????????????????????????????????????????????????
					// ???????????????????????????????????????????????????????????????????????????
					if ((player.getLawful() - 1000) < lawful) {
						lawful = player.getLawful() - 1000;
					}

					if (lawful <= -32768) {
						lawful = -32768;
					}
					player.setLawful(lawful);

					S_Lawful s_lawful = new S_Lawful(player.getId(), player
							.getLawful());
					player.sendPackets(s_lawful);
					player.broadcastPacket(s_lawful);

					if (isChangePkCount && player.get_PKcount() >= 5
							&& player.get_PKcount() < 10) {
						// ????????????PK?????????%0??????????????????????????????%1?????????????????????????????????
						player.sendPackets(new S_BlueMessage(551, String
								.valueOf(player.get_PKcount()), "10"));
					} else if (isChangePkCount && player.get_PKcount() >= 10) {
						player.beginHell(true);
					}
				} else {
					setPinkName(false);
				}
			}
		}
	}

	private void caoPenaltyResult(int count) {
		for (int i = 0; i < count; i++) {
			L1ItemInstance item = getInventory().CaoPenalty();

			if (item != null) {
				getInventory().tradeItem(
						item,
						item.isStackable() ? item.getCount() : 1,
						L1World.getInstance().getInventory(getX(), getY(),
								getMapId()));
				sendPackets(new S_ServerMessage(638,item.getLogName())); // %0?????????????????????
			} else {
			}
		}
	}

	public boolean castleWarResult() {
		if (getClanid() != 0 && isCrown()) { // ???????????????????????????????????????
			L1Clan clan = L1World.getInstance().getClan(getClanname());
			// ???????????????????????????
			for (L1War war : L1World.getInstance().getWarList()) {
				int warType = war.GetWarType();
				boolean isInWar = war.CheckClanInWar(getClanname());
				boolean isAttackClan = war.CheckAttackClan(getClanname());
				if (getId() == clan.getLeaderId() && // ????????????????????????????????????
						warType == 1 && isInWar && isAttackClan) {
					String enemyClanName = war.GetEnemyClanName(getClanname());
					if (enemyClanName != null) {
						war.CeaseWar(getClanname(), enemyClanName); // ??????
					}
					break;
				}
			}
		}

		int castleId = 0;
		boolean isNowWar = false;
		castleId = L1CastleLocation.getCastleIdByArea(this);
		if (castleId != 0) { // ???????????????
			isNowWar = WarTimeController.getInstance().isNowWar(castleId);
		}
		return isNowWar;
	}

	public boolean simWarResult(L1Character lastAttacker) {
		if (getClanid() == 0) { // ??????????????????????????????
			return false;
		}
		if (Config.SIM_WAR_PENALTY) { // ??????????????????????????????????????????false
			return false;
		}
		L1PcInstance attacker = null;
		String enemyClanName = null;
		boolean sameWar = false;

		if (lastAttacker instanceof L1PcInstance) {
			attacker = (L1PcInstance) lastAttacker;
		} else if (lastAttacker instanceof L1PetInstance) {
			attacker = (L1PcInstance) ((L1PetInstance) lastAttacker)
					.getMaster();
		} else if (lastAttacker instanceof L1SummonInstance) {
			attacker = (L1PcInstance) ((L1SummonInstance) lastAttacker)
					.getMaster();
		} else {
			return false;
		}

		// ???????????????????????????
		for (L1War war : L1World.getInstance().getWarList()) {
			L1Clan clan = L1World.getInstance().getClan(getClanname());

			int warType = war.GetWarType();
			boolean isInWar = war.CheckClanInWar(getClanname());
			if (attacker != null && attacker.getClanid() != 0) { // lastAttacker???PC?????????????????????????????????????????????
				sameWar = war.CheckClanInSameWar(getClanname(), attacker
						.getClanname());
			}

			if (getId() == clan.getLeaderId() && // ????????????????????????
					warType == 2 && isInWar == true) {
				enemyClanName = war.GetEnemyClanName(getClanname());
				if (enemyClanName != null) {
					war.CeaseWar(getClanname(), enemyClanName); // ??????
				}
			}

			if (warType == 2 && sameWar) {// ?????????????????????????????????????????????????????????????????????
				return true;
			}
		}
		return false;
	}

	public void resExp() {
		int oldLevel = getLevel();
		int needExp = ExpTable.getNeedExpNextLevel(oldLevel);
		int exp = 0;
		if (oldLevel < 45) {
			exp = (int) (needExp * 0.05);
		} else if (oldLevel == 45) {
			exp = (int) (needExp * 0.045);
		} else if (oldLevel == 46) {
			exp = (int) (needExp * 0.04);
		} else if (oldLevel == 47) {
			exp = (int) (needExp * 0.035);
		} else if (oldLevel == 48) {
			exp = (int) (needExp * 0.03);
		} else if (oldLevel >= 49) {
			exp = (int) (needExp * 0.025);
		}

		if (exp == 0) {
			return;
		}
		addExp(exp);
	}

	public void deathPenalty() {
		int oldLevel = getLevel();
		int needExp = ExpTable.getNeedExpNextLevel(oldLevel);
		int exp = 0;
		if (oldLevel >= 1 && oldLevel < 11) {
			exp = 0;
		} else if (oldLevel >= 11 && oldLevel < 45) {
			exp = (int) (needExp * 0.1);
		} else if (oldLevel == 45) {
			exp = (int) (needExp * 0.09);
		} else if (oldLevel == 46) {
			exp = (int) (needExp * 0.08);
		} else if (oldLevel == 47) {
			exp = (int) (needExp * 0.07);
		} else if (oldLevel == 48) {
			exp = (int) (needExp * 0.06);
		} else if (oldLevel >= 49) {
			exp = (int) (needExp * 0.05);
		}

		if (exp == 0) {
			return;
		}
		addExp(-exp);
	}

	private int _originalEr = 0; // ??? ???????????????DEX ER??????

	public int getOriginalEr() {

		return _originalEr;	
	}

	public int getEr() {
		if (hasSkillEffect(STRIKER_GALE)) {
			return 0;
		}

		int er = 0;
		if (isKnight()) {
			er = getLevel() / 4; // ?????????
		} else if (isCrown() || isElf()) {
			er = getLevel() / 8; // ??????????????????
		} else if (isDarkelf()) {
			er = getLevel() / 6; // ??????????????????
		} else if (isWizard()) {
			er = getLevel() / 10; // ???????????????
		} else if (isDragonKnight()) {
			er = getLevel() / 7; // ?????????????????????
		} else if (isIllusionist()) {
			er = getLevel() / 9; // ???????????????????????????
		}

		er += (getDex() - 8) / 2;

		er += getOriginalEr();

		if (hasSkillEffect(DRESS_EVASION)) {
			er += 12;
		}
		if (hasSkillEffect(SOLID_CARRIAGE)) {
			er += 15;
		}
		return er;
	}

	public L1BookMark getBookMark(String name) {
		for (int i = 0; i < _bookmarks.size(); i++) {
			L1BookMark element = _bookmarks.get(i);
			if (element.getName().equalsIgnoreCase(name)) {
				return element;
			}

		}
		return null;
	}

	public L1BookMark getBookMark(int id) {
		for (int i = 0; i < _bookmarks.size(); i++) {
			L1BookMark element = _bookmarks.get(i);
			if (element.getId() == id) {
				return element;
			}

		}
		return null;
	}

	public int getBookMarkSize() {
		return _bookmarks.size();
	}

	public void addBookMark(L1BookMark book) {
		_bookmarks.add(book);
	}

	public void removeBookMark(L1BookMark book) {
		_bookmarks.remove(book);
	}

	public L1ItemInstance getWeapon() {
		return _weapon;
	}

	public void setWeapon(L1ItemInstance weapon) {
		_weapon = weapon;
	}

	public L1Quest getQuest() {
		return _quest;
	}

	public boolean isCrown() {
		return (getClassId() == CLASSID_PRINCE
				|| getClassId() == CLASSID_PRINCESS);
	}

	public boolean isKnight() {
		return (getClassId() == CLASSID_KNIGHT_MALE
				|| getClassId() == CLASSID_KNIGHT_FEMALE);
	}

	public boolean isElf() {
		return (getClassId() == CLASSID_ELF_MALE
				|| getClassId() == CLASSID_ELF_FEMALE);
	}

	public boolean isWizard() {
		return (getClassId() == CLASSID_WIZARD_MALE
				|| getClassId() == CLASSID_WIZARD_FEMALE);
	}

	public boolean isDarkelf() {
		return (getClassId() == CLASSID_DARK_ELF_MALE
				|| getClassId() == CLASSID_DARK_ELF_FEMALE);
	}

	public boolean isDragonKnight() {
		return (getClassId() == CLASSID_DRAGON_KNIGHT_MALE
				|| getClassId() == CLASSID_DRAGON_KNIGHT_FEMALE);
	}
	public boolean isIllusionist() {
		return (getClassId() == CLASSID_ILLUSIONIST_MALE
				|| getClassId() == CLASSID_ILLUSIONIST_FEMALE);
	}

	private static Logger _log = Logger.getLogger(L1PcInstance.class.getName());
	private ClientThread _netConnection;
	private int _classId;
	private int _type;
	private int _exp;
	private final L1Karma _karma = new L1Karma();
	private boolean _gm;
	private boolean _monitor;
	private boolean _gmInvis;
	private short _accessLevel;
	private int _currentWeapon;
	private final L1PcInventory _inventory;
	private final L1DwarfInventory _dwarf;
	private final L1DwarfForElfInventory _dwarfForElf;
	private final L1Inventory _tradewindow;
	private L1ItemInstance _weapon;
	private L1Party _party;
	private L1ChatParty _chatParty;
	private int _partyID;
	private int _tradeID;
	private boolean _tradeOk;
	private int _tempID;
	private boolean _isTeleport = false;
	private boolean _isDrink = false;
	private boolean _isGres = false;
	private boolean _isPinkName = false;
	private final ArrayList<L1BookMark> _bookmarks;
	private L1Quest _quest;
	private MpRegeneration _mpRegen;
	private MpRegenerationByDoll _mpRegenByDoll;
	private MpReductionByAwake _mpReductionByAwake;
	private HpRegeneration _hpRegen;
	private HpRegenerationByDoll _hpRegenByDoll;//add ????????????????????????
	private static Timer _regenTimer = new Timer(true);
	private boolean _mpRegenActive;
	private boolean _mpRegenActiveByDoll;
	private boolean _mpReductionActiveByAwake;
	private boolean _hpRegenActive;
	private boolean _hpRegenActiveByDoll;//add ????????????????????????
	private L1EquipmentSlot _equipSlot;

	private String _accountName; // ??? ????????????????????????

	public String getAccountName() {
		return _accountName;
	}

	public void setAccountName(String s) {
		_accountName = s;
	}
	
	//????????????????????????????????? by eric1300460
	public void setBaseMaxHp(short i) {
		if (i >= 32767) {
			i = 32767;
		} else if (i < 1) {
			i = 1;
		}
		_baseMaxHp=i;
	}
	public void setBaseMaxMp(short i) {
		if (i >= 32767) {
			i = 32767;
		} else if (i < 1) {
			i = 1;
		}
		_baseMaxMp=i;
	}
	//~????????????????????????????????? by eric1300460
	
	private short _baseMaxHp = 0; // ??? ???????????????????????????1???32767???

	public short getBaseMaxHp() {
		return _baseMaxHp;
	}

	public void addBaseMaxHp(short i) {
		i += _baseMaxHp;
		if (i >= 32767) {
			i = 32767;
		} else if (i < 1) {
			i = 1;
		}
		addMaxHp(i - _baseMaxHp);
		_baseMaxHp = i;
	}

	private short _baseMaxMp = 0; // ??? ???????????????????????????0???32767???

	public short getBaseMaxMp() {
		return _baseMaxMp;
	}

	public void addBaseMaxMp(short i) {
		i += _baseMaxMp;
		if (i >= 32767) {
			i = 32767;
		} else if (i < 0) {
			i = 0;
		}
		addMaxMp(i - _baseMaxMp);
		_baseMaxMp = i;
	}

	private int _baseAc = 0; // ??? ??????????????????-128???127???

	public int getBaseAc() {
		return _baseAc;
	}

	private int _originalAc = 0; // ??? ???????????????DEX ????????????

	public int getOriginalAc() {

		return _originalAc;
	}


	private byte _baseStr = 0; // ??? ?????????????????????1???127???

	public byte getBaseStr() {
		return _baseStr;
	}

	public void addBaseStr(byte i) {
		i += _baseStr;
		if (i >= 127) {
			i = 127;
		} else if (i < 1) {
			i = 1;
		}
		addStr((byte) (i - _baseStr));
		_baseStr = i;
	}

	private byte _baseCon = 0; // ??? ?????????????????????1???127???

	public byte getBaseCon() {
		return _baseCon;
	}

	public void addBaseCon(byte i) {
		i += _baseCon;
		if (i >= 127) {
			i = 127;
		} else if (i < 1) {
			i = 1;
		}
		addCon((byte) (i - _baseCon));
		_baseCon = i;
	}

	private byte _baseDex = 0; // ??? ?????????????????????1???127???

	public byte getBaseDex() {
		return _baseDex;
	}

	public void addBaseDex(byte i) {
		i += _baseDex;
		if (i >= 127) {
			i = 127;
		} else if (i < 1) {
			i = 1;
		}
		addDex((byte) (i - _baseDex));
		_baseDex = i;
	}

	private byte _baseCha = 0; // ??? ?????????????????????1???127???

	public byte getBaseCha() {
		return _baseCha;
	}

	public void addBaseCha(byte i) {
		i += _baseCha;
		if (i >= 127) {
			i = 127;
		} else if (i < 1) {
			i = 1;
		}
		addCha((byte) (i - _baseCha));
		_baseCha = i;
	}

	private byte _baseInt = 0; // ??? ?????????????????????1???127???

	public byte getBaseInt() {
		return _baseInt;
	}

	public void addBaseInt(byte i) {
		i += _baseInt;
		if (i >= 127) {
			i = 127;
		} else if (i < 1) {
			i = 1;
		}
		addInt((byte) (i - _baseInt));
		_baseInt = i;
	}

	private byte _baseWis = 0; // ??? ?????????????????????1???127???

	public byte getBaseWis() {
		return _baseWis;
	}

	public void addBaseWis(byte i) {
		i += _baseWis;
		if (i >= 127) {
			i = 127;
		} else if (i < 1) {
			i = 1;
		}
		addWis((byte) (i - _baseWis));
		_baseWis = i;
	}

	private int _originalStr = 0; // ??? ??????????????? STR

	public int getOriginalStr() {
		return _originalStr;
	}

	public void setOriginalStr(int i) {
		_originalStr = i;
	}

	private int _originalCon = 0; // ??? ??????????????? CON

	public int getOriginalCon() {
		return _originalCon;
	}

	public void setOriginalCon(int i) {
		_originalCon = i;
	}

	private int _originalDex = 0; // ??? ??????????????? DEX

	public int getOriginalDex() {
		return _originalDex;
	}

	public void setOriginalDex(int i) {
		_originalDex = i;
	}

	private int _originalCha = 0; // ??? ??????????????? CHA

	public int getOriginalCha() {
		return _originalCha;
	}

	public void setOriginalCha(int i) {
		_originalCha = i;
	}

	private int _originalInt = 0; // ??? ??????????????? INT

	public int getOriginalInt() {
		return _originalInt;
	}

	public void setOriginalInt(int i) {
		_originalInt = i;
	}

	private int _originalWis = 0; // ??? ??????????????? WIS

	public int getOriginalWis() {
		return _originalWis;
	}

	public void setOriginalWis(int i) {
		_originalWis = i;		
	}

	private int _originalDmgup = 0; // ??? ???????????????STR ??????????????????

	public int getOriginalDmgup() {

		return _originalDmgup;
	}

	private int _originalBowDmgup = 0; // ??? ???????????????DEX ?????????????????????

	public int getOriginalBowDmgup() {

		return _originalBowDmgup;
	}

	private int _originalHitup = 0; // ??? ???????????????STR ????????????

	public int getOriginalHitup() {

		return _originalHitup;
	}

	public int getOriginalBowHitup() {
		return _originalHitup;
	}

	private int _originalMr = 0; // ??? ???????????????WIS ????????????

	public int getOriginalMr() {

		return _originalMr;
	}

	private int _originalMagicHit = 0; // ??? ???????????????INT ????????????

	public int getOriginalMagicHit() {

		return _originalMagicHit;
	}

	private int _originalMagicCritical = 0; // ??? ???????????????INT ????????????????????????

	public int getOriginalMagicCritical() {

		return _originalMagicCritical;
	}

	private int _originalMagicConsumeReduction = 0; // ??? ???????????????INT ??????MP??????

	public int getOriginalMagicConsumeReduction() {

		return _originalMagicConsumeReduction;
	}

	private int _originalMagicDamage = 0; // ??? ???????????????INT ??????????????????

	public int getOriginalMagicDamage() {

		return _originalMagicDamage;
	}

	private int _originalHpup = 0; // ??? ???????????????CON HP???????????????

	public int getOriginalHpup() {

		return _originalHpup;
	}

	private int _originalMpup = 0; // ??? ???????????????WIS MP???????????????

	public int getOriginalMpup() {

		return _originalMpup;
	}

	private int _baseDmgup = 0; // ??? ??????????????????????????????-128???127???

	public int getBaseDmgup() {
		return _baseDmgup;
	}

	private int _baseBowDmgup = 0; // ??? ?????????????????????????????????-128???127???

	public int getBaseBowDmgup() {
		return _baseBowDmgup;
	}

	private int _baseHitup = 0; // ??? ????????????????????????-128???127???

	public int getBaseHitup() {
		return _baseHitup;
	}

	private int _baseBowHitup = 0; // ??? ???????????????????????????-128???127???

	public int getBaseBowHitup() {
		return _baseBowHitup;
	}

	private int _baseMr = 0; // ??? ????????????????????????0??????

	public int getBaseMr() {
		return _baseMr;
	}

	private int _advenHp; // ??? // ?????????????????? ??????????????????????????????????????????

	public int getAdvenHp() {
		return _advenHp;
	}

	public void setAdvenHp(int i) {
		_advenHp = i;
	}

	private int _advenMp; // ??? // ?????????????????? ??????????????????????????????????????????

	public int getAdvenMp() {
		return _advenMp;
	}

	public void setAdvenMp(int i) {
		_advenMp = i;
	}

	private int _highLevel; // ??? ?????????????????????

	public int getHighLevel() {
		return _highLevel;
	}

	public void setHighLevel(int i) {
		_highLevel = i;
	}

	private int _bonusStats; // ??? ??????????????????????????????????????????

	public int getBonusStats() {
		return _bonusStats;
	}

	public void setBonusStats(int i) {
		_bonusStats = i;
	}
	
	// STR, CON, DEX, CHA, INT, WIS
	private int[] _additionalAttr = {0,0,0,0,0,0};  
	
	public int getAdditionalAttr(int a) {
		return _additionalAttr[a];
	}
	
	public void setAdditionalAttr(int a, int val) {
		_additionalAttr[a] = val;
	}
	
	private int _elixirStats; // ??? ?????????????????????????????????????????????

	public int getElixirStats() {
		return _elixirStats;
	}

	public void setElixirStats(int i) {
		_elixirStats = i;
	}

	private int _elfAttr; // ??? ??????????????????

	public int getElfAttr() {
		return _elfAttr;
	}

	public void setElfAttr(int i) {
		_elfAttr = i;
	}

	private int _expRes; // ??? EXP??????

	public int getExpRes() {
		return _expRes;
	}

	public void setExpRes(int i) {
		_expRes = i;
	}

	private int _partnerId; // ??? ????????????

	public int getPartnerId() {
		return _partnerId;
	}

	public void setPartnerId(int i) {
		_partnerId = i;
	}

	private int _onlineStatus; // ??? ?????????????????????

	public int getOnlineStatus() {
		return _onlineStatus;
	}

	public void setOnlineStatus(int i) {
		_onlineStatus = i;
	}

	private int _homeTownId; // ??? ??????????????????

	public int getHomeTownId() {
		return _homeTownId;
	}

	public void setHomeTownId(int i) {
		_homeTownId = i;
	}

	private int _contribution; // ??? ?????????

	public int getContribution() {
		return _contribution;
	}

	public void setContribution(int i) {
		_contribution = i;
	}

	// ????????????????????????????????????
	private int _hellTime;

	public int getHellTime() {
		return _hellTime;
	}

	public void setHellTime(int i) {
		_hellTime = i;
	}

	private boolean _banned; // ??? ??????

	public boolean isBanned() {
		return _banned;
	}

	public void setBanned(boolean flag) {
		_banned = flag;
	}

	private int _food; // ??? ?????????

	public int get_food() {
		return _food;
	}

	public void set_food(int i) {
		_food = i;
	}

	public L1EquipmentSlot getEquipSlot() {
		return _equipSlot;
	}

	public static L1PcInstance load(String charName) {
		L1PcInstance result = null;
		try {
			result = CharacterTable.getInstance().loadCharacter(charName);
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		return result;
	}

	/**
	 * ??????????????????????????????????????????????????????????????????
	 * 
	 * @throws Exception
	 */
	public void save() throws Exception {
		if (isGhost()) {
			return;
		}
		if (isInCharReset()) {
			return;
		}
		CharacterTable.getInstance().storeCharacter(this);
	}

	/**
	 * ???????????????????????????????????????????????????????????????????????????????????????????????????
	 */
	public void saveInventory() {
		for (L1ItemInstance item : getInventory().getItems()) {
			getInventory().saveItem(item, item.getRecordingColumns());
		}
	}

	public static final int REGENSTATE_NONE = 4;
	public static final int REGENSTATE_MOVE = 2;
	public static final int REGENSTATE_ATTACK = 1;

	public void setRegenState(int state) {
		_mpRegen.setState(state);
		_hpRegen.setState(state);
	}

	public double getMaxWeight() {
		int str = getStr();
		int con = getCon();
		double maxWeight = 150 * (Math.floor(0.6 * str + 0.4 * con + 1));

		double weightReductionByArmor = getWeightReduction(); // ???????????????????????????
		weightReductionByArmor /= 100;

		double weightReductionByDoll = 0; // ??????????????????????????????????????????
		Object[] dollList = getDollList().values().toArray();
		for (Object dollObject : dollList) {
			L1DollInstance doll = (L1DollInstance) dollObject;
			weightReductionByDoll += doll.getWeightReductionByDoll();
			weightReductionByDoll /= 100;
		}

		int weightReductionByMagic = 0;
		if (hasSkillEffect(DECREASE_WEIGHT)) { // ??????????????????????????????
			weightReductionByMagic = 180;
		}

		double originalWeightReduction = 0; // ???????????????????????????????????????????????????
		originalWeightReduction += 0.04 * (getOriginalStrWeightReduction()
				+ getOriginalConWeightReduction());

		double weightReduction = 1 + weightReductionByArmor
				+ weightReductionByDoll + originalWeightReduction;

		maxWeight *= weightReduction;

		maxWeight += weightReductionByMagic;

		maxWeight *= Config.RATE_WEIGHT_LIMIT; // ?????????????????????????????????

		return maxWeight;
	}

	public boolean isFastMovable() {
		return (hasSkillEffect(HOLY_WALK)
				|| hasSkillEffect(MOVING_ACCELERATION)
				|| hasSkillEffect(WIND_WALK)
				|| hasSkillEffect(STATUS_RIBRAVE)
				//XXX ???????????? - ??????????????????
				|| hasSkillEffect(STATUS_BRAVE2)
				//????????????
				|| hasSkillEffect(STATUS_THIRD_SPEED)
				//END
				);
	}

	public boolean isBrave() {
		return hasSkillEffect(STATUS_BRAVE);
	}

	public boolean isElfBrave() {
		return hasSkillEffect(STATUS_ELFBRAVE);
	}

	public boolean isHaste() {
		return (hasSkillEffect(STATUS_HASTE)
				|| hasSkillEffect(HASTE)
				|| hasSkillEffect(GREATER_HASTE) || getMoveSpeed() == 1);
	}

	private int invisDelayCounter = 0;

	public boolean isInvisDelay() {
		return (invisDelayCounter > 0);
	}

	private Object _invisTimerMonitor = new Object();

	public void addInvisDelayCounter(int counter) {
		synchronized (_invisTimerMonitor) {
			invisDelayCounter += counter;
		}
	}

	private static final long DELAY_INVIS = 3000L;

	public void beginInvisTimer() {
		addInvisDelayCounter(1);
		GeneralThreadPool.getInstance().pcSchedule(new L1PcInvisDelay(getId()),
				DELAY_INVIS);
	}
	public synchronized void addExp(int addExp) {
		_exp += addExp;
		if (_exp > ExpTable.MAX_EXP) {
			_exp = ExpTable.MAX_EXP;
		}
	}

	public synchronized void addContribution(int contribution) {
		_contribution += contribution;
	}

	public void beginExpMonitor() {
		_expMonitorFuture = GeneralThreadPool.getInstance().pcScheduleAtFixedRate(new L1PcExpMonitor(getId()), 0L, INTERVAL_EXP_MONITOR);
	}

	private void levelUp(int gap) {
		resetLevel();

		// ????????????????????????
		if (getLevel() == 99 && Config.ALT_REVIVAL_POTION) {
			try {
				L1Item l1item = ItemTable.getInstance().getTemplate(43000);
				if (l1item != null) {
					getInventory().storeItem(43000, 1);
					sendPackets(new S_ServerMessage(403, l1item.getName()));
				} else {
					sendPackets(new S_SystemMessage("??????????????????????????????????????????????????????"));
				}
			} catch (Exception e) {
				_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
				sendPackets(new S_SystemMessage("??????????????????????????????????????????????????????"));
			}
		}

		for (int i = 0; i < gap; i++) {
			short randomHp = CalcStat.calcStatHp(getType(), getBaseMaxHp(),
					getBaseCon(), getOriginalHpup());
			short randomMp = CalcStat.calcStatMp(getType(), getBaseMaxMp(),
					getBaseWis(), getOriginalMpup());
			addBaseMaxHp(randomHp);
			addBaseMaxMp(randomMp);
			//TODO ?????????????????????
			if (Config.MaxHPMP){
				setCurrentHp(getMaxHp());
				setCurrentMp(getMaxMp());
			}
			//end
		}
		resetBaseHitup();
		resetBaseDmgup();
		resetBaseAc();
		resetBaseMr();
		if (getLevel() > getHighLevel()) {
			setHighLevel(getLevel());
		}

		try {
			// DB??????????????????????????????????????????
			save();
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		// ???????????????????????????
		if (getLevel() >= 51 && getLevel() - 50 > getBonusStats()) {
			sendPackets(new S_bonusstats(getId(), 1));
		}
		sendPackets(new S_OwnCharStatus(this));
		l1j.william.Reward.getItem(this); //TODO?????????????????????  by ??????
		// ?????? 13?????????????????? By AMD ???case by hot183
		if (getLevel() >= 13) { 
			switch(getMapId()){
				case 69: case 86:
					L1Teleport.teleport(this, 33080, 33392, (short)4, (byte)5, true); //<-?????????????????????
				break; 
				
				case 68: case 85:
					L1Teleport.teleport(this, 32580, 32931, (short)0, (byte)5, true); //<-?????????????????????
				break;
			}
		}
		if (getLevel() >= 52) { // ???????????????
			if (getMapId() == 777) { // ?????????????????????????????????(????????????)
				L1Teleport.teleport(this, 34043, 32184, (short) 4, 5, true); // ???????????????
			} else if (getMapId() == 778
					|| getMapId() == 779) { // ?????????????????????????????????(???????????????)
				L1Teleport.teleport(this, 32608, 33178, (short) 4, 5, true); // WB
			}
		}
	}

	private void levelDown(int gap) {
		resetLevel();

		for (int i = 0; i > gap; i--) {
			// ?????????????????????????????????????????????????????????????????????????????????base??????0?????????
			short randomHp = CalcStat.calcStatHp(getType(), 0, getBaseCon(), getOriginalHpup());
			short randomMp = CalcStat.calcStatMp(getType(), 0, getBaseWis(), getOriginalMpup());
			addBaseMaxHp((short) -randomHp);
			addBaseMaxMp((short) -randomMp);
		}
		resetBaseHitup();
		resetBaseDmgup();
		resetBaseAc();
		resetBaseMr();
		if (Config.LEVEL_DOWN_RANGE != 0) {
			if (getHighLevel() - getLevel() >= Config.LEVEL_DOWN_RANGE) {
				sendPackets(new S_ServerMessage(64)); // ???????????????????????????????????????????????????
				sendPackets(new S_Disconnect());
				_log.info(String.format("?????????????????????????????????????????????????????????????????????",
						getName()));
			}
		}

		try {
			// DB??????????????????????????????????????????
			save();
		} catch (Exception e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		}
		sendPackets(new S_OwnCharStatus(this));
	}

	public void beginGameTimeCarrier() {
		new L1GameTimeCarrier(this).start();
	}

	private boolean _ghost = false; // ????????????

	public boolean isGhost() {
		return _ghost;
	}

	private void setGhost(boolean flag) {
		_ghost = flag;
	}

	private boolean _ghostCanTalk = true; // NPC???????????????????????????

	public boolean isGhostCanTalk() {
		return _ghostCanTalk;
	}

	private void setGhostCanTalk(boolean flag) {
		_ghostCanTalk = flag;
	}

	private boolean _isReserveGhost = false; // ????????????????????????

	public boolean isReserveGhost() {
		return _isReserveGhost;
	}

	private void setReserveGhost(boolean flag) {
		_isReserveGhost = flag;
	}

	public void beginGhost(int locx, int locy, short mapid, boolean canTalk) {
		beginGhost(locx, locy, mapid, canTalk, 0);
	}

	public void beginGhost(int locx, int locy, short mapid, boolean canTalk,
			int sec) {
		if (isGhost()) {
			return;
		}
		setGhost(true);
		_ghostSaveLocX = getX();
		_ghostSaveLocY = getY();
		_ghostSaveMapId = getMapId();
		_ghostSaveHeading = getHeading();
		setGhostCanTalk(canTalk);
		L1Teleport.teleport(this, locx, locy, mapid, 5, true);
		if (sec > 0) {
			_ghostFuture = GeneralThreadPool.getInstance().pcSchedule(
					new L1PcGhostMonitor(getId()), sec * 1000);
		}
	}

	public void makeReadyEndGhost() {
		setReserveGhost(true);
		L1Teleport.teleport(this, _ghostSaveLocX, _ghostSaveLocY,
				_ghostSaveMapId, _ghostSaveHeading, true);
	}

	public void endGhost() {
		setGhost(false);
		setGhostCanTalk(true);
		setReserveGhost(false);
	}

	private ScheduledFuture<?> _ghostFuture;

	private int _ghostSaveLocX = 0;
	private int _ghostSaveLocY = 0;
	private short _ghostSaveMapId = 0;
	private int _ghostSaveHeading = 0;

	private ScheduledFuture<?> _hellFuture;

	public void beginHell(boolean isFirst) {
		// ???????????????????????????????????????????????????
		if (getMapId() != 666) {
			int locx = 32701;
			int locy = 32777;
			short mapid = 666;
			L1Teleport.teleport(this, locx, locy, mapid, 5, false);
		}

		if (isFirst) {
			if (get_PKcount() <= 10) {
				setHellTime(300);
			} else {
				setHellTime(300 * (get_PKcount() - 10) + 300);
			}
			// ????????????PK?????????%0??????????????????????????????????????????????????????????????????%1?????????????????????????????????????????????
			sendPackets(new S_BlueMessage(552, String.valueOf(get_PKcount()),
					String.valueOf(getHellTime() / 60)));
		} else {
			// ????????????%0?????????????????????????????????????????????????????????
			sendPackets(new S_BlueMessage(637, String.valueOf(getHellTime())));
		}
		if (_hellFuture == null) {
			_hellFuture = GeneralThreadPool.getInstance()
					.pcScheduleAtFixedRate(new L1PcHellMonitor(getId()), 0L,
							1000L);
		}
	}

	public void endHell() {
		if (_hellFuture != null) {
			_hellFuture.cancel(false);
			_hellFuture = null;
		}
		// ?????????????????????????????????????????????????????????
		int[] loc = L1TownLocation
				.getGetBackLoc(L1TownLocation.TOWNID_ORCISH_FOREST);
		L1Teleport.teleport(this, loc[0], loc[1], (short) loc[2], 5, true);
		try {
			save();
		} catch (Exception ignore) {
			// ignore
		}
	}

	@Override
	public void setPoisonEffect(int effectId) {
		sendPackets(new S_Poison(getId(), effectId));

		if (!isGmInvis() && !isGhost() && !isInvisble()) {
			broadcastPacket(new S_Poison(getId(), effectId));
		}
	}

	@Override
	public void healHp(int pt) {
		super.healHp(pt);

		sendPackets(new S_HPUpdate(this));
	}

	@Override
	public int getKarma() {
		return _karma.get();
	}

	@Override
	public void setKarma(int i) {
		_karma.set(i);
	}

	public void addKarma(int i) {
		synchronized (_karma) {
			_karma.add(i);
		}
	}

	public int getKarmaLevel() {
		return _karma.getLevel();
	}

	public int getKarmaPercent() {
		return _karma.getPercent();
	}

	private Timestamp _lastPk;

	/**
	 * ????????????????????????PK??????????????????
	 * 
	 * @return _lastPk
	 * 
	 */
	public Timestamp getLastPk() {
		return _lastPk;
	}

	/**
	 * ????????????????????????PK????????????????????????
	 * 
	 * @param time
	 *            ??????PK?????????Timestamp?????? ?????????????????????null?????????
	 */
	public void setLastPk(Timestamp time) {
		_lastPk = time;
	}

	/**
	 * ????????????????????????PK??????????????????????????????????????????
	 */
	public void setLastPk() {
		_lastPk = new Timestamp(System.currentTimeMillis());
	}

	/**
	 * ???????????????????????????????????????????????????
	 * 
	 * @return ????????????????????????true
	 */
	public boolean isWanted() {
		if (_lastPk == null) {
			return false;
		} else if (System.currentTimeMillis() - _lastPk.getTime() > 24 * 3600 * 1000) {
			setLastPk(null);
			return false;
		}
		return true;
	}

	private Timestamp _deleteTime; // ???????????????????????????????????????

	public Timestamp getDeleteTime() {
		return _deleteTime;
	}

	public void setDeleteTime(Timestamp time) {
		_deleteTime = time;
	}

	@Override
	public int getMagicLevel() {
		return getClassFeature().getMagicLevel(getLevel());
	}

	private int _weightReduction = 0;

	public int getWeightReduction() {
		return _weightReduction;
	}

	public void addWeightReduction(int i) {
		_weightReduction += i;
	}

	private int _originalStrWeightReduction = 0; // ??? ???????????????STR ????????????

	public int getOriginalStrWeightReduction() {

		return _originalStrWeightReduction;
	}

	private int _originalConWeightReduction = 0; // ??? ???????????????CON ????????????

	public int getOriginalConWeightReduction() {

		return _originalConWeightReduction;
	}

	private int _hasteItemEquipped = 0;

	public int getHasteItemEquipped() {
		return _hasteItemEquipped;
	}

	public void addHasteItemEquipped(int i) {
		_hasteItemEquipped += i;
	}

	public void removeHasteSkillEffect() {
		if (hasSkillEffect(SLOW)) {
			removeSkillEffect(SLOW);
		}
		if (hasSkillEffect(MASS_SLOW)) {
			removeSkillEffect(MASS_SLOW);
		}
		if (hasSkillEffect(ENTANGLE)) {
			removeSkillEffect(ENTANGLE);
		}
		if (hasSkillEffect(HASTE)) {
			removeSkillEffect(HASTE);
		}
		if (hasSkillEffect(GREATER_HASTE)) {
			removeSkillEffect(GREATER_HASTE);
		}
		if (hasSkillEffect(STATUS_HASTE)) {
			removeSkillEffect(STATUS_HASTE);
		}
	}

	private int _damageReductionByArmor = 0; // ?????????????????????????????????

	public int getDamageReductionByArmor() {
		return _damageReductionByArmor;
	}

	public void addDamageReductionByArmor(int i) {
		_damageReductionByArmor += i;
	}

	private int _hitModifierByArmor = 0; // ??????????????????????????????

	public int getHitModifierByArmor() {
		return _hitModifierByArmor;
	}

	public void addHitModifierByArmor(int i) {
		_hitModifierByArmor += i;
	}

	private int _dmgModifierByArmor = 0; // ?????????????????????????????????

	public int getDmgModifierByArmor() {
		return _dmgModifierByArmor;
	}

	public void addDmgModifierByArmor(int i) {
		_dmgModifierByArmor += i;
	}

	private int _bowHitModifierByArmor = 0; // ????????????????????????????????????

	public int getBowHitModifierByArmor() {
		return _bowHitModifierByArmor;
	}

	public void addBowHitModifierByArmor(int i) {
		_bowHitModifierByArmor += i;
	}

	private int _bowDmgModifierByArmor = 0; // ???????????????????????????????????????

	public int getBowDmgModifierByArmor() {
		return _bowDmgModifierByArmor;
	}

	public void addBowDmgModifierByArmor(int i) {
		_bowDmgModifierByArmor += i;
	}

	private boolean _gresValid; // G-RES????????????

	private void setGresValid(boolean valid) {
		_gresValid = valid;
	}

	public boolean isGresValid() {
		return _gresValid;
	}

	private long _fishingTime = 0;

	public long getFishingTime() {
		return _fishingTime;
	}

	public void setFishingTime(long i) {
		_fishingTime = i;
	}

	private boolean _isFishing = false;

	public boolean isFishing() {
		return _isFishing;
	}

	public void setFishing(boolean flag) {
		_isFishing = flag;
	}

	private boolean _isFishingReady = false;

	public boolean isFishingReady() {
		return _isFishingReady;
	}

	public void setFishingReady(boolean flag) {
		_isFishingReady = flag;
	}

	private int _cookingId = 0;

	public int getCookingId() {
		return _cookingId;
	}

	public void setCookingId(int i) {
		_cookingId = i;
	}

	private int _dessertId = 0;

	public int getDessertId() {
		return _dessertId;
	}

	public void setDessertId(int i) {
		_dessertId = i;
	}

	/**
	 * LV?????????????????????????????????????????? LV???????????????????????????????????????????????????????????????
	 * 
	 * @return
	 */
	public void resetBaseDmgup() {
		int newBaseDmgup = 0;
		int newBaseBowDmgup = 0;
		if (isKnight() || isDarkelf() || isDragonKnight()) { // ??????????????????????????????????????????????????????
			newBaseDmgup = getLevel() / 10;
			newBaseBowDmgup = 0;
		} else if (isElf()) { // ?????????
			newBaseDmgup = 0;
			newBaseBowDmgup = getLevel() / 10;
		}
		addDmgup(newBaseDmgup - _baseDmgup);
		addBowDmgup(newBaseBowDmgup - _baseBowDmgup);
		_baseDmgup = newBaseDmgup;
		_baseBowDmgup = newBaseBowDmgup;
	}

	/**
	 * LV?????????????????????????????????????????? LV???????????????????????????????????????????????????????????????
	 * 
	 * @return
	 */
	public void resetBaseHitup() {
		int newBaseHitup = 0;
		int newBaseBowHitup = 0;
		if (isCrown()) { // ??????
			newBaseHitup = getLevel() / 5;
			newBaseBowHitup = getLevel() / 5;
		} else if (isKnight()) { // ?????????
			newBaseHitup = getLevel() / 3;
			newBaseBowHitup = getLevel() / 3;
		} else if (isElf()) { // ?????????
			newBaseHitup = getLevel() / 5;
			newBaseBowHitup = getLevel() / 5;
		} else if (isDarkelf()) { // ??????????????????
			newBaseHitup = getLevel() / 3;
			newBaseBowHitup = getLevel() / 3;
		} else if (isDragonKnight()) { // ?????????????????????
			newBaseHitup = getLevel() / 3;
			newBaseBowHitup = getLevel() / 3;
		} else if (isIllusionist()) { // ???????????????????????????
			newBaseHitup = getLevel() / 5;
			newBaseBowHitup = getLevel() / 5;
		}
		addHitup(newBaseHitup - _baseHitup);
		addBowHitup(newBaseBowHitup - _baseBowHitup);
		_baseHitup = newBaseHitup;
		_baseBowHitup = newBaseBowHitup;
	}

	/**
	 * ???????????????????????????????????????AC?????????????????????????????? ??????????????????LVUP,LVDown????????????????????????
	 */
	public void resetBaseAc() {
		int newAc = CalcStat.calcAc(getLevel(), getBaseDex());
		addAc(newAc - _baseAc);
		_baseAc = newAc;
	}

	/**
	 * ?????????????????????????????????????????????MR?????????????????????????????? ???????????????????????????????????????LVUP,LVDown??????????????????
	 */
	public void resetBaseMr() {
		int newMr = 0;
		if (isCrown()) { // ??????
			newMr = 10;
		} else if (isElf()) { // ?????????
			newMr = 25;
		} else if (isWizard()) { // ???????????????
			newMr = 15;
		} else if (isDarkelf()) { // ??????????????????
			newMr = 10;
		} else if (isDragonKnight()) { // ?????????????????????
			newMr = 18;
		} else if (isIllusionist()) { // ???????????????????????????
			newMr = 20;
		}
		newMr += CalcStat.calcStatMr(getWis()); // WIS??????MR????????????
		newMr += getLevel() / 2; // LV?????????????????????
		addMr(newMr - _baseMr);
		_baseMr = newMr;
	}

	/**
	 * EXP???????????????Lv?????????????????????????????? ??????????????????????????????LVUP??????????????????
	 */
	public void resetLevel() {
		setLevel(ExpTable.getLevelByExp(_exp));

		if (_hpRegen != null) {
			_hpRegen.updateLevel();
		}
	}

	/**
	 * ?????????????????????????????????????????????????????????????????????????????? ?????????????????????????????????????????????
	 */
	public void resetOriginalHpup() {
		int originalCon = getOriginalCon();
		if (isCrown()) {
			if(originalCon == 12 || originalCon == 13) {
				_originalHpup = 1;
			} else if(originalCon == 14 || originalCon == 15) {
				_originalHpup = 2;
			} else if(originalCon >= 16) {
				_originalHpup = 3;
			} else {
				_originalHpup = 0;
			}
		} else if(isKnight()) {
			if(originalCon == 15 || originalCon == 16) {
				_originalHpup = 1;
			} else if(originalCon >= 17) {
				_originalHpup = 3;
			} else {
				_originalHpup = 0;
			}
		} else if(isElf()) {
			if(originalCon >= 13 && originalCon <= 17) {
				_originalHpup = 1;
			} else if(originalCon == 18) {
				_originalHpup = 2;
			} else {
				_originalHpup = 0;
			}
		} else if(isDarkelf()) {
			if(originalCon == 10 || originalCon == 11) {
				_originalHpup = 1;
			} else if(originalCon >= 12) {
				_originalHpup = 2;
			} else {
				_originalHpup = 0;
			}
		} else if(isWizard()) {
			if(originalCon == 14 || originalCon == 15) {
				_originalHpup = 1;
			} else if(originalCon >= 16) {
				_originalHpup = 2;
			} else {
				_originalHpup = 0;
			}
		} else if(isDragonKnight()) {
			if(originalCon == 15 || originalCon == 16) {
				_originalHpup = 1;
			} else if(originalCon >= 17) {
				_originalHpup = 3;
			} else {
				_originalHpup = 0;
			}
		} else if(isIllusionist()) {
			if(originalCon == 13 || originalCon == 14) {
				_originalHpup = 1;
			} else if(originalCon >= 15) {
				_originalHpup = 2;
			} else {
				_originalHpup = 0;
			}
		}
	}

	public void resetOriginalMpup() {
		int originalWis = getOriginalWis(); {
			if (isCrown()) {
				if(originalWis >= 16) {
					_originalMpup = 1;
				} else {
					_originalMpup = 0;
				}
			} else if(isKnight()) {
					_originalMpup = 0;
			} else if(isElf()) {
				if(originalWis >= 14 && originalWis <= 16) {
					_originalMpup = 1;
				} else if(originalWis >= 17) {
					_originalMpup = 2;
				} else {
					_originalMpup = 0;
				}
			} else if(isDarkelf()) {
				if(originalWis >= 12) {
					_originalMpup = 1;
				} else {
					_originalMpup = 0;
				}
			} else if(isWizard()) {
				if(originalWis >= 13 && originalWis <= 16) {
					_originalMpup = 1;
				} else if(originalWis >= 17) {
					_originalMpup = 2;
				} else {
					_originalMpup = 0;
				}
			} else if(isDragonKnight()) {
				if(originalWis >= 13 && originalWis <= 15) {
					_originalMpup = 1;
				} else if(originalWis >= 16) {
					_originalMpup = 2;
				} else {
					_originalMpup = 0;
				}
			} else if(isIllusionist()) {
				if(originalWis >= 13 && originalWis <= 15) {
					_originalMpup = 1;
				} else if(originalWis >= 16) {
					_originalMpup = 2;
				} else {
					_originalMpup = 0;
				}
			}
		}
	}

	public void resetOriginalStrWeightReduction() {
		int originalStr = getOriginalStr();
		if (isCrown()) {
			if(originalStr >= 14 && originalStr <= 16) {
				_originalStrWeightReduction = 1;
			} else if(originalStr >= 17 && originalStr <= 19) {
				_originalStrWeightReduction = 2;
			} else if(originalStr == 20) {
				_originalStrWeightReduction = 3;
			} else {
				_originalStrWeightReduction = 0;
			}
		} else if(isKnight()) {
				_originalStrWeightReduction = 0;
		} else if(isElf()) {
			if(originalStr >= 16) {
				_originalStrWeightReduction = 2;
			} else {
				_originalStrWeightReduction = 0;
			}
		} else if(isDarkelf()) {
			if(originalStr >= 13 && originalStr <= 15) {
				_originalStrWeightReduction = 2;
			} else if(originalStr >= 16) {
				_originalStrWeightReduction = 3;
			} else {
				_originalStrWeightReduction = 0;
			}
		} else if(isWizard()) {
			if(originalStr >= 9) {
				_originalStrWeightReduction = 1;
			} else {
				_originalStrWeightReduction = 0;
			}
		} else if(isDragonKnight()) {
			if(originalStr >= 16) {
				_originalStrWeightReduction = 1;
			} else {
				_originalStrWeightReduction = 0;
			}
		} else if(isIllusionist()) {
			if(originalStr == 18) {
				_originalStrWeightReduction = 1;
			} else {
				_originalStrWeightReduction = 0;
			}
		}
	}

	public void resetOriginalDmgup() {
		int originalStr = getOriginalStr();
		if (isCrown()) {
			if(originalStr >= 15 && originalStr <= 17) {
				_originalDmgup = 1;
			} else if(originalStr >= 18) {
				_originalDmgup = 2;
			} else {
				_originalDmgup = 0;
			}
		} else if(isKnight()) {
			if(originalStr == 18 || originalStr == 19) {
				_originalDmgup = 2;
			} else if(originalStr == 20) {
				_originalDmgup = 4;
			} else {
				_originalDmgup = 0;
			}
		} else if(isElf()) {
			if(originalStr == 12 || originalStr == 13) {
				_originalDmgup = 1;
			} else if(originalStr >= 14) {
				_originalDmgup = 2;
			} else {
				_originalDmgup = 0;
			}
		} else if(isDarkelf()) {
			if(originalStr >= 14 && originalStr <= 17) {
				_originalDmgup = 1;
			} else if(originalStr == 18) {
				_originalDmgup = 2;
			} else {
				_originalDmgup = 0;
			}
		} else if(isWizard()) {
			if(originalStr == 10 || originalStr == 11) {
				_originalDmgup = 1;
			} else if(originalStr >= 12) {
				_originalDmgup = 2;
			} else {
				_originalDmgup = 0;
			}
		} else if(isDragonKnight()) {
			if(originalStr >= 15 && originalStr <= 17) {
				_originalDmgup = 1;
			} else if(originalStr >= 18) {
				_originalDmgup = 3;
			} else {
				_originalDmgup = 0;
			}
		} else if(isIllusionist()) {
			if(originalStr == 13 || originalStr == 14) {
				_originalDmgup = 1;
			} else if(originalStr >= 15) {
				_originalDmgup = 2;
			} else {
				_originalDmgup = 0;
			}
		}
	}

	public void resetOriginalConWeightReduction() {
		int originalCon = getOriginalCon();
		if (isCrown()) {
			if(originalCon >= 11) {
				_originalConWeightReduction = 1;
			} else {
				_originalConWeightReduction = 0;
			}
		} else if(isKnight()) {
			if(originalCon >= 15) {
				_originalConWeightReduction = 1;
			} else {
				_originalConWeightReduction = 0;
			}
		} else if(isElf()) {
			if(originalCon >= 15) {
				_originalConWeightReduction = 2;
			} else {
				_originalConWeightReduction = 0;
			}
		} else if(isDarkelf()) {
			if(originalCon >= 9) {
				_originalConWeightReduction = 1;
			} else {
				_originalConWeightReduction = 0;
			}
		} else if(isWizard()) {
			if(originalCon == 13 || originalCon == 14) {
				_originalConWeightReduction = 1;
			} else if(originalCon >= 15) {
				_originalConWeightReduction = 2;
			} else {
				_originalConWeightReduction = 0;
			}
		} else if(isDragonKnight()) {
				_originalConWeightReduction = 0;
		} else if(isIllusionist()) {
			if(originalCon == 17) {
				_originalConWeightReduction = 1;
			} else if(originalCon == 18) {
				_originalConWeightReduction = 2;
			} else {
				_originalConWeightReduction = 0;
			}
		}
	}

	public void resetOriginalBowDmgup() {
		int originalDex = getOriginalDex();
		if (isCrown()) {
			if(originalDex >= 13) {
				_originalBowDmgup = 1;
			} else {
				_originalBowDmgup = 0;
			}
		} else if(isKnight()) {
				_originalBowDmgup = 0;
		} else if(isElf()) {
			if(originalDex >= 14 && originalDex <= 16) {
				_originalBowDmgup = 2;
			} else if(originalDex >= 17) {
				_originalBowDmgup = 3;
			} else {
				_originalBowDmgup = 0;
			}
		} else if(isDarkelf()) {
			if(originalDex == 18) {
				_originalBowDmgup = 2;
			} else {
				_originalBowDmgup = 0;
			}
		} else if(isWizard()) {
				_originalBowDmgup = 0;
		} else if(isDragonKnight()) {
				_originalBowDmgup = 0;
		} else if(isIllusionist()) {
				_originalBowDmgup = 0;
		}
	}

	public void resetOriginalHitup() {
		int originalStr = getOriginalStr();
		if (isCrown()) {
			if(originalStr >= 16 && originalStr <= 18) {
				_originalHitup = 1;
			} else if(originalStr >= 19) {
				_originalHitup = 2;
			} else {
				_originalHitup = 0;
			}
		} else if(isKnight()) {
			if(originalStr == 17 || originalStr == 18) {
				_originalHitup = 2;
			} else if(originalStr >= 19) {
				_originalHitup = 4;
			} else {
				_originalHitup = 0;
			}
		} else if(isElf()) {
			if(originalStr == 13 || originalStr == 14) {
				_originalHitup = 1;
			} else if(originalStr >= 15) {
				_originalHitup = 2;
			} else {
				_originalHitup = 0;
			}
		} else if(isDarkelf()) {
			if(originalStr >= 15 && originalStr <= 17) {
				_originalHitup = 1;
			} else if(originalStr == 18) {
				_originalHitup = 2;
			} else {
				_originalHitup = 0;
			}
		} else if(isWizard()) {
			if(originalStr == 11 || originalStr == 12) {
				_originalHitup = 1;
			} else if(originalStr >= 13) {
				_originalHitup = 2;
			} else {
				_originalHitup = 0;
			}
		} else if(isDragonKnight()) {
			if(originalStr >= 14 && originalStr <= 16) {
				_originalHitup = 1;
			} else if(originalStr >= 17) {
				_originalHitup = 3;
			} else {
				_originalHitup = 0;
			}
		} else if(isIllusionist()) {
			if(originalStr == 12 || originalStr == 13) {
				_originalHitup = 1;
			} else if(originalStr == 14 || originalStr == 15) {
				_originalHitup = 2;
			} else if(originalStr == 16) {
				_originalHitup = 3;
			} else if(originalStr >= 17) {
				_originalHitup = 4;
			} else {
				_originalHitup = 0;
			}
		}
	}

	public void resetOriginalMr() {
		int originalWis = getOriginalWis();
		if (isCrown()) {
			if(originalWis == 12 || originalWis == 13) {
				_originalMr = 1;
			} else if(originalWis >= 14) {
				_originalMr = 2;
			} else {
				_originalMr = 0;
			}
		} else if(isKnight()) {
			if(originalWis == 10 || originalWis == 11) {
				_originalMr = 1;
			} else if(originalWis >= 12) {
				_originalMr = 2;
			} else {
				_originalMr = 0;
			}
		} else if(isElf()) {
			if(originalWis >= 13 && originalWis <= 15) {
				_originalMr = 1;
			} else if(originalWis >= 16) {
				_originalMr = 2;
			} else {
				_originalMr = 0;
			}
		} else if(isDarkelf()) {
			if(originalWis >= 11 && originalWis <= 13) {
				_originalMr = 1;
			} else if(originalWis == 14) {
				_originalMr = 2;
			} else if(originalWis == 15) {
				_originalMr = 3;
			} else if(originalWis >= 16) {
				_originalMr = 4;
			} else {
				_originalMr = 0;
			}
		} else if(isWizard()) {
			if(originalWis >= 15) {
				_originalMr = 1;
			} else {
				_originalMr = 0;
			}
		} else if(isDragonKnight()) {
			if(originalWis >= 14) {
				_originalMr = 2;
			} else {
				_originalMr = 0;
			}
		} else if(isIllusionist()) {
			if(originalWis >= 15 && originalWis <= 17) {
				_originalMr = 2;
			} else if(originalWis == 18) {
				_originalMr = 4;
			} else {
				_originalMr = 0;
			}
		}
		
	addMr(_originalMr);
	}

	public void resetOriginalMagicHit() {
		int originalInt = getOriginalInt();
		if (isCrown()) {
			if(originalInt == 12 || originalInt == 13) {
				_originalMagicHit = 1;
			} else if(originalInt >= 14) {
				_originalMagicHit = 2;
			} else {
				_originalMagicHit = 0;
			}
		} else if(isKnight()) {
			if(originalInt == 10 || originalInt == 11) {
				_originalMagicHit = 1;
			} else if(originalInt == 12) {
				_originalMagicHit = 2;
			} else {
				_originalMagicHit = 0;
			}
		} else if(isElf()) {
			if(originalInt == 13 || originalInt == 14) {
				_originalMagicHit = 1;
			} else if(originalInt >= 15) {
				_originalMagicHit = 2;
			} else {
				_originalMagicHit = 0;
			}
		} else if(isDarkelf()) {
			if(originalInt == 12 || originalInt == 13) {
				_originalMagicHit = 1;
			} else if(originalInt >= 14) {
				_originalMagicHit = 2;
			} else {
				_originalMagicHit = 0;
			}
		} else if(isWizard()) {
			if(originalInt >= 14) {
				_originalMagicHit = 1;
			} else {
				_originalMagicHit = 0;
			}
		} else if(isDragonKnight()) {
			if(originalInt == 12 || originalInt == 13) {
				_originalMagicHit = 2;
			} else if(originalInt == 14 || originalInt == 15) {
				_originalMagicHit = 3;
			} else if(originalInt >= 16) {
				_originalMagicHit = 4;
			} else {
				_originalMagicHit = 0;
			}
		} else if(isIllusionist()) {
			if(originalInt >= 13) {
				_originalMagicHit = 1;
			} else {
				_originalMagicHit = 0;
			}
		}
	}

	public void resetOriginalMagicCritical() {
		int originalInt = getOriginalInt();
		if (isCrown()) {
				_originalMagicCritical = 0;
		} else if(isKnight()) {
				_originalMagicCritical = 0;
		} else if(isElf()) {
			if(originalInt == 14 || originalInt == 15) {
				_originalMagicCritical = 2;
			} else if(originalInt >= 16) {
				_originalMagicCritical = 4;
			} else {
				_originalMagicCritical = 0;
			}
		} else if(isDarkelf()) {
				_originalMagicCritical = 0;
		} else if(isWizard()) {
			if(originalInt == 15) {
				_originalMagicCritical = 2;
			} else if(originalInt == 16) {
				_originalMagicCritical = 4;
			} else if(originalInt == 17) {
				_originalMagicCritical = 6;
			} else if(originalInt == 18) {
				_originalMagicCritical = 8;
			} else {
				_originalMagicCritical = 0;
			}
		} else if(isDragonKnight()) {
				_originalMagicCritical = 0;
		} else if(isIllusionist()) {
				_originalMagicCritical = 0;
		}
	}

	public void resetOriginalMagicConsumeReduction() {
		int originalInt = getOriginalInt();
		if (isCrown()) {
			if(originalInt == 11 || originalInt == 12) {
				_originalMagicConsumeReduction = 1;
			} else if(originalInt >= 13) {
				_originalMagicConsumeReduction = 2;
			} else {
				_originalMagicConsumeReduction = 0;
			}
		} else if(isKnight()) {
			if(originalInt == 9 || originalInt == 10) {
				_originalMagicConsumeReduction = 1;
			} else if(originalInt >= 11) {
				_originalMagicConsumeReduction = 2;
			} else {
				_originalMagicConsumeReduction = 0;
			}
		} else if(isElf()) {
				_originalMagicConsumeReduction = 0;
		} else if(isDarkelf()) {
			if(originalInt == 13 || originalInt == 14) {
				_originalMagicConsumeReduction = 1;
			} else if(originalInt >= 15) {
				_originalMagicConsumeReduction = 2;
			} else {
				_originalMagicConsumeReduction = 0;
			}
		} else if(isWizard()) {
				_originalMagicConsumeReduction = 0;
		} else if(isDragonKnight()) {
				_originalMagicConsumeReduction = 0;
		} else if(isIllusionist()) {
			if(originalInt == 14) {
				_originalMagicConsumeReduction = 1;
			} else if(originalInt >= 15) {
				_originalMagicConsumeReduction = 2;
			} else {
				_originalMagicConsumeReduction = 0;
			}
		}
	}

	public void resetOriginalMagicDamage() {
		int originalInt = getOriginalInt();
		if (isCrown()) {
				_originalMagicDamage = 0;
		} else if(isKnight()) {
				_originalMagicDamage = 0;
		} else if(isElf()) {
				_originalMagicDamage = 0;
		} else if(isDarkelf()) {
				_originalMagicDamage = 0;
		} else if(isWizard()) {
			if(originalInt >= 13) {
				_originalMagicDamage = 1;
			} else {
				_originalMagicDamage = 0;
			}
		} else if(isDragonKnight()) {
			if(originalInt == 13 || originalInt == 14) {
				_originalMagicDamage = 1;
			} else if(originalInt == 15 || originalInt == 16) {
				_originalMagicDamage = 2;
			} else if(originalInt == 17) {
				_originalMagicDamage = 3;
			} else {
				_originalMagicDamage = 0;
			}
		} else if(isIllusionist()) {
			if(originalInt == 16) {
				_originalMagicDamage = 1;
			} else if(originalInt == 17) {
				_originalMagicDamage = 2;
			} else {
				_originalMagicDamage = 0;
			}
		}
	}

	public void resetOriginalAc() {
		int originalDex = getOriginalDex();
		if (isCrown()) {
			if(originalDex >= 12 && originalDex <= 14) {
				_originalAc = 1;
			} else if(originalDex == 15 || originalDex == 16) {
				_originalAc = 2;
			} else if(originalDex >= 17) {
				_originalAc = 3;
			} else {
				_originalAc = 0;
			}
		} else if(isKnight()) {
			if(originalDex == 13 || originalDex == 14) {
				_originalAc = 1;
			} else if(originalDex >= 15) {
				_originalAc = 3;
			} else {
				_originalAc = 0;
			}
		} else if(isElf()) {
			if(originalDex >= 15 && originalDex <= 17) {
				_originalAc = 1;
			} else if(originalDex == 18) {
				_originalAc = 2;
			} else {
				_originalAc = 0;
			}
		} else if(isDarkelf()) {
			if(originalDex >= 17) {
				_originalAc = 1;
			} else {
				_originalAc = 0;
			}
		} else if(isWizard()) {
			if(originalDex == 8 || originalDex == 9) {
				_originalAc = 1;
			} else if(originalDex >= 10) {
				_originalAc = 2;
			} else {
				_originalAc = 0;
			}
		} else if(isDragonKnight()) {
			if(originalDex == 12 || originalDex == 13) {
				_originalAc = 1;
			} else if(originalDex >= 14) {
				_originalAc = 2;
			} else {
				_originalAc = 0;
			}
		} else if(isIllusionist()) {
			if(originalDex == 11 || originalDex == 12) {
				_originalAc = 1;
			} else if(originalDex >= 13) {
				_originalAc = 2;
			} else {
				_originalAc = 0;
			}
		}
		
		addAc(0 - _originalAc);
	}

	public void resetOriginalEr() {
		int originalDex = getOriginalDex();
		if (isCrown()) {
			if(originalDex == 14 || originalDex == 15) {
				_originalEr = 1;
			} else if(originalDex == 16 || originalDex == 17) {
				_originalEr = 2;
			} else if(originalDex == 18) {
				_originalEr = 3;
			} else {
				_originalEr = 0;
			}
		} else if(isKnight()) {
			if(originalDex == 14 || originalDex == 15) {
				_originalEr = 1;
			} else if(originalDex == 16) {
				_originalEr = 3;
			} else {
				_originalEr = 0;
			}
		} else if(isElf()) {
				_originalEr = 0;
		} else if(isDarkelf()) {
			if(originalDex >= 16) {
				_originalEr = 2;
			} else {
				_originalEr = 0;
			}
		} else if(isWizard()) {
			if(originalDex == 9 || originalDex == 10) {
				_originalEr = 1;
			} else if(originalDex == 11) {
				_originalEr = 2;
			} else {
				_originalEr = 0;
			}
		} else if(isDragonKnight()) {
			if(originalDex == 13 || originalDex == 14) {
				_originalEr = 1;
			} else if(originalDex >= 15) {
				_originalEr = 2;
			} else {
				_originalEr = 0;
			}
		} else if(isIllusionist()) {
			if(originalDex == 12 || originalDex == 13) {
				_originalEr = 1;
			} else if(originalDex >= 14) {
				_originalEr = 2;
			} else {
				_originalEr = 0;
			}
		}
	}

	public void resetOriginalHpr() {
		int originalCon = getOriginalCon();
		if (isCrown()) {
			if(originalCon == 13 || originalCon == 14) {
				_originalHpr = 1;
			} else if(originalCon == 15 || originalCon == 16) {
				_originalHpr = 2;
			} else if(originalCon == 17) {
				_originalHpr = 3;
			} else if(originalCon == 18) {
				_originalHpr = 4;
			} else {
				_originalHpr = 0;
			}
		} else if(isKnight()) {
			if(originalCon == 16 || originalCon == 17) {
				_originalHpr = 2;
			} else if(originalCon == 18) {
				_originalHpr = 4;
			} else {
				_originalHpr = 0;
			}
		} else if(isElf()) {
			if(originalCon == 14 || originalCon == 15) {
				_originalHpr = 1;
			} else if(originalCon == 16) {
				_originalHpr = 2;
			} else if(originalCon >= 17) {
				_originalHpr = 3;
			} else {
				_originalHpr = 0;
			}
		} else if(isDarkelf()) {
			if(originalCon == 11 || originalCon == 12) {
				_originalHpr = 1;
			} else if(originalCon >= 13) {
				_originalHpr = 2;
			} else {
				_originalHpr = 0;
			}
		} else if(isWizard()) {
			if(originalCon == 17) {
				_originalHpr = 1;
			} else if(originalCon == 18) {
				_originalHpr = 2;
			} else {
				_originalHpr = 0;
			}
		} else if(isDragonKnight()) {
			if(originalCon == 16 || originalCon == 17) {
				_originalHpr = 1;
			} else if(originalCon == 18) {
				_originalHpr = 3;
			} else {
				_originalHpr = 0;
			}
		} else if(isIllusionist()) {
			if(originalCon == 14 || originalCon == 15) {
				_originalHpr = 1;
			} else if(originalCon >= 16) {
				_originalHpr = 2;
			} else {
				_originalHpr = 0;
			}
		}
	}

	public void resetOriginalMpr() {
		int originalWis = getOriginalWis();
		if (isCrown()) {
			if(originalWis == 13 || originalWis == 14) {
				_originalMpr = 1;
			} else if(originalWis >= 15) {
				_originalMpr = 2;
			} else {
				_originalMpr = 0;
			}
		} else if(isKnight()) {
			if(originalWis == 11 || originalWis == 12) {
				_originalMpr = 1;
			} else if(originalWis == 13) {
				_originalMpr = 2;
			} else {
				_originalMpr = 0;
			}
		} else if(isElf()) {
			if(originalWis >= 15 && originalWis <= 17) {
				_originalMpr = 1;
			} else if(originalWis == 18) {
				_originalMpr = 2;
			} else {
				_originalMpr = 0;
			}
		} else if(isDarkelf()) {
			if(originalWis >= 13) {
				_originalMpr = 1;
			} else {
				_originalMpr = 0;
			}
		} else if(isWizard()) {
			if(originalWis == 14 || originalWis == 15) {
				_originalMpr = 1;
			} else if(originalWis == 16 || originalWis == 17) {
				_originalMpr = 2;
			} else if(originalWis == 18) {
				_originalMpr = 3;
			} else {
				_originalMpr = 0;
			}
		} else if(isDragonKnight()) {
			if(originalWis == 15 || originalWis == 16) {
				_originalMpr = 1;
			} else if(originalWis >= 17) {
				_originalMpr = 2;
			} else {
				_originalMpr = 0;
			}
		} else if(isIllusionist()) {
			if(originalWis >= 14 && originalWis <= 16) {
				_originalMpr = 1;
			} else if(originalWis >= 17) {
				_originalMpr = 2;
			} else {
				_originalMpr = 0;
			}
		}
	}

	public void refresh() {
		resetLevel();
		resetBaseHitup();
		resetBaseDmgup();
		resetBaseMr();
		resetBaseAc();
		resetOriginalHpup();
		resetOriginalMpup();
		resetOriginalDmgup();
		resetOriginalBowDmgup();
		resetOriginalHitup();
		resetOriginalMr();
		resetOriginalMagicHit();
		resetOriginalMagicCritical();
		resetOriginalMagicConsumeReduction();
		resetOriginalMagicDamage();
		resetOriginalAc();
		resetOriginalEr();
		resetOriginalHpr();
		resetOriginalMpr();
		resetOriginalStrWeightReduction();
		resetOriginalConWeightReduction();
	}

	private final L1ExcludingList _excludingList = new L1ExcludingList();

	public L1ExcludingList getExcludingList() {
		return _excludingList;
	}

	// -- ????????????????????? --
	private final AcceleratorChecker _acceleratorChecker = new AcceleratorChecker(
			this);

	public AcceleratorChecker getAcceleratorChecker() {
		return _acceleratorChecker;
	}

	/**
	 * ???????????????????????????
	 */
	private int _teleportX = 0;

	public int getTeleportX() {
		return _teleportX;
	}

	public void setTeleportX(int i) {
		_teleportX = i;
	}

	private int _teleportY = 0;

	public int getTeleportY() {
		return _teleportY;
	}

	public void setTeleportY(int i) {
		_teleportY = i;
	}

	private short _teleportMapId = 0;

	public short getTeleportMapId() {
		return _teleportMapId;
	}

	public void setTeleportMapId(short i) {
		_teleportMapId = i;
	}

	private int _teleportHeading = 0;

	public int getTeleportHeading() {
		return _teleportHeading;
	}

	public void setTeleportHeading(int i) {
		_teleportHeading = i;
	}

	private int _tempCharGfxAtDead;

	public int getTempCharGfxAtDead() {
		return _tempCharGfxAtDead;
	}

	public void setTempCharGfxAtDead(int i) {
		_tempCharGfxAtDead = i;
	}

	private boolean _isCanWhisper = true;

	public boolean isCanWhisper() {
		return _isCanWhisper;
	}

	public void setCanWhisper(boolean flag) {
		_isCanWhisper = flag;
	}

	private boolean _isShowTradeChat = true;

	public boolean isShowTradeChat() {
		return _isShowTradeChat;
	}

	public void setShowTradeChat(boolean flag) {
		_isShowTradeChat = flag;
	}

	private boolean _isShowWorldChat = true;

	public boolean isShowWorldChat() {
		return _isShowWorldChat;
	}

	public void setShowWorldChat(boolean flag) {
		_isShowWorldChat = flag;
	}

	private int _fightId;

	public int getFightId() {
		return _fightId;
	}

	public void setFightId(int i) {
		_fightId = i;
	}

	private byte _chatCount = 0;

	private long _oldChatTimeInMillis = 0L;

	public void checkChatInterval() {
		long nowChatTimeInMillis = System.currentTimeMillis();
		if (_chatCount == 0) {
			_chatCount++;
			_oldChatTimeInMillis = nowChatTimeInMillis;
			return;
		}

		long chatInterval = nowChatTimeInMillis - _oldChatTimeInMillis;
		if (chatInterval > 2000) {
			_chatCount = 0;
			_oldChatTimeInMillis = 0;
		} else {
			if (_chatCount >= 3) {
				setSkillEffect(STATUS_CHAT_PROHIBITED, 120 * 1000);
				sendPackets(new S_SkillIconGFX(36, 120));
				sendPackets(new S_ServerMessage(153)); // \f3???????????????????????????????????????????????????2??????????????????????????????????????????????????????
				_chatCount = 0;
				_oldChatTimeInMillis = 0;
			}
			_chatCount++;
		}
	}

	private int _callClanId;

	public int getCallClanId() {
		return _callClanId;
	}

	public void setCallClanId(int i) {
		_callClanId = i;
	}

	private int _callClanHeading;

	public int getCallClanHeading() {
		return _callClanHeading;
	}

	public void setCallClanHeading(int i) {
		_callClanHeading = i;
	}

	private boolean _isInCharReset = false;

	public boolean isInCharReset() {
		return _isInCharReset;
	}

	public void setInCharReset(boolean flag) {
		_isInCharReset = flag;
	}

	private int _tempLevel = 1;

	public int getTempLevel() {
		return _tempLevel;
	}

	public void setTempLevel(int i) {
		_tempLevel = i;
	}
	private int _tempMaxLevel = 1;

	public int getTempMaxLevel() {
		return _tempMaxLevel;
	}

	public void setTempMaxLevel(int i) {
		_tempMaxLevel = i;
	}

	private int _awakeSkillId = 0;

	public int getAwakeSkillId() {
		return _awakeSkillId;
	}

	public void setAwakeSkillId(int i) {
		_awakeSkillId = i;
	}
	
	//????????????????????????????????? by 0936690278
	private ArrayList<Integer> skillList = new ArrayList<Integer>();

	public void setSkillMastery(int skillid) {
		if (!skillList.contains(skillid)) {
			skillList.add(skillid);
		}
	}

	public void removeSkillMastery(int skillid) {
		if (skillList.contains(skillid)) {
			skillList.remove(skillid);
		}
	}
	
	public boolean isSkillMastery(int skillid) {
		return skillList.contains(skillid);
	}
	
	public void clearSkillMastery() {
		skillList.clear();
	}
	//~????????????????????????????????? by 0936690278
	//XXX ????????????
	private int _lap = 1;
	public void setLap(int i){
		_lap = i;
	}
	public int getLap(){
		return _lap;
	}
	
	private int _lapCheck = 0;
	public void setLapCheck(int i){
		_lapCheck = i;
	}
	public int getLapCheck(){
		return _lapCheck;
	}
	/**
	 ???????????????????????????????????????
	 */
	public int getLapScore(){
		return _lap*29 + _lapCheck;
	}
	//??????
	private boolean _order_list=false;
	public boolean isInOrderList(){
		return _order_list;
	}
	public void setInOrderList(boolean bool){
		_order_list=bool;
	}
	//~??????
	//END
	//////////////??????/////////////////////////
	private int _bank;

	public int getBank()
	{
		return _bank;
	}

	public void setBank(int i)
	{
		_bank = i;
   	 }
  	  ///////////////////////////////////////

	//????????????
	private boolean _thirdSpeed;
	public void setThirdSpeed(int i) {
		_thirdSpeed = (i!=0);
	}
	
	public boolean isThirdSpeed() {
		return _thirdSpeed;
	}
	//~????????????
	//??????C_NPCAction.java(??????????????? ????????????????????? ???????????????????????????) by seroidv
	//????????????????????????(???????????????????????????)  
	private boolean _isSummonMonster = false;  
	private boolean _isShapeChange = false;  
	 
	public void setSummonMonster(boolean SummonMonster) {  
	_isSummonMonster = SummonMonster;  
	}  
	 
	public boolean isSummonMonster() {  
	return _isSummonMonster;  
	}  
	 
	public void setShapeChange(boolean isShapeChange) {  
	_isShapeChange = isShapeChange;  
	}  
	 
	public boolean isShapeChange() {  
	return _isShapeChange;  
	}
	//~??????C_NPCAction.java(??????????????? ????????????????????? ???????????????????????????) by seroidv

}
