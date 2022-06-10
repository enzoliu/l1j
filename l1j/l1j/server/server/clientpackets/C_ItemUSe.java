/*
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA
 * 02111-1307, USA.
 *
 * http://www.gnu.org/copyleft/gpl.html
 */
package l1j.server.server.clientpackets;

import java.sql.Timestamp;
import java.util.Calendar;
import java.util.logging.Logger;

import l1j.server.server.ClientThread;
import l1j.server.server.Opcodes;
import l1j.server.server.datatables.NpcTable;
import l1j.server.server.model.L1ItemDelay;
import l1j.server.server.model.L1PcInventory;
import l1j.server.server.model.L1PolyMorph;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.item.L1EtcGame;
import l1j.server.server.model.item.L1EtcPotion;
import l1j.server.server.model.item.L1EtcScroll;
import l1j.server.server.model.item.L1Food;
import l1j.server.server.model.item.L1ItemEtc;
import l1j.server.server.model.item.L1ItemQuest;
import l1j.server.server.model.item.L1Potion;
import l1j.server.server.model.item.L1Scroll;
import l1j.server.server.model.item.L1TreasureBox;
import l1j.server.server.serverpackets.S_ItemName;
import l1j.server.server.serverpackets.S_OwnCharAttrDef;
import l1j.server.server.serverpackets.S_OwnCharStatus;
import l1j.server.server.serverpackets.S_PacketBox;
import l1j.server.server.serverpackets.S_Paralysis;
import l1j.server.server.serverpackets.S_SPMR;
import l1j.server.server.serverpackets.S_ScrollShopBuyList;
import l1j.server.server.serverpackets.S_ScrollShopSellList;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1Armor;
import l1j.server.server.templates.L1EtcItem;
import static l1j.server.server.model.skill.L1SkillId.*;

/*
 * read_type
 *  0:預設
 * 	1:變卷
 *  2:強化卷軸類
 *  3:傳送卷軸類
 *  4:空的魔法卷軸
 *  5:對自己施法的卷軸
 *  6:復活卷軸類
 *  7:信紙類
 *  8:料理書
 *  9:釣竿
 */
public class C_ItemUSe extends ClientBasePacket {

	private static final String C_ITEM_USE = "[C] C_ItemUSe";
	private static Logger _log = Logger.getLogger(C_ItemUSe.class.getName());

