package l1j.server.server.model.item;

import static l1j.server.server.model.skill.L1SkillId.ABSOLUTE_BARRIER;
import static l1j.server.server.model.skill.L1SkillId.SHAPE_CHANGE;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.util.ArrayList;
import java.util.Random;

import l1j.server.Config;
import l1j.server.L1DatabaseFactory;
import l1j.server.server.ClientThread;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.datatables.LogEnchantTable;
import l1j.server.server.datatables.PolyTable;
import l1j.server.server.datatables.SkillsTable;
import l1j.server.server.model.Getback;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1HouseLocation;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.L1PcInventory;
import l1j.server.server.model.L1PolyMorph;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1TownLocation;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.model.Instance.L1TowerInstance;
import l1j.server.server.model.skill.L1SkillUse;
import l1j.server.server.serverpackets.S_IdentifyDesc;
import l1j.server.server.serverpackets.S_ItemStatus;
import l1j.server.server.serverpackets.S_Message_YN;
import l1j.server.server.serverpackets.S_OwnCharStatus;
import l1j.server.server.serverpackets.S_SPMR;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.storage.CharactersItemStorage;
import l1j.server.server.templates.L1Armor;
import l1j.server.server.templates.L1Item;
import l1j.server.server.templates.L1Skills;

public class L1EtcScroll {
	private static Random _random = new Random();
	private static ArrayList<Integer> _weaponScroll = new ArrayList<Integer>();
	private static ArrayList<Integer> _armorScroll = new ArrayList<Integer>();
	public L1EtcScroll(){
		_weaponScroll.add(40077);	//�隞�鈭箇����頠�
		_weaponScroll.add(40087);	//撠郎��瘜�頠�
		_weaponScroll.add(40128);	//撠��瘜�劂鞊∪頠�
		_weaponScroll.add(40130);	//����頠�
		_weaponScroll.add(40660);	//閰衣�頠�
		_weaponScroll.add(49999);	//100%撠郎��瘜�頠�
		_weaponScroll.add(50000);	//80%撠郎��瘜�頠�
		_weaponScroll.add(140087);	//撠郎��瘜�頠�(B)
		_weaponScroll.add(140130);	//����頠�(B)
		_weaponScroll.add(240087);	//撠郎��瘜�頠�(C)
		
		_armorScroll.add(40074);
		_armorScroll.add(40078);
		_armorScroll.add(40127);
		_armorScroll.add(40129);
		_armorScroll.add(140074);
		_armorScroll.add(140129);
		_armorScroll.add(240074);
	}
	public void use(L1PcInstance pc,ClientThread client,L1ItemInstance l1iteminstance1,L1ItemInstance l1iteminstance,int itemId, String s,int blanksc_skillid,int spellsc_objid,int spellsc_x,int spellsc_y,int resid,int l) throws Exception {
		if (_weaponScroll.contains(itemId)) { // 甇血撘瑕��
			if (l1iteminstance1 == null	|| l1iteminstance1.getItem().getType2() != 1) {
				pc.sendPackets(new S_ServerMessage(79)); // �隞颱��
				return;
			}
			int safe_enchant = l1iteminstance1.getItem().get_safeenchant();
			if (safe_enchant < 0) { // 撘瑕��
				pc.sendPackets(new S_ServerMessage(79)); 
				return;
			}
			int quest_weapon = l1iteminstance1.getItem().getItemId();
			if (quest_weapon >= 246 && quest_weapon <= 249) {
				if (itemId != 40660) {
					pc.sendPackets(new S_ServerMessage(79));
					return;
				}
			}else if (itemId == 40660){
				pc.sendPackets(new S_ServerMessage(79));
				return;
			}
			int weaponId = l1iteminstance1.getItem().getItemId();
			if (weaponId == 36 || weaponId == 183 || weaponId >= 250 && weaponId <= 255) {
				if (itemId != 40128) {
					pc.sendPackets(new S_ServerMessage(79));
					return;
				}
			}else if(itemId == 40128) {
				pc.sendPackets(new S_ServerMessage(79));
				return;
			}
			int enchant_level = l1iteminstance1.getEnchantLevel();

			if (itemId == 240087) {
				pc.getInventory().removeItem(l1iteminstance, 1);
				if (enchant_level < -6) {
					FailureEnchant(pc, l1iteminstance1, client);
				} else {
					SuccessEnchant(pc, l1iteminstance1, client, -1);
				}
			} else if (enchant_level < safe_enchant) {
				pc.getInventory().removeItem(l1iteminstance, 1);
				SuccessEnchant(pc, l1iteminstance1, client, RandomELevel(l1iteminstance1, itemId));
			} else {
				pc.getInventory().removeItem(l1iteminstance, 1);

				int rnd = _random.nextInt(100) + 1;
				int enchant_chance_wepon;
				int deviate = enchant_level - l1iteminstance1.getItem().get_safeenchant();
				if (deviate >= 3) {
					enchant_chance_wepon = (100 + 3 * Config.ENCHANT_CHANCE_WEAPON) / (deviate*deviate);
				} else {
					enchant_chance_wepon = (100 + 3 * Config.ENCHANT_CHANCE_WEAPON) / 3;
				}
				if (rnd < enchant_chance_wepon) {
					int randomEnchantLevel = RandomELevel(l1iteminstance1, itemId);
					SuccessEnchant(pc, l1iteminstance1, client, randomEnchantLevel);
				} else if (deviate >= 3 && rnd < (enchant_chance_wepon * 2)) {
					pc.sendPackets(new S_ServerMessage(160, l1iteminstance1.getLogName(), "$245", "$248"));
				} else {
					FailureEnchant(pc, l1iteminstance1, client);
				}
			}
		} else if (itemId == 300013){ // 摰��潭郎� by sdcom 2009.6.20
			if (l1iteminstance1 == null || l1iteminstance1.getItem().getType2() != 1) {
				pc.sendPackets(new S_ServerMessage(79));
				return;
			}
			int safe_enchant = l1iteminstance1.getItem().get_safeenchant();
			if (safe_enchant < 0) {
				pc.sendPackets(new S_ServerMessage(79)); 
				return;
			}
			int quest_weapon = l1iteminstance1.getItem().getItemId();
			if (quest_weapon >= 246 && quest_weapon <= 249) {
				pc.sendPackets(new S_ServerMessage(79));
				return;
			}
			int weaponId = l1iteminstance1.getItem().getItemId();
			if (weaponId == 36 || weaponId == 183 || weaponId >= 250 && weaponId <= 255) {
				pc.sendPackets(new S_ServerMessage(79)); 
				return;
			}
			int enchant_level = l1iteminstance1.getEnchantLevel();
			if(enchant_level < safe_enchant){
				int plus_num = safe_enchant - enchant_level;
				SuccessEnchant(pc, l1iteminstance1, client, plus_num);
				pc.getInventory().removeItem(l1iteminstance, 1);
			}else{
				pc.sendPackets(new S_ServerMessage(79));
				return;
			}
		} else if (itemId >= 49144 && itemId <= 49147) {
			useAttrEnchantScroll(pc, l1iteminstance, l1iteminstance1);
		} else if (itemId == 49148) {
			if(!pc.getInventory().checkItem(itemId,1)){
				return;
			}
			if (l1iteminstance1.getItem().getType2() != 2) {
				pc.sendPackets(new S_ServerMessage(79));
				return;
			} else if (l1iteminstance1 != null && (l1iteminstance1.getItem().getType() == 8 || l1iteminstance1.getItem().getType() == 9
					|| l1iteminstance1.getItem().getType() == 10 || l1iteminstance1.getItem().getType() == 12)) {
				int chance = _random.nextInt(100) + 1;
				if(l1iteminstance1.getEnchantLevel()>=-1 && l1iteminstance1.getEnchantLevel()<=9 && l1iteminstance1.getEnchantLevel() != 5){
					if(l1iteminstance1.getEnchantLevel() == -1){
						l1iteminstance1.setEnchantLevel(0);
					}
					if (chance < 25) {
						l1iteminstance1.setaddHp(l1iteminstance1.getaddHp() + 2);
						if (l1iteminstance1.isEquipped()) {
							pc.addMaxHp(2);
							pc.addAc(1);
						}
					} else if (chance > 25 && chance < 50) {
						l1iteminstance1.setaddMp(l1iteminstance1.getaddMp() + 1);
						if (l1iteminstance1.isEquipped()) {
							pc.addMaxMp(1);
							pc.addAc(1);
						}
					} else if (chance > 50 && chance < 75) {
						l1iteminstance1.setFireMr(l1iteminstance1.getFireMr() + 1);
						l1iteminstance1.setWaterMr(l1iteminstance1.getWaterMr() + 1);
						l1iteminstance1.setEarthMr(l1iteminstance1.getEarthMr() + 1);
						l1iteminstance1.setWindMr(l1iteminstance1.getWindMr() + 1);
						if (l1iteminstance1.isEquipped()) {
							pc.addFire(1);
							pc.addWater(1);
							pc.addEarth(1);
							pc.addWind(1);
							pc.addAc(1);
						}
					} else {
						FailureEnchant(pc, l1iteminstance1, client);
						pc.getInventory().removeItem(l1iteminstance, 1);
						return;
					}
				}else if(l1iteminstance1.getEnchantLevel()==5){
					if (chance < 25) {
						l1iteminstance1.setaddHp(l1iteminstance1.getaddHp() + 2);
						l1iteminstance1.setMpr(l1iteminstance1.getItem().get_addmpr() + 1);
						if (l1iteminstance1.isEquipped()) {
							pc.addMaxHp(2);
							pc.addAc(1);
						}
					} else if (chance > 25 && chance < 50) {
						l1iteminstance1.setaddMp(l1iteminstance1.getaddMp() + 1);
						l1iteminstance1.setaddSp(l1iteminstance1.getItem().get_addsp() + 1);
						if (l1iteminstance1.isEquipped()) {
							pc.addMaxMp(1);
							pc.addAc(1);
						}
					} else if (chance > 50 && chance < 75) {
						l1iteminstance1.setFireMr(l1iteminstance1.getFireMr() + 1);
						l1iteminstance1.setWaterMr(l1iteminstance1.getWaterMr() + 1);
						l1iteminstance1.setEarthMr(l1iteminstance1.getEarthMr() + 1);
						l1iteminstance1.setWindMr(l1iteminstance1.getWindMr() + 1);
						l1iteminstance1.setMpr(l1iteminstance1.getMpr() + 1);
						l1iteminstance1.setHpr(l1iteminstance1.getHpr() + 1);
						if (l1iteminstance1.isEquipped()) {
							pc.addFire(1);
							pc.addWater(1);
							pc.addEarth(1);
							pc.addWind(1);
							pc.addAc(1);
						}
					} else {
						FailureEnchant(pc, l1iteminstance1, client);
						pc.getInventory().removeItem(l1iteminstance, 1);
						return;
					}
				}else{
					pc.sendPackets(new S_ServerMessage(79));
					return;
				}
				
				SuccessEnchant(pc, l1iteminstance1, client, 1);
				pc.sendPackets(new S_ItemStatus(l1iteminstance1));// 蝺��
				CharactersItemStorage storage = CharactersItemStorage.create();// �摮�
				storage.updateFireMr(l1iteminstance1);
				storage.updateWaterMr(l1iteminstance1);
				storage.updateEarthMr(l1iteminstance1);
				storage.updateWindMr(l1iteminstance1);
				storage.updateaddSp(l1iteminstance1);
				storage.updateaddHp(l1iteminstance1);
				storage.updateaddMp(l1iteminstance1);
				storage.updateHpr(l1iteminstance1);
				storage.updateMpr(l1iteminstance1);
				pc.getInventory().removeItem(l1iteminstance, 1);// ����
			}
		}else if (itemId == 999983 || itemId == 999982) {
			if(!pc.getInventory().checkItem(itemId,1)){
				return;
			}
			if (l1iteminstance1.getItem().getType2() != 1) {
				pc.sendPackets(new S_ServerMessage(79));
				return;
			}
			if(itemId==999983 && l1iteminstance1.getgetHp()>=Config.DrainHP){//hp
				pc.sendPackets(new S_SystemMessage("�銵�銝��:"+Config.DrainHP));
			}else if(itemId == 999982 && l1iteminstance1.getgetMp()>=Config.DrainMP){//mp
				pc.sendPackets(new S_SystemMessage("�擳���:"+Config.DrainMP));
			}
			int chance = _random.nextInt(100);
			int hp=0,mp=0;
			if(chance>=Config.ENCHANT_CHANCE_GET_HPMP){//failure
				if(itemId==999983){
					FailureEnchantGetHpMp(pc, l1iteminstance1,1, client);
				}else{
					FailureEnchantGetHpMp(pc, l1iteminstance1,2, client);
				}
				pc.getInventory().removeItem(l1iteminstance, 1);
				pc.sendPackets(new S_ItemStatus(l1iteminstance1));// 蝺��
				return;
			}
			hp=_random.nextInt(Config.GET_HPMP_MAX_NUM)+1;
			mp=hp;
			CharactersItemStorage storage = CharactersItemStorage.create();// �摮�
			if(itemId==999983){//hp
				l1iteminstance1.setgetHp(l1iteminstance1.getgetHp()+hp);
				storage.updateGetHp(l1iteminstance1);
				pc.sendPackets(new S_SystemMessage("+"+l1iteminstance1.getEnchantLevel()+l1iteminstance1.getName()+"��銝��擳������撥��銵�擳��+"+hp+"!"));
			}else{//mp
				l1iteminstance1.setgetMp(l1iteminstance1.getgetMp()+mp);
				storage.updateGetMp(l1iteminstance1);
				pc.sendPackets(new S_SystemMessage("+"+l1iteminstance1.getEnchantLevel()+l1iteminstance1.getName()+"��銝��擳������撥��擳���+"+mp+"!"));
			}
			pc.sendPackets(new S_ItemStatus(l1iteminstance1));// 蝺��
			l1iteminstance1.setproctect(false);
			pc.getInventory().removeItem(l1iteminstance, 1);// ����
		} else if (itemId == 300039) {
			if (l1iteminstance1 != null){
				if (l1iteminstance1.getItem().get_safeenchant() <= -1){
					pc.sendPackets(new S_ServerMessage(1309));
					return;
				}
				if (l1iteminstance1.getproctect() == true){
					pc.sendPackets(new S_ServerMessage(1300));
					return;
				}
				if (l1iteminstance1.getItem().getType2() == 0){
					pc.sendPackets(new S_ServerMessage(79));
					return;
				} else {
					l1iteminstance1.setproctect(true);
					pc.sendPackets(new S_ServerMessage(1308, l1iteminstance1.getLogName()));
					pc.getInventory().removeItem(l1iteminstance, 1);
				}
			}
		} else if(itemId == 300014){ //摰��潮� by sdcom 2009.6.20
			if (l1iteminstance1 == null	|| l1iteminstance1.getItem().getType2() != 2) {
				pc.sendPackets(new S_ServerMessage(79));
				return;
			}
			int safe_enchant = ((L1Armor) l1iteminstance1.getItem()).get_safeenchant();
			if (safe_enchant < 0) {
				pc.sendPackets(new S_ServerMessage(79));
				return;
			}
			int armorId = l1iteminstance1.getItem().getItemId();
			if (armorId == 20161 || armorId >= 21035 && armorId <= 21038) {
				if (itemId != 40127) {
					pc.sendPackets(new S_ServerMessage(79));
					return;
				}
			}

			int enchant_level = l1iteminstance1.getEnchantLevel();
			if(enchant_level < safe_enchant){
				int plus_num = safe_enchant - enchant_level;
				SuccessEnchant(pc, l1iteminstance1, client, plus_num);
				pc.getInventory().removeItem(l1iteminstance, 1);
			}else{
				pc.sendPackets(new S_ServerMessage(79)); 
				return;
			}
		} else if (_armorScroll.contains(itemId)) {
			if (l1iteminstance1 == null	|| l1iteminstance1.getItem().getType2() != 2) {
				pc.sendPackets(new S_ServerMessage(79));
				return;
			}
			int safe_enchant = ((L1Armor) l1iteminstance1.getItem()).get_safeenchant();
			if (safe_enchant < 0) {
				pc.sendPackets(new S_ServerMessage(79)); 
				return;
			}
			int armorId = l1iteminstance1.getItem().getItemId();
			if (armorId == 20161 || armorId >= 21035 && armorId <= 21038) {
				if (itemId != 40127) {
					pc.sendPackets(new S_ServerMessage(79));
					return;
				}
			}else if(itemId == 40127){
				pc.sendPackets(new S_ServerMessage(79));
				return;
			}
			int enchant_level = l1iteminstance1.getEnchantLevel();
			if (itemId == 240074) {
				pc.getInventory().removeItem(l1iteminstance, 1);
				if (enchant_level < -6) {
					FailureEnchant(pc, l1iteminstance1, client);
				} else {
					SuccessEnchant(pc, l1iteminstance1, client, -1);
				}
			} else if (enchant_level < safe_enchant) {
				pc.getInventory().removeItem(l1iteminstance, 1);
				SuccessEnchant(pc, l1iteminstance1, client, RandomELevel(l1iteminstance1, itemId));
			} else {
				pc.getInventory().removeItem(l1iteminstance, 1);
				int rnd = _random.nextInt(100) + 1;
				int enchant_chance_armor;
				int enchant_level_tmp;
				if (safe_enchant == 0) {
					enchant_level_tmp = enchant_level + 2;
				} else {
					enchant_level_tmp = enchant_level;
				}
				if (enchant_level >= 9) {
					enchant_chance_armor = (100 + enchant_level_tmp	* Config.ENCHANT_CHANCE_ARMOR) / (enchant_level_tmp * 2);
				} else {
					enchant_chance_armor = (100 + enchant_level_tmp	* Config.ENCHANT_CHANCE_ARMOR) / enchant_level_tmp;
				}

				if (rnd < enchant_chance_armor) {
					int randomEnchantLevel = RandomELevel(l1iteminstance1, itemId);
					SuccessEnchant(pc, l1iteminstance1, client, randomEnchantLevel);
				} else if (enchant_level >= 9 && rnd < (enchant_chance_armor * 2)) {
					String item_name_id = l1iteminstance1.getName();
					String pm = "";
					String msg = "";
					if (enchant_level > 0) {
						pm = "+";
					}
					msg = (new StringBuilder()).append(pm + enchant_level).append(" ").append(item_name_id).toString();
					pc.sendPackets(new S_ServerMessage(160, msg, "$252", "$248"));
				} else {
					FailureEnchant(pc, l1iteminstance1, client);
				}
			}
		} else if (itemId == 300008) { //閫��暺頠詻��by xpatax
			Connection connection = null;
			connection = L1DatabaseFactory.getInstance().getConnection();
			PreparedStatement preparedstatement = connection.prepareStatement("UPDATE characters SET LocX=33442,LocY=32797,MapID=4 WHERE account_name=?");
			preparedstatement.setString(1, client.getAccountName());
			preparedstatement.execute();
			preparedstatement.close();
			connection.close();
			pc.sendPackets(new S_SystemMessage("�鈭��隞�撌脣�憟痔����"));
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40088 || itemId == 40096 || itemId == 140088) { // 蟡�����澈�����
			if (usePolyScroll(pc, itemId, s)) {
				pc.getInventory().removeItem(l1iteminstance, 1);
			} else {
				pc.sendPackets(new S_ServerMessage(181)); 
			}
		} else if (itemId == 40097 || itemId == 40119 || itemId == 140119 || itemId == 140329) {
			for (L1ItemInstance eachItem : pc.getInventory().getItems()) {
				if (eachItem.getItem().getBless() != 2 && eachItem.getItem().getBless() != 130) {
					continue;
				}
				if (!eachItem.isEquipped() && (itemId == 40119 || itemId == 40097)) {
					continue;
				}
				int id_normal = eachItem.getItemId() - 200000;
				L1Item template = ItemTable.getInstance().getTemplate(id_normal);
				if (template == null) {
					continue;
				}
				if (pc.getInventory().checkItem(id_normal) && template.isStackable()) {
					pc.getInventory().storeItem(id_normal, eachItem.getCount());
					pc.getInventory().removeItem(eachItem, eachItem.getCount());
				} else {
					eachItem.setItem(template);
					pc.getInventory().updateItem(eachItem,L1PcInventory.COL_ITEMID);
					pc.getInventory().saveItem(eachItem,L1PcInventory.COL_ITEMID);
				}
			}
			pc.getInventory().removeItem(l1iteminstance, 1);
			pc.sendPackets(new S_ServerMessage(155));
		} else if (itemId == 40126 || itemId == 40098) { // 蝣箄�����
			if (!l1iteminstance1.isIdentified()) {
				l1iteminstance1.setIdentified(true);
				pc.getInventory().updateItem(l1iteminstance1, L1PcInventory.COL_IS_ID);
			}
			pc.sendPackets(new S_IdentifyDesc(l1iteminstance1));
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40090 || itemId == 40091 || itemId == 40092 || itemId == 40093 || itemId == 40094) {
			if (pc.isWizard()) {
				if (itemId == 40090 && blanksc_skillid <= 7 || itemId == 40091 && blanksc_skillid <= 15 || itemId == 40092 && blanksc_skillid <= 22 || // ����
						itemId == 40093 && blanksc_skillid <= 31 || itemId == 40094 && blanksc_skillid <= 39) { 
					L1ItemInstance spellsc = ItemTable.getInstance().createItem(40859 + blanksc_skillid);
					if (spellsc != null) {
						if (pc.getInventory().checkAddItem(spellsc, 1) == L1Inventory.OK) {
							L1Skills l1skills = SkillsTable.getInstance().getTemplate(blanksc_skillid + 1); // blanksc_skillid�0憪���
							if (pc.getCurrentHp() + 1 < l1skills.getHpConsume() + 1) {
								pc.sendPackets(new S_ServerMessage(279)); 
								return;
							}
							if (pc.getCurrentMp() < l1skills.getMpConsume()) {
								pc.sendPackets(new S_ServerMessage(278)); 
								return;
							}
							if (l1skills.getItemConsumeId() != 0) {
								if (!pc.getInventory().checkItem(l1skills.getItemConsumeId(),l1skills.getItemConsumeCount())) { 
									pc.sendPackets(new S_ServerMessage(299)); 
									return;
								}
							}
							pc.setCurrentHp(pc.getCurrentHp() - l1skills.getHpConsume());
							pc.setCurrentMp(pc.getCurrentMp() - l1skills.getMpConsume());
							int lawful = pc.getLawful() + l1skills.getLawful();
							if (lawful > 32767) {
								lawful = 32767;
							}
							if (lawful < -32767) {
								lawful = -32767;
							}
							pc.setLawful(lawful);
							if (l1skills.getItemConsumeId() != 0) { // �������
								pc.getInventory().consumeItem(l1skills.getItemConsumeId(),l1skills.getItemConsumeCount());
							}
							pc.getInventory().removeItem(l1iteminstance, 1);
							pc.getInventory().storeItem(spellsc);
						}
					}
				} else {
					pc.sendPackets(new S_ServerMessage(591)); // \f1���������撘瑯�������������撘晞�����
				}
			} else {
				pc.sendPackets(new S_ServerMessage(264)); // \f1����������������雿輻��������
			}
		} else if ((itemId >= 40859 && itemId <= 40898) && itemId != 40863) { 
			if (spellsc_objid == pc.getId() && l1iteminstance.getItem().getUseType() != 30) { 
				pc.sendPackets(new S_ServerMessage(281)); 
				return;
			}
			pc.getInventory().removeItem(l1iteminstance, 1);
			if (spellsc_objid == 0 && l1iteminstance.getItem().getUseType() != 0 && l1iteminstance.getItem().getUseType() != 26	&& l1iteminstance.getItem().getUseType() != 27) {
				return;
			}
			cancelAbsoluteBarrier(pc); 
			int skillid = itemId - 40858;
			L1SkillUse l1skilluse = new L1SkillUse();
			l1skilluse.handleCommands(client.getActiveChar(), skillid, spellsc_objid, spellsc_x, spellsc_y, null, 0, L1SkillUse.TYPE_SPELLSC);
		} else if (itemId == 40089 || itemId == 140089) { // 敺拇暑�����������儔瘣颯����
			L1Character resobject = (L1Character) L1World.getInstance().findObject(resid);
			if (resobject != null) {
				if (resobject instanceof L1PcInstance) {
					L1PcInstance target = (L1PcInstance) resobject;
					if (pc.getId() == target.getId()) {
						return;
					}
					if (L1World.getInstance().getVisiblePlayer(target,0).size() > 0) {
						for (L1PcInstance visiblePc : L1World.getInstance().getVisiblePlayer(target,0)) {
							if (!visiblePc.isDead()) {
								pc.sendPackets(new S_ServerMessage(592));
								return;
							}
						}
					}
					if (target.getCurrentHp() == 0 && target.isDead() == true) {
						if (pc.getMap().isUseResurrection()) {
							target.setTempID(pc.getId());
							if (itemId == 40089) {
								target.sendPackets(new S_Message_YN(321, ""));
							} else if (itemId == 140089) {
								target.sendPackets(new S_Message_YN(322, ""));
							}
						} else {
							return;
						}
					}
				} else if (resobject instanceof L1NpcInstance) {
					if (!(resobject instanceof L1TowerInstance)) {
						L1NpcInstance npc = (L1NpcInstance) resobject;
						if (npc.getNpcTemplate().isCantResurrect()) {
							pc.getInventory().removeItem(l1iteminstance,1);
							return;
						}
						if (npc instanceof L1PetInstance && L1World.getInstance().getVisiblePlayer(npc, 0).size() > 0) {
							for (L1PcInstance visiblePc : L1World.getInstance().getVisiblePlayer(npc, 0)) {
								if (!visiblePc.isDead()) {
									pc.sendPackets(new S_ServerMessage(592));
									return;
								}
							}
						}
						if (npc.getCurrentHp() == 0 && npc.isDead()) {
							npc.resurrect(npc.getMaxHp() / 4);
							npc.setResurrect(true);
						}
					}
				}
			}
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 240100) { // ��������������(�����������)
			L1Teleport.teleport(pc, pc.getX(), pc.getY(), pc.getMapId(), pc.getHeading(), true);
			pc.getInventory().removeItem(l1iteminstance, 1);
			cancelAbsoluteBarrier(pc); 
		} else if (itemId == 41426) { // 撠�����
			L1ItemInstance lockItem = pc.getInventory().getItem(l);
			if (lockItem != null && lockItem.getItem().getType2() == 1 || lockItem.getItem().getType2() == 2) {
				if (lockItem.getBless() == 0 || lockItem.getBless() == 1 || lockItem.getBless() == 2 || lockItem.getBless() == 3) {
					int bless = 1;
					switch (lockItem.getBless()) {
					case 0:
						bless = 128;
						break;
					case 1:
						bless = 129;
						break;
					case 2:
						bless = 130;
						break;
					case 3:
						bless = 131;
						break;
					}
					lockItem.setBless(bless);
					pc.getInventory().updateItem(lockItem,L1PcInventory.COL_BLESS);
					pc.getInventory().saveItem(lockItem,L1PcInventory.COL_BLESS);
					pc.getInventory().removeItem(l1iteminstance, 1);
				} else {
					pc.sendPackets(new S_ServerMessage(79)); // \f1雿�絲����������
				}
			} else {
				pc.sendPackets(new S_ServerMessage(79)); // \f1雿�絲����������
			}
		} else if (itemId == 41427) { // 撠閫�������
			L1ItemInstance lockItem = pc.getInventory().getItem(l);
			if (lockItem != null && lockItem.getItem().getType2() == 1 || lockItem.getItem().getType2() == 2) {
				if (lockItem.getBless() == 128 || lockItem.getBless() == 129 || lockItem.getBless() == 130 || lockItem.getBless() == 131) {
					int bless = 1;
					switch (lockItem.getBless()) {
					case 128:
						bless = 0;
						break;
					case 129:
						bless = 1;
						break;
					case 130:
						bless = 2;
						break;
					case 131:
						bless = 3;
						break;
					}
					lockItem.setBless(bless);
					pc.getInventory().updateItem(lockItem,L1PcInventory.COL_BLESS);
					pc.getInventory().saveItem(lockItem,L1PcInventory.COL_BLESS);
					pc.getInventory().removeItem(l1iteminstance, 1);
				} else {
					pc.sendPackets(new S_ServerMessage(79)); 
				}
			} else {
				pc.sendPackets(new S_ServerMessage(79)); 
			}
		}
	}
	private void cancelAbsoluteBarrier(L1PcInstance pc) { // �������� ����閫��
		if (pc.hasSkillEffect(ABSOLUTE_BARRIER)) {
			pc.killSkillEffectTimer(ABSOLUTE_BARRIER);
			pc.startHpRegeneration();
			pc.startMpRegeneration();
			pc.startMpRegenerationByDoll();
			pc.startHpRegenerationByDoll();//add 擳�������
		}
	}
	private boolean usePolyScroll(L1PcInstance pc, int item_id, String s) {
		int time = 0;
		if (item_id == 40088 || item_id == 40096) { // 憭澈������情��憛憭澈�����
			time = 1800;
		} else if (item_id == 140088) { // 蟡�����澈�����
			time = 2100;
		}

		L1PolyMorph poly = PolyTable.getInstance().getTemplate(s);
		if (poly != null || s.equals("")) {
			if (s.equals("")) {
				if (pc.getTempCharGfx() == 6034
						|| pc.getTempCharGfx() == 6035) {
					return true;
				} else {
					pc.removeSkillEffect(SHAPE_CHANGE);
					return true;
				}
			} else if (poly.getMinLevel() <= pc.getLevel() || pc.isGm()) {
				L1PolyMorph.doPoly(pc, poly.getPolyId(), time,
						L1PolyMorph.MORPH_BY_ITEMMAGIC);
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
	
	//撅祆�批撥��頠�
	private void useAttrEnchantScroll(L1PcInstance pc,
			L1ItemInstance item, L1ItemInstance item1) {
		int itemId = item.getItem().getItemId();
		int rnd = _random.nextInt(100) + 1;
		if (item1 == null || item1.getItem().getType2() != 1) {
		    pc.sendPackets(new S_ServerMessage(79)); // \f1雿�絲����������
		    return;
		}
		int weapon_attr = item1.getAttribute();
		int weapon_attr_lvl = item1.getAttributeLevel();
		int attr = 0;
		if(weapon_attr_lvl >= 3){
		    pc.sendPackets(new S_ServerMessage(79)); // \f1瘝�遙雿������
		    return;
		}
		switch(itemId){
		case 49145: // �
		    attr = 1;
		    if(weapon_attr != 1 && weapon_attr != 0){
		        pc.sendPackets(new S_ServerMessage(79)); // \f1瘝�遙雿������
		        return;
		    }
		break;
		case 49147: // �
		    attr = 2;
		    if(weapon_attr != 2 && weapon_attr != 0){
		        pc.sendPackets(new S_ServerMessage(79)); // \f1瘝�遙雿������
		        return;
		    }
		break;
		case 49146: // 瘞�
		    attr = 4;
		    if(weapon_attr != 4 && weapon_attr != 0){
		        pc.sendPackets(new S_ServerMessage(79)); // \f1瘝�遙雿������
		        return;
		    }
		break;
		case 49144: // 憸�
		    attr = 8;
		    if(weapon_attr != 8 && weapon_attr != 0){
		        pc.sendPackets(new S_ServerMessage(79)); // \f1瘝�遙雿������
		        return;
		    }
		break;
		}
		short rtRate = Config.ENCHANT_CHANCE_ATTR;
		int chance;
		if (weapon_attr_lvl > 1) {
		    chance = (100 + 3 * rtRate) / 6;
		} else {
		    chance = (100 + 3 * rtRate) / 3;
		}
		if (rnd < chance) {
		    successAttr(pc, item1, attr);
		} else {
		    // \f1%0���%2�撘瑞�%1��������兢��鈭��������
		    pc.sendPackets(new S_ServerMessage(160, item1.getLogName(), "$245", "$248"));
		    failureAttr(pc, item1);
		}
		pc.getInventory().removeItem(item, 1);
		
	}
	
	private void successAttr(L1PcInstance pc, L1ItemInstance item, int attr) {
	     String attr_name = "";
	     String first = "";
	     String next = "";
	     String last = "";
	     String item_name = item.getName();
	     String lvl = "";
	     if (item.getEnchantLevel() > 0) {
	         lvl = "+";
	     }
	     if(item.getAttributeLevel() > 0){
	         attr_name = item.weaponAttrName() + " ";
	     }
	     /* $246:暺���
	      * $247:銝�����
	      * $248:����
	      * $252:������
	      */
	     if (!item.isIdentified() || item.getEnchantLevel() == 0) {
	         first = item_name;
	     } else {
	         first = (new StringBuilder())
	             .append(attr_name + lvl + item.getEnchantLevel())
	             .append(" ").append(item_name)
	             .toString(); // \f1%0���%2%1�������
	     }
	     next = "$252";
	     last = "$247 ";
	     // \f1%0%s %2 %1 �����
	     pc.sendPackets(new S_ServerMessage(161, first, next, last));
	     int newAttrLvl = item.getAttributeLevel() + 1;
	     item.setAttributeLevel(newAttrLvl);
	     item.setAttribute(attr);	     
	     pc.getInventory().updateItem(item, L1PcInventory.COL_ATTRIBUTE);
	     pc.getInventory().updateItem(item, L1PcInventory.COL_ATTRIBUTELVL);
	}
	
	private void failureAttr(L1PcInstance pc, L1ItemInstance item) {
		item.setAttributeLevel(0);
		item.setAttribute(0);
		pc.getInventory().updateItem(item, L1PcInventory.COL_ATTRIBUTE);
		pc.getInventory().updateItem(item, L1PcInventory.COL_ATTRIBUTELVL);
	}
	
	private int RandomELevel(L1ItemInstance item, int itemId) {
		if (itemId == L1ItemId.B_SCROLL_OF_ENCHANT_ARMOR
				|| itemId == L1ItemId.B_SCROLL_OF_ENCHANT_WEAPON
				|| itemId == 140129 || itemId == 140130) {
			if (item.getEnchantLevel() <= 2) {
				int j = _random.nextInt(100) + 1;
				if (j < 32) {
					return 1;
				} else if (j >= 33 && j <= 76) {
					return 2;
				} else if (j >= 77 && j <= 100) {
					return 3;
				}
			} else if (item.getEnchantLevel() >= 3
					&& item.getEnchantLevel() <= 5) {
				int j = _random.nextInt(100) + 1;
				if (j < 50) {
					return 2;
				} else {
					return 1;
				}
			}
			{
				return 1;
			}
		}
		return 1;
	}
	
	private void SuccessEnchant(L1PcInstance pc, L1ItemInstance item, ClientThread client, int i) {
		//鋆��風�頠� by missu0524
		item.setproctect(false);
		
		String s = "";
		String sa = "";
		String sb = "";
		String s1 = item.getName();
		String pm = "";
		if (item.getEnchantLevel() > 0) {
			pm = "+";
		}
		if (item.getItem().getType2() == 1) {
			//code arranged by sdcom 2010.3.4
			s = (!item.isIdentified() || item.getEnchantLevel() == 0)?s1:(new StringBuilder()).append(pm + item.getEnchantLevel()).append(" ").append(s1).toString();// \f1%0���%2%1�������
			switch (i) {
			case -1:
				sa = "$246";
				sb = "$247";
				break;

			case 1: // '\001'
				sa = "$245";
				sb = "$247";
				break;

			case 2: // '\002'
				sa = "$245";
				sb = "$248";
				break;

			case 3: // '\003'
				sa = "$245";
				sb = "$248";
				break;
			default :	// add by sdcom 2009.6.21
				sa = "$245";
				sb = "$248";
				break;
			}
		} else if (item.getItem().getType2() == 2) {
			s = (!item.isIdentified() || item.getEnchantLevel() == 0)?s1:(new StringBuilder()).append(pm + item.getEnchantLevel()).append(" ").append(s1).toString();// \f1%0���%2%1�������
			switch (i) {
			case -1:
				sa = "$246";
				sb = "$247";
				break;

			case 1: // '\001'
				sa = "$252";
				sb = "$247 ";
				break;

			case 2: // '\002'
				sa = "$252";
				sb = "$248 ";
				break;

			case 3: // '\003'
				sa = "$252";
				sb = "$248 ";
				break;
			default :
				sa = "$252";
				sb = "$248";
				break;
			}
		}
		if (Config.SuccessBoard){
			if (item.getItem().getType2() == 1 && item.getEnchantLevel() >= item.getItem().get_safeenchant() + Config.WeaponOverSafeBoard ) {
				L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(166,"\\f=*��" + pc.getName() + "����+" + item.getEnchantLevel() + " " + item.getName() + "�撥����� *"));
			} else if (item.getItem().getType2() == 2 && item.getEnchantLevel() >= item.getItem().get_safeenchant() + Config.ArmorOverSafeBoard ) {
				L1World.getInstance().broadcastPacketToAll(new S_ServerMessage(166,"\\f=*��" + pc.getName() + "����+" + item.getEnchantLevel() + " " + item.getName() + "�撥����� *"));
			}
		}
		pc.sendPackets(new S_ServerMessage(161, s, sa, sb));
		int oldEnchantLvl = item.getEnchantLevel();
		int newEnchantLvl = item.getEnchantLevel() + i;
		int safe_enchant = item.getItem().get_safeenchant();
		item.setEnchantLevel(newEnchantLvl);
		client.getActiveChar().getInventory().updateItem(item, L1PcInventory.COL_ENCHANTLVL);
		if (newEnchantLvl > safe_enchant) {
			client.getActiveChar().getInventory().saveItem(item, L1PcInventory.COL_ENCHANTLVL);
		}
		if (item.getItem().getType2() == 1 && Config.LOGGING_WEAPON_ENCHANT != 0) {
			if (safe_enchant == 0 || newEnchantLvl >= Config.LOGGING_WEAPON_ENCHANT) {
				LogEnchantTable logenchant = new LogEnchantTable();
				logenchant.storeLogEnchant(pc.getId(), item.getId(), oldEnchantLvl, newEnchantLvl);
			}
		}
		if (item.getItem().getType2() == 2 && Config.LOGGING_ARMOR_ENCHANT != 0) {
			if (safe_enchant == 0 || newEnchantLvl >= Config.LOGGING_ARMOR_ENCHANT) {
				LogEnchantTable logenchant = new LogEnchantTable();
				logenchant.storeLogEnchant(pc.getId(), item.getId(), oldEnchantLvl, newEnchantLvl);
			}
		}

		if (item.getItem().getType2() == 2) {
			if (item.isEquipped()) {
				pc.addAc(-i);
				int i2 = item.getItem().getItemId();
				//�������
				if (i2 == 20011 || i2 == 20110 || i2 == 21108 || i2 == 120011) { 
					pc.addMr(i);
					pc.sendPackets(new S_SPMR(pc));
				}
				if (i2 == 20056 || i2 == 120056 || i2 == 220056) { 
					pc.addMr(i * 2);
					pc.sendPackets(new S_SPMR(pc));
				}
			}
			pc.sendPackets(new S_OwnCharStatus(pc));
		}
	}
	
	//甇血�銵��擳��頠� by eric1300460
	private void FailureEnchantGetHpMp(L1PcInstance pc, L1ItemInstance item,int HpOrMp,	ClientThread client) {
		
		//鋆��風�頠� by missu0524
		if (item.getproctect() == true){
			try {
				CharactersItemStorage storage = CharactersItemStorage
				.create();// �摮�
				if(HpOrMp==1){
					item.setgetHp(0);
					storage.updateGetHp(item);
				}else{
					item.setgetMp(0);
					storage.updateGetMp(item);
				}
			} catch (Exception e) {
				e.printStackTrace();
			}
			item.setproctect(false);
			pc.sendPackets(new S_ServerMessage(1310));
			return;
		}
		//~鋆��風�頠�  by missu0524
		
		String s = "";
		String sa = "";
		int itemType = item.getItem().getType2();
		String nameId = item.getName();
		String pm = "";
		if (itemType == 1) { // 甇血
			if (!item.isIdentified() || item.getEnchantLevel() == 0) {
				s = nameId; // \f1%0��撥��%1������������������
				sa = "$245";
			} else {
				if (item.getEnchantLevel() > 0) {
					pm = "+";
				}
				s = (new StringBuilder()).append(pm + item.getEnchantLevel())
						.append(" ").append(nameId).toString(); // \f1%0��撥��%1������������������
				sa = "$245";
			}
		} 
		pc.sendPackets(new S_ServerMessage(164, s, sa));
		pc.getInventory().removeItem(item, item.getCount());
	}
	//~甇血�銵��擳��頠� by eric1300460
	private void FailureEnchant(L1PcInstance pc, L1ItemInstance item, ClientThread client) {
		
		//鋆��風�頠� by missu0524
		if (item.getproctect() == true){
			//2009/04/25
			if(item.getItem().getType2()==2 && item.isEquipped()) {
				pc.addAc(+item.getEnchantLevel());
			}
			//2009/04/25
			item.setEnchantLevel(0);
			pc.sendPackets(new S_ItemStatus(item));
			pc.getInventory().saveItem(item, L1PcInventory.COL_ENCHANTLVL);
			item.setproctect(false);
			pc.sendPackets(new S_ServerMessage(1310));
			return;
		}
		//~鋆��風�頠�  by missu0524
		
		String s = "";
		String sa = "";
		int itemType = item.getItem().getType2();
		String nameId = item.getName();
		String pm = "";
		if (itemType == 1) { // 甇血
			if (!item.isIdentified() || item.getEnchantLevel() == 0) {
				s = nameId; // \f1%0��撥��%1������������������
				sa = "$245";
			} else {
				if (item.getEnchantLevel() > 0) {
					pm = "+";
				}
				s = (new StringBuilder()).append(pm + item.getEnchantLevel())
						.append(" ").append(nameId).toString(); // \f1%0��撥��%1������������������
				sa = "$245";
			}
		} else if (itemType == 2) { // ��
			if (!item.isIdentified() || item.getEnchantLevel() == 0) {
				s = nameId; // \f1%0��撥��%1������������������
				sa = " $252";
			} else {
				if (item.getEnchantLevel() > 0) {
					pm = "+";
				}
				s = (new StringBuilder()).append(pm + item.getEnchantLevel())
						.append(" ").append(nameId).toString(); // \f1%0��撥��%1������������������
				sa = " $252";
			}
		}
		pc.sendPackets(new S_ServerMessage(164, s, sa));
		pc.getInventory().removeItem(item, item.getCount());
	}

}
