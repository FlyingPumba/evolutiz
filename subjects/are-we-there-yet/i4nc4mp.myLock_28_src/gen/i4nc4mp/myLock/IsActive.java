/*
 * This file is auto-generated.  DO NOT MODIFY.
 * Original file: /vagrant/subjects/tool_subjects/dynodroid/i4nc4mp.myLock_28_src/src/i4nc4mp/myLock/IsActive.aidl
 */
package i4nc4mp.myLock;
public interface IsActive extends android.os.IInterface
{
/** Local-side IPC implementation stub class. */
public static abstract class Stub extends android.os.Binder implements i4nc4mp.myLock.IsActive
{
private static final java.lang.String DESCRIPTOR = "i4nc4mp.myLock.IsActive";
/** Construct the stub at attach it to the interface. */
public Stub()
{
this.attachInterface(this, DESCRIPTOR);
}
/**
 * Cast an IBinder object into an i4nc4mp.myLock.IsActive interface,
 * generating a proxy if needed.
 */
public static i4nc4mp.myLock.IsActive asInterface(android.os.IBinder obj)
{
if ((obj==null)) {
return null;
}
android.os.IInterface iin = obj.queryLocalInterface(DESCRIPTOR);
if (((iin!=null)&&(iin instanceof i4nc4mp.myLock.IsActive))) {
return ((i4nc4mp.myLock.IsActive)iin);
}
return new i4nc4mp.myLock.IsActive.Stub.Proxy(obj);
}
@Override public android.os.IBinder asBinder()
{
return this;
}
@Override public boolean onTransact(int code, android.os.Parcel data, android.os.Parcel reply, int flags) throws android.os.RemoteException
{
switch (code)
{
case INTERFACE_TRANSACTION:
{
reply.writeString(DESCRIPTOR);
return true;
}
case TRANSACTION_Exists:
{
data.enforceInterface(DESCRIPTOR);
boolean _result = this.Exists();
reply.writeNoException();
reply.writeInt(((_result)?(1):(0)));
return true;
}
}
return super.onTransact(code, data, reply, flags);
}
private static class Proxy implements i4nc4mp.myLock.IsActive
{
private android.os.IBinder mRemote;
Proxy(android.os.IBinder remote)
{
mRemote = remote;
}
@Override public android.os.IBinder asBinder()
{
return mRemote;
}
public java.lang.String getInterfaceDescriptor()
{
return DESCRIPTOR;
}
@Override public boolean Exists() throws android.os.RemoteException
{
android.os.Parcel _data = android.os.Parcel.obtain();
android.os.Parcel _reply = android.os.Parcel.obtain();
boolean _result;
try {
_data.writeInterfaceToken(DESCRIPTOR);
mRemote.transact(Stub.TRANSACTION_Exists, _data, _reply, 0);
_reply.readException();
_result = (0!=_reply.readInt());
}
finally {
_reply.recycle();
_data.recycle();
}
return _result;
}
}
static final int TRANSACTION_Exists = (android.os.IBinder.FIRST_CALL_TRANSACTION + 0);
}
public boolean Exists() throws android.os.RemoteException;
}
