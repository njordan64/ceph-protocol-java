package ca.venom.ceph.client;

import ca.venom.ceph.protocol.CephDecoder;
import ca.venom.ceph.protocol.frames.AuthSignatureFrame;
import ca.venom.ceph.protocol.frames.ClientIdentFrame;
import ca.venom.ceph.protocol.frames.CompressionDoneFrame;
import ca.venom.ceph.protocol.frames.CompressionRequestFrame;
import ca.venom.ceph.protocol.frames.MessageFrame;
import ca.venom.ceph.protocol.frames.ServerIdentFrame;
import ca.venom.ceph.protocol.types.auth.CephXTicketBlob;
import ca.venom.ceph.protocol.types.auth.CephXServiceTicket;
import ca.venom.ceph.utils.HexFunctions;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;

import javax.crypto.Cipher;
import javax.crypto.Mac;
import javax.crypto.spec.GCMParameterSpec;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.InputStream;
import java.util.Arrays;

public class AESTest {
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

    private static final byte[] SERVICE_TICKET_BYTES = new byte[] {
            (byte) 0xd4, (byte) 0x2d, (byte) 0x08, (byte) 0x02,
            (byte) 0x94, (byte) 0xbd, (byte) 0x28, (byte) 0x4b,
            (byte) 0x6f, (byte) 0x1f, (byte) 0x17, (byte) 0x7f,
            (byte) 0x41, (byte) 0xb0, (byte) 0xdb, (byte) 0x35,
            (byte) 0xca, (byte) 0x1f, (byte) 0x23, (byte) 0xda,
            (byte) 0x15, (byte) 0x30, (byte) 0x3c, (byte) 0x9d,
            (byte) 0xa2, (byte) 0xdf, (byte) 0xc1, (byte) 0xb8,
            (byte) 0x4a, (byte) 0x4e, (byte) 0xbe, (byte) 0xad,
            (byte) 0x26, (byte) 0x21, (byte) 0xb0, (byte) 0x1d,
            (byte) 0xa8, (byte) 0xfe, (byte) 0x1f, (byte) 0xef,
            (byte) 0x28, (byte) 0xd5, (byte) 0x07, (byte) 0x60,
            (byte) 0xd6, (byte) 0xe8, (byte) 0xb0, (byte) 0x87
    };

    private static final byte[] TICKET_BYTES = new byte[] {
            (byte) 0x01, (byte) 0x0a, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x00, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x60, (byte) 0x00, (byte) 0x00,
            (byte) 0x00, (byte) 0x0c, (byte) 0xe8, (byte) 0x61,
            (byte) 0x7c, (byte) 0xa6, (byte) 0xff, (byte) 0x21,
            (byte) 0xff, (byte) 0xd0, (byte) 0xeb, (byte) 0x78,
            (byte) 0x5b, (byte) 0x97, (byte) 0x91, (byte) 0x1c,
            (byte) 0x5c, (byte) 0x30, (byte) 0x51, (byte) 0x64,
            (byte) 0x55, (byte) 0xc1, (byte) 0x7c, (byte) 0xfa,
            (byte) 0xcf, (byte) 0x2c, (byte) 0x17, (byte) 0x5f,
            (byte) 0xa5, (byte) 0x01, (byte) 0x47, (byte) 0x9d,
            (byte) 0x12, (byte) 0x51, (byte) 0x8a, (byte) 0xf4,
            (byte) 0x2f, (byte) 0x11, (byte) 0x30, (byte) 0x90,
            (byte) 0x54, (byte) 0xbe, (byte) 0xff, (byte) 0xc3,
            (byte) 0x3a, (byte) 0xb9, (byte) 0xcb, (byte) 0xb3,
            (byte) 0xef, (byte) 0x06, (byte) 0xc5, (byte) 0xc7,
            (byte) 0x93, (byte) 0x24, (byte) 0x09, (byte) 0x0f,
            (byte) 0x1a, (byte) 0x51, (byte) 0x34, (byte) 0x55,
            (byte) 0xc1, (byte) 0x29, (byte) 0xd2, (byte) 0x0b,
            (byte) 0x8a, (byte) 0xac, (byte) 0x2f, (byte) 0x6c,
            (byte) 0x67, (byte) 0x46, (byte) 0xc3, (byte) 0x46,
            (byte) 0x33, (byte) 0x1d, (byte) 0x94, (byte) 0x47,
            (byte) 0xd4, (byte) 0xc0, (byte) 0x20, (byte) 0xb1,
            (byte) 0xb4, (byte) 0xb5, (byte) 0x05, (byte) 0x78,
            (byte) 0xab, (byte) 0x81, (byte) 0xd8, (byte) 0xfa,
            (byte) 0x4f, (byte) 0xee, (byte) 0x7a, (byte) 0xa9,
            (byte) 0x5f, (byte) 0x21, (byte) 0xb4, (byte) 0xbe,
            (byte) 0x9b
    };

    private static final byte[] CONNECTION_SECRET_BYTES = new byte[] {
            (byte) 0x6c, (byte) 0xc2, (byte) 0x00, (byte) 0xc2,
            (byte) 0xb5, (byte) 0x1b, (byte) 0x3d, (byte) 0x2c,
            (byte) 0x3e, (byte) 0x79, (byte) 0x91, (byte) 0x5d,
            (byte) 0xc7, (byte) 0x5c, (byte) 0x82, (byte) 0x49,
            (byte) 0x58, (byte) 0xd9, (byte) 0x0b, (byte) 0x05,
            (byte) 0x20, (byte) 0xec, (byte) 0x75, (byte) 0x90,
            (byte) 0x88, (byte) 0xcf, (byte) 0x61, (byte) 0x3d,
            (byte) 0x70, (byte) 0x47, (byte) 0x3d, (byte) 0xa6,
            (byte) 0xc9, (byte) 0xe1, (byte) 0x02, (byte) 0x03,
            (byte) 0x1c, (byte) 0x0f, (byte) 0x8b, (byte) 0x41,
            (byte) 0x5f, (byte) 0xe8, (byte) 0xbb, (byte) 0x94,
            (byte) 0x64, (byte) 0x35, (byte) 0xf8, (byte) 0x52,
            (byte) 0x7c, (byte) 0x3b, (byte) 0x7c, (byte) 0xc9,
            (byte) 0x75, (byte) 0x30, (byte) 0x5f, (byte) 0x3c,
            (byte) 0x0f, (byte) 0xd0, (byte) 0x3a, (byte) 0x77,
            (byte) 0x8e, (byte) 0x8c, (byte) 0x44, (byte) 0x8a,
            (byte) 0x74, (byte) 0x09, (byte) 0xfe, (byte) 0x94,
            (byte) 0x57, (byte) 0x0a, (byte) 0xa4, (byte) 0x93,
            (byte) 0xd1, (byte) 0xdb, (byte) 0xa4, (byte) 0xf0,
            (byte) 0x6a, (byte) 0xbf, (byte) 0x63, (byte) 0x46
    };

    private static final byte[] ENCRYPTED_SECRET_BYTES = new byte[] {
            (byte) 0x6c, (byte) 0xc2, (byte) 0x00, (byte) 0xc2,
            (byte) 0xb5, (byte) 0x1b, (byte) 0x3d, (byte) 0x2c,
            (byte) 0x3e, (byte) 0x79, (byte) 0x91, (byte) 0x5d,
            (byte) 0xc7, (byte) 0x5c, (byte) 0x82, (byte) 0x49,
            (byte) 0x58, (byte) 0xd9, (byte) 0x0b, (byte) 0x05,
            (byte) 0x20, (byte) 0xec, (byte) 0x75, (byte) 0x90,
            (byte) 0x88, (byte) 0xcf, (byte) 0x61, (byte) 0x3d,
            (byte) 0x70, (byte) 0x47, (byte) 0x3d, (byte) 0xa6,
            (byte) 0xc9, (byte) 0xe1, (byte) 0x02, (byte) 0x03,
            (byte) 0x1c, (byte) 0x0f, (byte) 0x8b, (byte) 0x41,
            (byte) 0x5f, (byte) 0xe8, (byte) 0xbb, (byte) 0x94,
            (byte) 0x64, (byte) 0x35, (byte) 0xf8, (byte) 0x52,
            (byte) 0x7c, (byte) 0x3b, (byte) 0x7c, (byte) 0xc9,
            (byte) 0x75, (byte) 0x30, (byte) 0x5f, (byte) 0x3c,
            (byte) 0x0f, (byte) 0xd0, (byte) 0x3a, (byte) 0x77,
            (byte) 0x8e, (byte) 0x8c, (byte) 0x44, (byte) 0x8a,
            (byte) 0x74, (byte) 0x09, (byte) 0xfe, (byte) 0x94,
            (byte) 0x57, (byte) 0x0a, (byte) 0xa4, (byte) 0x93,
            (byte) 0xd1, (byte) 0xdb, (byte) 0xa4, (byte) 0xf0,
            (byte) 0x6a, (byte) 0xbf, (byte) 0x63, (byte) 0x46
    };

    private static final byte[] ENCRYPTED_MESSAGE = new byte[] {
            (byte) 0x81, (byte) 0xbe, (byte) 0xe1, (byte) 0x6e,
            (byte) 0xd3, (byte) 0x89, (byte) 0x98, (byte) 0x74,
            (byte) 0x29, (byte) 0x8f, (byte) 0xd5, (byte) 0x94,
            (byte) 0xa2, (byte) 0x47, (byte) 0x87, (byte) 0xb8,
            (byte) 0xc8, (byte) 0xcc, (byte) 0x1d, (byte) 0x3f,
            (byte) 0x60, (byte) 0xef, (byte) 0xef, (byte) 0xaf,
            (byte) 0x24, (byte) 0xdd, (byte) 0x96, (byte) 0xbd,
            (byte) 0x0a, (byte) 0x65, (byte) 0xc8, (byte) 0xe7,
            (byte) 0xbe, (byte) 0x13, (byte) 0xaa, (byte) 0x19,
            (byte) 0x0b, (byte) 0x30, (byte) 0xf2, (byte) 0xfb,
            (byte) 0x90, (byte) 0x2c, (byte) 0x40, (byte) 0x6e,
            (byte) 0x68, (byte) 0xc5, (byte) 0x9e, (byte) 0xdb,
            (byte) 0x89, (byte) 0x87, (byte) 0xec, (byte) 0x88,
            (byte) 0xcc, (byte) 0x9e, (byte) 0xbc, (byte) 0x8a,
            (byte) 0x38, (byte) 0xd6, (byte) 0x06, (byte) 0x2f,
            (byte) 0xe4, (byte) 0x27, (byte) 0xc5, (byte) 0x34,
            (byte) 0xe4, (byte) 0xe5, (byte) 0x8c, (byte) 0x8e,
            (byte) 0x08, (byte) 0x44, (byte) 0x0f, (byte) 0x99,
            (byte) 0x1c, (byte) 0x69, (byte) 0xd0, (byte) 0x9d,
            (byte) 0xcc, (byte) 0x02, (byte) 0x4f, (byte) 0x6c,
            (byte) 0xde, (byte) 0xf2, (byte) 0xef, (byte) 0xf5,
            (byte) 0x95, (byte) 0x6e, (byte) 0x19, (byte) 0x80,
            (byte) 0xb8, (byte) 0x57, (byte) 0x57, (byte) 0x4f,
            (byte) 0x42, (byte) 0x26, (byte) 0xe1, (byte) 0x4f
    };

