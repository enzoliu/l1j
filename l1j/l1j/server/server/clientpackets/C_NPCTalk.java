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

package l1j.server.server.clientpackets;

import static l1j.server.server.model.skill.L1SkillId.ABSOLUTE_BARRIER;
import static l1j.server.server.model.skill.L1SkillId.ENTANGLE;
import static l1j.server.server.model.skill.L1SkillId.GREATER_HASTE;
import static l1j.server.server.model.skill.L1SkillId.HASTE;
import static l1j.server.server.model.skill.L1SkillId.HOLY_WALK;
import static l1j.server.server.model.skill.L1SkillId.MASS_SLOW;
import static l1j.server.server.model.skill.L1SkillId.MOVING_ACCELERATION;
import static l1j.server.server.model.skill.L1SkillId.SLOW;
import static l1j.server.server.model.skill.L1SkillId.STATUS_BRAVE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_ELFBRAVE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_HASTE;
import static l1j.server.server.model.skill.L1SkillId.STATUS_RIBRAVE;
import static l1j.server.server.model.skill.L1SkillId.WIND_WALK;

import java.util.logging.Logger;

import l1j.server.server.ClientThread;
import l1j.server.server.datatables.ItemTable;
import l1j.server.server.datatables.NpcActionTable;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.npc.L1NpcHtml;
import l1j.server.server.model.npc.action.L1NpcAction;
import l1j.server.server.model.skill.L1SkillUse;
import static l1j.server.server.model.skill.L1SkillId.*;
import l1j.server.server.serverpackets.S_NPCTalkReturn;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillBrave;
import l1j.server.server.serverpackets.S_SkillHaste;
import l1j.server.server.serverpackets.S_SkillSound;
import l1j.server.server.serverpackets.S_SystemMessage;
// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket, C_NPCTalk

public class C_NPCTalk extends ClientBasePacket {

	private static final String C_NPC_TALK = "[C] C_NPCTalk";
	private static Logger _log = Logger.getLogger(C_NPCTalk.class.getName());

