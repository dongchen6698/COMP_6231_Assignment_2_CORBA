package DSMS_CORBA;

/**
* DSMS_CORBA/DSMSHolder.java .
* Generated by the IDL-to-Java compiler (portable), version "3.2"
* from Distributed_Staff_Management_System.idl
* Thursday, June 16, 2016 2:08:40 AM EDT
*/

public final class DSMSHolder implements org.omg.CORBA.portable.Streamable
{
  public DSMS_CORBA.DSMS value = null;

  public DSMSHolder ()
  {
  }

  public DSMSHolder (DSMS_CORBA.DSMS initialValue)
  {
    value = initialValue;
  }

  public void _read (org.omg.CORBA.portable.InputStream i)
  {
    value = DSMS_CORBA.DSMSHelper.read (i);
  }

  public void _write (org.omg.CORBA.portable.OutputStream o)
  {
    DSMS_CORBA.DSMSHelper.write (o, value);
  }

  public org.omg.CORBA.TypeCode _type ()
  {
    return DSMS_CORBA.DSMSHelper.type ();
  }

}
