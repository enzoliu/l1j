package l1j.server.server.model.item;

import l1j.server.server.ClientThread;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1ItemInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.utils.L1SpawnUtil;

public class L1ItemQuest {
	public L1ItemQuest(){
	}
	public void use(L1PcInstance pc,ClientThread client,L1ItemInstance l1iteminstance1,L1ItemInstance l1iteminstance,int itemId) throws Exception {
		if ((itemId == 40576 && !pc.isElf()) // 魂の結晶の破片（白）
				|| (itemId == 40577 && !pc.isWizard()) // 魂の結晶の破片（黒）
				|| (itemId == 40578 && !pc.isKnight())) { // 魂の結晶の破片（赤）
			pc.sendPackets(new S_ServerMessage(264)); // \f1あなたのクラスではこのアイテムは使用できません。
			return;
		}else if (itemId == 40555) { // 秘密の部屋のキー
			if (pc.isKnight() && (pc.getX() >= 32806 && pc.getX() <= 32814) && (pc.getY() >= 32798 && pc.getY() <= 32807) && pc.getMapId() == 13) {
				short mapid = 13;
				L1Teleport.teleport(pc, 32815, 32810, mapid, 5, false);
			} else {
				pc.sendPackets(new S_ServerMessage(79)); 
			}
		} else if (itemId == 40557) { // 暗殺リスト(グルーディン)
			if (pc.getX() == 32620 && pc.getY() == 32641 && pc.getMapId() == 4) {
				for (L1Object object : L1World.getInstance().getObject()) {
					if (object instanceof L1NpcInstance) {
						L1NpcInstance npc = (L1NpcInstance) object;
						if (npc.getNpcTemplate().get_npcId() == 45883) {
							pc.sendPackets(new S_ServerMessage(79));
							return;
						}
					}
				}
				L1SpawnUtil.spawn(pc, 45883, 0, 300000);
			} else {
				pc.sendPackets(new S_ServerMessage(79)); 
			}
		} else if (itemId == 40563) { // 暗殺リスト(火田村)
			if (pc.getX() == 32730 && pc.getY() == 32426 && pc.getMapId() == 4) {
				for (L1Object object : L1World.getInstance().getObject()) {
					if (object instanceof L1NpcInstance) {
						L1NpcInstance npc = (L1NpcInstance) object;
						if (npc.getNpcTemplate().get_npcId() == 45884) {
							pc.sendPackets(new S_ServerMessage(79));
							return;
						}
					}
				}
				L1SpawnUtil.spawn(pc, 45884, 0, 300000);
			} else {
				pc.sendPackets(new S_ServerMessage(79));
			}
		} else if (itemId == 40561) { // 暗殺リスト(ケント)
			if (pc.getX() == 33046 && pc.getY() == 32806 && pc.getMapId() == 4) {
				for (L1Object object : L1World.getInstance().getObject()) {
					if (object instanceof L1NpcInstance) {
						L1NpcInstance npc = (L1NpcInstance) object;
						if (npc.getNpcTemplate().get_npcId() == 45885) {
							pc.sendPackets(new S_ServerMessage(79));
							return;
						}
					}
				}
				L1SpawnUtil.spawn(pc, 45885, 0, 300000);
			} else {
				pc.sendPackets(new S_ServerMessage(79));
			}
		} else if (itemId == 40560) { // 暗殺リスト(ウッドベック)
			if (pc.getX() == 32580 && pc.getY() == 33260 && pc.getMapId() == 4) {
				for (L1Object object : L1World.getInstance().getObject()) {
					if (object instanceof L1NpcInstance) {
						L1NpcInstance npc = (L1NpcInstance) object;
						if (npc.getNpcTemplate().get_npcId() == 45886) {
							pc.sendPackets(new S_ServerMessage(79));
							return;
						}
					}
				}
				L1SpawnUtil.spawn(pc, 45886, 0, 300000);
			} else {
				pc.sendPackets(new S_ServerMessage(79)); 
			}
		} else if (itemId == 40562) { // 暗殺リスト(ハイネ)
			if (pc.getX() == 33447 && pc.getY() == 33476 && pc.getMapId() == 4) {
				for (L1Object object : L1World.getInstance().getObject()) {
					if (object instanceof L1NpcInstance) {
						L1NpcInstance npc = (L1NpcInstance) object;
						if (npc.getNpcTemplate().get_npcId() == 45887) {
							pc.sendPackets(new S_ServerMessage(79));
							return;
						}
					}
				}
				L1SpawnUtil.spawn(pc, 45887, 0, 300000);
			} else {
				pc.sendPackets(new S_ServerMessage(79)); 
			}
		} else if (itemId == 40559) { // 暗殺リスト(アデン)
			if (pc.getX() == 34215 && pc.getY() == 33195 && pc.getMapId() == 4) {
				for (L1Object object : L1World.getInstance().getObject()) {
					if (object instanceof L1NpcInstance) {
						L1NpcInstance npc = (L1NpcInstance) object;
						if (npc.getNpcTemplate().get_npcId() == 45888) {
							pc.sendPackets(new S_ServerMessage(79));
							return;
						}
					}
				}
				L1SpawnUtil.spawn(pc, 45888, 0, 300000);
			} else {
				pc.sendPackets(new S_ServerMessage(79));
			}
		} else if (itemId == 40558) { // 暗殺リスト(ギラン)
			if (pc.getX() == 33513 && pc.getY() == 32890 && pc.getMapId() == 4) {
				for (L1Object object : L1World.getInstance().getObject()) {
					if (object instanceof L1NpcInstance) {
						L1NpcInstance npc = (L1NpcInstance) object;
						if (npc.getNpcTemplate().get_npcId() == 45889) {
							pc.sendPackets(new S_ServerMessage(79));
							return;
						}
					}
				}
				L1SpawnUtil.spawn(pc, 45889, 0, 300000);
			} else {
				pc.sendPackets(new S_ServerMessage(79)); // \f1何も起きませんでした。
			}
		} else if (itemId == 40572) { // アサシンの印
			if (pc.getX() == 32778 && pc.getY() == 32738 && pc.getMapId() == 21) {
				L1Teleport.teleport(pc, 32781, 32728, (short) 21, 5,true);
			} else if (pc.getX() == 32781 && pc.getY() == 32728 && pc.getMapId() == 21) {
				L1Teleport.teleport(pc, 32778, 32738, (short) 21, 5,true);
			} else {
				pc.sendPackets(new S_ServerMessage(79));
			}
		}
	}
}
