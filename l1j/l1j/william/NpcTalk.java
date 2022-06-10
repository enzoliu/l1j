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
 * [url]http://www.gnu.org/copyleft/gpl.html[/url]
 */

package l1j.william;

import java.util.ArrayList;
import java.sql.*;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.model.Instance.L1NpcInstance;
import l1j.server.server.serverpackets.S_NpcChatPacket;

public class NpcTalk {
	//private static ArrayList array = new ArrayList();
	private static ArrayList<ArrayList> aData6 = new ArrayList<ArrayList>();
	private static boolean NO_MORE_GET_DATA6 = false;

	private NpcTalk() {
	}

	public static void forL1MonsterTalking(L1NpcInstance monster) {
		ArrayList<Object> aTempData = null;
		if (!NO_MORE_GET_DATA6) {
			NO_MORE_GET_DATA6 = true;
			getData6();
		}

		for (int i = 0; i < aData6.size(); i++) {
			aTempData = aData6.get(i);

			if (((Integer) aTempData.get(0)).intValue() == monster
					.getNpcTemplate().get_npcId()
					&& ((Integer) aTempData.get(3)).intValue() == monster
							.getOrder()) {

				if (((Integer) aTempData.get(1)).intValue() != 0) { // 大喊
					monster.broadcastPacket(new S_NpcChatPacket(monster,
							(String) aTempData.get(2), 2));
				} else {
					monster.broadcastPacket(new S_NpcChatPacket(monster,
							(String) aTempData.get(2), 0));
				}

				monster.setOrder(monster.getOrder() + 1); // 記錄目前的說話順序

				if (((Integer) aTempData.get(4)).intValue() != 0) { // 判斷是否重置說話順序
					monster.setOrder(0);
				}
				return;
			}
		}
	}

	private static void getData6() {
		java.sql.Connection con = null;
		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			Statement stat = con.createStatement();
			ResultSet rset = stat
					.executeQuery("SELECT * FROM william_npc_talk");
			ArrayList<Object> aReturn = null;
			if (rset != null)
				while (rset.next()) {
					aReturn = new ArrayList<Object>();
					aReturn.add(0, new Integer(rset.getInt("npc_id")));
					aReturn.add(1, new Integer(rset.getInt("shout")));
					aReturn.add(2, rset.getString("talk"));
					aReturn.add(3, new Integer(rset.getInt("order")));
					aReturn.add(4, new Integer(rset.getInt("end")));
					aData6.add(aReturn);
				}
			if (con != null && !con.isClosed())
				con.close();
		} catch (Exception ex) {
		}
	}
}