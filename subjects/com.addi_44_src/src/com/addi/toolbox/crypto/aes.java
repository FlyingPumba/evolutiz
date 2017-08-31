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

package com.addi.toolbox.crypto;

import com.addi.core.functions.ExternalFunction;
import com.addi.core.interpreter.GlobalValues;
import com.addi.core.tokens.*;
import com.addi.core.tokens.numbertokens.DoubleNumberToken;



/**An external function for computing a mesh of a matrix  */
public class aes extends ExternalFunction
{
    /**returns two  matrices 
    * @param operands[0] = x values (e.g. [-2:0.2:2]) 
    * @param operands[1] = y values (e.g. [-2:0.2:2])
    * @param operands[2] = y values (e.g. [-2:0.2:2])
    * @return [X,Y] as matrices                                 */
    public OperandToken evaluate(Token[] operands, GlobalValues globals)
    {

        // one operand 
        if (getNArgIn(operands)!=3)
            throwMathLibException("AES: number of input arguments != 3");

        if (!(operands[0] instanceof CharToken))
            throwMathLibException("AES: first argument: encrypt or decrypt");
        
        if ( !(operands[1] instanceof DoubleNumberToken) || 
             !(operands[2] instanceof DoubleNumberToken)    )
            throwMathLibException("AES: works only on numbers");

        // get data from arguments
        String     type = ((CharToken)operands[0]).getValue();
        double[][] x    = ((DoubleNumberToken)operands[1]).getReValues();
        double[][] y    = ((DoubleNumberToken)operands[2]).getReValues();

        // AES works only on blocks of 16 bytes = 128bits
        if ((x.length    !=  1) ||
            (x[0].length != 16) ||
            (y.length    !=  1) ||
            (y[0].length != 16)    )
            throwMathLibException("AES: works only on row vectors with 16 bytes");

       
        // convert key from double to byte array
        byte[] cipherKey = new byte[16];
        for (int i=0; i<16; i++)
        {
            // check if key numbers are positiv and only 8 bits max
            if ((y[0][i]<0) || (y[0][i]>255))
                throwMathLibException("AES expects input between 0 (0x0) and 255 (0xff)");     

            cipherKey[i] = new Double(y[0][i]).byteValue();
        }

        // prepare key
        makeKey(cipherKey, 128 );

        // allocate memory of plain text and cipher text
        byte[] plainText  = { 0, 0, 0, 0,  0, 0, 0, 0,
                              0, 0, 0, 0,  0, 0, 0, 0 };
        byte[] cipherText = { 0, 0, 0, 0,  0, 0, 0, 0,
                              0, 0, 0, 0,  0, 0, 0, 0 };
       
        // allocate memry for return numbers
        double[][] ret = new double[1][16];
       

        // encryption or decryption
        if (type.toLowerCase().substring(0,1).equals("e"))
        {
            // encrypt
 
            // convert plain text to byte array
            for (int i=0; i<16; i++)
            {
                // check if  numbers are positiv and only 8 bits max
                if ((x[0][i]<0) || (x[0][i]>255))
                    throwMathLibException("AES expects plain text between 0 (0x0) and 255 (0xff)");     
                
                plainText[i] = new Double(x[0][i]).byteValue();
            }
            
            // compute encryption
            encrypt(plainText, cipherText);

            // convert byte to double
            for (int i=0; i<16; i++)
            {
                ret[0][i] = (double)(cipherText[i]);

                // move range -128 to 127  into the range 0 to 255
                if (ret[0][i]<0)
                    ret[0][i] = 256 + ret[0][i];
            }

        }
        else
        {
            // decrypt
            
            // convert cipher text to byte array
            for (int i=0; i<16; i++)
            {
                // check if  numbers are positiv and only 8 bits max
                if ((x[0][i]<0) || (x[0][i]>255))
                    throwMathLibException("AES expects cypher text between 0 (0x0) and 255 (0xff)");     

                cipherText[i] = new Double(x[0][i]).byteValue();
            }

            // compute decryption
            decrypt(cipherText, plainText);

            // convert byte to double
            for (int i=0; i<16; i++)
            {
                ret[0][i] = (double)(plainText[i]);

                // move range -128 to 127  into the range 0 to 255
                if (ret[0][i]<0)
                    ret[0][i] = 256 + ret[0][i];
            }


        } // end encrypt/decrypt
        
        //try {
        //    System.out.println("cipher Key "      + new String(cipherKey,  "UTF8") );
        //    System.out.println("plaintext block  "+ new String(plainText,  "UTF8") );
        //    System.out.println("ciphertext block "+ new String(cipherText, "UTF8") );
        //}
        //catch (Exception e){;}
        
        
        return new DoubleNumberToken(ret);

    } // end eval



/**
 * AES.java
 *
 * The Advanced Encryption Standard (Rijndael, aka AES) block cipher,
 * designed by J. Daemen and V. Rijmen.
 *
 * @author Paulo S. L. M. Barreto
 *
 * This software is hereby placed in the public domain.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHORS ''AS IS'' AND ANY EXPRESS
 * OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE
 * ARE DISCLAIMED.  IN NO EVENT SHALL THE AUTHORS OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF
 * SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR
 * BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
 * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE
 * OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE,
 * EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
//public final class AES { //extends BlockCipher {

//    public AES() {
//    }

    /**
     * AES block size in bits
     * (N.B. the Rijndael algorithm itself allows for other sizes).
     */
    public static final int BLOCK_BITS  = 128;