    private static final byte[] ENCRYPTED_MESSAGE2 = new byte[] {
            (byte) 0x6b, (byte) 0x06, (byte) 0x65, (byte) 0xe2,
            (byte) 0xfd, (byte) 0x7e, (byte) 0xfd, (byte) 0x3c,
            (byte) 0x33, (byte) 0x57, (byte) 0x2e, (byte) 0xf9,
            (byte) 0xa7, (byte) 0xe1, (byte) 0x77, (byte) 0xea,
            (byte) 0x3c, (byte) 0x6b, (byte) 0x1a, (byte) 0x73,
            (byte) 0x54, (byte) 0x72, (byte) 0x0c, (byte) 0xbb,
            (byte) 0x1a, (byte) 0xc8, (byte) 0x8f, (byte) 0x57,
            (byte) 0xe9, (byte) 0xea, (byte) 0x88, (byte) 0x0e,
            (byte) 0xab, (byte) 0xee, (byte) 0x43, (byte) 0xef,
            (byte) 0x9f, (byte) 0x24, (byte) 0x21, (byte) 0xaf,
            (byte) 0x50, (byte) 0x54, (byte) 0x60, (byte) 0x00,
            (byte) 0x90, (byte) 0x91, (byte) 0xa3, (byte) 0xa9,
            (byte) 0x7f, (byte) 0xc2, (byte) 0x21, (byte) 0xe7,
            (byte) 0xe4, (byte) 0xe5, (byte) 0x90, (byte) 0x41,
            (byte) 0x1a, (byte) 0x55, (byte) 0x1a, (byte) 0x2b,
            (byte) 0xbc, (byte) 0x12, (byte) 0x57, (byte) 0xb1,
            (byte) 0xf8, (byte) 0xe5, (byte) 0x82, (byte) 0x82,
            (byte) 0x51, (byte) 0x78, (byte) 0xc2, (byte) 0x8a,
            (byte) 0x2b, (byte) 0xb6, (byte) 0x59, (byte) 0xd2,
            (byte) 0x7d, (byte) 0x64, (byte) 0xc9, (byte) 0xfe,
            (byte) 0x4a, (byte) 0xd2, (byte) 0x8c, (byte) 0xc7,
            (byte) 0x19, (byte) 0xf9, (byte) 0x86, (byte) 0x4f,
            (byte) 0xac, (byte) 0xc4, (byte) 0x40, (byte) 0x66,
            (byte) 0x2c, (byte) 0x98, (byte) 0xce, (byte) 0xbd,
            (byte) 0x74, (byte) 0x01, (byte) 0x08, (byte) 0x26,
            (byte) 0x2d, (byte) 0x4b, (byte) 0x8b, (byte) 0xce,
            (byte) 0xe7, (byte) 0xd6, (byte) 0x8a, (byte) 0x31,
            (byte) 0xac, (byte) 0x8b, (byte) 0xc2, (byte) 0xae,
            (byte) 0x8c, (byte) 0x02, (byte) 0x74, (byte) 0x6f,
            (byte) 0x32, (byte) 0xaa, (byte) 0xc5, (byte) 0xd7,
            (byte) 0x51, (byte) 0x9d, (byte) 0xf6, (byte) 0xc1,
            (byte) 0x27, (byte) 0x89, (byte) 0x20, (byte) 0x36,
            (byte) 0x56, (byte) 0x81, (byte) 0x06, (byte) 0x4b,
            (byte) 0x01, (byte) 0x4f, (byte) 0xd6, (byte) 0xe4,
            (byte) 0xc0, (byte) 0x12, (byte) 0x32, (byte) 0x9e,
            (byte) 0x96, (byte) 0x01, (byte) 0x2f, (byte) 0xb7,
            (byte) 0x0c, (byte) 0x41, (byte) 0x84, (byte) 0xa1,
            (byte) 0x64, (byte) 0xca, (byte) 0x4d, (byte) 0x74,
            (byte) 0xc9, (byte) 0xa5, (byte) 0xb6, (byte) 0x1c,
            (byte) 0x00, (byte) 0x9d, (byte) 0x2e, (byte) 0x90,
            (byte) 0x2b, (byte) 0xe9, (byte) 0xe0, (byte) 0xea,
            (byte) 0x44, (byte) 0x6b, (byte) 0x6d, (byte) 0x60,
            (byte) 0x7a, (byte) 0x30, (byte) 0x03, (byte) 0x94,
            (byte) 0x8b, (byte) 0x23, (byte) 0x3e, (byte) 0x0c,
            (byte) 0x1e, (byte) 0x93, (byte) 0x91, (byte) 0xdb,
            (byte) 0x3e, (byte) 0x40, (byte) 0x17, (byte) 0x3d,
            (byte) 0x9a, (byte) 0x93, (byte) 0x86, (byte) 0x0e,
            (byte) 0xfe, (byte) 0x9c, (byte) 0x7e, (byte) 0x78
    };

    private static final byte[] ENCRYPTED_MESSAGE3 = new byte[] {
            (byte) 0xd0, (byte) 0xb4, (byte) 0x54, (byte) 0x8b,
            (byte) 0x57, (byte) 0x44, (byte) 0x25, (byte) 0xf0,
            (byte) 0x06, (byte) 0xdb, (byte) 0x87, (byte) 0xa2,
            (byte) 0x3a, (byte) 0x0b, (byte) 0x55, (byte) 0xef,
            (byte) 0x4c, (byte) 0xe2, (byte) 0xab, (byte) 0xa5,
            (byte) 0x9c, (byte) 0x9c, (byte) 0xf7, (byte) 0xbd,
            (byte) 0x0d, (byte) 0xfb, (byte) 0xb0, (byte) 0x7f,
            (byte) 0x89, (byte) 0xee, (byte) 0xd1, (byte) 0xf4,
            (byte) 0x8c, (byte) 0x75, (byte) 0x80, (byte) 0x45,
            (byte) 0xad, (byte) 0xfd, (byte) 0xbe, (byte) 0x52,
            (byte) 0xca, (byte) 0xcb, (byte) 0xf0, (byte) 0xb4,
            (byte) 0x89, (byte) 0x5e, (byte) 0x52, (byte) 0x7e,
            (byte) 0x9f, (byte) 0x76, (byte) 0x94, (byte) 0x7c,
            (byte) 0xa5, (byte) 0x31, (byte) 0x03, (byte) 0x8e,
            (byte) 0xbc, (byte) 0xed, (byte) 0xbe, (byte) 0x59,
            (byte) 0xee, (byte) 0x18, (byte) 0x4a, (byte) 0x78,
            (byte) 0x45, (byte) 0xcb, (byte) 0x8f, (byte) 0x23,
            (byte) 0x2e, (byte) 0xb3, (byte) 0x10, (byte) 0xf3,
            (byte) 0x9f, (byte) 0xab, (byte) 0x17, (byte) 0xfb,
            (byte) 0x91, (byte) 0x5a, (byte) 0xae, (byte) 0x16,
            (byte) 0xee, (byte) 0xed, (byte) 0xfa, (byte) 0x89,
            (byte) 0x27, (byte) 0x9d, (byte) 0xc5, (byte) 0xe7,
            (byte) 0x04, (byte) 0x03, (byte) 0x84, (byte) 0x65,
            (byte) 0x0f, (byte) 0xd2, (byte) 0xe7, (byte) 0xd3
    };

    private static final byte[] ENCRYPTED_MESSAGE4 = new byte[] {
            (byte) 0x3b, (byte) 0x75, (byte) 0x33, (byte) 0xf6,
            (byte) 0x2d, (byte) 0xff, (byte) 0x59, (byte) 0x64,
            (byte) 0xa8, (byte) 0xef, (byte) 0x95, (byte) 0x2c,
            (byte) 0xe1, (byte) 0xd1, (byte) 0x28, (byte) 0xa1,
            (byte) 0xb1, (byte) 0xf8, (byte) 0x40, (byte) 0x07,
            (byte) 0x17, (byte) 0xc9, (byte) 0xa4, (byte) 0x77,
            (byte) 0x2b, (byte) 0x3e, (byte) 0xaa, (byte) 0x68,
            (byte) 0xeb, (byte) 0x79, (byte) 0x65, (byte) 0x02,
            (byte) 0x15, (byte) 0x66, (byte) 0x04, (byte) 0x4b,
            (byte) 0xef, (byte) 0x01, (byte) 0x68, (byte) 0xd8,
            (byte) 0x44, (byte) 0x2c, (byte) 0x7d, (byte) 0xde,
            (byte) 0x75, (byte) 0x61, (byte) 0xe7, (byte) 0x50,
            (byte) 0xd4, (byte) 0xa8, (byte) 0x46, (byte) 0x8d,
            (byte) 0x4b, (byte) 0x67, (byte) 0x67, (byte) 0xf9,
            (byte) 0x4b, (byte) 0x90, (byte) 0x91, (byte) 0x20,
            (byte) 0xc3, (byte) 0x2a, (byte) 0x43, (byte) 0xd5,
            (byte) 0x99, (byte) 0x35, (byte) 0xc2, (byte) 0x71,
            (byte) 0x8c, (byte) 0xea, (byte) 0x99, (byte) 0xa6,
            (byte) 0x0c, (byte) 0x02, (byte) 0x7b, (byte) 0x64,
            (byte) 0xcc, (byte) 0xa1, (byte) 0xb5, (byte) 0xb5,
            (byte) 0xfe, (byte) 0x7b, (byte) 0xcb, (byte) 0xb4,
            (byte) 0x47, (byte) 0xd5, (byte) 0x3d, (byte) 0xda,
            (byte) 0x45, (byte) 0x60, (byte) 0x3e, (byte) 0x47,
            (byte) 0x26, (byte) 0x19, (byte) 0x69, (byte) 0x11,
            (byte) 0x62, (byte) 0x86, (byte) 0x31, (byte) 0xe3,
            (byte) 0xa1, (byte) 0xba, (byte) 0x68, (byte) 0xd2,
            (byte) 0xe6, (byte) 0x71, (byte) 0xa5, (byte) 0xe3,
            (byte) 0x43, (byte) 0x2c, (byte) 0x58, (byte) 0xf9,
            (byte) 0x63, (byte) 0x3c, (byte) 0xa0, (byte) 0xab,
            (byte) 0xab, (byte) 0x46, (byte) 0x12, (byte) 0xda,
            (byte) 0x57, (byte) 0x19, (byte) 0xdf, (byte) 0xb7,
            (byte) 0x94, (byte) 0xc7, (byte) 0x96, (byte) 0xfa,
            (byte) 0x13, (byte) 0x3f, (byte) 0x4c, (byte) 0x2c,
            (byte) 0x24, (byte) 0xd1, (byte) 0x95, (byte) 0x42,
            (byte) 0x91, (byte) 0xda, (byte) 0x2f, (byte) 0x11,
            (byte) 0xc9, (byte) 0xa0, (byte) 0x4f, (byte) 0x02,
            (byte) 0xbc, (byte) 0x37, (byte) 0x3e, (byte) 0x47,
            (byte) 0xa2, (byte) 0xe8, (byte) 0xd0, (byte) 0x2c,
            (byte) 0x65, (byte) 0x13, (byte) 0xc2, (byte) 0x25,
            (byte) 0x60, (byte) 0xb7, (byte) 0x7d, (byte) 0xe4,
            (byte) 0x8f, (byte) 0xa4, (byte) 0xf6, (byte) 0x96,
            (byte) 0xbf, (byte) 0x88, (byte) 0x84, (byte) 0xee,
            (byte) 0x97, (byte) 0x35, (byte) 0xab, (byte) 0x7e,
            (byte) 0x37, (byte) 0xfb, (byte) 0xe0, (byte) 0x9f,
            (byte) 0x17, (byte) 0x70, (byte) 0x8c, (byte) 0x06,
            (byte) 0x8f, (byte) 0xf1, (byte) 0xa6, (byte) 0x32,
            (byte) 0x7c, (byte) 0x3c, (byte) 0xc3, (byte) 0x7e,
            (byte) 0x7a, (byte) 0x90, (byte) 0xca, (byte) 0x27,
    };

