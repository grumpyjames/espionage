package net.digihippo.cryptnet.json;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JsonAccessTest
{
    @Test
    public void expectObjectKeyWhenTheKeyIsFirst() throws IOException
    {
        JsonAccess access = beginJsonAccess("{\"thingy\": 1}");

        AtomicBoolean called = new AtomicBoolean(false);
        access.expectObjectKey("thingy", inner ->
        {
            assertThat(inner.getLongValue(), equalTo(1L));
            called.set(true);
        });

        assertTrue(called.get());
    }

    @Test
    public void expectObjectKeyWhenTheKeyIsThirdInAFlatThing() throws IOException
    {
        JsonAccess access = beginJsonAccess("{" +
                "   \"one\": 1," +
                "   \"two\": 2," +
                "   \"thingy\": 5" +
                "}");

        AtomicBoolean called = new AtomicBoolean(false);
        access.expectObjectKey("thingy", jsonParser ->
        {
            assertThat(jsonParser.getLongValue(), equalTo(5L));
            called.set(true);
        });

        assertTrue(called.get());
    }

    @Test
    public void expectObjectKeyWhenTheKeyIsAfterADeeplyNestedThing() throws IOException
    {
        JsonAccess access = beginJsonAccess("{" +
                "   \"one\": {" +
                "      \"two\": [2, 2]" +
                "   }," +
                "   \"thingy\": 6" +
                "}");

        AtomicBoolean called = new AtomicBoolean(false);
        access.expectObjectKey("thingy", jsonParser ->
        {
            assertThat(jsonParser.getLongValue(), equalTo(6L));
            called.set(true);
        });

        assertTrue(called.get());
    }

    @Test
    public void expectObjectKeyWhenTheKeyIsNotPresent() throws IOException
    {
        JsonAccess access = beginJsonAccess("{" +
                "   \"one\": {" +
                "      \"two\": [2, 2]" +
                "   }," +
                "   \"not-thingy\": " +
                "   {" +
                "   }" +
                "}");

        AtomicBoolean called = new AtomicBoolean(false);
        access.expectObjectKey("thingy", jsonParser -> called.set(true));

        assertFalse(called.get());
    }

    @Test
    public void expectArray() throws IOException
    {
        JsonAccess jParser = beginJsonAccess("[1, 2, 3]");

        List<Long> elements = new ArrayList<>();
        jParser.expectArray(jsonParser -> elements.add(jsonParser.getLongValue()));

        assertThat(elements, equalTo(Arrays.asList(1L, 2L, 3L)));
    }

    @Test
    public void expectEmptyArray() throws IOException
    {
        JsonAccess jParser = beginJsonAccess("[]");

        List<Long> elements = new ArrayList<>();
        jParser.expectArray(jsonParser -> elements.add(jsonParser.getLongValue()));

        assertThat(elements, equalTo(Collections.emptyList()));
    }

    private static JsonAccess beginJsonAccess(String jsonStr) throws IOException
    {
        return JsonAccess.begin(new ByteArrayInputStream(jsonStr.getBytes(StandardCharsets.UTF_8)));
    }
}