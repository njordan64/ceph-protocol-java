# Hello

Report the node type of the current client to the remote server. Also report the current host's IP address (either IPv4 or IPv6).

Number of Segments: 1

## Segment 1 Format

| Field           | Length          | Description                                           |
| --------------- | --------------- | ----------------------------------------------------- |
| Node Type       | 1               | Types of node (OR of all applicable node type values) |
| Address         | Remaining Bytes | IPv4 or IPv6 address                                  |

[Source Code](https://github.com/ceph/ceph/blob/main/src/msg/async/frames_v2.h#L512)

# Auth Request

Initiates the authentication process. Specifies the supported auth methods as well as provides some information needed to start the auth process.

Number of Segments: 1

## Segment 1 Format

| Field           | Length      | Description                   |
| --------------- | ----------- | ----------------------------- |
| Auth Method     | 4           | unsigned integer              |
| Preferred Modes | 4 * (n + 1) | list of unsigned integers (1) |
| Payload         |             | Auth request payload          |

1. First four bytes are an unsigned integer of the length of the list. Remaining bytes are the values.

[Source Code](https://github.com/ceph/ceph/blob/main/src/msg/async/frames_v2.h#L526)

# Auth Bad Method

Sent by the server when the client and server cannot agree on an auth method or mode.

Number of Segments: 1

## Segment 1 Format

| Field           | Length      | Description                                        |
| --------------- | ----------- | -------------------------------------------------- |
| Method          | 4           | unsigned integer - Auth method that was requested  |
| Result          | 4           | unsigned integer                                   |
| Allowed Methods | 4 * (n + 1) | list of unsigned integers - valid auth methods (1) |
| Allowed Modes   | 4 * (n + 1) | list of unsigned integers - valid auth modes (1)   |

1. First four bytes are an unsigned integer of the length of the list. Remaining bytes are the values.

[Source Code](https://github.com/ceph/ceph/blob/main/src/msg/async/frames_v2.h#L542)

# Auth Reply More

Sent by the server provide more information to the client for it to continue authentication.

Number of Segments: 1

| Field   | Length | Description             |
| --------| ------ | ----------------------- |
| Payload |        | Auth reply more payload |

[Source Code](https://github.com/ceph/ceph/blob/main/src/msg/async/frames_v2.h#L560)

# Auth Request More

Sent from the client to the server to continue the authentication process.

Number of Segments: 1

## Segment 1 Format

| Field   | Length | Description               |
| ------- | ------ | ------------------------- |
| Payload |        | Auth request more payload |

[Source Code](https://github.com/ceph/ceph/blob/main/src/msg/async/frames_v2.h#L572)
