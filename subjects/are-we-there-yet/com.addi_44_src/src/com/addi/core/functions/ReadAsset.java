// Copyright (C) 2011 Free Software Foundation FSF
//
// This file is part of Addi.
//
// Addi is free software; you can redistribute it and/or modify it
// under the terms of the GNU General Public License as published by
// the Free Software Foundation; either version 3 of the License, or (at
// your option) any later version.
//
// Addi is distributed in the hope that it will be useful, but
// WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
// General Public License for more details.
//
// You should have received a copy of the GNU General Public License
// along with Addi. If not, see <http://www.gnu.org/licenses/>.

package com.addi.core.functions;

import android.app.Activity;
import android.os.Bundle;
import android.widget.TextView;

import java.io.IOException;
import java.io.InputStream;


/**
 * Demonstration of styled text resources.
 */
public class ReadAsset extends Activity
{

	public String readAsset(String asset) {

        // Programmatically load text from an asset and place it into the
        // text view.  Note that the text we are loading is ASCII, so we
        // need to convert it to UTF-16.
        try {
            InputStream is = getAssets().open(asset);

            // We guarantee that the available method returns the total
            // size of the asset...  of course, this does mean that a single
            // asset can't be more than 2 gigs.
            int size = is.available();

            // Read the entire asset into a local byte buffer.
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            // Convert the buffer into a string.
            String text = new String(buffer);
            
            return text;

        } catch (IOException e) {
            // Should never happen!
            throw new RuntimeException(e);
        }
	}
}

//package aexp.assets;
//
//import android.app.Activity;
//import android.content.res.AssetManager;
//import android.os.Bundle;
//import android.util.Log;
//import android.widget.TextView;
//import java.io.IOException;
//import java.io.InputStream;
//
//public class ReadAsset extends Activity
//{
//    public static final String LOG_TAG = "Assets";
//
//    static final int views[] = {
//        R.id.f1,
//        R.id.f2,
//        R.id.f3
//    };
//
//    /** Called when the activity is first created. */
//    @Override
//    public void onCreate(Bundle savedInstanceState)
//    {
//        super.onCreate(savedInstanceState);
//        setContentView(R.layout.main);
//        AssetManager am = getResources().getAssets();
//        String assets[] = null;
//        try {
//            assets = am.list( "" );
//            for( int i = 0 ; i < views.length ; ++i ) {
//                if( i >= assets.length )
//                    break;
//                TextView t = (TextView)findViewById( views[i] );
//                readTextResource( am, t, assets[i] );
//            }
//        } catch( IOException ex ) {
//            Log.e( LOG_TAG, 
//                    "I/O Exception",
//                    ex );
//        }
//    }
//
//    private void readTextResource( AssetManager am, TextView t,String asset ) 
//                                        throws IOException {
//        InputStream is = am.open( asset );
//        StringBuffer sb = new StringBuffer();
//        while( true ) {
//            int c = is.read();
//            if( c < 0 )
//                break;
//            if( c >= 32 )
//                sb.append( (char)c );
//        }
//        t.setText( new String( sb ) );
//    }
//}
