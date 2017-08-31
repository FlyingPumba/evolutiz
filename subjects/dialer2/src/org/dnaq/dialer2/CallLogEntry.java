package org.dnaq.dialer2;

public class CallLogEntry {
    public final String number;
    public final String cachedName;
    public final long date;
    public final long duration;
    public final boolean isNew;
    public final int type;
    
    public CallLogEntry(String number, String cachedName, long date, long duration, boolean isNew, int type) {
        this.number = number;
        this.cachedName = cachedName;
        this.date = date;
        this.duration = duration;
        this.isNew = isNew;
        this.type = type;
    }
    
//    public static CallLogEntry fromCursor(Cursor c) {
//        String number = c.getString(c.getColumnIndex(Calls.NUMBER));
//        number = "-1".equals(number) ? null : number;
//        String cachedName = c.getString(c.getColumnIndex(Calls.CACHED_NAME));
//        long date = c.getLong(c.getColumnIndex(Calls.DATE));
//        long duration = c.getLong(c.getColumnIndex(Calls.DURATION));
//        boolean isNew = c.getInt(c.getColumnIndex(Calls.NEW)) == 1 ? true : false;
//        int type = c.getInt(c.getColumnIndex(Calls.TYPE));
//        return new CallLogEntry(number, cachedName, date, duration, isNew, type);
//    }
    
}
