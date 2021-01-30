package net.digihippo.cryptnet.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import net.digihippo.cryptnet.lang.IOConsumer;

import java.io.IOException;
import java.io.InputStream;

public class JsonParsers
{
    public static void skipToObjectEnd(JsonParser jParser) throws IOException
    {
        int stackCount = 1;
        while (stackCount > 0)
        {
            JsonToken jsonToken = jParser.nextToken();
            if (jsonToken == JsonToken.START_OBJECT || jsonToken == JsonToken.START_ARRAY)
            {
                stackCount++;
            }
            else if (jsonToken == JsonToken.END_OBJECT || jsonToken == JsonToken.END_ARRAY)
            {
                stackCount--;
            }
        }
    }

    public static void expectObjectKey(
            JsonParser jParser,
            String fieldName,
            IOConsumer<JsonParser> jsonParserConsumer) throws IOException
    {
        while (!fieldName.equals(jParser.currentName()) && jParser.hasCurrentToken())
        {
            jParser.nextFieldName();
        }

        if (jParser.hasCurrentToken())
        {
            jParser.nextToken();
            jsonParserConsumer.accept(jParser);
        }
    }

    public static void expectArray(JsonParser jParser, IOConsumer<JsonParser> con) throws IOException
    {
        if (jParser.getCurrentToken() != JsonToken.START_ARRAY)
        {
            return;
        }

        while (jParser.nextToken() != JsonToken.END_ARRAY)
        {
            con.accept(jParser);
        }
    }

    public static JsonParser begin(InputStream inputStream) throws IOException
    {
        JsonParser jParser = new JsonFactory().createParser(inputStream);
        jParser.nextToken();

        return jParser;
    }
}