    private static final byte[] ENCRYPTED_MESSAGE5 = new byte[] {
            (byte) 0x56, (byte) 0x9a, (byte) 0x98, (byte) 0xf2,
            (byte) 0x58, (byte) 0xdc, (byte) 0x29, (byte) 0xec,
            (byte) 0xb7, (byte) 0x5b, (byte) 0x6a, (byte) 0x49,
            (byte) 0xb0, (byte) 0x42, (byte) 0x21, (byte) 0x1a,
            (byte) 0x14, (byte) 0x67, (byte) 0x6f, (byte) 0x59,
            (byte) 0x4c, (byte) 0x75, (byte) 0x7c, (byte) 0x18,
            (byte) 0x77, (byte) 0x49, (byte) 0xab, (byte) 0x6e,
            (byte) 0x94, (byte) 0x34, (byte) 0x48, (byte) 0xad,
            (byte) 0x3b, (byte) 0x49, (byte) 0xb9, (byte) 0x67,
            (byte) 0x59, (byte) 0xdf, (byte) 0x74, (byte) 0x37,
            (byte) 0x73, (byte) 0xcb, (byte) 0x0d, (byte) 0x5b,
            (byte) 0x06, (byte) 0x5e, (byte) 0x90, (byte) 0x45,
            (byte) 0x6d, (byte) 0xa9, (byte) 0xcc, (byte) 0x40,
            (byte) 0x56, (byte) 0x3a, (byte) 0x36, (byte) 0x8b,
            (byte) 0x8e, (byte) 0x40, (byte) 0x83, (byte) 0x28,
            (byte) 0xc0, (byte) 0xe2, (byte) 0x73, (byte) 0x50,
            (byte) 0x09, (byte) 0xf7, (byte) 0x77, (byte) 0xa6,
            (byte) 0x50, (byte) 0xeb, (byte) 0xca, (byte) 0x0a,
            (byte) 0xe0, (byte) 0x96, (byte) 0x7c, (byte) 0xeb,
            (byte) 0x48, (byte) 0x9d, (byte) 0xcd, (byte) 0x18,
            (byte) 0xb6, (byte) 0xce, (byte) 0xcb, (byte) 0x2f,
            (byte) 0xe6, (byte) 0x6f, (byte) 0x00, (byte) 0x5e,
            (byte) 0x02, (byte) 0xbf, (byte) 0x17, (byte) 0xa8,
            (byte) 0x41, (byte) 0x2f, (byte) 0x35, (byte) 0xec,
            (byte) 0xe7, (byte) 0xba, (byte) 0x83, (byte) 0x2d,
            (byte) 0xfc, (byte) 0x5e, (byte) 0x1c, (byte) 0x55,
            (byte) 0xfd, (byte) 0x8e, (byte) 0xcf, (byte) 0x96,
            (byte) 0x29, (byte) 0x55, (byte) 0xdc, (byte) 0x4f,
            (byte) 0x02, (byte) 0x53, (byte) 0x96, (byte) 0x95,
            (byte) 0x75, (byte) 0x62, (byte) 0xdd, (byte) 0xb7,
            (byte) 0x2d, (byte) 0x84, (byte) 0xae, (byte) 0x3e,
            (byte) 0xe4, (byte) 0xad, (byte) 0x84, (byte) 0xc3,
            (byte) 0x63, (byte) 0x01, (byte) 0x1f, (byte) 0xe4,
            (byte) 0x5b, (byte) 0xfe, (byte) 0x0a, (byte) 0x41,
            (byte) 0x73, (byte) 0xfb, (byte) 0x04, (byte) 0xaa,
            (byte) 0x99, (byte) 0xb4, (byte) 0xef, (byte) 0x04,
            (byte) 0x7f, (byte) 0x20, (byte) 0x62, (byte) 0xf9,
            (byte) 0xe9, (byte) 0xaa, (byte) 0x31, (byte) 0x6e,
            (byte) 0x20, (byte) 0xc8, (byte) 0x9d, (byte) 0x30,
            (byte) 0xb1, (byte) 0x89, (byte) 0x8c, (byte) 0x8e,
            (byte) 0x19, (byte) 0xa9, (byte) 0x75, (byte) 0x12,
            (byte) 0xa4, (byte) 0x8a, (byte) 0x83, (byte) 0xce,
            (byte) 0xad, (byte) 0x0a, (byte) 0x36, (byte) 0x77,
            (byte) 0xbf, (byte) 0x80, (byte) 0xe4, (byte) 0xf5,
            (byte) 0x7c, (byte) 0x8a, (byte) 0xef, (byte) 0x5f,
            (byte) 0x3f, (byte) 0x12, (byte) 0x1b, (byte) 0xee,
            (byte) 0xa8, (byte) 0x14, (byte) 0xc5, (byte) 0xa5,
            (byte) 0x68, (byte) 0xcc, (byte) 0xe3, (byte) 0x9c,
    };

    private static final byte[] ENCRYPTED_MESSAGE6 = new byte[] {
            (byte) 0xe2, (byte) 0xbb, (byte) 0x51, (byte) 0x49,
            (byte) 0x3f, (byte) 0x12, (byte) 0x63, (byte) 0xe2,
            (byte) 0x41, (byte) 0x78, (byte) 0x58, (byte) 0xc5,
            (byte) 0x18, (byte) 0xb6, (byte) 0x7d, (byte) 0x84,
            (byte) 0x29, (byte) 0xc3, (byte) 0xb7, (byte) 0x70,
            (byte) 0x58, (byte) 0x1a, (byte) 0xdd, (byte) 0x70,
            (byte) 0x9e, (byte) 0xd9, (byte) 0xa9, (byte) 0x2c,
            (byte) 0x3b, (byte) 0xaa, (byte) 0xe2, (byte) 0x33,
            (byte) 0x50, (byte) 0xae, (byte) 0x48, (byte) 0xab,
            (byte) 0x4f, (byte) 0x7a, (byte) 0x03, (byte) 0x02,
            (byte) 0x8c, (byte) 0x4b, (byte) 0xc6, (byte) 0xa7,
            (byte) 0xe7, (byte) 0x55, (byte) 0xbf, (byte) 0xcf,
            (byte) 0xca, (byte) 0x43, (byte) 0x4a, (byte) 0xf2,
            (byte) 0xb1, (byte) 0x06, (byte) 0x78, (byte) 0x6c,
            (byte) 0x3a, (byte) 0xc6, (byte) 0xa0, (byte) 0x6c,
            (byte) 0x9c, (byte) 0x8a, (byte) 0x89, (byte) 0x49,
            (byte) 0x96, (byte) 0xf5, (byte) 0xcc, (byte) 0xaa,
            (byte) 0x88, (byte) 0x82, (byte) 0xf3, (byte) 0x67,
            (byte) 0xb2, (byte) 0xd7, (byte) 0x19, (byte) 0x13,
            (byte) 0xd8, (byte) 0x33, (byte) 0x3d, (byte) 0x36,
            (byte) 0x6e, (byte) 0xc6, (byte) 0x6a, (byte) 0x4c,
            (byte) 0x77, (byte) 0xef, (byte) 0x82, (byte) 0x29,
            (byte) 0xb7, (byte) 0x20, (byte) 0x0a, (byte) 0x17,
            (byte) 0xb4, (byte) 0x0d, (byte) 0xc5, (byte) 0xbc,
            (byte) 0x8e, (byte) 0xa7, (byte) 0x2b, (byte) 0x1e,
            (byte) 0x5b, (byte) 0xb2, (byte) 0x86, (byte) 0x8f,
            (byte) 0xba, (byte) 0x43, (byte) 0x4f, (byte) 0xc9,
            (byte) 0x1e, (byte) 0x79, (byte) 0xa3, (byte) 0x19,
            (byte) 0xcf, (byte) 0xeb, (byte) 0xcf, (byte) 0xd2,
            (byte) 0x97, (byte) 0x14, (byte) 0x62, (byte) 0x93,
            (byte) 0x05, (byte) 0x22, (byte) 0xa0, (byte) 0x6b,
            (byte) 0x3e, (byte) 0xd6, (byte) 0x02, (byte) 0x61,
            (byte) 0x91, (byte) 0xc2, (byte) 0xdf, (byte) 0x14,
            (byte) 0x73, (byte) 0x94, (byte) 0x37, (byte) 0xaa,
            (byte) 0x74, (byte) 0x83, (byte) 0xda, (byte) 0xf7,
            (byte) 0x1f, (byte) 0xff, (byte) 0xa8, (byte) 0x39,
            (byte) 0x87, (byte) 0xbb, (byte) 0x5b, (byte) 0x31,
            (byte) 0xf0, (byte) 0xae, (byte) 0x99, (byte) 0x80,
            (byte) 0xe1, (byte) 0x8e, (byte) 0x60, (byte) 0xd7,
            (byte) 0x32, (byte) 0x3a, (byte) 0xb4, (byte) 0x7b,
            (byte) 0xef, (byte) 0xd9, (byte) 0x11, (byte) 0xe3,
            (byte) 0x26, (byte) 0xa9, (byte) 0x19, (byte) 0xb9,
            (byte) 0x31, (byte) 0x41, (byte) 0x58, (byte) 0x0b,
            (byte) 0x7e, (byte) 0x4c, (byte) 0x4f, (byte) 0xc0,
            (byte) 0x00, (byte) 0x2b, (byte) 0x08, (byte) 0x18,
            (byte) 0xc4, (byte) 0x9d, (byte) 0x19, (byte) 0xc2,
            (byte) 0x24, (byte) 0xbe, (byte) 0x8e, (byte) 0x96,
            (byte) 0xe3, (byte) 0xca, (byte) 0x20, (byte) 0x4e,
            (byte) 0x82, (byte) 0xac, (byte) 0x06, (byte) 0x6c,
            (byte) 0x7e, (byte) 0x05, (byte) 0x4e, (byte) 0x16,
            (byte) 0xa6, (byte) 0xf1, (byte) 0xb5, (byte) 0xaf,
            (byte) 0x5a, (byte) 0x88, (byte) 0x98, (byte) 0x67,
            (byte) 0xe9, (byte) 0x14, (byte) 0x75, (byte) 0xc1,
            (byte) 0x2f, (byte) 0x7c, (byte) 0xe8, (byte) 0x29,
            (byte) 0xb3, (byte) 0x70, (byte) 0xc7, (byte) 0xbd,
            (byte) 0x19, (byte) 0xff, (byte) 0x4f, (byte) 0x06,
            (byte) 0x10, (byte) 0x57, (byte) 0xff, (byte) 0x60,
            (byte) 0x50, (byte) 0x97, (byte) 0xe6, (byte) 0xf4,
            (byte) 0xdd, (byte) 0x0f, (byte) 0xa2, (byte) 0x4b,
            (byte) 0x4b, (byte) 0x4a, (byte) 0x08, (byte) 0x33,
            (byte) 0x2f, (byte) 0xdd, (byte) 0x74, (byte) 0x96,
            (byte) 0xd3, (byte) 0x7f, (byte) 0x0b, (byte) 0x4d,
            (byte) 0xce, (byte) 0x07, (byte) 0xd3, (byte) 0x2a,
            (byte) 0xe5, (byte) 0xb2, (byte) 0x04, (byte) 0xca,
            (byte) 0x15, (byte) 0x22, (byte) 0xf2, (byte) 0xc4,
            (byte) 0xb4, (byte) 0x2c, (byte) 0x44, (byte) 0xad,
            (byte) 0x01, (byte) 0x11, (byte) 0x21, (byte) 0x67,
            (byte) 0xf8, (byte) 0x92, (byte) 0x98, (byte) 0xd3,
            (byte) 0xf5, (byte) 0x8c, (byte) 0xa2, (byte) 0x18,
            (byte) 0x98, (byte) 0x9f, (byte) 0x38, (byte) 0xe6,
            (byte) 0x86, (byte) 0x1b, (byte) 0x39, (byte) 0x16,
            (byte) 0xc3, (byte) 0x10, (byte) 0x6f, (byte) 0x6b,
    };

