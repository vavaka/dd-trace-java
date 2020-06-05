package com.datadog.profiling.jfr;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

class ResolvableTypeTest {
  private static final String FIELD_NAME = "field";

  private ResolvableType resolvableType;
  private final String targetTypeName = "custom.Type";
  private Metadata metadata;
  private Types types;
  private Type targetType;

  @BeforeEach
  void setUp() {
    ConstantPools constantPools = new ConstantPools();
    metadata = new Metadata(constantPools);
    types = new Types(metadata);
    resolvableType = new ResolvableType(targetTypeName, metadata);
  }

  @Test
  void isResolved() {
    assertFalse(resolvableType.isResolved());
    resolve();
    assertTrue(resolvableType.isResolved());
  }

  @Test
  void getId() {
    assertThrows(IllegalStateException.class, () -> resolvableType.getId());
    resolve();
    assertEquals(targetType.getId(), resolvableType.getId());
  }

  @Test
  void hasConstantPool() {
    assertThrows(IllegalStateException.class, () -> resolvableType.hasConstantPool());
    resolve();
    assertEquals(targetType.hasConstantPool(), resolvableType.hasConstantPool());
  }

  @Test
  void asValueByte() {
    assertThrows(IllegalStateException.class, () -> resolvableType.asValue((byte) 1));
    resolve();
    // a resolvable type is always a complex/custom type so `asValue(byte)` will always fail
    assertThrows(IllegalArgumentException.class, () -> resolvableType.asValue((byte) 1));
  }

  @Test
  void asValueChar() {
    assertThrows(IllegalStateException.class, () -> resolvableType.asValue((char) 1));
    resolve();
    // a resolvable type is always a complex/custom type so `asValue(char)` will always fail
    assertThrows(IllegalArgumentException.class, () -> resolvableType.asValue((char) 1));
  }

  @Test
  void asValueShort() {
    assertThrows(IllegalStateException.class, () -> resolvableType.asValue((short) 1));
    resolve();
    // a resolvable type is always a complex/custom type so `asValue(short)` will always fail
    assertThrows(IllegalArgumentException.class, () -> resolvableType.asValue((short) 1));
  }

  @Test
  void asValueInt() {
    assertThrows(IllegalStateException.class, () -> resolvableType.asValue((int) 1));
    resolve();
    // a resolvable type is always a complex/custom type so `asValue(int)` will always fail
    assertThrows(IllegalArgumentException.class, () -> resolvableType.asValue((int) 1));
  }

  @Test
  void asValueLong() {
    assertThrows(IllegalStateException.class, () -> resolvableType.asValue((long) 1));
    resolve();
    // a resolvable type is always a complex/custom type so `asValue(long)` will always fail
    assertThrows(IllegalArgumentException.class, () -> resolvableType.asValue((long) 1));
  }

  @Test
  void asValueFloat() {
    assertThrows(IllegalStateException.class, () -> resolvableType.asValue((float) 1));
    resolve();
    // a resolvable type is always a complex/custom type so `asValue(float)` will always fail
    assertThrows(IllegalArgumentException.class, () -> resolvableType.asValue((float) 1));
  }

  @Test
  void asValueDouble() {
    assertThrows(IllegalStateException.class, () -> resolvableType.asValue((double) 1));
    resolve();
    // a resolvable type is always a complex/custom type so `asValue(double)` will always fail
    assertThrows(IllegalArgumentException.class, () -> resolvableType.asValue((double) 1));
  }

  @Test
  void asValueBoolean() {
    assertThrows(IllegalStateException.class, () -> resolvableType.asValue(true));
    resolve();
    // a resolvable type is always a complex/custom type so `asValue(boolean)` will always fail
    assertThrows(IllegalArgumentException.class, () -> resolvableType.asValue(true));
  }

  @Test
  void asValueString() {
    assertThrows(IllegalStateException.class, () -> resolvableType.asValue("1"));
    resolve();
    // a resolvable type is always a complex/custom type so `asValue(String)` will always fail
    assertThrows(IllegalArgumentException.class, () -> resolvableType.asValue("1"));
  }

