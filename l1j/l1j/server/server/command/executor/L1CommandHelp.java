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

import java.util.List;

import l1j.server.server.command.L1Commands;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SystemMessage;
import l1j.server.server.templates.L1Command;

public class L1CommandHelp implements L1CommandExecutor {

	private L1CommandHelp() {
	}

	public static L1CommandExecutor getInstance() {
		return new L1CommandHelp();
	}

	@Override
	public void execute(L1PcInstance pc, String cmdName, String arg) {
		List<L1Command> list = L1Commands.availableCommandList(pc.getAccessLevel());
		for(int i=0;i<list.size();i++){
			pc.sendPackets(new S_SystemMessage(list.get(i).getName()));
		}
		//fix by sdcom 2009.11.20
		//pc.sendPackets(new S_SystemMessage(join(list, ", ")));
	}
}
