
package com.fsck.k9.mail;

import java.util.ArrayList;

import com.fsck.k9.mail.internet.MimeHeader;
import com.fsck.k9.mail.internet.MimeUtility;
import com.fsck.k9.mail.internet.TextBody;

public abstract class Multipart implements Body
{
    protected Part mParent;

    protected ArrayList<BodyPart> mParts = new ArrayList<BodyPart>();

    protected String mContentType;

    public void addBodyPart(BodyPart part)
    {
        mParts.add(part);
        part.setParent(this);
    }

    public void addBodyPart(BodyPart part, int index)
    {
        mParts.add(index, part);
        part.setParent(this);
    }

    public BodyPart getBodyPart(int index)
    {
        return mParts.get(index);
    }

    public String getContentType()
    {
        return mContentType;
    }

    public int getCount()
    {
        return mParts.size();
    }

    public boolean removeBodyPart(BodyPart part)
    {
        part.setParent(null);
        return mParts.remove(part);
    }

    public void removeBodyPart(int index)
    {
        mParts.get(index).setParent(null);
        mParts.remove(index);
    }

    public Part getParent()
    {
        return mParent;
    }

    public void setParent(Part parent)
    {
        this.mParent = parent;
    }

    public void setEncoding(String encoding)
    {
        for (BodyPart part : mParts)
        {
            try
            {
                Body body = part.getBody();
                if (body instanceof TextBody)
                {
                    part.setHeader(MimeHeader.HEADER_CONTENT_TRANSFER_ENCODING, encoding);
                    ((TextBody)body).setEncoding(encoding);
                }
            }
            catch (MessagingException e)
            {
                // Ignore
            }
        }

    }

    public void setCharset(String charset) throws MessagingException
    {
        if (mParts.isEmpty())
            return;

        BodyPart part = mParts.get(0);
        Body body = part.getBody();
        if (body instanceof TextBody)
        {
            MimeUtility.setCharset(charset, part);
            ((TextBody)body).setCharset(charset);
        }
    }
}
