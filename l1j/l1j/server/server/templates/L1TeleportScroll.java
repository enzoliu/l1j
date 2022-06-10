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

package l1j.server.server.templates;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;

import l1j.server.L1DatabaseFactory;
import l1j.server.server.IdFactory;
import l1j.server.server.utils.SQLUtil;

public class L1TeleportScroll {
	private static Logger _log = Logger.getLogger(L1TeleportScroll.class.getName());

	private int _charId;

	private int _id;

	private String _name;

	private int _locX;

	private int _locY;

	private short _mapId;

	public L1TeleportScroll() {
	}

	public static void addBookmark(int x,int y,short mapid,String s) {
		// クライアント側でチェックされるため不要
//		if (s.length() > 12) {
//			pc.sendPackets(new S_ServerMessage(204));
//			return;
//		}
		L1TeleportScroll bookmark = new L1TeleportScroll();
		bookmark.setId(IdFactory.getInstance().nextId());
		bookmark.setCharId(1);
		bookmark.setName(s);
		bookmark.setLocX(x);
		bookmark.setLocY(y);
		bookmark.setMapId(mapid);
		Connection con = null;
		PreparedStatement pstm = null;

		try {
			con = L1DatabaseFactory.getInstance().getConnection();
			pstm = con.prepareStatement("INSERT INTO character_teleport SET id = ?, char_id = ?, name = ?, locx = ?, locy = ?, mapid = ?");
			pstm.setInt(1, bookmark.getId());
			pstm.setInt(2, bookmark.getCharId());
			pstm.setString(3, bookmark.getName());
			pstm.setInt(4, bookmark.getLocX());
			pstm.setInt(5, bookmark.getLocY());
			pstm.setInt(6, bookmark.getMapId());
			pstm.execute();
		} catch (SQLException e) {
			_log.log(Level.SEVERE, "插入座標記憶發生問題。", e);
		} finally {
			SQLUtil.close(pstm);
			SQLUtil.close(con);
		}
	}

	public int getId() {
		return _id;
	}

	public void setId(int i) {
		_id = i;
	}

	public int getCharId() {
		return _charId;
	}

	public void setCharId(int i) {
		_charId = i;
	}

	public String getName() {
		return _name;
	}

	public void setName(String s) {
		_name = s;
	}

	public int getLocX() {
		return _locX;
	}

	public void setLocX(int i) {
		_locX = i;
	}

	public int getLocY() {
		return _locY;
	}

	public void setLocY(int i) {
		_locY = i;
	}

	public short getMapId() {
		return _mapId;
	}

	public void setMapId(short i) {
		_mapId = i;
	}
}