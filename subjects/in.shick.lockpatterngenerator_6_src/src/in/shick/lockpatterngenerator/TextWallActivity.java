/*
Copyright 2010-2012 Michael Shick

This file is part of 'Lock Pattern Generator'.

'Lock Pattern Generator' is free software: you can redistribute it and/or
modify it under the terms of the GNU General Public License as published by the
Free Software Foundation, either version 3 of the License, or (at your option)
any later version.

'Lock Pattern Generator' is distributed in the hope that it will be useful, but
WITHOUT ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
FITNESS FOR A PARTICULAR PURPOSE.  See the GNU General Public License for more
details.

You should have received a copy of the GNU General Public License along with
'Lock Pattern Generator'.  If not, see <http://www.gnu.org/licenses/>.
*/
package in.shick.lockpatterngenerator;

import android.content.Intent;
import android.content.res.AssetManager;
import android.os.Bundle;
import android.text.Html;
import android.text.method.LinkMovementMethod;
import android.view.View;
import android.widget.TextView;

import java.io.InputStream;

public class TextWallActivity extends BaseActivity
{
    public static final String EXTRA_LAYOUT_RESOURCE = "layout_res";
    public static final String EXTRA_HTML_ASSET = "html_source";
    public static final int DEFAULT_LAYOUT_RESOURCE = R.layout.text_wall_basic;

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        int layout = getIntent().getIntExtra(EXTRA_LAYOUT_RESOURCE,
                DEFAULT_LAYOUT_RESOURCE);
        setContentView(layout);

        TextView textWall = (TextView) findViewById(R.id.text_wall);
        String wallText = getString(R.string.text_wall_failure);

        String htmlAsset = getIntent().getStringExtra(EXTRA_HTML_ASSET);

        AssetManager assetManager = this.getAssets();
        try
        {
            InputStream input = assetManager.open(htmlAsset);
            byte bytes[] = new byte[input.available()];
            input.read(bytes);
            wallText = new String(bytes);
        }
        catch(Throwable e)
        {
            wallText += e.toString();
        }

        textWall.setText(Html.fromHtml(wallText));
        textWall.setMovementMethod(LinkMovementMethod.getInstance());
        textWall.setClickable(false);
        textWall.setLongClickable(false);
    }
}
