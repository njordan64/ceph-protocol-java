# Strings

A string is simply the bytes of the string prefixed by the length as a 4 byte unsigned integer.

# Time

Represents a time with nano second precision. The first 4 bytes is the time in UNIX epoch seconds. The second 4 bytes are the nanoseconds within the second.

# Byte Array

A list of bytes, usually prefixed with the length as an unsigned 32-bit integer

# Address IPv4

Wraps a socket address which is either a `sockaddr_in` or `sockaddr_in6` data structure from `sys/socket.h`.

| Field                 | Length | Description                                  |
| --------------------- | ------ | -------------------------------------------- |
| Marker                | 1      | Constant (Always 1)                          |
| Version               | 1      | Data structure version (always 1)            |
| Compat Version        | 1      | Data structure compatible version            |
| Size                  | 4      | Data structure size (always 26)              |
| Type                  | 4      | Data structure encoding type                 |
| Nonce                 | 4      | Constant (always 0)                          |
| C data structure size | 2      | Size of C data structure                     |
| Socket Address        |        | A sockaddr_in or sockaddr_in6 data structure |

*Type* is used to specify the data structure version used.
* 0 - No version specified
* 1 - Legacy msgr1 protocol (ceph jewel and older)
* 2 - msgr2 protocol (ceph kraken and newer)
* 3 - ambiguous version

