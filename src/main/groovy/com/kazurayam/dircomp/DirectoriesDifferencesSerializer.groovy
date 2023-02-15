package com.kazurayam.dircomp

import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.ser.std.StdSerializer

class DirectoriesDifferencesSerializer
        extends StdSerializer<DirectoriesDifferences> {

    public DirectoriesDifferencesSerializer(Class<DirectoriesDifferences> t) {
        super(t)
    }

    @Override
    public void serialize(DirectoriesDifferences differences,
                          JsonGenerator jgen,
                          SerializerProvider serializerProvider) throws IOException {
        jgen.writeStartObject()
        jgen.writeArrayFieldStart("filesOnlyInA")
        jgen.writeArray
        jgen.writeEndArray()
        jgen.writeEndObject()
    }
}
