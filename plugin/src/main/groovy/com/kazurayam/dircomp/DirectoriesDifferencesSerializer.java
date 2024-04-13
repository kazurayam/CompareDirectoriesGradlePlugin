package com.kazurayam.dircomp;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;

import java.io.IOException;

public class DirectoriesDifferencesSerializer
        extends StdSerializer<DirectoriesDifferences> {
    private static final long serialVersionUID = 1L;

    public DirectoriesDifferencesSerializer() { this(null); }

    public DirectoriesDifferencesSerializer(Class clazz) { super(clazz); }

    @Override
    public void serialize(DirectoriesDifferences directoriesDifferences,
                          JsonGenerator jsonGenerator,
                          SerializerProvider serializer) throws IOException {
        jsonGenerator.writeStartObject();
        jsonGenerator.writeStringField("dirA",
                directoriesDifferences.getDirA().toString());
        jsonGenerator.writeStringField("dirB",
                directoriesDifferences.getDirB().toString());
        jsonGenerator.writeObjectField("filesOnlyInA",
                directoriesDifferences.getFilesOnlyInA());
        jsonGenerator.writeObjectField("filesOnlyInB",
                directoriesDifferences.getFilesOnlyInB());
        jsonGenerator.writeObjectField("intersection",
                directoriesDifferences.getIntersection());
        jsonGenerator.writeObjectField("modifiedFiles",
                directoriesDifferences.getModifiedFiles());
        jsonGenerator.writeEndObject();
    }
}