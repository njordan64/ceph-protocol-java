package ca.venom.ceph.client;

import ca.venom.ceph.protocol.CephDecoder;
import ca.venom.ceph.protocol.frames.AuthDoneFrame;
import ca.venom.ceph.protocol.frames.AuthReplyMoreFrame;
import ca.venom.ceph.protocol.frames.AuthRequestFrame;
import ca.venom.ceph.protocol.frames.AuthRequestMoreFrame;
import ca.venom.ceph.protocol.frames.AuthSignatureFrame;
import ca.venom.ceph.protocol.types.auth.CephXServiceTicket;
import ca.venom.ceph.utils.HexFunctions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;

/*
actual_tx_sig=cd7d5cfaa7e989a02e6ba58e7ab1b4265fabef0509b3011aa18abd06a4fa2d79
sig_frame.signature()=79d7d052ddce25cf516b512e8fad987b95b6c83b9bc9c2c83f3d5d4e18203514
 */

/*
    00000000  63 65 70 68 20 76 32 0a  10 00 03 00 00 00 00 00   ceph v2. ........
    00000010  00 00 00 00 00 00 00 00  00 00                     ........ ..
00000000  63 65 70 68 20 76 32 0a  10 00 01 00 00 00 00 00   ceph v2. ........
00000010  00 00 01 00 00 00 00 00  00 00                     ........ ..
    0000001A  01 01 24 00 00 00 08 00  00 00 00 00 00 00 00 00   ..$..... ........
    0000002A  00 00 00 00 00 00 00 00  00 00 00 00 3f bd 6b 06   ........ ....?.k.
    0000003A  01 01 01 01 1c 00 00 00  02 00 00 00 00 00 00 00   ........ ........
    0000004A  10 00 00 00 02 00 ce 96  c0 a8 7a 01 00 00 00 00   ........ ..z.....
    0000005A  00 00 00 00 ba 3d 82 c9                            .....=..
0000001A  01 01 24 00 00 00 08 00  00 00 00 00 00 00 00 00   ..$..... ........
0000002A  00 00 00 00 00 00 00 00  00 00 00 00 3f bd 6b 06   ........ ....?.k.
0000003A  08 01 01 01 1c 00 00 00  02 00 00 00 00 00 00 00   ........ ........
0000004A  10 00 00 00 02 00 ab dc  7f 00 00 01 00 00 00 00   ........ ........
0000005A  00 00 00 00 a2 13 4c 16                            ......L.
00000062  02 01 2a 00 00 00 08 00  00 00 00 00 00 00 00 00   ..*..... ........
00000072  00 00 00 00 00 00 00 00  00 00 00 00 39 18 c6 60   ........ ....9..`
00000082  02 00 00 00 02 00 00 00  02 00 00 00 01 00 00 00   ........ ........
00000092  16 00 00 00 0a 08 00 00  00 05 00 00 00 61 64 6d   ........ .....adm
000000A2  69 6e 00 00 00 00 00 00  00 00 8a 3c 91 2e         in...... ...<..
    00000062  04 01 0d 00 00 00 08 00  00 00 00 00 00 00 00 00   ........ ........
    00000072  00 00 00 00 00 00 00 00  00 00 00 00 6a 09 4b c5   ........ ....j.K.
    00000082  09 00 00 00 01 f1 2a 6f  0a f0 35 3a 78 53 79 94   ......*o ..5:xSy.
    00000092  fd                                                 .
000000B0  05 01 28 00 00 00 08 00  00 00 00 00 00 00 00 00   ..(..... ........
000000C0  00 00 00 00 00 00 00 00  00 00 00 00 b8 3a c7 5b   ........ .....:.[
000000D0  24 00 00 00 00 01 03 ac  f8 81 6b 39 72 5c 51 88   $....... ..k9r\Q.
000000E0  b7 35 a9 7b c7 cc 57 01  00 00 00 00 00 00 00 00   .5.{..W. ........
000000F0  00 00 00 00 20 00 00 00  f7 46 0b b5               .... ... .F..
    00000093  06 01 22 01 00 00 08 00  00 00 00 00 00 00 00 00   .."..... ........
    000000A3  00 00 00 00 00 00 00 00  00 00 00 00 1f 44 e4 a5   ........ .....D..
    000000B3  d0 0b 04 00 00 00 00 00  02 00 00 00 12 01 00 00   ........ ........
    000000C3  00 01 00 00 00 00 01 01  00 00 00 20 00 00 00 01   ........ ... ....
    000000D3  30 00 00 00 e0 93 f0 22  a0 d6 7f a9 df 59 44 c3   0......" .....YD.
    000000E3  62 bb d8 16 b1 78 44 f9  fe 69 b0 68 f1 5a 57 a4   b....xD. .i.h.ZW.
    000000F3  d2 cd 81 bd 30 14 ce c7  9e 4e f7 80 7b 2e 08 9f   ....0... .N..{...
    00000103  07 3f 3b 27 00 6d 00 00  00 01 12 00 00 00 00 00   .?;'.m.. ........
    00000113  00 00 60 00 00 00 de 4a  77 b9 de d1 99 5b fd b2   ..`....J w....[..
    00000123  f3 fc f1 8d a9 4c 75 c7  fa d4 62 54 24 17 33 70   .....Lu. ..bT$.3p
    00000133  f1 a6 d4 ed 4f ba 6f 50  a5 68 a3 ab 11 f2 01 e2   ....O.oP .h......
    00000143  d9 1e c3 2f 89 81 b2 70  5c b6 e2 94 5e 5d a7 7e   .../...p \...^].~
    00000153  3d 14 b7 61 1f 40 9c 7f  be 86 63 b4 31 cd 69 08   =..a.@.. ..c.1.i.
    00000163  b6 6e 82 ef f3 ef e5 8c  3f 69 4f cc 51 ac b8 7d   .n...... ?iO.Q..}
    00000173  5f 69 0f b4 ba a1 54 00  00 00 50 00 00 00 b2 e6   _i....T. ..P.....
    00000183  2f 89 2f 81 5a f8 5a 30  85 f2 52 59 11 96 11 cc   /./.Z.Z0 ..RY....
    00000193  f5 9a f1 16 41 35 98 1b  e8 bd 22 54 03 8e e7 2c   ....A5.. .."T...,
    000001A3  8e 27 3a 81 bc 34 85 55  b2 0c f5 33 a0 b5 f6 3c   .':..4.U ...3...<
    000001B3  69 16 d0 60 86 ca 11 42  ad 65 87 12 63 9a bb c4   i..`...B .e..c...
    000001C3  96 b3 31 96 7f e9 15 30  2c b5 6c a6 29 b1 00 00   ..1....0 ,.l.)...
    000001D3  00 00 b4 44 64 d9 aa ef  4b 4e 4c 2c 48 11 4e 37   ...Dd... KNL,H.N7
    000001E3  a7 aa 80 75 21 5a 71 08  c0 a8 a3 54 64 98 a7 5d   ...u!Zq. ...Td..]
    000001F3  2d 8e c2 56 6f 77 b3 45  03 48 69 46 8e d8 52 cf   -..Vow.E .HiF..R.
    00000203  a4 36 0f b4 d3 0d 82 12  8c 4c 09 4b dc cd 7e 74   .6...... .L.K..~t
    00000213  1d 00 01 bf de b5 b4 c1  15 e7 3a 3f 68 8e d6 5f   ........ ..:?h.._
    00000223  bb e7 0c 4e a6 fc d4 31  98 06 6a 7d c4 52 ad 77   ...N...1 ..j}.R.w
    00000233  f8 29 9e 0e 1c 6b                                  .)...k
000000FC  80 e9 86 2a 60 91 32 2c  07 9d 2a 90 fe 70 d7 d7   ...*`.2, ..*..p..
0000010C  1c af 7a f3 47 18 a3 5b  7c 4c dd 82 95 c6 d3 69   ..z.G..[ |L.....i
0000011C  1b 45 45 6b 1e cc d0 60  7a 5f 43 46 62 59 5a 97   .EEk...` z_CFbYZ.
0000012C  88 4f fd ba 50 9a 3e a3  10 11 d8 65 d6 c2 e7 50   .O..P.>. ...e...P
0000013C  1e 2f ce e6 d2 05 04 aa  e7 ba ca dc 65 47 c8 15   ./...... ....eG..
0000014C  f3 fb 57 09 25 c7 39 62  c1 83 3e 58 e1 be 51 a8   ..W.%.9b ..>X..Q.
0000015C  fe 1f 8b 6c b1 2f 85 82  3f ad 54 34 9f be cb df   ...l./.. ?.T4....
0000016C  19 cc 15 d8 28 de 86 1c  a4 cf f9 7a 37 25 92 66   ....(... ...z7%.f
0000017C  35 9e 82 5d 07 5d cb 52  c6 a1 67 0f 83 1d ad 65   5..].].R ..g....e
0000018C  d5 d1 29 cc e1 52 74 fa  07 2c 24 49 7f c3 ce 6e   ..)..Rt. .,$I...n
0000019C  aa a4 c0 a2 be 37 cf 11  1b 00 ec 40 e8 d6 99 53   .....7.. ...@...S
000001AC  4a 1d a8 58 57 07 9a fe  87 55 d8 b5 49 55 95 cf
 */
