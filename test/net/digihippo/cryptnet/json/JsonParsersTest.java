package net.digihippo.cryptnet.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class JsonParsersTest
{
    @Test
    public void expectObjectKeyWhenTheKeyIsFirst() throws IOException
    {
        JsonFactory jfactory = new JsonFactory();
        JsonParser jParser = jfactory.createParser("{" +
                "   \"thingy\": " +
                "   {" +
                "   }" +
                "}");
        jParser.nextToken();

        AtomicBoolean called = new AtomicBoolean(false);
        JsonParsers.expectObjectKey(jParser, "thingy", jsonParser ->
        {
            assertThat(jsonParser.currentToken(), equalTo(JsonToken.START_OBJECT));
            called.set(true);
        });

        assertTrue(called.get());
    }

    @Test
    public void expectObjectKeyWhenTheKeyIsThirdInAFlatThing() throws IOException
    {
        JsonFactory jfactory = new JsonFactory();
        JsonParser jParser = jfactory.createParser("{" +
                "   \"one\": 1," +
                "   \"two\": 2," +
                "   \"thingy\": " +
                "   {" +
                "   }" +
                "}");
        jParser.nextToken();

        AtomicBoolean called = new AtomicBoolean(false);
        JsonParsers.expectObjectKey(jParser, "thingy", jsonParser ->
        {
            assertThat(jsonParser.currentToken(), equalTo(JsonToken.START_OBJECT));
            called.set(true);
        });

        assertTrue(called.get());
    }

    @Test
    public void expectObjectKeyWhenTheKeyIsAfterADeeplyNestedThing() throws IOException
    {
        JsonFactory jfactory = new JsonFactory();
        JsonParser jParser = jfactory.createParser("{" +
                "   \"one\": {" +
                "      \"two\": [2, 2]" +
                "   }," +
                "   \"thingy\": " +
                "   {" +
                "   }" +
                "}");
        jParser.nextToken();

        AtomicBoolean called = new AtomicBoolean(false);
        JsonParsers.expectObjectKey(jParser, "thingy", jsonParser ->
        {
            assertThat(jsonParser.currentToken(), equalTo(JsonToken.START_OBJECT));
            called.set(true);
        });

        assertTrue(called.get());
    }

    @Test
    public void expectObjectKeyWhenTheKeyIsNotPresent() throws IOException
    {
        JsonFactory jfactory = new JsonFactory();
        JsonParser jParser = jfactory.createParser("{" +
                "   \"one\": {" +
                "      \"two\": [2, 2]" +
                "   }," +
                "   \"not-thingy\": " +
                "   {" +
                "   }" +
                "}");
        jParser.nextToken();

        AtomicBoolean called = new AtomicBoolean(false);
        JsonParsers.expectObjectKey(jParser, "thingy", jsonParser ->
        {
            assertThat(jsonParser.currentToken(), equalTo(JsonToken.START_OBJECT));
            called.set(true);
        });

        assertFalse(called.get());
    }

    @Test
    public void expectArray() throws IOException
    {
        JsonFactory jfactory = new JsonFactory();
        JsonParser jParser = jfactory.createParser("[1, 2, 3]");
        jParser.nextToken();

        List<Long> elements = new ArrayList<>();
        JsonParsers.expectArray(jParser, jsonParser -> elements.add(jsonParser.getLongValue()));

        assertThat(elements, equalTo(Arrays.asList(1L, 2L, 3L)));
    }

    @Test
    public void expectEmptyArray() throws IOException
    {
        JsonFactory jfactory = new JsonFactory();
        JsonParser jParser = jfactory.createParser("[]");
        jParser.nextToken();

        List<Long> elements = new ArrayList<>();
        JsonParsers.expectArray(jParser, jsonParser -> elements.add(jsonParser.getLongValue()));

        assertThat(elements, equalTo(Collections.emptyList()));
    }
}