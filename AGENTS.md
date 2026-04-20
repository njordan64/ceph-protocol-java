# AGENTS.md

## Project Overview
A pure Java Ceph client using Netty for async I/O. Data structures use custom annotations (`@CephType`, `@CephField`) that generate encoder/decoder code at compile time.

## Build & Run
```bash
mvn clean install
```

**Java 17 required**. Module path is used (`--add-reads` in compiler config).

## Module Structure
- `types/` - Protocol types (Lombok processed)
- `annotations/` - Custom encoding annotations
- `encoding-annotation-processor/` - Annotation processor generating encoder/decoder classes
- `protocol/` - Core protocol encoding/decoding (uses annotation processor)
- `utils/` - Utility classes
- `client/` - Netty-based client implementation
- `analysis/` - Analysis tools (Jackson-based)

## Key Dependencies
- Netty 4.1.113.Final (buffer, codec, common)
- Lombok 1.18.34 (processor path)
- SLF4J 2.0.16 + Logback 1.5.8
- JUnit 5.11.0

## Testing
```bash
mvn test
```

Tests are JUnit 5 based. Run per-module with `-pl <module>`.

## Annotation Processor Usage
- Classes annotated with `@CephType` trigger code generation
- Generated encoder/decoder classes are placed in same package
- Annotation processor must run before compilation (configured in `maven-compiler-plugin`)
- For `protocol/` module, explicitly add processor path:
  - `encoding-annotation-processor` 
  - `lombok`

## Architecture Highlights
1. **Encoding**: Custom annotations define how fields map to Ceph binary protocol
2. **Code Generation**: `EncodingAnnotationProcessor` generates `CephEncoder` and `CephDecoder` implementations
3. **Client**: `CephNettyClient` handles CephX auth and mon map retrieval
4. **Pipeline**: Netty handlers chain: Banner → Frame Codec → Hello → Auth → Compression → ServerIdent → GeneralMessage

## Known Limitations
- Only `GetMonMap` and `MonMap` message types (type 17) supported in control frames
- No integration test suite yet