    /**
     * AES block size in bytes
     * (N.B. the Rijndael algorithm itself allows for other sizes).
     */
    public static final int BLOCK_SIZE  = (BLOCK_BITS >>> 3);

    
    public static final int DIR_DECRYPT = 1;
    public static final int DIR_ENCRYPT = 2;
    public static final int DIR_BOTH    = 3;
    
    
    /**
     * Block size in bytes
     */
    public final int blockSize() {
        return BLOCK_SIZE;
    }

    /**
     * Block size in bytes
     */
    public final int keySize() {
        return Nk << 5;
    }

    /**
     * Substitution table (S-box).
     */
    private static final String SS =
        "\u637C\u777B\uF26B\u6FC5\u3001\u672B\uFED7\uAB76" +
        "\uCA82\uC97D\uFA59\u47F0\uADD4\uA2AF\u9CA4\u72C0" +
        "\uB7FD\u9326\u363F\uF7CC\u34A5\uE5F1\u71D8\u3115" +
        "\u04C7\u23C3\u1896\u059A\u0712\u80E2\uEB27\uB275" +
        "\u0983\u2C1A\u1B6E\u5AA0\u523B\uD6B3\u29E3\u2F84" +
        "\u53D1\u00ED\u20FC\uB15B\u6ACB\uBE39\u4A4C\u58CF" +
        "\uD0EF\uAAFB\u434D\u3385\u45F9\u027F\u503C\u9FA8" +
        "\u51A3\u408F\u929D\u38F5\uBCB6\uDA21\u10FF\uF3D2" +
        "\uCD0C\u13EC\u5F97\u4417\uC4A7\u7E3D\u645D\u1973" +
        "\u6081\u4FDC\u222A\u9088\u46EE\uB814\uDE5E\u0BDB" +
        "\uE032\u3A0A\u4906\u245C\uC2D3\uAC62\u9195\uE479" +
        "\uE7C8\u376D\u8DD5\u4EA9\u6C56\uF4EA\u657A\uAE08" +
        "\uBA78\u252E\u1CA6\uB4C6\uE8DD\u741F\u4BBD\u8B8A" +
        "\u703E\uB566\u4803\uF60E\u6135\u57B9\u86C1\u1D9E" +
        "\uE1F8\u9811\u69D9\u8E94\u9B1E\u87E9\uCE55\u28DF" +
        "\u8CA1\u890D\uBFE6\u4268\u4199\u2D0F\uB054\uBB16";

