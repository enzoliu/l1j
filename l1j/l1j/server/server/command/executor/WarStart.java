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

import java.util.Calendar;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.Config;
import l1j.server.server.WarTimeController;
import l1j.server.server.datatables.CastleTable;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1Castle;

public class WarStart implements L1CommandExecutor {
	private static Logger _log = Logger.getLogger(WarStart.class.getName());

	private WarStart() {
	}

	public static L1CommandExecutor getInstance() {
		return new WarStart();
	}

	@Override
	public void execute(L1PcInstance pc, String cmdName, String arg) {
		try {
			StringTokenizer st = new StringTokenizer(arg);
			int time = Integer.parseInt(st.nextToken());
			int castle = Integer.parseInt(st.nextToken());

			//L1Castle _l1castle = CastleTable.getInstance().getCastleTable(castle);
			L1Castle _l1castle = WarTimeController.getCastle(castle);
			L1World.getInstance().broadcastPacketToAll(new S_SystemMessage(time+"分鐘以後"+_l1castle.getName()+"開始攻城!"));
			Calendar nowTime = WarTimeController.getRealTime();
			nowTime.add(Calendar.MINUTE, time);
			_l1castle.setWarTime(nowTime);
			CastleTable.getInstance().updateCastle(_l1castle);
			WarTimeController.setWarStartTime(castle, nowTime);
			nowTime = WarTimeController.getRealTime();
			nowTime.add(Config.ALT_WAR_TIME_UNIT, Config.ALT_WAR_TIME);
			WarTimeController.setWarEndTime(castle, nowTime);
		} catch (Exception exception) {
			pc.sendPackets(new S_SystemMessage(cmdName + " 幾分鐘後開始  0:肯特城|1:妖魔城|2:風木城"+
					"|3:奇岩城|4:海音城|5:鐵門工會|6:亞丁城|7:狄亞得要塞"));
			_log.log(Level.SEVERE, exception.getLocalizedMessage(), exception);
		}
	}
}
