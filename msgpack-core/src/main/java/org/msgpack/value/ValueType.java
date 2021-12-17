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

/**
 * Representation of MessagePack types.
 * <p>
 * MessagePack uses hierarchical type system. Integer and Float are subypte of Number, Thus {@link #isNumberType()}
 * returns true if type is Integer or Float.
 */
public enum ValueType
{
    NIL(false),
    BOOLEAN(false),
    INTEGER(true),
    FLOAT(true),
    STRING(false),
    ARRAY(false),
    MAP(false),
    ;

    private final boolean numberType;

    private ValueType(boolean numberType)
    {
        this.numberType = numberType;
    }

    public boolean isNilType()
    {
        return this == NIL;
    }

    public boolean isBooleanType()
    {
        return this == BOOLEAN;
    }

    public boolean isNumberType()
    {
        return numberType;
    }

    public boolean isIntegerType()
    {
        return this == INTEGER;
    }

    public boolean isFloatType()
    {
        return this == FLOAT;
    }

    public boolean isStringType()
    {
        return this == STRING;
    }

    public boolean isArrayType()
    {
        return this == ARRAY;
    }

    public boolean isMapType()
    {
        return this == MAP;
    }
}
