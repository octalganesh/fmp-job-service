package com.octal.fsm.utils;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class TextUtilsTest {

    @Test
    void isEmpty_String_NullValue() {
        assertThat(TextUtils.isEmpty((String) null)).isTrue();
    }

    @Test
    void isEmpty_String_EmptyValue() {
        assertThat(TextUtils.isEmpty("")).isTrue();
    }

    @Test
    void isEmpty_String_NonEmptyValue() {
        assertThat(TextUtils.isEmpty("test")).isFalse();
    }

    @Test
    void isEmpty_Long_NullValue() {
        assertThat(TextUtils.isEmpty((Long) null)).isTrue();
    }

    @Test
    void isEmpty_Long_ZeroValue() {
        assertThat(TextUtils.isEmpty(0L)).isTrue();
    }

    @Test
    void isEmpty_Long_NegativeValue() {
        assertThat(TextUtils.isEmpty(-1L)).isTrue();
    }

    @Test
    void isEmpty_Long_PositiveValue() {
        assertThat(TextUtils.isEmpty(1L)).isFalse();
    }

    @Test
    void isEmpty_Integer_NullValue() {
        assertThat(TextUtils.isEmpty((Integer) null)).isTrue();
    }

    @Test
    void isEmpty_Integer_ZeroValue() {
        assertThat(TextUtils.isEmpty(0)).isTrue();
    }

    @Test
    void isEmpty_Integer_NegativeValue() {
        assertThat(TextUtils.isEmpty(-1)).isTrue();
    }

    @Test
    void isEmpty_Integer_PositiveValue() {
        assertThat(TextUtils.isEmpty(1)).isFalse();
    }

    @Test
    void isEmpty_Double_NullValue() {
        assertThat(TextUtils.isEmpty((Double) null)).isTrue();
    }

    @Test
    void isEmpty_Double_ZeroValue() {
        assertThat(TextUtils.isEmpty(0.0)).isTrue();
    }

    @Test
    void isEmpty_Double_NegativeValue() {
        assertThat(TextUtils.isEmpty(-1.0)).isTrue();
    }

    @Test
    void isEmpty_Double_PositiveValue() {
        assertThat(TextUtils.isEmpty(1.0)).isFalse();
    }

    @Test
    void isEmptyWithOutZero_Double_NullValue() {
        assertThat(TextUtils.isEmptyWithOutZero(null)).isTrue();
    }

    @Test
    void isEmptyWithOutZero_Double_ZeroValue() {
        assertThat(TextUtils.isEmptyWithOutZero(0.0)).isFalse();
    }

    @Test
    void isEmptyWithOutZero_Double_NegativeValue() {
        assertThat(TextUtils.isEmptyWithOutZero(-1.0)).isTrue();
    }

    @Test
    void isEmptyWithOutZero_Double_PositiveValue() {
        assertThat(TextUtils.isEmptyWithOutZero(1.0)).isFalse();
    }
}