    private static byte[] ENCRYPTED_MESSAGE7 = new byte[] {
            (byte) 0x00, (byte) 0x77, (byte) 0xa9, (byte) 0x96,
            (byte) 0x65, (byte) 0x49, (byte) 0x91, (byte) 0x8c,
            (byte) 0x2e, (byte) 0x0e, (byte) 0xdb, (byte) 0x43,
            (byte) 0x37, (byte) 0x63, (byte) 0x0b, (byte) 0x29,
            (byte) 0xea, (byte) 0xf8, (byte) 0xe2, (byte) 0x42,
            (byte) 0xcb, (byte) 0x0d, (byte) 0x6f, (byte) 0xb5,
            (byte) 0x9e, (byte) 0x42, (byte) 0x38, (byte) 0xd8,
            (byte) 0xff, (byte) 0xd2, (byte) 0x1b, (byte) 0x85,
            (byte) 0xaf, (byte) 0xd7, (byte) 0xc4, (byte) 0x97,
            (byte) 0x43, (byte) 0xb1, (byte) 0x26, (byte) 0xe7,
            (byte) 0xd8, (byte) 0x5c, (byte) 0x23, (byte) 0x1e,
            (byte) 0xfd, (byte) 0xda, (byte) 0xe1, (byte) 0xe9,
            (byte) 0xda, (byte) 0x2d, (byte) 0x0b, (byte) 0x22,
            (byte) 0x17, (byte) 0x64, (byte) 0x97, (byte) 0xfa,
            (byte) 0x4c, (byte) 0xdd, (byte) 0x76, (byte) 0x10,
            (byte) 0x6f, (byte) 0x56, (byte) 0x5e, (byte) 0xa3,
            (byte) 0xdb, (byte) 0xf7, (byte) 0xeb, (byte) 0x1c,
            (byte) 0x06, (byte) 0x0e, (byte) 0x22, (byte) 0xc9,
            (byte) 0xc7, (byte) 0xd6, (byte) 0x8f, (byte) 0x72,
            (byte) 0x77, (byte) 0x1f, (byte) 0x65, (byte) 0x75,
            (byte) 0xe6, (byte) 0x3d, (byte) 0xfa, (byte) 0xe5,
            (byte) 0x58, (byte) 0xad, (byte) 0x72, (byte) 0xd2,
            (byte) 0x5d, (byte) 0x76, (byte) 0x44, (byte) 0xdc,
            (byte) 0xca, (byte) 0x5c, (byte) 0xd6, (byte) 0x01,
            (byte) 0xed, (byte) 0x56, (byte) 0x4a, (byte) 0xd5,
            (byte) 0xd7, (byte) 0x89, (byte) 0x4f, (byte) 0x1f,
            (byte) 0x44, (byte) 0xfb, (byte) 0x4a, (byte) 0x9e,
            (byte) 0xab, (byte) 0x58, (byte) 0x93, (byte) 0x78,
            (byte) 0x55, (byte) 0x68, (byte) 0x48, (byte) 0xb5,
            (byte) 0xde, (byte) 0x3a, (byte) 0xbd, (byte) 0xcf,
            (byte) 0xcb, (byte) 0x95, (byte) 0x60, (byte) 0x9e,
            (byte) 0x5a, (byte) 0xc8, (byte) 0xd6, (byte) 0x7f,
            (byte) 0x7a, (byte) 0x73, (byte) 0x8b, (byte) 0x61,
            (byte) 0xc2, (byte) 0x67, (byte) 0x17, (byte) 0x5f,
            (byte) 0xcd, (byte) 0xe3, (byte) 0x9a, (byte) 0xb4,
            (byte) 0x01, (byte) 0xf7, (byte) 0x3c, (byte) 0x6c,
            (byte) 0x96, (byte) 0xe1, (byte) 0x7c, (byte) 0x04,
            (byte) 0x83, (byte) 0xc4, (byte) 0x17, (byte) 0xaa,
            (byte) 0xa1, (byte) 0x8a, (byte) 0x74, (byte) 0x76,
            (byte) 0x7d, (byte) 0xf4, (byte) 0x0f, (byte) 0xac,
            (byte) 0x95, (byte) 0x98, (byte) 0x60, (byte) 0xad,
            (byte) 0xa2, (byte) 0xfc, (byte) 0x46, (byte) 0xec,
            (byte) 0x65, (byte) 0xa1, (byte) 0xf7, (byte) 0x1c,
            (byte) 0x03, (byte) 0x00, (byte) 0x80, (byte) 0x72,
            (byte) 0x73, (byte) 0xae, (byte) 0xa0, (byte) 0x47,
            (byte) 0x14, (byte) 0x70, (byte) 0x44, (byte) 0x9a,
            (byte) 0x4e, (byte) 0xac, (byte) 0xd7, (byte) 0xa5,
            (byte) 0x3d, (byte) 0x14, (byte) 0x85, (byte) 0x53,
            (byte) 0x41, (byte) 0x27, (byte) 0xf8, (byte) 0x19,
            (byte) 0x64, (byte) 0xa4, (byte) 0x71, (byte) 0xf1,
            (byte) 0x8f, (byte) 0xc5, (byte) 0xd4, (byte) 0x3c,
            (byte) 0xd9, (byte) 0xd8, (byte) 0x16, (byte) 0xa8,
            (byte) 0x09, (byte) 0xb2, (byte) 0x4b, (byte) 0x80,
            (byte) 0x6d, (byte) 0xe1, (byte) 0x14, (byte) 0x29,
            (byte) 0x05, (byte) 0xcc, (byte) 0x23, (byte) 0x70,
            (byte) 0x2a, (byte) 0x0e, (byte) 0x11, (byte) 0xdf,
            (byte) 0x69, (byte) 0x15, (byte) 0x15, (byte) 0x1c,
            (byte) 0xdb, (byte) 0xcc, (byte) 0xcd, (byte) 0x2b,
            (byte) 0x0e, (byte) 0xa1, (byte) 0x6b, (byte) 0x79,
            (byte) 0x49, (byte) 0xaa, (byte) 0xd2, (byte) 0x1d,
            (byte) 0x47, (byte) 0xd1, (byte) 0x9e, (byte) 0x09,
            (byte) 0x6a, (byte) 0xbf, (byte) 0x5e, (byte) 0x61,
            (byte) 0xa2, (byte) 0xf2, (byte) 0x4b, (byte) 0x56,
            (byte) 0xaf, (byte) 0x22, (byte) 0x5e, (byte) 0xc3,
            (byte) 0x09, (byte) 0xf7, (byte) 0x68, (byte) 0x12,
            (byte) 0x80, (byte) 0x5d, (byte) 0x0a, (byte) 0x2c,
            (byte) 0xa3, (byte) 0xd3, (byte) 0xfb, (byte) 0x50,
            (byte) 0xe7, (byte) 0x4a, (byte) 0x1c, (byte) 0x3d,
            (byte) 0xcf, (byte) 0xf5, (byte) 0x98, (byte) 0x8d,
            (byte) 0x1e, (byte) 0x4d, (byte) 0x6b, (byte) 0x8b,
            (byte) 0x18, (byte) 0xed, (byte) 0xd6, (byte) 0x34,
            (byte) 0x60, (byte) 0x4f, (byte) 0x60, (byte) 0x70,
            (byte) 0xf0, (byte) 0xde, (byte) 0xb2, (byte) 0x05,
            (byte) 0x78, (byte) 0x9c, (byte) 0xf3, (byte) 0x30,
            (byte) 0x5d, (byte) 0xe1, (byte) 0xd6, (byte) 0xb1,
            (byte) 0xaf, (byte) 0x55, (byte) 0x5e, (byte) 0xae,
            (byte) 0x90, (byte) 0xb0, (byte) 0x8e, (byte) 0x2d,
            (byte) 0x73, (byte) 0xbf, (byte) 0xd3, (byte) 0x70,
            (byte) 0x41, (byte) 0xe0, (byte) 0x8f, (byte) 0xc2,
            (byte) 0x68, (byte) 0xdf, (byte) 0xaa, (byte) 0x82,
            (byte) 0x50, (byte) 0x6b, (byte) 0x05, (byte) 0xf1,
            (byte) 0xcc, (byte) 0xd9, (byte) 0xe6, (byte) 0xe6,
            (byte) 0xc7, (byte) 0x13, (byte) 0x28, (byte) 0x1b,
            (byte) 0xb0, (byte) 0xbb, (byte) 0xe9, (byte) 0x82,
            (byte) 0x2c, (byte) 0x42, (byte) 0xaf, (byte) 0xf7,
            (byte) 0xfd, (byte) 0x85, (byte) 0xa6, (byte) 0x22,
            (byte) 0x00, (byte) 0xc2, (byte) 0xc2, (byte) 0x50,
            (byte) 0x4e, (byte) 0x1d, (byte) 0xb1, (byte) 0x12,
            (byte) 0x4b, (byte) 0x51, (byte) 0xac, (byte) 0xf3,
            (byte) 0xb5, (byte) 0x0c, (byte) 0x3f, (byte) 0x19,
            (byte) 0x4c, (byte) 0xaa, (byte) 0xc9, (byte) 0x18,
            (byte) 0x89, (byte) 0x62, (byte) 0x20, (byte) 0x4e,
            (byte) 0x1e, (byte) 0xdd, (byte) 0xca, (byte) 0x9e,
            (byte) 0x15, (byte) 0x38, (byte) 0xc1, (byte) 0xb5,
            (byte) 0xba, (byte) 0x98, (byte) 0x79, (byte) 0x6f,
            (byte) 0xf1, (byte) 0x62, (byte) 0x5c, (byte) 0xac,
            (byte) 0x51, (byte) 0xe8, (byte) 0x1e, (byte) 0x39,
            (byte) 0x04, (byte) 0x7d, (byte) 0xa8, (byte) 0x77,
            (byte) 0x7c, (byte) 0xa6, (byte) 0xd9, (byte) 0xd6,
            (byte) 0xde, (byte) 0xbd, (byte) 0x61, (byte) 0x5d,
            (byte) 0x89, (byte) 0x66, (byte) 0x8d, (byte) 0xcc,
            (byte) 0xdf, (byte) 0xf0, (byte) 0x94, (byte) 0xc1,
            (byte) 0x93, (byte) 0xfb, (byte) 0xb0, (byte) 0x6c,
            (byte) 0x7d, (byte) 0x95, (byte) 0xde, (byte) 0x37,
            (byte) 0x43, (byte) 0xa0, (byte) 0x03, (byte) 0xe9,
            (byte) 0xf3, (byte) 0x09, (byte) 0xfb, (byte) 0xe5,
            (byte) 0x83, (byte) 0xd6, (byte) 0xb3, (byte) 0x48,
            (byte) 0x46, (byte) 0xcc, (byte) 0xb4, (byte) 0xc9,
            (byte) 0xe7, (byte) 0x73, (byte) 0xea, (byte) 0xc4,
            (byte) 0x67, (byte) 0x06, (byte) 0x6f, (byte) 0x2c,
            (byte) 0x64, (byte) 0x15, (byte) 0xcc, (byte) 0x24,
            (byte) 0xf8, (byte) 0xdb, (byte) 0x09, (byte) 0x4b,
            (byte) 0x67, (byte) 0x6e, (byte) 0x1a, (byte) 0x7d,
            (byte) 0xad, (byte) 0xe3, (byte) 0x4f, (byte) 0x5b,
            (byte) 0x55, (byte) 0xc2, (byte) 0xd4, (byte) 0x24,
            (byte) 0xf2, (byte) 0x5a, (byte) 0x73, (byte) 0xde,
            (byte) 0x48, (byte) 0x77, (byte) 0x42, (byte) 0x69,
            (byte) 0x2f, (byte) 0x9f, (byte) 0xf2, (byte) 0x41,
            (byte) 0xa2, (byte) 0x08, (byte) 0xed, (byte) 0x6d,
            (byte) 0x05, (byte) 0x7f, (byte) 0xa4, (byte) 0xce,
            (byte) 0xce, (byte) 0xec, (byte) 0xc2, (byte) 0x47,
            (byte) 0x20, (byte) 0x02, (byte) 0xf1, (byte) 0x48,
            (byte) 0xec, (byte) 0x1c, (byte) 0xfe, (byte) 0xec,
            (byte) 0x43, (byte) 0x12, (byte) 0x29, (byte) 0x4c,
            (byte) 0x86, (byte) 0xc1, (byte) 0x57, (byte) 0x33,
            (byte) 0xa9, (byte) 0xa9, (byte) 0x9a, (byte) 0x43,
            (byte) 0xb3, (byte) 0xec, (byte) 0x23, (byte) 0x70,
            (byte) 0x78, (byte) 0x5d, (byte) 0x77, (byte) 0x38,
            (byte) 0x59, (byte) 0x36, (byte) 0xdd, (byte) 0x19,
            (byte) 0x07, (byte) 0x4a, (byte) 0x78, (byte) 0x49,
            (byte) 0x27, (byte) 0x6b, (byte) 0x2f, (byte) 0xe8,
            (byte) 0xb3, (byte) 0xef, (byte) 0xd0, (byte) 0x1a,
            (byte) 0x75, (byte) 0xdc, (byte) 0xa9, (byte) 0x0c,
            (byte) 0x39, (byte) 0xb4, (byte) 0x48, (byte) 0x10,
            (byte) 0x7b, (byte) 0x0e, (byte) 0xfc, (byte) 0xa2,
            (byte) 0x56, (byte) 0xa2, (byte) 0x12, (byte) 0xbe,
            (byte) 0x5e, (byte) 0x82, (byte) 0xa5, (byte) 0xee,
            (byte) 0xd5, (byte) 0x76, (byte) 0x84, (byte) 0xb6,
            (byte) 0x1a, (byte) 0xba, (byte) 0x67, (byte) 0x57,
            (byte) 0x19, (byte) 0x12, (byte) 0xd9, (byte) 0x4a,
            (byte) 0x44, (byte) 0xa7, (byte) 0x86, (byte) 0x56,
            (byte) 0x9e, (byte) 0xe0, (byte) 0x2b, (byte) 0x0b,
            (byte) 0x3b, (byte) 0x46, (byte) 0x0c, (byte) 0x3a,
            (byte) 0xea, (byte) 0x12, (byte) 0x9b, (byte) 0x8d,
            (byte) 0x79, (byte) 0x1e, (byte) 0x34, (byte) 0x09,
            (byte) 0x98, (byte) 0xa9, (byte) 0x23, (byte) 0x3f,
            (byte) 0x49, (byte) 0xf5, (byte) 0x56, (byte) 0xdc,
            (byte) 0xe9, (byte) 0x8b, (byte) 0x77, (byte) 0x51,
            (byte) 0xec, (byte) 0x80, (byte) 0x40, (byte) 0xd2,
            (byte) 0xd3, (byte) 0x4a, (byte) 0x50, (byte) 0xc2,
            (byte) 0x7d, (byte) 0x2a, (byte) 0x38, (byte) 0x09,
            (byte) 0x70, (byte) 0x1e, (byte) 0xae, (byte) 0x5e,
            (byte) 0x4c, (byte) 0x56, (byte) 0xc1, (byte) 0xb5,
            (byte) 0x7c, (byte) 0x33, (byte) 0xb2, (byte) 0xb5,
            (byte) 0x8b, (byte) 0x57, (byte) 0xed, (byte) 0x92,
            (byte) 0x46, (byte) 0xeb, (byte) 0xb3, (byte) 0xd4,
            (byte) 0xc7, (byte) 0xa8, (byte) 0xab, (byte) 0x46,
            (byte) 0x27, (byte) 0x1a, (byte) 0xe9, (byte) 0xa0,
            (byte) 0x1e, (byte) 0x55, (byte) 0xde, (byte) 0x7a,
            (byte) 0x8b, (byte) 0xa5, (byte) 0x9b, (byte) 0x9f,
            (byte) 0x49, (byte) 0x65, (byte) 0x8f, (byte) 0xf6,
            (byte) 0x18, (byte) 0xf5, (byte) 0xdb, (byte) 0x51,
            (byte) 0x32, (byte) 0x80, (byte) 0xd2, (byte) 0xa7,
            (byte) 0x23, (byte) 0xf4, (byte) 0x1b, (byte) 0xd6,
            (byte) 0x4e, (byte) 0xc6, (byte) 0x3c, (byte) 0xc6,
            (byte) 0x26, (byte) 0x79, (byte) 0xde, (byte) 0xa1,
            (byte) 0x57, (byte) 0x27, (byte) 0x06, (byte) 0x22,
            (byte) 0x11, (byte) 0x7a, (byte) 0xcb, (byte) 0x99,
            (byte) 0xe4, (byte) 0x4c, (byte) 0x58, (byte) 0xde,
            (byte) 0x55, (byte) 0x95, (byte) 0x8f, (byte) 0xe7,
            (byte) 0x05, (byte) 0x29, (byte) 0x5e, (byte) 0xff,
            (byte) 0x1f, (byte) 0x36, (byte) 0xcd, (byte) 0xcd,
            (byte) 0x19, (byte) 0x3b, (byte) 0x54, (byte) 0xb0,
            (byte) 0x8c, (byte) 0x24, (byte) 0xf1, (byte) 0x8f,
            (byte) 0x9e, (byte) 0x34, (byte) 0xe1, (byte) 0xa7,
            (byte) 0x38, (byte) 0x60, (byte) 0xf1, (byte) 0xe9,
            (byte) 0x1e, (byte) 0xe6, (byte) 0x2b, (byte) 0x81,
            (byte) 0x10, (byte) 0x00, (byte) 0x0f, (byte) 0xb6,
            (byte) 0x60, (byte) 0xac, (byte) 0x43, (byte) 0xb5,
            (byte) 0x83, (byte) 0x05, (byte) 0x5a, (byte) 0x65,
            (byte) 0x42, (byte) 0x69, (byte) 0xad, (byte) 0x30,
            (byte) 0x51, (byte) 0x60, (byte) 0xae, (byte) 0x34,
            (byte) 0x89, (byte) 0xa2, (byte) 0x9f, (byte) 0x04,
            (byte) 0x0b, (byte) 0x29, (byte) 0x0f, (byte) 0x2e,
            (byte) 0x70, (byte) 0xaf, (byte) 0xf6, (byte) 0xd3,
            (byte) 0x2a, (byte) 0x96, (byte) 0xcb, (byte) 0xf9,
            (byte) 0x5f, (byte) 0x35, (byte) 0xd9, (byte) 0x56,
            (byte) 0x92, (byte) 0x2a, (byte) 0x8e, (byte) 0xb2,
            (byte) 0xb6, (byte) 0xba, (byte) 0xf9, (byte) 0xae,
            (byte) 0x8d, (byte) 0xdb, (byte) 0xb0, (byte) 0x1c,
            (byte) 0x34, (byte) 0xc5, (byte) 0xc3, (byte) 0x21,
            (byte) 0x0f, (byte) 0x2c, (byte) 0x6e, (byte) 0x4d,
            (byte) 0xe1, (byte) 0x69, (byte) 0x8c, (byte) 0xa8,
            (byte) 0x2a, (byte) 0xe9, (byte) 0xb9, (byte) 0xc0,
            (byte) 0xd5, (byte) 0xd2, (byte) 0xf2, (byte) 0x20,
            (byte) 0x7c, (byte) 0x49, (byte) 0x7e, (byte) 0xc0,
            (byte) 0x22, (byte) 0xb0, (byte) 0xab, (byte) 0x1a,
            (byte) 0x9f, (byte) 0x86, (byte) 0x3f, (byte) 0xff,
            (byte) 0x8c, (byte) 0x32, (byte) 0x21, (byte) 0xaf,
            (byte) 0x99, (byte) 0xed, (byte) 0x03, (byte) 0xcf,
            (byte) 0x80, (byte) 0xb8, (byte) 0x36, (byte) 0x57,
            (byte) 0x45, (byte) 0x12, (byte) 0x0c, (byte) 0xf9,
            (byte) 0x07, (byte) 0x8b, (byte) 0xc9, (byte) 0x90,
            (byte) 0xb5, (byte) 0x02, (byte) 0xda, (byte) 0x3c,
            (byte) 0xbb, (byte) 0x12, (byte) 0x22, (byte) 0xdc,
            (byte) 0xe4, (byte) 0x11, (byte) 0x7d, (byte) 0x99,
            (byte) 0x90, (byte) 0xf1, (byte) 0xf9, (byte) 0xf1,
            (byte) 0x0a, (byte) 0xd3, (byte) 0x11, (byte) 0x5c,
            (byte) 0xbb, (byte) 0xbe, (byte) 0x2d, (byte) 0x35,
            (byte) 0xf4, (byte) 0x7b, (byte) 0xc1, (byte) 0x29,
            (byte) 0x8c, (byte) 0x4c, (byte) 0x6f, (byte) 0xdd,
            (byte) 0x98, (byte) 0x3c, (byte) 0xf1, (byte) 0xc0,
            (byte) 0x2a, (byte) 0xd0, (byte) 0x22, (byte) 0x47,
            (byte) 0x4b, (byte) 0x6c, (byte) 0xf7, (byte) 0xe3,
            (byte) 0x6e, (byte) 0x2a, (byte) 0x86, (byte) 0x18,
            (byte) 0x29, (byte) 0xc1, (byte) 0x59, (byte) 0xc6,
            (byte) 0x1f, (byte) 0x89, (byte) 0xd8, (byte) 0xd9,
            (byte) 0x97, (byte) 0x2a, (byte) 0x3e, (byte) 0x8f,
            (byte) 0x6c, (byte) 0xa2, (byte) 0x0f, (byte) 0x58,
            (byte) 0x52, (byte) 0x59, (byte) 0xd5, (byte) 0x4b,
            (byte) 0x29, (byte) 0xcf, (byte) 0x0d, (byte) 0x68,
            (byte) 0x27, (byte) 0xcb, (byte) 0x60, (byte) 0x73,
            (byte) 0x0c, (byte) 0x20, (byte) 0xdd, (byte) 0xd1,
            (byte) 0x18, (byte) 0x95, (byte) 0x9b, (byte) 0xd9,
            (byte) 0x78, (byte) 0x29, (byte) 0x9d, (byte) 0xf6,
            (byte) 0x83, (byte) 0xfc, (byte) 0xe5, (byte) 0x26,
            (byte) 0x28, (byte) 0x31, (byte) 0x24, (byte) 0xc8,
            (byte) 0x5c, (byte) 0xcc, (byte) 0xf8, (byte) 0x2c,
            (byte) 0x38, (byte) 0x56, (byte) 0x9a, (byte) 0xb1,
            (byte) 0x4f, (byte) 0x5e, (byte) 0xd3, (byte) 0xa1,
            (byte) 0x53, (byte) 0xa6, (byte) 0xeb, (byte) 0xa7,
            (byte) 0xe3, (byte) 0xad, (byte) 0x2d, (byte) 0x76,
            (byte) 0xa3, (byte) 0xeb, (byte) 0xbd, (byte) 0xbe,
            (byte) 0x23, (byte) 0x95, (byte) 0x8f, (byte) 0x13,
            (byte) 0x37, (byte) 0xdd, (byte) 0xf7, (byte) 0xe5,
            (byte) 0x58, (byte) 0xf1, (byte) 0xef, (byte) 0xa7,
            (byte) 0xcb, (byte) 0x70, (byte) 0x90, (byte) 0x32,
            (byte) 0x5b, (byte) 0x92, (byte) 0xad, (byte) 0xf7,
            (byte) 0x8e, (byte) 0xb0, (byte) 0xc5, (byte) 0x7e,
            (byte) 0x79, (byte) 0xe2, (byte) 0x0e, (byte) 0xce,
            (byte) 0x7f, (byte) 0xe3, (byte) 0xde, (byte) 0x05,
            (byte) 0x46, (byte) 0x52, (byte) 0x78, (byte) 0x25,
            (byte) 0xbe, (byte) 0xa4, (byte) 0x16, (byte) 0x78,
            (byte) 0xe5, (byte) 0x76, (byte) 0x46, (byte) 0xb4,
            (byte) 0x69, (byte) 0x28, (byte) 0x65, (byte) 0xaa,
            (byte) 0x37, (byte) 0xbd, (byte) 0x07, (byte) 0x58,
            (byte) 0x59, (byte) 0x4a, (byte) 0xa0, (byte) 0x05,
            (byte) 0x57, (byte) 0x1a, (byte) 0x58, (byte) 0xbb,
            (byte) 0xfa, (byte) 0x70, (byte) 0x74, (byte) 0x02,
            (byte) 0xf2, (byte) 0xb3, (byte) 0x64, (byte) 0x88,
            (byte) 0x49, (byte) 0x9d, (byte) 0x51, (byte) 0xe2,
            (byte) 0x45, (byte) 0xb9, (byte) 0xaa, (byte) 0xd6,
            (byte) 0x31, (byte) 0xae, (byte) 0x58, (byte) 0x41,
            (byte) 0x5a, (byte) 0xac, (byte) 0x84, (byte) 0x53,
            (byte) 0x21, (byte) 0x3c, (byte) 0x63, (byte) 0x5d,
            (byte) 0xba, (byte) 0x86, (byte) 0x1a, (byte) 0xbb,
            (byte) 0xed, (byte) 0x3f, (byte) 0xd2, (byte) 0xa2,
            (byte) 0x13, (byte) 0x35, (byte) 0x22, (byte) 0x6d,
            (byte) 0xf0, (byte) 0x4d, (byte) 0xf3, (byte) 0x36,
            (byte) 0x68, (byte) 0xb8, (byte) 0x43, (byte) 0x8d,
            (byte) 0x80, (byte) 0x33, (byte) 0x65, (byte) 0x37,
            (byte) 0x32, (byte) 0x8e, (byte) 0xbf, (byte) 0x7d,
            (byte) 0x07, (byte) 0xc0, (byte) 0x1c, (byte) 0xb4,
            (byte) 0x3a, (byte) 0x0c, (byte) 0xe4, (byte) 0xb9,
            (byte) 0xfb, (byte) 0x51, (byte) 0x56, (byte) 0x81,
            (byte) 0xad, (byte) 0x05, (byte) 0x11, (byte) 0x04,
            (byte) 0x17, (byte) 0x94, (byte) 0x72, (byte) 0x03,
            (byte) 0x52, (byte) 0xfd, (byte) 0x1c, (byte) 0xcb,
            (byte) 0xd3, (byte) 0x63, (byte) 0xc0, (byte) 0xcf,
            (byte) 0x54, (byte) 0x84, (byte) 0xbc, (byte) 0x1f,
            (byte) 0x02, (byte) 0xbf, (byte) 0x9c, (byte) 0xb3,
            (byte) 0x9b, (byte) 0xa2, (byte) 0x0e, (byte) 0x3d,
            (byte) 0x29, (byte) 0x2e, (byte) 0x72, (byte) 0x6c,
            (byte) 0xb8, (byte) 0x06, (byte) 0x5f, (byte) 0xf4,
            (byte) 0x99, (byte) 0xc4, (byte) 0x04, (byte) 0xdd,
            (byte) 0x99, (byte) 0x84, (byte) 0xdc, (byte) 0xc3,
            (byte) 0xbb, (byte) 0xe1, (byte) 0x33, (byte) 0xf3,
            (byte) 0x04, (byte) 0x51, (byte) 0x15, (byte) 0x42,
            (byte) 0x53, (byte) 0x2b, (byte) 0x29, (byte) 0x2e,
            (byte) 0xd5, (byte) 0xa8, (byte) 0x55, (byte) 0x16,
            (byte) 0xc3, (byte) 0x0e, (byte) 0x07, (byte) 0x98,
            (byte) 0x1b, (byte) 0xed, (byte) 0xe8, (byte) 0xfc,
            (byte) 0xab, (byte) 0xdb, (byte) 0x6a, (byte) 0x8a,
            (byte) 0xfd, (byte) 0xcc, (byte) 0x0b, (byte) 0x42,
            (byte) 0x2e, (byte) 0x04, (byte) 0x20, (byte) 0x24,
            (byte) 0x6b, (byte) 0x18, (byte) 0x77, (byte) 0xe7,
            (byte) 0x98, (byte) 0xff, (byte) 0x32, (byte) 0x74,
            (byte) 0x2b, (byte) 0x37, (byte) 0xba, (byte) 0xe8,
            (byte) 0x1d, (byte) 0xfb, (byte) 0xab, (byte) 0xf3,
            (byte) 0x52, (byte) 0x41, (byte) 0x5a, (byte) 0xaf,
            (byte) 0xf7, (byte) 0x44, (byte) 0x72, (byte) 0x7f,
            (byte) 0x0c, (byte) 0x09, (byte) 0x52, (byte) 0x51,
            (byte) 0x53, (byte) 0x20, (byte) 0x0b, (byte) 0x78,
            (byte) 0xaf, (byte) 0x9f, (byte) 0x17, (byte) 0x9d,
            (byte) 0xa0, (byte) 0xbc, (byte) 0x67, (byte) 0x37,
            (byte) 0x08, (byte) 0x59, (byte) 0xa1, (byte) 0xf3,
            (byte) 0x80, (byte) 0xce, (byte) 0xa2, (byte) 0x0a,
            (byte) 0xce, (byte) 0x16, (byte) 0xa3, (byte) 0xcf,
            (byte) 0x36, (byte) 0x86, (byte) 0x2a, (byte) 0xf1,
            (byte) 0x20, (byte) 0xd0, (byte) 0xfe, (byte) 0xae,
            (byte) 0x11, (byte) 0x89, (byte) 0x98, (byte) 0x3e,
            (byte) 0x47, (byte) 0x11, (byte) 0x2c, (byte) 0xda,
            (byte) 0x23, (byte) 0xe9, (byte) 0x5e, (byte) 0x48,
            (byte) 0xd2, (byte) 0xf7, (byte) 0x76, (byte) 0xa3,
            (byte) 0xac, (byte) 0xc1, (byte) 0xba, (byte) 0x5a,
            (byte) 0x01, (byte) 0xc1, (byte) 0x37, (byte) 0xa1,
            (byte) 0x3c, (byte) 0xfb, (byte) 0x92, (byte) 0x9d,
            (byte) 0x41, (byte) 0xc0, (byte) 0x22, (byte) 0xda,
            (byte) 0x09, (byte) 0xa2, (byte) 0xcd, (byte) 0xac,
            (byte) 0xdf, (byte) 0xd4, (byte) 0x8c, (byte) 0x37,
            (byte) 0x7a, (byte) 0x18, (byte) 0x08, (byte) 0xa1,
            (byte) 0x1c, (byte) 0x3b, (byte) 0x37, (byte) 0xf0,
            (byte) 0x91, (byte) 0xeb, (byte) 0x1e, (byte) 0x74,
            (byte) 0xf5, (byte) 0xc5, (byte) 0xf5, (byte) 0x35,
            (byte) 0x97, (byte) 0x88, (byte) 0x01, (byte) 0x59,
            (byte) 0x32, (byte) 0xaf, (byte) 0xad, (byte) 0xa7,
            (byte) 0x87, (byte) 0xf5, (byte) 0xf4, (byte) 0x5b,
            (byte) 0x0d, (byte) 0xe8, (byte) 0xe5, (byte) 0x28,
            (byte) 0xae, (byte) 0xda, (byte) 0x7c, (byte) 0x5c,
            (byte) 0x7b, (byte) 0x81, (byte) 0x25, (byte) 0x31,
            (byte) 0x05, (byte) 0xff, (byte) 0x4f, (byte) 0x64,
            (byte) 0x8e, (byte) 0x67, (byte) 0x3e, (byte) 0x75,
            (byte) 0x87, (byte) 0x7c, (byte) 0x9b, (byte) 0x1f,
            (byte) 0xe5, (byte) 0x37, (byte) 0xf0, (byte) 0x41,
            (byte) 0xe3, (byte) 0xd3, (byte) 0x17, (byte) 0x12,
            (byte) 0x3c, (byte) 0xce, (byte) 0xdd, (byte) 0x4e,
            (byte) 0xfd, (byte) 0xd3, (byte) 0x65, (byte) 0x81,
            (byte) 0xc5, (byte) 0x13, (byte) 0x30, (byte) 0x12,
            (byte) 0xea, (byte) 0x99, (byte) 0xff, (byte) 0xc1,
            (byte) 0x1b, (byte) 0x9f, (byte) 0x20, (byte) 0x82,
            (byte) 0xee, (byte) 0x01, (byte) 0xb2, (byte) 0x15,
            (byte) 0xc1, (byte) 0x22, (byte) 0xc7, (byte) 0x60,
            (byte) 0x0b, (byte) 0x19, (byte) 0x12, (byte) 0xa7,
            (byte) 0x89, (byte) 0x81, (byte) 0xb9, (byte) 0xdc,
            (byte) 0x76, (byte) 0x7b, (byte) 0x3b, (byte) 0x31,
            (byte) 0xa1, (byte) 0x7c, (byte) 0xe6, (byte) 0xae,
            (byte) 0xd2, (byte) 0x94, (byte) 0x37, (byte) 0xf1,
            (byte) 0xa9, (byte) 0xd0, (byte) 0x6d, (byte) 0xbd,
            (byte) 0x48, (byte) 0x2e, (byte) 0x21, (byte) 0x7f,
            (byte) 0xdc, (byte) 0xe4, (byte) 0x61, (byte) 0x47,
            (byte) 0x8f, (byte) 0xef, (byte) 0x68, (byte) 0x9d,
            (byte) 0xe5, (byte) 0xb2, (byte) 0x30, (byte) 0x63,
            (byte) 0x6c, (byte) 0x3d, (byte) 0x96, (byte) 0x0d,
            (byte) 0x3d, (byte) 0x00, (byte) 0x86, (byte) 0x59,
            (byte) 0xd8, (byte) 0x9f, (byte) 0x24, (byte) 0x12,
            (byte) 0x38, (byte) 0xb6, (byte) 0xe8, (byte) 0x71,
            (byte) 0x6e, (byte) 0xba, (byte) 0x64, (byte) 0x4d,
            (byte) 0xdd, (byte) 0x76, (byte) 0xd3, (byte) 0x87,
            (byte) 0xd7, (byte) 0xbd, (byte) 0x64, (byte) 0x83,
            (byte) 0x19, (byte) 0x99, (byte) 0x9c, (byte) 0x4e,
            (byte) 0x21, (byte) 0x24, (byte) 0xc3, (byte) 0xcc,
            (byte) 0x73, (byte) 0x56, (byte) 0x55, (byte) 0x25,
            (byte) 0xa2, (byte) 0x8d, (byte) 0xa2, (byte) 0x08,
            (byte) 0xa6, (byte) 0x51, (byte) 0xe5, (byte) 0x65,
            (byte) 0x56, (byte) 0xcb, (byte) 0x75, (byte) 0x1d,
            (byte) 0xb2, (byte) 0x08, (byte) 0x99, (byte) 0xb8,
            (byte) 0xbe, (byte) 0x66, (byte) 0xec, (byte) 0x48,
            (byte) 0x05, (byte) 0xf4, (byte) 0xe9, (byte) 0xe1,
            (byte) 0xaa, (byte) 0xb5, (byte) 0x2a, (byte) 0x8c,
            (byte) 0x78, (byte) 0xd3, (byte) 0xc0, (byte) 0x26,
            (byte) 0x8b, (byte) 0x80, (byte) 0x68, (byte) 0x53,
            (byte) 0x96, (byte) 0x39, (byte) 0xba, (byte) 0x99,
            (byte) 0xe2, (byte) 0x19, (byte) 0x4b, (byte) 0x6d,
            (byte) 0xa0, (byte) 0xd2, (byte) 0x47, (byte) 0x41,
            (byte) 0x28, (byte) 0xb7, (byte) 0x6b, (byte) 0xdb,
            (byte) 0x82, (byte) 0xcc, (byte) 0x12, (byte) 0xea,
            (byte) 0x2b, (byte) 0x64, (byte) 0xea, (byte) 0x86,
            (byte) 0x0a, (byte) 0xa9, (byte) 0xa5, (byte) 0x98,
            (byte) 0xac, (byte) 0xfe, (byte) 0x9f, (byte) 0x3e,
            (byte) 0x2f, (byte) 0xf1, (byte) 0x85, (byte) 0x87,
            (byte) 0xb4, (byte) 0x21, (byte) 0x99, (byte) 0xc5,
            (byte) 0x37, (byte) 0x5b, (byte) 0xd9, (byte) 0x02,
            (byte) 0x6d, (byte) 0x4b, (byte) 0x7f, (byte) 0xc4,
            (byte) 0x62, (byte) 0x30, (byte) 0x51, (byte) 0xbc,
            (byte) 0x00, (byte) 0xaa, (byte) 0x8a, (byte) 0x14,
            (byte) 0xee, (byte) 0x2c, (byte) 0x7f, (byte) 0x90,
            (byte) 0xdc, (byte) 0xd6, (byte) 0xf6, (byte) 0xf8,
            (byte) 0x86, (byte) 0xc5, (byte) 0xfb, (byte) 0xf0,
            (byte) 0x5e, (byte) 0x7b, (byte) 0xc2, (byte) 0xed,
            (byte) 0xc8, (byte) 0xbd, (byte) 0x5a, (byte) 0x26,
            (byte) 0xd3, (byte) 0x69, (byte) 0xc8, (byte) 0x9a,
            (byte) 0x80, (byte) 0x5f, (byte) 0x8e, (byte) 0x91,
            (byte) 0x31, (byte) 0xfc, (byte) 0xac, (byte) 0x76,
            (byte) 0x56, (byte) 0xeb, (byte) 0x19, (byte) 0xa0,
            (byte) 0x40, (byte) 0x9f, (byte) 0xe3, (byte) 0x93,
            (byte) 0x64, (byte) 0xa9, (byte) 0x4a, (byte) 0x03,
            (byte) 0x08, (byte) 0x0d, (byte) 0xbd, (byte) 0xe4,
            (byte) 0x70, (byte) 0x72, (byte) 0x94, (byte) 0x9d,
            (byte) 0xd3, (byte) 0x72, (byte) 0xa9, (byte) 0x66,
            (byte) 0x6a, (byte) 0x9b, (byte) 0xf5, (byte) 0xcc,
            (byte) 0xa1, (byte) 0x30, (byte) 0xee, (byte) 0xc9,
            (byte) 0x46, (byte) 0xda, (byte) 0xf8, (byte) 0x0b,
            (byte) 0x2e, (byte) 0xb5, (byte) 0x72, (byte) 0x9a,
            (byte) 0xdd, (byte) 0xeb, (byte) 0x71, (byte) 0x0c,
            (byte) 0x28, (byte) 0xda, (byte) 0xaf, (byte) 0x66,
            (byte) 0xb1, (byte) 0x09, (byte) 0xbc, (byte) 0x84,
            (byte) 0x6a, (byte) 0xab, (byte) 0x32, (byte) 0x9a,
            (byte) 0xef, (byte) 0x83, (byte) 0xc4, (byte) 0x0a,
            (byte) 0xfe, (byte) 0xb6, (byte) 0xa5, (byte) 0x40,
            (byte) 0xe3, (byte) 0xfa, (byte) 0x47, (byte) 0x49,
            (byte) 0x0c, (byte) 0x7c, (byte) 0x92, (byte) 0xbe,
            (byte) 0x6c, (byte) 0xcd, (byte) 0xbe, (byte) 0xe3,
            (byte) 0xd2, (byte) 0xc7, (byte) 0x95, (byte) 0xd4,
            (byte) 0x3d, (byte) 0x48, (byte) 0xa5, (byte) 0xb8,
            (byte) 0xfd, (byte) 0x45, (byte) 0x18, (byte) 0xc8,
            (byte) 0xd0, (byte) 0xd0, (byte) 0xd1, (byte) 0xbf,
            (byte) 0x88, (byte) 0x89, (byte) 0x88, (byte) 0xc4,
            (byte) 0x08, (byte) 0xde, (byte) 0x92, (byte) 0x22,
            (byte) 0x62, (byte) 0x71, (byte) 0x4b, (byte) 0xcc,
            (byte) 0x8f, (byte) 0xe7, (byte) 0x40, (byte) 0x33,
            (byte) 0x89, (byte) 0xc2, (byte) 0x81, (byte) 0x19,
            (byte) 0xa8, (byte) 0x45, (byte) 0x9d, (byte) 0x7b,
            (byte) 0xf1, (byte) 0xc2, (byte) 0xf0, (byte) 0x59,
            (byte) 0x8a, (byte) 0x8b, (byte) 0x6b, (byte) 0x5c,
            (byte) 0xef, (byte) 0x89, (byte) 0xe6, (byte) 0x6d,
            (byte) 0x6b, (byte) 0xd9, (byte) 0x54, (byte) 0xf3,
            (byte) 0xa6, (byte) 0x19, (byte) 0xfd, (byte) 0x32,
            (byte) 0xf1, (byte) 0xbf, (byte) 0xe5, (byte) 0x95,
            (byte) 0xe1, (byte) 0x9a, (byte) 0x74, (byte) 0x94,
            (byte) 0x0b, (byte) 0x77, (byte) 0x89, (byte) 0xd0,
            (byte) 0x35, (byte) 0x57, (byte) 0x9d, (byte) 0xde,
            (byte) 0x14, (byte) 0x89, (byte) 0x2b, (byte) 0x4b,
            (byte) 0x66, (byte) 0xc9, (byte) 0x58, (byte) 0x8d,
            (byte) 0x53, (byte) 0x7a, (byte) 0xcb, (byte) 0xc6,
            (byte) 0xf1, (byte) 0xb7, (byte) 0xb1, (byte) 0xa5,
            (byte) 0xb0, (byte) 0x59, (byte) 0x3d, (byte) 0x29,
            (byte) 0x2a, (byte) 0x78, (byte) 0x0c, (byte) 0x63,
            (byte) 0x2f, (byte) 0xa0, (byte) 0x49, (byte) 0xeb,
            (byte) 0x88, (byte) 0x51, (byte) 0x4f, (byte) 0x2d,
            (byte) 0xe9, (byte) 0x29, (byte) 0xfd, (byte) 0xa9,
            (byte) 0xf0, (byte) 0x89, (byte) 0x5e, (byte) 0x5c,
            (byte) 0x29, (byte) 0x0e, (byte) 0x89, (byte) 0x0f,
            (byte) 0xae, (byte) 0x05, (byte) 0x00, (byte) 0x21,
            (byte) 0x25, (byte) 0x64, (byte) 0x5f, (byte) 0x30,
            (byte) 0x5e, (byte) 0x6b, (byte) 0x2c, (byte) 0xe6,
            (byte) 0xe6, (byte) 0x69, (byte) 0x29, (byte) 0x85,
            (byte) 0x92, (byte) 0xd1, (byte) 0xe5, (byte) 0x11,
            (byte) 0xcd, (byte) 0x5e, (byte) 0xb1, (byte) 0x46,
            (byte) 0xe8, (byte) 0x1c, (byte) 0x47, (byte) 0x3c,
            (byte) 0x93, (byte) 0x18, (byte) 0xe9, (byte) 0xea,
            (byte) 0x59, (byte) 0x78, (byte) 0x7d, (byte) 0x1b,
            (byte) 0x3d, (byte) 0x9d, (byte) 0x30, (byte) 0x54,
            (byte) 0x9a, (byte) 0x72, (byte) 0xea, (byte) 0x5a,
            (byte) 0x78, (byte) 0xc8, (byte) 0xaf, (byte) 0xa5,
            (byte) 0xd4, (byte) 0x78, (byte) 0xf7, (byte) 0xa1,
            (byte) 0x42, (byte) 0xff, (byte) 0xa1, (byte) 0x19,
            (byte) 0x6d, (byte) 0xb9, (byte) 0x56, (byte) 0xc5,
            (byte) 0x9b, (byte) 0x37, (byte) 0x3a, (byte) 0x88,
    };

