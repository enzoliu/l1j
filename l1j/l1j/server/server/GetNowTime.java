//********************************************************************
//  GetNowTime.java
//  by mca
// 2008/4/1
//********************************************************************
package l1j.server.server;

import java.util.*;// class Scanner
//************************************************************************
// 取得現在時間
//************************************************************************
public class GetNowTime
{
  public static int GetNowYear()
  {
    Calendar rightNow = Calendar.getInstance(); //取得預設月曆物件
    int nowYear;
    nowYear=rightNow.get(Calendar.YEAR); //取得現年之值
    return nowYear; // 傳回取得現年之值
  }
  
    public static int GetNowMonth()
  {
    Calendar rightNow = Calendar.getInstance(); //取得預設月曆物件
    int nowMonth;
    nowMonth=rightNow.get(Calendar.MONTH); //取得現月之值
    return nowMonth; // 傳回取得現月之值
  }
  
    public static int GetNowDay()
  {
    Calendar rightNow = Calendar.getInstance(); //取得預設月曆物件
    int nowDay;
    nowDay=rightNow.get(Calendar.DATE); //取得今日之值
    return nowDay; // 傳回取得今日之值
  }
  
    public static int GetNowHour()
  {
    Calendar rightNow = Calendar.getInstance(); //取得預設月曆物件
    int nowHour;
    nowHour=rightNow.get(Calendar.HOUR_OF_DAY); //取得此時之值
    return nowHour; // 傳回取得此時之值
  }
  
    public static int GetNowMinute()
  {
    Calendar rightNow = Calendar.getInstance(); //取得預設月曆物件
    int nowMinute;
    nowMinute=rightNow.get(Calendar.MINUTE); //取得此分之值
    return nowMinute; // 傳回取得此分之值
  }
  
    public static int GetNowSecond()
  {
    Calendar rightNow = Calendar.getInstance(); //取得預設月曆物件
    int nowSecond;
    nowSecond=rightNow.get(Calendar.SECOND); //取得此秒之值
    return nowSecond; // 傳回取得此秒之值
  }
}
//<====程式結束