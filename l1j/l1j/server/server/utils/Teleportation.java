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

package l1j.server.server.utils;

import java.util.HashSet;
import java.util.logging.Logger;
import java.util.Random;

import l1j.server.Config;
import l1j.server.server.model.L1Clan;
import l1j.server.server.model.L1Location;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1DollInstance;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.Instance.L1PetInstance;
import l1j.server.server.model.Instance.L1SummonInstance;
import l1j.server.server.model.map.L1Map;
import l1j.server.server.model.map.L1WorldMap;
import l1j.server.server.model.skill.L1SkillId;
import l1j.server.server.serverpackets.S_CharVisualUpdate;
import l1j.server.server.serverpackets.S_DollPack;
import l1j.server.server.serverpackets.S_SkillBrave;
import l1j.server.server.serverpackets.S_SkillIconWindShackle;
import l1j.server.server.serverpackets.S_MapID;
import l1j.server.server.serverpackets.S_OtherCharPacks;
import l1j.server.server.serverpackets.S_OwnCharPack;
import l1j.server.server.serverpackets.S_Paralysis;
import l1j.server.server.serverpackets.S_PetPack;
import l1j.server.server.serverpackets.S_SummonPack;
import l1j.server.server.serverpackets.S_SystemMessage;

import static l1j.server.server.model.skill.L1SkillId.*;

// Referenced classes of package l1j.server.server.utils:
// FaceToFace

public class Teleportation {

	private static Logger _log = Logger
			.getLogger(Teleportation.class.getName());

	private static Random _random = new Random();

	private Teleportation() {
	}

