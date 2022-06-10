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
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.serverpackets.S_Test;

public class L1Test implements L1CommandExecutor {
	
	private L1Test() {
	}

	public static L1CommandExecutor getInstance() {
		return new L1Test();
	}

	@Override
	public void execute(L1PcInstance pc, String cmdName, String arg) {
			try {
				StringTokenizer st = new StringTokenizer(arg);
				String num = st.nextToken();
				String flag = st.nextToken();
				
				pc.sendPackets(new S_Test(Integer.parseInt(num), Boolean.parseBoolean(flag)));
			} catch(Exception e) {
				pc.sendPackets(new S_SystemMessage("請輸入 .test num flag"));
			}
		}
}