    public static void main(String[] args) throws Exception {
        byte[] bytesToEncrypt = new byte[25];
        bytesToEncrypt[0] = 1;
        System.arraycopy(MAGIC_VALUE, 0, bytesToEncrypt, 1, MAGIC_VALUE.length);

        byte[] serverChallenge = new byte[] {
                (byte) 0x38, (byte) 0x51, (byte) 0x4f, (byte) 0x6a,
                (byte) 0x50, (byte) 0xc3, (byte) 0x2a, (byte) 0x26
        };
        System.arraycopy(serverChallenge, 0, bytesToEncrypt, 9, serverChallenge.length);
        byte[] clientChallenge = new byte[] {
                (byte) 0x5c, (byte) 0xe7, (byte) 0x9e, (byte) 0xf8,
                (byte) 0xf8, (byte) 0xf7, (byte) 0x58, (byte) 0xbc
        };
        System.arraycopy(clientChallenge, 0, bytesToEncrypt, 17, clientChallenge.length);

        int paddingLength = 32 - 25;
        byte[] toEncrypt = new byte[bytesToEncrypt.length + paddingLength];
        System.arraycopy(bytesToEncrypt, 0, toEncrypt, 0, bytesToEncrypt.length);
        Arrays.fill(toEncrypt, bytesToEncrypt.length, toEncrypt.length, (byte) paddingLength);

        SecretKeySpec secretKey = new SecretKeySpec(KEY_BYTES, "AES");
        byte[] iv = "cephsageyudagreg".getBytes();

        Cipher cipher = Cipher.getInstance("AES/CBC/NoPadding");
        cipher.init(Cipher.ENCRYPT_MODE, secretKey, new IvParameterSpec(iv));

        byte[] encrypted = cipher.doFinal(toEncrypt);
        byte[] readyForXor = new byte[encrypted.length + 4];
        readyForXor[0] = 32;
        System.arraycopy(encrypted, 0, readyForXor, 4, encrypted.length);

        byte[] result = new byte[8];
        for (int i = 0; i - (i % 8) + 8 < readyForXor.length; i++) {
            result[i % 8] ^= readyForXor[i];
        }

        HexFunctions.printHexString(result);

        System.out.println("---------------------------------------- ");
        cipher.init(Cipher.DECRYPT_MODE, secretKey, new IvParameterSpec(iv));
        byte[] decryptedBytes = cipher.doFinal(SERVICE_TICKET_BYTES);
        HexFunctions.printHexString(decryptedBytes);
        ByteBuf byteBuf = Unpooled.wrappedBuffer(decryptedBytes);
        byteBuf.skipBytes(9);
        CephXServiceTicket serviceTicket = CephDecoder.decode(byteBuf, true, CephXServiceTicket.class);

        System.out.println("---------------------------------------- ");
        byteBuf = Unpooled.wrappedBuffer(TICKET_BYTES);
        CephXTicketBlob blob = CephDecoder.decode(byteBuf, true, CephXTicketBlob.class);
        HexFunctions.printHexString(blob.getBlob());

        System.out.println("---------------------------------------- ");
        SecretKeySpec sessionKey = new SecretKeySpec(serviceTicket.getSessionKey().getSecret(), "AES");
        cipher.init(Cipher.DECRYPT_MODE, sessionKey, new IvParameterSpec(iv));
        byte[] decryptedSecretBytes = cipher.doFinal(CONNECTION_SECRET_BYTES);
        byteBuf = Unpooled.wrappedBuffer(decryptedSecretBytes);
        byteBuf.skipBytes(9);
        int byteLength = byteBuf.readIntLE();
        byte[] connectionSecret = new byte[byteLength];
        byteBuf.readBytes(connectionSecret);
        HexFunctions.printHexString(connectionSecret);

        // Connection Secret
        // 16 bytes - key - AES GCM
        // 12 bytes - RX nonce
        //   4 bytes - fixed (rev0)
        //   8 bytes - counter (rev1)
        // 12 bytes - TX nonce
        //   4 bytes - fixed (rev0)
        //   8 bytes - counter (rev1)
        byte[] keyBytes = new byte[16];
        System.arraycopy(connectionSecret, 0, keyBytes, 0, 16);
        byte[] nonceBytes = new byte[12];
        System.arraycopy(connectionSecret, 16, nonceBytes, 0, 12);
        byte[] nonceBytes2 = new byte[12];
        System.arraycopy(connectionSecret, 28, nonceBytes2, 0, 12);
        System.out.println("-----------------------------");
        HexFunctions.printHexString(keyBytes);
        System.out.println("-----------------------------");
        //ByteBuffer nonceBuffer = ByteBuffer.wrap(nonceBytes);
        //nonceBuffer.order(ByteOrder.LITTLE_ENDIAN);
        //long nonce = nonceBuffer.getLong(4);
        //nonce++;
        //nonceBuffer.position(4);
        //nonceBuffer.putLong(nonce);

        GCMParameterSpec gcmParameterSpec = new GCMParameterSpec(128, nonceBytes);
        SecretKeySpec newKey = new SecretKeySpec(keyBytes, "AES");
        cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, newKey, gcmParameterSpec);
        byte[] sigMsgBytes = cipher.doFinal(ENCRYPTED_MESSAGE);
        HexFunctions.printHexString(sigMsgBytes);
        System.out.println("-----------------------------");
        //AuthSignatureFrame authSignature = new AuthSignatureFrame();
        //authSignature.decode(new ByteArrayInputStream(sigMsgBytes));
        //HexFunctions.printHexString(authSignature.getSha256Digest());