	public static void Teleportation(L1PcInstance pc) {
		if (pc.isDead() || pc.isTeleport()) {
			return;
		}
		//傳送位移對策:使用S_Paralysis.TYPE_TELEPORT
		pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_TELEPORT,true));
		pc.setTeleport(true);

		int x = pc.getTeleportX();
		int y = pc.getTeleportY();
		short mapId = pc.getTeleportMapId();
		int head = pc.getTeleportHeading();

		// テレポート先が不正であれば元の座標へ(GMは除く)
		L1Map map = L1WorldMap.getInstance().getMap(mapId);

		if (!map.isInMap(x, y) && !pc.isGm()) {
			x = pc.getX();
			y = pc.getY();
			mapId = pc.getMapId();
		}

		L1Clan clan = L1World.getInstance().getClan(pc.getClanname());
		if (clan != null) {
			if (clan.getWarehouseUsingChar() == pc.getId()) { // 自キャラがクラン倉庫使用中
				clan.setWarehouseUsingChar(0); // クラン倉庫のロックを解除
			}
		}

		L1World.getInstance().moveVisibleObject(pc, mapId);
		pc.setLocation(x, y, mapId);
		pc.setHeading(head);
		pc.sendPackets(new S_MapID(pc.getMapId(), pc.getMap().isUnderwater()));

		if (pc.isReserveGhost()) { // ゴースト状態解除
			pc.endGhost();
		}
		if (!pc.isGhost() && !pc.isGmInvis() && !pc.isInvisble()) {
			pc.broadcastPacket(new S_OtherCharPacks(pc));
		}
		pc.sendPackets(new S_OwnCharPack(pc));
		pc.removeAllKnownObjects();
		pc.sendVisualEffectAtTeleport(); // クラウン、毒、水中等の視覚効果を表示
		pc.updateObject();
		// spr番号6310, 5641の変身中にテレポートするとテレポート後に移動できなくなる
		// 武器を着脱すると移動できるようになるため、S_CharVisualUpdateを送信する
		pc.sendPackets(new S_CharVisualUpdate(pc));

		pc.killSkillEffectTimer(MEDITATION);
		pc.setCallClanId(0); // コールクランを唱えた後に移動すると召喚無効
		/*
		 * subjects ペットとサモンのテレポート先画面内へ居たプレイヤー。
		 * 各ペット毎にUpdateObjectを行う方がコード上ではスマートだが、
		 * ネットワーク負荷が大きくなる為、一旦Setへ格納して最後にまとめてUpdateObjectする。
		 */
		HashSet<L1PcInstance> subjects = new HashSet<L1PcInstance>();
		subjects.add(pc);
		
		if (!pc.isGhost() && pc.getMap().isTakePets()) {
			// ペットとサモンも一緒に移動させる。
			for (L1NpcInstance petNpc : pc.getPetList().values()) {

				// テレポート先の設定
				L1Location loc = pc.getLocation().randomLocation(3, false);
				int nx = loc.getX();
				int ny = loc.getY();
				if (pc.getMapId() == 5125 || pc.getMapId() == 5131
						|| pc.getMapId() == 5132 || pc.getMapId() == 5133
						|| pc.getMapId() == 5134) { // ペットマッチ会場
					nx = 32799 + _random.nextInt(5) - 3;
					ny = 32864 + _random.nextInt(5) - 3;
				}
				teleport(petNpc, nx, ny, mapId, head);
				if (petNpc instanceof L1SummonInstance) { // サモンモンスター
					L1SummonInstance summon = (L1SummonInstance) petNpc;
					pc.sendPackets(new S_SummonPack(summon, pc));
				} else if (petNpc instanceof L1PetInstance) { // ペット
					L1PetInstance pet = (L1PetInstance) petNpc;
					pc.sendPackets(new S_PetPack(pet, pc));
				}

				for (L1PcInstance visiblePc : L1World.getInstance()
						.getVisiblePlayer(petNpc)) {
					// テレポート元と先に同じPCが居た場合、正しく更新されない為、一度removeする。
					visiblePc.removeKnownObject(petNpc);
					subjects.add(visiblePc);
				}

			}

			// マジックドールも一緒に移動させる。
			for (L1DollInstance doll : pc.getDollList().values()) {

				// テレポート先の設定
				L1Location loc = pc.getLocation().randomLocation(3, false);
				int nx = loc.getX();
				int ny = loc.getY();

				teleport(doll, nx, ny, mapId, head);
				pc.sendPackets(new S_DollPack(doll, pc));

				for (L1PcInstance visiblePc : L1World.getInstance()
						.getVisiblePlayer(doll)) {
					// テレポート元と先に同じPCが居た場合、正しく更新されない為、一度removeする。
					visiblePc.removeKnownObject(doll);
					subjects.add(visiblePc);
				}

			}
		}
		for (L1PcInstance updatePc : subjects) {
			updatePc.updateObject();
		}
		
		
		if (pc.hasSkillEffect(WIND_SHACKLE)) {
			pc.sendPackets(new S_SkillIconWindShackle(pc.getId(),
					pc.getSkillEffectTimeSec(WIND_SHACKLE)));
		}
		//超級加速
		if (pc.hasSkillEffect(STATUS_THIRD_SPEED)) {
			pc.sendPackets(new S_SkillBrave(pc.getId(),5,
					pc.getSkillEffectTimeSec(STATUS_THIRD_SPEED)));
		}
		//~超級加速
		try {
			Thread.sleep(Config.TELEPORT_LOCK_TIME);
		} catch (Exception e) {
		}
		//傳送位移對策:使用S_Paralysis.TYPE_TELEPORT
		//延伸問題:在傳送期間再次使用會使畫面卡住
		//延伸問題對策:於鎖定期間再次使用item的地方解除鎖定，因此道具鎖定時間需大於Config.TELEPORT_LOCK_TIME。
		pc.sendPackets(new S_Paralysis(S_Paralysis.TYPE_TELEPORT, false));
		pc.setTeleport(false);
	}

	private static void teleport(L1NpcInstance npc, int x, int y, short map,
			int head) {
		L1World.getInstance().moveVisibleObject(npc, map);

		L1WorldMap.getInstance().getMap(npc.getMapId()).setPassable(npc.getX(),
				npc.getY(), true);
		npc.setX(x);
		npc.setY(y);
		npc.setMap(map);
		npc.setHeading(head);
		L1WorldMap.getInstance().getMap(npc.getMapId()).setPassable(npc.getX(),
				npc.getY(), false);
	}

}
