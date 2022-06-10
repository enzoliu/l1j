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

package l1j.server.server.clientpackets;

import l1j.server.server.ClientThread;
import l1j.server.server.model.L1Object;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_Message_YN;
import l1j.server.server.serverpackets.S_ServerMessage;

// Referenced classes of package l1j.server.server.clientpackets:
// ClientBasePacket

public class C_CreateParty extends ClientBasePacket {

	private static final String C_CREATE_PARTY = "[C] C_CreateParty";
	
	public C_CreateParty(byte decrypt[], ClientThread client) throws Exception {
		super(decrypt);

		L1PcInstance pc = client.getActiveChar();

		int type = readC();
		if (type == 0 || type == 1) { // パーティー(パーティー自動分配ON/OFFで異なる)
			int targetId = readD();
			L1Object temp = L1World.getInstance().findObject(targetId);
			if (temp instanceof L1PcInstance) {
				L1PcInstance targetPc = (L1PcInstance) temp;
				if (pc.getId() == targetPc.getId()) {
					return;
				}
				if (targetPc.isInParty()) {
					// すでに他のパーティーに所属しているため招待できません
					pc.sendPackets(new S_ServerMessage(415));
					return;
				}

				if (pc.isInParty()) {
					if (pc.getParty().isLeader(pc)) {
						targetPc.setPartyID(pc.getId());
						// \f2%0\f>%sから \fUパーティー\f> に招待されました。応じますか？（Y/N）
						targetPc.sendPackets(new S_Message_YN(953, pc
								.getName()));
					} else {
						// パーティーのリーダーのみが招待できます。
						pc.sendPackets(new S_ServerMessage(416));
					}
				} else {
					targetPc.setPartyID(pc.getId());
					// \f2%0\f>%sから \fUパーティー\f> に招待されました。応じますか？（Y/N）
					targetPc.sendPackets(new S_Message_YN(953, pc.getName()));
				}
			}
		} else if (type == 2) { // チャットパーティー
			String name = readS();
			L1PcInstance targetPc = L1World.getInstance().getPlayer(name);
			if (targetPc == null) {
				// %0という名前の人はいません。
				pc.sendPackets(new S_ServerMessage(109));
				return;
			}
			if (pc.getId() == targetPc.getId()) {
				return;
			}
			if (targetPc.isInChatParty()) {
				// すでに他のパーティーに所属しているため招待できません
				pc.sendPackets(new S_ServerMessage(415));
				return;
			}

			if (pc.isInChatParty()) {
				if (pc.getChatParty().isLeader(pc)) {
					targetPc.setPartyID(pc.getId());
					// \f2%0\f>%sから\fUチャットパーティー\f>に招待されました。応じますか？（Y/N）
					targetPc.sendPackets(new S_Message_YN(951, pc.getName()));
				} else {
					// パーティーのリーダーのみが招待できます。
					pc.sendPackets(new S_ServerMessage(416));
				}
			} else {
				targetPc.setPartyID(pc.getId());
				// \f2%0\f>%sから\fUチャットパーティー\f>に招待されました。応じますか？（Y/N）
				targetPc.sendPackets(new S_Message_YN(951, pc.getName()));
			}
		}

	}

	@Override
	public String getType() {
		return C_CREATE_PARTY;
	}

}
