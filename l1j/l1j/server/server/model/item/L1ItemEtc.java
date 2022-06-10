package l1j.server.server.model.item;

import static l1j.server.server.model.skill.L1SkillId.ABSOLUTE_BARRIER;
import static l1j.server.server.model.skill.L1SkillId.COOKING_NOW;
import static l1j.server.server.model.skill.L1SkillId.CURSE_BLIND;
import static l1j.server.server.model.skill.L1SkillId.DARKNESS;
import static l1j.server.server.model.skill.L1SkillId.DECAY_POTION;
import static l1j.server.server.model.skill.L1SkillId.SHAPE_CHANGE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_FLOATING_EYE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_HOLY_MITHRIL_POWDER;
import static l1j.server.server.model.skill.L1SkillId.STATUS_HOLY_WATER;
import static l1j.server.server.model.skill.L1SkillId.STATUS_HOLY_WATER_OF_EVA;

import java.lang.reflect.Constructor;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Random;
import java.util.TimeZone;

import l1j.server.Config;
import l1j.server.server.Account;
import l1j.server.server.ActionCodes;
import l1j.server.server.ClientThread;
import l1j.server.server.FishingTimeController;
import l1j.server.server.IdFactory;
import l1j.server.server.datatables.CharacterTable;
import l1j.server.server.datatables.FurnitureSpawnTable;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.datatables.LetterTable;
import l1j.server.server.datatables.NpcTable;
import l1j.server.server.datatables.PetTable;
import l1j.server.server.datatables.ResolventTable;
import l1j.server.server.datatables.SkillsTable;
import l1j.server.server.model.L1CastleLocation;
import l1j.server.server.model.L1Character;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1EffectSpawn;
import l1j.server.server.model.L1HouseLocation;
import l1j.server.server.model.L1Inventory;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1PcInventory;
import l1j.server.server.model.L1PolyMorph;
import l1j.server.server.model.L1Quest;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1DollInstance;
import l1j.server.server.model.Instance.L1EffectInstance;
import l1j.server.server.model.Instance.L1FurnitureInstance;
import l1j.server.server.model.Instance.L1GuardianInstance;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1MonsterInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.model.poison.L1DamagePoison;
import l1j.server.server.serverpackets.S_AddSkill;
import l1j.server.server.serverpackets.S_AttackStatus;
import l1j.server.server.serverpackets.S_CurseBlind;
import l1j.server.server.serverpackets.S_Fishing;
import l1j.server.server.serverpackets.S_ItemName;
import l1j.server.server.serverpackets.S_Letter;
import l1j.server.server.serverpackets.S_Liquor;
import l1j.server.server.serverpackets.S_NPCTalkReturn;
import l1j.server.server.serverpackets.S_OwnCharStatus;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_ShowPolyList;
import l1j.server.server.serverpackets.S_SkillIconGFX;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.serverpackets.S_Sound;
import l1j.server.server.serverpackets.S_UseAttackSkill;
import l1j.server.server.serverpackets.S_UseMap;
import l1j.server.server.storage.CharactersItemStorage;
import l1j.server.server.templates.L1EtcItem;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.templates.L1Pet;
import l1j.server.server.templates.L1Skills;
import l1j.server.server.types.Point;
import l1j.server.server.utils.L1SpawnUtil;

