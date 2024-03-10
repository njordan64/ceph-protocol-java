# Ceph Java Client

A pure Java Ceph client.

## Current Status

* Can authenticate using the CephX authentication mechanism
* Able to parse and encode all control frame types
* Able to handle encrypting/decrypting messages after switching to
secure mode
* For message control frames (type 17) only `GetMonMap` and
`MonMap` are supported

## Design

* Uses Netty for asynchronous I/O
* Data structures that are encoded/decoded are simple Java Beans
with custom annotations
* The custom annotations are used to generate Java classes that
handle encoding/decoding to Netty `ByteBuf` objects

## Future Ideas

* Idealy the C++ structs/classes that are used for the Ceph protocol
could automatically be converted to the Java bean classes with the
custom annotations. This may require changes to the core Ceph code.
* Create an integration test suite