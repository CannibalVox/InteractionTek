package com.voxtech.validators;

import com.hypixel.hytale.codec.schema.SchemaContext;
import com.hypixel.hytale.codec.schema.config.Schema;
import com.hypixel.hytale.codec.validation.ValidationResults;
import com.hypixel.hytale.codec.validation.Validator;

public class ValueOr<T> implements Validator<T> {
    private final Validator<T> inner;
    private final T value;

    public ValueOr(T value, Validator<T> inner) {
        this.value = value;
        this.inner = inner;
    }

    @Override
    public void accept(T t, ValidationResults validationResults) {
        if (t == null && value == null) {
            return;
        }

        if (t != null && t.equals(value)) {
            return;
        }

        this.inner.accept(t, validationResults);
    }

    @Override
    public void updateSchema(SchemaContext schemaContext, Schema schema) {
        this.inner.updateSchema(schemaContext, schema);
    }
}
