//
// MessagePack for Java
//
//    Licensed under the Apache License, Version 2.0 (the "License");
//    you may not use this file except in compliance with the License.
//    You may obtain a copy of the License at
//
//        http://www.apache.org/licenses/LICENSE-2.0
//
//    Unless required by applicable law or agreed to in writing, software
//    distributed under the License is distributed on an "AS IS" BASIS,
//    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
//    See the License for the specific language governing permissions and
//    limitations under the License.
//
package org.msgpack.value;

import static org.junit.Assert.assertEquals;

import java.nio.charset.StandardCharsets;
import org.junit.Test;

/**
  *
  */
public class TestValueFactory {
    @Test
    public void testNil() {
        assertValid(ValueType.NIL, true, false, false, false, false, false, false, false, false, false, false, ValueFactory.newNil());
    }

    @Test
    public void testBoolean() {
        assertValid(ValueType.BOOLEAN, false, true, false, false, false, false, false, false, false, false, false, ValueFactory.newBoolean(true));
        assertValid(ValueType.BOOLEAN, false, true, false, false, false, false, false, false, false, false, false, ValueFactory.newBoolean(false));
    }

    @Test
    public void testInteger() {
        assertValid(ValueType.INTEGER, false, false, true, false, false, false, false, false, false, false, true, ValueFactory.newInteger(1234));
        assertValid(ValueType.INTEGER, false, false, true, false, false, false, false, false, false, false, true, ValueFactory.newInteger(0));
        assertValid(ValueType.INTEGER, false, false, true, false, false, false, false, false, false, false, true, ValueFactory.newInteger(-2184901));
    }

    @Test
    public void testFloat() {
        assertValid(ValueType.FLOAT, false, false, false, false, true, false, false, false, false, false, true, ValueFactory.newFloat(1234.124));
        assertValid(ValueType.FLOAT, false, false, false, false, true, false, false, false, false, false, true, ValueFactory.newFloat(0.0));
        assertValid(ValueType.FLOAT, false, false, false, false, true, false, false, false, false, false, true, ValueFactory.newFloat(-0.0));
        assertValid(ValueType.FLOAT, false, false, false, false, true, false, false, false, false, false, true, ValueFactory.newFloat(1.23e-10));
    }

    @Test
    public void testString() {
        assertValid(ValueType.STRING, false, false, false, true, false, false, false, false, false, true, false, ValueFactory.newString("hoge"));
        assertValid(ValueType.STRING, false, false, false, true, false, false, false, false, false, true, false, ValueFactory.newString(""));
    }

    @Test
    public void testBinary() {
        assertValid(ValueType.BINARY, false, false, false, false, false, true, false, false, false, true, false, ValueFactory.newBinary("hoge".getBytes(StandardCharsets.UTF_8)));
        assertValid(ValueType.BINARY, false, false, false, false, false, true, false, false, false, true, false, ValueFactory.newBinary(new byte[0]));
    }

    @Test
    public void testArray() {
        assertValid(ValueType.ARRAY, false, false, false, false, false, false, true, false, false, false, false, ValueFactory.emptyArray());
    }

    @Test
    public void testMap() {
        assertValid(ValueType.MAP, false, false, false, false, false, false, false, true, false, false, false, ValueFactory.emptyMap());
    }

    @Test
    public void testExtension() {
        assertValid(ValueType.EXTENSION, false, false, false, false, false, false, false, false, true, false, false, ValueFactory.newExtension((byte) 0, "hoge".getBytes(StandardCharsets.UTF_8)));
    }

    private static void assertValid(
            final ValueType expected,
            final boolean isNil,
            final boolean isBoolean,
            final boolean isInteger,
            final boolean isString,
            final boolean isFloat,
            final boolean isBinary,
            final boolean isArray,
            final boolean isMap,
            final boolean isExtension,
            final boolean isRaw,
            final boolean isNumber,
            final Value v) {
        assertEquals(isNil, v.isNilValue());
        assertEquals(isBoolean, v.isBooleanValue());
        assertEquals(isInteger, v.isIntegerValue());
        assertEquals(isFloat, v.isFloatValue());
        assertEquals(isString, v.isStringValue());
        assertEquals(isBinary, v.isBinaryValue());
        assertEquals(isArray, v.isArrayValue());
        assertEquals(isMap, v.isMapValue());
        assertEquals(isExtension, v.isExtensionValue());
        assertEquals(isRaw, v.isRawValue());
        assertEquals(isNumber, v.isNumberValue());
    }
}