    private static final byte[]
        Se = new byte[256];

    private static final int[]
        Te0 = new int[256],
        Te1 = new int[256],
        Te2 = new int[256],
        Te3 = new int[256];

    private static final byte[]
        Sd = new byte[256];

    private static final int[]
        Td0 = new int[256],
        Td1 = new int[256],
        Td2 = new int[256],
        Td3 = new int[256];

    /**
     * Round constants
     */
    private static final int[]
        rcon = new int[10]; /* for 128-bit blocks, Rijndael never uses more than 10 rcon values */

    /**
     * Number of rounds (depends on key size).
     */
    private int Nr = 0;

    private int Nk = 0;

    private int Nw = 0;

    /**
     * Encryption key schedule
     */
    private int rek[] = null;

    /**
     * Decryption key schedule
     */
    private int rdk[] = null;

    static {
        /*
            Te0[x] = Se[x].[02, 01, 01, 03];
            Te1[x] = Se[x].[03, 02, 01, 01];
            Te2[x] = Se[x].[01, 03, 02, 01];
            Te3[x] = Se[x].[01, 01, 03, 02];

            Td0[x] = Sd[x].[0e, 09, 0d, 0b];
            Td1[x] = Sd[x].[0b, 0e, 09, 0d];
            Td2[x] = Sd[x].[0d, 0b, 0e, 09];
            Td3[x] = Sd[x].[09, 0d, 0b, 0e];
        */
        int ROOT = 0x11B;
        int s1, s2, s3, i1, i2, i4, i8, i9, ib, id, ie, t;
        for (i1 = 0; i1 < 256; i1++) {
            char c = SS.charAt(i1 >>> 1);
            s1 = (byte)((i1 & 1) == 0 ? c >>> 8 : c) & 0xff;
            s2 = s1 << 1;
            if (s2 >= 0x100) {
                s2 ^= ROOT;
            }
            s3 = s2 ^ s1;
            i2 = i1 << 1;
            if (i2 >= 0x100) {
                i2 ^= ROOT;
            }
            i4 = i2 << 1;
            if (i4 >= 0x100) {
                i4 ^= ROOT;
            }
            i8 = i4 << 1;
            if (i8 >= 0x100) {
                i8 ^= ROOT;
            }
            i9 = i8 ^ i1;
            ib = i9 ^ i2;
            id = i9 ^ i4;
            ie = i8 ^ i4 ^ i2;

            Se[i1] = (byte)s1;
            Te0[i1] = t = (s2 << 24) | (s1 << 16) | (s1 << 8) | s3;
            Te1[i1] = (t >>>  8) | (t  << 24);
            Te2[i1] = (t >>> 16) | (t  << 16);
            Te3[i1] = (t >>> 24) | (t  <<  8);

            Sd[s1] = (byte)i1;
            Td0[s1] = t = (ie << 24) | (i9 << 16) | (id << 8) | ib;
            Td1[s1] = (t >>>  8) | (t  << 24);
            Td2[s1] = (t >>> 16) | (t  << 16);
            Td3[s1] = (t >>> 24) | (t  <<  8);
		}
        /*
         * round constants
         */
        int r = 1;
        rcon[0] = r << 24;
        for (int i = 1; i < 10; i++) {
            r <<= 1;
            if (r >= 0x100) {
                r ^= ROOT;
            }
            rcon[i] = r << 24;
        }
    }

