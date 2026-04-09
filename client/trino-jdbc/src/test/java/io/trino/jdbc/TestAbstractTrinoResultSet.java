/*
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.trino.jdbc;

import com.google.common.collect.ImmutableList;
import io.trino.client.ClientTypeSignature;
import io.trino.client.ClientTypeSignatureParameter;
import io.trino.client.Column;
import org.junit.jupiter.api.Test;

import java.sql.SQLException;
import java.util.Arrays;
import java.util.List;

import static io.trino.jdbc.AbstractTrinoResultSet.DEFAULT_OBJECT_REPRESENTATION;
import static io.trino.jdbc.AbstractTrinoResultSet.TYPE_CONVERSIONS;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Fail.fail;

public class TestAbstractTrinoResultSet
{
    @Test
    public void testRepresentationImplemented()
    {
        DEFAULT_OBJECT_REPRESENTATION.forEach((sourceRawType, target) -> {
            if (!TYPE_CONVERSIONS.hasConversion(sourceRawType, target)) {
                fail(String.format("No conversion registered from %s to %s", sourceRawType, target));
            }
        });
    }

    @Test
    public void testVariantJsonFallbackReturnsString()
            throws SQLException
    {
        String json = "{\"a\":1}";
        try (InMemoryTrinoResultSet resultSet = new InMemoryTrinoResultSet(
                ImmutableList.of(variantColumn()),
                ImmutableList.of(ImmutableList.of(json)))) {
            assertThat(resultSet.next()).isTrue();

            assertThat(resultSet.getObject(1)).isEqualTo(json);
            assertThat(resultSet.getObject(1, Object.class)).isEqualTo(json);
            assertThat(resultSet.getObject(1, String.class)).isEqualTo(json);
            assertThat(resultSet.getString(1)).isEqualTo(json);
            assertThat(resultSet.getMetaData().getColumnClassName(1)).isEqualTo(Variant.class.getName());

            assertThatThrownBy(() -> resultSet.getObject(1, Variant.class))
                    .isInstanceOf(SQLException.class)
                    .hasMessage("Cannot convert VARIANT JSON text to class io.trino.jdbc.Variant");
            assertThatThrownBy(() -> resultSet.getObject(1, List.class))
                    .isInstanceOf(SQLException.class)
                    .hasMessage("Cannot convert VARIANT JSON text to interface java.util.List");
        }
    }

    @Test
    public void testNestedVariantJsonFallbackReturnsStrings()
            throws SQLException
    {
        String left = "{\"a\":1}";
        String right = "[1,2]";
        List<String> variantValues = Arrays.asList(left, right, null);
        try (InMemoryTrinoResultSet resultSet = new InMemoryTrinoResultSet(
                ImmutableList.of(arrayOfVariantColumn()),
                ImmutableList.of(ImmutableList.of(variantValues)))) {
            assertThat(resultSet.next()).isTrue();

            assertThat(resultSet.getObject(1, List.class)).isEqualTo(variantValues);
            assertThat((Object[]) resultSet.getArray(1).getArray()).containsExactly(left, right, null);
        }
    }

    private static Column variantColumn()
    {
        return new Column("variant", "variant", new ClientTypeSignature("variant"));
    }

    private static Column arrayOfVariantColumn()
    {
        return new Column(
                "variants",
                "array",
                new ClientTypeSignature("array", ImmutableList.of(ClientTypeSignatureParameter.ofType(new ClientTypeSignature("variant")))));
    }
}
