package cn.kuwo.base.bean;

public class BytesResult
{
  public ResultType mType = ResultType.none;
  public byte[] mXmlBytes;
  
  public static enum ResultType
  {
    bytes,  none;
    
    private ResultType() {}
  }
}