[Source Code](https://github.com/ceph/ceph/blob/main/src/msg/msg_types.h#L237)

# Auth Request Payload

The data structure used depends on the authentication method that is used. The two options here are:
* CephX Mon Auth Payload
* CephX Authorizer Payload

## CephX Mon Auth Payload

| Field                 | Length | Description                                                              |
| --------------------- | ------ | ------------------------------------------------------------------------ |
| Data Structure Length | 4      | unsigned integer of the number of remaining bytes in this data structure |
| Auth Mode             | 1      | Always 10 for this data structure                                        |
| Entity Name           |        | Name of the entity to authenticate (such as username)                    |
| Global ID             | 8      | unsigned 64-bit integer                                                  |

[Source Code](https://github.com/ceph/ceph/blob/main/src/mon/MonClient.cc#L1730)


## CephX Authorizer Auth Payload

| Field                 | Length | Description                                                              |
| --------------------- | ------ | ------------------------------------------------------------------------ |
| Data Structure Length | 4      | unsigned integer of the number of remaining bytes in this data structure |
| Auth Mode             | 1      | Always 1 for this data structure                                         |
| Global ID             | 8      | unsigned 64-bit integer                                                  |
| Service ID            | 4      | Entity type of entity to authenticate with                               |
| Ticket Blob Length    | 4      | unsigned 64-bit integer                                                  |
| Ticket Blob           |        | An encrypted Ticket Blob                                                 |
| Authorize Length      | 4      | unsigned 64-bit integer                                                  |
| Authorize             |        | An encrypted CephX Authorize                                             |

[Source Code](https://github.com/ceph/ceph/blob/main/src/auth/cephx/CephxProtocol.cc#L323)

# Entity Name

| Field       | Length | Description      |
| ----------- | ------ | ---------------- |
| Entity Type | 4      | unsigned integer |
| Entity Name |        | string           |

[Source Code](https://github.com/ceph/ceph/blob/main/src/include/msgr.h#L89)

# Ticket Blob

| Field                  | Length | Description                                |
| ---------------------- | ------ | ------------------------------------------ |
| Data Structure Version | 1      | Constant value (1)                         |
| Secret ID              | 8      | unsigned 64-bit integer                    |
| Blob Length            | 4      | unsigned integer of the length of the blob |
| Blob                   |        | Encoded Service Ticket Info                |

[Source Code](https://github.com/ceph/ceph/blob/main/src/auth/cephx/CephxProtocol.h#L118)

# Service Ticket Info

| Field                  | Length | Description            |
| ---------------------- | ------ | ---------------------- |
| Data Structure Version | 1      | Constant value (1)     | 
| Ticket                 |        | An encoded Auth Ticket |
| Session Key            |        | An encoded Crypto Key  |

[Source Code](https://github.com/ceph/ceph/blob/main/src/auth/cephx/CephxProtocol.h#L453)

# Auth Ticket

| Field                  | Length | Description                               |
| ---------------------- | ------ | ----------------------------------------- |
| Entity Name            |        | Name of entity that the ticket is for     |
| Global ID              | 8      | unsigned 64-bit integer                   |
| Created Time           | 8      | Time value when the ticket was created    |
| Renew After            | 8      | Time value when the ticket can be renewed |
| Expires                | 8      | Time value when the ticket expires        |
| Flags                  | 4      | unsigned 32-bit value (OR of the values)  |

[Source Code](https://github.com/ceph/ceph/blob/main/src/auth/Auth.h#L128)

# Crypto Key

| Field   | Length | Description                            |
| ------- | ------ | -------------------------------------- |
| Type    | 1      | Crypto Key Type                        |
| Created | 8      | A Time value - created time of the key |
| Secret  | 16     | Raw bytes for the crypo key            |

[Source Code](https://github.com/ceph/ceph/blob/main/src/auth/Crypto.h#L91)

# CephX Authorize

| Field                  | Length | Description                                                      |
| ---------------------- | ------ | ---------------------------------------------------------------- |
| Data Structure Version | 1      | Constant value (2)                                               |
| Nonce                  | 8      | unsigned 64-bit integer                                          |
| Have Challenge         | 1      | boolean value                                                    |
| Server Challenge + 1   | 8      | unsigned 64-bit integer of the server challenge incremented by 1 |

[Source Code](https://github.com/ceph/ceph/blob/main/src/auth/cephx/CephxProtocol.h#L510)

# Auth Reply More Payload

| Field                 | Length | Description                                                              |
| --------------------- | ------ | ------------------------------------------------------------------------ |
| Data Structure Length | 4      | unsigned integer of the number of remaining bytes in this data structure |
| Server Challenge      |        | An encoded CephX Server Challenge                                        |

[Source Code](https://github.com/ceph/ceph/blob/main/src/auth/cephx/CephxServiceHandler.cc#L296)

# CephX Server Challenge

| Field                  | Length | Description           |
| ---------------------- | ------ | --------------------- |
| Data Structure Version | 1      | Constant value (1)    |
| Data                   | 8      | Server challenge data |

[Source Code](https://github.com/ceph/ceph/blob/main/src/auth/cephx/CephxProtocol.h#L43)

# Auth Request More Payload

The data structure used depends on the authentication method that is used. The two options here are:
* CephX Mon Auth Request More Payload
* CephX Authorizer Request More Payload

# CephX Mon Auth Request More Payload

| Field                 | Length | Description                                                              |
| --------------------- | ------ | ------------------------------------------------------------------------ |
| Data Structure Length | 4      | unsigned integer of the number of remaining bytes in this data structure |
| Request Header        |        | An encoded CephX Request Header                                          |
| Authenticate          |        | An encoded CephX Authenticate                                            |

[Source Code](https://github.com/ceph/ceph/blob/main/src/auth/cephx/CephxClientHandler.cc#L46)

# CephX Request Header

| Field        | Length | Description                                 |
| ------------ | ------ | ------------------------------------------- |
| Request Type | 2      | unsigned 16-bit integer of the request type |

[Source Code](https://github.com/ceph/ceph/blob/main/src/auth/cephx/CephxProtocol.h#L71)

# CephX Authenticate

| Field            | Length | Description                                     |
| ---------------- | ------ | ----------------------------------------------- |
| Client Challenge | 8      | unsigned 64-bit integer of the client challenge |
| Key              | 8      | unsigned 64-bit integer of the key              |
| Old Ticket       |        | An encoded CephX Ticket Blob                    |
| Other Keys       | 4      | unsigned 32-bit integer of the other keys       |

[Source Code](https://github.com/ceph/ceph/blob/main/src/auth/cephx/CephxProtocol.h#L154)

# CephX Ticket Blob

| Field     | Length | Description                              |
| --------- | ------ | ---------------------------------------- |
| Version   | 1      | Data structure version (always 1)        |
| Secret ID | 8      | unsigned 64-bit integer of the secret ID |
| Blob      |        | encoded ticket data                      |

[Source Code](https://github.com/ceph/ceph/blob/main/src/auth/cephx/CephxProtocol.h#L118)

# CephX Authorizer Request More Payload

| Field             | Length | Description                                       |
| ----------------- | ------ | ------------------------------------------------- |
| Version           | 1      | Data structure version (always 1)                 |
| Global ID         | 8      | unsigned 64-bit integer                           |
| Service ID        | 4      | unsigned 32-bit integer                           |
| Ticket            |        | An encoded CephX Ticket Blob                      |
| Auth Message Size | 4      | unsigned 32-bit integer of length of Auth Message |
| Auth Message      |        | Encrypted auth message                            |