    /**
     * Expand a cipher key into a full encryption key schedule.
     *
     * @param   cipherKey   the cipher key (128, 192, or 256 bits).
     */
    private final void expandKey(byte[] cipherKey) {
        int temp, r = 0;
        for (int i = 0, k = 0; i < Nk; i++, k += 4) {
            rek[i] =
                ((cipherKey[k    ]       ) << 24) |
                ((cipherKey[k + 1] & 0xff) << 16) |
                ((cipherKey[k + 2] & 0xff) <<  8) |
                ((cipherKey[k + 3] & 0xff));
        }
        for (int i = Nk, n = 0; i < Nw; i++, n--) {
            temp = rek[i - 1];
            if (n == 0) {
            	n = Nk;
                temp =
                    ((Se[(temp >>> 16) & 0xff]       ) << 24) |
                    ((Se[(temp >>>  8) & 0xff] & 0xff) << 16) |
                    ((Se[(temp       ) & 0xff] & 0xff) <<  8) |
                    ((Se[(temp >>> 24)       ] & 0xff));
                temp ^= rcon[r++];
            } else if (Nk == 8 && n == 4) {
                temp =
                    ((Se[(temp >>> 24)       ]       ) << 24) |
                    ((Se[(temp >>> 16) & 0xff] & 0xff) << 16) |
                    ((Se[(temp >>>  8) & 0xff] & 0xff) <<  8) |
                    ((Se[(temp       ) & 0xff] & 0xff));
            }
            rek[i] = rek[i - Nk] ^ temp;
        }
        temp = 0;
    }


    /**
     * Compute the decryption schedule from the encryption schedule .
     */
    private final void invertKey() {
        int d = 0, e = 4*Nr, w;
	    /*
         * apply the inverse MixColumn transform to all round keys
         * but the first and the last:
         */
        rdk[d    ] = rek[e    ];
        rdk[d + 1] = rek[e + 1];
        rdk[d + 2] = rek[e + 2];
        rdk[d + 3] = rek[e + 3];
	    d += 4;
	    e -= 4;
	    for (int r = 1; r < Nr; r++) {
            w = rek[e    ];
	    	rdk[d    ] =
	    		Td0[Se[(w >>> 24)       ] & 0xff] ^
	    		Td1[Se[(w >>> 16) & 0xff] & 0xff] ^
	    		Td2[Se[(w >>>  8) & 0xff] & 0xff] ^
	    		Td3[Se[(w       ) & 0xff] & 0xff];
            w = rek[e + 1];
	    	rdk[d + 1] =
	    		Td0[Se[(w >>> 24)       ] & 0xff] ^
	    		Td1[Se[(w >>> 16) & 0xff] & 0xff] ^
	    		Td2[Se[(w >>>  8) & 0xff] & 0xff] ^
	    		Td3[Se[(w       ) & 0xff] & 0xff];
            w = rek[e + 2];
	    	rdk[d + 2] =
	    		Td0[Se[(w >>> 24)       ] & 0xff] ^
	    		Td1[Se[(w >>> 16) & 0xff] & 0xff] ^
	    		Td2[Se[(w >>>  8) & 0xff] & 0xff] ^
	    		Td3[Se[(w       ) & 0xff] & 0xff];
            w = rek[e + 3];
	    	rdk[d + 3] =
	    		Td0[Se[(w >>> 24)       ] & 0xff] ^
	    		Td1[Se[(w >>> 16) & 0xff] & 0xff] ^
	    		Td2[Se[(w >>>  8) & 0xff] & 0xff] ^
	    		Td3[Se[(w       ) & 0xff] & 0xff];
            d += 4;
            e -= 4;
	    }
        rdk[d    ] = rek[e    ];
        rdk[d + 1] = rek[e + 1];
        rdk[d + 2] = rek[e + 2];
        rdk[d + 3] = rek[e + 3];
    }

