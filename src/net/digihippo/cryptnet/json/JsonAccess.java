package net.digihippo.cryptnet.json;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import net.digihippo.cryptnet.lang.IOConsumer;

import java.io.IOException;
import java.io.InputStream;

public final class JsonAccess
{
    private final JsonParser jParser;

    public JsonAccess(JsonParser jParser)
    {
        this.jParser = jParser;
    }

    public void skipToObjectEnd() throws IOException
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

    public void expectObjectKey(
            String fieldName,
            IOConsumer<JsonAccess> jsonParserConsumer) throws IOException
    {
        while (!fieldName.equals(jParser.currentName()) && jParser.hasCurrentToken())
        {
            jParser.nextFieldName();
        }

        if (jParser.hasCurrentToken())
        {
            jParser.nextToken();
            jsonParserConsumer.accept(this);
        }
    }

    public void expectArray(IOConsumer<JsonAccess> con) throws IOException
    {
        if (jParser.getCurrentToken() != JsonToken.START_ARRAY)
        {
            return;
        }

        while (jParser.nextToken() != JsonToken.END_ARRAY)
        {
            con.accept(this);
        }
    }

    public static JsonAccess begin(InputStream inputStream) throws IOException
    {
        JsonParser jParser = new JsonFactory().createParser(inputStream);
        jParser.nextToken();

        return new JsonAccess(jParser);
    }

    public void close() throws IOException
    {
        jParser.close();
    }

    public String getText() throws IOException
    {
        return jParser.getText();
    }

    public long getLongValue() throws IOException
    {
        return jParser.getLongValue();
    }

    public double getDoubleValue() throws IOException
    {
        return jParser.getDoubleValue();
    }
}