        byte[] allBytes;
        try (InputStream is = AESTest.class.getClassLoader().getResourceAsStream("all_messages.bin")) {
            allBytes = is.readAllBytes();
        }
        Mac mac = Mac.getInstance("HmacSHA256");
        mac.init(sessionKey);
        HexFunctions.printHexString(mac.doFinal(allBytes));

        System.out.println("--------------------------------------------------------");
        gcmParameterSpec = new GCMParameterSpec(128, nonceBytes2);
        SecretKeySpec txKey = new SecretKeySpec(keyBytes, "AES");
        cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, txKey, gcmParameterSpec);
        byte[] sentBytes = cipher.doFinal(ENCRYPTED_MESSAGE2, 0, 96);
        HexFunctions.printHexString(sentBytes);

        System.out.println("--------------------------------------------------------");
        HexFunctions.printHexString(nonceBytes);
        System.out.println();
        HexFunctions.printHexString(nonceBytes2);
        System.out.println("--------------------------------------------------------");
        gcmParameterSpec = new GCMParameterSpec(128, nonceBytes2);
        cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, txKey, gcmParameterSpec);
        sentBytes = cipher.doFinal(ENCRYPTED_MESSAGE2, 0, 96);
        AuthSignatureFrame signatureFrame = new AuthSignatureFrame();
        signatureFrame.decodeSegment1(Unpooled.wrappedBuffer(sentBytes, 32, 32), true);
        HexFunctions.printHexString(sentBytes);
        System.out.println("--------------------------------------------------------");
        nonceBytes2[4] = (byte) 0x11;
        gcmParameterSpec = new GCMParameterSpec(128, nonceBytes2);
        cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, txKey, gcmParameterSpec);
        sentBytes = cipher.doFinal(ENCRYPTED_MESSAGE2, 96, 96);
        CompressionRequestFrame compressionRequestFrame = new CompressionRequestFrame();
        compressionRequestFrame.decodeSegment1(Unpooled.wrappedBuffer(sentBytes, 32, 5), true);
        HexFunctions.printHexString(sentBytes);

        System.out.println("--------------------------------------------------------");
        nonceBytes[4] = (byte) 0xb9;
        gcmParameterSpec = new GCMParameterSpec(128, nonceBytes);
        cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, txKey, gcmParameterSpec);
        sentBytes = cipher.doFinal(ENCRYPTED_MESSAGE3, 0, 96);
        CompressionDoneFrame compressionDoneFrame = new CompressionDoneFrame();
        compressionDoneFrame.decodeSegment1(Unpooled.wrappedBuffer(sentBytes, 32, 5), true);
        HexFunctions.printHexString(sentBytes);

        System.out.println("--------------------------------------------------------");
        nonceBytes2[4] = (byte) 0x12;
        gcmParameterSpec = new GCMParameterSpec(128, nonceBytes2);
        cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, txKey, gcmParameterSpec);
        sentBytes = cipher.doFinal(ENCRYPTED_MESSAGE4, 0, 96);
        byte[] fullSegment = new byte[123];
        System.arraycopy(sentBytes, 32, fullSegment, 0, 48);
        HexFunctions.printHexString(sentBytes);

        //System.out.println("--------------------------------------------------------");
        nonceBytes2[4] = (byte) 0x13;
        gcmParameterSpec = new GCMParameterSpec(128, nonceBytes2);
        cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, txKey, gcmParameterSpec);
        sentBytes = cipher.doFinal(ENCRYPTED_MESSAGE4, 96, 96);
        HexFunctions.printHexString(sentBytes);
        System.arraycopy(sentBytes, 0, fullSegment, 48, 123 - 48);
        ClientIdentFrame clientIdentFrame = new ClientIdentFrame();
        clientIdentFrame.decodeSegment1(Unpooled.wrappedBuffer(fullSegment), true);

        byte[] bar = new byte[8];
        ByteBuf foo = Unpooled.wrappedBuffer(bar);
        foo.writerIndex(0);
        foo.writeLongLE(clientIdentFrame.getSegment1().getClientCookie());
        System.out.println("--------------------------------------------------------");
        HexFunctions.printHexString(bar);

        System.out.println("--------------------------------------------------------");
        nonceBytes[4] = (byte) 0xba;
        gcmParameterSpec = new GCMParameterSpec(128, nonceBytes);
        cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, txKey, gcmParameterSpec);
        sentBytes = cipher.doFinal(ENCRYPTED_MESSAGE5, 0, 96);
        HexFunctions.printHexString(sentBytes);

        nonceBytes[4] = (byte) 0xbb;
        gcmParameterSpec = new GCMParameterSpec(128, nonceBytes);
        cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, txKey, gcmParameterSpec);
        byte[] sentBytes2 = cipher.doFinal(ENCRYPTED_MESSAGE5, 96, 96);
        HexFunctions.printHexString(sentBytes2);

        byte[] x = new byte[sentBytes.length + sentBytes2.length];
        System.arraycopy(sentBytes, 0, x, 0, sentBytes.length);
        System.arraycopy(sentBytes2, 0, x, sentBytes.length, sentBytes2.length);
        ServerIdentFrame serverIdentFrame = new ServerIdentFrame();
        serverIdentFrame.decodeSegment1(Unpooled.wrappedBuffer(x, 32, x.length - 32), true);

        System.out.println("MSG 6a -------------------------------------------------");
        nonceBytes2[4] = (byte) 0x14;
        gcmParameterSpec = new GCMParameterSpec(128, nonceBytes2);
        cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, txKey, gcmParameterSpec);
        sentBytes = cipher.doFinal(ENCRYPTED_MESSAGE6, 0, 96);
        HexFunctions.printHexString(sentBytes);
        MessageFrame messageFrame = new MessageFrame();
        messageFrame.decodeSegment1(Unpooled.wrappedBuffer(sentBytes, 32, sentBytes.length - 32), true);

        System.out.println("MSG 6b -------------------------------------------------");
        nonceBytes2[4] = (byte) 0x15;
        gcmParameterSpec = new GCMParameterSpec(128, nonceBytes2);
        cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, txKey, gcmParameterSpec);
        sentBytes = cipher.doFinal(ENCRYPTED_MESSAGE6, 96, 96);
        HexFunctions.printHexString(sentBytes);
        sentBytes2 = new byte[160];
        System.arraycopy(sentBytes, 0, sentBytes2, 0, sentBytes.length);

        nonceBytes2[4] = (byte) 0x16;
        gcmParameterSpec = new GCMParameterSpec(128, nonceBytes2);
        cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, txKey, gcmParameterSpec);
        sentBytes = cipher.doFinal(ENCRYPTED_MESSAGE6, 192, 96);
        HexFunctions.printHexString(sentBytes);
        System.arraycopy(sentBytes, 0, sentBytes2, 80, sentBytes.length);
        messageFrame = new MessageFrame();
        messageFrame.decodeSegment1(Unpooled.wrappedBuffer(sentBytes2, 32, 41), true);
        messageFrame.decodeSegment2(Unpooled.wrappedBuffer(sentBytes2, 80, 57), true);

        System.out.println("MSG 7 --------------------------------------------------");
        nonceBytes[4] = (byte) 0xbc;
        gcmParameterSpec = new GCMParameterSpec(128, nonceBytes);
        cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, txKey, gcmParameterSpec);
        sentBytes = cipher.doFinal(ENCRYPTED_MESSAGE7, 0, 96);
        HexFunctions.printHexString(sentBytes);

        nonceBytes[4] = (byte) 0xbd;
        gcmParameterSpec = new GCMParameterSpec(128, nonceBytes);
        cipher = Cipher.getInstance("AES/GCM/NoPadding");
        cipher.init(Cipher.DECRYPT_MODE, txKey, gcmParameterSpec);
        sentBytes = cipher.doFinal(ENCRYPTED_MESSAGE7, 96, 672);
        HexFunctions.printHexString(sentBytes);
    }
}