    /**
     * Setup the AES key schedule for encryption, decryption, or both.
     *
     * @param   cipherKey   the cipher key (128, 192, or 256 bits).
     * @param   keyBits     size of the cipher key in bits.
     * @param   direction   cipher direction (DIR_ENCRYPT, DIR_DECRYPT, or DIR_BOTH).
     */
    public final void makeKey(byte[] cipherKey, int keySize, int direction)
            throws RuntimeException {
        // check key size:
        switch (keySize) {
        case 16:
        case 24:
        case 32:
            keySize <<= 3; // keySize is now in bits
            break;
        case 128:
        case 192:
        case 256:
            break;
        default:
            throw new RuntimeException("Invalid AES key size (" + keySize + ")");
        }
        Nk = keySize >>> 5;
        Nr = Nk + 6;
        Nw = 4*(Nr + 1);
        rek = new int[Nw];
        rdk = new int[Nw];
        if ((direction & DIR_BOTH) != 0) {
            expandKey(cipherKey);
            /*
            for (int r = 0; r <= Nr; r++) {
            	System.out.print("RK" + r + "=");
            	for (int i = 0; i < 4; i++) {
            		int w = rek[4*r + i];
            		System.out.print(" " + Integer.toHexString(w));
            	}
            	System.out.println();
            }
            */
            if ((direction & DIR_DECRYPT) != 0) {
                invertKey();
            }
        }
    }

    /**
     * Setup the AES key schedule (any cipher direction).
     *
     * @param   cipherKey   the cipher key (128, 192, or 256 bits).
     * @param   keyBits     size of the cipher key in bits.
     */
    public final void makeKey(byte[] cipherKey, int keyBits)
            throws RuntimeException {
        makeKey(cipherKey, keyBits, DIR_BOTH);
    }

    /**
     * Encrypt exactly one block (BLOCK_SIZE bytes) of plaintext.
     *
     * @param   pt          plaintext block.
     * @param   ct          ciphertext block.
     */
    public final void encrypt(byte[] pt, byte[] ct) {
        /*
	     * map byte array block to cipher state
	     * and add initial round key:
	     */
        int k = 0, v;
        int t0   = ((pt[ 0]       ) << 24 |
                    (pt[ 1] & 0xff) << 16 |
                    (pt[ 2] & 0xff) <<  8 |
                    (pt[ 3] & 0xff)        ) ^ rek[0];
        int t1   = ((pt[ 4]       ) << 24 |
                    (pt[ 5] & 0xff) << 16 |
                    (pt[ 6] & 0xff) <<  8 |
                    (pt[ 7] & 0xff)        ) ^ rek[1];
        int t2   = ((pt[ 8]       ) << 24 |
                    (pt[ 9] & 0xff) << 16 |
                    (pt[10] & 0xff) <<  8 |
                    (pt[11] & 0xff)        ) ^ rek[2];
        int t3   = ((pt[12]       ) << 24 |
                    (pt[13] & 0xff) << 16 |
                    (pt[14] & 0xff) <<  8 |
                    (pt[15] & 0xff)        ) ^ rek[3];
        /*
	     * Nr - 1 full rounds:
	     */
        for (int r = 1; r < Nr; r++) {
            k += 4;
            int a0 =
                Te0[(t0 >>> 24)       ] ^
                Te1[(t1 >>> 16) & 0xff] ^
                Te2[(t2 >>>  8) & 0xff] ^
                Te3[(t3       ) & 0xff] ^
                rek[k    ];
            int a1 =
                Te0[(t1 >>> 24)       ] ^
                Te1[(t2 >>> 16) & 0xff] ^
                Te2[(t3 >>>  8) & 0xff] ^
                Te3[(t0       ) & 0xff] ^
                rek[k + 1];
            int a2 =
                Te0[(t2 >>> 24)       ] ^
                Te1[(t3 >>> 16) & 0xff] ^
                Te2[(t0 >>>  8) & 0xff] ^
                Te3[(t1       ) & 0xff] ^
                rek[k + 2];
            int a3 =
                Te0[(t3 >>> 24)       ] ^
                Te1[(t0 >>> 16) & 0xff] ^
                Te2[(t1 >>>  8) & 0xff] ^
                Te3[(t2       ) & 0xff] ^
                rek[k + 3];
            t0 = a0; t1 = a1; t2 = a2; t3 = a3;
        }
        /*
         * last round lacks MixColumn:
         */
        k += 4;

        v = rek[k    ];
        ct[ 0] = (byte)(Se[(t0 >>> 24)       ] ^ (v >>> 24));
        ct[ 1] = (byte)(Se[(t1 >>> 16) & 0xff] ^ (v >>> 16));
                ct[ 2] = (byte)(Se[(t2 >>>  8) & 0xff] ^ (v >>>  8));
        ct[ 3] = (byte)(Se[(t3       ) & 0xff] ^ (v       ));

        v = rek[k + 1];
        ct[ 4] = (byte)(Se[(t1 >>> 24)       ] ^ (v >>> 24));
        ct[ 5] = (byte)(Se[(t2 >>> 16) & 0xff] ^ (v >>> 16));
        ct[ 6] = (byte)(Se[(t3 >>>  8) & 0xff] ^ (v >>>  8));
        ct[ 7] = (byte)(Se[(t0       ) & 0xff] ^ (v       ));

        v = rek[k + 2];
        ct[ 8] = (byte)(Se[(t2 >>> 24)       ] ^ (v >>> 24));
        ct[ 9] = (byte)(Se[(t3 >>> 16) & 0xff] ^ (v >>> 16));
        ct[10] = (byte)(Se[(t0 >>>  8) & 0xff] ^ (v >>>  8));
        ct[11] = (byte)(Se[(t1       ) & 0xff] ^ (v       ));

        v = rek[k + 3];
        ct[12] = (byte)(Se[(t3 >>> 24)       ] ^ (v >>> 24));
        ct[13] = (byte)(Se[(t0 >>> 16) & 0xff] ^ (v >>> 16));
        ct[14] = (byte)(Se[(t1 >>>  8) & 0xff] ^ (v >>>  8));
        ct[15] = (byte)(Se[(t2       ) & 0xff] ^ (v       ));
    }

