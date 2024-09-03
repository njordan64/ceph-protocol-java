# Ceph Protocol v2.1

Packets of information are exchanged between two hosts as `ControlFrames`. It is possible to upgrade a connection so that the frames are compressed or encrypted.

## ControlFrame

A `ControlFrame` consists of a header (the first 32 bytes) containing metadata and up to four data segments.

### ControlFrame Header

This is the layout of the header. It is 32 bytes long. All values are little endian encoded.

| Data Type      | Length  | Description                       |
| -------------- | ------- | --------------------------------- |
| byte           | 1 byte  | Number of data segments           |
| byte           | 1 byte  | Frame type                        |
| unsigned int   | 4 bytes | Length of the first data segment  |
| unsigned short | 2 bytes | Indicates whether the first data segment is little endian or big endian encoded |
| unsigned int   | 4 bytes | Length of the second data segment |
| unsigned short | 2 bytes | Indicates whether the second data segment is little endian or big endian encoded |
| unsigned int   | 4 bytes | Length of the third data segment  |
| unsigned short | 2 bytes | Indicates whether the third data segment is little endian or big endian encoded |
| unsigned int   | 4 bytes | Length of the fourth data segment |
| unsigned short | 2 bytes | Indicates whether the fourth data segment is little endian or big endian encoded |
| byte           | 1 byte  | Flags                             |
| byte           | 1 byte  | Reserved                          |
| unsigned int   | 4 bytes | CRC-32C checksum of the previous 28 bytes |

**Example**
```
01 01 24 00 00 00 08 00   00 00 00 00 00 00 00 00
00 00 00 00 00 00 00 00   00 00 00 00 3f bd 6b 06
```

These are the parsed values

| Field                   | Bytes         | Value     |
| ----------------------- | ------------- | --------- |
| Frame Type              | `01`          | 1         |
| Number of data segments | `01`          | 1         |
| Segment 1 legnth        | `24 00 00 00` | 36        |
| Segment 1 Endianness    | `08 00`       | 8         |
| Segment 2 legnth        | `00 00 00 00` | 0         |
| Segment 2 Endianness    | `00 00`       | 0         |
| Segment 3 legnth        | `00 00 00 00` | 0         |
| Segment 3 Endianness    | `00 00`       | 0         |
| Segment 4 legnth        | `00 00 00 00` | 0         |
| Segment 4 Endianness    | `00 00`       | 0         |
| Flags                   | `00`          | 0         |
| CRC32-C checksum        | `3f bd 6b 06` | 107724095 |

## ControlFrame Types

| Name                   | Value |
| ---------------------- | ----- |
| Hello                  | 1     |
| Auth Request           | 2     |
| Auth Bad Method        | 3     |
| Auth Reply More        | 4     |
| Auth Request More      | 5     |
| Auth Done              | 6     |
| Auth Signature         | 7     |
| Client Ident           | 8     |
| Server Ident           | 9     |
| Ident Missing Features | 10    |
| Session Reconnect      | 11    |
| Session Reset          | 12    |
| Session Retry          | 13    |
| Session Retry Global   | 14    |
| Session Reconnect OK   | 15    |
| Wait                   | 16    |
| Message                | 17    |
| Keep Alive 2           | 18    |
| Keep Alive 2 ACK       | 19    |
| ACK                    | 20    |
| Compression Request    | 21    |
| Compression Done       | 22    |

[Source Code](https://github.com/ceph/ceph/blob/main/src/msg/async/frames_v2.h#L39)

## Banner Frame

This a special message used to initialize the connection. Immediately after connecting to the remote host, this message must be sent. The remote host will also send this message. Parse it to determine the protocol features to use.

The banner will look like this:

```
63 65 70 68 20 76 32 0a  10 00 03 00 00 00 00 00
00 00 00 00 00 00 00 00  00 00
```

Only the last 16 bytes can change.

### Format

| Field              | Length | Description                                          |
| ------------------ | ------ | ---------------------------------------------------- |
| Prefix             | 8      | Constant bytes (ASCII encoding of `ceph v2\n`)       |
| Body Length        | 2      | little endian encoded length of the body (always 16) |
| Supported Features | 8      | Protocol features supported by this host. Little endian encoded unsigned int (64 bit) |
| Required Features  | 8      | Protocol features that this host requires. Little endian encoded unsigned int (64 bit) |

[Source Code](https://github.com/ceph/ceph/blob/main/src/msg/async/ProtocolV2.cc#L851)