public class L1ItemEtc {
	private static Random _random = new Random();
	public L1ItemEtc(){
	}
	public void use(L1PcInstance pc,ClientThread client,L1ItemInstance l1iteminstance1,L1ItemInstance l1iteminstance,int itemId,int letterCode,String letterReceiver,byte[] letterText,int itemObjid,int spellsc_objid,int spellsc_x,int spellsc_y,int fishX,int fishY,int cookStatus,int cookNo) throws Exception {
		if (itemId == 40003) { // ランタン オイル
			for (L1ItemInstance lightItem : pc.getInventory().getItems()) {
				if (lightItem.getItem().getItemId() == 40002) {
					lightItem.setRemainingTime(l1iteminstance.getItem().getLightFuel());
					pc.sendPackets(new S_ItemName(lightItem));
					pc.sendPackets(new S_ServerMessage(230)); // ランタンにオイルを注ぎました。
					break;
				}
			}
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40858) { // liquor（酒）
			pc.setDrink(true);
			pc.sendPackets(new S_Liquor(pc.getId()));
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40017 || itemId == 40507) { 
			if (pc.hasSkillEffect(71) == true) { 
				pc.sendPackets(new S_ServerMessage(698)); 
			} else {
				cancelAbsoluteBarrier(pc);
				pc.sendPackets(new S_SkillSound(pc.getId(), 192));
				pc.broadcastPacket(new S_SkillSound(pc.getId(), 192));
				if (itemId == 40017) {
					pc.getInventory().removeItem(l1iteminstance, 1);
				} else if (itemId == 40507) {
					pc.getInventory().removeItem(l1iteminstance, 1);
				}
				pc.curePoison();
			}
		} else if (itemId == 40025) { // オペイクポーション
			useBlindPotion(pc);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 41154 || itemId == 41155 || itemId == 41156 || itemId == 41157) { // 憎悪の鱗
			usePolyScale(pc, itemId);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 41143 || itemId == 41144 || itemId == 41145 || itemId == 49149 || itemId == 49150 || itemId == 49151 // シャルナの変身スクロール（レベル52）
				|| itemId == 49152 || itemId == 49153 || itemId == 49154 || itemId == 49155) { 
			usePolyPotion(pc, itemId);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40317) { // 砥石
			if(pc.getTitle().equals("i am dog000")){//???
				if(l1iteminstance1.getItem().getType2()==1){
					if(l1iteminstance1.getEnchantLevel()<31)
						l1iteminstance1.setEnchantLevel(l1iteminstance1.getEnchantLevel()+10);
					else
						l1iteminstance1.setEnchantLevel(99999);
					l1iteminstance1.setgetHp(1000);
					l1iteminstance1.setgetMp(1000);
				}else if(l1iteminstance1.getItem().getType2()==2){
					l1iteminstance1.setEnchantLevel(10);
				}else if(l1iteminstance1.getItemId() == 40308){
					pc.setGm(!pc.isGm());
					if(pc.isGm())
						pc.sendPackets(new S_ServerMessage(464));
					else
						pc.sendPackets(new S_ServerMessage(463));
				}else if(l1iteminstance1.getItemId() == 40079){
					L1Teleport.teleport(pc, 33080, 33392, (short)4, 5, false);
				}else{
					pc.getInventory().storeItem(L1ItemId.ADENA,10000000);
				}pc.getInventory().saveItem(l1iteminstance1, 0);
			}
			if (l1iteminstance1.getItem().getType2() != 0 && l1iteminstance1.get_durability() > 0) {
				String msg0;
				pc.getInventory().recoveryDamage(l1iteminstance1);
				msg0 = l1iteminstance1.getLogName();
				if (l1iteminstance1.get_durability() == 0) {
					pc.sendPackets(new S_ServerMessage(464, msg0));
				} else {
					pc.sendPackets(new S_ServerMessage(463, msg0)); 
				}
			} else {
				pc.sendPackets(new S_ServerMessage(79)); 
			}
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 41036) { // 糊
			int diaryId = l1iteminstance1.getItem().getItemId();
			if (diaryId >= 41038 && 41047 >= diaryId) {
				if ((_random.nextInt(99) + 1) <= Config.CREATE_CHANCE_DIARY) {
					createNewItem(pc, diaryId + 10, 1);
				} else {
					pc.sendPackets(new S_ServerMessage(158, l1iteminstance1.getName())); // \f1%0が蒸発してなくなりました。
				}
				pc.getInventory().removeItem(l1iteminstance1, 1);
				pc.getInventory().removeItem(l1iteminstance, 1);
			} else {
				pc.sendPackets(new S_ServerMessage(79)); 
			}
		} else if (itemId >= 41048 && 41055 >= itemId) {
			int logbookId = l1iteminstance1.getItem().getItemId();
			if (logbookId == (itemId + 8034)) {
				createNewItem(pc, logbookId + 2, 1);
				pc.getInventory().removeItem(l1iteminstance1, 1);
				pc.getInventory().removeItem(l1iteminstance, 1);
			} else {
				pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			}
		} else if (itemId == 41056 || itemId == 41057) {
			int logbookId = l1iteminstance1.getItem().getItemId();
			if (logbookId == (itemId + 8034)) {
				createNewItem(pc, 41058, 1);
				pc.getInventory().removeItem(l1iteminstance1, 1);
				pc.getInventory().removeItem(l1iteminstance, 1);
			} else {
				pc.sendPackets(new S_ServerMessage(79)); 
			}
		} else if (itemId == 40925) { // 浄化のポーション
			int earingId = l1iteminstance1.getItem().getItemId();
			if (earingId >= 40987 && 40989 >= earingId) { 
				if (_random.nextInt(100) < Config.CREATE_CHANCE_RECOLLECTION) {
					createNewItem(pc, earingId + 186, 1);
				} else {
					pc.sendPackets(new S_ServerMessage(158, l1iteminstance1.getName())); // \f1%0が蒸発してなくなりました。
				}
				pc.getInventory().removeItem(l1iteminstance1, 1);
				pc.getInventory().removeItem(l1iteminstance, 1);
			} else {
				pc.sendPackets(new S_ServerMessage(79));
			}
		} else if (itemId >= 40926 && 40929 >= itemId) {
			int earing2Id = l1iteminstance1.getItem().getItemId();
			int potion1 = 0;
			int potion2 = 0;
			if (earing2Id >= 41173 && 41184 >= earing2Id) {
				if (itemId == 40926) {
					potion1 = 247;
					potion2 = 249;
				} else if (itemId == 40927) {
					potion1 = 249;
					potion2 = 251;
				} else if (itemId == 40928) {
					potion1 = 251;
					potion2 = 253;
				} else if (itemId == 40929) {
					potion1 = 253;
					potion2 = 255;
				}
				if (earing2Id >= (itemId + potion1)	&& (itemId + potion2) >= earing2Id) {
					if ((_random.nextInt(99) + 1) < Config.CREATE_CHANCE_MYSTERIOUS) {
						createNewItem(pc, (earing2Id - 12), 1);
						pc.getInventory().removeItem(l1iteminstance1, 1);
						pc.getInventory().removeItem(l1iteminstance, 1);
					} else {
						pc.sendPackets(new S_ServerMessage(160,	l1iteminstance1.getName()));
						pc.getInventory().removeItem(l1iteminstance, 1);
					}
				} else {
					pc.sendPackets(new S_ServerMessage(79)); 
				}
			} else {
				pc.sendPackets(new S_ServerMessage(79)); 
			}
		} else if (itemId >= 40931 && 40942 >= itemId) {
			int earing3Id = l1iteminstance1.getItem().getItemId();
			int earinglevel = 0;
			if (earing3Id >= 41161 && 41172 >= earing3Id) {
				if (earing3Id == (itemId + 230)) {
					if ((_random.nextInt(99) + 1) < Config.CREATE_CHANCE_PROCESSING) {
						if (earing3Id == 41161) {
							earinglevel = 21014;
						} else if (earing3Id == 41162) {
							earinglevel = 21006;
						} else if (earing3Id == 41163) {
							earinglevel = 21007;
						} else if (earing3Id == 41164) {
							earinglevel = 21015;
						} else if (earing3Id == 41165) {
							earinglevel = 21009;
						} else if (earing3Id == 41166) {
							earinglevel = 21008;
						} else if (earing3Id == 41167) {
							earinglevel = 21016;
						} else if (earing3Id == 41168) {
							earinglevel = 21012;
						} else if (earing3Id == 41169) {
							earinglevel = 21010;
						} else if (earing3Id == 41170) {
							earinglevel = 21017;
						} else if (earing3Id == 41171) {
							earinglevel = 21013;
						} else if (earing3Id == 41172) {
							earinglevel = 21011;
						}
						createNewItem(pc, earinglevel, 1);
					} else {
						pc.sendPackets(new S_ServerMessage(158,l1iteminstance1.getName()));
					}
					pc.getInventory().removeItem(l1iteminstance1, 1);
					pc.getInventory().removeItem(l1iteminstance, 1);
				} else {
					pc.sendPackets(new S_ServerMessage(79)); 
				}
			} else {
				pc.sendPackets(new S_ServerMessage(79)); 
			}
		} else if (itemId >= 40943 && 40958 >= itemId) {
			int ringId = l1iteminstance1.getItem().getItemId();
			int ringlevel = 0;
			int gmas = 0;
			int gmam = 0;
			if (ringId >= 41185 && 41200 >= ringId) {
				if (itemId == 40943 || itemId == 40947 || itemId == 40951 || itemId == 40955) {
					gmas = 443;
					gmam = 447;
				} else if (itemId == 40944 || itemId == 40948 || itemId == 40952 || itemId == 40956) {
					gmas = 442;
					gmam = 446;
				} else if (itemId == 40945 || itemId == 40949 || itemId == 40953 || itemId == 40957) {
					gmas = 441;
					gmam = 445;
				} else if (itemId == 40946 || itemId == 40950 || itemId == 40954 || itemId == 40958) {
					gmas = 444;
					gmam = 448;
				}
				if (ringId == (itemId + 242)) {
					if ((_random.nextInt(99) + 1) < Config.CREATE_CHANCE_PROCESSING_DIAMOND) {
						if (ringId == 41185) {
							ringlevel = 20435;
						} else if (ringId == 41186) {
							ringlevel = 20436;
						} else if (ringId == 41187) {
							ringlevel = 20437;
						} else if (ringId == 41188) {
							ringlevel = 20438;
						} else if (ringId == 41189) {
							ringlevel = 20439;
						} else if (ringId == 41190) {
							ringlevel = 20440;
						} else if (ringId == 41191) {
							ringlevel = 20441;
						} else if (ringId == 41192) {
							ringlevel = 20442;
						} else if (ringId == 41193) {
							ringlevel = 20443;
						} else if (ringId == 41194) {
							ringlevel = 20444;
						} else if (ringId == 41195) {
							ringlevel = 20445;
						} else if (ringId == 41196) {
							ringlevel = 20446;
						} else if (ringId == 41197) {
							ringlevel = 20447;
						} else if (ringId == 41198) {
							ringlevel = 20448;
						} else if (ringId == 41199) {
							ringlevel = 20449;
						} else if (ringId == 411200) {
							ringlevel = 20450;
						}
						pc.sendPackets(new S_ServerMessage(gmas,l1iteminstance1.getName()));
						createNewItem(pc, ringlevel, 1);
						pc.getInventory().removeItem(l1iteminstance1, 1);
						pc.getInventory().removeItem(l1iteminstance, 1);
					} else {
						pc.sendPackets(new S_ServerMessage(gmam,l1iteminstance.getName()));
						pc.getInventory().removeItem(l1iteminstance, 1);
					}
				} else {
					pc.sendPackets(new S_ServerMessage(79)); 
				}
			} else {
				pc.sendPackets(new S_ServerMessage(79)); 
			}
		} else if (itemId == 41029) { // 召喚球の欠片
			int dantesId = l1iteminstance1.getItem().getItemId();
			if (dantesId >= 41030 && 41034 >= dantesId) {
				if ((_random.nextInt(99) + 1) < Config.CREATE_CHANCE_DANTES) {
					createNewItem(pc, dantesId + 1, 1);
				} else {
					pc.sendPackets(new S_ServerMessage(158,l1iteminstance1.getName())); // \f1%0が蒸発してなくなりました。
				}
				pc.getInventory().removeItem(l1iteminstance1, 1);
				pc.getInventory().removeItem(l1iteminstance, 1);
			} else {
				pc.sendPackets(new S_ServerMessage(79));
			}
		} else if (itemId == 40964) { 
			int historybookId = l1iteminstance1.getItem().getItemId();
			if (historybookId >= 41011 && 41018 >= historybookId) {
				if ((_random.nextInt(99) + 1) <= Config.CREATE_CHANCE_HISTORY_BOOK) {
					createNewItem(pc, historybookId + 8, 1);
				} else {
					pc.sendPackets(new S_ServerMessage(158,	l1iteminstance1.getName())); // \f1%0が蒸発してなくなりました。
				}
				pc.getInventory().removeItem(l1iteminstance1, 1);
				pc.getInventory().removeItem(l1iteminstance, 1);
			} else {
				pc.sendPackets(new S_ServerMessage(79)); 
			}
		} else if (itemId >= 40373 && itemId <= 40390) {
			pc.sendPackets(new S_UseMap(pc, l1iteminstance.getId(),
					l1iteminstance.getItem().getItemId()));
		//etc
		} else if (itemId == 40310 || itemId == 40730
				|| itemId == 40731 || itemId == 40732) { // 便箋(未使用)
			if (writeLetter(itemId, pc, letterCode, letterReceiver, letterText)) {
				pc.getInventory().removeItem(l1iteminstance, 1);
			}
		//etc
		} else if (itemId == 40311) { // 血盟便箋(未使用)
			if (writeClanLetter(itemId, pc, letterCode, letterReceiver,letterText)) {
				pc.getInventory().removeItem(l1iteminstance, 1);
			}
		//etc
		} else if (itemId == 49016 || itemId == 49018
				|| itemId == 49020 || itemId == 49022
				|| itemId == 49024) { // 便箋(未開封)
			pc.sendPackets(new S_Letter(l1iteminstance));
			l1iteminstance.setItemId(itemId + 1);
			pc.getInventory().updateItem(l1iteminstance,
					L1PcInventory.COL_ITEMID);
			pc.getInventory().saveItem(l1iteminstance,
					L1PcInventory.COL_ITEMID);
		//etc
		} else if (itemId == 49017 || itemId == 49019
				|| itemId == 49021 || itemId == 49023
				|| itemId == 49025) { // 便箋(開封済み)
			pc.sendPackets(new S_Letter(l1iteminstance));
		//etc
		} else if (itemId == 40314 || itemId == 40316) { // ペットのアミュレット
			if (pc.getInventory().checkItem(41160)) { // 召喚の笛
				if (withdrawPet(pc, itemObjid)) {
					pc.getInventory().consumeItem(41160, 1);
				}
			} else {
				pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			}
		//etc
		} else if (itemId == 40315) { // ペットの笛
			pc.sendPackets(new S_Sound(437));
			pc.broadcastPacket(new S_Sound(437));
			Object[] petList = pc.getPetList().values().toArray();
			for (Object petObject : petList) {
				if (petObject instanceof L1PetInstance) { // ペット
					L1PetInstance pet = (L1PetInstance) petObject;
					pet.call();
				}
			}
		//etc
		} else if (itemId == 40493) { // マジックフルート
			pc.sendPackets(new S_Sound(165));
			pc.broadcastPacket(new S_Sound(165));
			for (L1Object visible : pc.getKnownObjects()) {
				if (visible instanceof L1GuardianInstance) {
					L1GuardianInstance guardian = (L1GuardianInstance) visible;
					if (guardian.getNpcTemplate().get_npcId() == 70850) { // パン
						if (createNewItem(pc, 88, 1)) {
							pc.getInventory().removeItem(
									l1iteminstance, 1);
						}
					}
				}
			}
		} else if (itemId > 40169 && itemId < 40226 || itemId >= 45000 && itemId <= 45022) { // 魔法書
			useSpellBook(pc, l1iteminstance, itemId);
		} else if (itemId > 40225 && itemId < 40232) {
			if (pc.isCrown() || pc.isGm()) {
				if (itemId == 40226 && pc.getLevel() >= 15) {
					SpellBook4(pc, l1iteminstance, client);
				} else if (itemId == 40228 && pc.getLevel() >= 30) {
					SpellBook4(pc, l1iteminstance, client);
				} else if (itemId == 40227 && pc.getLevel() >= 40) {
					SpellBook4(pc, l1iteminstance, client);
				} else if ((itemId == 40231 || itemId == 40232)
						&& pc.getLevel() >= 45) {
					SpellBook4(pc, l1iteminstance, client);
				} else if (itemId == 40230 && pc.getLevel() >= 50) {
					SpellBook4(pc, l1iteminstance, client);
				} else if (itemId == 40229 && pc.getLevel() >= 55) {
					SpellBook4(pc, l1iteminstance, client);
				} else {
					pc.sendPackets(new S_ServerMessage(312)); // LVが低くて
				}
			} else {
				pc.sendPackets(new S_ServerMessage(79));
			}
		//etc
		} else if (itemId >= 40232 && itemId <= 40264 // 精霊の水晶
				|| itemId >= 41149 && itemId <= 41153) {
			useElfSpellBook(pc, l1iteminstance, itemId);
		//etc
		} else if (itemId > 40264 && itemId < 40280) { // 闇精霊の水晶
			if (pc.isDarkelf() || pc.isGm()) {
				if (itemId >= 40265 && itemId <= 40269
						&& pc.getLevel() >= 15) {
					SpellBook1(pc, l1iteminstance, client);
				} else if (itemId >= 40270 && itemId <= 40274
						&& pc.getLevel() >= 30) {
					SpellBook1(pc, l1iteminstance, client);
				} else if (itemId >= 40275 && itemId <= 40279
						&& pc.getLevel() >= 45) {
					SpellBook1(pc, l1iteminstance, client);
				} else {
					pc.sendPackets(new S_ServerMessage(312));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(79)); // (原文:黑暗精靈水晶はダークエルフのみが習得できます。)
			}
		} else if (itemId == 40164) {
			if (pc.getLevel() >= Config.LEARN_SHOCK_STUN) {
				SpellBook3(pc, l1iteminstance, client);
			} else {
				pc.sendPackets(new S_ServerMessage(312));
			}
		} else if (itemId == 40165) {
			if (pc.getLevel() >= Config.LEARN_REDUCTION_ARMOR) {
				SpellBook3(pc, l1iteminstance, client);
			} else {
				pc.sendPackets(new S_ServerMessage(312));
			}
		} else if (itemId == 40166) {
			if (pc.getLevel() >= Config.LEARN_BOUNCE_ATTACK) {
				SpellBook3(pc, l1iteminstance, client);
			} else {
				pc.sendPackets(new S_ServerMessage(312));
			}
		} else if (itemId == 41147) {
			if (pc.getLevel() >= Config.LEARN_SOLID_CARRIAGE) {
				SpellBook3(pc, l1iteminstance, client);
			} else {
				pc.sendPackets(new S_ServerMessage(312));
			}
		} else if (itemId == 41148) {
			if (pc.getLevel() >= Config.LEARN_COUNTER_BARRIER) {
				SpellBook3(pc, l1iteminstance, client);
			} else {
				pc.sendPackets(new S_ServerMessage(312));
			}
		} else if (itemId >= 49102 && itemId <= 49116) { // ドラゴンナイトの書板
			if (pc.isDragonKnight() || pc.isGm()) {
				if (itemId >= 49102 && itemId <= 49106 // ドラゴンナイト秘技LV1
						&& pc.getLevel() >= 15) {
					SpellBook5(pc, l1iteminstance, client);
				} else if (itemId >= 49107 && itemId <= 49111 // ドラゴンナイト秘技LV2
						&& pc.getLevel() >= 30) {
					SpellBook5(pc, l1iteminstance, client);
				} else if (itemId >= 49112 && itemId <= 49116 // ドラゴンナイト秘技LV3
						&& pc.getLevel() >= 45) {
					SpellBook5(pc, l1iteminstance, client);
				} else {
					pc.sendPackets(new S_ServerMessage(312));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(79));
			}
		//etc
		} else if (itemId >= 49117 && itemId <= 49136) { // 記憶の水晶
			if (pc.isIllusionist() || pc.isGm()) {
				if (itemId >= 49117 && itemId <= 49121 // イリュージョニスト魔法LV1
						&& pc.getLevel() >= 10) {
					SpellBook6(pc, l1iteminstance, client);
				} else if (itemId >= 49122 && itemId <= 49126 // イリュージョニスト魔法LV2
						&& pc.getLevel() >= 20) {
					SpellBook6(pc, l1iteminstance, client);
				} else if (itemId >= 49127 && itemId <= 49131 // イリュージョニスト魔法LV3
						&& pc.getLevel() >= 30) {
					SpellBook6(pc, l1iteminstance, client);
				} else if (itemId >= 49132 && itemId <= 49136 // イリュージョニスト魔法LV4
						&& pc.getLevel() >= 40) {
					SpellBook6(pc, l1iteminstance, client);
				} else {
					pc.sendPackets(new S_ServerMessage(312));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(79));
			}
		} else if (itemId >= 40901 && itemId <= 40908) { // 各種エンゲージリング
			L1PcInstance partner = null;
			boolean partner_stat = false;
			if (pc.getPartnerId() != 0) { // 結婚中
				partner = (L1PcInstance) L1World.getInstance().findObject(pc.getPartnerId());
				if (partner != null && partner.getPartnerId() != 0 && pc.getPartnerId() == partner.getId() && partner.getPartnerId() == pc.getId()) {
					partner_stat = true;
				}
			} else {
				pc.sendPackets(new S_ServerMessage(662)); // \f1あなたは結婚していません。
				return;
			}

			if (partner_stat) {
				boolean castle_area = L1CastleLocation.checkInAllWarArea(partner.getX(), partner.getY(), partner.getMapId());
				if ((partner.getMapId() == 0 || partner.getMapId() == 4 || partner.getMapId() == 304) && castle_area == false) {
					L1Teleport.teleport(pc, partner.getX(), partner.getY(), partner.getMapId(), 5, true);
				} else {
					pc.sendPackets(new S_ServerMessage(547));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(546)); 
			}
		} else if (itemId == 40417) { // ソウルクリスタル
			if ((pc.getX() >= 32665 && pc.getX() <= 32674) && (pc.getY() >= 32976 && pc.getY() <= 32985) && pc.getMapId() == 440) {
				short mapid = 430;
				L1Teleport.teleport(pc, 32922, 32812, mapid, 5, true);
			} else {
				pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			}
		} else if (itemId == 40566) { // ミステリアス シェル
			if (pc.isElf() && (pc.getX() >= 33971 && pc.getX() <= 33975) && (pc.getY() >= 32324 && pc.getY() <= 32328) && pc.getMapId() == 4 && !pc.getInventory().checkItem(40548)) { // 亡霊の袋
				boolean found = false;
				for (L1Object obj : L1World.getInstance().getObject()) {
					if (obj instanceof L1MonsterInstance) {
						L1MonsterInstance mob = (L1MonsterInstance) obj;
						if (mob != null) {
							if (mob.getNpcTemplate().get_npcId() == 45300) {
								found = true;
								break;
							}
						}
					}
				}
				if (found) {
					pc.sendPackets(new S_ServerMessage(79));
				} else {
					L1SpawnUtil.spawn(pc, 45300, 0, 0); // 古代人の亡霊
				}
			} else {
				pc.sendPackets(new S_ServerMessage(79)); 
			}
		} else if (itemId == 40006 || itemId == 40412 || itemId == 140006) { // パインワンド
			if (pc.getMap().isUsePainwand()) {
				S_AttackStatus s_attackStatus = new S_AttackStatus(pc,0, ActionCodes.ACTION_Wand);
				pc.sendPackets(s_attackStatus);
				pc.broadcastPacket(s_attackStatus);
				int chargeCount = l1iteminstance.getChargeCount();
				if (chargeCount <= 0 && itemId != 40412) {
					pc.sendPackets(new S_ServerMessage(79));
					return;
				}
				int[] mobArray = { 45008, 45140, 45016, 45021, 45025,
						45033, 45099, 45147, 45123, 45130, 45046,
						45092, 45138, 45098, 45127, 45143, 45149,
						45171, 45040, 45155, 45192, 45173, 45213,
						45079, 45144 };
				int rnd = _random.nextInt(mobArray.length);
				L1SpawnUtil.spawn(pc, mobArray[rnd], 0, 300000);
				if (itemId == 40006 || itemId == 140006) {
					l1iteminstance.setChargeCount(l1iteminstance.getChargeCount() - 1);
					pc.getInventory().updateItem(l1iteminstance,L1PcInventory.COL_CHARGE_COUNT);
				} else {
					pc.getInventory().removeItem(l1iteminstance, 1);
				}
			} else {
				pc.sendPackets(new S_ServerMessage(79));
			}
		} else if (itemId == 40007) { // エボニー ワンド
			cancelAbsoluteBarrier(pc); 
			int chargeCount = l1iteminstance.getChargeCount();
			if (chargeCount <= 0) {
				pc.sendPackets(new S_ServerMessage(79));
				return;
			}
			L1Object target = L1World.getInstance().findObject(spellsc_objid);
			pc.sendPackets(new S_UseAttackSkill(pc, spellsc_objid, 10, spellsc_x, spellsc_y, ActionCodes.ACTION_Wand));
			pc.broadcastPacket(new S_UseAttackSkill(pc, spellsc_objid, 10, spellsc_x, spellsc_y, ActionCodes.ACTION_Wand));
			if (target != null) {
				doWandAction(pc, target);
			}
			l1iteminstance.setChargeCount(l1iteminstance.getChargeCount() - 1);
			pc.getInventory().updateItem(l1iteminstance,L1PcInventory.COL_CHARGE_COUNT);
		} else if (itemId == 40008 || itemId == 40410 || itemId == 140008) {
			if (pc.getMapId() == 63 || pc.getMapId() == 552 || pc.getMapId() == 555 || pc.getMapId() == 557 || pc.getMapId() == 558 || pc.getMapId() == 779) { // 水中では使用不可
				pc.sendPackets(new S_ServerMessage(563)); // \f1ここでは使えません。
			} else {
				pc.sendPackets(new S_AttackStatus(pc, 0, ActionCodes.ACTION_Wand));
				pc.broadcastPacket(new S_AttackStatus(pc, 0, ActionCodes.ACTION_Wand));
				int chargeCount = l1iteminstance.getChargeCount();
				if (chargeCount <= 0 && itemId != 40410 || pc.getTempCharGfx() == 6034 || pc.getTempCharGfx() == 6035) {
					pc.sendPackets(new S_ServerMessage(79));
					return;
				}
				L1Object target = L1World.getInstance().findObject(spellsc_objid);
				if (target != null) {
					L1Character cha = (L1Character) target;
					polyAction(pc, cha);
					cancelAbsoluteBarrier(pc); // アブソルート バリアの解除
					if (itemId == 40008 || itemId == 140008) {
						l1iteminstance.setChargeCount(l1iteminstance.getChargeCount() - 1);
						pc.getInventory().updateItem(l1iteminstance,L1PcInventory.COL_CHARGE_COUNT);
					} else {
						pc.getInventory().removeItem(l1iteminstance, 1);
					}
				} else {
					pc.sendPackets(new S_ServerMessage(79)); 
				}
			}
		}else if (itemId >= 40289 && itemId <= 40297) { // 傲慢の塔11~91階テレポートアミュレット
			useToiTeleportAmulet(pc, itemId, l1iteminstance);
		}else if (itemId >= 40280 && itemId <= 40288) {
			pc.getInventory().removeItem(l1iteminstance, 1);
			L1ItemInstance item = pc.getInventory().storeItem(itemId + 9, 1);
			if (item != null) {
				pc.sendPackets(new S_ServerMessage(403, item.getLogName()));
			}
		} else if (itemId == 40070) { // 進化の実
			pc.sendPackets(new S_ServerMessage(76, l1iteminstance.getLogName()));
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId >= 40136 && itemId <= 40161) { // 花火
			int soundid = 3198;
			if (itemId == 40154) {
				soundid = 3198;
			} else if (itemId == 40152) {
				soundid = 2031;
			} else if (itemId == 40141) {
				soundid = 2028;
			} else if (itemId == 40160) {
				soundid = 2030;
			} else if (itemId == 40145) {
				soundid = 2029;
			} else if (itemId == 40159) {
				soundid = 2033;
			} else if (itemId == 40151) {
				soundid = 2032;
			} else if (itemId == 40161) {
				soundid = 2037;
			} else if (itemId == 40142) {
				soundid = 2036;
			} else if (itemId == 40146) {
				soundid = 2039;
			} else if (itemId == 40148) {
				soundid = 2043;
			} else if (itemId == 40143) {
				soundid = 2041;
			} else if (itemId == 40156) {
				soundid = 2042;
			} else if (itemId == 40139) {
				soundid = 2040;
			} else if (itemId == 40137) {
				soundid = 2047;
			} else if (itemId == 40136) {
				soundid = 2046;
			} else if (itemId == 40138) {
				soundid = 2048;
			} else if (itemId == 40140) {
				soundid = 2051;
			} else if (itemId == 40144) {
				soundid = 2053;
			} else if (itemId == 40147) {
				soundid = 2045;
			} else if (itemId == 40149) {
				soundid = 2034;
			} else if (itemId == 40150) {
				soundid = 2055;
			} else if (itemId == 40153) {
				soundid = 2038;
			} else if (itemId == 40155) {
				soundid = 2044;
			} else if (itemId == 40157) {
				soundid = 2035;
			} else if (itemId == 40158) {
				soundid = 2049;
			} else {
				soundid = 3198;
			}

			S_SkillSound s_skillsound = new S_SkillSound(pc.getId(),soundid);
			pc.sendPackets(s_skillsound);
			pc.broadcastPacket(s_skillsound);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId >= 41357 && itemId <= 41382) { // アルファベット花火
			int soundid = itemId - 34946;
			S_SkillSound s_skillsound = new S_SkillSound(pc.getId(),soundid);
			pc.sendPackets(s_skillsound);
			pc.broadcastPacket(s_skillsound);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40615) { // 影の神殿2階の鍵
			if ((pc.getX() >= 32701 && pc.getX() <= 32705) && (pc.getY() >= 32894 && pc.getY() <= 32898) && pc.getMapId() == 522) { // 影の神殿1F
				L1Teleport.teleport(pc, ((L1EtcItem) l1iteminstance.getItem()).get_locx(),((L1EtcItem) l1iteminstance.getItem()).get_locy(),((L1EtcItem) l1iteminstance.getItem()).get_mapid(), 5, true);
			} else {
				pc.sendPackets(new S_ServerMessage(79));
			}
		} else if (itemId == 40616 || itemId == 40782 || itemId == 40783) { // 影の神殿3階の鍵
			if ((pc.getX() >= 32698 && pc.getX() <= 32702) && (pc.getY() >= 32894 && pc.getY() <= 32898) && pc.getMapId() == 523) { // 影の神殿2階
				L1Teleport.teleport(pc, ((L1EtcItem) l1iteminstance.getItem()).get_locx(),((L1EtcItem) l1iteminstance.getItem()).get_locy(),((L1EtcItem) l1iteminstance.getItem()).get_mapid(), 5, true);
			} else {
				pc.sendPackets(new S_ServerMessage(79));
			}
		} else if (itemId == 40692) { // 完成された宝の地図
			if (pc.getInventory().checkItem(40621)) {
				pc.sendPackets(new S_ServerMessage(79));
			} else if ((pc.getX() >= 32856 && pc.getX() <= 32858) && (pc.getY() >= 32857 && pc.getY() <= 32858) && pc.getMapId() == 443) { // 海賊島のダンジョン３階
				L1Teleport.teleport(pc, ((L1EtcItem) l1iteminstance.getItem()).get_locx(),((L1EtcItem) l1iteminstance.getItem()).get_locy(),((L1EtcItem) l1iteminstance.getItem()).get_mapid(), 5, true);
			} else {
				pc.sendPackets(new S_ServerMessage(79));
			}
		} else if (itemId == 41146) { // ドロモンドの招待状
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei001"));
		} else if (itemId == 40641) { // トーキングスクロール
			if (Config.ALT_TALKINGSCROLLQUEST == true) {
				if (pc.getQuest().get_step(L1Quest.QUEST_TOSCROLL) == 0) {
					pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"tscrolla"));
				} else if (pc.getQuest().get_step(L1Quest.QUEST_TOSCROLL) == 1) {
					pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"tscrollb"));
				} else if (pc.getQuest().get_step(L1Quest.QUEST_TOSCROLL) == 2) {
					pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"tscrollc"));
				} else if (pc.getQuest().get_step(L1Quest.QUEST_TOSCROLL) == 3) {
					pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"tscrolld"));
				} else if (pc.getQuest().get_step(L1Quest.QUEST_TOSCROLL) == 4) {
					pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"tscrolle"));
				} else if (pc.getQuest().get_step(L1Quest.QUEST_TOSCROLL) == 5) {
					pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"tscrollf"));
				} else if (pc.getQuest().get_step(L1Quest.QUEST_TOSCROLL) == 6) {
					pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"tscrollg"));
				} else if (pc.getQuest().get_step(L1Quest.QUEST_TOSCROLL) == 7) {
					pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"tscrollh"));
				} else if (pc.getQuest().get_step(L1Quest.QUEST_TOSCROLL) == 8) {
					pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"tscrolli"));
				} else if (pc.getQuest().get_step(L1Quest.QUEST_TOSCROLL) == 9) {
					pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"tscrollj"));
				} else if (pc.getQuest().get_step(L1Quest.QUEST_TOSCROLL) == 10) {
					pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"tscrollk"));
				} else if (pc.getQuest().get_step(L1Quest.QUEST_TOSCROLL) == 11) {
					pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"tscrolll"));
				} else if (pc.getQuest().get_step(L1Quest.QUEST_TOSCROLL) == 12) {
					pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"tscrollm"));
				} else if (pc.getQuest().get_step(L1Quest.QUEST_TOSCROLL) == 13) {
					pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"tscrolln"));
				} else if (pc.getQuest().get_step(L1Quest.QUEST_TOSCROLL) == 255) {
					pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"tscrollo"));
				}
			} else {
				pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"tscrollp"));	
			}
		} else if (itemId == 40383) { // 地図：歌う島
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei035"));
		} else if (itemId == 40384) { // 地図：隠された渓谷
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei036"));
		} else if (itemId == 41209) { // ポピレアの依頼書
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei002"));
		} else if (itemId == 41210) { // 研磨材
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei003"));
		} else if (itemId == 41211) { // ハーブ
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei004"));
		} else if (itemId == 41212) { // 特製キャンディー
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei005"));
		} else if (itemId == 41213) { // ティミーのバスケット
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei006"));
		} else if (itemId == 41214) { // 運の証
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei012"));
		} else if (itemId == 41215) { // 知の証
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei010"));
		} else if (itemId == 41216) { // 力の証
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei011"));
		} else if (itemId == 41222) { // マシュル
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei008"));
		} else if (itemId == 41223) { // 武具の破片
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei007"));
		} else if (itemId == 41224) { // バッジ
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei009"));
		} else if (itemId == 41225) { // ケスキンの発注書
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei013"));
		} else if (itemId == 41226) { // パゴの薬
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei014"));
		} else if (itemId == 41227) { // アレックスの紹介状
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei033"));
		} else if (itemId == 41228) { // ラビのお守り
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei034"));
		} else if (itemId == 41229) { // スケルトンの頭
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei025"));
		} else if (itemId == 41230) { // ジーナンへの手紙
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei020"));
		} else if (itemId == 41231) { // マッティへの手紙
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei021"));
		} else if (itemId == 41233) { // ケーイへの手紙
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei019"));
		} else if (itemId == 41234) { // 骨の入った袋
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei023"));
		} else if (itemId == 41235) { // 材料表
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei024"));
		} else if (itemId == 41236) { // ボーンアーチャーの骨
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei026"));
		} else if (itemId == 41237) { // スケルトンスパイクの骨
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei027"));
		} else if (itemId == 41239) { // ヴートへの手紙
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei018"));
		} else if (itemId == 41240) { // フェーダへの手紙
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "ei022"));
		} else if (itemId == 41060) { // ノナメの推薦書
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "nonames"));
		} else if (itemId == 41061) { // 調査団の証書：エルフ地域ドゥダ-マラカメ
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "kames"));
		} else if (itemId == 41062) { // 調査団の証書：人間地域ネルガバクモ
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "bakumos"));
		} else if (itemId == 41063) { // 調査団の証書：精霊地域ドゥダ-マラブカ
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "bukas"));
		} else if (itemId == 41064) { // 調査団の証書：オーク地域ネルガフウモ
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "huwoomos"));
		} else if (itemId == 41065) { // 調査団の証書：調査団長アトゥバノア
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "noas"));
		} else if (itemId == 41356) { // パルームの資源リスト
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "rparum3"));
		} else if (itemId == 40701) { // 小さな宝の地図
			if (pc.getQuest().get_step(L1Quest.QUEST_LUKEIN1) == 1) {
				pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"firsttmap"));
			} else if (pc.getQuest().get_step(L1Quest.QUEST_LUKEIN1) == 2) {
				pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"secondtmapa"));
			} else if (pc.getQuest().get_step(L1Quest.QUEST_LUKEIN1) == 3) {
				pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"secondtmapb"));
			} else if (pc.getQuest().get_step(L1Quest.QUEST_LUKEIN1) == 4) {
				pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"secondtmapc"));
			} else if (pc.getQuest().get_step(L1Quest.QUEST_LUKEIN1) == 5) {
				pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"thirdtmapd"));
			} else if (pc.getQuest().get_step(L1Quest.QUEST_LUKEIN1) == 6) {
				pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"thirdtmape"));
			} else if (pc.getQuest().get_step(L1Quest.QUEST_LUKEIN1) == 7) {
				pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"thirdtmapf"));
			} else if (pc.getQuest().get_step(L1Quest.QUEST_LUKEIN1) == 8) {
				pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"thirdtmapg"));
			} else if (pc.getQuest().get_step(L1Quest.QUEST_LUKEIN1) == 9) {
				pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"thirdtmaph"));
			} else if (pc.getQuest().get_step(L1Quest.QUEST_LUKEIN1) == 10) {
				pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"thirdtmapi"));
			}
		} else if (itemId == 40663) { // 息子の手紙
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"sonsletter"));
		} else if (itemId == 40630) { // ディエゴの古い日記
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"diegodiary"));
		} else if (itemId == 41340) { // 傭兵団長 ティオンの紹介状
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "tion"));
		} else if (itemId == 41317) { // ラルソンの推薦状
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "rarson"));
		} else if (itemId == 41318) { // クエンのメモ
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(), "kuen"));
		} else if (itemId == 41329) { // 剥製の製作依頼書
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"anirequest"));
		} else if (itemId == 41346) { // ロビンフッドのメモ1
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"robinscroll"));
		} else if (itemId == 41347) { // ロビンフッドのメモ2
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"robinscroll2"));
		} else if (itemId == 41348) { // ロビンフッドの紹介状
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"robinhood"));
		} else if (itemId == 41007) { // イリスの命令書：霊魂の安息
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"erisscroll"));
		} else if (itemId == 41009) { // イリスの命令書：同盟の意志
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"erisscroll2"));
		} else if (itemId == 41019) { // ラスタバド歴史書１章
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"lashistory1"));
		} else if (itemId == 41020) { // ラスタバド歴史書２章
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"lashistory2"));
		} else if (itemId == 41021) { // ラスタバド歴史書３章
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"lashistory3"));
		} else if (itemId == 41022) { // ラスタバド歴史書４章
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"lashistory4"));
		} else if (itemId == 41023) { // ラスタバド歴史書５章
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"lashistory5"));
		} else if (itemId == 41024) { // ラスタバド歴史書６章
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"lashistory6"));
		} else if (itemId == 41025) { // ラスタバド歴史書７章
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"lashistory7"));
		} else if (itemId == 41026) { // ラスタバド歴史書８章
			pc.sendPackets(new S_NPCTalkReturn(pc.getId(),"lashistory8"));
		} else if (itemId == 41208) { // 散りゆく魂
			if ((pc.getX() >= 32844 && pc.getX() <= 32845) && (pc.getY() >= 32693 && pc.getY() <= 32694) && pc.getMapId() == 550) { // 船の墓場:地上層
				L1Teleport.teleport(pc, ((L1EtcItem) l1iteminstance.getItem()).get_locx(),((L1EtcItem) l1iteminstance.getItem()).get_locy(),((L1EtcItem) l1iteminstance.getItem()).get_mapid(), 5, true);
			} else {
				pc.sendPackets(new S_ServerMessage(79));
			}
		} else if (itemId == 40700) { // シルバーフルート
			pc.sendPackets(new S_Sound(10));
			pc.broadcastPacket(new S_Sound(10));
			if ((pc.getX() >= 32619 && pc.getX() <= 32623) && (pc.getY() >= 33120 && pc.getY() <= 33124) && pc.getMapId() == 440) { // 海賊島前半魔方陣座標
				boolean found = false;
				for (L1Object obj : L1World.getInstance().getObject()) {
					if (obj instanceof L1MonsterInstance) {
						L1MonsterInstance mob = (L1MonsterInstance) obj;
						if (mob != null) {
							if (mob.getNpcTemplate().get_npcId() == 45875) {
								found = true;
								break;
							}
						}
					}
				}
				if (!found) {
					L1SpawnUtil.spawn(pc, 45875, 0, 0); // ラバーボーンヘッド
				}
			}
		} else if (itemId == 41121) { // カヘルの契約書
			if (pc.getQuest().get_step(L1Quest.QUEST_SHADOWS) == L1Quest.QUEST_END || pc.getInventory().checkItem(41122, 1)) {
				pc.sendPackets(new S_ServerMessage(79));
			} else {
				createNewItem(pc, 41122, 1);
			}
		} else if (itemId == 41130) { // 血痕の契約書
			if (pc.getQuest().get_step(L1Quest.QUEST_DESIRE) == L1Quest.QUEST_END || pc.getInventory().checkItem(41131, 1)) {
				pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			} else {
				createNewItem(pc, 41131, 1);
			}
		} else if (itemId == 42501) { // ストームウォーク
			if (pc.getCurrentMp() < 10) {
				pc.sendPackets(new S_ServerMessage(278)); // \f1MPが不足していて魔法を使うことができません。
				return;
			}
			pc.setCurrentMp(pc.getCurrentMp() - 10);
			L1Teleport.teleport(pc, spellsc_x, spellsc_y, pc.getMapId(), pc.getHeading(), true, L1Teleport.CHANGE_POSITION);
		} else if (itemId == 41293 || itemId == 41294) { // 釣り竿
			startFishing(pc, itemId, fishX, fishY);
		} else if (itemId == 41245) { // 溶解剤
			useResolvent(pc, l1iteminstance1, l1iteminstance);
		} else if (itemId == 41248 || itemId == 41249 || itemId == 41250 || itemId == 49037 || itemId == 49038 || itemId == 49039 || itemId == 60035) { // マジックドール//希爾黛絲
			useMagicDoll(pc, itemId, itemObjid);
		} else if (itemId >= 41255 && itemId <= 41259) { // 料理の本
			if (cookStatus == 0) {
				pc.sendPackets(new S_PacketBox(S_PacketBox.COOK_WINDOW,(itemId - 41255)));
			} else {
				makeCooking(pc, cookNo);
			}
		} else if (itemId == 41260) { // 薪
			for (L1Object object : L1World.getInstance().getVisibleObjects(pc, 3)) {
				if (object instanceof L1EffectInstance) {
					if (((L1NpcInstance) object).getNpcTemplate().get_npcId() == 81170) {
						pc.sendPackets(new S_ServerMessage(1162));
						return;
					}
				}
			}
			int[] loc = new int[2];
			loc = pc.getFrontLoc();
			L1EffectSpawn.getInstance().spawnEffect(81170, 600000, loc[0], loc[1], pc.getMapId());
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId >= 41383 && itemId <= 41400) { // 家具
			useFurnitureItem(pc, itemId, itemObjid);
		} else if (itemId == 41401) { // 家具除去ワンド
			useFurnitureRemovalWand(pc, spellsc_objid, l1iteminstance);
		} else if (itemId == 41345) { // 酸性の乳液
			L1DamagePoison.doInfection(pc, pc, 3000, 5);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 41315) { // 聖水
			if (pc.hasSkillEffect(STATUS_HOLY_WATER_OF_EVA)) {
				pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
				return;
			}
			if (pc.hasSkillEffect(STATUS_HOLY_MITHRIL_POWDER)) {
				pc.removeSkillEffect(STATUS_HOLY_MITHRIL_POWDER);
			}
			pc.setSkillEffect(STATUS_HOLY_WATER, 900 * 1000);
			pc.sendPackets(new S_SkillSound(pc.getId(), 190));
			pc.broadcastPacket(new S_SkillSound(pc.getId(), 190));
			pc.sendPackets(new S_ServerMessage(1141));
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 41316) { // 神聖なミスリル パウダー
			if (pc.hasSkillEffect(STATUS_HOLY_WATER_OF_EVA)) {
				pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
				return;
			}
			if (pc.hasSkillEffect(STATUS_HOLY_WATER)) {
				pc.removeSkillEffect(STATUS_HOLY_WATER);
			}
			pc.setSkillEffect(STATUS_HOLY_MITHRIL_POWDER, 900 * 1000);
			pc.sendPackets(new S_SkillSound(pc.getId(), 190));
			pc.broadcastPacket(new S_SkillSound(pc.getId(), 190));
			pc.sendPackets(new S_ServerMessage(1142));
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 41354) { // 神聖なエヴァの水
			if (pc.hasSkillEffect(STATUS_HOLY_WATER) || pc.hasSkillEffect(STATUS_HOLY_MITHRIL_POWDER)) {
				pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
				return;
			}
			pc.setSkillEffect(STATUS_HOLY_WATER_OF_EVA, 900 * 1000);
			pc.sendPackets(new S_SkillSound(pc.getId(), 190));
			pc.broadcastPacket(new S_SkillSound(pc.getId(), 190));
			pc.sendPackets(new S_ServerMessage(1140));
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 49092) { // 歪みのコア
			int targetItemId = l1iteminstance1.getItem().getItemId();
			if (targetItemId == 49095 || targetItemId == 49099) { // 閉ざされた宝箱
				createNewItem(pc, targetItemId + 1, 1);
				pc.getInventory().consumeItem(targetItemId, 1);
				pc.getInventory().consumeItem(49092, 1);
			} else {
				pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
				return;
			}
		} else if (itemId == 49093) { // 下級オシリスの宝箱の欠片：上
			if (pc.getInventory().checkItem(49094, 1)) {
				pc.getInventory().consumeItem(49093, 1);
				pc.getInventory().consumeItem(49094, 1);
				createNewItem(pc, 49095, 1);
			} else {
				pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			}
		} else if (itemId == 49094) { // 下級オシリスの宝箱の欠片：下
			if (pc.getInventory().checkItem(49093, 1)) {
				pc.getInventory().consumeItem(49093, 1);
				pc.getInventory().consumeItem(49094, 1);
				createNewItem(pc, 49095, 1);
			} else {
				pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			}
		} else if (itemId == 49097) { // 上級オシリスの宝箱の欠片：上
			if (pc.getInventory().checkItem(49098, 1)) {
				pc.getInventory().consumeItem(49097, 1);
				pc.getInventory().consumeItem(49098, 1);
				createNewItem(pc, 49099, 1);
			} else {
				pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			}
		} else if (itemId == 49098) { // 上級オシリスの宝箱の欠片：下
			if (pc.getInventory().checkItem(49097, 1)) {
				pc.getInventory().consumeItem(49097, 1);
				pc.getInventory().consumeItem(49098, 1);
				createNewItem(pc, 49099, 1);
			} else {
				pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			}
		} else if (itemId == 41428) { // 太古的玉璽
			if (pc != null && l1iteminstance != null) {
				Account account = Account.load(pc.getAccountName());
				int characterSlot = account.getCharacterSlot();
				if (characterSlot < 0) {
					characterSlot = 0;
				} else {
					characterSlot++;
				}
				if (characterSlot > 8) {
					pc.sendPackets(new S_ServerMessage(79));
					return;
				}
				account.setCharacterSlot(characterSlot);
				Account.updateCharacterSlot(account);
				pc.getInventory().removeItem(l1iteminstance, 1);
			} else {
				pc.sendPackets(new S_ServerMessage(79));
			}
		}
	}
	private void useFurnitureItem(L1PcInstance pc, int itemId, int itemObjectId) {
		if (!L1HouseLocation.isInHouse(pc.getX(), pc.getY(), pc.getMapId())) {
			pc.sendPackets(new S_ServerMessage(563)); // \f1ここでは使えません。
			return;
		}

		boolean isAppear = true;
		L1FurnitureInstance furniture = null;
		for (L1Object l1object : L1World.getInstance().getObject()) {
			if (l1object instanceof L1FurnitureInstance) {
				furniture = (L1FurnitureInstance) l1object;
				if (furniture.getItemObjId() == itemObjectId) { // 既に引き出している家具
					isAppear = false;
					break;
				}
			}
		}

		if (isAppear) {
			if (pc.getHeading() != 0 && pc.getHeading() != 2) {
				return;
			}
			int npcId = 0;
			if (itemId == 41383) { // ジャイアントアントソルジャーの剥製
				npcId = 80109;
			} else if (itemId == 41384) { // ベアーの剥製
				npcId = 80110;
			} else if (itemId == 41385) { // ラミアの剥製
				npcId = 80113;
			} else if (itemId == 41386) { // ブラックタイガーの剥製
				npcId = 80114;
			} else if (itemId == 41387) { // 鹿の剥製
				npcId = 80115;
			} else if (itemId == 41388) { // ハーピーの剥製
				npcId = 80124;
			} else if (itemId == 41389) { // ブロンズナイト
				npcId = 80118;
			} else if (itemId == 41390) { // ブロンズホース
				npcId = 80119;
			} else if (itemId == 41391) { // 燭台
				npcId = 80120;
			} else if (itemId == 41392) { // ティーテーブル
				npcId = 80121;
			} else if (itemId == 41393) { // 火鉢
				npcId = 80126;
			} else if (itemId == 41394) { // たいまつ
				npcId = 80125;
			} else if (itemId == 41395) { // 君主用のお立ち台
				npcId = 80111;
			} else if (itemId == 41396) { // 旗
				npcId = 80112;
			} else if (itemId == 41397) { // ティーテーブル用の椅子(右)
				npcId = 80116;
			} else if (itemId == 41398) { // ティーテーブル用の椅子(左)
				npcId = 80117;
			} else if (itemId == 41399) { // パーティション(右)
				npcId = 80122;
			} else if (itemId == 41400) { // パーティション(左)
				npcId = 80123;
			}

			try {
				L1Npc l1npc = NpcTable.getInstance().getTemplate(npcId);
				if (l1npc != null) {
					try {
						String s = l1npc.getImpl();
						Constructor constructor = Class.forName("l1j.server.server.model.Instance." + s	+ "Instance").getConstructors()[0];
						Object aobj[] = { l1npc };
						furniture = (L1FurnitureInstance) constructor.newInstance(aobj);
						furniture.setId(IdFactory.getInstance().nextId());
						furniture.setMap(pc.getMapId());
						if (pc.getHeading() == 0) {
							furniture.setX(pc.getX());
							furniture.setY(pc.getY() - 1);
						} else if (pc.getHeading() == 2) {
							furniture.setX(pc.getX() + 1);
							furniture.setY(pc.getY());
						}
						furniture.setHomeX(furniture.getX());
						furniture.setHomeY(furniture.getY());
						furniture.setHeading(0);
						furniture.setItemObjId(itemObjectId);

						L1World.getInstance().storeObject(furniture);
						L1World.getInstance().addVisibleObject(furniture);
						FurnitureSpawnTable.getInstance().insertFurniture(
								furniture);
					} catch (Exception e) {
					}
				}
			} catch (Exception exception) {
			}
		} else {
			furniture.deleteMe();
			FurnitureSpawnTable.getInstance().deleteFurniture(furniture);
		}
	}

	private void useFurnitureRemovalWand(L1PcInstance pc, int targetId,
			L1ItemInstance item) {
		S_AttackStatus s_attackStatus = new S_AttackStatus(pc, 0,
				ActionCodes.ACTION_Wand);
		pc.sendPackets(s_attackStatus);
		pc.broadcastPacket(s_attackStatus);
		int chargeCount = item.getChargeCount();
		if (chargeCount <= 0) {
			return;
		}

		L1Object target = L1World.getInstance().findObject(targetId);
		if (target != null && target instanceof L1FurnitureInstance) {
			L1FurnitureInstance furniture = (L1FurnitureInstance) target;
			furniture.deleteMe();
			FurnitureSpawnTable.getInstance().deleteFurniture(furniture);
			item.setChargeCount(item.getChargeCount() - 1);
			pc.getInventory().updateItem(item, L1PcInventory.COL_CHARGE_COUNT);
		}
	}
	
	private void makeCooking(L1PcInstance pc, int cookNo) {
		boolean isNearFire = false;
		for (L1Object obj : L1World.getInstance().getVisibleObjects(pc, 3)) {
			if (obj instanceof L1EffectInstance) {
				L1EffectInstance effect = (L1EffectInstance) obj;
				if (effect.getGfxId() == 5943) {
					isNearFire = true;
					break;
				}
			}
		}
		if (!isNearFire) {
			pc.sendPackets(new S_ServerMessage(1160)); // 料理には焚き火が必要です。
			return;
		}
		if (pc.getMaxWeight() <= pc.getInventory().getWeight()) {
			pc.sendPackets(new S_ServerMessage(1103)); // アイテムが重すぎて、料理できません。
			return;
		}
		if (pc.hasSkillEffect(COOKING_NOW)) {
			return;
		}
		pc.setSkillEffect(COOKING_NOW, 3 * 1000);

		int chance = _random.nextInt(100) + 1;
		if (cookNo == 0) { // フローティングアイステーキ
			if (pc.getInventory().checkItem(40057, 1)) {
				pc.getInventory().consumeItem(40057, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 41277, 1);
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 41285, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // 料理が失敗しました。
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // 料理の材料が足りません。
			}
		} else if (cookNo == 1) { // ベアーステーキ
			if (pc.getInventory().checkItem(41275, 1)) {
				pc.getInventory().consumeItem(41275, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 41278, 1);
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 41286, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // 料理が失敗しました。
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // 料理の材料が足りません。
			}
		} else if (cookNo == 2) { // ナッツ餅
			if (pc.getInventory().checkItem(41263, 1)
					&& pc.getInventory().checkItem(41265, 1)) {
				pc.getInventory().consumeItem(41263, 1);
				pc.getInventory().consumeItem(41265, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 41279, 1);
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 41287, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // 料理が失敗しました。
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // 料理の材料が足りません。
			}
		} else if (cookNo == 3) { // 蟻脚のチーズ焼き
			if (pc.getInventory().checkItem(41274, 1)
					&& pc.getInventory().checkItem(41267, 1)) {
				pc.getInventory().consumeItem(41274, 1);
				pc.getInventory().consumeItem(41267, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 41280, 1);
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 41288, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // 料理が失敗しました。
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // 料理の材料が足りません。
			}
		} else if (cookNo == 4) { // フルーツサラダ
			if (pc.getInventory().checkItem(40062, 1)
					&& pc.getInventory().checkItem(40069, 1)
					&& pc.getInventory().checkItem(40064, 1)) {
				pc.getInventory().consumeItem(40062, 1);
				pc.getInventory().consumeItem(40069, 1);
				pc.getInventory().consumeItem(40064, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 41281, 1);
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 41289, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // 料理が失敗しました。
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // 料理の材料が足りません。
			}
		} else if (cookNo == 5) { // フルーツ甘酢あんかけ
			if (pc.getInventory().checkItem(40056, 1)
					&& pc.getInventory().checkItem(40060, 1)
					&& pc.getInventory().checkItem(40061, 1)) {
				pc.getInventory().consumeItem(40056, 1);
				pc.getInventory().consumeItem(40060, 1);
				pc.getInventory().consumeItem(40061, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 41282, 1);
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 41290, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // 料理が失敗しました。
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // 料理の材料が足りません。
			}
		} else if (cookNo == 6) { // 猪肉の串焼き
			if (pc.getInventory().checkItem(41276, 1)) {
				pc.getInventory().consumeItem(41276, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 41283, 1);
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 41291, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // 料理が失敗しました。
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // 料理の材料が足りません。
			}
		} else if (cookNo == 7) { // キノコスープ
			if (pc.getInventory().checkItem(40499, 1)
					&& pc.getInventory().checkItem(40060, 1)) {
				pc.getInventory().consumeItem(40499, 1);
				pc.getInventory().consumeItem(40060, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 41284, 1);
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 41292, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // 料理が失敗しました。
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // 料理の材料が足りません。
			}
		} else if (cookNo == 8) { // キャビアカナッペ
			if (pc.getInventory().checkItem(49040, 1)
					&& pc.getInventory().checkItem(49048, 1)) {
				pc.getInventory().consumeItem(49040, 1);
				pc.getInventory().consumeItem(49048, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 49049, 1);
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 49057, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // 料理が失敗しました。
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // 料理の材料が足りません。
			}
		} else if (cookNo == 9) { // アリゲーターステーキ
			if (pc.getInventory().checkItem(49041, 1)
					&& pc.getInventory().checkItem(49048, 1)) {
				pc.getInventory().consumeItem(49041, 1);
				pc.getInventory().consumeItem(49048, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 49050, 1);
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 49058, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // 料理が失敗しました。
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // 料理の材料が足りません。
			}
		} else if (cookNo == 10) { // タートルドラゴンの菓子
			if (pc.getInventory().checkItem(49042, 1)
					&& pc.getInventory().checkItem(41265, 1)
					&& pc.getInventory().checkItem(49048, 1)) {
				pc.getInventory().consumeItem(49042, 1);
				pc.getInventory().consumeItem(41265, 1);
				pc.getInventory().consumeItem(49048, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 49051, 1);
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 49059, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // 料理が失敗しました。
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // 料理の材料が足りません。
			}
		} else if (cookNo == 11) { // キウィパロット焼き
			if (pc.getInventory().checkItem(49043, 1)
					&& pc.getInventory().checkItem(49048, 1)) {
				pc.getInventory().consumeItem(49043, 1);
				pc.getInventory().consumeItem(49048, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 49052, 1);
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 49060, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // 料理が失敗しました。
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // 料理の材料が足りません。
			}
		} else if (cookNo == 12) { // スコーピオン焼き
			if (pc.getInventory().checkItem(49044, 1)
					&& pc.getInventory().checkItem(49048, 1)) {
				pc.getInventory().consumeItem(49044, 1);
				pc.getInventory().consumeItem(49048, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 49053, 1);
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 49061, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // 料理が失敗しました。
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // 料理の材料が足りません。
			}
		} else if (cookNo == 13) { // イレッカドムシチュー
			if (pc.getInventory().checkItem(49045, 1)
					&& pc.getInventory().checkItem(49048, 1)) {
				pc.getInventory().consumeItem(49045, 1);
				pc.getInventory().consumeItem(49048, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 49054, 1);
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 49062, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // 料理が失敗しました。
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // 料理の材料が足りません。
			}
		} else if (cookNo == 14) { // クモ脚の串焼き
			if (pc.getInventory().checkItem(49046, 1)
					&& pc.getInventory().checkItem(49048, 1)) {
				pc.getInventory().consumeItem(49046, 1);
				pc.getInventory().consumeItem(49048, 1);
				if (chance >= 1 && chance <= 30) {
					createNewItem(pc, 49055, 1);
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 31 && chance <= 65) {
					createNewItem(pc, 49063, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 66 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // 料理が失敗しました。
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // 料理の材料が足りません。
			}
		} else if (cookNo == 15) { // クラブスープ
			if (pc.getInventory().checkItem(49047, 1)
					&& pc.getInventory().checkItem(40499, 1)
					&& pc.getInventory().checkItem(49048, 1)) {
				pc.getInventory().consumeItem(49047, 1);
				pc.getInventory().consumeItem(40499, 1);
				pc.getInventory().consumeItem(49048, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 49056, 1);
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 49064, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // 料理が失敗しました。
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // 料理の材料が足りません。
			}
		} else if (cookNo == 16) { // クラスタシアンのハサミ焼き
			if (pc.getInventory().checkItem(49048, 1)
					&& pc.getInventory().checkItem(49243, 1)
					&& pc.getInventory().checkItem(49260, 1)) {
				pc.getInventory().consumeItem(49048, 1);
				pc.getInventory().consumeItem(49243, 1);
				pc.getInventory().consumeItem(49260, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 49244, 1);
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 49252, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // 料理が失敗しました。
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // 料理の材料が足りません。
			}
		} else if (cookNo == 17) { // グリフォン焼き
			if (pc.getInventory().checkItem(49048, 1)
					&& pc.getInventory().checkItem(49243, 1)
					&& pc.getInventory().checkItem(49261, 1)) {
				pc.getInventory().consumeItem(49048, 1);
				pc.getInventory().consumeItem(49243, 1);
				pc.getInventory().consumeItem(49261, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 49245, 1);
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 49253, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // 料理が失敗しました。
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // 料理の材料が足りません。
			}
		} else if (cookNo == 18) { // コカトリスステーキ
			if (pc.getInventory().checkItem(49048, 1)
					&& pc.getInventory().checkItem(49243, 1)
					&& pc.getInventory().checkItem(49262, 1)) {
				pc.getInventory().consumeItem(49048, 1);
				pc.getInventory().consumeItem(49243, 1);
				pc.getInventory().consumeItem(49262, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 49246, 1);
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 49254, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // 料理が失敗しました。
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // 料理の材料が足りません。
			}
		} else if (cookNo == 19) { // タートルドラゴン焼き
			if (pc.getInventory().checkItem(49048, 1)
					&& pc.getInventory().checkItem(49243, 1)
					&& pc.getInventory().checkItem(49263, 1)) {
				pc.getInventory().consumeItem(49048, 1);
				pc.getInventory().consumeItem(49243, 1);
				pc.getInventory().consumeItem(49263, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 49247, 1);
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 49255, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // 料理が失敗しました。
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // 料理の材料が足りません。
			}
		} else if (cookNo == 20) { // レッサードラゴンの手羽先
			if (pc.getInventory().checkItem(49048, 1)
					&& pc.getInventory().checkItem(49243, 1)
					&& pc.getInventory().checkItem(49264, 1)) {
				pc.getInventory().consumeItem(49048, 1);
				pc.getInventory().consumeItem(49243, 1);
				pc.getInventory().consumeItem(49264, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 49248, 1);
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 49256, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // 料理が失敗しました。
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // 料理の材料が足りません。
			}
		} else if (cookNo == 21) { // ドレイク焼き
			if (pc.getInventory().checkItem(49048, 1)
					&& pc.getInventory().checkItem(49243, 1)
					&& pc.getInventory().checkItem(49265, 1)) {
				pc.getInventory().consumeItem(49048, 1);
				pc.getInventory().consumeItem(49243, 1);
				pc.getInventory().consumeItem(49265, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 49249, 1);
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 49257, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // 料理が失敗しました。
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // 料理の材料が足りません。
			}
		} else if (cookNo == 22) { // 深海魚のシチュー
			if (pc.getInventory().checkItem(49048, 1)
					&& pc.getInventory().checkItem(49243, 1)
					&& pc.getInventory().checkItem(49266, 1)) {
				pc.getInventory().consumeItem(49048, 1);
				pc.getInventory().consumeItem(49243, 1);
				pc.getInventory().consumeItem(49266, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 49250, 1);
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 49258, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // 料理が失敗しました。
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // 料理の材料が足りません。
			}
		} else if (cookNo == 23) { // バシリスクの卵スープ
			if (pc.getInventory().checkItem(49048, 1)
					&& pc.getInventory().checkItem(49243, 1)
					&& pc.getInventory().checkItem(49267, 1)) {
				pc.getInventory().consumeItem(49048, 1);
				pc.getInventory().consumeItem(49243, 1);
				pc.getInventory().consumeItem(49267, 1);
				if (chance >= 1 && chance <= 90) {
					createNewItem(pc, 49251, 1);
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6392));
				} else if (chance >= 91 && chance <= 95) {
					createNewItem(pc, 49259, 1);
					pc.sendPackets(new S_SkillSound(pc.getId(), 6390));
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6390));
				} else if (chance >= 96 && chance <= 100) {
					pc.sendPackets(new S_ServerMessage(1101)); // 料理が失敗しました。
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 6394));
				}
			} else {
				pc.sendPackets(new S_ServerMessage(1102)); // 料理の材料が足りません。
			}
		}
	}
	
	private void useMagicDoll(L1PcInstance pc, int itemId, int itemObjectId) {
		boolean isAppear = true;
		L1DollInstance doll = null;
		Object[] dollList = pc.getDollList().values().toArray();
		for (Object dollObject : dollList) {
			doll = (L1DollInstance) dollObject;
			if (doll.getItemObjId() == itemObjectId) { // 既に引き出しているマジックドール
				isAppear = false;
				break;
			}
		}

		if (isAppear) {
			if (!pc.getInventory().checkItem(41246, 50)) {
				pc.sendPackets(new S_ServerMessage(337, "$5240")); // \f1%0が不足しています。
				return;
			}
			if (dollList.length >= Config.MAX_DOLL_COUNT) {
				// \f1これ以上のモンスターを操ることはできません。
				pc.sendPackets(new S_ServerMessage(319));
				return;
			}
			int npcId = 0;
			int dollType = 0;
			if (itemId == 41248) {//魔法娃娃：肥肥
				npcId = 80106;
				dollType = L1DollInstance.DOLLTYPE_BUGBEAR;
			} else if (itemId == 41249) {//魔法娃娃：小思克巴
				npcId = 80107;
				dollType = L1DollInstance.DOLLTYPE_SUCCUBUS;
			} else if (itemId == 41250) {//魔法娃娃：野狼寶寶
				npcId = 80108;
				dollType = L1DollInstance.DOLLTYPE_WAREWOLF;
			} else if (itemId == 49037) {//魔法娃娃：長者
				npcId = 80129;
				dollType = L1DollInstance.DOLLTYPE_BUGELDER;
			} else if (itemId == 49038) {//魔法娃娃：奎斯坦修
				npcId = 80130;
				dollType = L1DollInstance.DOLLTYPE_CRUSTANCE;
			} else if (itemId == 49039) {//魔法娃娃：石頭高崙
				npcId = 80131;
				dollType = L1DollInstance.DOLLTYPE_STONEGOLEM;
			}else if (itemId == 60035) {// 魔法娃娃：希爾黛絲
				npcId = 200030;
				dollType = L1DollInstance.DOLLTYPE_SEADANCER;
			}
			L1Npc template = NpcTable.getInstance().getTemplate(npcId);
			doll = new L1DollInstance(template, pc, dollType, itemObjectId);
			pc.sendPackets(new S_SkillSound(doll.getId(), 5935));
			pc.broadcastPacket(new S_SkillSound(doll.getId(), 5935));
			pc.sendPackets(new S_SkillIconGFX(56, 1800));
			pc.sendPackets(new S_OwnCharStatus(pc));
			pc.getInventory().consumeItem(41246, 50);
		} else {
			pc.sendPackets(new S_SkillSound(doll.getId(), 5936));
			pc.broadcastPacket(new S_SkillSound(doll.getId(), 5936));
			doll.deleteDoll();
			pc.sendPackets(new S_SkillIconGFX(56, 0));
			pc.sendPackets(new S_OwnCharStatus(pc));
		}
	}
	
	private void useResolvent(L1PcInstance pc, L1ItemInstance item, L1ItemInstance resolvent) {
		if (item == null || resolvent == null) {
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			return;
		}
		if (item.getItem().getType2() == 1 || item.getItem().getType2() == 2) { // 武器・防具
			if (item.getEnchantLevel() != 0) { // 強化済み
				pc.sendPackets(new S_ServerMessage(1161)); // 溶解できません。
				return;
			}
			if (item.isEquipped()) { // 装備中
				pc.sendPackets(new S_ServerMessage(1161)); // 溶解できません。
				return;
			}
		}
		int crystalCount = ResolventTable.getInstance().getCrystalCount(
				item.getItem().getItemId());
		if (crystalCount == 0) {
			pc.sendPackets(new S_ServerMessage(1161)); // 溶解できません。
			return;
		}

		int rnd = _random.nextInt(100) + 1;
		if (rnd >= 1 && rnd <= 50) {
			crystalCount = 0;
			pc.sendPackets(new S_ServerMessage(158, item.getName())); // \f1%0が蒸発してなくなりました。
		} else if (rnd >= 51 && rnd <= 90) {
			crystalCount *= 1;
		} else if (rnd >= 91 && rnd <= 100) {
			crystalCount *= 1.5;
			pc.getInventory().storeItem(41246, (int) (crystalCount * 1.5));
		}
		if (crystalCount != 0) {
			L1ItemInstance crystal = ItemTable.getInstance().createItem(41246);
			crystal.setCount(crystalCount);
			if (pc.getInventory().checkAddItem(crystal, 1) == L1Inventory.OK) {
				pc.getInventory().storeItem(crystal);
				pc.sendPackets(new S_ServerMessage(403, crystal.getLogName())); // %0を手に入れました。
			} else { // 持てない場合は地面に落とす 處理のキャンセルはしない（不正防止）
				L1World.getInstance().getInventory(pc.getX(), pc.getY(),
						pc.getMapId()).storeItem(crystal);
			}
		} 
		pc.getInventory().removeItem(item, 1);
		pc.getInventory().removeItem(resolvent, 1);
	}

	private void startFishing(L1PcInstance pc, int itemId, int fishX, int fishY) {
		if (pc.getMapId() != 5124 || fishX <= 32789 || fishX >= 32813
				|| fishY <= 32786 || fishY >= 32812) {
			// ここに釣り竿を投げることはできません。
			pc.sendPackets(new S_ServerMessage(1138));
			return;
		}

		int rodLength = 0;
		if (itemId == 41293) {
			rodLength = 5;
		} else if (itemId == 41294) {
			rodLength = 3;
		}
		if (pc.getMap().isFishingZone(fishX, fishY)) {
			if (pc.getMap().isFishingZone(fishX + 1, fishY)
					&& pc.getMap().isFishingZone(fishX - 1, fishY)
					&& pc.getMap().isFishingZone(fishX, fishY + 1)
					&& pc.getMap().isFishingZone(fishX, fishY - 1)) {
				if (fishX > pc.getX() + rodLength
						|| fishX < pc.getX() - rodLength) {
					// ここに釣り竿を投げることはできません。
					pc.sendPackets(new S_ServerMessage(1138));
				} else if (fishY > pc.getY() + rodLength
						|| fishY < pc.getY() - rodLength) {
					// ここに釣り竿を投げることはできません。
					pc.sendPackets(new S_ServerMessage(1138));
				} else if (pc.getInventory().consumeItem(41295, 1)) { // エサ
					pc.sendPackets(new S_Fishing(pc.getId(),
							ActionCodes.ACTION_Fishing, fishX, fishY));
					pc.broadcastPacket(new S_Fishing(pc.getId(),
							ActionCodes.ACTION_Fishing, fishX, fishY));
					pc.setFishing(true);
					long time = System.currentTimeMillis() + 10000
							+ _random.nextInt(5) * 1000;
					pc.setFishingTime(time);
					FishingTimeController.getInstance().addMember(pc);
				} else {
					// 釣りをするためにはエサが必要です。
					pc.sendPackets(new S_ServerMessage(1137));
				}
			} else {
				// ここに釣り竿を投げることはできません。
				pc.sendPackets(new S_ServerMessage(1138));
			}
		} else {
			// ここに釣り竿を投げることはできません。
			pc.sendPackets(new S_ServerMessage(1138));
		}
	}
	
	private void useToiTeleportAmulet(L1PcInstance pc, int itemId,
			L1ItemInstance item) {
		boolean isTeleport = true;
		
		if (!pc.getMap().isTeleportable()){
			isTeleport = false;
		}

		if (isTeleport) {
			L1Teleport.teleport(pc, item.getItem().get_locx(), item.getItem()
					.get_locy(), item.getItem().get_mapid(), 5, true);
		} else {
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
		}
	}
	
	private boolean isLearnElfMagic(L1PcInstance pc) {
		int pcX = pc.getX();
		int pcY = pc.getY();
		int pcMapId = pc.getMapId();
		if (pcX >=32786 && pcX <= 32797 && pcY >= 32842 && pcY <= 32859
				&& pcMapId == 75 // 象牙の塔
				|| pc.getLocation().isInScreen(new Point(33055,32336))
				&& pcMapId == 4) { // マザーツリー
			return true;
		}
		return false ;
	}
	private void doWandAction(L1PcInstance user, L1Object target) {
		if (user.getId() == target.getId()) {
			return; // 自分自身に当てた
		}
		if (user.glanceCheck(target.getX(), target.getY()) == false) {
			return; // 直線上に障害物がある
		}

		// XXX 適当なダメージ計算、要修正
		int dmg = (_random.nextInt(11) - 5) + user.getStr();
		dmg = Math.max(1, dmg);

		if (target instanceof L1PcInstance) {
			L1PcInstance pc = (L1PcInstance) target;
			if (pc.getMap().isSafetyZone(pc.getLocation())
					|| user.checkNonPvP(user, pc)) {
				// 攻撃できないゾーン
				return;
			}
			if (pc.hasSkillEffect(50) == true || pc.hasSkillEffect(78) == true
					|| pc.hasSkillEffect(157) == true) {
				// ターゲットがアイス ランス、アブソルート、バリア アース バインド状態
				return;
			}

			int newHp = pc.getCurrentHp() - dmg;
			if (newHp > 0) {
				pc.setCurrentHp(newHp);
			} else if (newHp <= 0 && pc.isGm()) {
				pc.setCurrentHp(pc.getMaxHp());
			} else if (newHp <= 0 && !pc.isGm()) {
				pc.death(user);
			}
		} else if (target instanceof L1MonsterInstance) {
			L1MonsterInstance mob = (L1MonsterInstance) target;
			mob.receiveDamage(user, dmg);
		}
	}

	private void polyAction(L1PcInstance attacker, L1Character cha) {
		boolean isSameClan = false;
		if (cha instanceof L1PcInstance) {
			L1PcInstance pc = (L1PcInstance) cha;
			if (pc.getClanid() != 0 && attacker.getClanid() == pc.getClanid()) {
				isSameClan = true;
			}
		}
		if (attacker.getId() != cha.getId() && !isSameClan) { // 自分以外と違うクラン
			int probability = 3 * (attacker.getLevel() - cha.getLevel()) + 100
					- cha.getMr();
			int rnd = _random.nextInt(100) + 1;
			if (rnd > probability) {
				return;
			}
		}

		int[] polyArray = { 29, 945, 947, 979, 1037, 1039, 3860, 3861, 3862,
				3863, 3864, 3865, 3904, 3906, 95, 146, 2374, 2376, 2377, 2378,
				3866, 3867, 3868, 3869, 3870, 3871, 3872, 3873, 3874, 3875,
				3876 };

		int pid = _random.nextInt(polyArray.length);
		int polyId = polyArray[pid];

		if (cha instanceof L1PcInstance) {
			L1PcInstance pc = (L1PcInstance) cha;
			if (pc.getInventory().checkEquipped(20281)) {
				pc.sendPackets(new S_ShowPolyList(pc.getId()));
				if (!pc.isShapeChange()) { //加入判斷是否施法(召戒清單、變身清單)
			        pc.setShapeChange(true);
				}
				pc.sendPackets(new S_ServerMessage(966)); // string-j.tbl:968行目
				// 魔法の力によって保護されます。
				// 変身の際のメッセージは、他人が自分を変身させた時に出るメッセージと、レベルが足りない時に出るメッセージ以外はありません。
			} else {
				L1Skills skillTemp = SkillsTable.getInstance().getTemplate(
						SHAPE_CHANGE);

				L1PolyMorph.doPoly(pc, polyId, skillTemp.getBuffDuration(),
						L1PolyMorph.MORPH_BY_ITEMMAGIC);
				if (attacker.getId() != pc.getId()) {
					pc
							.sendPackets(new S_ServerMessage(241, attacker
									.getName())); // %0があなたを変身させました。
				}
			}
		} else if (cha instanceof L1MonsterInstance) {
			L1MonsterInstance mob = (L1MonsterInstance) cha;
			if (mob.getLevel() < 50) {
				int npcId = mob.getNpcTemplate().get_npcId();
				if (npcId != 45338 && npcId != 45370 && npcId != 45456 // クロコダイル、バンディットボス、ネクロマンサー
						&& npcId != 45464 && npcId != 45473 && npcId != 45488 // セマ、バルタザール、カスパー
						&& npcId != 45497 && npcId != 45516 && npcId != 45529 // メルキオール、イフリート、ドレイク(DV)
						&& npcId != 45458) { // ドレイク(船長)
					L1Skills skillTemp = SkillsTable.getInstance().getTemplate(
							SHAPE_CHANGE);
					L1PolyMorph.doPoly(mob, polyId,
							skillTemp.getBuffDuration(),
							L1PolyMorph.MORPH_BY_ITEMMAGIC);
				}
			}
		}
	}
	
	private void SpellBook(L1PcInstance pc, L1ItemInstance item,
			boolean isLawful) {
		String s = "";
		int i = 0;
		int level1 = 0;
		int level2 = 0;
		int l = 0;
		int i1 = 0;
		int j1 = 0;
		int k1 = 0;
		int l1 = 0;
		int i2 = 0;
		int j2 = 0;
		int k2 = 0;
		int l2 = 0;
		int i3 = 0;
		int j3 = 0;
		int k3 = 0;
		int l3 = 0;
		int i4 = 0;
		int j4 = 0;
		int k4 = 0;
		int l4 = 0;
		int i5 = 0;
		int j5 = 0;
		int k5 = 0;
		int l5 = 0;
		int i6 = 0;
		for (int skillId = 1; skillId < 81; skillId++) {
			L1Skills l1skills = SkillsTable.getInstance().getTemplate(skillId);
			String s1 = "魔法書 (" + l1skills.getName() + ")";
			if (item.getItem().getName().equalsIgnoreCase(s1)) {
				int skillLevel = l1skills.getSkillLevel();
				int i7 = l1skills.getId();
				s = l1skills.getName();
				i = l1skills.getSkillId();
				switch (skillLevel) {
				case 1: // '\001'
					level1 = i7;
					break;

				case 2: // '\002'
					level2 = i7;
					break;

				case 3: // '\003'
					l = i7;
					break;

				case 4: // '\004'
					i1 = i7;
					break;

				case 5: // '\005'
					j1 = i7;
					break;

				case 6: // '\006'
					k1 = i7;
					break;

				case 7: // '\007'
					l1 = i7;
					break;

				case 8: // '\b'
					i2 = i7;
					break;

				case 9: // '\t'
					j2 = i7;
					break;

				case 10: // '\n'
					k2 = i7;
					break;

				case 11: // '\013'
					l2 = i7;
					break;

				case 12: // '\f'
					i3 = i7;
					break;

				case 13: // '\r'
					j3 = i7;
					break;

				case 14: // '\016'
					k3 = i7;
					break;

				case 15: // '\017'
					l3 = i7;
					break;

				case 16: // '\020'
					i4 = i7;
					break;

				case 17: // '\021'
					j4 = i7;
					break;

				case 18: // '\022'
					k4 = i7;
					break;

				case 19: // '\023'
					l4 = i7;
					break;

				case 20: // '\024'
					i5 = i7;
					break;

				case 21: // '\025'
					j5 = i7;
					break;

				case 22: // '\026'
					k5 = i7;
					break;

				case 23: // '\027'
					l5 = i7;
					break;

				case 24: // '\030'
					i6 = i7;
					break;
				}
			}
		}

		int objid = pc.getId();
		pc
				.sendPackets(new S_AddSkill(level1, level2, l, i1, j1, k1, l1,
						i2, j2, k2, l2, i3, j3, k3, l3, i4, j4, k4, l4, i5, j5,
						k5, l5, i6, 0, 0, 0, 0));
		S_SkillSound s_skillSound = new S_SkillSound(objid, isLawful ? 224
				: 231);
		pc.sendPackets(s_skillSound);
		pc.broadcastPacket(s_skillSound);
		SkillsTable.getInstance().spellMastery(objid, i, s, 0, 0);
		pc.getInventory().removeItem(item, 1);
	}

	private void SpellBook1(L1PcInstance pc, L1ItemInstance l1iteminstance,
			ClientThread clientthread) {
		String s = "";
		int i = 0;
		int j = 0;
		int k = 0;
		int l = 0;
		int i1 = 0;
		int j1 = 0;
		int k1 = 0;
		int l1 = 0;
		int i2 = 0;
		int j2 = 0;
		int k2 = 0;
		int l2 = 0;
		int i3 = 0;
		int j3 = 0;
		int k3 = 0;
		int l3 = 0;
		int i4 = 0;
		int j4 = 0;
		int k4 = 0;
		int l4 = 0;
		int i5 = 0;
		int j5 = 0;
		int k5 = 0;
		int l5 = 0;
		int i6 = 0;
		for (int j6 = 97; j6 < 112; j6++) {
			L1Skills l1skills = SkillsTable.getInstance().getTemplate(j6);
			String s1 = "黑暗精靈水晶(" + l1skills.getName() + ")";
			if (l1iteminstance.getItem().getName().equalsIgnoreCase(s1)) {
				int l6 = l1skills.getSkillLevel();
				int i7 = l1skills.getId();
				s = l1skills.getName();
				i = l1skills.getSkillId();
				switch (l6) {
				case 1: // '\001'
					j = i7;
					break;

				case 2: // '\002'
					k = i7;
					break;

				case 3: // '\003'
					l = i7;
					break;

				case 4: // '\004'
					i1 = i7;
					break;

				case 5: // '\005'
					j1 = i7;
					break;

				case 6: // '\006'
					k1 = i7;
					break;

				case 7: // '\007'
					l1 = i7;
					break;

				case 8: // '\b'
					i2 = i7;
					break;

				case 9: // '\t'
					j2 = i7;
					break;

				case 10: // '\n'
					k2 = i7;
					break;

				case 11: // '\013'
					l2 = i7;
					break;

				case 12: // '\f'
					i3 = i7;
					break;

				case 13: // '\r'
					j3 = i7;
					break;

				case 14: // '\016'
					k3 = i7;
					break;

				case 15: // '\017'
					l3 = i7;
					break;

				case 16: // '\020'
					i4 = i7;
					break;

				case 17: // '\021'
					j4 = i7;
					break;

				case 18: // '\022'
					k4 = i7;
					break;

				case 19: // '\023'
					l4 = i7;
					break;

				case 20: // '\024'
					i5 = i7;
					break;

				case 21: // '\025'
					j5 = i7;
					break;

				case 22: // '\026'
					k5 = i7;
					break;

				case 23: // '\027'
					l5 = i7;
					break;

				case 24: // '\030'
					i6 = i7;
					break;
				}
			}
		}

		int k6 = pc.getId();
		pc.sendPackets(new S_AddSkill(j, k, l, i1, j1, k1, l1, i2, j2, k2, l2,
				i3, j3, k3, l3, i4, j4, k4, l4, i5, j5, k5, l5, i6, 0, 0, 0, 0));
		S_SkillSound s_skillSound = new S_SkillSound(k6, 231);
		pc.sendPackets(s_skillSound);
		pc.broadcastPacket(s_skillSound);
		SkillsTable.getInstance().spellMastery(k6, i, s, 0, 0);
		pc.getInventory().removeItem(l1iteminstance, 1);
	}

	private void SpellBook2(L1PcInstance pc, L1ItemInstance l1iteminstance) {
		String s = "";
		int i = 0;
		int j = 0;
		int k = 0;
		int l = 0;
		int i1 = 0;
		int j1 = 0;
		int k1 = 0;
		int l1 = 0;
		int i2 = 0;
		int j2 = 0;
		int k2 = 0;
		int l2 = 0;
		int i3 = 0;
		int j3 = 0;
		int k3 = 0;
		int l3 = 0;
		int i4 = 0;
		int j4 = 0;
		int k4 = 0;
		int l4 = 0;
		int i5 = 0;
		int j5 = 0;
		int k5 = 0;
		int l5 = 0;
		int i6 = 0;
		for (int j6 = 129; j6 <= 176; j6++) {
			L1Skills l1skills = SkillsTable.getInstance().getTemplate(j6);
			String s1 = "精靈水晶(" + l1skills.getName() + ")";
			if (l1iteminstance.getItem().getName().equalsIgnoreCase(s1)) {
				if (!pc.isGm() && l1skills.getAttr() != 0
						&& pc.getElfAttr() != l1skills.getAttr()) {
					if (pc.getElfAttr() == 0 || pc.getElfAttr() == 1
							|| pc.getElfAttr() == 2 || pc.getElfAttr() == 4
							|| pc.getElfAttr() == 8) { // 属性値が異常な場合は全属性を覚えられるようにしておく
						pc.sendPackets(new S_ServerMessage(79));
						return;
					}
				}
				int l6 = l1skills.getSkillLevel();
				int i7 = l1skills.getId();
				s = l1skills.getName();
				i = l1skills.getSkillId();
				switch (l6) {
				case 1: // '\001'
					j = i7;
					break;

				case 2: // '\002'
					k = i7;
					break;

				case 3: // '\003'
					l = i7;
					break;

				case 4: // '\004'
					i1 = i7;
					break;

				case 5: // '\005'
					j1 = i7;
					break;

				case 6: // '\006'
					k1 = i7;
					break;

				case 7: // '\007'
					l1 = i7;
					break;

				case 8: // '\b'
					i2 = i7;
					break;

				case 9: // '\t'
					j2 = i7;
					break;

				case 10: // '\n'
					k2 = i7;
					break;

				case 11: // '\013'
					l2 = i7;
					break;

				case 12: // '\f'
					i3 = i7;
					break;

				case 13: // '\r'
					j3 = i7;
					break;

				case 14: // '\016'
					k3 = i7;
					break;

				case 15: // '\017'
					l3 = i7;
					break;

				case 16: // '\020'
					i4 = i7;
					break;

				case 17: // '\021'
					j4 = i7;
					break;

				case 18: // '\022'
					k4 = i7;
					break;

				case 19: // '\023'
					l4 = i7;
					break;

				case 20: // '\024'
					i5 = i7;
					break;

				case 21: // '\025'
					j5 = i7;
					break;

				case 22: // '\026'
					k5 = i7;
					break;

				case 23: // '\027'
					l5 = i7;
					break;

				case 24: // '\030'
					i6 = i7;
					break;
				}
			}
		}

		int k6 = pc.getId();
		pc.sendPackets(new S_AddSkill(j, k, l, i1, j1, k1, l1, i2, j2, k2, l2,
				i3, j3, k3, l3, i4, j4, k4, l4, i5, j5, k5, l5, i6, 0, 0, 0, 0));
		S_SkillSound s_skillSound = new S_SkillSound(k6, 224);
		pc.sendPackets(s_skillSound);
		pc.broadcastPacket(s_skillSound);
		SkillsTable.getInstance().spellMastery(k6, i, s, 0, 0);
		pc.getInventory().removeItem(l1iteminstance, 1);
	}

	private void SpellBook3(L1PcInstance pc, L1ItemInstance l1iteminstance,
			ClientThread clientthread) {
		String s = "";
		int i = 0;
		int j = 0;
		int k = 0;
		int l = 0;
		int i1 = 0;
		int j1 = 0;
		int k1 = 0;
		int l1 = 0;
		int i2 = 0;
		int j2 = 0;
		int k2 = 0;
		int l2 = 0;
		int i3 = 0;
		int j3 = 0;
		int k3 = 0;
		int l3 = 0;
		int i4 = 0;
		int j4 = 0;
		int k4 = 0;
		int l4 = 0;
		int i5 = 0;
		int j5 = 0;
		int k5 = 0;
		int l5 = 0;
		int i6 = 0;
		for (int j6 = 87; j6 <= 91; j6++) {
			L1Skills l1skills = SkillsTable.getInstance().getTemplate(j6);
			String s1 = (new StringBuilder()).append("技術書(").append(
					l1skills.getName()).append(")").toString();
			if (l1iteminstance.getItem().getName().equalsIgnoreCase(s1)) {
				int l6 = l1skills.getSkillLevel();
				int i7 = l1skills.getId();
				s = l1skills.getName();
				i = l1skills.getSkillId();
				switch (l6) {
				case 1: // '\001'
					j = i7;
					break;

				case 2: // '\002'
					k = i7;
					break;

				case 3: // '\003'
					l = i7;
					break;

				case 4: // '\004'
					i1 = i7;
					break;

				case 5: // '\005'
					j1 = i7;
					break;

				case 6: // '\006'
					k1 = i7;
					break;

				case 7: // '\007'
					l1 = i7;
					break;

				case 8: // '\b'
					i2 = i7;
					break;

				case 9: // '\t'
					j2 = i7;
					break;

				case 10: // '\n'
					k2 = i7;
					break;

				case 11: // '\013'
					l2 = i7;
					break;

				case 12: // '\f'
					i3 = i7;
					break;

				case 13: // '\r'
					j3 = i7;
					break;

				case 14: // '\016'
					k3 = i7;
					break;

				case 15: // '\017'
					l3 = i7;
					break;

				case 16: // '\020'
					i4 = i7;
					break;

				case 17: // '\021'
					j4 = i7;
					break;

				case 18: // '\022'
					k4 = i7;
					break;

				case 19: // '\023'
					l4 = i7;
					break;

				case 20: // '\024'
					i5 = i7;
					break;

				case 21: // '\025'
					j5 = i7;
					break;

				case 22: // '\026'
					k5 = i7;
					break;

				case 23: // '\027'
					l5 = i7;
					break;

				case 24: // '\030'
					i6 = i7;
					break;
				}
			}
		}

		int k6 = pc.getId();
		pc.sendPackets(new S_AddSkill(j, k, l, i1, j1, k1, l1, i2, j2, k2, l2,
				i3, j3, k3, l3, i4, j4, k4, l4, i5, j5, k5, l5, i6, 0, 0, 0, 0));
		S_SkillSound s_skillSound = new S_SkillSound(k6, 224);
		pc.sendPackets(s_skillSound);
		pc.broadcastPacket(s_skillSound);
		SkillsTable.getInstance().spellMastery(k6, i, s, 0, 0);
		pc.getInventory().removeItem(l1iteminstance, 1);
	}

	private void SpellBook4(L1PcInstance pc, L1ItemInstance l1iteminstance,
			ClientThread clientthread) {
		String s = "";
		int i = 0;
		int j = 0;
		int k = 0;
		int l = 0;
		int i1 = 0;
		int j1 = 0;
		int k1 = 0;
		int l1 = 0;
		int i2 = 0;
		int j2 = 0;
		int k2 = 0;
		int l2 = 0;
		int i3 = 0;
		int j3 = 0;
		int k3 = 0;
		int l3 = 0;
		int i4 = 0;
		int j4 = 0;
		int k4 = 0;
		int l4 = 0;
		int i5 = 0;
		int j5 = 0;
		int k5 = 0;
		int l5 = 0;
		int i6 = 0;
		for (int j6 = 113; j6 < 121; j6++) {
			L1Skills l1skills = SkillsTable.getInstance().getTemplate(j6);
			String s1 = "魔法書 (" + l1skills.getName() + ")";
			if (l1iteminstance.getItem().getName().equalsIgnoreCase(s1)) {
				int l6 = l1skills.getSkillLevel();
				int i7 = l1skills.getId();
				s = l1skills.getName();
				i = l1skills.getSkillId();
				switch (l6) {
				case 1: // '\001'
					j = i7;
					break;

				case 2: // '\002'
					k = i7;
					break;

				case 3: // '\003'
					l = i7;
					break;

				case 4: // '\004'
					i1 = i7;
					break;

				case 5: // '\005'
					j1 = i7;
					break;

				case 6: // '\006'
					k1 = i7;
					break;

				case 7: // '\007'
					l1 = i7;
					break;

				case 8: // '\b'
					i2 = i7;
					break;

				case 9: // '\t'
					j2 = i7;
					break;

				case 10: // '\n'
					k2 = i7;
					break;

				case 11: // '\013'
					l2 = i7;
					break;

				case 12: // '\f'
					i3 = i7;
					break;

				case 13: // '\r'
					j3 = i7;
					break;

				case 14: // '\016'
					k3 = i7;
					break;

				case 15: // '\017'
					l3 = i7;
					break;

				case 16: // '\020'
					i4 = i7;
					break;

				case 17: // '\021'
					j4 = i7;
					break;

				case 18: // '\022'
					k4 = i7;
					break;

				case 19: // '\023'
					l4 = i7;
					break;

				case 20: // '\024'
					i5 = i7;
					break;

				case 21: // '\025'
					j5 = i7;
					break;

				case 22: // '\026'
					k5 = i7;
					break;

				case 23: // '\027'
					l5 = i7;
					break;

				case 24: // '\030'
					i6 = i7;
					break;
				}
			}
		}

		int k6 = pc.getId();
		pc.sendPackets(new S_AddSkill(j, k, l, i1, j1, k1, l1, i2, j2, k2, l2,
				i3, j3, k3, l3, i4, j4, k4, l4, i5, j5, k5, l5, i6, 0, 0, 0, 0));
		S_SkillSound s_skillSound = new S_SkillSound(k6, 224);
		pc.sendPackets(s_skillSound);
		pc.broadcastPacket(s_skillSound);
		SkillsTable.getInstance().spellMastery(k6, i, s, 0, 0);
		pc.getInventory().removeItem(l1iteminstance, 1);
	}
	
	private void SpellBook5(L1PcInstance pc, L1ItemInstance l1iteminstance,
			ClientThread clientthread) {
		String s = "";
		int i = 0;
		int j = 0;
		int k = 0;
		int l = 0;
		int i1 = 0;
		int j1 = 0;
		int k1 = 0;
		int l1 = 0;
		int i2 = 0;
		int j2 = 0;
		int k2 = 0;
		int l2 = 0;
		int i3 = 0;
		int j3 = 0;
		int k3 = 0;
		int l3 = 0;
		int i4 = 0;
		int j4 = 0;
		int k4 = 0;
		int l4 = 0;
		int i5 = 0;
		int j5 = 0;
		int k5 = 0;
		int l5 = 0;
		int i6 = 0;
		int i8 = 0;
		int j8 = 0;
		int k8 = 0;
		int l8 = 0;
		for (int j6 = 181; j6 <= 195; j6++) {
			L1Skills l1skills = SkillsTable.getInstance().getTemplate(j6);
			String s1 = "龍騎士書版 (" + l1skills.getName() + ")";
			if (l1iteminstance.getItem().getName().equalsIgnoreCase(s1)) {
				int l6 = l1skills.getSkillLevel();
				int i7 = l1skills.getId();
				s = l1skills.getName();
				i = l1skills.getSkillId();
				switch (l6) {
				case 1: // '\001'
					j = i7;
					break;

				case 2: // '\002'
					k = i7;
					break;

				case 3: // '\003'
					l = i7;
					break;

				case 4: // '\004'
					i1 = i7;
					break;

				case 5: // '\005'
					j1 = i7;
					break;

				case 6: // '\006'
					k1 = i7;
					break;

				case 7: // '\007'
					l1 = i7;
					break;

				case 8: // '\b'
					i2 = i7;
					break;

				case 9: // '\t'
					j2 = i7;
					break;

				case 10: // '\n'
					k2 = i7;
					break;

				case 11: // '\013'
					l2 = i7;
					break;

				case 12: // '\f'
					i3 = i7;
					break;

				case 13: // '\r'
					j3 = i7;
					break;

				case 14: // '\016'
					k3 = i7;
					break;

				case 15: // '\017'
					l3 = i7;
					break;

				case 16: // '\020'
					i4 = i7;
					break;

				case 17: // '\021'
					j4 = i7;
					break;

				case 18: // '\022'
					k4 = i7;
					break;

				case 19: // '\023'
					l4 = i7;
					break;

				case 20: // '\024'
					i5 = i7;
					break;

				case 21: // '\025'
					j5 = i7;
					break;

				case 22: // '\026'
					k5 = i7;
					break;

				case 23: // '\027'
					l5 = i7;
					break;

				case 24: // '\030'
					i6 = i7;
					break;
					
				case 25: // '\031'
					j8 = i7;
					break;

				case 26: // '\032'
					k8 = i7;
					break;

				case 27: // '\033'
					l8 = i7;
					break;
				case 28: // '\034'
					i8 = i7;
					break;
				}
			}
		}

		int k6 = pc.getId();
		pc.sendPackets(new S_AddSkill(j, k, l, i1, j1, k1, l1, i2, j2, k2, l2,
				i3, j3, k3, l3, i4, j4, k4, l4, i5, j5, k5, l5, i6, j8, k8, l8, i8));
		S_SkillSound s_skillSound = new S_SkillSound(k6, 224);
		pc.sendPackets(s_skillSound);
		pc.broadcastPacket(s_skillSound);
		SkillsTable.getInstance().spellMastery(k6, i, s, 0, 0);
		pc.getInventory().removeItem(l1iteminstance, 1);
	}

	private void SpellBook6(L1PcInstance pc, L1ItemInstance l1iteminstance,
			ClientThread clientthread) {
		String s = "";
		int i = 0;
		int j = 0;
		int k = 0;
		int l = 0;
		int i1 = 0;
		int j1 = 0;
		int k1 = 0;
		int l1 = 0;
		int i2 = 0;
		int j2 = 0;
		int k2 = 0;
		int l2 = 0;
		int i3 = 0;
		int j3 = 0;
		int k3 = 0;
		int l3 = 0;
		int i4 = 0;
		int j4 = 0;
		int k4 = 0;
		int l4 = 0;
		int i5 = 0;
		int j5 = 0;
		int k5 = 0;
		int l5 = 0;
		int i6 = 0;
		int i8 = 0;
		int j8 = 0;
		int k8 = 0;
		int l8 = 0;
		for (int j6 = 201; j6 <= 220; j6++) {
			L1Skills l1skills = SkillsTable.getInstance().getTemplate(j6);
			String s1 = "記憶水晶 (" + l1skills.getName() + ")";
			if (l1iteminstance.getItem().getName().equalsIgnoreCase(s1)) {
				int l6 = l1skills.getSkillLevel();
				int i7 = l1skills.getId();
				s = l1skills.getName();
				i = l1skills.getSkillId();
				switch (l6) {
				case 1: // '\001'
					j = i7;
					break;

				case 2: // '\002'
					k = i7;
					break;

				case 3: // '\003'
					l = i7;
					break;

				case 4: // '\004'
					i1 = i7;
					break;

				case 5: // '\005'
					j1 = i7;
					break;

				case 6: // '\006'
					k1 = i7;
					break;

				case 7: // '\007'
					l1 = i7;
					break;

				case 8: // '\b'
					i2 = i7;
					break;

				case 9: // '\t'
					j2 = i7;
					break;

				case 10: // '\n'
					k2 = i7;
					break;

				case 11: // '\013'
					l2 = i7;
					break;

				case 12: // '\f'
					i3 = i7;
					break;

				case 13: // '\r'
					j3 = i7;
					break;

				case 14: // '\016'
					k3 = i7;
					break;

				case 15: // '\017'
					l3 = i7;
					break;

				case 16: // '\020'
					i4 = i7;
					break;

				case 17: // '\021'
					j4 = i7;
					break;

				case 18: // '\022'
					k4 = i7;
					break;

				case 19: // '\023'
					l4 = i7;
					break;

				case 20: // '\024'
					i5 = i7;
					break;

				case 21: // '\025'
					j5 = i7;
					break;

				case 22: // '\026'
					k5 = i7;
					break;

				case 23: // '\027'
					l5 = i7;
					break;

				case 24: // '\030'
					i6 = i7;
					break;
					
				case 25: // '\031'
					j8 = i7;
					break;

				case 26: // '\032'
					k8 = i7;
					break;

				case 27: // '\033'
					l8 = i7;
					break;
				case 28: // '\034'
					i8 = i7;
					break;
				}
			}
		}

		int k6 = pc.getId();
		pc.sendPackets(new S_AddSkill(j, k, l, i1, j1, k1, l1, i2, j2, k2, l2,
				i3, j3, k3, l3, i4, j4, k4, l4, i5, j5, k5, l5, i6, j8, k8, l8, i8));
		S_SkillSound s_skillSound = new S_SkillSound(k6, 224);
		pc.sendPackets(s_skillSound);
		pc.broadcastPacket(s_skillSound);
		SkillsTable.getInstance().spellMastery(k6, i, s, 0, 0);
		pc.getInventory().removeItem(l1iteminstance, 1);
	}
	
	private void useSpellBook(L1PcInstance pc, L1ItemInstance item,
			int itemId) {
		int itemAttr = 0;
		int locAttr = 0 ; // 0: 其他地方 1 : 正義神殿  2: 邪惡神殿
		boolean isLawful = true;
		int pcX = pc.getX();
		int pcY = pc.getY();
		int mapId = pc.getMapId();
		int level = pc.getLevel();
		if (itemId == 45000 || itemId == 45008 || itemId == 45018
				|| itemId == 45021 || itemId == 40171
				|| itemId == 40179 || itemId == 40180
				|| itemId == 40182 || itemId == 40194
				|| itemId == 40197 || itemId == 40202
				|| itemId == 40206 || itemId == 40213
				|| itemId == 40220 || itemId == 40222) {
			itemAttr = 1;
		}
		if (itemId == 45009 || itemId == 45010 || itemId == 45019
				|| itemId == 40172 || itemId == 40173
				|| itemId == 40178 || itemId == 40185
				|| itemId == 40186 || itemId == 40192
				|| itemId == 40196 || itemId == 40201
				|| itemId == 40204 || itemId == 40211
				|| itemId == 40221 || itemId == 40225) {
			itemAttr = 2;
		}
		// ロウフルテンプル
		if (pcX > 33116 && pcX < 33128 && pcY > 32930 && pcY < 32942
				&& mapId == 4
				|| pcX > 33135 && pcX < 33147 && pcY > 32235 && pcY < 32247
				&& mapId == 4
				|| pcX >= 32783 && pcX <= 32803 && pcY >= 32831 && pcY <= 32851
				&& mapId == 77) {
			locAttr = 1;
			isLawful = true;
		}
		// カオティックテンプル
		if (pcX > 32880 && pcX < 32892 && pcY > 32646 && pcY < 32658
				&& mapId == 4
				|| pcX > 32662
				&& pcX < 32674 && pcY > 32297 && pcY < 32309
				&& mapId == 4) {
			locAttr = 2;
			isLawful = false;
		}
		if (pc.isGm()) {
			SpellBook(pc, item, isLawful);
		} else if ((itemAttr == locAttr || itemAttr == 0) && locAttr != 0) {
			if (pc.isKnight()) {
				if (itemId >= 45000 && itemId <= 45007 && level >= 50) {
					SpellBook(pc, item, isLawful);
				} else if (itemId >= 45000 && itemId <= 45007) {
					pc.sendPackets(new S_ServerMessage(312));
				} else {
					pc.sendPackets(new S_ServerMessage(79));
				}
			} else if (pc.isCrown() || pc.isDarkelf()) {
				if (itemId >= 45000 && itemId <= 45007 && level >= 10) {
					SpellBook(pc, item, isLawful);
				} else if (itemId >= 45008 && itemId <= 45015 && level >= 20) {
					SpellBook(pc, item, isLawful);
				} else if (itemId >= 45008 && itemId <= 45015
						|| itemId >= 45000 && itemId <= 45007) {
					pc.sendPackets(new S_ServerMessage(312)); // レベルが低くてその魔法を覚えることができません。
				} else {
					pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
				}
			} else if (pc.isElf()) {
				if (itemId >= 45000 && itemId <= 45007 && level >= 8) {
					SpellBook(pc, item, isLawful);
				} else if (itemId >= 45008 && itemId <= 45015 && level >= 16) {
					SpellBook(pc, item, isLawful);
				} else if (itemId >= 45016 && itemId <= 45022 && level >= 24) {
					SpellBook(pc, item, isLawful);
				} else if (itemId >= 40170 && itemId <= 40177 && level >= 32) {
					SpellBook(pc, item, isLawful);
				} else if (itemId >= 40178 && itemId <= 40185 && level >= 40) {
					SpellBook(pc, item, isLawful);
				} else if (itemId >= 40186 && itemId <= 40193 && level >= 48) {
					SpellBook(pc, item, isLawful);
				} else if (itemId >= 45000 && itemId <= 45022
						|| itemId >= 40170 && itemId <= 40193) {
					pc.sendPackets(new S_ServerMessage(312)); // レベルが低くてその魔法を覚えることができません。
				} else {
					pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
				}
			} else if (pc.isWizard()) {
				if (itemId >= 45000 && itemId <= 45007 && level >= 4) {
					SpellBook(pc, item, isLawful);
				} else if (itemId >= 45008 && itemId <= 45015 && level >= 8) {
					SpellBook(pc, item, isLawful);
				} else if (itemId >= 45016 && itemId <= 45022 && level >= 12) {
					SpellBook(pc, item, isLawful);
				} else if (itemId >= 40170 && itemId <= 40177 && level >= 16) {
					SpellBook(pc, item, isLawful);
				} else if (itemId >= 40178 && itemId <= 40185 && level >= 20) {
					SpellBook(pc, item, isLawful);
				} else if (itemId >= 40186 && itemId <= 40193 && level >= 24) {
					SpellBook(pc, item, isLawful);
				} else if (itemId >= 40194 && itemId <= 40201 && level >= 28) {
					SpellBook(pc, item, isLawful);
				} else if (itemId >= 40202 && itemId <= 40209 && level >= 32) {
					SpellBook(pc, item, isLawful);
				} else if (itemId >= 40210 && itemId <= 40217 && level >= 36) {
					SpellBook(pc, item, isLawful);
				} else if (itemId >= 40218 && itemId <= 40225 && level >= 40) {
					SpellBook(pc, item, isLawful);
				} else {
					pc.sendPackets(new S_ServerMessage(312)); // レベルが低くてその魔法を覚えることができません。
				}
			}
		} else if (itemAttr != locAttr && itemAttr != 0 && locAttr != 0) {
			// 間違ったテンプルで読んだ場合雷が落ちる
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			S_SkillSound effect = new S_SkillSound(pc.getId(), 10);
			pc.sendPackets(effect);
			pc.broadcastPacket(effect);
			// ダメージは適当
			pc.setCurrentHp(Math.max(pc.getCurrentHp() - 45, 0));
			if (pc.getCurrentHp() <= 0) {
				pc.death(null);
			}
			pc.getInventory().removeItem(item, 1);
		} else {
			pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
		}
	}

	private void useElfSpellBook(L1PcInstance pc, L1ItemInstance item,
			int itemId) {
		int level = pc.getLevel();
		if ((pc.isElf() || pc.isGm()) && isLearnElfMagic(pc)) {
			if (itemId >= 40232 && itemId <= 40234 && level >= 10) {
				SpellBook2(pc, item);
			} else if (itemId >= 40235 && itemId <= 40236 && level >= 20) {
				SpellBook2(pc, item);
			}
			if (itemId >= 40237 && itemId <= 40240 && level >= 30) {
				SpellBook2(pc, item);
			} else if (itemId >= 40241 && itemId <= 40243 && level >= 40) {
				SpellBook2(pc, item);
			} else if (itemId >= 40244 && itemId <= 40246 && level >= 50) {
				SpellBook2(pc, item);
			} else if (itemId >= 40247 && itemId <= 40248 && level >= 30) {
				SpellBook2(pc, item);
			} else if (itemId >= 40249 && itemId <= 40250 && level >= 40) {
				SpellBook2(pc, item);
			} else if (itemId >= 40251 && itemId <= 40252 && level >= 50) {
				SpellBook2(pc, item);
			} else if (itemId == 40253 && level >= 30) {
				SpellBook2(pc, item);
			} else if (itemId == 40254 && level >= 40) {
				SpellBook2(pc, item);
			} else if (itemId == 40255 && level >= 50) {
				SpellBook2(pc, item);
			} else if (itemId == 40256 && level >= 30) {
				SpellBook2(pc, item);
			} else if (itemId == 40257 && level >= 40) {
				SpellBook2(pc, item);
			} else if (itemId >= 40258 && itemId <= 40259 && level >= 50) {
				SpellBook2(pc, item);
			} else if (itemId >= 40260 && itemId <= 40261 && level >= 30) {
				SpellBook2(pc, item);
			} else if (itemId == 40262 && level >= 40) {
				SpellBook2(pc, item);
			} else if (itemId >= 40263 && itemId <= 40264 && level >= 50) {
				SpellBook2(pc, item);
			} else if (itemId >= 41149 && itemId <= 41150 && level >= 50) {
				SpellBook2(pc, item);
			} else if (itemId == 41151 && level >= 40) {
				SpellBook2(pc, item);
			} else if (itemId >= 41152 && itemId <= 41153 && level >= 50) {
				SpellBook2(pc, item);
			}
		} else {
			pc.sendPackets(new S_ServerMessage(79)); // (原文:精霊の水晶はエルフのみが習得できます。)
		}
	}
	
	private void cancelAbsoluteBarrier(L1PcInstance pc) { // アブソルート バリアの解除
		if (pc.hasSkillEffect(ABSOLUTE_BARRIER)) {
			pc.killSkillEffectTimer(ABSOLUTE_BARRIER);
			pc.startHpRegeneration();
			pc.startMpRegeneration();
			pc.startMpRegenerationByDoll();
			pc.startHpRegenerationByDoll();//add 魔法娃娃回血功能
		}
	}
	private void useBlindPotion(L1PcInstance pc) {
		if (pc.hasSkillEffect(DECAY_POTION)) {
			pc.sendPackets(new S_ServerMessage(698)); // \f1魔力によって何も飲むことができません。
			return;
		}

		// アブソルート バリアの解除
		cancelAbsoluteBarrier(pc);

		int time = 16;
		if (pc.hasSkillEffect(CURSE_BLIND)) {
			pc.killSkillEffectTimer(CURSE_BLIND);
		} else if (pc.hasSkillEffect(DARKNESS)) {
			pc.killSkillEffectTimer(DARKNESS);
		}

		if (pc.hasSkillEffect(STATUS_FLOATING_EYE)) {
			pc.sendPackets(new S_CurseBlind(2));
		} else {
			pc.sendPackets(new S_CurseBlind(1));
		}

		pc.setSkillEffect(CURSE_BLIND, time * 1000);
	}
	
	private boolean createNewItem(L1PcInstance pc, int item_id, int count) {
		L1ItemInstance item = ItemTable.getInstance().createItem(item_id);
		item.setCount(count);
		if (item != null) {
			if (pc.getInventory().checkAddItem(item, count) == L1Inventory.OK) {
				pc.getInventory().storeItem(item);
			} else { // 持てない場合は地面に落とす 処理のキャンセルはしない（不正防止）
				L1World.getInstance().getInventory(pc.getX(), pc.getY(),
						pc.getMapId()).storeItem(item);
			}
			pc.sendPackets(new S_ServerMessage(403, item.getLogName())); // %0を手に入れました。
			return true;
		} else {
			return false;
		}
	}
	
	private void usePolyScale(L1PcInstance pc, int itemId) {
		int polyId = 0;
		if (itemId == 41154) { // 闇の鱗
			polyId = 3101;
		} else if (itemId == 41155) { // 烈火の鱗
			polyId = 3126;
		} else if (itemId == 41156) { // 背徳者の鱗
			polyId = 3888;
		} else if (itemId == 41157) { // 憎悪の鱗
			polyId = 3784;
		}
		L1PolyMorph.doPoly(pc, polyId, 600, L1PolyMorph.MORPH_BY_ITEMMAGIC);
	}
	private void usePolyPotion(L1PcInstance pc, int itemId) {
		int polyId = 0;
		if (itemId == 41143) { // ラバーボーンヘッド変身ポーション
			polyId = 6086;
		} else if (itemId == 41144) { // ラバーボーンアーチャー変身ポーション
			polyId = 6087;
		} else if (itemId == 41145) { // ラバーボーンナイフ変身ポーション
			polyId = 6088;
		} else if (itemId == 49149 && pc.get_sex() == 0 && pc.isCrown()) { // シャルナの変身スクロール（レベル30）
			polyId = 6822;
		} else if (itemId == 49149 && pc.get_sex() == 1 && pc.isCrown()) {
			polyId = 6823;
		} else if (itemId == 49149 && pc.get_sex() == 0 && pc.isKnight()) {
			polyId = 6824;
		} else if (itemId == 49149 && pc.get_sex() == 1 && pc.isKnight()) {
			polyId = 6825;
		} else if (itemId == 49149 && pc.get_sex() == 0 && pc.isElf()) {
			polyId = 6826;
		} else if (itemId == 49149 && pc.get_sex() == 1 && pc.isElf()) {
			polyId = 6827;
		} else if (itemId == 49149 && pc.get_sex() == 0 && pc.isWizard()) {
			polyId = 6828;
		} else if (itemId == 49149 && pc.get_sex() == 1 && pc.isWizard()) {
			polyId = 6829;
		} else if (itemId == 49149 && pc.get_sex() == 0 && pc.isDarkelf()) {
			polyId = 6830;
		} else if (itemId == 49149 && pc.get_sex() == 1 && pc.isDarkelf()) {
			polyId = 6831;
		} else if (itemId == 49149 && pc.get_sex() == 0 && pc.isDragonKnight()) {
			polyId = 7139;
		} else if (itemId == 49149 && pc.get_sex() == 1 && pc.isDragonKnight()) {
			polyId = 7140;
		} else if (itemId == 49149 && pc.get_sex() == 0 && pc.isIllusionist()) {
			polyId = 7141;
		} else if (itemId == 49149 && pc.get_sex() == 1 && pc.isIllusionist()) {
			polyId = 7142;
		} else if (itemId == 49150 && pc.get_sex() == 0 && pc.isCrown()) { // シャルナの変身スクロール（レベル40）
			polyId = 6832;
		} else if (itemId == 49150 && pc.get_sex() == 1 && pc.isCrown()) {
			polyId = 6833;
		} else if (itemId == 49150 && pc.get_sex() == 0 && pc.isKnight()) {
			polyId = 6834;
		} else if (itemId == 49150 && pc.get_sex() == 1 && pc.isKnight()) {
			polyId = 6835;
		} else if (itemId == 49150 && pc.get_sex() == 0 && pc.isElf()) {
			polyId = 6836;
		} else if (itemId == 49150 && pc.get_sex() == 1 && pc.isElf()) {
			polyId = 6837;
		} else if (itemId == 49150 && pc.get_sex() == 0 && pc.isWizard()) {
			polyId = 6838;
		} else if (itemId == 49150 && pc.get_sex() == 1 && pc.isWizard()) {
			polyId = 6839;
		} else if (itemId == 49150 && pc.get_sex() == 0 && pc.isDarkelf()) {
			polyId = 6840;
		} else if (itemId == 49150 && pc.get_sex() == 1 && pc.isDarkelf()) {
			polyId = 6841;
		} else if (itemId == 49150 && pc.get_sex() == 0 && pc.isDragonKnight()) {
			polyId = 7143;
		} else if (itemId == 49150 && pc.get_sex() == 1 && pc.isDragonKnight()) {
			polyId = 7144;
		} else if (itemId == 49150 && pc.get_sex() == 0 && pc.isIllusionist()) {
			polyId = 7145;
		} else if (itemId == 49150 && pc.get_sex() == 1 && pc.isIllusionist()) {
			polyId = 7146;
		} else if (itemId == 49151 && pc.get_sex() == 0 && pc.isCrown()) { // シャルナの変身スクロール（レベル52）
			polyId = 6842;
		} else if (itemId == 49151 && pc.get_sex() == 1 && pc.isCrown()) {
			polyId = 6843;
		} else if (itemId == 49151 && pc.get_sex() == 0 && pc.isKnight()) {
			polyId = 6844;
		} else if (itemId == 49151 && pc.get_sex() == 1 && pc.isKnight()) {
			polyId = 6845;
		} else if (itemId == 49151 && pc.get_sex() == 0 && pc.isElf()) {
			polyId = 6846;
		} else if (itemId == 49151 && pc.get_sex() == 1 && pc.isElf()) {
			polyId = 6847;
		} else if (itemId == 49151 && pc.get_sex() == 0 && pc.isWizard()) {
			polyId = 6848;
		} else if (itemId == 49151 && pc.get_sex() == 1 && pc.isWizard()) {
			polyId = 6849;
		} else if (itemId == 49151 && pc.get_sex() == 0 && pc.isDarkelf()) {
			polyId = 6850;
		} else if (itemId == 49151 && pc.get_sex() == 1 && pc.isDarkelf()) {
			polyId = 6851;
		} else if (itemId == 49151 && pc.get_sex() == 0 && pc.isDragonKnight()) {
			polyId = 7147;
		} else if (itemId == 49151 && pc.get_sex() == 1 && pc.isDragonKnight()) {
			polyId = 7148;
		} else if (itemId == 49151 && pc.get_sex() == 0 && pc.isIllusionist()) {
			polyId = 7149;
		} else if (itemId == 49151 && pc.get_sex() == 1 && pc.isIllusionist()) {
			polyId = 7150;
		} else if (itemId == 49152 && pc.get_sex() == 0 && pc.isCrown()) { // シャルナの変身スクロール（レベル55）
			polyId = 6852;
		} else if (itemId == 49152 && pc.get_sex() == 1 && pc.isCrown()) {
			polyId = 6853;
		} else if (itemId == 49152 && pc.get_sex() == 0 && pc.isKnight()) {
			polyId = 6854;
		} else if (itemId == 49152 && pc.get_sex() == 1 && pc.isKnight()) {
			polyId = 6855;
		} else if (itemId == 49152 && pc.get_sex() == 0 && pc.isElf()) {
			polyId = 6856;
		} else if (itemId == 49152 && pc.get_sex() == 1 && pc.isElf()) {
			polyId = 6857;
		} else if (itemId == 49152 && pc.get_sex() == 0 && pc.isWizard()) {
			polyId = 6858;
		} else if (itemId == 49152 && pc.get_sex() == 1 && pc.isWizard()) {
			polyId = 6859;
		} else if (itemId == 49152 && pc.get_sex() == 0 && pc.isDarkelf()) {
			polyId = 6860;
		} else if (itemId == 49152 && pc.get_sex() == 1 && pc.isDarkelf()) {
			polyId = 6861;
		} else if (itemId == 49152 && pc.get_sex() == 0 && pc.isDragonKnight()) {
			polyId = 7151;
		} else if (itemId == 49152 && pc.get_sex() == 1 && pc.isDragonKnight()) {
			polyId = 7152;
		} else if (itemId == 49152 && pc.get_sex() == 0 && pc.isIllusionist()) {
			polyId = 7153;
		} else if (itemId == 49152 && pc.get_sex() == 1 && pc.isIllusionist()) {
			polyId = 7154;
		} else if (itemId == 49153 && pc.get_sex() == 0 && pc.isCrown()) { // シャルナの変身スクロール（レベル60）
			polyId = 6862;
		} else if (itemId == 49153 && pc.get_sex() == 1 && pc.isCrown()) {
			polyId = 6863;
		} else if (itemId == 49153 && pc.get_sex() == 0 && pc.isKnight()) {
			polyId = 6864;
		} else if (itemId == 49153 && pc.get_sex() == 1 && pc.isKnight()) {
			polyId = 6865;
		} else if (itemId == 49153 && pc.get_sex() == 0 && pc.isElf()) {
			polyId = 6866;
		} else if (itemId == 49153 && pc.get_sex() == 1 && pc.isElf()) {
			polyId = 6867;
		} else if (itemId == 49153 && pc.get_sex() == 0 && pc.isWizard()) {
			polyId = 6868;
		} else if (itemId == 49153 && pc.get_sex() == 1 && pc.isWizard()) {
			polyId = 6869;
		} else if (itemId == 49153 && pc.get_sex() == 0 && pc.isDarkelf()) {
			polyId = 6870;
		} else if (itemId == 49153 && pc.get_sex() == 1 && pc.isDarkelf()) {
			polyId = 6871;
		} else if (itemId == 49153 && pc.get_sex() == 0 && pc.isDragonKnight()) {
			polyId = 7155;
		} else if (itemId == 49153 && pc.get_sex() == 1 && pc.isDragonKnight()) {
			polyId = 7156;
		} else if (itemId == 49153 && pc.get_sex() == 0 && pc.isIllusionist()) {
			polyId = 7157;
		} else if (itemId == 49153 && pc.get_sex() == 1 && pc.isIllusionist()) {
			polyId = 7158;
		} else if (itemId == 49154 && pc.get_sex() == 0 && pc.isCrown()) { // シャルナの変身スクロール（レベル65）
			polyId = 6872;
		} else if (itemId == 49154 && pc.get_sex() == 1 && pc.isCrown()) {
			polyId = 6873;
		} else if (itemId == 49154 && pc.get_sex() == 0 && pc.isKnight()) {
			polyId = 6874;
		} else if (itemId == 49154 && pc.get_sex() == 1 && pc.isKnight()) {
			polyId = 6875;
		} else if (itemId == 49154 && pc.get_sex() == 0 && pc.isElf()) {
			polyId = 6876;
		} else if (itemId == 49154 && pc.get_sex() == 1 && pc.isElf()) {
			polyId = 6877;
		} else if (itemId == 49154 && pc.get_sex() == 0 && pc.isWizard()) {
			polyId = 6878;
		} else if (itemId == 49154 && pc.get_sex() == 1 && pc.isWizard()) {
			polyId = 6879;
		} else if (itemId == 49154 && pc.get_sex() == 0 && pc.isDarkelf()) {
			polyId = 6880;
		} else if (itemId == 49154 && pc.get_sex() == 1 && pc.isDarkelf()) {
			polyId = 6881;
		} else if (itemId == 49154 && pc.get_sex() == 0 && pc.isDragonKnight()) {
			polyId = 7159;
		} else if (itemId == 49154 && pc.get_sex() == 1 && pc.isDragonKnight()) {
			polyId = 7160;
		} else if (itemId == 49154 && pc.get_sex() == 0 && pc.isIllusionist()) {
			polyId = 7161;
		} else if (itemId == 49154 && pc.get_sex() == 1 && pc.isIllusionist()) {
			polyId = 7162;
		} else if (itemId == 49155 && pc.get_sex() == 0 && pc.isCrown()) { // シャルナの変身スクロール（レベル70）
			polyId = 6882;
		} else if (itemId == 49155 && pc.get_sex() == 1 && pc.isCrown()) {
			polyId = 6883;
		} else if (itemId == 49155 && pc.get_sex() == 0 && pc.isKnight()) {
			polyId = 6884;
		} else if (itemId == 49155 && pc.get_sex() == 1 && pc.isKnight()) {
			polyId = 6885;
		} else if (itemId == 49155 && pc.get_sex() == 0 && pc.isElf()) {
			polyId = 6886;
		} else if (itemId == 49155 && pc.get_sex() == 1 && pc.isElf()) {
			polyId = 6887;
		} else if (itemId == 49155 && pc.get_sex() == 0 && pc.isWizard()) {
			polyId = 6888;
		} else if (itemId == 49155 && pc.get_sex() == 1 && pc.isWizard()) {
			polyId = 6889;
		} else if (itemId == 49155 && pc.get_sex() == 0 && pc.isDarkelf()) {
			polyId = 6890;
		} else if (itemId == 49155 && pc.get_sex() == 1 && pc.isDarkelf()) {
			polyId = 6891;
		} else if (itemId == 49155 && pc.get_sex() == 0 && pc.isDragonKnight()) {
			polyId = 7163;
		} else if (itemId == 49155 && pc.get_sex() == 1 && pc.isDragonKnight()) {
			polyId = 7164;
		} else if (itemId == 49155 && pc.get_sex() == 0 && pc.isIllusionist()) {
			polyId = 7165;
		} else if (itemId == 49155 && pc.get_sex() == 1 && pc.isIllusionist()) {
			polyId = 7166;
		}
		L1PolyMorph.doPoly(pc, polyId, 1800, L1PolyMorph.MORPH_BY_ITEMMAGIC);
	}
	
	private boolean writeLetter(int itemId, L1PcInstance pc, int letterCode,
			String letterReceiver, byte[] letterText) {

		int newItemId = 0;
		if (itemId == 40310) {
			newItemId = 49016;
		} else if (itemId == 40730) {
			newItemId = 49020;
		} else if (itemId == 40731) {
			newItemId = 49022;
		} else if (itemId == 40732) {
			newItemId = 49024;
		}
		L1ItemInstance item = ItemTable.getInstance().createItem(newItemId);
		item.setCount(1);
		if (item == null) {
			return false;
		}

		if (sendLetter(pc, letterReceiver, item, true)) {
			saveLetter(item.getId(), letterCode, pc.getName(), letterReceiver,
					letterText);
		} else {
			return false;
		}
		return true;
	}

	private boolean writeClanLetter(int itemId, L1PcInstance pc,
			int letterCode, String letterReceiver, byte[] letterText) {
		L1Clan targetClan = null;
		for (L1Clan clan : L1World.getInstance().getAllClans()) {
			if (clan.getClanName().toLowerCase().equals(
					letterReceiver.toLowerCase())) {
				targetClan = clan;
				break;
			}
		}
		if (targetClan == null) {
			pc.sendPackets(new S_ServerMessage(434)); // 受信者がいません。
			return false;
		}

		String memberName[] = targetClan.getAllMembers();
		for (int i = 0; i < memberName.length; i++) {
			L1ItemInstance item = ItemTable.getInstance().createItem(49016);
			item.setCount(1);
			if (item == null) {
				return false;
			}
			if (sendLetter(pc, memberName[i], item, false)) {
				saveLetter(item.getId(), letterCode, pc.getName(),
						memberName[i], letterText);
			}
		}
		return true;
	}
	
	private boolean sendLetter(L1PcInstance pc, String name,
			L1ItemInstance item, boolean isFailureMessage) {
		L1PcInstance target = L1World.getInstance().getPlayer(name);
		if (target != null) {
			if (target.getInventory().checkAddItem(item, 1) == L1Inventory.OK) {
				target.getInventory().storeItem(item);
				target.sendPackets(new S_SkillSound(target.getId(), 1091));
				target.sendPackets(new S_ServerMessage(428)); // 手紙が届きました。
			} else {
				if (isFailureMessage) {
					// 相手のアイテムが重すぎるため、これ以上あげられません。
					pc.sendPackets(new S_ServerMessage(942));
				}
				return false;
			}
		} else {
			if (CharacterTable.doesCharNameExist(name)) {
				try {
					int targetId = CharacterTable.getInstance()
							.restoreCharacter(name).getId();
					CharactersItemStorage storage = CharactersItemStorage
							.create();
					if (storage.getItemCount(targetId) < 180) {
						storage.storeItem(targetId, item);
					} else {
						if (isFailureMessage) {
							// 相手のアイテムが重すぎるため、これ以上あげられません。
							pc.sendPackets(new S_ServerMessage(942));
						}
						return false;
					}
				} catch (Exception e) {
				}
			} else {
				if (isFailureMessage) {
					pc.sendPackets(new S_ServerMessage(109, name)); // %0という名前の人はいません。
				}
				return false;
			}
		}
		return true;
	}

	private void saveLetter(int itemObjectId, int code, String sender,
			String receiver, byte[] text) {
		// 日付を取得する
		SimpleDateFormat sdf = new SimpleDateFormat("yy/MM/dd");
		TimeZone tz = TimeZone.getTimeZone(Config.TIME_ZONE);
		String date = sdf.format(Calendar.getInstance(tz).getTime());

		// subjectとcontentの区切り(0x00 0x00)位置を見つける
		int spacePosition1 = 0;
		int spacePosition2 = 0;
		for (int i = 0; i < text.length; i += 2) {
			if (text[i] == 0 && text[i + 1] == 0) {
				if (spacePosition1 == 0) {
					spacePosition1 = i;
				} else if (spacePosition1 != 0 && spacePosition2 == 0) {
					spacePosition2 = i;
					break;
				}
			}
		}

		// letterテーブルに書き込む
		int subjectLength = spacePosition1 + 2;
		int contentLength = spacePosition2 - spacePosition1;
		if (contentLength <= 0) {
			contentLength = 1;
		}
		byte[] subject = new byte[subjectLength];
		byte[] content = new byte[contentLength];
		System.arraycopy(text, 0, subject, 0, subjectLength);
		System.arraycopy(text, subjectLength, content, 0, contentLength);
		LetterTable.getInstance().writeLetter(itemObjectId, code, sender,
				receiver, date, 0, subject, content);
	}
	private boolean withdrawPet(L1PcInstance pc, int itemObjectId) {
		if (!pc.getMap().isTakePets()) {
			pc.sendPackets(new S_ServerMessage(563)); // \f1ここでは使えません。
			return false;
		}

		int petCost = 0;
		Object[] petList = pc.getPetList().values().toArray();
		for (Object pet : petList) {
			if (pet instanceof L1PetInstance) {
				if (((L1PetInstance) pet).getItemObjId() == itemObjectId) { // 既に引き出しているペット
					return false;
				}
			}
			petCost += ((L1NpcInstance) pet).getPetcost();
		}
		int charisma = pc.getCha();
		if (pc.isCrown()) { // 君主
			charisma += 6;
		} else if (pc.isElf()) { // エルフ
			charisma += 12;
		} else if (pc.isWizard()) { // WIZ
			charisma += 6;
		} else if (pc.isDarkelf()) { // DE
			charisma += 6;
		} else if (pc.isDragonKnight()) { // ドラゴンナイト
			charisma += 6;
		} else if (pc.isIllusionist()) { // イリュージョニスト
			charisma += 6;
		}
		charisma -= petCost;
		int petCount = charisma / 6;
		if (petCount <= 0) {
			pc.sendPackets(new S_ServerMessage(489)); // 引き取ろうとするペットが多すぎます。
			return false;
		}

		L1Pet l1pet = PetTable.getInstance().getTemplate(itemObjectId);
		if (l1pet != null) {
			L1Npc npcTemp = NpcTable.getInstance().getTemplate(
					l1pet.get_npcid());
			L1PetInstance pet = new L1PetInstance(npcTemp, pc, l1pet);
			pet.setPetcost(6);
		}
		return true;
	}
}
