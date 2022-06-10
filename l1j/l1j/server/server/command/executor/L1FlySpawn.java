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
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.server.datatables.NpcTable;
import l1j.server.server.datatables.SpawnTable;
import l1j.server.server.model.L1Location;
import l1j.server.server.model.L1Teleport;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1Npc;
import l1j.server.server.utils.L1SpawnUtil;

public class L1FlySpawn implements L1CommandExecutor {
	private static Logger _log = Logger.getLogger(L1FlySpawn.class.getName());

	private L1FlySpawn() {
	}

	public static L1CommandExecutor getInstance() {
		return new L1FlySpawn();
	}

	@Override
	public void execute(L1PcInstance pc, String cmdName, String arg) {
		String msg = null;
		/**
		 * .flyspawn npcid count
		 */
		try {
			StringTokenizer tok = new StringTokenizer(arg);
			int npcId = Integer.parseInt(tok.nextToken().trim());
			int count = Integer.parseInt(tok.nextToken().trim());
			L1Npc template = NpcTable.getInstance().getTemplate(npcId);

			if (template == null) {
				msg = "找不到符合的NPC。";
				return;
			}
			for(int i=0;i<count;i++){
				L1Location newLocation = pc.getLocation().randomLocation(200, true);
				int newX = newLocation.getX();
				int newY = newLocation.getY();
				short mapId = (short) newLocation.getMapId();
				L1Teleport.teleport(pc, newX, newY, mapId, 5, true);
				SpawnTable.storeSpawn(pc, template);
				L1SpawnUtil.spawn(pc, npcId, 0, 0);
			}
			
		} catch (Exception e) {
			_log.log(Level.SEVERE, "", e);
			msg = cmdName + " NPCID 數量 ←請輸入。 ";
		} finally {
			if (msg != null) {
				pc.sendPackets(new S_SystemMessage(msg));
			}
		}
	}
}
