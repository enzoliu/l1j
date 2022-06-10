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
package l1j.server.server.datatables;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.model.L1BossSpawn;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.utils.SQLUtil;
//所有怪物都可抓進化
import java.util.HashMap;
//~所有怪物都可抓進化

public class BossSpawnTable {
	
	//所有怪物都可抓進化
	private static BossSpawnTable _instance;
	private final static HashMap<Integer, L1BossSpawn> _bossSpawn = new HashMap<Integer, L1BossSpawn>();
	
	public static BossSpawnTable getInstance() {
		if (_instance == null) {
			_instance = new BossSpawnTable();
		}
		return _instance;
	}
	
	public boolean isBoss(int monsterId){
		for (L1BossSpawn bossSpawn : _bossSpawn.values()) {
			if(monsterId==bossSpawn.getNpcId()){
				return true;
			}
		}
		return false;
	}
	
	//~所有怪物都可抓進化
	private static Logger _log = Logger.getLogger(BossSpawnTable.class
			.getName());

	private BossSpawnTable() {
	}

	public static void fillSpawnTable() {

		int spawnCount = 0;
		java.sql.Connection con = null;
		PreparedStatement pstm = null;
		ResultSet rs = null;
		try {

			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("SELECT * FROM spawnlist_boss");
			rs = pstm.executeQuery();

			L1BossSpawn spawnDat;
			L1Npc template1;
			while (rs.next()) {
				int npcTemplateId = rs.getInt("npc_id");
				template1 = NpcTable.getInstance().getTemplate(npcTemplateId);

				if (template1 == null) {
					_log.warning("mob data for id:" + npcTemplateId
							+ " missing in npc table");
					spawnDat = null;
				} else {
					spawnDat = new L1BossSpawn(template1);
					//所有怪物都可抓進化
					int bossSpawnId = rs.getInt("id");
					spawnDat.setId(bossSpawnId);
					//~所有怪物都可抓進化
					spawnDat.setNpcid(npcTemplateId);
					spawnDat.setLocation(rs.getString("location"));
					spawnDat.setCycleType(rs.getString("cycle_type"));
					spawnDat.setAmount(rs.getInt("count"));
					spawnDat.setGroupId(rs.getInt("group_id"));
					spawnDat.setLocX(rs.getInt("locx"));
					spawnDat.setLocY(rs.getInt("locy"));
					spawnDat.setRandomx(rs.getInt("randomx"));
					spawnDat.setRandomy(rs.getInt("randomy"));
					spawnDat.setLocX1(rs.getInt("locx1"));
					spawnDat.setLocY1(rs.getInt("locy1"));
					spawnDat.setLocX2(rs.getInt("locx2"));
					spawnDat.setLocY2(rs.getInt("locy2"));
					spawnDat.setHeading(rs.getInt("heading"));
					spawnDat.setMapId(rs.getShort("mapid"));
					spawnDat.setRespawnScreen(rs.getBoolean("respawn_screen"));
					spawnDat
							.setMovementDistance(rs.getInt("movement_distance"));
					spawnDat.setRest(rs.getBoolean("rest"));
					spawnDat.setSpawnType(rs.getInt("spawn_type"));
					spawnDat.setPercentage(rs.getInt("percentage"));

					spawnDat.setName(template1.get_name());
					
					//所有怪物都可抓進化
					_bossSpawn.put(bossSpawnId, spawnDat);
					//~所有怪物都可抓進化
					
					// start the spawning
					spawnDat.init();
					spawnCount += spawnDat.getAmount();

				}

			}

		} catch (SQLException e) {
			_log.log(Level.SEVERE, e.getLocalizedMessage(), e);
		} finally {
			SQLUtil.close(rs);
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
		_log.log(Level.FINE, "総ボスモンスター数 " + spawnCount + "匹");
	}
}
