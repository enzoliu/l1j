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
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.serverpackets.S_Weather;

public class L1ChangeWeather implements L1CommandExecutor {

	private L1ChangeWeather() {
	}

	public static L1CommandExecutor getInstance() {
		return new L1ChangeWeather();
	}

	@Override
	public void execute(L1PcInstance pc, String cmdName, String arg) {
		try {
			StringTokenizer tok = new StringTokenizer(arg);
			int weather = Integer.parseInt(tok.nextToken());
			L1World.getInstance().setWeather(weather);
			L1World.getInstance().broadcastPacketToAll(new S_Weather(weather));
		} catch (Exception e) {
			pc
					.sendPackets(new S_SystemMessage(cmdName
							+ "【.weather 0取消下雪、[1~3]下雪、16取消下雨、[17~19]下雨】 ←請輸入。 "));
		}
	}
}
