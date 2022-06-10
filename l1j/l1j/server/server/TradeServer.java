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
package l1j.server.server;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import l1j.server.Config;
//this class created by sdcom 2010.3.8

public class TradeServer extends Thread {
	private ServerSocket _tradeServerSocket;
	private int _tradePort;
	
	
	@Override
	public void run() {
		System.out.println("交易伺服器就緒");
		while (true) {
			try {
				Socket trade_socket = _tradeServerSocket.accept();
				System.out.println("交易進行中...");
				String host = trade_socket.getInetAddress().getHostAddress();
				if (!host.equals(Config.ALLOW_TRADE_IP)) {
					System.out.println("非允許的ip嘗試連線 : "+trade_socket.getInetAddress());
				} else {
					TradeClientThread trade_client = new TradeClientThread(trade_socket);
					GeneralThreadPool.getInstance().execute(trade_client);
				}
			}catch(IOException e){
			}
		}
	}

	private static TradeServer _instance;

	private TradeServer() {
		super("TradeServer");
	}

	public static TradeServer getInstance() {
		if (_instance == null) {
			_instance = new TradeServer();
		}
		return _instance;
	}

	public void initialize() throws Exception {
		String s = Config.GAME_SERVER_HOST_NAME;
		_tradePort = Config.TRADE_SERVER_PORT;
		if (!"*".equals(s)) {
			InetAddress inetaddress = InetAddress.getByName(s);
			inetaddress.getHostAddress();
			_tradeServerSocket = new ServerSocket(_tradePort, 50, inetaddress);
		} else {
			_tradeServerSocket = new ServerSocket(_tradePort);
		}
		this.start();
	}
}
