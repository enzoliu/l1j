/* This program is free software; you can redistribute it and/or modify
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

import static l1j.server.server.model.Instance.L1PcInstance.REGENSTATE_MOVE;

import java.util.ArrayList;

import l1j.server.Config;
import l1j.server.server.ClientThread;
import l1j.server.server.model.AcceleratorChecker;
import l1j.server.server.model.Dungeon;
import l1j.server.server.model.DungeonRandom;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.model.trap.L1WorldTraps;
import l1j.server.server.serverpackets.S_MoveCharPacket;

import static l1j.server.server.model.skill.L1SkillId.*;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_MoveChar extends ClientBasePacket {

	private static final byte HEADING_TABLE_X[] = { 0, 1, 1, 1, 0, -1, -1, -1 };

	private static final byte HEADING_TABLE_Y[] = { -1, -1, 0, 1, 1, 1, 0, -1 };

	private static final int CLIENT_LANGUAGE = Config.CLIENT_LANGUAGE;

	// マップタイル調査用
	/*
	private void sendMapTileLog(L1PcInstance pc) {
		pc.sendPackets(new S_SystemMessage(pc.getMap().toString(pc.getLocation())));
	}
	*/
	// 移動
	public C_MoveChar(byte decrypt[], ClientThread client)
			throws Exception {
		super(decrypt);
		int locx = readH();
		int locy = readH();
		int heading = readC();

		L1PcInstance pc = client.getActiveChar();

		if (pc.isTeleport()) { // テレポート処理中
			return;
		}
		//TODO 角色XY軸修正 by linsf260
		if(Config.CLIENT_LANGUAGE == 3)
		{
			locx = pc.getX();	// 修正X軸
			locy = pc.getY();	// 修正Y軸
		}
		//TODO 角色XY軸修正  end
		
		// 移動要求間隔をチェックする
		if (Config.CHECK_MOVE_INTERVAL) {
			int result;
			result = pc.getAcceleratorChecker()
					.checkInterval(AcceleratorChecker.ACT_TYPE.MOVE);
			if (result == AcceleratorChecker.R_DISCONNECTED) {
				return;
			}
		}

		pc.killSkillEffectTimer(MEDITATION);
		pc.setCallClanId(0); // コールクランを唱えた後に移動すると召喚無効

		if (!pc.hasSkillEffect(ABSOLUTE_BARRIER)) { // アブソルートバリア中ではない
			pc.setRegenState(REGENSTATE_MOVE);
		}
		pc.getMap().setPassable(pc.getLocation(), true);

		if (CLIENT_LANGUAGE == 3) { // Taiwan Only
			heading ^= 0x49;
			locx = pc.getX();
			locy = pc.getY();
		}
		
		locx += HEADING_TABLE_X[heading];
		locy += HEADING_TABLE_Y[heading];
		// 修正穿人bug by eric1300460
		if (Config.HACK1) {
			ArrayList<L1Object> objs = L1World.getInstance().getVisibleObjects(
					pc, 1);
			for (L1Object obj : objs) {
				if (pc.isDead()
						&& obj instanceof L1PcInstance
						&& pc.getName().equals(((L1PcInstance) obj).getName())
						&& ((L1PcInstance) obj).isDead()) {
					continue;
				}
				if (obj.getX() == locx//pc.getX()
						&& obj.getY() == locy//pc.getY()
						&& ((obj instanceof L1PcInstance)
								//|| (obj instanceof L1NpcInstance)
								//|| (obj instanceof L1PetInstance)
								//|| (obj instanceof L1GuardInstance) 
								//|| (obj instanceof L1TeleporterInstance)
								)) {
					/**
					 * 要斷線 要鎖帳號 要公告 要扣經驗值 都放在這邊
					 */
					//L1World.getInstance().broadcastServerMessage(
					//		"玩家:" + pc.getName() + "使用穿人外掛!");
					pc.getMap().setPassable(pc.getLocation(), false);
					L1Teleport.teleport(pc, pc.getLocation(), heading, true);
					return;
				}
			}
		}
		
		//pc.getMap().setPassable(pc.getLocation(), true);
		// ~修正穿人bug by eric1300460

		if (Dungeon.getInstance().dg(locx, locy, pc.getMap().getId(), pc)) { // ダンジョンにテレポートした場合
			return;
		}
		if (DungeonRandom.getInstance().dg(locx, locy, pc.getMap().getId(),
				pc)) { // テレポート先がランダムなテレポート地点
			return;
		}

		pc.getLocation().set(locx, locy);
		pc.setHeading(heading);
		if (!pc.isGmInvis() && !pc.isGhost() && !pc.isInvisble()) {
			pc.broadcastPacket(new S_MoveCharPacket(pc));
		}

		// sendMapTileLog(pc); // 移動先タイルの情報を送る(マップ調査用)
		//XXX 寵物競速-判斷圈數
		l1j.expand.L1PolyRace.getInstance().checkLapFinish(pc);
	    //END
		L1WorldTraps.getInstance().onPlayerMoved(pc);

		pc.getMap().setPassable(pc.getLocation(), false);
		// user.UpdateObject(); // 可視範囲内の全オブジェクト更新
	}
}