# CephX Authentication

Ceph uses a custom authentication mechanism known as CephX Authentication. CephX covers various
authentication scenarios including service-to-service and client-to-service.

## Definitions

### Client

A program that would like to gain authorization to the Ceph
services. It will initiate the authenticaton process and will
later make requests to Ceph services.

### Server

A Ceph service (Mon) that is able to authenticate a user and
issue tickets for them.

### Proof Initialization Vector

A constant byte array that has the ASCII value `cephsageyudagreg`.

### Magic Bytes

A constant byte array that is used for initializing cryptography operations. The value is:
```
55 aa 26 88 ad 9c 00 ff
```

## Authentication Flow

1. Client initiates authentication
2. Server requests more information and provides server generated data
3. Client finishes authentication using server and client generated data
4. Server reports that authentication is complete
5. Server sends an authentication signature
6. Client validates server authentication signature and returns the client authentication signature
7. Server validates client authentication signature

### Note

The client and server must keep track of all bytes sent and received until authentication completes.
These will be used later to generate and validae each other's signatures. This includes all messages
up to and including `AuthDone`.

## Client to Service Authentication

### Client Starts Authentication

Frame Type: AuthRequest

The client needs to specify the auth method to use as well as the supported security modes. The auth
method is always `2` (CephX authentication). The security modes are how messages are secured after
authentication completes. A message can be secured with CRC32 checksums or the control frames can
be encrypted.

CRC32 Checksums - Use the value `1`
Encrypt Control Frames - Use the value `2`

The payload for the request consists of three items:
1. Auth Mode - Set to `10` to indicate client to service authentication
2. Entity Name - Used to provide the username. Set the type to `8`.
3. Global ID - Set to `0`

### Server Generates Challenge

Frame Type: AuthReplyMore

The server will process the initial auth request message. If the message was valid, then the server
will generate a challenge for the client. The payload for this message is a `CephXServerChallenge`.
The challenge data is a random byte array of length 32.

### Client Generates Challenge

Frame Type: AuthRequestMore

The client will use the server challenge to generate a client challenge. The client can also supply
its previous ticket information.

The payload consists of two parts:
1. Request Header
2. Authentication data

#### Request Header

This is just a wrapper for 2 byte integer value. Use the value `0x100`.

#### Authentication Data

The client needs to generate generate some data to prove that it has the user's key and for the
server to be able to verify this.

Start by generating the client challenge. This is a random byte array of length 8.

Next step is to generate the proof. Start with generating a byte array of length 32 that will later
get encrypted. The structure of the byte array is:

| Bytes | Value                                                    |
| ----- | -------------------------------------------------------- |
| 1     | 1                                                        |
| 2-9   | Magic Bytes                                              |
| 10-17 | Server Challenge                                         |
| 18-25 | Client Challenge                                         |
| 26-32 | Each byte has the value `7` (the length of this padding) |

The byte array needs to be encrypted using these settings:
* Algorithm - AES/CBC/NoPadding
* Key - The auth key for the user. This is analogous to the password for the user and is usually
stored locally.
* Initialization Vector - Use the proof initialization vector

Finally the client needs to apply XOR over this encrypted value. The value to XOR will be a byte
array of length 36. Start by copying the encrypted value into the value to XOR starting from
byte 4. The first 4 bytes must be set to `20 00 00 00` in hex. This is the length of the
encrypted value as a little endian 4 byte integer.

The final value is a byte array of length 8. This is the java code to apply XOR:
```java
for (int i = 0; i - (i % 8) + 8 < valueToXor.length; i++) {
    proof[i % 8] ^= valueToXor[i];
}
```

Note: The code fragment above may look incorrect, but this is how Ceph expects the proof
to be generated.

Finally the client can generate the authentication data. This is a `CephXAuthenticate` data
structure.
* Client Challenge - Use the client challenge generated above
* Key - The proof value generated above
* Old Ticket - Fill in with the old ticket if you have one, otherwise use a secret ID of 0 and
and empty byte array for the blob.
* Other Keys - Set to the constant `32`

### Server Completes Authentication

Frame Type: AuthDone

If the client was able to successfully prove that it has the user's key, then the server will
complete authentication. The server will also send over the session key to use for subsequent
messages if the client requested to encrypt control frames.

The response will contain 3 items:
1. Global ID - Global ID of the connection
2. Connection Mode - The connection mode that will be used for subsequent messages
3. Auth Done Payload - The encoded payload. A CephX Mon Auth Done data structure.

#### Auth Done Payload

The payload consists of the following information:
1. Response Header - Contains the status of authentication request
2. Version - Constant, current set to `1`
3. List of Tickets Infos - A list of ticket infos issued by the server
4. Encrypted Secret - If changing to secure mode, then this contains the encryption key as well
as the Rx and Tx nonce bytes
5. Extra Data - A byte array of extra data from the server

The response header will have a response type value of `1` and a status value of `5`.

#### Preparing to Switch to Secure Mode