public class AESTest2 {
    private static final byte[] MAGIC_VALUE = new byte[] {
            (byte) 0x55, (byte) 0xaa, (byte) 0x26, (byte) 0x88,
            (byte) 0xad, (byte) 0x9c, (byte) 0x00, (byte) 0xff
    };
    private static final byte[] KEY_BYTES = new byte[] {
            (byte) 0x8e, (byte) 0x87, (byte) 0x3a, (byte) 0x66,
            (byte) 0x66, (byte) 0x66, (byte) 0x39, (byte) 0x22,
            (byte) 0x37, (byte) 0xc4, (byte) 0xf2, (byte) 0x7a,
            (byte) 0xbd, (byte) 0x1c, (byte) 0xc8, (byte) 0x9d
    };

    private static final String PROOF_IV = "cephsageyudagreg";

    private static final byte[] AUTH_REQUEST_BYTES = new byte[] {
            (byte) 0x02, (byte) 0x01, (byte) 0x2a, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x08, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x39, (byte) 0x18, (byte) 0xc6, (byte) 0x60,
            (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x01, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x16, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x0a, (byte) 0x08, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x05, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x61, (byte) 0x64, (byte) 0x6d,
            (byte) 0x69, (byte) 0x6e, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x8a, (byte) 0x3c,
            (byte) 0x91, (byte) 0x2e
    };

    private static final byte[] AUTH_REPLY_MORE_BYTES = new byte[] {
            (byte) 0x04, (byte) 0x01, (byte) 0x0d, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x08, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x6a, (byte) 0x09, (byte) 0x4b, (byte) 0xc5,
            (byte) 0x09, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x01, (byte) 0xf1, (byte) 0x2a, (byte) 0x6f,
            (byte) 0x0a, (byte) 0xf0, (byte) 0x35, (byte) 0x3a,
            (byte) 0x78, (byte) 0x53, (byte) 0x79, (byte) 0x94,
            (byte) 0xfd
    };

    private static final byte[] SERVER_CHALLENGE_BYTES = new byte[] {
            (byte) 0xf1, (byte) 0x2a, (byte) 0x6f, (byte) 0x0a,
            (byte) 0xf0, (byte) 0x35, (byte) 0x3a, (byte) 0x78
    };

    private static final byte[] AUTH_REQUEST_MORE_BYTES = new byte[] {
            (byte) 0x05, (byte) 0x01, (byte) 0x28, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x08, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0xb8, (byte) 0x3a, (byte) 0xc7, (byte) 0x5b,
            (byte) 0x24, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x01, (byte) 0x03, (byte) 0xac,
            (byte) 0xf8, (byte) 0x81, (byte) 0x6b, (byte) 0x39,
            (byte) 0x72, (byte) 0x5c, (byte) 0x51, (byte) 0x88,
            (byte) 0xb7, (byte) 0x35, (byte) 0xa9, (byte) 0x7b,
            (byte) 0xc7, (byte) 0xcc, (byte) 0x57, (byte) 0x01,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x20, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0xf7, (byte) 0x46, (byte) 0x0b, (byte) 0xb5
    };

    private static final byte[] CLIENT_NONCE_BYTES = new byte[] {
            (byte) 0xac, (byte) 0xf8, (byte) 0x81, (byte) 0x6b,
            (byte) 0x39, (byte) 0x72, (byte) 0x5c, (byte) 0x51
    };

    private static final byte[] CLIENT_PROOF = new byte[] {
            (byte) 0x88, (byte) 0xb7, (byte) 0x35, (byte) 0xa9,
            (byte) 0x7b, (byte) 0xc7, (byte) 0xcc, (byte) 0x57
    };

    private static final byte[] AUTH_DONE_1_BYTES = new byte[] {
            (byte) 0x06, (byte) 0x01, (byte) 0x22, (byte) 0x01,
            (byte) 0x00, (byte) 0x00, (byte) 0x08, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x1f, (byte) 0x44, (byte) 0xe4, (byte) 0xa5,
            (byte) 0xd0, (byte) 0x0b, (byte) 0x04, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x02, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x12, (byte) 0x01, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x01, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x01, (byte) 0x01,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x20,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x01,
            (byte) 0x30, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0xe0, (byte) 0x93, (byte) 0xf0, (byte) 0x22,
            (byte) 0xa0, (byte) 0xd6, (byte) 0x7f, (byte) 0xa9,
            (byte) 0xdf, (byte) 0x59, (byte) 0x44, (byte) 0xc3,
            (byte) 0x62, (byte) 0xbb, (byte) 0xd8, (byte) 0x16,
            (byte) 0xb1, (byte) 0x78, (byte) 0x44, (byte) 0xf9,
            (byte) 0xfe, (byte) 0x69, (byte) 0xb0, (byte) 0x68,
            (byte) 0xf1, (byte) 0x5a, (byte) 0x57, (byte) 0xa4,
            (byte) 0xd2, (byte) 0xcd, (byte) 0x81, (byte) 0xbd,
            (byte) 0x30, (byte) 0x14, (byte) 0xce, (byte) 0xc7,
            (byte) 0x9e, (byte) 0x4e, (byte) 0xf7, (byte) 0x80,
            (byte) 0x7b, (byte) 0x2e, (byte) 0x08, (byte) 0x9f,
            (byte) 0x07, (byte) 0x3f, (byte) 0x3b, (byte) 0x27,
            (byte) 0x00, (byte) 0x6d, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x01, (byte) 0x12, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x60, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0xde, (byte) 0x4a,
            (byte) 0x77, (byte) 0xb9, (byte) 0xde, (byte) 0xd1,
            (byte) 0x99, (byte) 0x5b, (byte) 0xfd, (byte) 0xb2,
            (byte) 0xf3, (byte) 0xfc, (byte) 0xf1, (byte) 0x8d,
            (byte) 0xa9, (byte) 0x4c, (byte) 0x75, (byte) 0xc7,
            (byte) 0xfa, (byte) 0xd4, (byte) 0x62, (byte) 0x54,
            (byte) 0x24, (byte) 0x17, (byte) 0x33, (byte) 0x70,
            (byte) 0xf1, (byte) 0xa6, (byte) 0xd4, (byte) 0xed,
            (byte) 0x4f, (byte) 0xba, (byte) 0x6f, (byte) 0x50,
            (byte) 0xa5, (byte) 0x68, (byte) 0xa3, (byte) 0xab,
            (byte) 0x11, (byte) 0xf2, (byte) 0x01, (byte) 0xe2,
            (byte) 0xd9, (byte) 0x1e, (byte) 0xc3, (byte) 0x2f,
            (byte) 0x89, (byte) 0x81, (byte) 0xb2, (byte) 0x70,
            (byte) 0x5c, (byte) 0xb6, (byte) 0xe2, (byte) 0x94,
            (byte) 0x5e, (byte) 0x5d, (byte) 0xa7, (byte) 0x7e,
            (byte) 0x3d, (byte) 0x14, (byte) 0xb7, (byte) 0x61,
            (byte) 0x1f, (byte) 0x40, (byte) 0x9c, (byte) 0x7f,
            (byte) 0xbe, (byte) 0x86, (byte) 0x63, (byte) 0xb4,
            (byte) 0x31, (byte) 0xcd, (byte) 0x69, (byte) 0x08,
            (byte) 0xb6, (byte) 0x6e, (byte) 0x82, (byte) 0xef,
            (byte) 0xf3, (byte) 0xef, (byte) 0xe5, (byte) 0x8c,
            (byte) 0x3f, (byte) 0x69, (byte) 0x4f, (byte) 0xcc,
            (byte) 0x51, (byte) 0xac, (byte) 0xb8, (byte) 0x7d,
            (byte) 0x5f, (byte) 0x69, (byte) 0x0f, (byte) 0xb4,
            (byte) 0xba, (byte) 0xa1, (byte) 0x54, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x50, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0xb2, (byte) 0xe6,
            (byte) 0x2f, (byte) 0x89, (byte) 0x2f, (byte) 0x81,
            (byte) 0x5a, (byte) 0xf8, (byte) 0x5a, (byte) 0x30,
            (byte) 0x85, (byte) 0xf2, (byte) 0x52, (byte) 0x59,
            (byte) 0x11, (byte) 0x96, (byte) 0x11, (byte) 0xcc,
            (byte) 0xf5, (byte) 0x9a, (byte) 0xf1, (byte) 0x16,
            (byte) 0x41, (byte) 0x35, (byte) 0x98, (byte) 0x1b,
            (byte) 0xe8, (byte) 0xbd, (byte) 0x22, (byte) 0x54,
            (byte) 0x03, (byte) 0x8e, (byte) 0xe7, (byte) 0x2c,
            (byte) 0x8e, (byte) 0x27, (byte) 0x3a, (byte) 0x81,
            (byte) 0xbc, (byte) 0x34, (byte) 0x85, (byte) 0x55,
            (byte) 0xb2, (byte) 0x0c, (byte) 0xf5, (byte) 0x33,
            (byte) 0xa0, (byte) 0xb5, (byte) 0xf6, (byte) 0x3c,
            (byte) 0x69, (byte) 0x16, (byte) 0xd0, (byte) 0x60,
            (byte) 0x86, (byte) 0xca, (byte) 0x11, (byte) 0x42,
            (byte) 0xad, (byte) 0x65, (byte) 0x87, (byte) 0x12,
            (byte) 0x63, (byte) 0x9a, (byte) 0xbb, (byte) 0xc4,
            (byte) 0x96, (byte) 0xb3, (byte) 0x31, (byte) 0x96,
            (byte) 0x7f, (byte) 0xe9, (byte) 0x15, (byte) 0x30,
            (byte) 0x2c, (byte) 0xb5, (byte) 0x6c, (byte) 0xa6,
            (byte) 0x29, (byte) 0xb1, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0xb4, (byte) 0x44,
            (byte) 0x64, (byte) 0xd9
    };

    private static final byte[] AUTH_SIG_SERVER_ENC_BYTES = new byte[] {
            (byte) 0xaa, (byte) 0xef, (byte) 0x4b, (byte) 0x4e,
            (byte) 0x4c, (byte) 0x2c, (byte) 0x48, (byte) 0x11,
            (byte) 0x4e, (byte) 0x37, (byte) 0xa7, (byte) 0xaa,
            (byte) 0x80, (byte) 0x75, (byte) 0x21, (byte) 0x5a,
            (byte) 0x71, (byte) 0x08, (byte) 0xc0, (byte) 0xa8,
            (byte) 0xa3, (byte) 0x54, (byte) 0x64, (byte) 0x98,
            (byte) 0xa7, (byte) 0x5d, (byte) 0x2d, (byte) 0x8e,
            (byte) 0xc2, (byte) 0x56, (byte) 0x6f, (byte) 0x77,
            (byte) 0xb3, (byte) 0x45, (byte) 0x03, (byte) 0x48,
            (byte) 0x69, (byte) 0x46, (byte) 0x8e, (byte) 0xd8,
            (byte) 0x52, (byte) 0xcf, (byte) 0xa4, (byte) 0x36,
            (byte) 0x0f, (byte) 0xb4, (byte) 0xd3, (byte) 0x0d,
            (byte) 0x82, (byte) 0x12, (byte) 0x8c, (byte) 0x4c,
            (byte) 0x09, (byte) 0x4b, (byte) 0xdc, (byte) 0xcd,
            (byte) 0x7e, (byte) 0x74, (byte) 0x1d, (byte) 0x00,
            (byte) 0x01, (byte) 0xbf, (byte) 0xde, (byte) 0xb5,
            (byte) 0xb4, (byte) 0xc1, (byte) 0x15, (byte) 0xe7,
            (byte) 0x3a, (byte) 0x3f, (byte) 0x68, (byte) 0x8e,
            (byte) 0xd6, (byte) 0x5f, (byte) 0xbb, (byte) 0xe7,
            (byte) 0x0c, (byte) 0x4e, (byte) 0xa6, (byte) 0xfc,
            (byte) 0xd4, (byte) 0x31, (byte) 0x98, (byte) 0x06,
            (byte) 0x6a, (byte) 0x7d, (byte) 0xc4, (byte) 0x52,
            (byte) 0xad, (byte) 0x77, (byte) 0xf8, (byte) 0x29,
            (byte) 0x9e, (byte) 0x0e, (byte) 0x1c, (byte) 0x6b
    };

    private static final byte[] AUTH_SIG_CLIENT_ENC_BYTES = new byte[] {
            (byte) 0x80, (byte) 0xe9, (byte) 0x86, (byte) 0x2a,
            (byte) 0x60, (byte) 0x91, (byte) 0x32, (byte) 0x2c,
            (byte) 0x07, (byte) 0x9d, (byte) 0x2a, (byte) 0x90,
            (byte) 0xfe, (byte) 0x70, (byte) 0xd7, (byte) 0xd7,
            (byte) 0x1c, (byte) 0xaf, (byte) 0x7a, (byte) 0xf3,
            (byte) 0x47, (byte) 0x18, (byte) 0xa3, (byte) 0x5b,
            (byte) 0x7c, (byte) 0x4c, (byte) 0xdd, (byte) 0x82,
            (byte) 0x95, (byte) 0xc6, (byte) 0xd3, (byte) 0x69,
            (byte) 0x1b, (byte) 0x45, (byte) 0x45, (byte) 0x6b,
            (byte) 0x1e, (byte) 0xcc, (byte) 0xd0, (byte) 0x60,
            (byte) 0x7a, (byte) 0x5f, (byte) 0x43, (byte) 0x46,
            (byte) 0x62, (byte) 0x59, (byte) 0x5a, (byte) 0x97,
            (byte) 0x88, (byte) 0x4f, (byte) 0xfd, (byte) 0xba,
            (byte) 0x50, (byte) 0x9a, (byte) 0x3e, (byte) 0xa3,
            (byte) 0x10, (byte) 0x11, (byte) 0xd8, (byte) 0x65,
            (byte) 0xd6, (byte) 0xc2, (byte) 0xe7, (byte) 0x50,
            (byte) 0x1e, (byte) 0x2f, (byte) 0xce, (byte) 0xe6,
            (byte) 0xd2, (byte) 0x05, (byte) 0x04, (byte) 0xaa,
            (byte) 0xe7, (byte) 0xba, (byte) 0xca, (byte) 0xdc,
            (byte) 0x65, (byte) 0x47, (byte) 0xc8, (byte) 0x15,
            (byte) 0xf3, (byte) 0xfb, (byte) 0x57, (byte) 0x09,
            (byte) 0x25, (byte) 0xc7, (byte) 0x39, (byte) 0x62,
            (byte) 0xc1, (byte) 0x83, (byte) 0x3e, (byte) 0x58,
            (byte) 0xe1, (byte) 0xbe, (byte) 0x51, (byte) 0xa8
    };

    public static void main(String[] args) throws Exception {
        byte[] bytesToEncrypt = new byte[25];
        bytesToEncrypt[0] = 1;
        System.arraycopy(MAGIC_VALUE, 0, bytesToEncrypt, 1, MAGIC_VALUE.length);

        AuthRequestFrame authRequestFrame = new AuthRequestFrame();
        ByteBuf byteBuf = Unpooled.wrappedBuffer(AUTH_REQUEST_BYTES, 32, AUTH_REQUEST_BYTES.length - 32);
        authRequestFrame.decodeSegment1(byteBuf, true);

        AuthReplyMoreFrame authReplyMoreFrame = new AuthReplyMoreFrame();
        byteBuf = Unpooled.wrappedBuffer(AUTH_REPLY_MORE_BYTES, 32, AUTH_REPLY_MORE_BYTES.length - 32);
        authReplyMoreFrame.decodeSegment1(byteBuf, true);

        AuthRequestMoreFrame authRequestMoreFrame = new AuthRequestMoreFrame();
        byteBuf = Unpooled.wrappedBuffer(AUTH_REQUEST_MORE_BYTES, 32, AUTH_REQUEST_MORE_BYTES.length - 32);
        authRequestMoreFrame.decodeSegment1(byteBuf, true);

        AuthDoneFrame authDoneFrame = new AuthDoneFrame();
        byteBuf = Unpooled.wrappedBuffer(AUTH_DONE_1_BYTES, 32, AUTH_DONE_1_BYTES.length - 32);
        authDoneFrame.decodeSegment1(byteBuf, true);

        SecretKeySpec authKey = new SecretKeySpec(KEY_BYTES, "AES");
        byte[] iv = PROOF_IV.getBytes();
        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, authKey, new IvParameterSpec(iv));
        byte[] decryptedBytes = cipher.doFinal(authDoneFrame.getSegment1().getPayload().getTicketInfos().get(0).getServiceTicket());

        ByteBuf decryptedByteBuf = Unpooled.wrappedBuffer(decryptedBytes);
        decryptedByteBuf.skipBytes(9);
        CephXServiceTicket serviceTicket = CephDecoder.decode(decryptedByteBuf, true, CephXServiceTicket.class);

        SecretKey sessionKey = new SecretKeySpec(serviceTicket.getSessionKey().getSecret(), "AES");

        cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, sessionKey, new IvParameterSpec(PROOF_IV.getBytes()));
        byte[] encryptedSecret = authDoneFrame.getSegment1().getPayload().getEncryptedSecret();
        byte[] decryptedSecret = cipher.doFinal(encryptedSecret, 4, encryptedSecret.length - 4);