	public C_ItemUSe(byte abyte0[], ClientThread client) throws Exception {
		super(abyte0);
		int itemObjid = readD();
		int itemId;
		L1PcInstance pc = client.getActiveChar();
		if (pc.isGhost()) {
			return;
		}
		L1ItemInstance l1iteminstance = pc.getInventory().getItem(itemObjid);
		
		int readtype = 0;
		String use_class = "";
		if(l1iteminstance.getItem() instanceof L1EtcItem){
			readtype = ((L1EtcItem) l1iteminstance.getItem()).get_read_type();
			use_class = ((L1EtcItem) l1iteminstance.getItem()).get_use_class();
		}
		//使用無效
		if (l1iteminstance.getItem().getUseType() == -1) {
			pc.sendPackets(new S_ServerMessage(74, l1iteminstance.getLogName())); 
			return;
		}
		//傳送中不可使用
		if (pc.isTeleport()) {
			return;
		}
		//角色死亡或使用道具對象為空
		if (l1iteminstance == null || pc.isDead()) {
			return;
		}
		//不可使用之道具
		if (!pc.getMap().isUsableItem()) {
			pc.sendPackets(new S_ServerMessage(563)); 
			return;
		}
		//取得item id
		try {
			itemId = l1iteminstance.getItem().getItemId();
		} catch (Exception e) {
			return;
		}
		int l = 0;
		String s = "";
		int btele = 0;
		int blanksc_skillid = 0;
		int spellsc_objid = 0;
		int spellsc_x = 0;
		int spellsc_y = 0;
		int resid = 0;
		int letterCode = 0;
		String letterReceiver = "";
		byte[] letterText = null;
		int cookStatus = 0;
		int cookNo = 0;
		int fishX = 0;
		int fishY = 0;
		int use_type = l1iteminstance.getItem().getUseType();
		//新增商店捲軸 by eric1300460
		try{
			/*
			 * 17:scroll shop
			 */
			if(l1iteminstance.getItem().getType()==17 && use_type==0){
				int npcId = Integer.parseInt(l1iteminstance.getItem().getName());
				pc.sendPackets(new S_ScrollShopSellList(NpcTable.getInstance().getTemplate(npcId)));
				return;
			}else if(l1iteminstance.getItem().getType()==18 && use_type==0){
				int npcId = Integer.parseInt(l1iteminstance.getItem().getName());
				pc.sendPackets(new S_ScrollShopBuyList(NpcTable.getInstance().getTemplate(npcId),pc));
				return;
			}
		}catch(NumberFormatException e){
			System.out.println("商店捲軸DB有誤 物品ID:"+l1iteminstance.getItem().getItemId());
			e.printStackTrace();
		}
		if (readtype == 1) {
			s = readS();
		} else if (readtype == 2 || use_type == 26) {
			l = readD();
		} else if (readtype == 3) {
			readH();//map id
			btele = readD();
		} else if (readtype == 4) {
			blanksc_skillid = readC();
		} else if (readtype == 5 || use_type == 30) {
			spellsc_objid = readD();
		} else if (use_type == 5 || use_type == 17) { // spell_long、spell_short
			spellsc_objid = readD();
			spellsc_x = readH();
			spellsc_y = readH();
		} else if (readtype == 6) {
			resid = readD();
		} else if (readtype == 7) {
			letterCode = readH();
			letterReceiver = readS();
			letterText = readByte();
		} else if (readtype == 8) {
			cookStatus = readC();
			cookNo = readC();
		} else if (readtype == 9) {
			fishX = readH();
			fishY = readH();
		} else {
			l = readC();
		}
		//確認使用的玩家是活的
		if (pc.getCurrentHp() > 0) {
			int delay_id = 0;
			if (l1iteminstance.getItem().getType2() == 0) {
				delay_id = ((L1EtcItem) l1iteminstance.getItem()).get_delayid();
			}
			if (delay_id != 0) {
				if (pc.hasItemDelay(delay_id) == true) {
					//傳送位移對策:使用S_Paralysis.TYPE_TELEPORT
					//延伸問題:在傳送期間再次使用會使畫面卡住
					//延伸問題對策:於鎖定期間再次使用item的地方解除鎖定，因此道具鎖定時間需大於Config.TELEPORT_LOCK_TIME。
					pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_TELEPORT, false));
					return;
				}
			}
			//計算延遲時間
			boolean isDelayEffect = false;
			if (l1iteminstance.getItem().getType2() == 0) {
				int delayEffect = ((L1EtcItem) l1iteminstance.getItem()).get_delayEffect();
				if (delayEffect > 0) {
					isDelayEffect = true;
					Timestamp lastUsed = l1iteminstance.getLastUsed();
					if (lastUsed != null) {
						Calendar cal = Calendar.getInstance();
						if ((cal.getTimeInMillis() - lastUsed.getTime()) / 1000 <= delayEffect) {
							pc.sendPackets(new S_ServerMessage(79));
							return;
						}
					}
				}
			}
			_log.finest("request item use (obj) = " + itemObjid + " action = " + l + " value = " + s);
			if (l1iteminstance.getItem().getType2() == 0) { //etcitem
				L1ItemInstance l1iteminstance1 = pc.getInventory().getItem(l);
				int item_minlvl = ((L1EtcItem) l1iteminstance.getItem()).getMinLevel();
				int item_maxlvl = ((L1EtcItem) l1iteminstance.getItem()).getMaxLevel();
				if (item_minlvl != 0 && item_minlvl > pc.getLevel()	&& !pc.isGm()) {
					pc.sendPackets(new S_ServerMessage(318, String.valueOf(item_minlvl))); // このアイテムは%0レベル以上にならなければ使用できません。
					return;
				} else if (item_maxlvl != 0 && item_maxlvl < pc.getLevel() && !pc.isGm()) {
					pc.sendPackets(new S_ServerMessage(673, String.valueOf(item_maxlvl))); // このアイテムは%dレベル以上のみ使用できます。
					return;
				}
				if (l1iteminstance.getItem().getType() == 0) { //arrow
					pc.getInventory().setArrow(l1iteminstance.getItem().getItemId());
					pc.sendPackets(new S_ServerMessage(452, l1iteminstance.getLogName())); // %0が選択されました。
				} else if (l1iteminstance.getItem().getType() == 15) { //sting
					pc.getInventory().setSting(l1iteminstance.getItem().getItemId());
					pc.sendPackets(new S_ServerMessage(452,l1iteminstance.getLogName()));
				} else if (l1iteminstance.getItem().getType() == 16) { //treasure_box
					L1TreasureBox box = L1TreasureBox.get(itemId);
					if (box != null) {
						if (box.open(pc)) {
							L1EtcItem temp = (L1EtcItem) l1iteminstance.getItem();
							if (temp.get_delayEffect() > 0) {
								isDelayEffect = true;
							} else {
								pc.getInventory().removeItem(l1iteminstance.getId(), 1);
							}
						}
					}
				} else if (l1iteminstance.getItem().getType() == 2) { // light
					if (l1iteminstance.getRemainingTime() <= 0 && itemId != 40004) {
						return;
					}
					if (l1iteminstance.isNowLighting()) {
						l1iteminstance.setNowLighting(false);
						pc.turnOnOffLight();
					} else {
						l1iteminstance.setNowLighting(true);
						pc.turnOnOffLight();
					}
					pc.sendPackets(new S_ItemName(l1iteminstance));
				} else if(use_class.equalsIgnoreCase("potion")){
					L1Potion Potion = new L1Potion();
					Potion.use(pc, client, l1iteminstance1, l1iteminstance, itemId);
				} else if(use_class.equalsIgnoreCase("scroll")){
					L1Scroll scroll = new L1Scroll();
					scroll.use(pc, client, l1iteminstance1, l1iteminstance, itemId, btele);
				} else if(use_class.equalsIgnoreCase("etc_scroll")){
					L1EtcScroll etcScroll = new L1EtcScroll();
					etcScroll.use(pc, client, l1iteminstance1, l1iteminstance, itemId, s, blanksc_skillid, spellsc_objid, spellsc_x, spellsc_y,resid,l);
				} else if(use_class.equalsIgnoreCase("etc_potion")){
					L1EtcPotion etcPotion = new L1EtcPotion();
					etcPotion.use(pc, client, l1iteminstance1, l1iteminstance, itemId);
				} else if(use_class.equalsIgnoreCase("etc_game")){
					L1EtcGame etcGame = new L1EtcGame();
					etcGame.use(pc, client, l1iteminstance1, l1iteminstance, itemId);
				} else if(use_class.equalsIgnoreCase("quest")){
					L1ItemQuest itemQuest = new L1ItemQuest();
					itemQuest.use(pc, client, l1iteminstance1, l1iteminstance, itemId);
				} else if(use_class.equalsIgnoreCase("etc")){
					L1ItemEtc itemEtc = new L1ItemEtc();
					itemEtc.use(pc, client, l1iteminstance1, l1iteminstance, itemId, letterCode, letterReceiver, letterText, itemObjid, spellsc_objid, spellsc_x, spellsc_y, fishX, fishY,cookStatus,cookNo);
				} else if(use_class.equalsIgnoreCase("food")){
					L1Food Food = new L1Food();
					Food.use(pc, client, l1iteminstance1, l1iteminstance, itemId);
				}
			} else if (l1iteminstance.getItem().getType2() == 1) {
				// 種別：武器
				int min = l1iteminstance.getItem().getMinLevel();
				int max = l1iteminstance.getItem().getMaxLevel();
				if (min != 0 && min > pc.getLevel()) {
					// このアイテムは%0レベル以上にならなければ使用できません。
					pc.sendPackets(new S_ServerMessage(318, String.valueOf(min)));
				} else if (max != 0 && max < pc.getLevel()) {
					// このアイテムは%dレベル以下のみ使用できます。
					// S_ServerMessageでは引数が表示されない
					if (max < 50) {
						pc.sendPackets(new S_PacketBox(S_PacketBox.MSG_LEVEL_OVER, max));
					} else {
						pc.sendPackets(new S_SystemMessage("等級 " + max	+ " 以下才可使用此道具。"));
					}
				} else {
					if (pc.isCrown() && l1iteminstance.getItem().isUseRoyal()
							|| pc.isKnight()
							&& l1iteminstance.getItem().isUseKnight()
							|| pc.isElf()
							&& l1iteminstance.getItem().isUseElf()
							|| pc.isWizard()
							&& l1iteminstance.getItem().isUseMage()
							|| pc.isDarkelf()
							&& l1iteminstance.getItem().isUseDarkelf()
							|| pc.isDragonKnight()
							&& l1iteminstance.getItem().isUseDragonknight()
							|| pc.isIllusionist()
							&& l1iteminstance.getItem().isUseIllusionist()) {
						UseWeapon(pc, l1iteminstance);
					} else {
						// \f1あなたのクラスではこのアイテムは使用できません。
						pc.sendPackets(new S_ServerMessage(264));
					}
				}
			} else if (l1iteminstance.getItem().getType2() == 2) { // 種別：防具
				if (pc.isCrown() && l1iteminstance.getItem().isUseRoyal()
						|| pc.isKnight()
						&& l1iteminstance.getItem().isUseKnight() || pc.isElf()
						&& l1iteminstance.getItem().isUseElf() || pc.isWizard()
						&& l1iteminstance.getItem().isUseMage()
						|| pc.isDarkelf()
						&& l1iteminstance.getItem().isUseDarkelf()
						|| pc.isDragonKnight()
						&& l1iteminstance.getItem().isUseDragonknight()
						|| pc.isIllusionist()
						&& l1iteminstance.getItem().isUseIllusionist()) {

					int min = ((L1Armor) l1iteminstance.getItem())
							.getMinLevel();
					int max = ((L1Armor) l1iteminstance.getItem())
							.getMaxLevel();
					if (min != 0 && min > pc.getLevel()) {
						// このアイテムは%0レベル以上にならなければ使用できません。
						pc.sendPackets(new S_ServerMessage(318, String
								.valueOf(min)));
					} else if (max != 0 && max < pc.getLevel()) {
						// このアイテムは%dレベル以下のみ使用できます。
						// S_ServerMessageでは引数が表示されない
						if (max < 50) {
							pc.sendPackets(new S_PacketBox(
									S_PacketBox.MSG_LEVEL_OVER, max));
						} else {
							pc.sendPackets(new S_SystemMessage("等級 " + max
									+ " 以下才可使用此道具。"));
						}
					} else {
						UseArmor(pc, l1iteminstance);
					}
				} else {
					// \f1あなたのクラスではこのアイテムは使用できません。
					pc.sendPackets(new S_ServerMessage(264));
				}
			}

