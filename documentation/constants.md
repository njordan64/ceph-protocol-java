# Node Types

| Code | Name      |
| ---- | --------- |
| 1    | MON node  |
| 2    | MDS node  |
| 4    | OSD node  |
| 8    | Client    |
| 16   | MGR node  |
| 32   | Auth node |
| 255  | Any       |

[Source Code](https://github.com/ceph/ceph/blob/main/src/include/msgr.h#L89)

# Auth Method

| Name    | Value |
| ------- | ----- |
| Unknown | 0     |
| None    | 1     |
| CephX   | 2     |
| GSS     | 4     |

[Source Code](https://github.com/ceph/ceph/blob/main/src/include/ceph_fs.h#L100)

# Preferred Modes

| Name    | Value |
| ------- | ----- |
| Unknown | 0     |
| CRC     | 1     |
| Secure  | 2     |

[Source Code](https://github.com/ceph/ceph/blob/main/src/include/ceph_fs.h#L105)

# Auth Modes

| Name           | Value |
| -------------- | ----- |
| None           | 0     |
| Authorizer     | 1     |
| Authorizer Max | 9     |
| Mon            | 10    |
| Mon Max        | 19    |

# Entity Type

| Name   | Value |
| ------ | ----- |
| MON    | 1     |
| MDS    | 2     |
| OSD    | 4     |
| CLIENT | 8     |
| MGR    | 16    |
| AUTH   | 32    |
| ANY    | 255   |

# Crypto Key Type

| Name | Value |
| ---- | ----- |
| None | 0     |
| AES  | 1     |
