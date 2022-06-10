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
package l1j.server.server.command.executor;

import java.util.StringTokenizer;

import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_ServerMessage;
import l1j.server.server.serverpackets.S_SystemMessage;

public class L1DescribeToPc implements L1CommandExecutor {

	private L1DescribeToPc() {
	}

	public static L1CommandExecutor getInstance() {
		return new L1DescribeToPc();
	}

	@Override
	public void execute(L1PcInstance pc, String cmdName, String arg) {
		try {
			StringTokenizer st = new StringTokenizer(arg);
			String name = st.nextToken();
			L1PcInstance target = L1World.getInstance().getPlayer(name);
			if (target == null) {
				pc.sendPackets(new S_ServerMessage(73, name)); // \f1%0はゲームをしていません。
				return;
			}
			StringBuilder msg = new StringBuilder();
			pc.sendPackets(new S_SystemMessage("-- describe: " + pc.getName()
					+ " --"));
			int hpr = target.getHpr() + target.getInventory().hpRegenPerTick();
			int mpr = target.getMpr() + target.getInventory().mpRegenPerTick();
			msg.append("帳號: " + target.getAccountName() + " / "); //帳號
			msg.append("ip: " + target.getNetConnection().getIp() + " / "); //Ip
			msg.append("lv: +" + target.getLevel() + " / "); //等級
			msg.append("str: +" + target.getStr() + " / "); //力
			msg.append("dex: +" + target.getDex() + " / "); //敏
			msg.append("con: +" + target.getCon() + " / "); //體
			msg.append("wis: +" + target.getWis() + " / "); //精
			msg.append("int: +" + target.getInt() + " / "); //智
			msg.append("cha: +" + target.getCha() + " / "); //魅
			msg.append("ac: +" + target.getAc() + " / "); //防
			msg.append("hp: +" + target.getMaxHp() + " / "); //hp
			msg.append("mp: +" + target.getMaxMp() + " / "); //mp
			msg.append("Accesslevel: +" + target.getAccessLevel() + " / "); //accesslevel
			msg.append("pk: +" + target.get_PKcount() + " / "); //pk
			msg.append("村民: +" + target.getContribution() + " / "); //村民
			msg.append("地獄: +" + target.getHellTime() + " / "); //地獄
			msg.append("karma: +" + target.getKarma() + " / "); //Karma
			msg.append("dmg: +" + target.getDmgup() + " / "); //最大輸出攻擊力
			msg.append("hit: +" + target.getHitup() + " / "); //命中率
			msg.append("sp: " + target.getSp() + " / "); //魔攻
			msg.append("mr: " + target.getMr() + " / "); //魔防
			msg.append("hpr: " + hpr + " / "); //回血
			msg.append("mpr: " + mpr); //回魔
			pc.sendPackets(new S_SystemMessage(msg.toString()));
			
		} catch (Exception e) {
			pc.sendPackets(new S_SystemMessage(cmdName + " 指令錯誤"));
		}
	}
}
