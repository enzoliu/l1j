package l1j.server.server.model.item;

import static l1j.server.server.model.skill.L1SkillId.ABSOLUTE_BARRIER;
import static l1j.server.server.model.skill.L1SkillId.POLLUTE_WATER;
import l1j.server.server.ClientThread;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SkillSound;

public class L1Potion {
	public L1Potion(){
	}
	public void use(L1PcInstance pc,ClientThread client,L1ItemInstance l1iteminstance1,L1ItemInstance l1iteminstance,int itemId) throws Exception {
		if (itemId == 40010	|| itemId == 40019 || itemId == 40029) {
			UseHeallingPotion(pc, 15, 189);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40022) { // 古代の体力回復剤
			UseHeallingPotion(pc, 20, 189);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40011 || itemId == 40020) {
			UseHeallingPotion(pc, 45, 194);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40023) { // 古代の高級体力回復剤
			UseHeallingPotion(pc, 30, 194);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40012 || itemId == 40021) {
			UseHeallingPotion(pc, 75, 197);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40024) { // 古代の強力体力回復剤
			UseHeallingPotion(pc, 55, 197);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40506) { // エントの実
			UseHeallingPotion(pc, 70, 197);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40026 || itemId == 40027 || itemId == 40028 || itemId == 140010) { //水果
			UseHeallingPotion(pc, 25, 189);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 240010) {
			UseHeallingPotion(pc, 10, 189);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 140011) { // 祝福されたオレンジ
			UseHeallingPotion(pc, 55, 194);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 140012) { // 祝福されたクリアー
			UseHeallingPotion(pc, 85, 197);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 140506) { // 祝福されたエントの実
			UseHeallingPotion(pc, 80, 197);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} else if (itemId == 40043) { // 兎の肝
			UseHeallingPotion(pc, 600, 189);
			pc.getInventory().removeItem(l1iteminstance, 1);
		} 
	}
	protected void UseHeallingPotion(L1PcInstance pc, int healHp, int gfxid) {
		if (pc.hasSkillEffect(71) == true) { // ディケイ ポーションの状態
			pc.sendPackets(new S_ServerMessage(698)); // 魔力によって何も飲むことができません。
			return;
		}
		// アブソルート バリアの解除
		cancelAbsoluteBarrier(pc);
		pc.sendPackets(new S_SkillSound(pc.getId(), gfxid));
		pc.broadcastPacket(new S_SkillSound(pc.getId(), gfxid));
		pc.sendPackets(new S_ServerMessage(77)); // \f1気分が良くなりました。
		if (pc.hasSkillEffect(POLLUTE_WATER)) { // ポルートウォーター中は回復量1/2倍
			healHp /= 2;
		}
		
		// add by sdcom. try to enhance performance.
		pc.addHp(healHp);
	}
	protected void cancelAbsoluteBarrier(L1PcInstance pc) { // アブソルート バリアの解除
		if (pc.hasSkillEffect(ABSOLUTE_BARRIER)) {
			pc.killSkillEffectTimer(ABSOLUTE_BARRIER);
			pc.startHpRegeneration();
			pc.startMpRegeneration();
			pc.startMpRegenerationByDoll();
			pc.startHpRegenerationByDoll();//add 魔法娃娃回血功能
		}
	}
}