			// 効果ディレイがある場合は現在時間をセット
			if (isDelayEffect) {
				Timestamp ts = new Timestamp(System.currentTimeMillis());
				l1iteminstance.setLastUsed(ts);
				pc.getInventory().updateItem(l1iteminstance,
						L1PcInventory.COL_DELAY_EFFECT);
				pc.getInventory().saveItem(l1iteminstance,
						L1PcInventory.COL_DELAY_EFFECT);
			}

			L1ItemDelay.onItemUse(client, l1iteminstance); // アイテムディレイ開始
		}
	}


	private void UseArmor(L1PcInstance activeChar, L1ItemInstance armor) {
		int type = armor.getItem().getType();
		L1PcInventory pcInventory = activeChar.getInventory();
		boolean equipeSpace; // 装備する箇所が空いているか
		if (type == 9) { // リングの場合
			equipeSpace = pcInventory.getTypeEquipped(2, 9) <= 1;
		} else {
			equipeSpace = pcInventory.getTypeEquipped(2, type) <= 0;
		}

		if (equipeSpace && !armor.isEquipped()) { // 使用した防具を装備していなくて、その装備箇所が空いている場合（装着を試みる）
			int polyid = activeChar.getTempCharGfx();

			if (!L1PolyMorph.isEquipableArmor(polyid, type)) { // その変身では装備不可
				return;
			}

			if (type == 13 && pcInventory.getTypeEquipped(2, 7) >= 1
					|| type == 7 && pcInventory.getTypeEquipped(2, 13) >= 1) { // シールド、ガーダー同時裝備不可
				activeChar.sendPackets(new S_ServerMessage(124)); // \f1すでに何かを装備しています。
				return;
			}
			if (type == 7 && activeChar.getWeapon() != null) { // シールドの場合、武器を装備していたら両手武器チェック
				if (activeChar.getWeapon().getItem().isTwohandedWeapon()) { // 両手武器
					activeChar.sendPackets(new S_ServerMessage(129)); // \f1両手の武器を武装したままシールドを着用することはできません。
					return;
				}
			}

			if (type == 3 && pcInventory.getTypeEquipped(2, 4) >= 1) { // シャツの場合、マントを着てないか確認
				activeChar
						.sendPackets(new S_ServerMessage(126, "$224", "$225")); // \f1%1上に%0を着ることはできません。
				return;
			} else if ((type == 3) && pcInventory.getTypeEquipped(2, 2) >= 1) { // シャツの場合、メイルを着てないか確認
				activeChar
						.sendPackets(new S_ServerMessage(126, "$224", "$226")); // \f1%1上に%0を着ることはできません。
				return;
			} else if ((type == 2) && pcInventory.getTypeEquipped(2, 4) >= 1) { // メイルの場合、マントを着てないか確認
				activeChar
						.sendPackets(new S_ServerMessage(126, "$226", "$225")); // \f1%1上に%0を着ることはできません。
				return;
			}

			cancelAbsoluteBarrier(activeChar); // アブソルート バリアの解除

			pcInventory.setEquipped(armor, true);
		} else if (armor.isEquipped()) { // 使用した防具を装備していた場合（脱着を試みる）
			if (armor.getItem().getBless() == 2) { // 呪われていた場合
				activeChar.sendPackets(new S_ServerMessage(150)); // \f1はずすことができません。呪いをかけられているようです。
				return;
			}
			if (type == 3 && pcInventory.getTypeEquipped(2, 2) >= 1) { // シャツの場合、メイルを着てないか確認
				activeChar.sendPackets(new S_ServerMessage(127)); // \f1それは脱ぐことができません。
				return;
			} else if ((type == 2 || type == 3)
					&& pcInventory.getTypeEquipped(2, 4) >= 1) { // シャツとメイルの場合、マントを着てないか確認
				activeChar.sendPackets(new S_ServerMessage(127)); // \f1それは脱ぐことができません。
				return;
			}

			pcInventory.setEquipped(armor, false);
		} else {
			activeChar.sendPackets(new S_ServerMessage(124)); // \f1すでに何かを装備しています。
		}
		// セット装備用HP、MP、MR更新
		activeChar.setCurrentHp(activeChar.getCurrentHp());
		activeChar.setCurrentMp(activeChar.getCurrentMp());
		activeChar.sendPackets(new S_OwnCharAttrDef(activeChar));
		activeChar.sendPackets(new S_OwnCharStatus(activeChar));
		activeChar.sendPackets(new S_SPMR(activeChar));
	}

	private void UseWeapon(L1PcInstance activeChar, L1ItemInstance weapon) {
		L1PcInventory pcInventory = activeChar.getInventory();
		if (activeChar.getWeapon() == null || !activeChar.getWeapon().equals(weapon)) { // 指定された武器が装備している武器と違う場合、装備できるか確認
			int weapon_type = weapon.getItem().getType();
			int polyid = activeChar.getTempCharGfx();

			if (!L1PolyMorph.isEquipableWeapon(polyid, weapon_type)) { // その変身では装備不可
				return;
			}
			if (weapon.getItem().isTwohandedWeapon()
					&& pcInventory.getTypeEquipped(2, 7) >= 1) { // 両手武器の場合、シールド装備の確認
				activeChar.sendPackets(new S_ServerMessage(128)); // \f1シールドを装備している時は両手で持つ武器を使うことはできません。
				return;
			}
		}

		cancelAbsoluteBarrier(activeChar); // アブソルート バリアの解除

		if (activeChar.getWeapon() != null) { // 既に何かを装備している場合、前の装備をはずす
			if (activeChar.getWeapon().getItem().getBless() == 2) { // 呪われていた場合
				activeChar.sendPackets(new S_ServerMessage(150)); // \f1はずすことができません。呪いをかけられているようです。
				return;
			}
			if (activeChar.getWeapon().equals(weapon)) {
				// 装備交換ではなく外すだけ
				pcInventory.setEquipped(activeChar.getWeapon(), false, false,false);
				return;
			} else {
				pcInventory.setEquipped(activeChar.getWeapon(), false, false, true);
			}
		}

		if (weapon.getItemId() == 200002) { // 呪われたダイスダガー
			activeChar.sendPackets(new S_ServerMessage(149, weapon.getLogName())); // \f1%0が手にくっつきました。
		}
		pcInventory.setEquipped(weapon, true, false, false);
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


	@Override
	public String getType() {
		return C_ITEM_USE;
	}
}