    /**
     * Decrypt exactly one block (BLOCK_SIZE bytes) of ciphertext.
     *
     * @param   ct          ciphertext block.
     * @param   pt          plaintext block.
     */
    public final void decrypt(byte[] ct, byte[] pt) {
        /*
	     * map byte array block to cipher state
	     * and add initial round key:
	     */
        int k = 0, v;
        int t0 =   ((ct[ 0]       ) << 24 |
                    (ct[ 1] & 0xff) << 16 |
                    (ct[ 2] & 0xff) <<  8 |
                    (ct[ 3] & 0xff)        ) ^ rdk[0];
        int t1 =   ((ct[ 4]       ) << 24 |
                    (ct[ 5] & 0xff) << 16 |
                    (ct[ 6] & 0xff) <<  8 |
                    (ct[ 7] & 0xff)        ) ^ rdk[1];
        int t2 =   ((ct[ 8]       ) << 24 |
                    (ct[ 9] & 0xff) << 16 |
                    (ct[10] & 0xff) <<  8 |
                    (ct[11] & 0xff)        ) ^ rdk[2];
        int t3 =   ((ct[12]       ) << 24 |
                    (ct[13] & 0xff) << 16 |
                    (ct[14] & 0xff) <<  8 |
                    (ct[15] & 0xff)        ) ^ rdk[3];
        /*
	     * Nr - 1 full rounds:
	     */
        for (int r = 1; r < Nr; r++) {
            k += 4;
            int a0 =
                Td0[(t0 >>> 24)       ] ^
                Td1[(t3 >>> 16) & 0xff] ^
                Td2[(t2 >>>  8) & 0xff] ^
                Td3[(t1       ) & 0xff] ^
                rdk[k    ];
            int a1 =
                Td0[(t1 >>> 24)       ] ^
                Td1[(t0 >>> 16) & 0xff] ^
                Td2[(t3 >>>  8) & 0xff] ^
                Td3[(t2       ) & 0xff] ^
                rdk[k + 1];
            int a2 =
                Td0[(t2 >>> 24)       ] ^
                Td1[(t1 >>> 16) & 0xff] ^
                Td2[(t0 >>>  8) & 0xff] ^
                Td3[(t3       ) & 0xff] ^
                rdk[k + 2];
            int a3 =
                Td0[(t3 >>> 24)       ] ^
                Td1[(t2 >>> 16) & 0xff] ^
                Td2[(t1 >>>  8) & 0xff] ^
                Td3[(t0       ) & 0xff] ^
                rdk[k + 3];
            t0 = a0; t1 = a1; t2 = a2; t3 = a3;
        }
        /*
         * last round lacks MixColumn:
         */
        k += 4;

        v = rdk[k    ];
        pt[ 0] = (byte)(Sd[(t0 >>> 24)       ] ^ (v >>> 24));
        pt[ 1] = (byte)(Sd[(t3 >>> 16) & 0xff] ^ (v >>> 16));
        pt[ 2] = (byte)(Sd[(t2 >>>  8) & 0xff] ^ (v >>>  8));
        pt[ 3] = (byte)(Sd[(t1       ) & 0xff] ^ (v       ));

        v = rdk[k + 1];
        pt[ 4] = (byte)(Sd[(t1 >>> 24)       ] ^ (v >>> 24));
        pt[ 5] = (byte)(Sd[(t0 >>> 16) & 0xff] ^ (v >>> 16));
        pt[ 6] = (byte)(Sd[(t3 >>>  8) & 0xff] ^ (v >>>  8));
        pt[ 7] = (byte)(Sd[(t2       ) & 0xff] ^ (v       ));

        v = rdk[k + 2];
        pt[ 8] = (byte)(Sd[(t2 >>> 24)       ] ^ (v >>> 24));
        pt[ 9] = (byte)(Sd[(t1 >>> 16) & 0xff] ^ (v >>> 16));
        pt[10] = (byte)(Sd[(t0 >>>  8) & 0xff] ^ (v >>>  8));
        pt[11] = (byte)(Sd[(t3       ) & 0xff] ^ (v       ));

        v = rdk[k + 3];
        pt[12] = (byte)(Sd[(t3 >>> 24)       ] ^ (v >>> 24));
        pt[13] = (byte)(Sd[(t2 >>> 16) & 0xff] ^ (v >>> 16));
        pt[14] = (byte)(Sd[(t1 >>>  8) & 0xff] ^ (v >>>  8));
        pt[15] = (byte)(Sd[(t0       ) & 0xff] ^ (v       ));
    }