	public C_NPCTalk(byte abyte0[], ClientThread client)
			throws Exception {
		super(abyte0);
		int objid = readD();
		L1Object obj = L1World.getInstance().findObject(objid);
		L1PcInstance pc = client.getActiveChar();
		if (obj != null && pc != null) {
			L1NpcAction action = NpcActionTable.getInstance().get(pc, obj);
			if (action != null) {
				L1NpcHtml html = action.execute("", pc, obj, new byte[0]);
				if (html != null) {
					pc.sendPackets(new S_NPCTalkReturn(obj.getId(), html));
				}
				return;
			}
			if(obj instanceof L1NpcInstance){
				//加天神祝福NPC by eric1300460
				if(((L1NpcInstance)obj).getNpcId()==9999991){
					if (pc.hasSkillEffect(71) == true) { // ディケイポーションの状態
						pc.sendPackets(new S_ServerMessage(698)); // \f1魔力によって何も飲むことができません。
						return;
					}
					int time = 3600;//1小時
					if(((L1NpcInstance)obj).getMapId()==4){//大陸NPC要收費
						L1ItemInstance adena = ItemTable.getInstance().createItem(40308);
						adena.setCount(pc.getInventory().countItems(40308));
						if(adena.getCount()<50000){//小於五萬
							pc.sendPackets(new S_SystemMessage("金幣不足!要5萬元唷!天神祝福不是平常人能有的!"));
							return;
						}else{
							pc.getInventory().consumeItem(40308, 50000);
							time = 900;//15分鐘
						}
					}
					
					// アブソルート バリアの解除
					if (pc.hasSkillEffect(ABSOLUTE_BARRIER)) {
						pc.killSkillEffectTimer(ABSOLUTE_BARRIER);
						pc.startHpRegeneration();
						pc.startMpRegeneration();
						pc.startMpRegenerationByDoll();
					}
					//勇水
					if (pc.hasSkillEffect(STATUS_ELFBRAVE)) { // エルヴンワッフルとは重複しない
						pc.killSkillEffectTimer(STATUS_ELFBRAVE);
						pc.sendPackets(new S_SkillBrave(pc.getId(), 0, 0));
						pc.broadcastPacket(new S_SkillBrave(pc.getId(), 0, 0));
						pc.setBraveSpeed(0);
					}
					if (pc.hasSkillEffect(HOLY_WALK)) { // ホーリーウォークとは重複しない
						pc.killSkillEffectTimer(HOLY_WALK);
						pc.sendPackets(new S_SkillBrave(pc.getId(), 0, 0));
						pc.broadcastPacket(new S_SkillBrave(pc.getId(), 0, 0));
						pc.setBraveSpeed(0);
					}
					if (pc.hasSkillEffect(MOVING_ACCELERATION)) { // ムービングアクセレーションとは重複しない
						pc.killSkillEffectTimer(MOVING_ACCELERATION);
						pc.sendPackets(new S_SkillBrave(pc.getId(), 0, 0));
						pc.broadcastPacket(new S_SkillBrave(pc.getId(), 0, 0));
						pc.setBraveSpeed(0);
					}
					if (pc.hasSkillEffect(WIND_WALK)) { // ウィンドウォークとは重複しない
						pc.killSkillEffectTimer(WIND_WALK);
						pc.sendPackets(new S_SkillBrave(pc.getId(), 0, 0));
						pc.broadcastPacket(new S_SkillBrave(pc.getId(), 0, 0));
						pc.setBraveSpeed(0);
					}
					if (pc.hasSkillEffect(STATUS_RIBRAVE)) { // ユグドラの実とは重複しない
						pc.killSkillEffectTimer(STATUS_RIBRAVE);
						// XXX ユグドラの実のアイコンを消す方法が不明
						pc.setBraveSpeed(0);
					}
					pc.sendPackets(new S_SkillBrave(pc.getId(), 1, time));
					pc.broadcastPacket(new S_SkillBrave(pc.getId(), 1, 0));
					pc.sendPackets(new S_SkillSound(pc.getId(), 751));
					pc.broadcastPacket(new S_SkillSound(pc.getId(), 751));
					pc.setSkillEffect(STATUS_BRAVE, time * 1000);
					pc.setBraveSpeed(1);
					//綠水
					// 酔った状態を解除
					pc.setDrink(false);
	
					// ヘイスト、グレーターヘイストとは重複しない
					if (pc.hasSkillEffect(HASTE)) {
						pc.killSkillEffectTimer(HASTE);
						pc.sendPackets(new S_SkillHaste(pc.getId(), 0, 0));
						pc.broadcastPacket(new S_SkillHaste(pc.getId(), 0, 0));
						pc.setMoveSpeed(0);
					} else if (pc.hasSkillEffect(GREATER_HASTE)) {
						pc.killSkillEffectTimer(GREATER_HASTE);
						pc.sendPackets(new S_SkillHaste(pc.getId(), 0, 0));
						pc.broadcastPacket(new S_SkillHaste(pc.getId(), 0, 0));
						pc.setMoveSpeed(0);
					} else if (pc.hasSkillEffect(STATUS_HASTE)) {
						pc.killSkillEffectTimer(STATUS_HASTE);
						pc.sendPackets(new S_SkillHaste(pc.getId(), 0, 0));
						pc.broadcastPacket(new S_SkillHaste(pc.getId(), 0, 0));
						pc.setMoveSpeed(0);
					}
	
					// スロー、マス スロー、エンタングル中はスロー状態を解除するだけ
					if (pc.hasSkillEffect(SLOW)) { // スロー
						pc.killSkillEffectTimer(SLOW);
						pc.sendPackets(new S_SkillHaste(pc.getId(), 0, 0));
						pc.broadcastPacket(new S_SkillHaste(pc.getId(), 0, 0));
					} else if (pc.hasSkillEffect(MASS_SLOW)) { // マス スロー
						pc.killSkillEffectTimer(MASS_SLOW);
						pc.sendPackets(new S_SkillHaste(pc.getId(), 0, 0));
						pc.broadcastPacket(new S_SkillHaste(pc.getId(), 0, 0));
					} else if (pc.hasSkillEffect(ENTANGLE)) { // エンタングル
						pc.killSkillEffectTimer(ENTANGLE);
						pc.sendPackets(new S_SkillHaste(pc.getId(), 0, 0));
						pc.broadcastPacket(new S_SkillHaste(pc.getId(), 0, 0));
					} else {
						pc.sendPackets(new S_SkillHaste(pc.getId(), 1, time));
						pc.broadcastPacket(new S_SkillHaste(pc.getId(), 1, 0));
						pc.setMoveSpeed(1);
						pc.setSkillEffect(STATUS_HASTE, time * 1000);
					}
					
					//體魄
					new L1SkillUse().handleCommands(pc, PHYSICAL_ENCHANT_STR, pc.getId(), pc.getX(), pc.getY(), null, time, L1SkillUse.TYPE_GMBUFF);
					//通暢
					new L1SkillUse().handleCommands(pc, PHYSICAL_ENCHANT_DEX, pc.getId(), pc.getX(), pc.getY(), null, time, L1SkillUse.TYPE_GMBUFF);
					//靈魂
					new L1SkillUse().handleCommands(pc, 79, pc.getId(), pc.getX(), pc.getY(), null, time, L1SkillUse.TYPE_GMBUFF);
					//pc.sendPackets(new S_SystemMessage("您感受到天神在給你祝福! 哈雷路亞~~~!"));
					pc.setCurrentHp(pc.getMaxHp());//補血
					pc.setCurrentMp(pc.getMaxMp());//補魔
					return;
				}
				//~加天神祝福NPC by eric1300460
			}
			obj.onTalkAction(pc);
		} else {
			_log.severe("オブジェクトが見つかりません objid=" + objid);
		}
	}

	@Override
	public String getType() {
		return C_NPC_TALK;
	}
}
