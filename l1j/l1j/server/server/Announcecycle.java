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
 * 
 */
package l1j.server.server;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.LineNumberReader;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.Collection;
import javolution.util.FastList;

import l1j.server.Config;
import l1j.server.server.model.L1World;
import l1j.server.server.model.Instance.L1PcInstance;
import l1j.server.server.serverpackets.S_SystemMessage;

public class Announcecycle {
        private static Logger _log = Logger.getLogger(Announcecycle.class.getName());

        private static Announcecycle _instance;

        private List<String> _Announcecycle = new FastList<String>();

        private int _Announcecyclesize = 0;

        //add by sdcom 2010.7.28
        private boolean _hasAnnounceExpCombo = false;
        
        private Announcecycle() {
                loadAnnouncecycle();
        }

        public static Announcecycle getInstance() {
                if (_instance == null) {
                     _instance = new Announcecycle();
                }

                return _instance;
        }

        public void loadAnnouncecycle() { 
                _Announcecycle.clear();
                File file = new File("data/Announcecycle.txt");
                if (file.exists()) {
                        readFromDiskmulti(file);
                        doAnnouncecycle();
                } else {
                    _log.config("data/Announcecycle.txt");
            }
        }

        private void readFromDiskmulti(File file) {
                LineNumberReader lnr = null;
                try {
                        int i=0;
                        String line = null;
                        lnr = new LineNumberReader(new FileReader(file));
                        while ( (line = lnr.readLine()) != null) {
                                StringTokenizer st = new StringTokenizer(line,"\n\r"); 
                                if (st.hasMoreTokens()) {
                                        String showAnnouncecycle = st.nextToken(); 
                                        _Announcecycle.add(showAnnouncecycle);

                                        i++;
                                }
                        }
                        _log.config("Announcecycle: Loaded " + i + " Announcecycle.");
                } catch (IOException e1)
                {
                        _log.log(Level.SEVERE, "Error reading Announcecycle", e1);
                } finally {
                        try {
                                lnr.close();
                        } catch (IOException e) {
                                e.printStackTrace();
                        }
                }
        }

        public void doAnnouncecycle() {
            AnnouncTask rs = new AnnouncTask();// 建立執行緒
            GeneralThreadPool.getInstance().scheduleAtFixedRate(rs, 180000, 60000 * Config.Show_Announcecycle_Time);
            // 10分鐘公告一次(60秒*1分*1000毫秒)
    }    
        
        /** The task launching the function doAnnouncCycle() */
        class AnnouncTask implements Runnable {
        	public void run() {
        		try {
        			//fix by sdcom 2010.7.28
        			if(Config.EXP_COMBO_ENABLE){
	        			SimpleDateFormat formatter = new SimpleDateFormat("HH");
	        		    Date currentTime = new Date();
	        		    int now = Integer.parseInt(formatter.format(currentTime));
	        		    if(Config.EXP_COMBO_END_HOUR < Config.EXP_COMBO_START_HOUR){
	        	    		if(now >= Config.EXP_COMBO_START_HOUR || now <= Config.EXP_COMBO_END_HOUR){
	        	    			Config.exp_combo_time = true;
	        	    			if(!_hasAnnounceExpCombo){
	        	    				_hasAnnounceExpCombo = true;
	        	    			}
	        	    		}else{
	        	    			Config.exp_combo_time = false;
	        	    		}
	        	    	}else{
	        			    if(now >= Config.EXP_COMBO_START_HOUR && now <= Config.EXP_COMBO_END_HOUR){
	        			    	Config.exp_combo_time = true;
	        			    	if(!_hasAnnounceExpCombo){
	        	    				_hasAnnounceExpCombo = true;
	        	    			}
	        			    }else{
	        	    			Config.exp_combo_time = false;
	        	    		}
	        	    	}
        			}else{
        				Config.exp_combo_time = false;
        			}
        			if((!_hasAnnounceExpCombo) && Config.exp_combo_time){
        				ShowAnnounceToAll("經驗加倍時段開始。");
        			}else if(_hasAnnounceExpCombo && !Config.exp_combo_time){
        				ShowAnnounceToAll("經驗加倍時段結束。");
        			}else{
	        			ShowAnnounceToAll(_Announcecycle.get(_Announcecyclesize));
	                    _Announcecyclesize++;
	                    if(_Announcecyclesize >= _Announcecycle.size()) 
	                    	_Announcecyclesize = 0;
	                    }
        			}
                catch (Exception e) {
                    _log.log(Level.WARNING, "", e);
                }
            }
        }

        private void ShowAnnounceToAll(String msg) {
                Collection <L1PcInstance> allpc = L1World.getInstance().getAllPlayers();
                for ( L1PcInstance pc : allpc )
                        pc.sendPackets( new S_SystemMessage(msg));
        }
}