  @Test
  void asValueCustom() {
    assertThrows(IllegalStateException.class, () -> resolvableType.asValue(v -> {}));
    resolve();
    assertEquals(targetType.asValue(v -> {}), resolvableType.asValue(v -> {}));
  }

  @Test
  void nullValue() {
    assertThrows(IllegalStateException.class, () -> resolvableType.nullValue());
    resolve();
    assertEquals(targetType.nullValue(), resolvableType.nullValue());
  }

  @Test
  void getConstantPool() {
    assertThrows(IllegalStateException.class, () -> resolvableType.getConstantPool());
    resolve();
    assertEquals(targetType.getConstantPool(), resolvableType.getConstantPool());
  }

  @Test
  void getMetadata() {
    assertThrows(IllegalStateException.class, () -> resolvableType.getMetadata());
    resolve();
    assertEquals(targetType.getMetadata(), resolvableType.getMetadata());
  }

  @Test
  void isBuiltin() {
    assertThrows(IllegalStateException.class, () -> resolvableType.isBuiltin());
    resolve();
    assertEquals(targetType.isBuiltin(), resolvableType.isBuiltin());
  }

  @Test
  void isSimple() {
    assertThrows(IllegalStateException.class, () -> resolvableType.isSimple());
    resolve();
    assertEquals(targetType.isSimple(), resolvableType.isSimple());
  }

  @Test
  void getSupertype() {
    assertThrows(IllegalStateException.class, () -> resolvableType.getSupertype());
    resolve();
    assertEquals(targetType.getSupertype(), resolvableType.getSupertype());
  }

  @Test
  void getFields() {
    assertThrows(IllegalStateException.class, () -> resolvableType.getFields());
    resolve();
    assertEquals(targetType.getFields(), resolvableType.getFields());
  }

  @Test
  void getField() {
    assertThrows(IllegalStateException.class, () -> resolvableType.getField(FIELD_NAME));
    resolve();
    assertEquals(targetType.getField(FIELD_NAME), resolvableType.getField(FIELD_NAME));
  }

  @Test
  void getAnnotations() {
    assertThrows(IllegalStateException.class, () -> resolvableType.getAnnotations());
    resolve();
    assertEquals(targetType.getAnnotations(), resolvableType.getAnnotations());
  }

  @Test
  void canAccept() {
    assertThrows(IllegalStateException.class, () -> resolvableType.canAccept("value"));
    resolve();
    assertEquals(targetType.canAccept("value"), resolvableType.canAccept("value"));
  }

  @Test
  void getTypeName() {
    assertEquals(targetTypeName, resolvableType.getTypeName());
    resolve();
    assertEquals(targetType.getFields(), resolvableType.getFields());
  }

  @Test
  void isSame() {
    assertThrows(IllegalStateException.class, () -> resolvableType.isSame(Types.JDK.CLASS_LOADER));
    resolve();
    assertEquals(
        targetType.isSame(Types.JDK.CLASS_LOADER), resolvableType.isSame(Types.JDK.CLASS_LOADER));
  }

  @Test
  void isUsedBy() {
    Type otherType = types.getType(Types.JDK.CLASS_LOADER);
    assertThrows(IllegalStateException.class, () -> resolvableType.isUsedBy(otherType));
    resolve();
    assertEquals(targetType.isUsedBy(otherType), resolvableType.isUsedBy(otherType));
  }

  /**
   * Resolvable type is supposed to work with {@linkplain BaseType} subclasses only. This test
   * asserts that a resolvable will not be resolved to anything that is not a {@linkplain BaseType}
   * subclass.
   */
  @Test
  void wrongTypeResolve() {
    Metadata mockedMetadata = Mockito.mock(Metadata.class);
    Mockito.when(
            mockedMetadata.getType(ArgumentMatchers.anyString(), ArgumentMatchers.anyBoolean()))
        .thenReturn(new InvalidType());

    assertFalse(resolvableType.resolve());
  }

  private void resolve() {
    List<TypedField> fields =
        Collections.singletonList(new TypedField(types.getType(Types.Builtin.STRING), FIELD_NAME));

    targetType =
        metadata.registerType(
            targetTypeName, null, () -> new TypeStructure(fields, Collections.emptyList()));
    metadata.resolveTypes();
  }
}