    /**
     * Destroy all sensitive information in this object.
     */
    public final void finalize() {
        if (rek != null) {
            for (int i = 0; i < rek.length; i++) {
                rek[i] = 0;
            }
            rek = null;
        }
        if (rdk != null) {
            for (int i = 0; i < rdk.length; i++) {
                rdk[i] = 0;
            }
            rdk = null;
        }
    }
}



/*
@GROUP
Crypto
@SYNTAX
answer = AES ("encrypt", plain, key) 
answer = AES ("e",       plain, key) 
answer = AES ("decrypt", cipher, key) 
answer = AES ("d",       cipher, key) 
@DOC
does encryption and descrption using the AES (advanced encryption standard) algorithm
@EXAMPLES
<programlisting>
> k=str2num("0987098709870987")  
k=[48,57,56,55,48,57,56,55,48,57,56,55,48,57,56,55] 

> a=str2num("asdfasdfasdfasdf") 
a=[97,115,100,102,97,115,100,102,97,115,100,102,97,115,100,102] 

> b=aes("e",a,k) 
b=[-103,34,84,18,-46,-58,35,89,80,17,81,118,-4,-15,-93,-31] 

> c=aes("d",b,k) 
c=[97,115,100,102,97,115,100,102,97,115,100,102,97,115,100,102] 

> num2str(c) 
 ans = asdfasdfasdfasdf 
</programlisting>
@NOTES
@SEE
*/