package cn.kuwo.kwmusiccar;

import cn.kuwo.base.http.HttpSession;
import cn.kuwo.base.util.KwDate;
import cn.kuwo.base.util.SettingsUtils;
import cn.kuwo.base.util.UrlManagerUtils;

public class CopyrightChecker
{
  public void init()
  {
    Object localObject = SettingsUtils.getStringSettings("last_check_copyright");
    if ((localObject != null) && (!((String)localObject).isEmpty()))
    {
      localObject = new KwDate((String)localObject);
      ((KwDate)localObject).increase(86400, 1);
      if (((KwDate)localObject).after(new KwDate())) {
        return;
      }
    }
    SettingsUtils.setStringSettings("last_check_copyright", new KwDate().toDateTimeString());
    new Thread(new Runnable()
    {
      public void run()
      {
        int i = 0;
        String str = HttpSession.getString(UrlManagerUtils.getCheckCopyrightRequest());
        if ((str != null) && (str.length() > "RESULT=0".length()) && (str.substring(0, "RESULT=0".length()).equals("RESULT=0"))) {}
        for (;;)
        {
          if (i != 0) {}
          for (str = "yes";; str = "no")
          {
            SettingsUtils.setStringSettings("hascopyright", str);
            return;
          }
          i = 1;
        }
      }
    }).start();
  }
}
