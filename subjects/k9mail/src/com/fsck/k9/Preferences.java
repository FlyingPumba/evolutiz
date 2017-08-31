
package com.fsck.k9;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import android.content.Context;
import android.content.SharedPreferences;
import android.util.Config;
import android.util.Log;
import com.fsck.k9.preferences.Editor;
import com.fsck.k9.preferences.Storage;

public class Preferences
{

    /**
     * Immutable empty {@link Account} array
     */
    private static final Account[] EMPTY_ACCOUNT_ARRAY = new Account[0];

    private static Preferences preferences;

    public static synchronized Preferences getPreferences(Context context)
    {
        if (preferences == null)
        {
            preferences = new Preferences(context);
        }
        return preferences;
    }


    private Storage mStorage;
    private List<Account> accounts;
    private Account newAccount;
    private Context mContext;

    private Preferences(Context context)
    {
        mStorage = Storage.getStorage(context);
        mContext = context;
        if (mStorage.size() == 0)
        {
            Log.i(K9.LOG_TAG, "Preferences storage is zero-size, importing from Android-style preferences");
            Editor editor = mStorage.edit();
            editor.copy(context.getSharedPreferences("AndroidMail.Main", Context.MODE_PRIVATE));
            editor.commit();
        }
    }

    private synchronized void loadAccounts()
    {
        String accountUuids = getPreferences().getString("accountUuids", null);
        if ((accountUuids != null) && (accountUuids.length() != 0))
        {
            String[] uuids = accountUuids.split(",");
            accounts = new ArrayList<Account>(uuids.length);
            for (String uuid : uuids)
            {
                accounts.add(new Account(this, uuid));
            }
        }
        else
        {
            accounts = new ArrayList<Account>();
        }
    }

    /**
     * Returns an array of the accounts on the system. If no accounts are
     * registered the method returns an empty array.
     * @return all accounts
     */
    public synchronized Account[] getAccounts()
    {
        if (accounts == null)
        {
            loadAccounts();
        }

        if ((newAccount != null) && newAccount.getAccountNumber() != -1)
        {
            accounts.add(newAccount);
            newAccount = null;
        }

        return accounts.toArray(EMPTY_ACCOUNT_ARRAY);
    }

    /**
     * Returns an array of the accounts on the system. If no accounts are
     * registered the method returns an empty array.
     * @return all accounts with {@link Account#isAvailable(Context)}
     */
    public synchronized Collection<Account> getAvailableAccounts()
    {
        if (accounts == null)
        {
            loadAccounts();
        }

        if ((newAccount != null) && newAccount.getAccountNumber() != -1)
        {
            accounts.add(newAccount);
            newAccount = null;
        }
        Collection<Account> retval = new ArrayList<Account>(accounts.size());
        for (Account account : accounts)
        {
            if (account.isAvailable(mContext))
            {
                retval.add(account);
            }
        }

        return retval;
    }

    public synchronized Account getAccount(String uuid)
    {
        if (accounts == null)
        {
            loadAccounts();
        }

        for (Account account : accounts)
        {
            if (account.getUuid().equals(uuid))
            {
                return account;
            }
        }

        if ((newAccount != null) && newAccount.getUuid().equals(uuid))
        {
            return newAccount;
        }

        return null;
    }

    public synchronized Account newAccount()
    {
        newAccount = new Account(K9.app);

        return newAccount;
    }

    public synchronized void deleteAccount(Account account)
    {
        accounts.remove(account);
        account.delete(this);

        if (newAccount == account)
        {
            newAccount = null;
        }
    }

    /**
     * Returns the Account marked as default. If no account is marked as default
     * the first account in the list is marked as default and then returned. If
     * there are no accounts on the system the method returns null.
     */
    public Account getDefaultAccount()
    {
        String defaultAccountUuid = getPreferences().getString("defaultAccountUuid", null);
        Account defaultAccount = getAccount(defaultAccountUuid);

        if (defaultAccount == null)
        {
            Collection<Account> accounts = getAvailableAccounts();
            if (accounts.size() > 0)
            {
                defaultAccount = accounts.iterator().next();
                setDefaultAccount(defaultAccount);
            }
        }

        return defaultAccount;
    }

    public void setDefaultAccount(Account account)
    {
        getPreferences().edit().putString("defaultAccountUuid", account.getUuid()).commit();
    }

    public void dump()
    {
        if (Config.LOGV)
        {
            for (String key : getPreferences().getAll().keySet())
            {
                Log.v(K9.LOG_TAG, key + " = " + getPreferences().getAll().get(key));
            }
        }
    }

    public SharedPreferences getPreferences()
    {
        return mStorage;
    }
}