The client needs to look at the last item in the list of ticket infos. It contains a service
ticket that is encrypted with the user's key. THe service ticket is used to decrypt the key,
Rx and Tx nonce bytes.

To decrypt the service ticket, use these settings:
* Algorithm - `AES/CBC/NoPadding`
* Key - User's key
* Initialization Vector - Use Proof Initialization Vector

The decrypted service ticket can be parsed by treating it as a `CephXServiceTicket` and skipping
the first 9 bytes. The `CephXServiceTicket` contains a session key. Create an AES key from the
session key.

The secure mode parameters are encrypted in the encypted secret field of the payload. To decrypt
it, use these settins:
* Algorithm - `AES/CBC/NoPadding`
* Key - session key
* Initialization Vector - Proof Initialization Vector

To extract the secure mode parameters from the decrypted value:

| Bytes | Description                               |
| ----- | ----------------------------------------- |
| 0-13  | Not needed                                |
| 14-29 | AES encryption key used to decrypt frames |
| 30-41 | Rx Nonce Bytes                            |
| 42-53 | Tx Nonce Bytes                            |

This is the last unencrypted frame, all subsequent frames are encypted using the AES key and
nonce bytes from above. The nonce bytes are incremented after each encryption/decryption operation.

### Server Sends Server Signature

Frame Type: AuthSignatureFrame

If client and server agreed to switch to secure mode, then this frame is encrypted.

The payload is only the 32 bytes of the SHA256 hash of bytes sent by the server. The client must
validate that this matches the SHA256 hash of the bytes received by the client.

### Client Sends Server Signature

Frame Type: AuthSignatureFrame

If client and server agreed to switch to secure mode, then this frame is encrypted.

The payload is only the 32 bytes of the SHA256 hash of bytes sent by the client. The server must
validate that this matches the SHA256 hash of the bytes received by the server.

## Service to Service Authentication

### Client Starts Authentication

Frame Type: AuthRequest

The client needs to specify the auth method to use as well as the supported security modes. The auth
method is always `2` (CephX authentication). The security modes are how messages are secured after
authentication completes. A message can be secured with CRC32 checksums or the control frames can
be encrypted.

CRC32 Checksums - Use the value `1`
Encrypt Control Frames - Use the value `2`

The payload for the request consists of five items:
1. Auth Mode - Set to `1` to indicate service to service authentication
2. Global ID - Use the value `0`
3. Service ID - The node type ID of the client

Next the request contains a `CephXTicketBlob`. Use `-1` for the secret ID. The blob is
a `CephXServiceTicketInfo`. This is a previously obtained ticket. The ticket contains a session key.

Finally the request contains an encrypted `CephXAuthorize` blob. It is encrypted using the session
key from the ticket. The `CephXAuthorize` blob contains the nonce information.

### Server Generates Challenge

Frame Type: AuthReplyMore

The server will process the initial auth request message. If the message was valid, then the server
will generate a challenge for the client. The payload for this message is a `CephXServerChallenge`.
The challenge data is a random byte array of length 32.

### Client Generates Challenge

Frame Type: AuthRequestMore

The client provides more information to continue authentication. The payload contains the following
information.
* Global ID - Use `0`
* Service ID - Use the proper service ID for the client
* Ticket - A `CephXTicketBlob`, the same as from the original request
* Encrypted Authorize Message - An encrypted `CephXAuthorize`. It is encrypted with the session key.
It contains the nonce and the server challenge plus one.

### Server Finishes Authentication

Frame Type: AuthDone

If all information provided by the client was valid, then the server will complete authentication.
The response contains:
* Global ID - Use the value `0`
* Connection Mode - Use the value for the connection mode that is used for all subsequent messages.
* Payload - An encrypted `CephXAuthorizeReply`. It is encrypted with the session key.

The secure mode parameters are in the connection secret field of the `CephXAuthorizeReply`.

To extract the secure mode parameters from the connection secret:

| Bytes | Description                               |
| ----- | ----------------------------------------- |
| 0-13  | Not needed                                |
| 14-29 | AES encryption key used to decrypt frames |
| 30-41 | Rx Nonce Bytes                            |
| 42-53 | Tx Nonce Bytes                            |

This is the last unencrypted frame, all subsequent frames are encypted using the AES key and
nonce bytes from above. The nonce bytes are incremented after each encryption/decryption operation.

### Server Sends Server Signature

Frame Type: AuthSignatureFrame

If client and server agreed to switch to secure mode, then this frame is encrypted.

The payload is only the 32 bytes of the SHA256 hash of bytes sent by the server. The client must
validate that this matches the SHA256 hash of the bytes received by the client.

### Client Sends Server Signature

Frame Type: AuthSignatureFrame

If client and server agreed to switch to secure mode, then this frame is encrypted.

The payload is only the 32 bytes of the SHA256 hash of bytes sent by the client. The server must
validate that this matches the SHA256 hash of the bytes received by the server.