        SecretKey streamKey = new SecretKeySpec(decryptedSecret, 13, 16, "AES");
        byte[] rxNonceBytes = new byte[12];
        System.arraycopy(decryptedSecret, 29, rxNonceBytes, 0, 12);
        byte[] txNonceBytes = new byte[12];
        System.arraycopy(decryptedSecret, 41, txNonceBytes, 0, 12);

        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, rxNonceBytes);
        cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, streamKey, gcmParameterSpec);
        byte[] sigMsgBytes = cipher.doFinal(AUTH_SIG_SERVER_ENC_BYTES);
        AuthSignatureFrame authSignatureFrame = new AuthSignatureFrame();
        byteBuf = Unpooled.wrappedBuffer(sigMsgBytes, 32, sigMsgBytes.length - 32);
        authSignatureFrame.decodeSegment1(byteBuf, true);
        HexFunctions.printHexString(authSignatureFrame.getSegment1().getSha256Digest());
        System.out.println("--------------------------------------------");

        gcmParameterSpec = new GCMParameterSpec(128, txNonceBytes);
        cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, streamKey, gcmParameterSpec);
        sigMsgBytes = cipher.doFinal(AUTH_SIG_CLIENT_ENC_BYTES);
        AuthSignatureFrame clientAuthSignatureFrame = new AuthSignatureFrame();
        byteBuf = Unpooled.wrappedBuffer(sigMsgBytes, 32, sigMsgBytes.length - 32);
        clientAuthSignatureFrame.decodeSegment1(byteBuf, true);

        byte[] allBytes;
        try (InputStream is = AESTest.class.getClassLoader().getResourceAsStream("all_messages_client.bin")) {
            allBytes = is.readAllBytes();
        }
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(sessionKey);
        HexFunctions.printHexString(mac.doFinal(allBytes));
    }
}
