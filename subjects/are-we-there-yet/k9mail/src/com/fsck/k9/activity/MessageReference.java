package com.fsck.k9.activity;

import android.util.Log;
import com.fsck.k9.K9;
import com.fsck.k9.helper.Utility;
import com.fsck.k9.mail.Flag;
import com.fsck.k9.mail.MessagingException;

import java.io.Serializable;
import java.util.StringTokenizer;

public class MessageReference implements Serializable
{
    private static final long serialVersionUID = -1625198750239083389L;
    public String accountUuid;
    public String folderName;
    public String uid;
    public Flag flag = null;

    /**
     * Initialize an empty MessageReference.
     */
    public MessageReference()
    {
    }

    // Version identifier for use when serializing. This will allow us to introduce future versions
    // if we have to rev MessageReference.
    private static final String IDENTITY_VERSION_1 = "!";
    private static final String IDENTITY_SEPARATOR = ":";

    /**
     * Initialize a MessageReference from a seraialized identity.
     * @param identity Serialized identity.
     * @throws MessagingException On missing or corrupted identity.
     */
    public MessageReference(final String identity) throws MessagingException
    {
        // Can't be null and must be at least length one so we can check the version.
        if (identity == null || identity.length() < 1)
        {
            throw new MessagingException("Null or truncated MessageReference identity.");
        }

        // Version check.
        if (identity.charAt(0) == IDENTITY_VERSION_1.charAt(0))
        {
            // Split the identity, stripping away the first two characters representing the version and delimiter.
            StringTokenizer tokens = new StringTokenizer(identity.substring(2), IDENTITY_SEPARATOR, false);
            if (tokens.countTokens() >= 3)
            {
                accountUuid = Utility.base64Decode(tokens.nextToken());
                folderName = Utility.base64Decode(tokens.nextToken());
                uid = Utility.base64Decode(tokens.nextToken());

                if (tokens.hasMoreTokens())
                {
                    final String flagString = tokens.nextToken();
                    try
                    {
                        flag = Flag.valueOf(flagString);
                    } catch (IllegalArgumentException ie)
                    {
                        throw new MessagingException("Could not thaw message flag '" + flagString + "'", ie);
                    }
                }

                if (K9.DEBUG)
                    Log.d(K9.LOG_TAG, "Thawed " + toString());
            }
            else
            {
                throw new MessagingException("Invalid MessageReference in " + identity + " identity.");
            }
        }
    }

    /**
     * Serialize this MessageReference for storing in a K9 identity.  This is a colon-delimited base64 string.
     *
     * @return Serialized string.
     */
    public String toIdentityString()
    {
        StringBuilder refString = new StringBuilder();

        refString.append(IDENTITY_VERSION_1);
        refString.append(IDENTITY_SEPARATOR);
        refString.append(Utility.base64Encode(accountUuid));
        refString.append(IDENTITY_SEPARATOR);
        refString.append(Utility.base64Encode(folderName));
        refString.append(IDENTITY_SEPARATOR);
        refString.append(Utility.base64Encode(uid));
        if (flag != null)
        {
            refString.append(IDENTITY_SEPARATOR);
            refString.append(flag.name());
        }

        return refString.toString();
    }

    @Override
    public boolean equals(Object o)
    {
        if (o instanceof MessageReference == false)
        {
            return false;
        }
        MessageReference other = (MessageReference)o;
        if ((accountUuid == other.accountUuid || (accountUuid != null && accountUuid.equals(other.accountUuid)))
                && (folderName == other.folderName || (folderName != null && folderName.equals(other.folderName)))
                && (uid == other.uid || (uid != null && uid.equals(other.uid))))
        {
            return true;
        }
        return false;
    }

    @Override
    public int hashCode()
    {
        final int MULTIPLIER = 31;

        int result = 1;
        result = MULTIPLIER * result + ((accountUuid == null) ? 0 : accountUuid.hashCode());
        result = MULTIPLIER * result + ((folderName == null) ? 0 : folderName.hashCode());
        result = MULTIPLIER * result + ((uid == null) ? 0 : uid.hashCode());
        return result;
    }

    @Override
    public String toString()
    {
        return "MessageReference{" +
                "accountUuid='" + accountUuid + '\'' +
                ", folderName='" + folderName + '\'' +
                ", uid='" + uid + '\'' +
                ", flag=" + flag +
                '}';
    }
}
