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

package l1j.server.server.model;

// Referenced classes of package l1j.server.server.model:
// L1PcInstance

public class L1UbSupplie {

	public L1UbSupplie() {
		
	}
	
	private int ub_id;
	
	public int getUbId(){
		return ub_id;
	}
	
	public void setUbId(int id){
		ub_id = id;
	}
	
	private String ub_name;
	
	public String getUbName(){
		return ub_name;
	}
	
	public void setUbName(String name){
		ub_name = name;
	}
	
	private int ub_item_id;
	
	public int getUbItemId(){
		return ub_item_id;
	}
	
	public void setUbItemId(int id){
		ub_item_id = id;
	}
	
	private int ub_round;
	
	public int getUbRound(){
		return ub_round;
	}
	
	public void setUbRound(int round){
		ub_round = round;
	}
	
	private int ub_item_stackcont;
	
	public int getUbItemStackCont(){
		return ub_item_stackcont;
	}
	
	public void setUbItemStackCont(int stackcont){
		ub_item_stackcont = stackcont;
	}
	
	private int ub_item_cont;
	
	public int getUbItemCont(){
		return ub_item_cont;
	}
	
	public void setUbItemCont(int cont){
		ub_item_cont = cont;
	}
	
	private int ub_item_bless;
	
	public int getUbItemBless(){
		return ub_item_bless;
	}
	
	public void setUbItemBless(int bless){
		ub_item_bless = bless;
	}
}